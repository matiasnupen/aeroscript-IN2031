package no.uio.aeroscript.ast.stmt.action.moveAction;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.DroneState;
import no.uio.aeroscript.type.Point;

public class MoveActionByNumber extends Statement {

    private final NumberExpression numberExpression;
    private final Float speed;
    private final Float secondsTravelling;

    public MoveActionByNumber(NumberExpression numberExpression, Float speed, Float secondsTravelling) {
        this.numberExpression = numberExpression;
        this.speed = speed != null ? speed : 1.0f; // Default speed to 1.0 if not provided (m/s)
        this.secondsTravelling = secondsTravelling;
    }

    @Override
    public void execute() { // TODO: fix comments and javadoc
        // Evaluate the number expression to get the distance to move
        Object distanceValue = numberExpression.evaluate();
        float distance = Float.parseFloat(distanceValue.toString());

        DroneState droneState = DroneState.getInstance();
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
        return "MoveActionByNumber: " + numberExpression.toString();
    }
}
