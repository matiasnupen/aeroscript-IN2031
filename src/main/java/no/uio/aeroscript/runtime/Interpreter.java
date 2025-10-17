package no.uio.aeroscript.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import no.uio.aeroscript.antlr.AeroScriptBaseVisitor;
import no.uio.aeroscript.antlr.AeroScriptParser;
import no.uio.aeroscript.ast.expr.Expression;
import no.uio.aeroscript.ast.expr.NegNumberExpression;
import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.expr.OperationExpression;
import no.uio.aeroscript.ast.expr.PointExpression;
import no.uio.aeroscript.ast.expr.RangeExpression;
import no.uio.aeroscript.ast.stmt.Action;
import no.uio.aeroscript.ast.stmt.Mode;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.ast.stmt.action.AltitudeAction;
import no.uio.aeroscript.ast.stmt.action.DockAction;
import no.uio.aeroscript.ast.stmt.action.MoveAction;
import no.uio.aeroscript.ast.stmt.action.TurnAction;
import no.uio.aeroscript.type.Memory;
import no.uio.aeroscript.type.Point;
import no.uio.aeroscript.type.Range;

/* For now, let us just ignore interrupts, will need to build
 * an interrupt lookup table and introduce a message queue later... */
public class Interpreter extends AeroScriptBaseVisitor<Object> {
    private static final Logger logger = Logger.getLogger(Interpreter.class.getName());

    private HashMap<Memory, Object> heap;
    private Stack<Statement> stack;
    private HashMap<String, ArrayList<Statement>> methodTable = new HashMap<>();

    public Interpreter(HashMap<Memory, Object> heap, Stack<Statement> stack) {
        this.heap = heap;
        this.stack = stack;
    }

    private Object getVariable(String varName) {
        HashMap<String, Object> vars = (HashMap<String, Object>) heap.get(Memory.VARIABLES);
        return vars.get(varName);
    }

