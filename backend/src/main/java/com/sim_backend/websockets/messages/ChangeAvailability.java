package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.enums.AvailabilityType;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChangeAvailability extends OCPPMessageRequest implements Cloneable {
  @SerializedName("connectorId")
  private int connectorID;

  @SerializedName("type")
  private AvailabilityType type;

  @Override
  public ChangeAvailability clone() {
    return (ChangeAvailability) super.clone();
  }
}
