package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.OCCPMessage;
import com.sim_backend.websockets.OCCPMessageInfo;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@OCCPMessageInfo(messageName = "HeartBeatResponse")
public final class HeartBeatResponse extends OCCPMessage {
    /**
     * The HeartBeat's time.
     */
    public final ZonedDateTime currentTime;

    /**
     * The Response Message for a HeartBeat, currentTime will be set to now.
     */
    public HeartBeatResponse() {
        super();
        this.currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    /**
     * The Response Message for a HeartBeat.
     * @param time A Provided Time.
     */
    public HeartBeatResponse(final ZonedDateTime time) {
        super();
        this.currentTime = time;
    }
}
