package com.sim_backend.websockets.observers;

import com.sim_backend.config.ConfigurationRegistry;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.MeterValuesSampledData;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.ChangeConfiguration;
import com.sim_backend.websockets.messages.ChangeConfigurationResponse;
import com.sim_backend.websockets.messages.MessageValidator;
import lombok.Getter;

/** Observer that handles ConfigurationRegistry Change Request and Response */
@Getter
public class ChangeConfigurationObserver implements OnOCPPMessageListener {

  OCPPWebSocketClient client;
  ConfigurationRegistry registry;

  /** Constructor self-register observer for the onReceiveMessage Callback */
  public ChangeConfigurationObserver(OCPPWebSocketClient client, ConfigurationRegistry registry) {
    this.client = client;
    this.registry = registry;

    client.onReceiveMessage(ChangeConfiguration.class, this);
  }

  /**
   * Processes incoming ChangeConfiguration messages and handles the change request and response
   * depends on the result.
   *
   * @param message the received OCPP message, expected to be a ChangeConfiguration.
   * @throws ClassCastException if the message is not a ChangeConfiguration.
   */
  @Override
  public void onMessageReceived(OnOCPPMessage message) {
    if (!(message.getMessage() instanceof ChangeConfiguration request)) {
      throw new ClassCastException("Message is not an ChangeConfiguration Request");
    }

    if (!MessageValidator.isValid(request)) {
      throw new IllegalArgumentException(MessageValidator.log_message(request));
    }

    String status = null;

    switch (request.getKey()) {
      case "MeterValueSampleInterval":
        try {
          int value = Integer.parseInt(request.getValue());
          if (value < 0) {
            System.err.println("Invalid Value Detected... Reason : Negative Value");
            status = "Rejected";
          } else {
            registry.setMeterValueSampleInterval(value);
            System.out.println("Successfully Applied Change to MeterValueSampleInterval.");
            status = "Accepted";
          }
        } catch (NumberFormatException e) {
          System.err.println("Invalid Value Format Detected...");
          status = "Rejected";
        }
        break;
      case "MeterValuesSampledData":
        try {
          MeterValuesSampledData value = MeterValuesSampledData.fromString(request.getValue());
          registry.setMeterValuesSampledData(value);
          System.out.println("Successfully Applied Change to MeterValuesSampledData.");
          status = "Accepted";
        } catch (IllegalArgumentException e) {
          System.err.println("Invalid MeterValuesSampledData Detected...");
          status = "Rejected";
        }
        break;
      default:
        System.err.println("Invalid Key Detected...");
        status = "NotSupported";
        break;
    }

    ChangeConfigurationResponse response = new ChangeConfigurationResponse(request, status);
    if (!MessageValidator.isValid(response)) {
      throw new IllegalArgumentException(MessageValidator.log_message(response));
    } else {
      client.pushMessage(response);
    }
  }
}
