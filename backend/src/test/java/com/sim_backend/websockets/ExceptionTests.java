package com.sim_backend.websockets;

import com.sim_backend.websockets.exceptions.*;
import com.sim_backend.websockets.messages.HeartBeat;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.junit.jupiter.api.Test;

public class ExceptionTests {
    @Test
    public void testOCPPBadCallID() {
        OCPPBadCallID exception = new OCPPBadCallID(10, "[10, \"2\", \"Woah\", {}]");
        String message = exception.getMessage();
        assert message.contains("Bad call ID 10: [10, \"2\", \"Woah\", {}]");
    }

    @Test
    public void testOCPPBadClass() {
        OCPPBadClass exception = new OCPPBadClass();
        String message = exception.getMessage();
        assert message.contains("Bad Class supplied to OCPPWebsocketClient");
    }

    @Test
    public void testOCPPCannotProcess() {
        OCPPCannotProcessResponse exception = new OCPPCannotProcessResponse("[2, \"2\", \"Woah\", {}]", "2");
        String message = exception.getMessage();
        assert message.contains("Received Message with ID \"2\" where we do not have a matching sent message: [2, \"2\", \"Woah\", {}]");
    }

    @Test
    public void testOCPPMessageFailure() {
        HeartBeat beat = new HeartBeat();
        beat.setMessageID("Why");
        OCPPMessageFailure exception = new OCPPMessageFailure(beat, new WebsocketNotConnectedException());
        String message = exception.getMessage();
        assert message.contains("Could not Send Message [2,\"Why\",\"HeartBeat\",{}]: null");
    }

    @Test
    public void testOCPPUnsupportedMessage() {
        OCPPUnsupportedMessage exception = new OCPPUnsupportedMessage("[2, \"2\", \"Woah\", {}]", "Woah");
        String message = exception.getMessage();
        assert message.contains("Received unknown message name \"Woah\": [2, \"2\", \"Woah\", {}]");
    }
}
