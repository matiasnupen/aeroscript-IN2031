package no.uio.aeroscript.ast.stmt.action;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.expr.PointExpression;
import no.uio.aeroscript.ast.stmt.Action;
import no.uio.aeroscript.runtime.DroneState;
import no.uio.aeroscript.type.Point;

public abstract class MoveAction extends Action {

    protected static final Float DISTANCE_BATTERY_MULTIPLIER = 0.05f;

    protected MoveAction(DroneState droneState, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
        super(droneState, numberSpeedExpression, numberDurationExpression);
    }

    @Override
    protected void executeAction() {
        super.executeAction();
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
        protected static final Float DISTANCE_BATTERY_MULTIPLIER = 0.07f;
        private final NumberExpression numberDistanceExpression;
        private Float cachedDistance = null;

        public ByDistance(DroneState droneState, NumberExpression numberDistanceExpression, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
            super(droneState, numberSpeedExpression, numberDurationExpression);
            this.numberDistanceExpression = numberDistanceExpression;
        }

        private Float getDistance() {
            if (cachedDistance == null) {
                cachedDistance = Float.parseFloat(numberDistanceExpression.evaluate().toString());
            }
            return cachedDistance;
        }

        @Override
        protected Point calculateNewPosition(Point currentPosition) {
            Float distance = getDistance();
            // Moving along the x-axis (STANDARD FOR THIS PROGRAM)
            return new Point(currentPosition.getX() + distance, currentPosition.getY());
        }

        @Override
        protected String getDetailedActionInfo() {
            Float distance = getDistance();
            Point currentPos = droneState.getCurrentPosition();
            Point newPos = new Point(currentPos.getX() + distance, currentPos.getY());
            return "Move by distance=" + distance + ", from=" + currentPos + " to=" + newPos + ", battery_multiplier=" + DISTANCE_BATTERY_MULTIPLIER;
        }

        @Override
        public String toString() {
            return "MoveAction.ByDistance{distance=" + numberDistanceExpression.toString() + '}';
        }
    }

    public static class ByPoint extends MoveAction {
        protected static final Float DISTANCE_BATTERY_MULTIPLIER = 0.07f;
        private final PointExpression pointExpression;
        private Point cachedTargetPoint = null;

        public ByPoint(DroneState droneState, PointExpression pointExpression, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
            super(droneState, numberSpeedExpression, numberDurationExpression);
            this.pointExpression = pointExpression;
        }

        private Point getTargetPoint() {
            if (cachedTargetPoint == null) {
                cachedTargetPoint = (Point) pointExpression.evaluate();
            }
            return cachedTargetPoint;
        }

        @Override
        protected Point calculateNewPosition(Point currentPosition) {
            return getTargetPoint();
        }

        @Override
        protected String getDetailedActionInfo() {
            Point currentPos = droneState.getCurrentPosition();
            Point targetPos = getTargetPoint();
            Float distance = currentPos.distanceTo(targetPos);
            return "Move to point=" + targetPos + ", from=" + currentPos + ", distance=" + distance + ", battery_multiplier=" + DISTANCE_BATTERY_MULTIPLIER;
        }

        @Override
        public String toString() {
            return "MoveAction.ByPoint{point=" + pointExpression.toString() + '}';
        }
    }
}
