package no.uio.aeroscript.runtime.exceptions;

// Exception thrown when the drone's battery is insufficient to perform an action.
public class BatteryDepletedException extends RuntimeException {
    
    private final float currentBatteryLevel;
    
    public BatteryDepletedException(float currentLevel, String action) {
        super(String.format("Battery depleted! Current: %.1f%% (Action: %s)", currentLevel, action));
        this.currentBatteryLevel = currentLevel;
    }
    
    public BatteryDepletedException(float currentLevel, float requiredLevel, String action) {
        super(String.format("Battery insufficient! Current: %.1f%%, Required: %.1f%% (Action: %s)", 
              currentLevel, requiredLevel, action));
        this.currentBatteryLevel = currentLevel;
    }
    
    public float getCurrentBatteryLevel() {
        return currentBatteryLevel;
    }
}