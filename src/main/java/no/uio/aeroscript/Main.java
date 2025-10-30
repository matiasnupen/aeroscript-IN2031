package no.uio.aeroscript;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.logging.Logger;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import no.uio.aeroscript.antlr.AeroScriptLexer;
import no.uio.aeroscript.antlr.AeroScriptParser;
import no.uio.aeroscript.ast.stmt.Mode;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.BatteryDepletedException;
import no.uio.aeroscript.runtime.Interpreter;
import no.uio.aeroscript.runtime.REPL;
import no.uio.aeroscript.type.Memory;
import no.uio.aeroscript.type.Point;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {
        System.setProperty("org.jline.terminal.dumb", "true");
        HashMap<Memory, Object> heap = new HashMap<>();
        Stack<Statement> stack = new Stack<>();
        float batteryLevel;
        Point initialPosition;

        if (args.length < 1) {
            logger.warning("Usage: java -jar aeroscript.jar <path to file> [-b <battery level>] [-p <x> <y>]\n");
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
                ArrayList<Mode> modes = (ArrayList<Mode>) interpreter.visitProgram(programContext);
                
                REPL repl = new REPL(heap, interpreter.getListeners(), interpreter.getFirstExecution());
                
                // Start program execution in background thread
                Thread programThread = new Thread(() -> {
                    try {
                        interpreter.executeProgram(modes);
                        logger.info("Program execution completed - REPL remains active for further commands");
                    } catch (BatteryDepletedException e) {
                        logger.info("Program terminated: " + e.getMessage() + " - REPL remains active");
                    }
                    // DON'T terminate REPL - keep it running for continuous interaction
                });

                // Start the program thread
                programThread.start();

                // Run REPL in main thread for better interaction
                System.out.println("Welcome to the AeroScript REPL!");
                System.out.println("Type 'help' for available commands or 'exit' to quit");
                
                while(!repl.isTerminating()) {
                    try {
                        System.setProperty("org.jline.terminal", "jline.UnsupportedTerminal");
                        LineReader reader = LineReaderBuilder.builder().build();
                        String next = reader.readLine("MO> ");
                        if (next == null) break;

                        String[] splits = next.trim().split(" ", 2);
                        String left = splits.length == 1 ? "" : splits[1].trim();

                        repl.command(splits[0], left);
                    } catch (Exception e) {
                        logger.severe("REPL error: " + e.getMessage());
                        break;
                    }
                }

                // Wait for program thread to finish
                try {
                    programThread.join();
                } catch (InterruptedException e) {
                    logger.warning("Program thread interrupted: " + e.getMessage());
                }

                @SuppressWarnings("unchecked")
                HashMap<String, Object> finalVars = (HashMap<String, Object>) heap.get(Memory.VARIABLES);
                Point finalPosition = (Point) finalVars.get("current position");
                Float initialBatteryLevel = (Float) finalVars.get("initial battery level");
                Float finalBatteryLevel = (Float) finalVars.get("battery level");
                Float distanceTravelled = (Float) finalVars.get("distance travelled");

                logger.info("SESSION COMPLETED" + 
                "\n\t" + "Initial Position: " + initialPosition + 
                "\n\t" + "Initial Battery Level: " + initialBatteryLevel + "%" +
                "\n\t" + "Final Position: " + finalPosition +
                "\n\t" + "Final Battery Level: " + finalBatteryLevel + "%" +
                "\n\t" + "Distance Travelled: " + distanceTravelled + "\n"
                );
            } catch (ParseCancellationException e) {
                logger.severe("Parser error: " + e.getMessage());
            }
        }  catch (/*IOException e*/Exception e) {
            logger.severe("Error reading file: " + e.getMessage());
        }
    }
}