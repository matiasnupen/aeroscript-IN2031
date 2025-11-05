package no.uio.aeroscript.runtime;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import no.uio.aeroscript.ast.stmt.Mode;
import no.uio.aeroscript.type.Memory;
import no.uio.aeroscript.type.Obstacle;
import no.uio.aeroscript.type.Point;

class Command {
    private final String name;
    private final REPL repl;
    private final Function<String, Boolean> command;
    private final String help;
    private final boolean requiresParameter;
    private final String parameterHelp;

    public Command(String name, REPL repl, Function<String, Boolean> command, String help, boolean requiresParameter, String parameterHelp) {
        this.name = name;
        this.repl = repl;
        this.command = command;
        this.help = help;
        this.requiresParameter = requiresParameter;
        this.parameterHelp = parameterHelp;
    }

    public String getName() {
        return name;
    }

    public String getHelp() {
        return help;
    }

    public boolean isRequiresParameter() {
        return requiresParameter;
    }

    public String getParameterHelp() {
        return parameterHelp;
    }

    public boolean execute(String param) {
        if (requiresParameter && Objects.equals(param, "")) {
            System.out.println(name + " requires a parameter: " + parameterHelp);
            return false;
        }

        return command.apply(param);
    }
}

public class REPL {
    private final HashMap<Memory, Object> heap;
    private final HashMap<String, Command> commands = new HashMap<>();
    private final Map<String, Runnable> listeners;
    private final Mode initialMode;
    private boolean terminating = false;

    public REPL(HashMap<Memory, Object> heap, Map<String, Runnable> listeners, Mode initialMode) {
        this.heap = heap;
        this.listeners = listeners;
        this.initialMode = initialMode;
        initCommands();
    }

    public boolean isTerminating() {
        return terminating;
    }

    private void printRepl(String str) {
        System.out.println("MO-out> " + str);
    }

    public boolean command(String str, String param) {
        LocalTime start = LocalTime.now();
        boolean result;

        if (str.equals("help")) {
            Command helpCmd = commands.get("help");
            helpCmd.execute("");
            result = true;
        } else {
            Command cmd = commands.get(str);

            if (cmd == null) {
                printRepl("Unknown command: " + str + ". Type \"help\" to get a list of available commands");
                result = false;
            } else {
                try {
                    cmd.execute(param);
                    result = true;
                } catch (Exception e) {
                    printRepl("Command " + str + " " + param + " caused an exception. Internal state may be inconsistent");
                    result = false;
                }
            }
        }

        float time = Duration.between(start, LocalTime.now()).toMillis() / 1000f;
        printRepl("Command executed in " + time + " seconds");

        return result;
    }

