package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
/**
 * Represents an OCPP 1.6 Status Notification Response sent by the Central System to acknowledge a
 * Status Notification Request.
 */
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_RESPONSE, messageName = "StatusNotificationResponse")
public class StatusNotificationResponse {
     // No fields are defined as per the protocol specification
}