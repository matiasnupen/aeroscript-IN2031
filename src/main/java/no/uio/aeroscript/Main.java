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
import org.jline.terminal.TerminalBuilder;

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

                // TODO: implement TypeChecking here for oblig4

                Interpreter interpreter = new Interpreter(heap, stack);
                ArrayList<Mode> modes = (ArrayList<Mode>) interpreter.visitProgram(programContext);
                
                // Initialize the program (don't execute blocking modes)
                interpreter.initializeProgram(modes);
                
                REPL repl = new REPL(heap, interpreter.getListeners(), interpreter.getCurrentMode());

                // Display simple launch screen
                System.out.println("\n" + "=".repeat(60));
                System.out.println("           AEROSCRIPT MISSION CONTROL");
                System.out.println("=".repeat(60));
                System.out.printf("Mission File: %s\n", path);
                System.out.printf("Battery: %.1f%% | Position: %s\n", batteryLevel, initialPosition);
                System.out.println("-".repeat(60));
                System.out.println("System ready! Type 'help' for commands or 'exit' to quit");
                System.out.println("=".repeat(60) + "\n");
                
                // Create the terminal and reader once, outside the loop
                LineReader reader = null;
                try {
                    reader = LineReaderBuilder.builder()
                            .terminal(TerminalBuilder.builder()
                                    .system(true)
                                    .build())
                            .build();
                } catch (Exception e) {
                    // Fallback to simple console input if JLine fails
                    logger.warning("JLine terminal setup failed, using simple console input: " + e.getMessage());
                    java.util.Scanner scanner = new java.util.Scanner(System.in);
                    
                    while(!repl.isTerminating()) {
                        try {
                            System.out.print("MO> ");
                            if (!scanner.hasNextLine()) break;
                            String next = scanner.nextLine();
                            if (next == null) break;

                            String[] splits = next.trim().split(" ", 2);
                            String left = splits.length == 1 ? "" : splits[1].trim();

                            repl.command(splits[0], left);
                        } catch (Exception ex) {
                            logger.severe("REPL error: " + ex.getClass().getName() + " - " + ex.getMessage());
                            break;
                        }
                    }
                    scanner.close();
                    return;
                }
                
                // Main REPL loop with JLine
                while(!repl.isTerminating()) {
                    try {
                        String next = reader.readLine("MO> ");
                        if (next == null) break;

                        String[] splits = next.trim().split(" ", 2);
                        String left = splits.length == 1 ? "" : splits[1].trim();

                        repl.command(splits[0], left);
                    } catch (org.jline.reader.EndOfFileException e) {
                        // Expected when input is closed, exit gracefully
                        logger.info("Input closed, terminating REPL");
                        break;
                    } catch (Exception e) {
                        logger.severe("REPL error: " + e.getClass().getName() + " - " + e.getMessage());
                        e.printStackTrace();
                        break;
                    }
                }

                @SuppressWarnings("unchecked")
                HashMap<String, Object> finalVars = (HashMap<String, Object>) heap.get(Memory.VARIABLES);
                Point finalPosition = (Point) finalVars.get("current position");
                Float initialBatteryLevel = (Float) finalVars.get("initial battery level");
                Float finalBatteryLevel = (Float) finalVars.get("battery level");
                Float distanceTravelled = (Float) finalVars.get("distance travelled");

                displaySessionSummary(path, initialPosition, initialBatteryLevel, finalPosition, finalBatteryLevel, distanceTravelled);
            } catch (ParseCancellationException e) {
                logger.severe("Parser error: " + e.getMessage());
            }
        }  catch (/*IOException e*/Exception e) {
            logger.severe("Error reading file: " + e.getMessage());
        }
    }
    
    private static void displaySessionSummary(String missionFile, Point initialPos, Float initialBattery, 
                                            Point finalPos, Float finalBattery, Float distance) {
        System.out.println();
        System.out.println("=".repeat(50));
        System.out.println("           SESSION COMPLETED");
        System.out.println("=".repeat(50));
        System.out.printf("Session File    : %s%n", missionFile);
        System.out.printf("Initial Position: %s%n", initialPos);
        System.out.printf("Final Position  : %s%n", finalPos);
        System.out.printf("Initial Battery : %.1f%%%n", initialBattery);
        System.out.printf("Final Battery   : %.1f%%%n", finalBattery);
        System.out.printf("Distance Flown  : %.1f units%n", distance);
        System.out.println("=".repeat(50));
        System.out.println();
    }
}