package no.uio.aeroscript.ast.stmt.action;

import no.uio.aeroscript.ast.stmt.Action;
import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.runtime.DroneState;

public class DockAction extends Action {

    private Float altitudeBeforeExecution;

    public DockAction(DroneState droneState, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
        super(droneState, numberSpeedExpression, numberDurationExpression);
    }

    @Override
    protected void executeAction() {
        altitudeBeforeExecution = droneState.getCurrentAltitude();
        super.executeAction();
        droneState.goToBase();
    }

    @Override
    protected Float calculateBatteryCost() {
        Float baseCost = getBaseBatteryCost();
        Float altitudeCost = altitudeBeforeExecution * 0.01f;
        Float totalCost = baseCost + altitudeCost;
        System.out.println("DEBUG DockAction: baseCost=" + baseCost + ", altitudeCost=" + altitudeCost + ", totalCost=" + totalCost);
        return totalCost;
    }
    
    @Override
    protected String getDetailedActionInfo() {
        return "Docking to base. Current altitude=" + altitudeBeforeExecution + " (altitude cost added to battery)";
    }
    
    @Override
    public String toString() {
        return "DockAction{}";
    }
}
