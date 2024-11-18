package com.sim_backend.websockets.annotations;

import com.sim_backend.websockets.types.OCPPMessage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OCPPMessageInfo {
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
  int messageCallID() default OCPPMessage.CALL_ID_REQUEST;
}
