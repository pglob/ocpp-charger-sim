package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.enums.AvailabilityStatus;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import lombok.Getter;

@Getter
public class ChangeAvailabilityResponse extends OCPPMessageResponse implements Cloneable {
  @SerializedName("status")
  private AvailabilityStatus status;

  public ChangeAvailabilityResponse(ChangeAvailability request, AvailabilityStatus status) {
    super(request);
    this.status = status;
  }

  @Override
  public ChangeAvailabilityResponse clone() {
    return (ChangeAvailabilityResponse) super.clone();
  }
}
