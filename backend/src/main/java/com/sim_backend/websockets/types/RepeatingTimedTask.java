package com.sim_backend.websockets.types;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class RepeatingTimedTask extends TimedTask implements RepeatingTask {
  public long repeatDelay;
  public ChronoUnit unit;

  public RepeatingTimedTask(ZonedDateTime time, long repeatTime, ChronoUnit unit, Runnable task) {
    super(time, task);
    this.unit = unit;
    this.repeatDelay = repeatTime;
  }

  @Override
  public TimedTask repeatTask() {
    ZonedDateTime nextExecutionTime = time.plus(this.repeatDelay, this.unit);
    return new RepeatingTimedTask(nextExecutionTime, this.repeatDelay, this.unit, this.task);
  }
}
