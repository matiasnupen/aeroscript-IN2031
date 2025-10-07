package no.uio.aeroscript.type;

public class Point {
    private final float x;
    private final float y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void print() {
        System.out.println("Point: (" + this.x + ", " + this.y + ")");
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    // Method for calculating the distance between two points
    public Float distanceTo(Point other) {
        Float distanceX = this.x - other.x;
        Float distanceY = this.y - other.y; 
        return (float) Math.max(Math.abs(distanceX), Math.abs(distanceY));
    }
}
