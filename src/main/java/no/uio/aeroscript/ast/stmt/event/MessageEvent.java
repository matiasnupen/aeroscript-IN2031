package no.uio.aeroscript.ast.stmt.event;

import no.uio.aeroscript.ast.stmt.Event;
import no.uio.aeroscript.runtime.DroneState;

public class MessageEvent extends Event {

    private String messageId;

    public MessageEvent(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public boolean isTriggered(DroneState droneState) {
        return false;
    }    
    
    public String getMessageId() {
        return messageId;
    }

    @Override
    public String getEventType() {
        return "MESSAGE";
    }
}