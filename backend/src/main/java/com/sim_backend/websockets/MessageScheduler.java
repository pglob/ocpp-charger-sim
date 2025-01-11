package com.sim_backend.websockets;

import com.google.common.annotations.VisibleForTesting;
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.types.OCPPMessage;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** An OCPPMessageScheduler. */
@Slf4j
public class MessageScheduler {

  public static class TimedTask {
    ZonedDateTime time; // Time as ZonedDateTime
    Runnable task;

    TimedTask(ZonedDateTime time, Runnable task) {
      this.time = time;
      this.task = task;
    }
  }

  public static class RepeatingTimedTask extends TimedTask {
    long repeatDelay;
    ChronoUnit unit;

    RepeatingTimedTask(ZonedDateTime time, long repeatTime, ChronoUnit unit, Runnable task) {
      super(time, task);
      this.unit = unit;
      this.repeatDelay = repeatTime;
    }
  }

  /** Our Synchronized Time. */
  @Getter private final OCPPTime time;

  /** The OCPPWebSocket we will send our messages through. */
  private final OCPPWebSocketClient client;

  /**
   * Our set heartbeat interval, we don't want this too common but every 4 minutes should be enough.
   */
  @Getter private static final long HEARTBEAT_INTERVAL = 240L; // seconds

  /** Our heartbeat job. */
  private TimedTask heartbeat;

  /** Our scheduled tasks. */
  @VisibleForTesting final CopyOnWriteArrayList<TimedTask> tasks = new CopyOnWriteArrayList<>();

  /**
   * An OCPPMessage Scheduler.
   *
   * @param targetClient The client to send our messages through.
   */
  public MessageScheduler(OCPPWebSocketClient targetClient) {
    this.client = targetClient;
    this.time = new OCPPTime(targetClient);
    this.setHeartbeatInterval(HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
  }

  /**
   * Set our interval between heartbeats.
   *
   * @param interval The Duration between heartbeats.
   * @param unit The unit of your interval.
   */
  public TimedTask setHeartbeatInterval(Long interval, TimeUnit unit) {
    tasks.remove(this.heartbeat);

    return (this.heartbeat = this.periodicJob(0, interval, unit, new Heartbeat()));
  }

  public void synchronizeTime(ZonedDateTime time) {
    this.time.setOffset(time);
  }

  /**
   * Create a Runnable instance for our job functions.
   *
   * @param message The message to wrap.
   * @return The created Runnable instance.
   */
  private Runnable createRunnable(final OCPPMessage message) {
    return () -> {
      try {
        this.client.pushMessage(message);
        message.refreshMessage();
      } catch (Exception exception) {
        log.error("Error while scheduling message: {}", message, exception);
      }
    };
  }

  /**
   * Create a Periodic Job.
   *
   * @param initialDelay The initial delay before we send our first message.
   * @param delay The delay between messages.
   * @param timeUnit The time units you wish to use.
   * @param message The message.
   */
  public TimedTask periodicJob(
      long initialDelay, long delay, TimeUnit timeUnit, OCPPMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("message must not be null");
    }

    if (initialDelay < 0 || delay <= 0) {
      throw new IllegalArgumentException("Initial delay and delay must be positive");
    }

    Runnable job = this.createRunnable(message);
    RepeatingTimedTask task =
        new RepeatingTimedTask(
            getTime().getSynchronizedTime().plus(initialDelay, timeUnit.toChronoUnit()),
            delay,
            timeUnit.toChronoUnit(),
            job);
    tasks.add(task);
    return task;
  }

  /**
   * Register a job at a set time.
   *
   * @param delay The delay at which to send the message.
   * @param timeUnit The time units you wish to use.
   * @param message The message.
   */
  public TimedTask registerJob(long delay, TimeUnit timeUnit, OCPPMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("message must not be null");
    }

    if (delay <= 0) {
      throw new IllegalArgumentException("Delay must be positive");
    }

    Runnable job = this.createRunnable(message);

    TimedTask task =
        new TimedTask(getTime().getSynchronizedTime().plus(delay, timeUnit.toChronoUnit()), job);
    tasks.add(task);
    return task;
  }

  /**
   * Register a job at a set time.
   *
   * @param timeToSend The time we should send it.
   * @param message The message to send.
   */
  public TimedTask registerJob(ZonedDateTime timeToSend, OCPPMessage message) {
    if (timeToSend == null || message == null) {
      throw new IllegalArgumentException("timeToSend and message must not be null");
    }

    TimedTask task = new TimedTask(timeToSend, this.createRunnable(message));
    tasks.add(task);
    return task;
  }

  /** Tick our scheduler to check for messages to be sent. */
  public void tick() {
    tasks.sort(Comparator.comparing(a -> a.time));

    ZonedDateTime currentTime = getTime().getSynchronizedTime();
    List<TimedTask> toExecute = new ArrayList<>();

    for (TimedTask task : tasks) {
      if (task.time.isBefore(currentTime)) {
        toExecute.add(task);
      }
    }

    tasks.removeAll(toExecute);

    for (TimedTask task : toExecute) {
      task.task.run();
      if (task instanceof RepeatingTimedTask repeatingTask) {
        ZonedDateTime nextExecutionTime =
            task.time.plus(repeatingTask.repeatDelay, repeatingTask.unit);
        tasks.add(
            new RepeatingTimedTask(
                nextExecutionTime,
                repeatingTask.repeatDelay,
                repeatingTask.unit,
                repeatingTask.task));
      }
    }
  }
}
