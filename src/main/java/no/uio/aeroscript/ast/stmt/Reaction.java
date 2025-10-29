package no.uio.aeroscript.ast.stmt;

import no.uio.aeroscript.runtime.DroneState;

public class Reaction extends Statement {
    
    private Event event;
    private String targetModeName;

    public Reaction(Event event, String targetModeName) {
        this.event = event;
        this.targetModeName = targetModeName;
    }

    public boolean isTriggered(DroneState droneState) {
        return event.isTriggered(droneState);
    }

    public String getTargetModeName() {
        return targetModeName;
    }

    @Override
    public void execute() {
        // TODO: Reactions don't execute directly, they trigger mode transitions.
    }

    @Override
    public String toString() {
        return "on " + event.getEventType() + " -> " + targetModeName + ";";
    }

    public Event getEvent() {
        return event;
    }
}