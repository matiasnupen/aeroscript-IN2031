package no.uio.aeroscript.ast.stmt.action;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.ast.stmt.Action;
import no.uio.aeroscript.runtime.DroneState;

public abstract class AltitudeAction extends Action {

    protected AltitudeAction(DroneState droneState, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
        super(droneState, numberSpeedExpression, numberDurationExpression);
    }

    public static class Ascend extends AltitudeAction {
        protected static final Float ASCENDING_BATTERY_MULTIPLIER = 0.6f;
        private final NumberExpression numberAscendingExpression;

        public Ascend(DroneState droneState, NumberExpression numberAscendingExpression, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
            super(droneState, numberSpeedExpression, numberDurationExpression);
            this.numberAscendingExpression = numberAscendingExpression;
        }

        @Override
        protected void executeAction() {
            Float ascending = Float.parseFloat(numberAscendingExpression.evaluate().toString());
            droneState.setCurrentAltitude(droneState.getCurrentAltitude() + ascending);
        }

        @Override
        protected Float calculateBatteryCost() {
            Float ascending = Float.parseFloat(numberAscendingExpression.evaluate().toString());
            return getBaseBatteryCost() + (ascending * ASCENDING_BATTERY_MULTIPLIER);
        }

        @Override
        public String toString() {
            return "AltitudeAction.Ascend{numberAscendingExpression=" + numberAscendingExpression.toString() + '}';
        }
    }

    public static class Descend extends AltitudeAction {
        protected static final Float DESCENDING_BATTERY_MULTIPLIER = 0.2f;
        private final NumberExpression numberDescendingExpression;

        public Descend(DroneState droneState, NumberExpression numberDescendingExpression, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
            super(droneState, numberSpeedExpression, numberDurationExpression);
            this.numberDescendingExpression = numberDescendingExpression;
        }

        @Override
        protected void executeAction() {
            Float descending = Float.parseFloat(numberDescendingExpression.evaluate().toString());
            droneState.setCurrentAltitude(droneState.getCurrentAltitude() - descending);
        }

        @Override
        protected Float calculateBatteryCost() {
            Float descending = Float.parseFloat(numberDescendingExpression.evaluate().toString());
            return getBaseBatteryCost() + (descending * DESCENDING_BATTERY_MULTIPLIER);
        }

        @Override
        public String toString() {
            return "AltitudeAction.Descend{numberDescendingExpression=" + numberDescendingExpression.toString() + '}';
        }
    }

    public static class DescendToGround extends AltitudeAction {
        protected static final Float DESCENDING_BATTERY_MULTIPLIER = 0.2f;

        public DescendToGround(DroneState droneState, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
            super(droneState, numberSpeedExpression, numberDurationExpression);
        }

        @Override
        protected void executeAction() {
            droneState.setCurrentAltitude(0.0f);
        }

        @Override
        protected Float calculateBatteryCost() {
            Float descending = droneState.getCurrentAltitude();
            return getBaseBatteryCost() + (descending * DESCENDING_BATTERY_MULTIPLIER);
        }

        @Override
        public String toString() {
            return "AltitudeAction.DescendToGround{}";
        }
    }
}
