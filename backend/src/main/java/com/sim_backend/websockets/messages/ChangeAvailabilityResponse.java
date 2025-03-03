package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.enums.AvailabilityStatus;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/*
 * Represents an OCPP 1.6 ChangeAvailability Response sent from a Charge Point to notify if the availability change was successful.
 */
@Getter
public class ChangeAvailabilityResponse extends OCPPMessageResponse implements Cloneable {
  @NotNull
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
