package no.uio.aeroscript.runtime;

import no.uio.aeroscript.type.Point;

public class DroneState {

    private Point currentPosition;
    private float currentAltitude;
    private float batteryLevel;
    private boolean isFlying;
    private float totalDistanceTraveled;

    private static DroneState instance = null;

    public DroneState() {
        this.currentPosition = new Point(0, 0); // starting point
        this.currentAltitude = 0;
        this.batteryLevel = 100; // Battery level in percentage (100% full)
        this.isFlying = false;
        this.totalDistanceTraveled = 0;
        instance = this;
    }

    public Point getCurrentPosition() { return currentPosition; }
    public float getCurrentAltitude() { return currentAltitude; }
    public float getBatteryLevel() { return batteryLevel;}
    public boolean isFlying() { return isFlying;}
    public float getTotalDistanceTraveled() { return totalDistanceTraveled;}

    public static DroneState getInstance() {
        if (instance == null) {
            instance = new DroneState();
        }
        return instance;
    }

    public void setCurrentPosition(Point currentPosition) { this.currentPosition = currentPosition; }
    public void setCurrentAltitude(float currentAltitude) { this.currentAltitude = currentAltitude; }
    public void setBatteryLevel(float batteryLevel) { this.batteryLevel = batteryLevel; }
    public void setFlying(boolean flying) { isFlying = flying; }
    public void setTotalDistanceTraveled(float totalDistanceTraveled) { this.totalDistanceTraveled = totalDistanceTraveled; }


}