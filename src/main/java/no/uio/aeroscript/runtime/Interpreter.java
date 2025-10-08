package no.uio.aeroscript.runtime;

import no.uio.aeroscript.antlr.AeroScriptBaseVisitor;
import no.uio.aeroscript.antlr.AeroScriptParser;
import no.uio.aeroscript.ast.expr.*;
import no.uio.aeroscript.ast.stmt.*;
import no.uio.aeroscript.type.*;

import java.util.*;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/* For now, let us just ignore interrupts, will need to build
 * an interrupt lookup table and introduce a message queue later... */
public class Interpreter extends AeroScriptBaseVisitor<Object> {
    private HashMap<Memory, Object> heap;
    private Stack<Statement> stack;
    private HashMap<String, ArrayList<Statement>> methodTable = new HashMap<>();

    public Interpreter(HashMap<Memory, Object> heap, Stack<Statement> stack) {
        this.heap = heap;
        this.stack = stack;
    }

    @Override
    public Object visitPoint(AeroScriptParser.PointContext ctx) {
        Expression xNode = (Expression) visit(ctx.expression(0));
        Expression yNode = (Expression) visit(ctx.expression(1));
        float x = Float.parseFloat(xNode.evaluate().toString());
        float y = Float.parseFloat(yNode.evaluate().toString());
        return new Point(x, y);
    }

    @Override
    public Object visitRange(AeroScriptParser.RangeContext ctx) {
        Expression startNode = (Expression) visit(ctx.expression(0));
        Expression endNode = (Expression) visit(ctx.expression(1));
        float start = Float.parseFloat(startNode.evaluate().toString());
        float end = Float.parseFloat(endNode.evaluate().toString());
        return new Range(start, end);
    }

    @Override
    public Action visitAcDock(AeroScriptParser.AcDockContext ctx) {
        //TODO!!
    }

    @Override
    public Action visitAcAscend(AeroScriptParser.AcAscendContext ctx) {
        //TODO!!
    }

    @Override
    public Action visitAcDescend(AeroScriptParser.AcDescendContext ctx) {
        //TODO!!
    }

    @Override
    public Action visitAcMove(AeroScriptParser.AcMoveContext ctx) {
        //TODO!!
    }

    @Override
    public Action visitAcTurn(AeroScriptParser.AcTurnContext ctx) {
        //TODO!!
    }

    @Override
    public Action visitAction(AeroScriptParser.ActionContext ctx) {
        // First get the base action
        Action action = null;
        
        if (ctx.acDock() != null) {
            action = visitAcDock(ctx.acDock());
        } else if (ctx.acMove() != null) {
            action = visitAcMove(ctx.acMove());
        } else if (ctx.acTurn() != null) {
            action = visitAcTurn(ctx.acTurn());
        } else if (ctx.acAscend() != null) {
            action = visitAcAscend(ctx.acAscend());
        } else if (ctx.acDescend() != null) {
            action = visitAcDescend(ctx.acDescend());
        }
        
        // Handle optional duration and speed modifiers
        if (ctx.getChildCount() > 1) {
            ParseTree modifierStart = ctx.getChild(1);
            if (modifierStart instanceof TerminalNode) {
                int tokenType = ((TerminalNode) modifierStart).getSymbol().getType();
                if (tokenType == AeroScriptParser.FOR) {
                    // Handle FOR expression SECONDS: child 2 is expression
                    AeroScriptParser.ExpressionContext exprCtx = (AeroScriptParser.ExpressionContext) ctx.getChild(2);
                    NumberExpression speedExpr = (NumberExpression) visitExpression(exprCtx);
                    action.setDurationExpression(speedExpr);
                } else if (tokenType == AeroScriptParser.AT) {
                    // Handle AT SPEED expression: child 3 is expression
                    AeroScriptParser.ExpressionContext exprCtx = (AeroScriptParser.ExpressionContext) ctx.getChild(3);
                    NumberExpression speedExpr = (NumberExpression) visitExpression(exprCtx);
                    action.setSpeedExpression(speedExpr);
                }
            }
        }

        return action;
    }   

