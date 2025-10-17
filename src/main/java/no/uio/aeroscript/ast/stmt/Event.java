package no.uio.aeroscript.ast.stmt;

import no.uio.aeroscript.runtime.DroneState;

public abstract class Event {
    public abstract boolean isTriggered(DroneState droneState);
    public abstract String getEventType();
}