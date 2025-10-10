package no.uio.aeroscript.ast.stmt.action;

import no.uio.aeroscript.ast.stmt.Action;
import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.runtime.DroneState;

public class DockAction extends Action {

    public DockAction(DroneState droneState, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
        super(droneState, numberSpeedExpression, numberDurationExpression);
    }

    @Override
    protected void executeAction() {
        super.executeAction();
        droneState.goToBase();
    }

    @Override
    protected Float calculateBatteryCost() {
        return getBaseBatteryCost() + droneState.getCurrentAltitude() * 0.1f; // Battery cost based on current altitude
    }
    
    @Override
    protected String getDetailedActionInfo() {
        return "Docking to base. Current altitude=" + droneState.getCurrentAltitude() + " (altitude cost added to battery)";
    }
    
    @Override
    public String toString() {
        return "DockAction{}";
    }
}
