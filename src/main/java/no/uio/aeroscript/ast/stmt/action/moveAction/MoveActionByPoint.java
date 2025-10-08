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

    private final DroneState droneState;

    public MoveActionByPoint(DroneState droneState, PointExpression pointExpression, NumberExpression numberSpeedExpression, NumberExpression numberSecondsExpression) {
        this.droneState = droneState;
        this.pointExpression = pointExpression;
        this.numberSpeedExpression = numberSpeedExpression != null ? numberSpeedExpression : new NumberExpression(1.0f); // Default speed to 1.0 if not provided (m/s)
        this.numberSecondsExpression = numberSecondsExpression != null ? numberSecondsExpression : new NumberExpression(1.0f); // Default seconds to 1.0 if not provided
    }

    @Override
    public void execute() {
        Object evaluatedPoint = pointExpression.evaluate();
        Point targetPoint = (Point) evaluatedPoint;

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
