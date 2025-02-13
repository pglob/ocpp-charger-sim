package com.sim_backend.websockets;

import com.google.common.annotations.VisibleForTesting;
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
    @Getter OCPPMessage message;

    TimedTask(ZonedDateTime time, Runnable task, OCPPMessage message) {
      this.time = time;
      this.task = task;
      this.message = message;
    }
  }

  public static class RepeatingTimedTask extends TimedTask {
    long repeatDelay;
    ChronoUnit unit;

    RepeatingTimedTask(
        ZonedDateTime time, long repeatTime, ChronoUnit unit, Runnable task, OCPPMessage message) {
      super(time, task, message);
      this.unit = unit;
      this.repeatDelay = repeatTime;
    }
  }

  /** Our Synchronized Time. */
  @Getter private final OCPPTime time;

  /** The OCPPWebSocket we will send our messages through. */
  private final OCPPWebSocketClient client;

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
  }

  /**
   * Sets the OCPPTime to match that of the Central Server
   *
   * @param time The time of the Central Server to synchronize to
   */
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
            job,
            message);
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
        new TimedTask(
            getTime().getSynchronizedTime().plus(delay, timeUnit.toChronoUnit()), job, message);
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

    TimedTask task = new TimedTask(timeToSend, this.createRunnable(message), message);
    tasks.add(task);
    return task;
  }

  /**
   * Kill a Registered Job.
   *
   * @param task the job to kill.
   */
  public void killJob(TimedTask task) {
    tasks.remove(task);
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
                repeatingTask.task,
                repeatingTask.message));
      }
    }
  }
}
