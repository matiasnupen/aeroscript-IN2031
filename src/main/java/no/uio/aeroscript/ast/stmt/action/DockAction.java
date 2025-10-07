package no.uio.aeroscript.ast.stmt.action;

import no.uio.aeroscript.ast.stmt.Statement;

public class DockAction extends Statement {

    @Override
    public void execute() {
        System.out.println("Returning to base");
        // TODO: implement actual docking logic
    }

    @Override
    public String toString() {
        return "DockAction: RETURN TO BASE";
    }
}
