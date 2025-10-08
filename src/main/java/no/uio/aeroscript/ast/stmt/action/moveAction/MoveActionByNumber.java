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

        // Evaluate speed and duration expressions
        Float speed = numberSpeedExpression != null ? Float.parseFloat(numberSpeedExpression.evaluate().toString()) : 1.0f;
        Float secondsTravelling = numberDurationExpression != null ? Float.parseFloat(numberDurationExpression.evaluate().toString()) : null;

        Point currentPosition = droneState.getCurrentPosition();

        // Moving along the x-axis (STANDARD FOR THIS PROGRAM)
        Point newPosition = new Point(currentPosition.getX() + distance, currentPosition.getY());
        droneState.setCurrentPosition(newPosition);

        Float currentDistanceTraveled = droneState.getTotalDistanceTraveled();
        droneState.setTotalDistanceTraveled(currentDistanceTraveled + distance);

        // Calculate battery cost: distance * 0.5 + time * 0.1 + speed * 1
        Float time = secondsTravelling != null ? secondsTravelling : distance / speed; // If secondsTravelling is not provided, calculate it based on distance and speed
        Float batteryCost = (distance * 0.5f) + (time * 0.1f) + (speed * 1.0f);

        Float currentBattery = droneState.getBatteryLevel();
        droneState.setBatteryLevel(currentBattery - batteryCost);
    }

    @Override
    public String toString() {
        return "MoveActionByNumber: " + numberDistanceExpression.toString() + (numberSpeedExpression != null ? " at speed " + numberSpeedExpression.toString() : "") + (numberDurationExpression != null ? " for " + numberDurationExpression.toString() + " seconds" : "");
    }
}
