package com.sim_backend.websockets.types;

import com.sim_backend.websockets.OCPPWebSocketClient;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class OCPPRepeatingTimedTask extends OCPPTimedTask implements RepeatingTask {

  public long repeatDelay;
  public ChronoUnit unit;

  public OCPPRepeatingTimedTask(
      ZonedDateTime time,
      long repeatTime,
      ChronoUnit unit,
      OCPPMessage message,
      OCPPWebSocketClient client) {
    super(time, message, client);
    this.unit = unit;
    this.repeatDelay = repeatTime;
  }

  @Override
  public TimedTask repeatTask() {
    ZonedDateTime now = client.getScheduler().getTime().getSynchronizedTime();
    ZonedDateTime nextExecutionTime = now.plus(this.repeatDelay, this.unit);
    return new OCPPRepeatingTimedTask(
        nextExecutionTime, this.repeatDelay, this.unit, this.message, this.client);
  }
}