    /** Initialize the available commands for the REPL
     * @command status - Show drone status and current mode
     * @command messages - List all available messages
     * @command obstacles - Simulate obstacle detection at coordinates
     * @command message - Send a specific message to the drone
     * @command obstacle - Creates an obstacle detection at coordinates
     * @command info - Show detailed system information
     * @command help - Show available commands and help
     * @command exit - Exit the mission control center
     * 
     * Each command has a design that shows the necessary information in a clear format.
     */
    private void initCommands() {
        commands.put("help", new Command("help", this, (param) -> {
            printRepl("=== AEROSCRIPT MISSION CONTROL CENTER ===");
            printRepl("Available Commands:");
            printRepl("┌──────────────────────┬────────────────────────────────────────────┐");
            printRepl("│ Command              │ Description                                │");
            printRepl("├──────────────────────┼────────────────────────────────────────────┤");
            printRepl("│ generateObstacles    │ Generate random obstacles                  │");
            printRepl("│ status               │ Show drone status and current mode         │");
            printRepl("│ messages             │ List all available messages                │");
            printRepl("│ obstacles            │ Shows all detected obstacles               │");
            printRepl("│ message <id>         │ Send a specific message to the drone       │");
            printRepl("│ obstacle x,y         │ Simulate obstacle detection at coordinates │");
            printRepl("│ info                 │ Show detailed system information           │");
            printRepl("│ help                 │ Show this help menu                        │");
            printRepl("│ exit                 │ Exit the mission control center            │");
            printRepl("└──────────────────────┴────────────────────────────────────────────┘");
            return true;
        }, "Show available commands and help", false, ""));

        commands.put("generateObstacles", new Command("generateObstacles", this, (param) -> {
            System.out.println(">>> GENERATING 5 RANDOM OBSTACLES...\n\n");
            DroneState droneState = DroneState.getInstance();
            droneState.generateRandomObstacles(5);
            printRepl(">>> Generated 5 random obstacles in the environment.");
            return true;
        }, "Generate random obstacles", false, ""));

        commands.put("messages", new Command("messages", this, (param) -> {
            printRepl("=== AVAILABLE MESSAGES ===");
            if (listeners.isEmpty()) {
                printRepl("No messages available in current mode.");
            } else {
                int count = 1;
                for (String messageId : listeners.keySet()) {
                    printRepl(count + ". " + messageId);
                    count++;
                }
                printRepl("");
                printRepl("Usage: message <message_id>");
                printRepl("Example: message " + listeners.keySet().iterator().next());
            }
            return true;
        }, "List all available messages", false, ""));

        commands.put("message", new Command("message", this, (param) -> {
            if (param.trim().isEmpty()) {
                printRepl("Please specify a message to send.");
                printRepl("Use 'messages' command to see available options.");
                return true;
            }

            Runnable listener = listeners.get(param);
            if (listener != null) {
                printRepl(">>> Sending message: " + param);
                // Add message to DroneState for MessageEvent detection
                DroneState.getInstance().addReceivedMessage(param);
                listener.run(); // Execute the listener
                printRepl(">>> Message '" + param + "' sent successfully!");
            } else {
                printRepl("ERROR: Unknown message '" + param + "'");
                printRepl("Available messages: " + listeners.keySet());
                printRepl("Use 'messages' to see all options.");
            }
            return true;
        }, "Send a message to trigger drone actions", true, "Message ID to send"));

        commands.put("status", new Command("status", this, (param) -> {
            DroneState droneState = DroneState.getInstance();
            printRepl("=== DRONE STATUS REPORT ===");
            printRepl("┌─────────────────────┬─────────────────────────────┐");
            printRepl("│ Parameter           │ Value                       │");
            printRepl("├─────────────────────┼─────────────────────────────┤");
            printRepl(String.format("│ Position            │ %-27s │",  droneState.getCurrentPosition()));
            printRepl(String.format("│ Altitude            │ %-27.1f │", droneState.getCurrentAltitude()));
            printRepl(String.format("│ Battery Level       │ %-27s │", String.format("%.1f%%", droneState.getBatteryLevel())));
            printRepl(String.format("│ Distance Travelled  │ %-27.1f │", droneState.getTotalDistanceTravelled()));
            printRepl(String.format("│ Current Mode        │ %-27s │", (initialMode != null ? initialMode.getName() : "Unknown")));
            printRepl(String.format("│ Available Messages  │ %-27d │", listeners.size()));
            printRepl("└─────────────────────┴─────────────────────────────┘");
            if (!listeners.isEmpty()) {
                printRepl("Use 'messages' to see available message options.");
            }
            return true;
        }, "Show comprehensive drone status", false, "")); 

        commands.put("info", new Command("info", this, (param) -> {
            printRepl("=== SYSTEM INFORMATION ===");
            printRepl("Available Commands: " + commands.size());
            printRepl("Active Listeners: " + listeners.size());
            printRepl("Memory Variables: " + heap.size());
            if (initialMode != null) {
                printRepl("Current Mode: " + initialMode.getName());
            }
            printRepl("System: AeroScript Mission Control v1.0");
            return true;
        }, "Show system information", false, ""));

        commands.put("obstacles", new Command("obstacles", this, (param) -> {
            DroneState droneState = DroneState.getInstance();
            printRepl("=== DETECTED OBSTACLES ===");
            if (droneState.getDetectedObstacles().isEmpty()) {
                printRepl("No obstacles detected.");
            } else {
                int count = 1;
                for (Obstacle obstacle : droneState.getDetectedObstacles()) {
                    printRepl(count + ". " + obstacle);
                    count++;
                }
            }
            return true;
        }, "Show all detected obstacles", false, ""));

        commands.put("obstacle", new Command("obstacle", this, (param) -> {
            if (param.trim().isEmpty()) {
                printRepl("Please specify obstacle coordinates, as well as its name.");
                printRepl("Usage: obstacle <name> <x> <y> or obstacle <name> at <x>, <y>");
                printRepl("Example: obstacle rock 10.5 20.0 or obstacle rock at 10.5, 20.0");
                return true;
            }
            
            String obstacleName;
            String[] coords;
            
            // Check for natural language format "name at x, y"
            if (param.toLowerCase().contains(" at ")) {
                String[] atParts = param.split("(?i)\\s+at\\s+"); // Case-insensitive split on " at "
                if (atParts.length == 2) {
                    obstacleName = atParts[0].trim();
                    String coordPart = atParts[1].trim();
                    
                    // Handle comma-separated coordinates
                    if (coordPart.contains(",")) {
                        coords = coordPart.split("\\s*,\\s*");
                    } else {
                        coords = coordPart.split("\\s+");
                    }
                } else {
                    printRepl("ERROR: Invalid 'at' format. Use: obstacle <name> at <x>, <y>");
                    return true;
                }
            } else {
                // Original format handling
                String[] parts = param.trim().split("\\s+");
                if (parts.length < 3) {
                    obstacleName = "Simulated Obstacle"; // Default name
                    if (param.contains(",")) {
                        coords = param.trim().split("\\s*,\\s*");
                    } else {
                        coords = param.trim().split("\\s+");
                    }
                } else {
                    obstacleName = parts[0];
                    String coordPart = param.substring(parts[0].length()).trim();
                    
                    if (coordPart.contains(",")) {
                        coords = coordPart.split("\\s*,\\s*");
                    } else {
                        coords = coordPart.split("\\s+");
                    }
                }
            }
            
            if (coords.length != 2) {
                printRepl("ERROR: Invalid coordinates format.");
                printRepl("Usage: obstacle <name> <x> <y> or obstacle <name> at <x>, <y>");
                return true;
            }
            
            try {
                float x = Float.parseFloat(coords[0]);
                float y = Float.parseFloat(coords[1]);
                Point obstacleLocation = new Point(x, y);
                Obstacle obstacle = new Obstacle(obstacleName, obstacleLocation);
                
                DroneState.getInstance().discoverObstacle(obstacle);
                printRepl(">>> " + obstacleName + " OBSTACLE DETECTED at (" + x + ", " + y + ")!");
                printRepl(">>> Obstacle event triggered!");
                
            } catch (NumberFormatException e) {
                printRepl("ERROR: Invalid number format. Use decimal numbers.");
                printRepl("Example: obstacle rock 10.5 20.0 or obstacle rock at 10.5, 20.0");
            }
            return true;
        }, "Simulate an obstacle detection", true, "name x y or name at x, y"));

        commands.put("exit", new Command("exit", this, (param) -> {
            printRepl("=== MISSION CONTROL SHUTDOWN ===");
            printRepl("Shutting down mission control center...");
            terminate();
            return true;
        }, "Exit the mission control center", false, ""));
    }

    public void terminate() {
        System.out.println("Terminating the REPL");
        this.terminating = true;
    }
}
