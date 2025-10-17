package no.uio.aeroscript.ast.stmt.event;

import no.uio.aeroscript.ast.stmt.Event;
import no.uio.aeroscript.runtime.DroneState;

public class LowBatteryEvent extends Event {

    @Override
    public boolean isTriggered(DroneState droneState) {
        return droneState.getBatteryLevel() <= 20;
    }

    @Override
    public String getEventType() {
        return "LOW_BATTERY";
    }
}