    @Override
    public Statement visitStatement(AeroScriptParser.StatementContext ctx) {
        return (Statement) visitAction(ctx.action());
    }

    @Override
    public Mode visitModeDecl(AeroScriptParser.ModeDeclContext ctx) {
        boolean isInitial = ctx.ARROW(0) != null;
        String name = ctx.ID(0).getText();

        List<Statement> statements = new ArrayList<>();
        for (AeroScriptParser.StatementContext statementCtx : ctx.statement()) {
            Statement statement = (Statement) visitStatement(statementCtx);
            if (statement != null) {
                statements.add(statement);
            }
        }

        String nextModeName = null;
        if (ctx.ID().size() > 1) {
            nextModeName = ctx.ID(1).getText();
        }

        return new Mode(name, isInitial, statements, nextModeName);
    }

    /* Program = list of executions */
    @Override
    public ArrayList<Mode> visitProgram(AeroScriptParser.ProgramContext ctx) {
        ArrayList<Mode> modes = new ArrayList<>();
        for (AeroScriptParser.ModeDeclContext modeCtx : ctx.modeDecl()) {
            Mode mode = visitModeDecl(modeCtx);
            modes.add(mode);
        }
        return modes;
    }

    /* Expressions */
    public Expression visitExpression(AeroScriptParser.ExpressionContext ctx) {
        return switch (ctx) {
            case AeroScriptParser.PlusExpContext pex -> visitPlusExp(pex);
            case AeroScriptParser.MinusExpContext pex -> visitMinusExp(pex);
            case AeroScriptParser.TimesExpContext pex -> visitTimesExp(pex);
            case AeroScriptParser.NumExpContext pex -> visitNumExp(pex);
            case AeroScriptParser.NegExpContext pex -> visitNegExp(pex);
            case AeroScriptParser.ParentExpContext pex -> visitParentExp(pex);
            case AeroScriptParser.RangeExpContext pex -> visitRangeExp(pex);
            case AeroScriptParser.PointExpContext pex -> visitPointExp(pex);
            default -> new NumberExpression((float) 0) ;
        };
    }
    @Override
    public Expression visitPlusExp(AeroScriptParser.PlusExpContext ctx) {
        Expression left = visitExpression(ctx.left);
        Expression right = visitExpression(ctx.right);
        return new OperationExpression("SUM", left, right);
    }
    @Override
    public Expression visitMinusExp(AeroScriptParser.MinusExpContext ctx) {
        Expression left = visitExpression(ctx.left);
        Expression right = visitExpression(ctx.right);
        return new OperationExpression("SUB",left, right);
    }
    @Override
    public Expression visitTimesExp(AeroScriptParser.TimesExpContext ctx) {
        Expression left = visitExpression(ctx.left);
        Expression right = visitExpression(ctx.right);
        return new OperationExpression("MUL",left, right);
    }
    @Override
    public Expression visitNumExp(AeroScriptParser.NumExpContext ctx) {
        return new NumberExpression(Float.parseFloat(ctx.NUMBER().getText()));
    }
    @Override
    public Expression visitNegExp(AeroScriptParser.NegExpContext ctx) {
        return new NegNumberExpression(visitExpression(ctx.expression())) ;
    }
    @Override
    public Expression visitParentExp(AeroScriptParser.ParentExpContext ctx){
        return visitExpression(ctx.expression());
    }
    @Override
    public Expression visitPointExp(AeroScriptParser.PointExpContext ctx) {
        Expression left = visitExpression(ctx.point().left);
        Expression right = visitExpression(ctx.point().right);
        return new PointExpression(left, right);
    }
    @Override
    public Expression visitRangeExp(AeroScriptParser.RangeExpContext ctx) {
        Expression left = visitExpression(ctx.range().left);
        Expression right = visitExpression(ctx.range().right);
        return new RangeExpression(left, right);
    }
}
