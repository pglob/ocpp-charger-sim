package com.sim_backend.websockets;

import java.awt.event.ActionListener;

public class OnOCPPMessage {
    /**
     * The received ocpp message.
     */
    private OCPPMessage message;

    /**
     * Create an OCPPMessage event.
     * @param message The received message.
     */
    public OnOCPPMessage(OCPPMessage message) {
        this.message = message;
    }

    /**
     *
     * Get the received OCPPMessage.
     * @return The received message.
     */
    OCPPMessage getMessage() {
        return message;
    }

}
