package com.sim_backend.websockets.observers;

import static org.mockito.Mockito.*;

import com.sim_backend.ConfigurationRegistry;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.ConfigurationStatus;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.messages.ChangeConfiguration;
import com.sim_backend.websockets.messages.ChangeConfigurationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChangeConfigurationObserverTest {
  @Mock private OCPPWebSocketClient client;
  @Mock private OnOCPPMessage message;
  private ConfigurationRegistry registry;
  private ChangeConfigurationObserver observer;

  @BeforeEach
  void SetUp() {
    MockitoAnnotations.openMocks(this);
    registry = new ConfigurationRegistry(null, null, null, null, 0, null);
    observer = new ChangeConfigurationObserver(client, registry);
  }

  @Test
  void testValidMeterValueSampleIntervalChange() {
    ChangeConfiguration request = new ChangeConfiguration("MeterValueSampleInterval", "33");

    when(message.getMessage()).thenReturn(request);
    observer.onMessageReceived(message);

    ArgumentCaptor<ChangeConfigurationResponse> captor =
        ArgumentCaptor.forClass(ChangeConfigurationResponse.class);
    verify(client, times(1)).pushMessage(captor.capture());
    assert captor.getValue().getStatus() == ConfigurationStatus.ACCEPTED;
    assert registry.getMeterValueSampleInterval() == 33;
  }

  @Test
  void testInvalidMeterValueSampleIntervalChange() {
    ChangeConfiguration request = new ChangeConfiguration("MeterValueSampleInterval", "-1");

    when(message.getMessage()).thenReturn(request);
    observer.onMessageReceived(message);

    ArgumentCaptor<ChangeConfigurationResponse> captor =
        ArgumentCaptor.forClass(ChangeConfigurationResponse.class);
    verify(client, times(1)).pushMessage(captor.capture());
    assert captor.getValue().getStatus() == ConfigurationStatus.REJECTED;
  }

  @Test
  void testInvalidFormatMeterValuesSampleIntervalChange() {
    ChangeConfiguration request = new ChangeConfiguration("MeterValueSampleInterval", "Noninteger");

    when(message.getMessage()).thenReturn(request);
    observer.onMessageReceived(message);
    ArgumentCaptor<ChangeConfigurationResponse> captor =
        ArgumentCaptor.forClass(ChangeConfigurationResponse.class);
    verify(client, times(1)).pushMessage(captor.capture());
    assert captor.getValue().getStatus() == ConfigurationStatus.REJECTED;
  }

  @Test
  void testValidMeterValuesSampledDataChange() {
    ChangeConfiguration request =
        new ChangeConfiguration("MeterValuesSampledData", "Current.Offered");

    when(message.getMessage()).thenReturn(request);
    observer.onMessageReceived(message);
    ArgumentCaptor<ChangeConfigurationResponse> captor =
        ArgumentCaptor.forClass(ChangeConfigurationResponse.class);
    verify(client, times(1)).pushMessage(captor.capture());
    assert captor.getValue().getStatus() == ConfigurationStatus.ACCEPTED;
  }

  @Test
  void testInvalidMeterValuesSampledDataChange() {
    ChangeConfiguration request = new ChangeConfiguration("MeterValuesSampledData", "Garbage");

    when(message.getMessage()).thenReturn(request);
    observer.onMessageReceived(message);
    ArgumentCaptor<ChangeConfigurationResponse> captor =
        ArgumentCaptor.forClass(ChangeConfigurationResponse.class);
    verify(client, times(1)).pushMessage(captor.capture());
    assert captor.getValue().getStatus() == ConfigurationStatus.REJECTED;
  }

  @Test
  void testInvalidKeyChange() {
    ChangeConfiguration request = new ChangeConfiguration("InvalidKey", "Test");

    when(message.getMessage()).thenReturn(request);
    observer.onMessageReceived(message);
    ArgumentCaptor<ChangeConfigurationResponse> captor =
        ArgumentCaptor.forClass(ChangeConfigurationResponse.class);
    verify(client, times(1)).pushMessage(captor.capture());
    assert captor.getValue().getStatus() == ConfigurationStatus.NOTSUPPORTED;
  }
}
