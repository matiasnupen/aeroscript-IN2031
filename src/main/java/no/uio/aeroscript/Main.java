package no.uio.aeroscript;

import no.uio.aeroscript.antlr.AeroScriptLexer;
import no.uio.aeroscript.antlr.AeroScriptParser;
import no.uio.aeroscript.ast.stmt.Execution;
import no.uio.aeroscript.runtime.Interpreter;
import no.uio.aeroscript.type.Memory;
import no.uio.aeroscript.type.Point;
import no.uio.aeroscript.ast.stmt.Statement;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Main {
    public static void main(String[] args) {
        System.setProperty("org.jline.terminal.dumb", "true");
        HashMap<Memory, Object> heap = new HashMap<>();
        Stack<Statement> stack = new Stack<>();
        float batteryLevel;
        Point initialPosition;

        if (args.length < 1) {
            System.err.println("Usage: java -jar aeroscript.jar <path to file> [-b <battery level>] [-p <x> <y>]");
            System.exit(1);
        }
        String path = args[0];
        // check if the user used the option -b to set the battery level
        if (args.length > 1 && args[1].equals("-b")) {
            batteryLevel = Float.parseFloat(args[2]);
        } else if (args.length > 3 && args[4].equals("-b")) {
            batteryLevel = Float.parseFloat(args[5]);
        } else {
            batteryLevel = 100.0f;
        }

        // check if the user used the option -p to set the initial position
        if (args.length > 1 && args[1].equals("-p")) {
            initialPosition = new Point(Float.parseFloat(args[2]), Float.parseFloat(args[3]));
        } else if (args.length > 3 && args[3].equals("-p")) {
            initialPosition = new Point(Float.parseFloat(args[4]), Float.parseFloat(args[5]));
        } else {
            initialPosition = new Point(0.0f, 0.0f);
        }

        float initialX = initialPosition.getX();
        float initialY = initialPosition.getY();
        float initialZ = 0.0f;

        HashMap<Memory, HashMap<String, Object>> variables = new HashMap<>();
        variables.put(Memory.VARIABLES, new HashMap<>());
        HashMap<String, Object> vars = variables.get(Memory.VARIABLES);

        vars.put("initial position", initialPosition);
        vars.put("current position", initialPosition);
        vars.put("battery level", batteryLevel);
        vars.put("initial battery level", batteryLevel);
        vars.put("distance travelled", 0.0f);
        vars.put("altitude", 0.0f);

        heap.put(Memory.VARIABLES, vars);
        try {
            String content = new String(Files.readAllBytes(Paths.get(path)));
            try {
                // Read all bytes of the file for parsing using the interpreter.
                AeroScriptLexer lexer = new AeroScriptLexer(CharStreams.fromString(content));
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                AeroScriptParser parser = new AeroScriptParser(tokens);
                AeroScriptParser.ProgramContext programContext = parser.program();

                Interpreter interpreter = new Interpreter(heap, stack);
                // Continue implementation for the interpreter

                // Print the initial position, initial battery capacity, final position, final battery level and distance travelled here

                System.out.println("Execution complete!");
            } catch (ParseCancellationException e) {
                System.err.println("Parser error: " + e.getMessage());
            }
        }  catch (/*IOException e*/Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}