package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.OCCPMessage;
import com.sim_backend.websockets.OCCPMessageInfo;


@OCCPMessageInfo(messageName = "HeartBeat")
public final class HeartBeat extends OCCPMessage {
    /***
     * A HeartBeat Message.
     */
    public HeartBeat() {
        super();
    }
}
