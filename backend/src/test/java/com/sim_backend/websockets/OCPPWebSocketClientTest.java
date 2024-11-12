package com.sim_backend.websockets;

import com.sim_backend.websockets.messages.HeartBeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OCPPWebSocketClientTest {

    OCPPWebSocketClient client;
    MessageQueue queue;
    OnOCPPMessage onOCPPMessageMock;

    @BeforeEach
    void setUp() throws URISyntaxException {
        onOCPPMessageMock = mock(OnOCPPMessage.class);
        client = spy(new OCPPWebSocketClient(new URI("")));
        queue = mock(MessageQueue.class);
    }

    @Test
    public void testPopOnEmptyQueue() throws OCPPMessageFailure, InterruptedException {
        assert client.popMessage() == null;
    }

    @Test
    public void testPushThenPop() throws OCPPMessageFailure, InterruptedException {
        HeartBeat heartbeat = new HeartBeat();
        doReturn(heartbeat).when(client).popMessage();
        client.pushMessage(heartbeat);

        assert client.popMessage() == heartbeat;
    }

    @Test
    public void testPopMessage() throws OCPPMessageFailure, InterruptedException {
        doAnswer(invocation -> {
            assert invocation.getArgument(0, String.class).equals("{}");
            return null;
        }).when(client).send(anyString());


        HeartBeat beat = new HeartBeat();
        client.pushMessage(beat);
        client.popMessage();
        verify(client, times(1)).send(anyString());
    }

    @Test void testPopAllMessages() throws OCPPMessageFailure, InterruptedException {
        doAnswer(invocation -> {
            assert invocation.getArgument(0, String.class).equals("{}");
            return null;
        }).when(client).send(anyString());

        HeartBeat beat = new HeartBeat();
        HeartBeat beat2 = new HeartBeat();

        client.pushMessage(beat);
        assert client.size() == 1;
        client.pushMessage(beat2);
        assert client.size() == 2;

        client.popAllMessages();
        assert client.isEmpty();

        verify(client, times(2)).send(anyString());
    }

    @Test
    public void testOnReceiveMessage() throws OCPPMessageFailure, InterruptedException {
        doAnswer(invocation -> {
            client.onMessage("{}");
            return null;
        }).when(client).send(anyString());

        HeartBeat beat = new HeartBeat();

        client.pushMessage(beat);
        client.setOnRecieveMessage(message -> {
            assert message.getMessage() instanceof HeartBeat;
        });

        client.popAllMessages();

        //verify(onOCPPMessageMock, times(1)).getMessage();
    }

    @Test
    public void testAllThrowsException() throws OCPPMessageFailure {

        HeartBeat beat = new HeartBeat();

        client.pushMessage(beat);
        assert client.size() == 1;

        assertThrows(OCPPMessageFailure.class, () -> {
            client.popAllMessages();
        });
  }
    @Test
    public void testThrowsException() throws OCPPMessageFailure {

        HeartBeat beat = new HeartBeat();

        client.pushMessage(beat);
        assert client.size() == 1;

        OCPPMessageFailure exception = assertThrows(OCPPMessageFailure.class, () -> {
            client.popMessage();
        });
        assert exception != null;
        assert exception.getFailedMessage() == beat;
        assert exception.getInnerException() != null;
    }
}
