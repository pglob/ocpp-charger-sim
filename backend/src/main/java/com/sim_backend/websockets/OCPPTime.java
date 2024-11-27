package com.sim_backend.websockets;

import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.HeartBeatResponse;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** Get Synchronized Time with our last received HeartBeatResponse. */
public class OCPPTime implements AutoCloseable {

  /** Our stored offset based on the time we received in our last HeartBeatResponse. */
  private Duration offset = Duration.ZERO;

  /** Our stored OCPPMessageListener. */
  private final OnOCPPMessageListener listener =
      message -> {
        HeartBeatResponse response = (HeartBeatResponse) message.getMessage();

        this.offset =
            Duration.between(ZonedDateTime.now(ZoneId.of("UTC")), response.getCurrentTime());
      };

  /** The client we are listening on. */
  private OCPPWebSocketClient client = null;

  /**
   * Create an OCPP Synchronized Time Object.
   *
   * @param currClient The client you want to listen for time from.
   */
  public OCPPTime(OCPPWebSocketClient currClient) {
    this.client = currClient;
    client.onReceiveMessage(HeartBeatResponse.class, listener);
  }

  /**
   * Get the current time synchronized with our last received HeartBeatResponse.
   *
   * @return The Synchronized time.
   */
  public ZonedDateTime getSynchronizedTime() {
    ZonedDateTime time = ZonedDateTime.now();
    return time.plus(offset);
  }

  /**
   * Get a Time synchronized with our last received HeartBeatResponse.
   *
   * @param time the time you wish to sync up.
   * @return The Synchronized time.
   */
  public ZonedDateTime getSynchronizedTime(ZonedDateTime time) {
    return time.plus(offset);
  }

  /**
   * Called when this is destroyed in a try catch.
   *
   * @throws Exception error in removing the receiver.
   */
  @Override
  public void close() throws Exception {
    if (this.client != null) {
      this.client.deleteOnReceiveMessage(HeartBeatResponse.class, listener);
    }
  }
}
