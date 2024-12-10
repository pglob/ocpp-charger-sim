package com.sim_backend.websockets.observers;

import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.events.*;
import com.sim_backend.websockets.messages.*;

public class AuthorizeObserver implements OnOCPPMessageListener{

    public void handleAuthorize(OCPPWebSocketClient client, AuthorizeResponse response){
        if(response.getIdTagInfo().getStatus() == AuthorizationStatus.ACCEPTED){
            client.pushMessage(new AuthorizeResponse(response.getIdTagInfo()));
        } else {
            System.out.println("Failed Authorize");
        }
    }

    @Override
    public void onMessageReceived(OnOCPPMessage message) {
        if(!(message.getMessage() instanceof AuthorizeResponse response)){
            throw new ClassCastException("Message is not an AuthorizeResponse");            
        } 
        switch(response.getIdTagInfo().getStatus()){
            case ACCEPTED:
                System.out.println("Authorization Accepted.");
                break;
            case BLOCKED:
                System.err.println("Authorization Blocked for ID.");
                break;
            case EXPIRED:
                System.err.println("Authorization Expired for ID.");
                break;
            case INVALID:
                System.err.println("Invalid ID.");
                break;
            case CONCURRENT:
                System.err.println("Concurrent Transaction Occured.");
            default:
            System.err.println("Unknown Status Received.");
        }
    }
}
