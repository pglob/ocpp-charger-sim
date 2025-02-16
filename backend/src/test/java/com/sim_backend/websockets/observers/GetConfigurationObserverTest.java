package com.sim_backend.websockets.observers;

import static org.mockito.Mockito.*;

import com.sim_backend.config.ConfigurationRegistry;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.messages.GetConfiguration;
import com.sim_backend.websockets.messages.GetConfigurationResponse;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GetConfigurationObserverTest {
  @Mock private OCPPWebSocketClient client;
  @Mock private OnOCPPMessage message;
  private ConfigurationRegistry registry;
  private GetConfigurationObserver observer;

  @BeforeEach
  void SetUp() {
    MockitoAnnotations.openMocks(this);
    registry = new ConfigurationRegistry(null, null);
    observer = new GetConfigurationObserver(client, registry);
  }

  @Test
  void testGetConfiguration() {
    GetConfiguration request =
        new GetConfiguration(
            Arrays.asList(
                "MeterValueSampleInterval", "MeterValuesSampledData", "Unknown1", "Unknown2"));

    when(message.getMessage()).thenReturn(request);
    observer.onMessageReceived(message);

    ArgumentCaptor<GetConfigurationResponse> captor =
        ArgumentCaptor.forClass(GetConfigurationResponse.class);
    verify(client, times(1)).pushMessage(captor.capture());
    GetConfigurationResponse response = captor.getValue();

    System.out.println(response);

    assert response.getConfigurationKey().size() == 2;
    assert response.getConfigurationKey().get(0).getKey().equals("MeterValueSampleInterval");
    assert response.getConfigurationKey().get(0).getValue().equals("30");
    assert response.getConfigurationKey().get(1).getKey().equals("MeterValuesSampledData");
    assert response.getConfigurationKey().get(1).getValue().equals("ENERGY_ACTIVE_IMPORT_REGISTER");

    assert response.getUnknownKey().size() == 2;
    assert response.getUnknownKey().get(0).equals("Unknown1");
    assert response.getUnknownKey().get(1).equals("Unknown2");
  }

  @Test
  void testEmptyKeyGetConfiguration() {
    GetConfiguration request = new GetConfiguration(null);

    when(message.getMessage()).thenReturn(request);
    observer.onMessageReceived(message);

    ArgumentCaptor<GetConfigurationResponse> captor =
        ArgumentCaptor.forClass(GetConfigurationResponse.class);
    verify(client, times(1)).pushMessage(captor.capture());
    GetConfigurationResponse response = captor.getValue();

    System.out.println(response);

    assert response.getConfigurationKey().size() == 2;
    assert response.getConfigurationKey().get(0).getKey().equals("MeterValueSampleInterval");
    assert response.getConfigurationKey().get(0).getValue().equals("30");
    assert response.getConfigurationKey().get(1).getKey().equals("MeterValuesSampledData");
    assert response.getConfigurationKey().get(1).getValue().equals("ENERGY_ACTIVE_IMPORT_REGISTER");
  }
}
