package com.sim_backend.websockets.observers;

import static org.mockito.Mockito.*;

import com.sim_backend.charger.Charger;
import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.Reason;
import com.sim_backend.websockets.enums.RemoteStartStopStatus;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.messages.RemoteStopTransaction;
import com.sim_backend.websockets.messages.RemoteStopTransactionResponse;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class RemoteStopTransactionObserverTest {

  @Mock private OCPPWebSocketClient client;
  @Mock private Charger charger;
  @Mock private TransactionHandler transactionHandler;

  private RemoteStopTransactionObserver observer;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(charger.getTransactionHandler()).thenReturn(transactionHandler);
    observer = new RemoteStopTransactionObserver(client, charger);
  }

  @Test
  void testRemoteStopTransactionAccepted() {
    when(transactionHandler.getTransactionId()).thenReturn(new AtomicInteger(11));

    RemoteStopTransaction request = new RemoteStopTransaction(11);
    OnOCPPMessage message = mock(OnOCPPMessage.class);
    when(message.getMessage()).thenReturn(request);

    observer.onMessageReceived(message);

    verify(transactionHandler).stopCharging(null, Reason.REMOTE);

    ArgumentCaptor<RemoteStopTransactionResponse> captor =
        ArgumentCaptor.forClass(RemoteStopTransactionResponse.class);
    verify(client).pushMessage(captor.capture());

    RemoteStopTransactionResponse response = captor.getValue();
    assert response.getStatus() == RemoteStartStopStatus.ACCEPTED;
  }

  @Test
  void testRemoteStopTransactionRejected() {
    when(transactionHandler.getTransactionId()).thenReturn(new AtomicInteger(11));

    RemoteStopTransaction request = new RemoteStopTransaction(22);
    OnOCPPMessage message = mock(OnOCPPMessage.class);
    when(message.getMessage()).thenReturn(request);

    observer.onMessageReceived(message);

    verify(transactionHandler, never()).stopCharging(any(), any());

    ArgumentCaptor<RemoteStopTransactionResponse> captor =
        ArgumentCaptor.forClass(RemoteStopTransactionResponse.class);
    verify(client).pushMessage(captor.capture());

    RemoteStopTransactionResponse response = captor.getValue();
    assert response.getStatus() == RemoteStartStopStatus.REJECTED;
  }
}
