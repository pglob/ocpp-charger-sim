package com.sim_backend.websockets.types;

import com.sim_backend.websockets.OCPPWebSocketClient;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OCPPTimedTask extends TimedTask {
  @Getter public OCPPMessage message;
  public OCPPWebSocketClient client;

  public OCPPTimedTask(ZonedDateTime time, OCPPMessage message, OCPPWebSocketClient client) {
    super(time, null);
    this.message = message;
    this.client = client;
    this.task =
        () -> {
          try {
            client.pushMessage(this.message);
            this.message.refreshMessage();
          } catch (Exception exception) {
            log.error("Error while scheduling message: {}", this.message, exception);
          }
        };
  }
}
