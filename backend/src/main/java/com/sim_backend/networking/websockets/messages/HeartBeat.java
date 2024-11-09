package com.sim_backend.networking.websockets.messages;

import com.sim_backend.networking.websockets.OCCPMessage;
import com.sim_backend.networking.websockets.OCCPMessageInfo;


@OCCPMessageInfo(messageName = "HeartBeat")
public class HeartBeat extends OCCPMessage {
    public HeartBeat() {
        super();
    }
}
