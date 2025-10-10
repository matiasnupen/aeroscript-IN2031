package no.uio.aeroscript.runtime;

/**
 * Exception thrown when the drone's battery is depleted or insufficient to perform an action.
 * This prevents infinite loops and ensures graceful termination when battery runs out.
 */
public class BatteryDepletedException extends RuntimeException {
    
    private final Float currentBatteryLevel;
    private final Float requiredBattery;
    private final String actionType;

    /**
     * Creates a BatteryDepletedException for complete battery depletion.
     * 
     * @param currentBatteryLevel The current battery level (should be 0.0f or negative)
     * @param actionType The type of action that caused the depletion
     */
    public BatteryDepletedException(Float currentBatteryLevel, String actionType) {
        super(String.format("Battery depleted! Current level: %.2f%%, Action: %s", 
              currentBatteryLevel, actionType));
        this.currentBatteryLevel = currentBatteryLevel;
        this.requiredBattery = null;
        this.actionType = actionType;
    }

    /**
     * Creates a BatteryDepletedException for insufficient battery.
     * 
     * @param currentBatteryLevel The current battery level
     * @param requiredBattery The battery required for the action
     * @param actionType The type of action that requires more battery
     */
    public BatteryDepletedException(Float currentBatteryLevel, Float requiredBattery, String actionType) {
        super(String.format("Insufficient battery! Current: %.2f%%, Required: %.2f%%, Action: %s", 
              currentBatteryLevel, requiredBattery, actionType));
        this.currentBatteryLevel = currentBatteryLevel;
        this.requiredBattery = requiredBattery;
        this.actionType = actionType;
    }

    public Float getCurrentBatteryLevel() {
        return currentBatteryLevel;
    }

    public Float getRequiredBattery() {
        return requiredBattery;
    }

    public String getActionType() {
        return actionType;
    }

    /**
     * Checks if this was caused by complete battery depletion (0%) or just insufficient battery.
     */
    public boolean isCompleteDepletion() {
        return currentBatteryLevel <= 0.0f;
    }
}