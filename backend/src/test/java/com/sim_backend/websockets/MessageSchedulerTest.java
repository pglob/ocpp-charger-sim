package com.sim_backend.websockets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sim_backend.websockets.OCPPWebSocketClientTest.TestOCPPWebSocketClient;
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPRepeatingTimedTask;
import com.sim_backend.websockets.types.OCPPTimedTask;
import com.sim_backend.websockets.types.TimedTask;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class MessageSchedulerTest {

  @Spy private TestOCPPWebSocketClient client = new TestOCPPWebSocketClient(new URI(""));

  @Spy private OCPPTime time = new OCPPTime(client);

  @Mock private OCPPMessage message;

  private MessageScheduler scheduler;

  public MessageSchedulerTest() throws URISyntaxException {}

  @BeforeEach
  void setUp() throws URISyntaxException {
    MockitoAnnotations.openMocks(this);
    when(time.getSynchronizedTime()).thenReturn(ZonedDateTime.now());

    client =
        spy(
            new TestOCPPWebSocketClient(
                new URI("ws://localhost:8080/sim_backend")));
    
    scheduler =
        new MessageScheduler(client) {
          @Override
          public OCPPTime getTime() {
            return time;
          }
        };
  }

  @Test
  public void testNullMessage() {
    ZonedDateTime time = ZonedDateTime.of(3000, 10, 10, 10, 2, 10, 10, ZoneId.of("UTC"));

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          scheduler.periodicJob(0, 2, TimeUnit.SECONDS, null);
        });
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          scheduler.registerJob(2, TimeUnit.SECONDS, null);
        });
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          scheduler.registerJob(time, null);
        });
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          scheduler.registerJob(null, new Heartbeat());
        });
  }

  @Test
  public void testNegativeDelays() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          scheduler.periodicJob(-1, 2, TimeUnit.SECONDS, new Heartbeat());
        });

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          scheduler.periodicJob(0, -2, TimeUnit.SECONDS, new Heartbeat());
        });

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          scheduler.registerJob(-1, TimeUnit.SECONDS, new Heartbeat());
        });
  }

  @Test
  void testSetHeartbeatInterval() {
    long interval = 240L;
    TimeUnit unit = TimeUnit.SECONDS;

    TimedTask heartbeatTask = scheduler.getTime().setHeartbeatInterval(interval, unit);

    assertNotNull(heartbeatTask);
  }

  @Test
  void testPeriodicJob() {
    long initialDelay = 10L;
    long delay = 20L;
    TimeUnit unit = TimeUnit.SECONDS;

    // Register a periodic job
    OCPPTimedTask task = scheduler.periodicJob(initialDelay, delay, unit, message);

    // Verify the task is scheduled correctly
    assertNotNull(task);
    assertEquals(
        initialDelay, Duration.between(time.getSynchronizedTime(), task.time).getSeconds());
  }

  @Test
  void testRegisterJobWithDelay() {
    long delay = 5L;
    TimeUnit unit = TimeUnit.SECONDS;

    // Register a job with a 5 second delay
    TimedTask task = scheduler.registerJob(delay, unit, message);

    // Verify the task is scheduled correctly
    assertNotNull(task);
    assertTrue(task.time.isAfter(time.getSynchronizedTime()));
  }

  @Test
  void testRegisterJobAtSpecificTime() {
    ZonedDateTime specificTime = ZonedDateTime.now().plusMinutes(1);

    // Register a job at a specific time
    OCPPTimedTask task = scheduler.registerJob(specificTime, message);

    // Verify the task is scheduled correctly
    assertNotNull(task);
    assertEquals(specificTime, task.time);
  }

  @Test
  void testTickExecutesScheduledTasks() {
    when(time.getSynchronizedTime()).thenReturn(ZonedDateTime.now());
    scheduler.tasks.clear();

    ZonedDateTime futureTime = ZonedDateTime.now().plusSeconds(15);

    OCPPTimedTask task = new OCPPTimedTask(futureTime, new Heartbeat(), client);

    // Add the task manually
    scheduler.tasks.add(task);
    when(time.getSynchronizedTime()).thenReturn(ZonedDateTime.now().plusSeconds(20));

    // Perform a tick
    scheduler.tick();

    // Verify that the task was executed
    verify(client, times(1)).pushMessage(any(OCPPMessage.class));
  }

  @Test
  void testTickDoesNotExecuteUnscheduledTasks() {
    when(time.getSynchronizedTime()).thenReturn(ZonedDateTime.now());
    scheduler.tasks.clear();

    ZonedDateTime futureTime = ZonedDateTime.now().plusSeconds(5);
    Runnable taskRunnable = mock(Runnable.class);
    OCPPTimedTask task = new OCPPTimedTask(futureTime, new Heartbeat(), client);

    // Add the task manually
    scheduler.tasks.add(task);

    // Perform a tick before the task is due
    scheduler.tick();

    // Verify that the task was not executed yet
    verify(taskRunnable, never()).run();
  }

  @Test
  void testTickExecutesRepeatingTask() {
    ZonedDateTime initialTime = ZonedDateTime.now().plusSeconds(5);
    long repeatDelay = 10L; // Task repeats every 10 seconds

    // Create a repeating task
    OCPPRepeatingTimedTask repeatingTask =
        new OCPPRepeatingTimedTask(
            initialTime, repeatDelay, ChronoUnit.SECONDS, new Heartbeat(), client);

    // Add the task manually
    scheduler.tasks.add(repeatingTask);
    when(time.getSynchronizedTime()).thenReturn(ZonedDateTime.now().plusSeconds(15));

    // Perform a tick
    scheduler.tick();

    // Verify that the task was executed
    verify(client, times(1)).pushMessage(any(OCPPMessage.class));

    // Verify that the task is rescheduled with the repeat delay
    assertTrue(
        scheduler.tasks.stream()
            .anyMatch(t -> t.time.isEqual(initialTime.plusSeconds(repeatDelay))));
  }

  @Test
  void testTickHandlesEmptyTaskList() {
    scheduler.tick();
  }
}
