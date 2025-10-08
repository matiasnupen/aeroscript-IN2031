package no.uio.aeroscript.ast.stmt.action;

import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.DroneState;

public class DockAction extends Statement {

    private final DroneState droneState;

    public DockAction(DroneState droneState) {
        this.droneState = droneState;
    }

    @Override
    public void execute() {
        droneState.goToBase();
        System.out.println("Drone is returning to base");
    }

    @Override
    public String toString() {
        return "DockAction: RETURN TO BASE";
    }
}
