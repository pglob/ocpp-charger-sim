package com.sim_backend.websockets.exceptions;

public class IllegalAuthorizeResponse extends RuntimeException {
  public IllegalAuthorizeResponse(String message) {
    super(message);
  }
}
