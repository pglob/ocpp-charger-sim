package com.sim_backend.websockets.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OCPPBadMessage extends RuntimeException {
  private String message;
}
