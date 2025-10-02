package no.uio.aeroscript.runtime;

import no.uio.aeroscript.antlr.AeroScriptBaseVisitor;
import no.uio.aeroscript.antlr.AeroScriptParser;
import no.uio.aeroscript.ast.expr.*;
import no.uio.aeroscript.ast.stmt.*;
import no.uio.aeroscript.type.*;

import java.util.*;

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

    /* Program = list of executions */
    @Override
    public ArrayList<Execution> visitProgram(AeroScriptParser.ProgramContext ctx) {
        // Implement the method
        return new ArrayList<Execution>();
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
    public Expression visitPlusExp(AeroScriptParser.PlusExpContext ctx){
        Expression left = visitExpression(ctx.left);
        Expression right = visitExpression(ctx.right);
        return new OperationExpression("SUM",left, right);
    }
    @Override
    public Expression visitMinusExp(AeroScriptParser.MinusExpContext ctx){
        Expression left = visitExpression(ctx.left);
        Expression right = visitExpression(ctx.right);
        return new OperationExpression("SUB",left, right);
    }
    @Override
    public Expression visitTimesExp(AeroScriptParser.TimesExpContext ctx){
        Expression left = visitExpression(ctx.left);
        Expression right = visitExpression(ctx.right);
        return new OperationExpression("MUL",left, right);
    }
    @Override
    public Expression visitNumExp(AeroScriptParser.NumExpContext ctx){
        return new NumberExpression(Float.parseFloat(ctx.NUMBER().getText()));
    }
    @Override
    public Expression visitNegExp(AeroScriptParser.NegExpContext ctx){
        return new NegNumberExpression(visitExpression(ctx.expression())) ;
    }
    @Override
    public Expression visitParentExp(AeroScriptParser.ParentExpContext ctx){
        return visitExpression(ctx.expression());
    }
    @Override
    public Expression visitPointExp(AeroScriptParser.PointExpContext ctx){
        Expression left = visitExpression(ctx.point().left);
        Expression right = visitExpression(ctx.point().right);
        return new PointExpression(left, right);
    }
    @Override
    public Expression visitRangeExp(AeroScriptParser.RangeExpContext ctx){
        Expression left = visitExpression(ctx.range().left);
        Expression right = visitExpression(ctx.range().right);
        return new RangeExpression(left, right);
    }
}
