package com.sim_backend.websockets.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OCPPUnsupportedProtocol extends RuntimeException {
  public String protocol;
}
