package com.sim_backend.websockets.messages;

import com.networknt.schema.*;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.OCCPMessage;
import com.sim_backend.websockets.OCCPWebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class HeartBeatTest {

    OCCPWebSocketClient client;

    @BeforeEach
    void setUp() throws URISyntaxException {
        client = spy(new OCCPWebSocketClient(new URI("")));
    }


    @Test
    public void testSendMessage() {
        doAnswer(invocation -> {
            assert invocation.getArgument(0, String.class).equals("{}");
            return null;
        }).when(client).send(anyString());

        OCCPMessage beatResponse = new HeartBeat();
        beatResponse.sendMessage(client);

        verify(client, times(1)).send(anyString());
    }


    @Test
    public void testResponseJSON() {
        HeartBeatResponse heartBeat = new HeartBeatResponse();
        JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/HeartbeatResponse.json");
        Set<ValidationMessage> errors = jsonSchema.validate(GsonUtilities.toString(heartBeat.generateMessage()), InputFormat.JSON);
        if (!errors.isEmpty()) {
            for (ValidationMessage error : errors) {
                System.out.println(error);
            }
        }
        assert errors.isEmpty();


        heartBeat = new HeartBeatResponse(ZonedDateTime.of(
                2004,
                10,
                10,
                10,
                2,
                10,
                10, ZoneId.of("UTC")));
        String json = GsonUtilities.toString(heartBeat.generateMessage());
        assert json.contains("\"currentTime\":\"2004-10-10T10:02:10.00000001Z\"");


        heartBeat = new HeartBeatResponse(ZonedDateTime.of(
                2004,
                10,
                10,
                10,
                2,
                10,
                10, ZoneId.of("UTC-7")));
        json = GsonUtilities.toString(heartBeat.generateMessage());
        assert json.contains("\"currentTime\":\"2004-10-10T10:02:10.00000001-07:00\"");
    }

    @Test
    public void testRequestJSON() {
        HeartBeat heartBeat = new HeartBeat();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        InputStream test = this.getClass().getResourceAsStream("schemas/Heartbeat.json");
        JsonSchema jsonSchema = factory.getSchema(test);

        Set<ValidationMessage> errors = jsonSchema.validate(GsonUtilities.toString(heartBeat.generateMessage()), InputFormat.JSON);
        if (!errors.isEmpty()) {
            for (ValidationMessage error : errors) {
                System.out.println(error);
            }
        }

        assert errors.isEmpty();
    }
}
