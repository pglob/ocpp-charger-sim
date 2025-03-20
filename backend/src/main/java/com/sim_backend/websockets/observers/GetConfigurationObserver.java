package com.sim_backend.websockets.observers;

import com.sim_backend.config.ConfigurationRegistry;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.GetConfiguration;
import com.sim_backend.websockets.messages.GetConfigurationResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/** Observer that handles Getting ConfigurationRegistry Keys and Values Request and Response */
@Getter
public class GetConfigurationObserver implements OnOCPPMessageListener {

  OCPPWebSocketClient client;
  ConfigurationRegistry registry;

  /** Constructor self-register observer for the onReceiveMessage Callback */
  public GetConfigurationObserver(OCPPWebSocketClient client, ConfigurationRegistry registry) {
    this.client = client;
    this.registry = registry;

    client.onReceiveMessage(GetConfiguration.class, this);
  }

  /**
   * Processes incoming GetConfiguration messages and handles the Registry Keys, Value data request
   * and response.
   *
   * @param message the received OCPP message, expected to be a GetConfiguration.
   * @throws ClassCastException if the message is not a GetConfiguration.
   */
  @Override
  public void onMessageReceived(OnOCPPMessage message) {
    if (!(message.getMessage() instanceof GetConfiguration request)) {
      throw new ClassCastException("Message is not an GetConfiguration Request");
    }

    List<GetConfigurationResponse.Configuration> configurationInfo = new ArrayList<>();
    List<String> unknownKeys = new ArrayList<>();

    /*
     * TODO: Retrieve GetConfigurationMaxKeys from the Charge Point
     */
    int GetConfigurationMaxKeys = 5;

    if (request.getKey() == null) {
      configurationInfo.add(
          new GetConfigurationResponse.Configuration(
              "MeterValueSampleInterval",
              String.valueOf(registry.getMeterValueSampleInterval()),
              false));
      configurationInfo.add(
          new GetConfigurationResponse.Configuration(
              "MeterValuesSampledData",
              String.valueOf(registry.getMeterValuesSampledData()),
              false));
      configurationInfo.add(
          new GetConfigurationResponse.Configuration(
              "authorizeRemoteTxRequests",
              String.valueOf(registry.isAuthorizeRemoteTxRequests()),
              false));
    } else {
      int count = 0;
      for (String key : request.getKey()) {
        if (count >= GetConfigurationMaxKeys) {
          break;
        }
        if ("MeterValueSampleInterval".equals(key)) {
          configurationInfo.add(
              new GetConfigurationResponse.Configuration(
                  key, String.valueOf(registry.getMeterValueSampleInterval()), false));
          count++;
        } else if ("MeterValuesSampledData".equals(key)) {
          configurationInfo.add(
              new GetConfigurationResponse.Configuration(
                  key, String.valueOf(registry.getMeterValuesSampledData()), false));
          count++;
        } else if ("authorizeRemoteTxRequests".equals(key)) {
          configurationInfo.add(
              new GetConfigurationResponse.Configuration(
                  key, String.valueOf(registry.isAuthorizeRemoteTxRequests()), false));
          count++;
        } else {
          unknownKeys.add(key);
        }
      }
    }

    GetConfigurationResponse response =
        new GetConfigurationResponse(request, configurationInfo, unknownKeys);

    client.pushMessage(response);
  }
}
