package no.uio.aeroscript.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import no.uio.aeroscript.type.Obstacle;
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
    
    private List<Obstacle> detectedObstacles;

    private List<String> messageIds;

    private static DroneState instance = null;

    public DroneState() {
        this.currentPosition = new Point(0, 0); // starting point
        this.base = currentPosition;
        this.currentAltitude = 0.0f;
        this.currentPointingDirection = 0.0f; // Default pointing direction (0 degrees)
        this.batteryLevel = 100.0f; // Battery level in percentage (100% full)
        this.isFlying = false;
        this.totalDistanceTravelled = 0.0f;
        
        // Initialize obstacle detection and messages
        this.detectedObstacles = new ArrayList<>();
        this.messageIds = new ArrayList<>();
    }

    public Point getCurrentPosition() { return currentPosition; }
    public Float getCurrentAltitude() { return currentAltitude; }
    public Float getBatteryLevel() { return batteryLevel; }
    public Float getCurrentPointingDirection() { return currentPointingDirection; }
    public boolean isFlying() { return isFlying;}
    public Float getTotalDistanceTravelled() { return totalDistanceTravelled;}
    public List<Obstacle> getDetectedObstacles() { return detectedObstacles; }

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

    /**
     * Methods to manage obstacles
     * @param discoverObstacle(Obstacle obstacle) updates the status of an obstacle and adds it to the detected obstacles list
     * @param removeObstacle(Obstacle obstacle) removes an obstacle from the detected obstacles list
     * @param hasObstacleOnPoint(Point point) checks if there is an obstacle at the given point
     * @param hasObstacleOnMoveByDistance(Point start, Point end) checks if there is an obstacle along the path from start point to end point
     * @param obstacleDetected() checks if any obstacles are currently detected
    **/
    public void discoverObstacle(Obstacle obstacle) {
        obstacle.discover();
        detectedObstacles.add(obstacle);
        logger.info("OBSTACLE DETECTED: " + obstacle.getObstacleName() + " at " + obstacle.getLocation());
        logger.info("ALL DETECTED OBSTACLES: " + detectedObstacles.toString());
    }
    public void removeObstacle(Obstacle obstacle) {
        detectedObstacles.remove(obstacle);
        logger.info("OBSTACLE REMOVED: " + obstacle.getObstacleName() + " at " + obstacle.getLocation());
    }
    public boolean hasObstacleOnPoint(Point point) {
        for (Obstacle obstacle : detectedObstacles) {
            if (obstacle.getLocation().equals(point)) {
                logger.info("OBSTACLE FOUND at point: " + point.toString());
                return true;
            }
        }
        return false;
    }
    public boolean hasObstacleOnMoveByDistance(Point start, Point end) {
        // Moving along the x-axis as standard for this program
        Float yRow = start.getY();
        for (Float x = start.getX(); x <= end.getX(); x += 0.1f) { // Put 0.1 as step size to check more points
            Point checkPoint = new Point(x, yRow);
            if (hasObstacleOnPoint(checkPoint)) {
                logger.info("OBSTACLE FOUND between points: " + start.toString() + " and " + end.toString());
                return true;
            }
        }
        return false;
    }
    public boolean obstacleDetected() {
        return !detectedObstacles.isEmpty();
    }

    /** Methods to handle received messages
     * @param addReceivedMessage(String messageId) adds a message ID to the list of received messages
     * @param removeMessageId(String messageId) removes a message ID from the list of received messages
     * @param messageReceived(String messageId) is used in the MessageEvent-class *
    **/
    public void addReceivedMessage(String messageId) {
        if (!messageIds.contains(messageId)) {
            messageIds.add(messageId);
        }
    }
    public String removeMessageId(String messageId) {
        if (messageIds.contains(messageId)) {
            messageIds.remove(messageId);
            return messageId;
        }
        return null;
    }
    public boolean messageReceived(String messageId) {
        return messageIds.contains(messageId);
    }

    /** Checks if the battery level is low
     * Used in LowBatteryEvent-class
    **/
    public boolean batteryIsLow() {
        return getBatteryLevel() <= 20.0f;
    }

    /** Reset drone state
     * Clears obstacles, messages, and resets to initial state
     * This method is intended for use in unit tests to ensure a clean state
     */
    public void resetForTesting() {
        this.currentPosition = new Point(0, 0);
        // base is final, so we keep the original base position
        this.currentAltitude = 0.0f;
        this.currentPointingDirection = 0.0f;
        this.batteryLevel = 100.0f;
        this.isFlying = false;
        this.totalDistanceTravelled = 0.0f;
        this.detectedObstacles.clear();
        this.messageIds.clear();
        logger.info("DroneState reset for testing");
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