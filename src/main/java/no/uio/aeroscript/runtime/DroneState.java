package no.uio.aeroscript.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import no.uio.aeroscript.type.Point;
import no.uio.aeroscript.type.Obstacle;

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
        this.detectedObstacles = new ArrayList<>();
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
     */
    public void discoverObstacle(Obstacle obstacle) {
        obstacle.discover();
        detectedObstacles.add(obstacle);
        logger.info("OBSTACLE DETECTED: " + obstacle.getObstacleName() + " at " + obstacle.getLocation());
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
        for (Float x = start.getX(); x <= end.getX(); x += 0.1f) { // TODO: adjust step size as needed
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