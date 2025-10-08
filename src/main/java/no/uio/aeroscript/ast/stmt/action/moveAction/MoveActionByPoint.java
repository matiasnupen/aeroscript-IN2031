package no.uio.aeroscript.ast.stmt.action.moveAction;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.expr.PointExpression;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.DroneState;
import no.uio.aeroscript.type.Point;

public class MoveActionByPoint extends Statement {

    private final PointExpression pointExpression;
    private final NumberExpression numberSpeedExpression;
    private final NumberExpression numberDurationExpression;

    private final DroneState droneState;

    public MoveActionByPoint(DroneState droneState, PointExpression pointExpression, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
        this.droneState = droneState;
        this.pointExpression = pointExpression;
        this.numberSpeedExpression = numberSpeedExpression;
        this.numberDurationExpression = numberDurationExpression;
    }

    @Override
    public void execute() {
        Object evaluatedPoint = pointExpression.evaluate();
        Point targetPoint = (Point) evaluatedPoint;

        Point currentPosition = droneState.getCurrentPosition();
        droneState.setCurrentPosition(targetPoint);

        Float currentDistanceTraveled = droneState.getTotalDistanceTraveled();
        Float distanceTraveled = currentPosition.distanceTo(targetPoint);
        droneState.setTotalDistanceTraveled(currentDistanceTraveled + distanceTraveled);

        Float speed = numberSpeedExpression != null ? Float.parseFloat(numberSpeedExpression.evaluate().toString()) : 1.0f;
        Float time = numberDurationExpression != null ? Float.parseFloat(numberDurationExpression.evaluate().toString()) : distanceTraveled / speed;
        
        Float batteryCost = (distanceTraveled * 0.5f) + (time * 0.1f) + (speed * 1.0f);
        droneState.setBatteryLevel(droneState.getBatteryLevel() - batteryCost);
    }

    @Override
    public String toString() {
        return "MoveActionByPoint{" +
                "pointExpression=" + pointExpression.toString() +
                ", numberSpeedExpression=" + numberSpeedExpression.toString() +
                ", numberDurationExpression=" + numberDurationExpression.toString() +
                '}';
    }

}
