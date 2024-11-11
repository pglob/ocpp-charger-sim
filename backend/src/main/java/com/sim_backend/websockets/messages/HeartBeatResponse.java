package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.OCPPMessage;
import com.sim_backend.websockets.OCPPMessageInfo;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@OCPPMessageInfo(messageName = "HeartBeatResponse")
public final class HeartBeatResponse extends OCPPMessage {
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
