package no.uio.aeroscript.ast.stmt;

import java.util.logging.Logger;

import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.runtime.DroneState;

public abstract class Action extends Statement{
    private static final Logger logger = Logger.getLogger(Action.class.getName());

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

        logger.info("EXECUTING ACTION: " + this.getClass().getSimpleName() + 
        "\n\tPoint before execution: " + droneState.getCurrentPosition() + ", " +
        "Altitude: " + droneState.getCurrentAltitude() + ", " +
        "Direction: " + droneState.getCurrentPointingDirection() + "°" + 
        "\n\tBase Variables: " + 
        "Speed=" + getSpeed() + ", " + 
        "Duration=" + getDuration() + 
        "\n\tAction Details: " + getDetailedActionInfo() + "\n"
        );
    }

    protected abstract Float calculateBatteryCost();
    
    protected abstract String getDetailedActionInfo();

    private void consumeBattery() {
        Float batteryCost = calculateBatteryCost();
        Float currentBatteryLevel = droneState.getBatteryLevel();
        Float newBatteryLevel = currentBatteryLevel - batteryCost;

        // Check if there's sufficient battery before executing the action
        if (currentBatteryLevel <= 0.0f) {
            throw new no.uio.aeroscript.runtime.exceptions.BatteryDepletedException(
                currentBatteryLevel, this.getClass().getSimpleName()
            );
        }
        
        if (batteryCost > currentBatteryLevel) {
            throw new no.uio.aeroscript.runtime.exceptions.BatteryDepletedException(
                currentBatteryLevel, batteryCost, this.getClass().getSimpleName()
            );
        }

        // Logging battery consumption details + details after execution
        logger.info("BATTERY: " + 
        "\n\tCost=" + batteryCost + ", " + 
        "\n\tCurrent batteryLevel: " + currentBatteryLevel + "% -> " + newBatteryLevel + "%" + 
        "\n\tPoint after execution: " + droneState.getCurrentPosition() + ", " + 
        "Altitude: " + droneState.getCurrentAltitude() + ", " + 
        "Direction: " + droneState.getCurrentPointingDirection() + "°\n"
        );
        
        newBatteryLevel = Math.max(0.0f, newBatteryLevel); // Prevent battery level from going below 0%
        droneState.setBatteryLevel(newBatteryLevel);

        if (newBatteryLevel <= 20.0f) {
            logger.warning("Low battery! Battery level: " + newBatteryLevel + "%\n");
        }
        
        // Throw exception if battery is now completely depleted
        if (newBatteryLevel <= 0.0f) {
            throw new no.uio.aeroscript.runtime.exceptions.BatteryDepletedException(
                newBatteryLevel, this.getClass().getSimpleName()
            );
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
