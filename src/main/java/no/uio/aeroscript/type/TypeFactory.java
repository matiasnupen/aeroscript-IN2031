package no.uio.aeroscript.type;

import no.uio.aeroscript.type.impl.Point;
import no.uio.aeroscript.type.impl.Range;
import no.uio.aeroscript.type.interfaces.PointInterface;
import no.uio.aeroscript.type.interfaces.RangeInterface;

public class TypeFactory {

    public static PointInterface createPoint(Float x, Float y) {
        return new Point(x, y);
    }

    public static RangeInterface createRange(Float start, Float end) {
        return new Range(start, end);
    }

    // public static ObstacleInterface createObstacle(String shape, PointInterface position, Float size) {
    //     return new Obstacle(shape, position, size);
    // }

}
