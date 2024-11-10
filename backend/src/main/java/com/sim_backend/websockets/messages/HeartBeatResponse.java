package com.sim_backend.websockets.messages;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.OCCPMessage;
import com.sim_backend.websockets.OCCPMessageInfo;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@OCCPMessageInfo(messageName = "HeartBeatResponse")
public class HeartBeatResponse extends OCCPMessage {
    public final ZonedDateTime currentTime;

    public HeartBeatResponse() {
        super();
        this.currentTime = ZonedDateTime.now(ZoneId.systemDefault());
    }

    public HeartBeatResponse(ZonedDateTime time) {
        super();
        this.currentTime = time;
    }
}
