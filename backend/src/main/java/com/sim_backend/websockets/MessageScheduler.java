package com.sim_backend.websockets;

import com.sim_backend.websockets.messages.HeartBeat;
import com.sim_backend.websockets.types.OCPPMessage;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/** An OCPPMessageScheduler. */
@Slf4j
public class MessageScheduler implements AutoCloseable {
  /** Our Synchronized Time. */
  private final OCPPTime time;

  /** The OCPPWebSocket we will send our messages through. */
  private final OCPPWebSocketClient client;

  /** Our message scheduler. */
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

  /**
   * Our set heartbeat interval, we don't want this too common but every 4 minutes should be enough.
   */
  private static final long HEARTBEAT_INTERVAL = 240L; // seconds

  /**
   * Our heartbeat job.
   */
  private ScheduledFuture<?> heartbeat;

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
   * @param interval The Duration between heartbeats.
   * @param unit The unit of your interval.
   * @return The created job.
   */
  public ScheduledFuture<?> setHeartbeatInterval(Long interval, TimeUnit unit) {
    if (this.heartbeat != null) {
      this.heartbeat.cancel(true);
    }

    return (this.heartbeat = this.periodicJob(0, interval, unit, new HeartBeat()));
  }

  /**
   * Create a Runnable instance for our job functions.
   *
   * @param message The message to wrap.
   * @return The created Runnable instance.
   */
  private Runnable createRunnable(final OCPPMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("message must not be null");
    }

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
   * @param initialDelay the initial delay.
   * @param delay The delay between messages.
   * @param timeUnit The time units you wish to use.
   * @param message The message.
   * @return A ScheduledFuture representing the job.
   */
  public ScheduledFuture<?> periodicJob(
      long initialDelay, long delay, TimeUnit timeUnit, OCPPMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("message must not be null");
    }

    if (initialDelay < 0 || delay <= 0) {
      throw new IllegalArgumentException("Initial delay and delay must be positive");
    }
    Runnable job = this.createRunnable(message);
    return scheduler.scheduleAtFixedRate(job, initialDelay, delay, timeUnit);
  }

  /**
   * Register a job at a set time.
   *
   * @param delay The delay at which to send the message.
   * @param timeUnit The time units you wish to use.
   * @param message The message.
   * @return A ScheduledFuture representing the job.
   */
  public ScheduledFuture<?> registerJob(long delay, TimeUnit timeUnit, OCPPMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("message must not be null");
    }

    if (delay <= 0) {
      throw new IllegalArgumentException("Initial delay and delay must be positive");
    }

    Runnable job = this.createRunnable(message);

    return scheduler.schedule(job, delay, timeUnit);
  }

  /**
   * Register a job at a set time.
   *
   * @param timeToSend The time we should send it.
   * @param message The message to send.
   * @return A ScheduledFuture representing the job.
   */
  public ScheduledFuture<?> registerJob(ZonedDateTime timeToSend, OCPPMessage message) {
    if (timeToSend == null || message == null) {
      throw new IllegalArgumentException("timeToSend and message must not be null");
    }

    ZonedDateTime currentTime = time.getSynchronizedTime();
    ZonedDateTime synchronizedTime = time.getSynchronizedTime(timeToSend);

    if (currentTime.isAfter(synchronizedTime)) {
      throw new IllegalArgumentException("Scheduled time is in the past: " + synchronizedTime);
    }
    Duration duration = Duration.between(currentTime, synchronizedTime);

    return this.registerJob(duration.toMillis(), TimeUnit.MILLISECONDS, message);
  }

  @Override
  public void close() {
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
        log.warn("Forcing scheduler shutdown due to timeout");
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      log.error("Interrupted during scheduler shutdown", e);
      Thread.currentThread().interrupt();
      scheduler.shutdownNow();
    }
  }
}
