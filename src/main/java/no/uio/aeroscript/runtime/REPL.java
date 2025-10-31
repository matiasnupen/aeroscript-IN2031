package no.uio.aeroscript.runtime;

import no.uio.aeroscript.ast.stmt.Mode;
import no.uio.aeroscript.type.Memory;
import no.uio.aeroscript.type.Obstacle;
import no.uio.aeroscript.runtime.DroneState;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

// Uncomment the following code for the REPL after implementing the mode and the rest of the code
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

    private void initCommands() {
        commands.put("help", new Command("help", this, (param) -> {
            printRepl("=== AEROSCRIPT MISSION CONTROL CENTER ===");
            printRepl("Available Commands:");
            printRepl("┌─────────────┬────────────────────────────────────────────┐");
            printRepl("│ Command     │ Description                                │");
            printRepl("├─────────────┼────────────────────────────────────────────┤");
            printRepl("│ status      │ Show drone status and current mode         │");
            printRepl("│ messages    │ List all available messages                │");
            printRepl("│ obstacles   │ Shows all detected obstacles               │");
            printRepl("│ message <id>│ Send a specific message to the drone       │");
            printRepl("│ obstacle x,y│ Simulate obstacle detection at coordinates │");
            printRepl("│ info        │ Show detailed system information           │");
            printRepl("│ help        │ Show this help menu                        │");
            printRepl("│ exit        │ Exit the mission control center            │");
            printRepl("└─────────────┴────────────────────────────────────────────┘");
            return true;
        }, "Show available commands and help", false, ""));

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
            DroneState drone = DroneState.getInstance();
            printRepl("=== DRONE STATUS REPORT ===");
            printRepl("┌─────────────────────┬─────────────────────────────┐");
            printRepl("│ Parameter           │ Value                       │");
            printRepl("├─────────────────────┼─────────────────────────────┤");
            printRepl(String.format("│ Position            │ %-27s │", drone.getCurrentPosition()));
            printRepl(String.format("│ Altitude            │ %-27.1f │", drone.getCurrentAltitude()));
            printRepl(String.format("│ Battery Level       │ %-27s │", String.format("%.1f%%", drone.getBatteryLevel())));
            printRepl(String.format("│ Distance Travelled  │ %-27.1f │", drone.getTotalDistanceTravelled()));
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
                printRepl("Please specify obstacle coordinates.");
                printRepl("Usage: obstacle <x> <y> or obstacle <x,y>");
                printRepl("Example: obstacle 10.5 20.0 or obstacle 10.5,20.0");
                return true;
            }
            
            String[] coords;
            // Check if parameter contains comma (for x,y format)
            if (param.contains(",")) {
                coords = param.trim().split("\\s*,\\s*"); // Split by comma with optional spaces
            } else {
                coords = param.trim().split("\\s+"); // Split by spaces
            }
            
            if (coords.length != 2) {
                printRepl("ERROR: Invalid coordinates format.");
                printRepl("Usage: obstacle <x> <y> or obstacle <x,y>");
                return true;
            }
            
            try {
                float x = Float.parseFloat(coords[0]);
                float y = Float.parseFloat(coords[1]);
                no.uio.aeroscript.type.Point obstacleLocation = new no.uio.aeroscript.type.Point(x, y);
                no.uio.aeroscript.type.Obstacle obstacle = new no.uio.aeroscript.type.Obstacle("Simulated Obstacle", obstacleLocation);
                
                DroneState.getInstance().discoverObstacle(obstacle);
                printRepl(">>> OBSTACLE DETECTED at (" + x + ", " + y + ")!");
                printRepl(">>> Obstacle event triggered!");
                
            } catch (NumberFormatException e) {
                printRepl("ERROR: Invalid number format. Use decimal numbers.");
                printRepl("Example: obstacle 10.5 20.0 or obstacle 10.5,20.0");
            }
            return true;
        }, "Simulate an obstacle detection", true, "X Y coordinates"));

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
