package com.sim_backend.websockets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sim_backend.websockets.OCPPWebSocketClientTest.TestOCPPWebSocketClient;
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.observers.StatusNotificationObserver;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPRepeatingTimedTask;
import com.sim_backend.websockets.types.OCPPTimedTask;
import com.sim_backend.websockets.types.RepeatingTimedTask;
import com.sim_backend.websockets.types.TimedTask;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class MessageSchedulerTest {

  @Mock private StatusNotificationObserver statusNotificationObserver;

  @Spy
  private TestOCPPWebSocketClient client =
      new TestOCPPWebSocketClient(new URI(""), statusNotificationObserver);

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
                new URI("ws://localhost:8080/sim_backend"), statusNotificationObserver));

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

  @Test
  void testSynchronizeTime() throws Exception {
    // Create a new synchronization time one hour in the future
    ZonedDateTime newSyncTime = ZonedDateTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS);

    Field timeField = MessageScheduler.class.getDeclaredField("time");
    timeField.setAccessible(true);
    timeField.set(scheduler, time);

    scheduler.synchronizeTime(newSyncTime);

    // Verify that the OCPPTime offset was updated
    verify(time).setOffset(newSyncTime);
  }

  @Test
  void testPeriodicFunctionJob_NullTask() {
    // Expect an exception when a null task is provided
    assertThrows(
        IllegalArgumentException.class,
        () -> scheduler.periodicFunctionJob(10, 10, TimeUnit.SECONDS, null));
  }

  @Test
  void testPeriodicFunctionJob_NegativeInitialDelay() {
    // Expect an exception when the initial delay is negative
    assertThrows(
        IllegalArgumentException.class,
        () -> scheduler.periodicFunctionJob(-1L, 10L, TimeUnit.SECONDS, () -> new Heartbeat()));
  }

  @Test
  void testPeriodicFunctionJob_NegativeDelay() {
    // Expect an exception when the delay is non-positive
    assertThrows(
        IllegalArgumentException.class,
        () -> scheduler.periodicJob(10, 0, TimeUnit.SECONDS, new Heartbeat()));
  }

  @Test
  void testPeriodicFunctionJob_Valid() {
    // Schedule a valid periodic function job and verify its scheduled execution time
    RepeatingTimedTask task =
        scheduler.periodicFunctionJob(10L, 20L, TimeUnit.SECONDS, () -> new Heartbeat());
    assertNotNull(task);

    ZonedDateTime expectedTime =
        time.getSynchronizedTime().plus(10, TimeUnit.SECONDS.toChronoUnit());
    assertEquals(
        expectedTime.truncatedTo(ChronoUnit.SECONDS), task.time.truncatedTo(ChronoUnit.SECONDS));
  }

  @Test
  void testKillJob_NonRepeating() {
    // Register a non-repeating job, then kill it and verify it is removed from the scheduler
    OCPPTimedTask task = scheduler.registerJob(5, TimeUnit.SECONDS, new Heartbeat());
    assertTrue(scheduler.tasks.contains(task));

    scheduler.killJob(task);
    assertFalse(scheduler.tasks.contains(task));
  }

  @Test
  void testKillJob_Repeating() {
    // Create a repeating job, then kill it and verify that it gets cancelled and removed
    RepeatingTimedTask task = scheduler.periodicFunctionJob(10, 10, TimeUnit.SECONDS, () -> {});
    assertFalse(task.isCancelled());

    scheduler.killJob(task);
    assertTrue(task.isCancelled());
    assertFalse(scheduler.tasks.contains(task));
  }

  @Test
  void testTickClientOffline() {
    // Simulate the client being offline; tasks should not execute but remain scheduled
    when(client.isOnline()).thenReturn(false);
    OCPPTimedTask task = scheduler.registerJob(1, TimeUnit.SECONDS, new Heartbeat());
    scheduler.tick();

    verify(client, never()).pushMessage(any(OCPPMessage.class));
    // Verify the task remains scheduled since the client is offline
    assertTrue(scheduler.tasks.contains(task));
  }

  @Test
  void testTickDoesNotExecuteCancelledRepeatingTask() {
    // Create a repeating task that is due, then cancel it and verify that it does not execute
    ZonedDateTime scheduledTime = time.getSynchronizedTime().minusSeconds(1);
    AtomicBoolean executed = new AtomicBoolean(false);
    Runnable taskRunnable = () -> executed.set(true);
    RepeatingTimedTask repeatingTask =
        new RepeatingTimedTask(scheduledTime, 10, ChronoUnit.SECONDS, taskRunnable);

    // Cancel the task so it should not run
    repeatingTask.cancel();
    scheduler.tasks.add(repeatingTask);

    scheduler.tick();

    // Verify that the task did not execute and was removed from the scheduler
    assertFalse(executed.get());
    assertFalse(scheduler.tasks.contains(repeatingTask));
  }

  @Test
  void testRepeatingTaskNotRescheduledWhenRepeatReturnsNull() {
    // Create a repeating task that is due, cancel it, and verify that it is not rescheduled
    ZonedDateTime scheduledTime = time.getSynchronizedTime().minusSeconds(1);
    AtomicBoolean executed = new AtomicBoolean(false);
    Runnable taskRunnable = () -> executed.set(true);
    RepeatingTimedTask repeatingTask =
        new RepeatingTimedTask(scheduledTime, 10, ChronoUnit.SECONDS, taskRunnable);

    // Cancel the task so that repeatTask() returns null
    repeatingTask.cancel();
    scheduler.tasks.add(repeatingTask);

    scheduler.tick();

    ZonedDateTime expectedNextTime = scheduledTime.plus(10, ChronoUnit.SECONDS);
    boolean foundRescheduled =
        scheduler.tasks.stream().anyMatch(t -> t.time.equals(expectedNextTime));

    // Verify that the task was not rescheduled
    assertFalse(foundRescheduled);
  }
}
