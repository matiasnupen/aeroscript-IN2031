package no.uio.aeroscript.type.impl;

import no.uio.aeroscript.type.interfaces.ObstacleInterface;
import no.uio.aeroscript.type.interfaces.PointInterface;

/**
 * @param class Obstacle represents an obstacle
 * @param name the name of the obstacle
 * @param location the location of the obstacle
 * An obstacle only takes up one point in space, meaning it has no dimensions.
 */

public class Obstacle implements ObstacleInterface {
    private String obstacleName;
    private PointInterface location;
    private boolean isDiscovered = false;

    public Obstacle(String obstacleName, PointInterface location) {
        this.obstacleName = obstacleName;
        this.location = location;
    }

    public String getObstacleName() {
        return obstacleName;
    }
    public PointInterface getLocation() {
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