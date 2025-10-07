package no.uio.aeroscript.ast.stmt.action.moveAction;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.expr.PointExpression;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.DroneState;
import no.uio.aeroscript.type.Point;

public class MoveActionByPoint extends Statement {

    private final PointExpression pointExpression;
    private final NumberExpression numberSpeedExpression;
    private final NumberExpression numberSecondsExpression;

    public MoveActionByPoint(PointExpression point, NumberExpression speed, NumberExpression secondsTravelling) {
        this.pointExpression = point;
        this.numberSpeedExpression = speed != null ? speed : new NumberExpression(1.0f); // Default speed to 1.0 if not provided (m/s)
        this.numberSecondsExpression = secondsTravelling;
    }

    @Override
    public void execute() {

        Object evaluatedPoint = pointExpression.evaluate();
        if (!(evaluatedPoint instanceof Point)) {
            throw new RuntimeException("Expected a Point, but got: " + evaluatedPoint);
        }

        Point targetPoint = (Point) evaluatedPoint;

        DroneState droneState = DroneState.getInstance();
        Point currentPosition = droneState.getCurrentPosition();
        droneState.setCurrentPosition(targetPoint);

        Float currentDistanceTraveled = droneState.getTotalDistanceTraveled();
        droneState.setTotalDistanceTraveled(currentDistanceTraveled + targetPoint.distanceTo(currentPosition));

        // Calculate battery cost: distance * 0.5 + time * 0.1 + speed * 1
        Float time = numberSecondsExpression != null ? numberSecondsExpression.evaluate() : currentPosition.distanceTo(targetPoint) / numberSpeedExpression.evaluate();
        Float batteryCost = (currentPosition.distanceTo(targetPoint) * 0.5f) + (time * 0.1f) + (numberSpeedExpression.evaluate() * 1.0f);

        Float currentBattery = droneState.getBatteryLevel();
        droneState.setBatteryLevel(currentBattery - batteryCost);
    }

    @Override
    public String toString() {
        return "MoveActionByPoint: " + pointExpression.toString();
        // TODO: implement toString method
    }

}
