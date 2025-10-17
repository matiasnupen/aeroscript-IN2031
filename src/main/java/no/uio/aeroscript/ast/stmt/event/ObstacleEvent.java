package no.uio.aeroscript.ast.stmt.event;

import no.uio.aeroscript.ast.stmt.Event;
import no.uio.aeroscript.runtime.DroneState;

public class ObstacleEvent extends Event {
    

    @Override
    public boolean isTriggered(DroneState droneState) {
        // Implementation for obstacle detection goes here
        return false; // Placeholder return value
    }

    @Override
    public String getEventType() {
        return "OBSTACLE";
    }
}
