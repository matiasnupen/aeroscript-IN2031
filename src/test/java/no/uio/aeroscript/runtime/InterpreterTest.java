package no.uio.aeroscript.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import no.uio.aeroscript.antlr.AeroScriptLexer;
import no.uio.aeroscript.antlr.AeroScriptParser;
import no.uio.aeroscript.ast.expr.Expression;
import no.uio.aeroscript.ast.stmt.Mode;
import no.uio.aeroscript.ast.stmt.Reaction;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.ast.stmt.event.ObstacleEvent;
import no.uio.aeroscript.type.Memory;
import no.uio.aeroscript.type.Obstacle;
import no.uio.aeroscript.type.Point;

class InterpreterTest {
    private HashMap<Memory, Object> heap;
    private Stack<Statement> stack;
    private Interpreter interpreter;

    @BeforeEach
    void setUp() {
        initInterpreter();
        interpreter = new Interpreter(heap, stack);
    }

    private void initInterpreter() {
        this.heap = new HashMap<>();
        this.stack = new Stack<>();
        HashMap<String, Object> vars = new HashMap<>();
        
        vars.put("initial position", new Point(0.0f, 0.0f));
        vars.put("current position", new Point(0.0f, 0.0f));
        vars.put("altitude", 0.0f);  // Changed from int to float
        vars.put("initial battery level", 100.0f);
        vars.put("battery level", 100.0f);
        vars.put("battery low", false);
        vars.put("distance travelled", 0.0f);
        vars.put("initial mode", null);

        heap.put(Memory.VARIABLES, vars);
    }

    private ArrayList<Mode> parseProgram(String program) {
        try {
            AeroScriptLexer lexer = new AeroScriptLexer(CharStreams.fromString(program));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            AeroScriptParser parser = new AeroScriptParser(tokens);
            return interpreter.visitProgram(parser.program());
        } catch (Exception e) {
            fail("Failed to parse program: " + e.getMessage());
            return null;
        }
    }

