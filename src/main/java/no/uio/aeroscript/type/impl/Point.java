package no.uio.aeroscript.type.impl;

import no.uio.aeroscript.type.interfaces.PointInterface;

public class Point implements PointInterface {
    private final float x;
    private final float y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void print() {
        System.out.println("Point: (" + this.x + ", " + this.y + ")");
    }

    @Override
    public Float getX() {
        return x;
    }

    @Override
    public Float getY() {
        return y;
    }

    // Method for calculating the distance between two points
    @Override
    public Float distanceTo(PointInterface other) {
        Float distanceX = this.x - other.getX();
        Float distanceY = this.y - other.getY(); 
        return (float) Math.max(Math.abs(distanceX), Math.abs(distanceY));
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
