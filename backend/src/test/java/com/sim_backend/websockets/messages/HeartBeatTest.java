package com.sim_backend.websockets.messages;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.google.gson.JsonElement;
import com.networknt.schema.*;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.OCPPMessage;
import com.sim_backend.websockets.OCPPWebSocketClient;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HeartBeatTest {

  OCPPWebSocketClient client;

  @BeforeEach
  void setUp() throws URISyntaxException {
    client = spy(new OCPPWebSocketClient(new URI("")));
  }

  @Test
  public void testHeartBeatConstructor() {
    ZonedDateTime time = ZonedDateTime.of(2004, 10, 10, 10, 2, 10, 10, ZoneId.of("UTC"));
    HeartBeatResponseMessage heartBeat = new HeartBeatResponseMessage(time);
    assert heartBeat.getCurrentTime() == time;
  }

  @Test
  public void testSendMessage() {
    Pattern pattern = Pattern.compile("^\\[2,\\s*\".*?\",\\s*\"HeartBeat\",\\s*\\{}]$");
    doAnswer(
            invocation -> {
              assert pattern.matcher(invocation.getArgument(0)).matches();
              return null;
            })
        .when(client)
        .send(anyString());

    OCPPMessage beatResponse = new HeartBeatMessage();
    beatResponse.sendMessage(client);

    verify(client, times(1)).send(anyString());
  }

  @Test
  public void testResponseJSON() {
    HeartBeatResponseMessage heartBeat = new HeartBeatResponseMessage();
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/HeartbeatResponse.json");
    JsonElement jsonElement = heartBeat.generateMessage().get(3);
    Set<ValidationMessage> errors =
        jsonSchema.validate(GsonUtilities.toString(jsonElement), InputFormat.JSON);
    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }
    assert errors.isEmpty();

    heartBeat =
        new HeartBeatResponseMessage(
            ZonedDateTime.of(2004, 10, 10, 10, 2, 10, 10, ZoneId.of("UTC")));
    String json = GsonUtilities.toString(heartBeat.generateMessage().get(3));
    assert json.contains("\"currentTime\":\"2004-10-10T10:02:10.00000001Z\"");

    heartBeat =
        new HeartBeatResponseMessage(
            ZonedDateTime.of(2004, 10, 10, 10, 2, 10, 10, ZoneId.of("UTC-7")));
    json = GsonUtilities.toString(heartBeat.generateMessage().get(3));
    assert json.contains("\"currentTime\":\"2004-10-10T10:02:10.00000001-07:00\"");
  }

  @Test
  public void testRequestJSON() {
    HeartBeatMessage heartBeat = new HeartBeatMessage();
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
    InputStream test = this.getClass().getResourceAsStream("schemas/Heartbeat.json");
    JsonSchema jsonSchema = factory.getSchema(test);

    JsonElement jsonElement = heartBeat.generateMessage().get(3);
    Set<ValidationMessage> errors =
        jsonSchema.validate(GsonUtilities.toString(jsonElement), InputFormat.JSON);
    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    assert errors.isEmpty();
  }
}