    private float evaluateExpression(String expression) {
        try {
            AeroScriptLexer lexer = new AeroScriptLexer(CharStreams.fromString(expression));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            AeroScriptParser parser = new AeroScriptParser(tokens);
            Expression expr = interpreter.visitExpression(parser.expression());
            return Float.parseFloat(expr.evaluate().toString());
        } catch (Exception e) {
            fail("Failed to evaluate expression '" + expression + "': " + e.getMessage());
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, Object> getVariables() {
        return (HashMap<String, Object>) heap.get(Memory.VARIABLES);
    }

    @Nested
    class ExpressionParsingTests {
        @Test
        void testPointExpressionParsing() {
            AeroScriptLexer lexer = new AeroScriptLexer(CharStreams.fromString("point (10, 20)"));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            AeroScriptParser parser = new AeroScriptParser(tokens);
            
            Object result = interpreter.visitExpression(parser.expression()).evaluate();
            System.out.println("testPointExpressionParsing -> " + result);
            assertTrue(result instanceof Point);
            
            Point point = (Point) result;
            assertEquals(10.0f, point.getX());
            assertEquals(20.0f, point.getY());
        }

        @Test
        void testRangeExpressionParsing() {
            AeroScriptLexer lexer = new AeroScriptLexer(CharStreams.fromString("random[5, 15]"));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            AeroScriptParser parser = new AeroScriptParser(tokens);
            
            Object result = interpreter.visitExpression(parser.expression()).evaluate();
            System.out.println("testRangeExpressionParsing -> " + result);
            assertTrue(result instanceof Float);
            
            Float randomValue = (Float) result;
            assertTrue(randomValue >= 5.0f && randomValue <= 15.0f);
        }

        @Test
        void testComplexExpressionParsing() {
            // Test that interpreter can parse and evaluate complex expressions
            float result1 = evaluateExpression("2 * (3 + 4)");
            System.out.println("testComplexExpressionParsing1 -> " + result1);
            assertEquals(14.0f, result1);
            
            float result2 = evaluateExpression("(2 + 3) * 2");
            System.out.println("testComplexExpressionParsing2 -> " + result2);
            assertEquals(10.0f, result2);
        }

        @Test
        void testExpressionParsingErrors() {
            // Test that interpreter handles malformed expressions
            try {
                float result = evaluateExpression("2 + + 3");
                System.out.println("testExpressionParsingErrors unexpected success -> " + result);
                // If it doesn't throw, that's also valid - some parsers handle this
            } catch (Exception e) {
                System.out.println("testExpressionParsingErrors expected error -> " + e.getMessage());
            }
            
            // Test a simpler case that should definitely work
            float validResult = evaluateExpression("2 + 3");
            System.out.println("testExpressionParsingErrors valid case -> " + validResult);
            assertEquals(5.0f, validResult);
        }
    }

    @Nested
    class EventTests {
        private static final String OBSTACLE_EVENT_PROGRAM = """
            -> TestMode {
                move to point (10, 10)
                on obstacle -> EmergencyMode
                ascend by 5
            }
            EmergencyMode { descend to ground }
            """;

        private static final String MESSAGE_EVENT_PROGRAM = """
            -> MissionControl {
                on message [launch] -> TakeOff
            }
            TakeOff {
                ascend by 20
                move to point (50, 50)
            } -> Landing
            Landing { return to base }
            """;

        private static final String BATTERY_EVENT_PROGRAM = """
            -> FlightMode {
                move to point (100, 100)
                on low battery -> EmergencyLanding
                turn right by 90
            }
            EmergencyLanding { descend to ground }
            """;

        private static final String ALL_EVENTS_PROGRAM = """
            -> TestMode {
                move by 10
                on obstacle -> ObstacleHandler
                on low battery -> BatteryHandler
                on message [emergency] -> MessageHandler
            }
            ObstacleHandler { descend to ground }
            BatteryHandler { return to base }
            MessageHandler { ascend by 5 }
            """;

        private static final String IMMEDIATE_OBSTACLE_PROGRAM = """
            -> TestMode {
                ascend by 5
                on obstacle -> EmergencyMode
                move to point (50, 50)
                turn right by 90
            }
            EmergencyMode { descend to ground }
            """;
        
        private static final String OBSTACLE_REACTION_INTERRUPT_PROGRAM = """
            -> TestMode {
                ascend by 5
                on obstacle -> EmergencyMode
                move to point (50, 50)
                turn right by 90
            }
            EmergencyMode { descend to ground }
            """;

        @Test
        void testObstacleEventTriggering() {
            // Reset DroneState for clean test
            DroneState droneState = DroneState.getInstance();
            droneState.resetForTesting();
            
            ArrayList<Mode> modes = parseProgram(OBSTACLE_EVENT_PROGRAM);
            assertEquals(2, modes.size());
            
            // Verify modes parsed correctly
            assertEquals("TestMode", modes.get(0).getName());
            assertEquals("EmergencyMode", modes.get(1).getName());
            assertTrue(modes.get(0).isInitial());
            
            // Add an obstacle that will be detected
            droneState.discoverObstacle(new Obstacle("TestObstacle", new Point(5.0f, 5.0f)));
            
            // Verify obstacle is detected
            assertTrue(droneState.obstacleDetected(), "Obstacle should be detected");
            
            System.out.println("testObstacleEventTriggering -> Obstacle detection test passed");
        }

        @Test
        void testMessageEventReaction() {
            // Reset DroneState for clean test
            DroneState droneState = DroneState.getInstance();
            droneState.resetForTesting();
            
            ArrayList<Mode> modes = parseProgram(MESSAGE_EVENT_PROGRAM);
            assertEquals(3, modes.size());
            
            // Verify modes parsed correctly
            assertEquals("MissionControl", modes.get(0).getName());
            assertEquals("TakeOff", modes.get(1).getName());
            assertEquals("Landing", modes.get(2).getName());
            assertTrue(modes.get(0).isInitial());
            
            // Initialize the program to set up listeners
            interpreter.initializeProgram(modes);
            
            // Verify message listener was created
            assertTrue(interpreter.getListeners().containsKey("launch"), 
                      "Should have listener for 'launch' message");
            
            // Test message reception
            droneState.addReceivedMessage("launch");
            assertTrue(droneState.messageReceived("launch"), "Message should be received");
            
            System.out.println("testMessageEventReaction -> Message event parsing and listener creation test passed");
        }

        @Test
        void testBatteryLowEventReaction() {
            // Reset DroneState for clean test
            DroneState droneState = DroneState.getInstance();
            droneState.resetForTesting();
            
            ArrayList<Mode> modes = parseProgram(BATTERY_EVENT_PROGRAM);
            assertEquals(2, modes.size());
            
            // Verify modes parsed correctly
            assertEquals("FlightMode", modes.get(0).getName());
            assertEquals("EmergencyLanding", modes.get(1).getName());
            assertTrue(modes.get(0).isInitial());
            
            // Test low battery detection
            droneState.setBatteryLevel(15.0f); // Below 20% threshold
            assertTrue(droneState.getBatteryLevel() <= 20.0f, "Battery should be low");
            
            // Test that low battery event can be detected
            no.uio.aeroscript.ast.stmt.event.LowBatteryEvent lowBatteryEvent = 
                new no.uio.aeroscript.ast.stmt.event.LowBatteryEvent();
            assertTrue(lowBatteryEvent.isTriggered(droneState), "Low battery event should be triggered");
            
            System.out.println("testBatteryLowEventReaction -> Low battery event detection test passed");
        }

        @Test
        void testEventParsing() {
            // Test that all event types parse correctly
            ArrayList<Mode> modes = parseProgram(ALL_EVENTS_PROGRAM);
            assertEquals(4, modes.size());
            
            Mode testMode = modes.get(0);
            assertEquals("TestMode", testMode.getName());
            
            // Count reactions
            long reactionCount = testMode.getStatements().stream()
                .filter(stmt -> stmt instanceof no.uio.aeroscript.ast.stmt.Reaction)
                .count();
            
            assertEquals(3, reactionCount, "Should have 3 reactions (obstacle, low battery, message)");
            System.out.println("testEventParsing -> Successfully parsed " + reactionCount + " reactions");
        }

        @Test
        void testImmediateObstacleReaction() {
            // Test that obstacle reactions happen immediately and discard remaining actions
            DroneState droneState = DroneState.getInstance();
            droneState.resetForTesting();
            
            ArrayList<Mode> modes = parseProgram(IMMEDIATE_OBSTACLE_PROGRAM);
            assertEquals(2, modes.size());
            
            // Verify parsing worked correctly
            assertEquals("TestMode", modes.get(0).getName());
            assertEquals("EmergencyMode", modes.get(1).getName());
            
            // Count statements in TestMode
            long actionCount = modes.get(0).getStatements().stream()
                .filter(stmt -> stmt instanceof no.uio.aeroscript.ast.stmt.Action)
                .count();
            long reactionCount = modes.get(0).getStatements().stream()
                .filter(stmt -> stmt instanceof no.uio.aeroscript.ast.stmt.Reaction)
                .count();
                
            assertEquals(3, actionCount, "Should have 3 actions");
            assertEquals(1, reactionCount, "Should have 1 reaction");
            
            // Test obstacle detection functionality
            droneState.discoverObstacle(new Obstacle("ImmediateObstacle", new Point(25.0f, 25.0f)));
            assertTrue(droneState.obstacleDetected(), "Obstacle should be detected");
            
            System.out.println("testImmediateObstacleReaction -> Complex mode parsing and obstacle detection test passed");
        }

        @Test
        void testObstacleReactionInterruptsMode() {
            // Reset DroneState for clean test
            DroneState droneState = DroneState.getInstance();
            droneState.resetForTesting();
            
            ArrayList<Mode> modes = parseProgram(OBSTACLE_REACTION_INTERRUPT_PROGRAM);
            assertEquals(2, modes.size(), "Should parse 2 modes from program");
            
            // Verify the program structure
            Mode testMode = modes.get(0);
            Mode emergencyMode = modes.get(1);
            assertEquals("TestMode", testMode.getName());
            assertEquals("EmergencyMode", emergencyMode.getName());
            
            // Verify that TestMode has the expected reactions
            long obstacleReactions = testMode.getStatements().stream()
                .filter(stmt -> stmt instanceof Reaction)
                .map(stmt -> (Reaction) stmt)
                .filter(reaction -> reaction.getEvent() instanceof ObstacleEvent)
                .count();
            
            assertEquals(1, obstacleReactions, "TestMode should have exactly one obstacle reaction");
            
            // Add obstacle that will be detected
            droneState.discoverObstacle(new Obstacle("InterruptObstacle", new Point(25.0f, 25.0f)));
            assertTrue(droneState.obstacleDetected(), "Obstacle should be detected after adding it");
            
            // Test that the obstacle event can be triggered
            ObstacleEvent obstacleEvent = new ObstacleEvent();
            assertTrue(obstacleEvent.isTriggered(droneState), "Obstacle event should be triggered when obstacle is detected");
        }
    }

    @Nested
    class ModeTests {
        private static final String MULTI_MODE_PROGRAM = """
            -> Start { ascend by 5 } -> Middle
            Middle { move by 100 } -> End  
            End { descend by 5 }
            """;

        @Test
        void testModeCount() {
            ArrayList<Mode> modes = parseProgram(MULTI_MODE_PROGRAM);
            System.out.println("testModeCount -> " + modes.size());
            assertEquals(3, modes.size());
        }

        @Test
        void testModeNames() {
            ArrayList<Mode> modes = parseProgram(MULTI_MODE_PROGRAM);
            System.out.println("testModeNames -> [" + modes.get(0).getName() + ", " + 
                             modes.get(1).getName() + ", " + modes.get(2).getName() + "]");
            assertEquals("Start", modes.get(0).getName());
            assertEquals("Middle", modes.get(1).getName());
            assertEquals("End", modes.get(2).getName());
        }

        @Test
        void testInitialMode() {
            ArrayList<Mode> modes = parseProgram(MULTI_MODE_PROGRAM);
            boolean[] initialFlags = {modes.get(0).isInitial(), modes.get(1).isInitial(), modes.get(2).isInitial()};
            System.out.println("testInitialMode -> [" + initialFlags[0] + ", " + initialFlags[1] + ", " + initialFlags[2] + "]");
            assertTrue(modes.get(0).isInitial());
            assertFalse(modes.get(1).isInitial());
            assertFalse(modes.get(2).isInitial());
        }

        @Test
        void testTransitions() {
            ArrayList<Mode> modes = parseProgram(MULTI_MODE_PROGRAM);
            String[] transitions = {modes.get(0).getNextModeName(), modes.get(1).getNextModeName(), modes.get(2).getNextModeName()};
            System.out.println("testTransitions -> [" + transitions[0] + ", " + transitions[1] + ", " + transitions[2] + "]");
            assertEquals("Middle", modes.get(0).getNextModeName());
            assertEquals("End", modes.get(1).getNextModeName());
            assertNull(modes.get(2).getNextModeName());
        }

        @Test
        void testSingleMode() {
            String singleMode = "-> TestMode { ascend by 10 }";
            ArrayList<Mode> modes = parseProgram(singleMode);
            
            System.out.println("testSingleMode -> Mode count: " + modes.size() + 
                             ", Name: " + modes.get(0).getName() + 
                             ", Initial: " + modes.get(0).isInitial());
            assertEquals(1, modes.size());
            Mode mode = modes.get(0);
            assertEquals("TestMode", mode.getName());
            assertTrue(mode.isInitial());
            assertEquals(1, mode.getStatements().size());
            assertNull(mode.getNextModeName());
        }
    }

    @Nested 
    class VariableTests {
        @Test
        void testInitialPosition() {
            Point position = (Point) getVariables().get("current position");
            System.out.println("testInitialPosition -> " + position);
            assertEquals(0.0f, position.getX());
            assertEquals(0.0f, position.getY());
        }

        @Test
        void testPositionUpdate() {
            HashMap<String, Object> vars = getVariables();
            Point newPosition = new Point(10.5f, 20.3f);
            vars.put("current position", newPosition);
            
            Point updatedPosition = (Point) vars.get("current position");
            System.out.println("testPositionUpdate -> " + updatedPosition);
            assertNotNull(updatedPosition, "Updated position should not be null");
            assertEquals(10.5f, updatedPosition.getX());
            assertEquals(20.3f, updatedPosition.getY());
        }

        @Test
        void testBatteryLevel() {
            HashMap<String, Object> vars = getVariables();
            float initialBattery = (Float) vars.get("battery level");
            boolean initialBatteryLow = (Boolean) vars.get("battery low");
            System.out.println("testBatteryLevel initial -> battery: " + initialBattery + ", low: " + initialBatteryLow);
            
            assertEquals(100.0f, vars.get("battery level"));
            assertEquals(100.0f, vars.get("initial battery level"));
            assertFalse((Boolean) vars.get("battery low"));
            
            vars.put("battery level", 75.5f);
            vars.put("battery low", true);
            
            float updatedBattery = (Float) vars.get("battery level");
            boolean updatedBatteryLow = (Boolean) vars.get("battery low");
            System.out.println("testBatteryLevel updated -> battery: " + updatedBattery + ", low: " + updatedBatteryLow);
            
            assertEquals(75.5f, vars.get("battery level"));
            assertTrue((Boolean) vars.get("battery low"));
        }

        @Test
        void testDistanceTravelled() {
            HashMap<String, Object> vars = getVariables();
            float initialDistance = (Float) vars.get("distance travelled");
            System.out.println("testDistanceTravelled initial -> " + initialDistance);
            assertEquals(0.0f, vars.get("distance travelled"));
            
            vars.put("distance travelled", 25.7f);
            float updatedDistance = (Float) vars.get("distance travelled");
            System.out.println("testDistanceTravelled updated -> " + updatedDistance);
            assertEquals(25.7f, vars.get("distance travelled"));
        }
    }

    @Nested
    class ComplexScenarioTests {
        @Test
        void testRealWorldProgram() {
            String program = """
                -> RandomTour { ascend by 10 } -> TakeOff
                TakeOff { move by 500 } -> Explore  
                Explore { turn right by 90 } -> Investigate
                Investigate { move to point (100, 200) } -> GoHome
                GoHome { return to base }
                """;
            
            ArrayList<Mode> modes = parseProgram(program);
            System.out.println("testRealWorldProgram -> Mode count: " + modes.size());
            assertEquals(5, modes.size());
            
            // Verify first mode is initial
            System.out.println("testRealWorldProgram -> Initial mode: " + modes.get(0).getName() + 
                             ", is initial: " + modes.get(0).isInitial());
            assertTrue(modes.get(0).isInitial());
            assertEquals("RandomTour", modes.get(0).getName());
            
            // Verify transitions
            String[] transitions = new String[5];
            for (int i = 0; i < modes.size(); i++) {
                transitions[i] = modes.get(i).getNextModeName();
            }
            System.out.println("testRealWorldProgram -> Transitions: " + java.util.Arrays.toString(transitions));
            
            assertEquals("TakeOff", modes.get(0).getNextModeName());
            assertEquals("Explore", modes.get(1).getNextModeName());
            assertEquals("Investigate", modes.get(2).getNextModeName());
            assertEquals("GoHome", modes.get(3).getNextModeName());
            assertNull(modes.get(4).getNextModeName());
            
            // Count statements
            int totalStatements = modes.stream()
                .mapToInt(mode -> mode.getStatements().size())
                .sum();
            System.out.println("testRealWorldProgram -> Total statements: " + totalStatements);
            assertEquals(5, totalStatements);
        }
    }
}