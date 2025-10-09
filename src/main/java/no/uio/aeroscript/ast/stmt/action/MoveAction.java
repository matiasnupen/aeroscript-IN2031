package no.uio.aeroscript.ast.stmt.action;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.expr.PointExpression;
import no.uio.aeroscript.ast.stmt.Action;
import no.uio.aeroscript.runtime.DroneState;
import no.uio.aeroscript.type.Point;

public abstract class MoveAction extends Action {
    
    protected static final Float DISTANCE_BATTERY_MULTIPLIER = 0.5f;

    protected MoveAction(DroneState droneState, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
        super(droneState, numberSpeedExpression, numberDurationExpression);
    }

    @Override
    protected void executeAction() {
        Point currentPosition = droneState.getCurrentPosition();
        Point newPosition = calculateNewPosition(currentPosition);
        Float distance = currentPosition.distanceTo(newPosition);

        droneState.setCurrentPosition(newPosition);
        droneState.setTotalDistanceTravelled(droneState.getTotalDistanceTravelled() + distance);
    }

    protected abstract Point calculateNewPosition(Point currentPosition);

    @Override
    protected Float calculateBatteryCost() {
        Point currentPosition = droneState.getCurrentPosition();
        Point newPosition = calculateNewPosition(currentPosition);
        Float distance = currentPosition.distanceTo(newPosition);
        return getBaseBatteryCost() + (distance * DISTANCE_BATTERY_MULTIPLIER);
    }

    public static class ByDistance extends MoveAction {
        protected static final Float DISTANCE_BATTERY_MULTIPLIER = 0.7f;
        private final NumberExpression numberDistanceExpression;

        public ByDistance(DroneState droneState, NumberExpression numberDistanceExpression, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
            super(droneState, numberSpeedExpression, numberDurationExpression);
            this.numberDistanceExpression = numberDistanceExpression;
        }

        @Override
        protected Point calculateNewPosition(Point currentPosition) {
            Float distance = Float.parseFloat(numberDistanceExpression.evaluate().toString());
            // Moving along the x-axis (STANDARD FOR THIS PROGRAM)
            return new Point(currentPosition.getX() + distance, currentPosition.getY());
        }

        @Override
        public String toString() {
            return "MoveAction.ByDistance{distance=" + numberDistanceExpression.toString() + '}';
        }
    }

    public static class ByPoint extends MoveAction {
        protected static final Float DISTANCE_BATTERY_MULTIPLIER = 0.7f;
        private final PointExpression pointExpression;

        public ByPoint(DroneState droneState, PointExpression pointExpression, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
            super(droneState, numberSpeedExpression, numberDurationExpression);
            this.pointExpression = pointExpression;
        }

        @Override
        protected Point calculateNewPosition(Point currentPosition) {
            return (Point) pointExpression.evaluate();
        }

        @Override
        public String toString() {
            return "MoveAction.ByPoint{point=" + pointExpression.toString() + '}';
        }
    }
}
