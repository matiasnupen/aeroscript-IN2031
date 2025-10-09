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
        executeAction();
        consumeBattery();
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
        Float newBatteryLevel = droneState.getBatteryLevel() - batteryCost;

        System.out.println("Action: " + this.getClass().getSimpleName() +
                ", Speed: " + getSpeed() +
                ", Duration: " + getDuration() +
                ", Battery Cost: " + batteryCost +
                ", New Battery Level: " + newBatteryLevel);
        
        newBatteryLevel = Math.max(0.0f, newBatteryLevel); // Prevent battery level from going below 0%
        droneState.setBatteryLevel(newBatteryLevel);

        if (newBatteryLevel <= 20.0f) {
            System.out.println("WARNING: Low battery! Battery level: " + newBatteryLevel + "%");
        }
    }

    protected Float getSpeed() {
        return numberSpeedExpression != null ? Float.parseFloat(numberSpeedExpression.evaluate().toString()) : 0.0f;
    }

    protected Float getDuration() {
        return numberDurationExpression != null ? Float.parseFloat(numberDurationExpression.evaluate().toString()) : 0.0f;
    }

    protected Float getBaseBatteryCost() {
        return (getDuration() * 0.1f) + (getSpeed() * 1.0f);
    }
}
