package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.OCCPMessage;
import com.sim_backend.websockets.OCCPMessageInfo;


@OCCPMessageInfo(messageName = "HeartBeat")
public class HeartBeat extends OCCPMessage {
    public HeartBeat() {
        super();
    }
}
