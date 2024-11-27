package com.sim_backend.websockets;

import com.sim_backend.websockets.messages.HeartBeat;
import com.sim_backend.websockets.types.OCPPMessage;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MessageScheduler {
  /** Our Synchronized Time. */
  private final OCPPTime time;

  /** The OCPPWebSocket we will send our messages through. */
  private final OCPPWebSocketClient client;

  /** Our message scheduler. */
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  /**
   * An OCPPMessage Scheduler.
   *
   * @param targetClient The client to send our messages through.
   */
  public MessageScheduler(OCPPWebSocketClient targetClient) {
    this.client = targetClient;
    this.time = new OCPPTime(targetClient);

    this.periodicJob(240, TimeUnit.SECONDS, new HeartBeat());
  }

  /**
   * Create a Runnable instance for our job functions.
   *
   * @param message The message to wrap.
   * @return The created Runnable instance.
   */
  private Runnable createRunnable(final OCPPMessage message) {
    return () -> {
      this.client.pushMessage(message);
      message.refreshMessage();
    };
  }

  /**
   * Create a Periodic Job.
   *
   * @param delay The delay between messages.
   * @param timeUnit The time units you wish to use.
   * @param message The message.
   * @return A ScheduledFuture representing the job.
   */
  public ScheduledFuture<?> periodicJob(int delay, TimeUnit timeUnit, OCPPMessage message) {
    Runnable job = this.createRunnable(message);

    return scheduler.scheduleAtFixedRate(job, 0, delay, timeUnit);
  }

  /**
   * Register a job at a set time.
   *
   * @param delay The delay at which to send the message.
   * @param timeUnit The time units you wish to use.
   * @param message The message.
   * @return A ScheduledFuture representing the job.
   */
  public ScheduledFuture<?> registerJob(int delay, TimeUnit timeUnit, OCPPMessage message) {
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
    ZonedDateTime currentTime = time.getSynchronizedTime();
    ZonedDateTime synchronizedTime = time.getSynchronizedTime(timeToSend);

    if (currentTime.isAfter(synchronizedTime)) {
      return null;
    }
    Duration duration = Duration.between(currentTime, synchronizedTime);

    return this.registerJob(duration.getNano(), TimeUnit.NANOSECONDS, message);
  }
}
