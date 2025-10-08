package no.uio.aeroscript.ast.stmt.action.descendAction;


import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.DroneState;

public class DescendActionToGround extends Statement {

    private final NumberExpression descendingSpeedExpression;
    private final NumberExpression descendingDurationExpression;

    private final DroneState droneState;

    public DescendActionToGround(DroneState droneState, NumberExpression descendingSpeedExpression, NumberExpression descendingDurationExpression) {
        this.droneState = droneState;
        this.descendingSpeedExpression = descendingSpeedExpression;
        this.descendingDurationExpression = descendingDurationExpression;
    }

    @Override
    public void execute() {
        Float currentPreviousAltitude = droneState.getCurrentAltitude();
        droneState.setCurrentAltitude(0.0f);
        
        Float speed = descendingSpeedExpression != null ? Float.parseFloat(descendingSpeedExpression.evaluate().toString()) : 1.0f;
        Float time = descendingDurationExpression != null ? Float.parseFloat(descendingDurationExpression.evaluate().toString()) : currentPreviousAltitude / speed;

        Float batteryCost = (currentPreviousAltitude * 0.6f) + (time * 0.1f) + (speed * 1.0f);
        droneState.setBatteryLevel(droneState.getBatteryLevel() - batteryCost);
    }

    @Override
    public String toString() {
        return "DescendActionToGround{" +
                "descendingSpeedExpression=" + descendingSpeedExpression +
                ", descendingDurationExpression=" + descendingDurationExpression +
                '}';
    }
}
