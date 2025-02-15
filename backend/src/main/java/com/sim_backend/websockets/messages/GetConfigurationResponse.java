package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/** Represents an OCPP 1.6 GetConfiguration Request from Central System to a Charge Point */
@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "GetConfigurationResponse")
public class GetConfigurationResponse extends OCPPMessageResponse {
  @SerializedName("configurationKey")
  private final List<Configuration> configurationKey;

  @SerializedName("unknownKey")
  private final List<String> unknownKey;

  /** Represents a configuration key and its value */
  @Getter
  @Setter
  @AllArgsConstructor
  public static class Configuration {
    @SerializedName("key")
    private final String key;

    @SerializedName("value")
    private final String value;

    @SerializedName("readonly")
    private final boolean readonly;
  }
}
