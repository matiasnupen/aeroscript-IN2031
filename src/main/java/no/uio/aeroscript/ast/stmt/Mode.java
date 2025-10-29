package no.uio.aeroscript.ast.stmt;

import java.util.List;
import java.util.logging.Logger;

import no.uio.aeroscript.runtime.DroneState;

public class Mode {

    private static final Logger logger = Logger.getLogger(Mode.class.getName());

    private final String name;
    private final boolean isInitial;
    private final List<Statement> statements;
    private final String nextModeName;

    public Mode(String name, boolean isInitial, List<Statement> statements, String nextModeName) {
        this.name = name;
        this.isInitial = isInitial;
        this.statements = statements;
        this.nextModeName = nextModeName;
    }

    public String getName() { return name; }
    public boolean isInitial() { return isInitial; }
    public List<Statement> getStatements() { return statements; }
    public String getNextModeName() { return nextModeName; }

    public boolean hasTransition() { return nextModeName != null; }

    public void receiveMessage(String messageId) {
        logger.info("Mode " + name + " received message: " + messageId);
        // Just using for logging directly in the Mode-class 
        // TODO: Maybe remove this method if not needed
    }

    public String checkReactions(DroneState droneState) {
        for (Statement statement : statements) {
            if (statement instanceof Reaction) {
                Reaction reaction = (Reaction) statement;
                if (reaction.isTriggered(droneState)) {
                    return reaction.getTargetModeName();
                }
            }
        }
        return null;
    }

    public void executeStatements() {
        for (Statement statement : statements) {
            if (statement instanceof Action) {
                // Execute action
                statement.execute();
            } else if (statement instanceof Reaction) {
                // Register reaction for this mode
                // Implementation depends on the broader context of the interpreter
            }
        }
    }

    // Using StringBuilder for making it easier to read string representation
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isInitial) {
            sb.append("-> ");
        }
        sb.append(name).append(" {\n");
        for (Statement stmt : statements) {
            sb.append("  ").append(stmt.toString()).append("\n");
        }
        sb.append("}");
        if (hasTransition()) {
            sb.append(" -> ").append(nextModeName);
        }
        return sb.toString();
    }
}
