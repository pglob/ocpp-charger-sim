package com.sim_backend.websockets.types;

import java.time.ZonedDateTime;
import lombok.Setter;

public class TimedTask {
  public ZonedDateTime time; // Time as ZonedDateTime
  @Setter public Runnable task;

  public TimedTask(ZonedDateTime time, Runnable task) {
    this.time = time;
    this.task = task;
  }
}
