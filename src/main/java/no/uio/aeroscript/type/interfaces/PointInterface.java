package no.uio.aeroscript.type.interfaces;

public interface PointInterface {
    Float getX();
    Float getY();
    Float distanceTo(PointInterface other);
    String toString();
}
