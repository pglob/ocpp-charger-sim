package com.sim_backend.networking.websockets.messages;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sim_backend.networking.websockets.GsonUtilities;
import com.sim_backend.networking.websockets.OCCPMessage;
import com.sim_backend.networking.websockets.OCCPMessageInfo;

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
