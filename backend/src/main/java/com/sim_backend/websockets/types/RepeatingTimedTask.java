package com.sim_backend.websockets.types;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class RepeatingTimedTask extends TimedTask implements RepeatingTask {
  public long repeatDelay;
  public ChronoUnit unit;
  private final AtomicBoolean cancelled;

  public boolean isCancelled() {
    return cancelled.get();
  }

  // Public constructor creates a new cancellation flag
  public RepeatingTimedTask(ZonedDateTime time, long repeatTime, ChronoUnit unit, Runnable task) {
    super(time, task);
    this.unit = unit;
    this.repeatDelay = repeatTime;
    this.cancelled = new AtomicBoolean(false);
  }

  // Private constructor to propagate the cancellation flag
  private RepeatingTimedTask(
      ZonedDateTime time,
      long repeatTime,
      ChronoUnit unit,
      Runnable task,
      AtomicBoolean cancelled) {
    super(time, task);
    this.unit = unit;
    this.repeatDelay = repeatTime;
    this.cancelled = cancelled;
  }

  @Override
  public TimedTask repeatTask() {
    if (cancelled.get()) {
      return null; // Don't create a new task if cancelled
    }
    ZonedDateTime nextExecutionTime = time.plus(this.repeatDelay, this.unit);
    return new RepeatingTimedTask(
        nextExecutionTime, this.repeatDelay, this.unit, this.task, this.cancelled);
  }

  // Method to cancel the repeating task
  public void cancel() {
    cancelled.set(true);
  }
}
