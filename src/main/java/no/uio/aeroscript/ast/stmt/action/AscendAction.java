package no.uio.aeroscript.ast.stmt.action;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.DroneState;

public class AscendAction extends Statement {

    private final NumberExpression numberAscendingExpression;
    private final NumberExpression numberSpeedExpression;
    private final NumberExpression numberDurationExpression;
    
    private final DroneState droneState;

    public AscendAction(DroneState droneState,NumberExpression numberAscendingExpression, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
        this.droneState = droneState;
        this.numberAscendingExpression = numberAscendingExpression;
        this.numberSpeedExpression = numberSpeedExpression;
        this.numberDurationExpression = numberDurationExpression;
    }

    @Override
    public void execute() {
        Object ascendingValue = numberAscendingExpression.evaluate();
        Float ascending = Float.parseFloat(ascendingValue.toString());
        droneState.setCurrentAltitude(droneState.getCurrentAltitude() + ascending);
        
        Float speed = numberSpeedExpression != null ? Float.parseFloat(numberSpeedExpression.evaluate().toString()) : 1.0f;
        Float time = numberDurationExpression != null ? Float.parseFloat(numberDurationExpression.evaluate().toString()) : ascending / speed;

        Float batteryCost = (ascending * 0.6f) + (time * 0.1f) + (speed * 1.0f);
        droneState.setBatteryLevel(droneState.getBatteryLevel() - batteryCost);
    }

    @Override
    public String toString() {
        return "AscendAction{" +
                "numberAscendingExpression=" + numberAscendingExpression.toString() +
                ", numberSpeedExpression=" + numberSpeedExpression.toString() +
                ", numberDurationExpression=" + numberDurationExpression.toString() +
                '}';
    }
}
