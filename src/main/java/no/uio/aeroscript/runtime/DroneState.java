package no.uio.aeroscript.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import no.uio.aeroscript.type.Point;

public class DroneState {

    private static final Logger logger = Logger.getLogger(DroneState.class.getName());

    private final Point base;
    private Point currentPosition;
    private Float currentAltitude;
    private Float currentPointingDirection;
    private Float batteryLevel;
    private boolean isFlying;
    private Float totalDistanceTravelled;
    
    // Obstacle detection state
    private boolean obstacleDetected;
    private String obstacleType;
    private Point obstacleLocation;
    private List<Point> detectedObstacles;

    private static DroneState instance = null;

    public DroneState() {
        this.currentPosition = new Point(0, 0); // starting point
        this.base = currentPosition;
        this.currentAltitude = 0.0f;
        this.currentPointingDirection = 0.0f; // Default pointing direction (0 degrees)
        this.batteryLevel = 100.0f; // Battery level in percentage (100% full)
        this.isFlying = false;
        this.totalDistanceTravelled = 0.0f;
        
        // Initialize obstacle detection state
        this.obstacleDetected = false;
        this.obstacleType = null;
        this.obstacleLocation = null;
        this.detectedObstacles = new ArrayList<>();
    }

    public Point getCurrentPosition() { return currentPosition; }
    public Float getCurrentAltitude() { return currentAltitude; }
    public Float getBatteryLevel() { return batteryLevel; }
    public Float getCurrentPointingDirection() { return currentPointingDirection; }
    public boolean isFlying() { return isFlying;}
    public Float getTotalDistanceTravelled() { return totalDistanceTravelled;}
    
    // Obstacle detection getters
    public boolean isObstacleDetected() { return obstacleDetected; }
    public String getObstacleType() { return obstacleType; }
    public Point getObstacleLocation() { return obstacleLocation; }
    public List<Point> getDetectedObstacles() { return detectedObstacles; }

    public static DroneState getInstance() {
        if (instance == null) {
            instance = new DroneState();
        }
        return instance;
    }

    public void setCurrentPosition(Point currentPosition) { this.currentPosition = currentPosition; }
    public void setCurrentAltitude(Float currentAltitude) { this.currentAltitude = currentAltitude; }
    public void setBatteryLevel(Float batteryLevel) { this.batteryLevel = batteryLevel; }
    public void setFlying(boolean flying) { isFlying = flying; }
    public void setTotalDistanceTravelled(Float totalDistanceTravelled) { this.totalDistanceTravelled = totalDistanceTravelled; }
    public void setCurrentPointingDirection(Float currentPointingDirection) { this.currentPointingDirection = currentPointingDirection; }
    public void goToBase() { 
        currentPosition = base; 
        currentAltitude = 0.0f; 
        isFlying = false;
    }

    /** Methods for usage in Event-handling
     * @return true if battery level is 20% or lower
     * @return the entire message queue as a String
     * 
     */
    public boolean batteryIsLow() {
        return batteryLevel <= 20.0f;
    }

    public String getMessageQueue() {
        // TODO: Implement message queue system
        return "";
    }

    public boolean obstacleDetected() { 
        return obstacleDetected; 
    }
    
    /**
     * Reports an obstacle detected by sensors or external systems
     * @param obstacleType Type of obstacle (e.g., "building", "aircraft", "terrain")
     * @param location Location where obstacle was detected (can be null)
     */
    public void reportObstacle(String obstacleType, Point location) {
        this.obstacleDetected = true;
        this.obstacleType = obstacleType;
        this.obstacleLocation = location;
        logger.info("OBSTACLE DETECTED: " + obstacleType + 
            (location != null ? " at " + location : ""));
    }
    
    /**
     * Reports an obstacle without specific location
     */
    public void reportObstacle(String obstacleType) {
        reportObstacle(obstacleType, null);
    }
    
    /**
     * Clears the obstacle detection state
     */
    public void clearObstacle() {
        this.obstacleDetected = false;
        this.obstacleType = null;
        this.obstacleLocation = null;
        logger.info("OBSTACLE REMOVED - continuing normal flight");
    }


    @Override
    public String toString() {
        return "DroneState{" +
                "base=" + base.toString() +
                ", currentPosition=" + currentPosition.toString() +
                ", currentAltitude=" + currentAltitude.toString() +
                ", currentPointingDirection=" + currentPointingDirection.toString() +
                ", batteryLevel=" + batteryLevel.toString() +
                ", isFlying=" + isFlying +
                ", totalDistanceTravelled=" + totalDistanceTravelled.toString() +
                '}';
    }

}