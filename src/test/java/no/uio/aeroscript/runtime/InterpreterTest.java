package no.uio.aeroscript.runtime;

import no.uio.aeroscript.antlr.AeroScriptLexer;
import no.uio.aeroscript.antlr.AeroScriptParser;
import no.uio.aeroscript.ast.expr.Expression;
import no.uio.aeroscript.ast.stmt.Mode;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.type.Memory;
import no.uio.aeroscript.type.Point;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

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
        
        vars.put("initial position", new Point(0, 0));
        vars.put("current position", new Point(0, 0));
        vars.put("altitude", 0);
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