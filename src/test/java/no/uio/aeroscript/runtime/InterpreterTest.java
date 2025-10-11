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
    class ExpressionTests {
        @Test
        void testBasicArithmetic() {
            assertEquals(5.0f, evaluateExpression("2 + 3"));
            assertEquals(-1.0f, evaluateExpression("2 - 3"));
            assertEquals(6.0f, evaluateExpression("2 * 3"));
        }

        @Test
        void testComplexExpressions() {
            assertEquals(14.0f, evaluateExpression("2 * (3 + 4)"));
            assertEquals(10.0f, evaluateExpression("(2 + 3) * 2"));
            assertEquals(26.0f, evaluateExpression("2 * 10 + 3 * 2"));
        }

        @Test
        void testDecimalNumbers() {
            assertEquals(3.5f, evaluateExpression("1.5 + 2"));
            assertEquals(2.25f, evaluateExpression("1.5 * 1.5"));
        }

        @Test
        void testNegation() {
            assertEquals(-1.0f, evaluateExpression("-- 1"));
            assertEquals(-2.0f, evaluateExpression("-- 2"));
        }

        @Test
        void testPointExpression() {
            AeroScriptLexer lexer = new AeroScriptLexer(CharStreams.fromString("point (10, 20)"));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            AeroScriptParser parser = new AeroScriptParser(tokens);
            
            Object result = interpreter.visitExpression(parser.expression()).evaluate();
            assertTrue(result instanceof Point);
            
            Point point = (Point) result;
            assertEquals(10.0f, point.getX());
            assertEquals(20.0f, point.getY());
        }

        @Test
        void testRangeExpression() {
            AeroScriptLexer lexer = new AeroScriptLexer(CharStreams.fromString("random[5, 15]"));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            AeroScriptParser parser = new AeroScriptParser(tokens);
            
            Object result = interpreter.visitExpression(parser.expression()).evaluate();
            assertTrue(result instanceof Float);
            
            Float randomValue = (Float) result;
            assertTrue(randomValue >= 5.0f && randomValue <= 15.0f);
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
            assertEquals(3, modes.size());
        }

        @Test
        void testModeNames() {
            ArrayList<Mode> modes = parseProgram(MULTI_MODE_PROGRAM);
            assertEquals("Start", modes.get(0).getName());
            assertEquals("Middle", modes.get(1).getName());
            assertEquals("End", modes.get(2).getName());
        }

        @Test
        void testInitialMode() {
            ArrayList<Mode> modes = parseProgram(MULTI_MODE_PROGRAM);
            assertTrue(modes.get(0).isInitial());
            assertFalse(modes.get(1).isInitial());
            assertFalse(modes.get(2).isInitial());
        }

        @Test
        void testTransitions() {
            ArrayList<Mode> modes = parseProgram(MULTI_MODE_PROGRAM);
            assertEquals("Middle", modes.get(0).getNextModeName());
            assertEquals("End", modes.get(1).getNextModeName());
            assertNull(modes.get(2).getNextModeName());
        }

        @Test
        void testSingleMode() {
            String singleMode = "-> TestMode { ascend by 10 }";
            ArrayList<Mode> modes = parseProgram(singleMode);
            
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
            assertEquals(0.0f, position.getX());
            assertEquals(0.0f, position.getY());
        }

        @Test
        void testPositionUpdate() {
            HashMap<String, Object> vars = getVariables();
            Point newPosition = new Point(10.5f, 20.3f);
            vars.put("current position", newPosition);
            
            Point updatedPosition = (Point) vars.get("current position");
            assertNotNull(updatedPosition, "Updated position should not be null");
            assertEquals(10.5f, updatedPosition.getX());
            assertEquals(20.3f, updatedPosition.getY());
        }

        @Test
        void testBatteryLevel() {
            HashMap<String, Object> vars = getVariables();
            assertEquals(100.0f, vars.get("battery level"));
            assertEquals(100.0f, vars.get("initial battery level"));
            assertFalse((Boolean) vars.get("battery low"));
            
            vars.put("battery level", 75.5f);
            vars.put("battery low", true);
            
            assertEquals(75.5f, vars.get("battery level"));
            assertTrue((Boolean) vars.get("battery low"));
        }

        @Test
        void testDistanceTravelled() {
            HashMap<String, Object> vars = getVariables();
            assertEquals(0.0f, vars.get("distance travelled"));
            
            vars.put("distance travelled", 25.7f);
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
            assertEquals(5, modes.size());
            
            // Verify first mode is initial
            assertTrue(modes.get(0).isInitial());
            assertEquals("RandomTour", modes.get(0).getName());
            
            // Verify transitions
            assertEquals("TakeOff", modes.get(0).getNextModeName());
            assertEquals("Explore", modes.get(1).getNextModeName());
            assertEquals("Investigate", modes.get(2).getNextModeName());
            assertEquals("GoHome", modes.get(3).getNextModeName());
            assertNull(modes.get(4).getNextModeName());
            
            // Count statements
            int totalStatements = modes.stream()
                .mapToInt(mode -> mode.getStatements().size())
                .sum();
            assertEquals(5, totalStatements);
        }
    }
}