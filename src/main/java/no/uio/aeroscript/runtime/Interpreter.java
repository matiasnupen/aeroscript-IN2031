package no.uio.aeroscript.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import no.uio.aeroscript.ast.stmt.Event;
import no.uio.aeroscript.ast.stmt.Mode;
import no.uio.aeroscript.ast.stmt.Reaction;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.ast.stmt.action.AltitudeAction;
import no.uio.aeroscript.ast.stmt.action.DockAction;
import no.uio.aeroscript.ast.stmt.action.MoveAction;
import no.uio.aeroscript.ast.stmt.action.TurnAction;
import no.uio.aeroscript.ast.stmt.event.LowBatteryEvent;
import no.uio.aeroscript.ast.stmt.event.MessageEvent;
import no.uio.aeroscript.ast.stmt.event.ObstacleEvent;
import no.uio.aeroscript.type.Memory;
import no.uio.aeroscript.type.impl.Point;
import no.uio.aeroscript.type.impl.Range;

/** Interpreter class
 * @param heap is the memory heap for variable storage
 * @param stack is the execution stack for statements
 * @param methodTable stores method definitions
 * @param firstExecution keeps the first execution mode
 * @param allModes keeps track of all available modes, 
 *  and it gets initialized at @param executeProgram(modes)
 * @param pendingModeTransition keeps track of any pending mode transitions
 * @param modeTransitionRequested indicates if a mode transition is requested
 **/
public class Interpreter extends AeroScriptBaseVisitor<Object> {
    private static final Logger logger = Logger.getLogger(Interpreter.class.getName());

    private HashMap<Memory, Object> heap;
    private Stack<Statement> stack;
    private HashMap<String, ArrayList<Statement>> methodTable = new HashMap<>();

    private Mode firstExecution = null;
    private Map<String, Runnable> listeners = new HashMap<>();

    private ArrayList<Mode> allModes;

    private Mode currentMode = null;

    private volatile Mode pendingModeTransition = null;
    private volatile boolean modeTransitionRequested = false;

    public Interpreter(HashMap<Memory, Object> heap, Stack<Statement> stack) {
        this.heap = heap;
        this.stack = stack;
    }

    public void setFirstExecution(Mode firstExecution) { this.firstExecution = firstExecution; }
    public Mode getFirstExecution() { return firstExecution; }
    public Map<String, Runnable> getListeners() { return listeners; }

    public Mode getCurrentMode() { return currentMode; }
    public void setCurrentMode(Mode mode) { this.currentMode = mode; }
    private ArrayList<Mode> getAllModes() { return allModes; }

    private Object getVariable(String varName) {
        HashMap<String, Object> vars = (HashMap<String, Object>) heap.get(Memory.VARIABLES);
        return vars.get(varName);
    }

    private void setVariable(String varName, Object value) {
        HashMap<String, Object> vars = (HashMap<String, Object>) heap.get(Memory.VARIABLES);
        vars.put(varName, value);
    }

    /** visitPoint
     * @param ctx is the point context
     * @return Point object created from the context */
    @Override
    public Object visitPoint(AeroScriptParser.PointContext ctx) {
        Expression xNode = (Expression) visit(ctx.expression(0));
        Expression yNode = (Expression) visit(ctx.expression(1));
        float x = Float.parseFloat(xNode.evaluate().toString());
        float y = Float.parseFloat(yNode.evaluate().toString());
        return new Point(x, y);
    }

    /** visitRange
     * @param ctx is the range context
     * @return Range object created from the context */
    @Override
    public Object visitRange(AeroScriptParser.RangeContext ctx) {
        Expression startNode = (Expression) visit(ctx.expression(0));
        Expression endNode = (Expression) visit(ctx.expression(1));
        float start = Float.parseFloat(startNode.evaluate().toString());
        float end = Float.parseFloat(endNode.evaluate().toString());
        return new Range(start, end);
    }


        /** visitActions in general
         * visitAc-methods create the appropriate Action objects, and updates the DroneState singleton
         * @param ctx is the action context
         * @return Action object created from the context
         * 
         * The duration and speed expressions are set to null for now
         * as these will be set later if they are present in the action
        **/

