package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.enums.AvailabilityType;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ChangeAvailability extends OCPPMessageRequest {
  @SerializedName("connectorId")
  private int connectorID;

  @SerializedName("type")
  private AvailabilityType type;
}
