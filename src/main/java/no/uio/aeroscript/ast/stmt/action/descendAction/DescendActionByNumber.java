package no.uio.aeroscript.ast.stmt.action.descendAction;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.DroneState;

public class DescendActionByNumber extends Statement{

    private final NumberExpression descendingNumberExpression;
    private final NumberExpression descendingSpeedExpression;
    private final NumberExpression descendingDurationExpression;

    private final DroneState droneState;

    public DescendActionByNumber(DroneState droneState, NumberExpression descendingNumberExpression, NumberExpression descendingSpeedExpression, NumberExpression descendingDurationExpression) {
        this.droneState = droneState;
        this.descendingNumberExpression = descendingNumberExpression;
        this.descendingSpeedExpression = descendingSpeedExpression;
        this.descendingDurationExpression = descendingDurationExpression;
    }

    @Override
    public void execute() {
        Object descendingValue = descendingNumberExpression.evaluate();
        Float descending = Float.parseFloat(descendingValue.toString());
        Float currentPreviousAltitude = droneState.getCurrentAltitude();
        droneState.setCurrentAltitude(droneState.getCurrentAltitude() - descending);

        Float speed = descendingSpeedExpression != null ? Float.parseFloat(descendingSpeedExpression.evaluate().toString()) : 1.0f;
        Float time = descendingDurationExpression != null ? Float.parseFloat(descendingDurationExpression.evaluate().toString()) : descending / speed;

        Float batteryCost = (currentPreviousAltitude * 0.6f) + (time * 0.1f) + (speed * 1.0f);
        droneState.setBatteryLevel(droneState.getBatteryLevel() - batteryCost);
    }

    @Override
    public String toString() {
        return "DescendActionByNumber{" +
                "descendingNumberExpression=" + descendingNumberExpression.toString() +
                ", descendingSpeedExpression=" + descendingSpeedExpression.toString() +
                ", descendingDurationExpression=" + descendingDurationExpression.toString() +
                '}';
    }
}
