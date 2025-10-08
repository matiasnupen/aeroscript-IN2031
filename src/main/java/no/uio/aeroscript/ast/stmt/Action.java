package no.uio.aeroscript.ast.stmt;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.runtime.DroneState;

public abstract class Action extends Statement{

    protected DroneState droneState;
    protected NumberExpression numberSpeedExpression;
    protected NumberExpression numberDurationExpression;

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

    public void setSpeedExpression(NumberExpression numberSpeedExpression) {
        this.numberSpeedExpression = numberSpeedExpression;
    }

    public void setDurationExpression(NumberExpression numberDurationExpression) {
        this.numberDurationExpression = numberDurationExpression;
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