    /** Dock Action Visitor
     * @param ctx is the dock action context
     * @return Action object created from the context */
    @Override
    public Action visitAcDock(AeroScriptParser.AcDockContext ctx) {
        DroneState droneState = DroneState.getInstance();
        return new DockAction(droneState, null, null);
    }

    /** Ascend Action Visitor
     * @param ctx is the ascend action context
     * @handles only ASCEND BY NUMBER for now
     * @return Action object created from the context */
    @Override
    public Action visitAcAscend(AeroScriptParser.AcAscendContext ctx) {
        DroneState droneState = DroneState.getInstance();

        // ONLY ASCENDING BY NUMBER
        NumberExpression numberAscendingExpression = (NumberExpression) visit(ctx.expression());
        return new AltitudeAction.Ascend(droneState, numberAscendingExpression, null, null);
    }

    /** Descend Action Visitor
     * @param ctx is the descend action context
     * @handles both DESCEND BY NUMBER and DESCEND TO GROUND with the correct classes
     * @return Action object created from the context */
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

    /** Move Action Visitor
     * @param ctx is the move action context
     * @handles both MOVE TO POINT and MOVE BY NUMBER with the correct classes
     * @return Action object created from the context */
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

    /** Turn Action Visitor
     * @param ctx is the turn action context
     * @handles both LEFT and RIGHT turns with the correct classes
     * @return Action object created from the context */
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

