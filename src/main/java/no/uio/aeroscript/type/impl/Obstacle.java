package no.uio.aeroscript.type.impl;

import no.uio.aeroscript.type.interfaces.ObstacleInterface;

/**
 * @param class Obstacle represents an obstacle
 * @param name the name of the obstacle
 * @param location the location of the obstacle
 * An obstacle only takes up one point in space, meaning it has no dimensions.
 */

public class Obstacle implements ObstacleInterface {
    private String obstacleName;
    private Point location;
    private boolean isDiscovered = false;

    public Obstacle(String obstacleName, Point location) {
        this.obstacleName = obstacleName;
        this.location = location;
    }

    public String getObstacleName() {
        return obstacleName;
    }
    public Point getLocation() {
        return location;
    }
    public boolean isDiscovered() {
        return isDiscovered;
    }

    public void discover () {
        isDiscovered = true;
    }

    @Override
    public String toString() {
        return obstacleName +" at: " + location.toString();
    }

}