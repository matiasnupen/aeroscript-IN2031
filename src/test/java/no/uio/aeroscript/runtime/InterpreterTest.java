package no.uio.aeroscript.runtime;

import no.uio.aeroscript.antlr.AeroScriptLexer;
import no.uio.aeroscript.antlr.AeroScriptParser;
import no.uio.aeroscript.ast.stmt.Execution;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.type.Memory;
import no.uio.aeroscript.type.Point;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {
    private HashMap<Memory, Object> heap;
    private Stack<Statement> stack;

    private void initInterpreter() {
        this.heap = new HashMap<>();
        this.stack = new Stack<>();
        HashMap<Memory, HashMap<String, Object>> variables = new HashMap<>();
        variables.put(Memory.VARIABLES, new HashMap<>());
        HashMap<String, Object> vars = variables.get(Memory.VARIABLES);

        float batteryLevel = 100;
        int initialZ = 0;
        Point initialPosition = new Point(0, 0);

        vars.put("initial position", initialPosition);
        vars.put("current position", initialPosition);
        vars.put("altitude", initialZ);
        vars.put("initial battery level", batteryLevel);
        vars.put("battery level", batteryLevel);
        vars.put("battery low", false);
        vars.put("distance travelled", 0.0f);
        vars.put("initial execution", null);

        heap.put(Memory.VARIABLES, vars);
    }

    private AeroScriptParser.ExpressionContext parseExpression(String expression) {
        AeroScriptLexer lexer = new AeroScriptLexer(CharStreams.fromString(expression));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        AeroScriptParser parser = new AeroScriptParser(tokens);
        return parser.expression();
    }

    @Test
    void getFirstExecution() {
        initInterpreter();
        Interpreter interpreter = new Interpreter(this.heap, this.stack);

        // Implement the test, ensure I have a first execution
        // Also, if you create a new execution and set if as first in the interpreter you should get that
    }

    @Test
    void getPosition() {
        initInterpreter();
        Interpreter interpreter = new Interpreter(this.heap, this.stack);

        // Implement the test, ensure I get the correct position
    }

    @Test
    void getDistanceTravelled() {
        initInterpreter();
        Interpreter interpreter = new Interpreter(this.heap, this.stack);
    }

    @Test
    void getBatteryLevel() {
        initInterpreter();
        Interpreter interpreter = new Interpreter(this.heap, this.stack);
    }

    @Test
    void visitProgram() {
        initInterpreter();
        Interpreter interpreter = new Interpreter(this.heap, this.stack);

        // Implement the test, read a file and parse it, then ensure you have the first execution, and that the number
        // of exeuctions is correct (in the program.aero file there are 9 executions)
    }

    @Test
    void visitExpression() {
        initInterpreter();
        Interpreter interpreter = new Interpreter(this.heap, this.stack);

        assertEquals(5.0f, Float.parseFloat(interpreter.visitExpression(parseExpression("2 + 3")).evaluate().toString()));
        assertEquals(-1.0f, Float.parseFloat(interpreter.visitExpression(parseExpression("2 - 3")).evaluate().toString()));
        assertEquals(6.0f, Float.parseFloat(interpreter.visitExpression(parseExpression("2 * 3")).evaluate().toString()));
        assertEquals(-1, Float.parseFloat(interpreter.visitExpression(parseExpression("-- 1")).evaluate().toString()));
    }
}
