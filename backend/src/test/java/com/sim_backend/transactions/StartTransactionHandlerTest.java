package com.sim_backend.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.exceptions.OCPPMessageFailure;
import com.sim_backend.websockets.messages.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StartTransactionHandlerTest {
  private SimulatorStateMachine testStateMachine;
  private OCPPWebSocketClient client;

  @BeforeEach
  void setUp() throws URISyntaxException {
    testStateMachine = new SimulatorStateMachine();
    testStateMachine.transition(SimulatorState.BootingUp);
    testStateMachine.transition(SimulatorState.Available);

    client = spy(new OCPPWebSocketClient(new URI("")));
  }

  @Test
  public void testStartTransaction() throws OCPPMessageFailure, InterruptedException {
    String idTag = "Accepted";
    int connectorId = 1;
    int meterStart = 0;
    String timestamp = Instant.now().toString();

    // Mock Authorize Response
    AuthorizeResponse response = new AuthorizeResponse("Accepted");
    Authorize auth = new Authorize(idTag);
    client.addPreviousMessage(auth);
    client.pushMessage(auth);
    doAnswer(
            invocation -> {
              client.addPreviousMessage(auth);
              response.setMessageID(auth.getMessageID());
              client.onMessage(response.toJsonString());
              return null;
            })
        .when(client)
        .send(anyString());

    client.onReceiveMessage(
        AuthorizeResponse.class,
        message -> {
          assert (message.getMessage() instanceof AuthorizeResponse);
          AuthorizeResponse response2 = (AuthorizeResponse) message.getMessage();
          assert (response2.getIdTagInfo().getStatus() == AuthorizationStatus.ACCEPTED);
          testStateMachine.transition(SimulatorState.Preparing);
        });

    client.popAllMessages();
    assertEquals(testStateMachine.getCurrentState(), SimulatorState.Preparing);

    //Mock Transaction Response
    StartTransactionResponse response3 = new StartTransactionResponse(1, idTag);
    StartTransaction transaction = new StartTransaction(connectorId, idTag, meterStart, timestamp);
    client.addPreviousMessage(transaction);
    client.pushMessage(transaction);
    doAnswer(
            invocation -> {
              client.addPreviousMessage(transaction);
              response3.setMessageID(transaction.getMessageID());
              client.onMessage(response3.toJsonString());
              return null;
            })
        .when(client)
        .send(anyString());

    client.onReceiveMessage(
        StartTransactionResponse.class,
        message -> {
          assert (message.getMessage() instanceof StartTransactionResponse);
          StartTransactionResponse response4 = (StartTransactionResponse) message.getMessage();
          assertEquals(response4.getIdTaginfo().getStatus(), "Accepted");
          testStateMachine.transition(SimulatorState.Charging);
        });

    client.popAllMessages();
    assertEquals(testStateMachine.getCurrentState(), SimulatorState.Charging);
  }
}
