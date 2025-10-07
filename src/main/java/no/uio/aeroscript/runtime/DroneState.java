package no.uio.aeroscript.runtime;

import no.uio.aeroscript.type.Point;

public class DroneState {

    private Point currentPosition;
    private Float currentAltitude;
    private Float currentPointingDirection;
    private Float batteryLevel;
    private boolean isFlying;
    private Float totalDistanceTraveled;

    private static DroneState instance = null;

    public DroneState() {
        this.currentPosition = new Point(0, 0); // starting point
        this.currentAltitude = 0.0f;
        this.currentPointingDirection = 0.0f; // Default pointing direction (0 degrees)
        this.batteryLevel = 100.0f; // Battery level in percentage (100% full)
        this.isFlying = false;
        this.totalDistanceTraveled = 0.0f;
        instance = this;
    }

    public Point getCurrentPosition() { return currentPosition; }
    public Float getCurrentAltitude() { return currentAltitude; }
    public Float getBatteryLevel() { return batteryLevel; }
    public Float getCurrentPointingDirection() { return currentPointingDirection; }
    public boolean isFlying() { return isFlying;}
    public Float getTotalDistanceTraveled() { return totalDistanceTraveled;}

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
    public void setTotalDistanceTraveled(Float totalDistanceTraveled) { this.totalDistanceTraveled = totalDistanceTraveled; }
    public void setCurrentPointingDirection(Float currentPointingDirection) { this.currentPointingDirection = currentPointingDirection; }

}