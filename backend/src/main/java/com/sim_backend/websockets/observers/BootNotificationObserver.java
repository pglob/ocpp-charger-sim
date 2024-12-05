package com.sim_backend.websockets.observers;

import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.messages.BootNotification;
import com.sim_backend.websockets.messages.BootNotificationResponse;

public class BootNotificationObserver {

    private OCPPWebSocketClient webSocketClient;

    private void handleBootNotificationRequest(SimulatorStateMachine currState, BootNotification request) {

        // Create the BootNotification request

        // Current state is booting
        if(currState.getCurrentState() == SimulatorState.BootingUp){
            // TODO: Validate request here
            webSocketClient.pushMessage(request);
        }

    }

    // Received a BootNotification Response back from recipient backend
    private void handleBootNotificationResponse(BootNotificationResponse response) {

    }
}
