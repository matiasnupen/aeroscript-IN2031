package no.uio.aeroscript.ast.stmt.action.moveAction;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.DroneState;
import no.uio.aeroscript.type.Point;

public class MoveActionByNumber extends Statement {

    private final NumberExpression numberDistanceExpression;
    private final NumberExpression numberSpeedExpression;
    private final NumberExpression numberDurationExpression;

    private final DroneState droneState;

    public MoveActionByNumber(DroneState droneState, NumberExpression numberDistanceExpression, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
        this.droneState = droneState;
        this.numberDistanceExpression = numberDistanceExpression;
        this.numberSpeedExpression = numberSpeedExpression;
        this.numberDurationExpression = numberDurationExpression;
    }

    @Override
    public void execute() {
        // Evaluate the number expression to get the distance to move
        Object distanceValue = numberDistanceExpression.evaluate();
        Float distance = Float.parseFloat(distanceValue.toString());
        Point currentPosition = droneState.getCurrentPosition();

        // Moving along the x-axis (STANDARD FOR THIS PROGRAM)
        Point newPosition = new Point(currentPosition.getX() + distance, currentPosition.getY());
        droneState.setCurrentPosition(newPosition);

        Float currentDistanceTraveled = droneState.getTotalDistanceTraveled();
        droneState.setTotalDistanceTraveled(currentDistanceTraveled + distance);

        Float speed = numberSpeedExpression != null ? Float.parseFloat(numberSpeedExpression.evaluate().toString()) : 1.0f;
        Float time = numberDurationExpression != null ? Float.parseFloat(numberDurationExpression.evaluate().toString()) : distance / speed;

        Float batteryCost = (distance * 0.5f) + (time * 0.1f) + (speed * 1.0f);
        droneState.setBatteryLevel(droneState.getBatteryLevel() - batteryCost);
    }

    @Override
    public String toString() {
        return "MoveActionByNumber{" +
                "numberDistanceExpression=" + numberDistanceExpression.toString() +
                ", numberSpeedExpression=" + numberSpeedExpression.toString() +
                ", numberDurationExpression=" + numberDurationExpression.toString() +
                '}';
    }
}
