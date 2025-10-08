package no.uio.aeroscript.ast.stmt.action;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.DroneState;

public class DockAction extends Statement {

    private final NumberExpression numberSpeedExpression;
    private final NumberExpression numberDurationExpression;
    private final DroneState droneState;

    public DockAction(DroneState droneState, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
        this.droneState = droneState;
        this.numberSpeedExpression = numberSpeedExpression;
        this.numberDurationExpression = numberDurationExpression;
    }

    @Override
    public void execute() {
        Float speed = numberSpeedExpression != null ? Float.parseFloat(numberSpeedExpression.evaluate().toString()) : 1.0f;
        Float time = numberDurationExpression != null ? Float.parseFloat(numberDurationExpression.evaluate().toString()) : droneState.getCurrentAltitude() / speed;

        Float batteryCost = (droneState.getCurrentAltitude()) + (time * 0.1f) + (speed * 1.0f);
        droneState.setBatteryLevel(droneState.getBatteryLevel() - batteryCost);

        droneState.goToBase();
    }

    @Override
    public String toString() {
        return "DockAction{" +
                "numberSpeedExpression=" + numberSpeedExpression.toString() +
                ", numberDurationExpression=" + numberDurationExpression.toString() +
                '}';
    }
}
