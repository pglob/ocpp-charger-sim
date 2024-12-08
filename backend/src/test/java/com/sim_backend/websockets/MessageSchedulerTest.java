package com.sim_backend.websockets;

import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.messages.Heartbeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class MessageSchedulerTest {
    OCPPWebSocketClient client;
    MessageQueue queue;
    OnOCPPMessage onOCPPMessageMock;
    MessageScheduler scheduler;
    @BeforeEach
    void setUp() throws URISyntaxException {
        onOCPPMessageMock = mock(OnOCPPMessage.class);
        client = spy(new OCPPWebSocketClient(new URI("")));
        queue = mock(MessageQueue.class);
        scheduler = spy(new MessageScheduler(client));
    }

    @Test
    public void testNullMessage() {
        ZonedDateTime time = ZonedDateTime.of(3000, 10, 10, 10, 2, 10, 10, ZoneId.of("UTC"));

        assertThrows(IllegalArgumentException.class, ()-> {
            scheduler.periodicJob(0, 2, TimeUnit.SECONDS, null);
        });
        assertThrows(IllegalArgumentException.class, ()-> {
            scheduler.registerJob(2, TimeUnit.SECONDS, null);
        });
        assertThrows(IllegalArgumentException.class, ()-> {
            scheduler.registerJob(time, null);
        });
        assertThrows(IllegalArgumentException.class, ()-> {
            scheduler.registerJob(null, new Heartbeat());
        });
    }

    @Test
    public void testNegativeDelays() {
        assertThrows(IllegalArgumentException.class, ()-> {
            scheduler.periodicJob(-1, 2, TimeUnit.SECONDS, new Heartbeat());
        });

        assertThrows(IllegalArgumentException.class, ()-> {
            scheduler.periodicJob(0, -2, TimeUnit.SECONDS, new Heartbeat());
        });

        assertThrows(IllegalArgumentException.class, ()-> {
            scheduler.registerJob(-1, TimeUnit.SECONDS, new Heartbeat());
        });
    }
}
