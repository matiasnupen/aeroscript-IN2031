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

    protected void executeAction() {
        // Logging action execution details
        System.out.println("=== ACTION EXECUTION LOG ===");
        System.out.println("Action Type: " + this.getClass().getSimpleName());
        System.out.println("Point before execution: " + droneState.getCurrentPosition() + ", Altitude: " + droneState.getCurrentAltitude() + ", Direction: " + droneState.getCurrentPointingDirection() + "°");
        System.out.println("Base Variables: Speed=" + getSpeed() + ", Duration=" + getDuration());
        System.out.println("Action Details: " + getDetailedActionInfo());
    }

    protected abstract Float calculateBatteryCost();
    
    protected abstract String getDetailedActionInfo();

    private void consumeBattery() {
        Float batteryCost = calculateBatteryCost();
        Float newBatteryLevel = droneState.getBatteryLevel() - batteryCost;

        // Logging battery consumption details + details after execution
        System.out.println("Battery: Cost=" + batteryCost + ", Current batteryLevel: " + droneState.getBatteryLevel() + "% -> " + newBatteryLevel + "%");
        System.out.println("Point after execution: " + droneState.getCurrentPosition() + ", Altitude: " + droneState.getCurrentAltitude() + ", Direction: " + droneState.getCurrentPointingDirection() + "°");
        System.out.println("============================");
        
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
        return (getDuration() * 0.01f) + (getSpeed() * 0.1f);
    }
}