    /** Action Visitor
     * @param ctx is the action context
     * @handles optional duration and speed modifiers
     * @return Action object created from the context */
    @Override
    public Action visitAction(AeroScriptParser.ActionContext ctx) {
        Action action = null; // Get the base action
        
        // Determine which action type it is
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

    /** Action Execution
     * @param action is the action to be executed
     * @throws BatteryDepletedException if battery depletes during action execution
     * @logger logs battery depletion events and updates heap variables after action execution */
    private void executeAction(Action action) {
        action.execute(); // Updates the DroneState singleton
        DroneState droneState = DroneState.getInstance(); // Get the updated state from DroneState singleton

        // Update heap with new values
        setVariable("current position", droneState.getCurrentPosition());
        setVariable("altitude", droneState.getCurrentAltitude());
        setVariable("battery level", droneState.getBatteryLevel());
        setVariable("distance travelled", droneState.getTotalDistanceTravelled());

        // Low battery warning is already handled in Action.consumeBattery()
        // Set variable for potential future use
        setVariable("battery low", droneState.getBatteryLevel() <= 20.0f);
    }
    
    /** Mode Execution
     * @param mode is the mode to be executed
     * The executeMode method handles:
     * - Pre-action checks for message-triggered transitions
     * - Post-action checks for message-triggered transitions
     * - Post-action checks for other event-based reactions (obstacle, low_battery, etc.)
     * @throws BatteryDepletedException if battery depletes during execution
     * @logger logs the entering and completion of modes, as well as battery depletion events */
    public void executeMode(Mode mode) throws BatteryDepletedException {
        setCurrentMode(mode);
        logger.info("ENTERING MODE: " + mode.getName() + "\n");
        DroneState droneState = DroneState.getInstance();

        // Check if this mode has only reactions (no actions)
        boolean hasActions = false;
        boolean hasReactions = false;
        for (Statement statement : mode.getStatements()) {
            if (statement instanceof Action) {
                hasActions = true;
            } else if (statement instanceof Reaction) {
                hasReactions = true;
            }
        }

        // Handle modes with only reactions (no actions) - wait for events
        if (!hasActions && hasReactions) {
            logger.info("Mode '" + mode.getName() + "' has only reactions. Use REPL to send messages or trigger events.\n");

            // For reactive-only modes, we don't block - we just set up and return
            // The REPL will handle triggering transitions via messages
            
            // Check for immediate non-message reactions (obstacle, low_battery, etc.)
            String targetModeName = mode.checkReactions(droneState);
            if (targetModeName != null) {
                Mode targetMode = findModeByName(getAllModes(), targetModeName);
                if (targetMode != null) {
                    logger.info("Transitioning to mode: " + targetModeName + " due to event reaction");
                    executeMode(targetMode);
                    return;
                }
            }
            return;
        }

        // Handle modes with actions
        if (hasActions) {
            for (Statement statement : mode.getStatements()) {
                if (statement instanceof Action) {
                    // Check for obstacle reactions BEFORE executing the action
                    if (droneState.obstacleDetected()) {
                        String targetModeName = mode.checkReactions(droneState);
                        if (targetModeName != null) {
                            Mode targetMode = findModeByName(getAllModes(), targetModeName);
                            if (targetMode != null) {
                                logger.info("OBSTACLE DETECTED! Immediately transitioning to mode: " + targetModeName);
                                executeMode(targetMode);
                                return; // Stop current mode execution - discard remaining actions
                            }
                        }
                    }
                    executeAction((Action) statement);
                    // Check for reactions after each action
                    String targetModeName = mode.checkReactions(droneState);
                    if (targetModeName != null) {
                        Mode targetMode = findModeByName(getAllModes(), targetModeName);
                        if (targetMode != null) {
                            logger.info("Transitioning to mode: " + targetModeName + " due to event reaction");
                            executeMode(targetMode);
                            return;
                        }
                    }
                }
            }
        }

        // Handle normal mode transitions after all actions are completed
        if (mode.hasTransition()) {
            String nextModeName = mode.getNextModeName();
            Mode nextMode = findModeByName(getAllModes(), nextModeName);
            if (nextMode != null) {
                logger.info("Transitioning to next mode: " + nextModeName + "\n");
                executeMode(nextMode);
            } else {
                logger.info("WARNING: Next mode '" + nextModeName + "' not found.\n");
            }
        } else {
            logger.info("MODE COMPLETED: " + mode.getName() + " with no further transitions.\n");
        }
    }

    /** Initialize the program for REPL execution
     * Initialize DroneState with heap values and set up listeners
     * @param modes is the list of all modes in the program, and is copied into allModes
     * This method sets up the program but doesn't execute it - execution happens via REPL messages */
    public void initializeProgram(ArrayList<Mode> modes) {
        this.allModes = modes; // At the start of program execution copy modes to allModes
        
        DroneState droneState = DroneState.getInstance();
        droneState.setCurrentPosition((Point) getVariable("initial position"));
        droneState.setBatteryLevel((Float) getVariable("initial battery level"));
        droneState.setCurrentAltitude((Float) getVariable("altitude"));
        droneState.setTotalDistanceTravelled((Float) getVariable("distance travelled"));
        
        logger.info("AEROSCRIPT PROGRAM INITIALIZED" + 
        "\n\tInitial Battery Level: " + droneState.getBatteryLevel() + "%" + 
        "\n\tInitial Position: " + droneState.getCurrentPosition() + "\n"
        );

        Mode initialMode = null;
        for (Mode mode : modes) {
            if (mode.isInitial()) {
                initialMode = mode;
                setFirstExecution(mode);
                break;
            }
        }

        buildListeners(modes);
        
        if (initialMode != null) {
            setCurrentMode(initialMode);
            logger.info("ENTERING MODE: " + initialMode.getName() + "\n");
            
            // Check if initial mode has only reactions
            boolean hasActions = false;
            boolean hasReactions = false;
            for (Statement statement : initialMode.getStatements()) {
                if (statement instanceof Action) {
                    hasActions = true;
                } else if (statement instanceof Reaction) {
                    hasReactions = true;
                }
            }
            
            if (!hasActions && hasReactions) {
                logger.info("Mode '" + initialMode.getName() + "' has only reactions. Waiting for events...\n");
                logger.info("Use REPL commands to interact with the program. Type 'help' for available commands.\n");
            } else {
                // If initial mode has actions, execute them immediately
                try {
                    executeMode(initialMode);
                } catch (BatteryDepletedException e) {
                    logger.info("PROGRAM TERMINATED DUE TO BATTERY DEPLETION" +
                    "\n\tFinal Status: " + e.getMessage() +
                    "\n\tFinal Battery Level: " + e.getCurrentBatteryLevel() + "%\n"
                    );
                }
            }
        } else {
            logger.info("ERROR: No initial mode found in program! Program not initialized.\n");
        }
    }


    /** The method builds listeners for message events
     * @param modes is the list of all modes in the program
     * @param messageEvent is the message event to listen for
     * @logger logs when a listener is triggered */
    private void buildListeners(ArrayList<Mode> modes) {
        for (Mode mode : modes) {
            for (Statement statement : mode.getStatements()) {
                if (statement instanceof Reaction reaction) {
                    Event event = reaction.getEvent();
                    if (event instanceof MessageEvent messageEvent) {
                        listeners.put(messageEvent.getMessageId(), () -> {
                            logger.info("Message listener triggered for message ID: " + messageEvent.getMessageId());
                            
                            String targetModeName = reaction.getTargetModeName();
                            Mode targetMode = findModeByName(modes, targetModeName);

                            if (targetMode != null) {
                                logger.info("Requesting transition to mode: " + targetModeName + " due to message event.");
                                requestModeTransition(targetMode);
                            } else {
                                logger.warning("Target mode '" + targetModeName + "' not found for message: " + messageEvent.getMessageId());
                            }
                        });
                    }
                }
            }
        }
    }
    public void requestModeTransition(Mode targetMode) {
        logger.info("Mode transition requested to: " + targetMode.getName());
        try {
            executeMode(targetMode);
        } catch (BatteryDepletedException e) {
            logger.info("PROGRAM TERMINATED DUE TO BATTERY DEPLETION" +
            "\n\tFinal Status: " + e.getMessage() +
            "\n\tFinal Battery Level: " + e.getCurrentBatteryLevel() + "%\n"
            );
        }
    }

    /** Finds a mode by its name
     * @param modes is the list of all modes kept in the program
     * @param name is the name of the mode to find
     * @return the Mode object if found, null otherwise */
    private Mode findModeByName(ArrayList<Mode> modes, String name) {
        for (Mode mode : modes) {
            if (mode.getName().equals(name)) {
                return mode;
            }
        }
        return null;
    }

    /** Statements 
     * @param ctx is the statement context
     * @if the statement is an action, visit the action context
     * @if the statement is a reaction, visit the reaction context
     * @return null if neither action nor reaction is found */
    @Override
    public Statement visitStatement(AeroScriptParser.StatementContext ctx) {
        if (ctx.action() != null) {
            return (Statement) visitAction(ctx.action());
        } else if (ctx.reaction() != null) {
            return (Statement) visitReaction(ctx.reaction());
        }
        return null;
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

    /** Program = list of executions 
     * @param ctx is the program context
     * @return list of modes in the program */
    @Override
    public ArrayList<Mode> visitProgram(AeroScriptParser.ProgramContext ctx) {
        ArrayList<Mode> modes = new ArrayList<>();
        for (AeroScriptParser.ModeDeclContext modeCtx : ctx.modeDecl()) {
            Mode mode = visitModeDecl(modeCtx);
            modes.add(mode);
        }
        return modes;
    }

    /** Reaction & Events 
     * @param ctx is the reaction context
     * @return Reaction object created from the context */
    @Override
    public Reaction visitReaction(AeroScriptParser.ReactionContext ctx) {
        Event event = (Event) visitEvent(ctx.event());
        String targetModeName = ctx.ID().getText();
        return new Reaction(event, targetModeName);
    }

    /** Event
     * @param ctx is the event context
     * @return Event object created from the context */
    @Override
    public Event visitEvent(AeroScriptParser.EventContext ctx) {
        if (ctx.OBSTACLE() != null) {
            return new ObstacleEvent();
        } else if (ctx.LOW_BATTERY() != null) {
            return new LowBatteryEvent();
        } else if (ctx.MESSAGE() != null) {
            String messageId = ctx.ID().getText();
            return new MessageEvent(messageId);
        }
        throw new IllegalArgumentException("Unknown event type");
    }

        /** Expressions in general
         * @param ctx is the expression context
         * @switch on the type of expression context, and call the appropriate visit method
         * @return Expression object created from the context 
        **/
    
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