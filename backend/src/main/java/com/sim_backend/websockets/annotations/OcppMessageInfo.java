package com.sim_backend.websockets.annotations;

import com.sim_backend.websockets.types.OcppMessage;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Holds the message name and call ID for an OCPP Message for reflection purposes. */
@Retention(RetentionPolicy.RUNTIME)
public @interface OcppMessageInfo {
  /**
   * The Message name.
   *
   * @return The Current Message Name.
   */
  String messageName() default "";

  /**
   * The Call ID to send this message with.
   *
   * @return The call ID either 2, 3, or 4.
   */
  int messageCallId() default OcppMessage.CALL_ID_REQUEST;
}
