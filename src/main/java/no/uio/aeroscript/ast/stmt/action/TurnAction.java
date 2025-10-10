package no.uio.aeroscript.ast.stmt.action;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.stmt.Action;
import no.uio.aeroscript.runtime.DroneState;

public abstract class TurnAction extends Action {

    protected static final Float TURN_BATTERY_MULTIPLIER = 0.03f;
    protected final NumberExpression numberDegreesExpression;

    protected TurnAction(DroneState droneState, NumberExpression numberDegreesExpression, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
        super(droneState, numberSpeedExpression, numberDurationExpression);
        this.numberDegreesExpression = numberDegreesExpression;
    }

    @Override
    protected void executeAction() {
        super.executeAction();
        Float degrees = Float.parseFloat(numberDegreesExpression.evaluate().toString());
        Float currentPointingDirection = droneState.getCurrentPointingDirection();
        Float newPointingDirection = calculateNewDirection(currentPointingDirection, degrees);
        droneState.setCurrentPointingDirection(newPointingDirection);
    }

    protected abstract Float calculateNewDirection(Float newDirection, Float degrees);
    
    @Override
    protected Float calculateBatteryCost() {
        Float degrees = Float.parseFloat(numberDegreesExpression.evaluate().toString());
        System.out.println("Turning " + this.getClass().getSimpleName() + " by " + degrees + " degrees.");
        return getBaseBatteryCost() + (degrees * TURN_BATTERY_MULTIPLIER);
    }

    public static class Left extends TurnAction {
        public Left(DroneState droneState, NumberExpression numberDegreesExpression, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
            super(droneState, numberDegreesExpression, numberSpeedExpression, numberDurationExpression);
        }

        @Override
        protected Float calculateNewDirection(Float currentDirection, Float degrees) {
            return currentDirection - degrees;
        }

        @Override
        protected String getDetailedActionInfo() {
            Float degrees = Float.parseFloat(numberDegreesExpression.evaluate().toString());
            Float currentDirection = droneState.getCurrentPointingDirection();
            Float newDirection = calculateNewDirection(currentDirection, degrees);
            return "Turn LEFT by degrees=" + degrees + ", from_direction=" + currentDirection + "° to_direction=" + newDirection + "°, battery_multiplier=" + TURN_BATTERY_MULTIPLIER;
        }

        @Override
        public String toString() {
            return "TurnAction.Left{numberDegreesExpression=" + numberDegreesExpression.toString() + '}';
        }
    }

    public static class Right extends TurnAction {
        public Right(DroneState droneState, NumberExpression numberDegreesExpression, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
            super(droneState, numberDegreesExpression, numberSpeedExpression, numberDurationExpression);
        }

        @Override
        protected Float calculateNewDirection(Float currentDirection, Float degrees) {
            return currentDirection + degrees;
        }

        @Override
        protected String getDetailedActionInfo() {
            Float degrees = Float.parseFloat(numberDegreesExpression.evaluate().toString());
            Float currentDirection = droneState.getCurrentPointingDirection();
            Float newDirection = calculateNewDirection(currentDirection, degrees);
            return "Turn RIGHT by degrees=" + degrees + ", from_direction=" + currentDirection + "° to_direction=" + newDirection + "°, battery_multiplier=" + TURN_BATTERY_MULTIPLIER;
        }

        @Override
        public String toString() {
            return "TurnAction.Right{numberDegreesExpression=" + numberDegreesExpression.toString() + '}';
        }
    }
}
