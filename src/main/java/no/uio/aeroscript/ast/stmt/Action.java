package no.uio.aeroscript.ast.stmt;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.runtime.DroneState;

public abstract class Action extends Statement{

    protected final DroneState droneState;
    protected final NumberExpression numberSpeedExpression;
    protected final NumberExpression numberDurationExpression;

    protected Action(DroneState droneState, NumberExpression numberSpeedExpression, NumberExpression numberDurationExpression) {
        this.droneState = droneState;
        this.numberSpeedExpression = numberSpeedExpression;
        this.numberDurationExpression = numberDurationExpression;
    }

    @Override
    public final void execute() {
        consumeBattery();
        executeAction();
    }

    protected abstract void executeAction();

    protected abstract Float calculateBatteryCost();

    private void consumeBattery() {
        Float batteryCost = calculateBatteryCost();
        droneState.setBatteryLevel(droneState.getBatteryLevel() - batteryCost);
    }

    protected Float getSpeed() {
        return numberSpeedExpression != null ? Float.parseFloat(numberSpeedExpression.evaluate().toString()) : 1.0f;
    }

    protected Float getDuration() {
        return numberDurationExpression != null ? Float.parseFloat(numberDurationExpression.evaluate().toString()) : getDefaultDuration();
    }

    protected abstract Float getDefaultDuration();

    protected Float getBaseBatteryCost() {
        return (getDuration() * 0.1f) + (getSpeed() * 1.0f);
    }
}
