package no.uio.aeroscript.ast.stmt;

import java.util.List;

public class Mode {

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

    public void executeStatements() {
        for (Statement statement : statements) {
            statement.execute();
        }
    }

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