    private void setVariable(String varName, Object value) {
        HashMap<String, Object> vars = (HashMap<String, Object>) heap.get(Memory.VARIABLES);
        vars.put(varName, value);
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

        /** Actions 
         * The visitAc...-methods create the appropriate Action objects
         * and set the drone state and any relevant parameters.
         * 
         * The duration and speed expressions are set to null for now
         * as these will be set later if they are present in the action
        */

    @Override
    public Action visitAcDock(AeroScriptParser.AcDockContext ctx) {
        DroneState droneState = DroneState.getInstance();
        return new DockAction(droneState, null, null);
    }

    @Override
    public Action visitAcAscend(AeroScriptParser.AcAscendContext ctx) {
        DroneState droneState = DroneState.getInstance();

        // ONLY ASCENDING BY NUMBER
        NumberExpression numberAscendingExpression = (NumberExpression) visit(ctx.expression());
        return new AltitudeAction.Ascend(droneState, numberAscendingExpression, null, null);
    }

    @Override
    public Action visitAcDescend(AeroScriptParser.AcDescendContext ctx) {
        DroneState droneState = DroneState.getInstance();

        if (ctx.BY() != null) {
            // DESCEND BY NUMBER
            NumberExpression numberDescendingExpression = (NumberExpression) visit(ctx.expression());
            return new AltitudeAction.Descend(droneState, numberDescendingExpression, null, null);
        } else {
            // DESCEND TO GROUND
            return new AltitudeAction.DescendToGround(droneState, null, null);
        }
    }

    @Override
    public Action visitAcMove(AeroScriptParser.AcMoveContext ctx) {
        DroneState droneState = DroneState.getInstance();
        
        if (ctx.point() != null) {
            // MOVE TO POINT
            Expression xCordinateExpression = visitExpression(ctx.point().left);
            Expression yCordinateExpression = visitExpression(ctx.point().right);
            PointExpression pointExpr = new PointExpression(xCordinateExpression, yCordinateExpression);
            return new MoveAction.ByPoint(droneState, pointExpr, null, null);
        } else {
            // MOVE BY NUMBER
            NumberExpression distanceExpr = new NumberExpression(Float.parseFloat(ctx.NUMBER().getText()));
            return new MoveAction.ByDistance(droneState, distanceExpr, null, null);
        }
    }

    @Override
    public Action visitAcTurn(AeroScriptParser.AcTurnContext ctx) {
        DroneState droneState = DroneState.getInstance();
        NumberExpression numberDegreesExpression = (NumberExpression) visit(ctx.expression());
        
        if (ctx.LEFT() != null) {
            // TURN LEFT
            return new TurnAction.Left(droneState, numberDegreesExpression, null, null);
        } else {
            // TURN RIGHT (default when direction not specified or RIGHT explicitly given)
            return new TurnAction.Right(droneState, numberDegreesExpression, null, null);
        } 
    }

    @Override
    public Action visitAction(AeroScriptParser.ActionContext ctx) {
        // Get the base action
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
                    NumberExpression durationExpr = (NumberExpression) visitExpression(exprCtx);
                    action.setDurationExpression(durationExpr);
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

    private void executeAction(Action action) {
        // Execute the action (this will update the DroneState singleton)
        action.execute();
        
        // Get the updated state from DroneState singleton
        DroneState droneState = DroneState.getInstance();

        // Update heap with new values
        setVariable("current position", droneState.getCurrentPosition());
        setVariable("altitude", droneState.getCurrentAltitude());
        setVariable("battery level", droneState.getBatteryLevel());
        setVariable("distance travelled", droneState.getTotalDistanceTravelled());

        // Low battery warning is already handled in Action.consumeBattery()
        // Set variable for potential future use
        setVariable("battery low", droneState.getBatteryLevel() <= 20.0f);
    }
    
    public void executeMode(Mode mode) throws BatteryDepletedException {
        logger.info("ENTERING MODE: " + mode.getName() + "\n");
        DroneState droneState = DroneState.getInstance();

        // Execute all statements in this mode
        for (Statement statement : mode.getStatements()) {
            // Pre-check battery level before attempting action
            if (droneState.getBatteryLevel() <= 0.0f) {
                throw new BatteryDepletedException(droneState.getBatteryLevel(), "PreCheck");
            }

            if (statement instanceof Action) {
                try {
                    executeAction((Action) statement);
                } catch (BatteryDepletedException e) {
                    logger.warning("BATTERY DEPLETION DETECTED" + 
                    "\n\tMode: " + mode.getName() + 
                    "\n\tReason: " + e.getMessage() + 
                    "\n\tTERMINATING EXECUTION\n"
                    );
                    throw e; // Re-throw to stop execution at higher level
                }
            }
        }

        logger.info("MODE COMPLETED: " + mode.getName() + "\n");
    }
    
    public void executeProgram(ArrayList<Mode> modes) {
        // Initialize DroneState with heap values before execution
        DroneState droneState = DroneState.getInstance();
        droneState.setCurrentPosition((Point) getVariable("initial position"));
        droneState.setBatteryLevel((Float) getVariable("initial battery level"));
        droneState.setCurrentAltitude((Float) getVariable("altitude"));
        droneState.setTotalDistanceTravelled((Float) getVariable("distance travelled"));
        
        logger.info("AEROSCRIPT PROGRAM EXECUTION STARTED" + 
        "\n\tInitial Battery Level: " + droneState.getBatteryLevel() + "%" + 
        "\n\tInitial Position: " + droneState.getCurrentPosition() + "\n"
        );

        // Find the initial mode and execute it
        Mode initialMode = null;
        for (Mode mode : modes) {
            if (mode.isInitial()) {
                initialMode = mode;
                break;
            }
        }
        
        if (initialMode != null) {
            try {
                executeMode(initialMode);
                

                String nextModeName = initialMode.getNextModeName();
                while (nextModeName != null) {
                    // Check battery before mode transition
                    if (droneState.getBatteryLevel() <= 0.0f) {
                        throw new BatteryDepletedException(droneState.getBatteryLevel(), "ModeTransition");
                    }
                    
                    Mode nextMode = findModeByName(modes, nextModeName);
                    if (nextMode != null) {
                        executeMode(nextMode);
                        nextModeName = nextMode.getNextModeName();
                    } else {
                        logger.info("WARNING: Next mode '" + nextModeName + "' not found. Terminating.\n");
                        break;
                    }
                }

                logger.info("PROGRAM COMPLETED SUCCESSFULLY\n");

            } catch (BatteryDepletedException e) {
                logger.info("PROGRAM TERMINATED DUE TO BATTERY DEPLETION" +
                "\n\tFinal Status: " + e.getMessage() +
                "\n\tFinal Battery Level: " + e.getCurrentBatteryLevel() + "%\n"
                );
            }
        } else {
            logger.info("ERROR: No initial mode found in program! Terminating execution.\n");
        }
    }
    
    private Mode findModeByName(ArrayList<Mode> modes, String name) {
        for (Mode mode : modes) {
            if (mode.getName().equals(name)) {
                return mode;
            }
        }
        return null;
    }

    @Override
    public Statement visitStatement(AeroScriptParser.StatementContext ctx) {
        return (Statement) visitAction(ctx.action());
    }

    @Override
    public Mode visitModeDecl(AeroScriptParser.ModeDeclContext ctx) {
        boolean isInitial = ctx.getChild(0).getText().equals("->");
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
