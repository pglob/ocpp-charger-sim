package com.sim_backend.websockets;

import com.google.common.annotations.VisibleForTesting;
import com.sim_backend.websockets.types.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** An OCPPMessageScheduler. */
@Slf4j
public class MessageScheduler {
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
   * Create a Periodic Job.
   *
   * @param initialDelay The initial delay before we send our first message.
   * @param delay The delay between messages.
   * @param timeUnit The time units you wish to use.
   * @param message The message.
   */
  public OCPPRepeatingTimedTask periodicJob(
      long initialDelay, long delay, TimeUnit timeUnit, OCPPMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("message must not be null");
    }

    if (initialDelay < 0 || delay <= 0) {
      throw new IllegalArgumentException("Initial delay and delay must be positive");
    }

    OCPPRepeatingTimedTask task =
        new OCPPRepeatingTimedTask(
            getTime().getSynchronizedTime().plus(initialDelay, timeUnit.toChronoUnit()),
            delay,
            timeUnit.toChronoUnit(),
            message,
            client);
    tasks.add(task);
    return task;
  }

  /**
   * Create a periodic function job.
   *
   * @param initialDelay The initial delay before the first execution.
   * @param delay The delay between subsequent executions.
   * @param timeUnit The time units for the delays.
   * @param task The Runnable function to execute.
   * @return A RepeatingTimedTask representing the scheduled function.
   */
  public RepeatingTimedTask periodicFunctionJob(
      long initialDelay, long delay, TimeUnit timeUnit, Runnable task) {
    if (task == null) {
      throw new IllegalArgumentException("Task must not be null");
    }
    if (initialDelay < 0 || delay <= 0) {
      throw new IllegalArgumentException("Initial delay and delay must be positive");
    }

    RepeatingTimedTask repeatingTask =
        new RepeatingTimedTask(
            getTime().getSynchronizedTime().plus(initialDelay, timeUnit.toChronoUnit()),
            delay,
            timeUnit.toChronoUnit(),
            task);
    tasks.add(repeatingTask);
    return repeatingTask;
  }

  /**
   * Register a job at a set time.
   *
   * @param delay The delay at which to send the message.
   * @param timeUnit The time units you wish to use.
   * @param message The message.
   */
  public OCPPTimedTask registerJob(long delay, TimeUnit timeUnit, OCPPMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("message must not be null");
    }

    if (delay <= 0) {
      throw new IllegalArgumentException("Delay must be positive");
    }

    OCPPTimedTask task =
        new OCPPTimedTask(
            getTime().getSynchronizedTime().plus(delay, timeUnit.toChronoUnit()), message, client);
    tasks.add(task);
    return task;
  }

  /**
   * Register a job at a set time.
   *
   * @param timeToSend The time we should send it.
   * @param message The message to send.
   */
  public OCPPTimedTask registerJob(ZonedDateTime timeToSend, OCPPMessage message) {
    if (timeToSend == null || message == null) {
      throw new IllegalArgumentException("timeToSend and message must not be null");
    }

    OCPPTimedTask task = new OCPPTimedTask(timeToSend, message, client);
    tasks.add(task);
    return task;
  }

  /**
   * Kill a Registered Job.
   *
   * @param task the job to kill.
   */
  public void killJob(TimedTask task) {
    if (task instanceof RepeatingTimedTask repeatingTask) {
      repeatingTask.cancel();
    }
    tasks.remove(task);
  }

  /** Tick our scheduler to check for messages to be sent. */
  public void tick() {
    if (!client.isOnline()) return;

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
      // Skip execution if this repeating task has been cancelled
      if (task instanceof RepeatingTimedTask repeatingTask && repeatingTask.isCancelled()) {
        continue;
      }
      task.task.run();
      if (task instanceof RepeatingTask repeatingTask) {
        TimedTask newTask = repeatingTask.repeatTask();
        if (newTask != null) { // Only add if not cancelled
          tasks.add(newTask);
        }
      }
    }
  }
}
