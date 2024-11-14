package com.sim_backend;

import static org.mockito.Mockito.*;

import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class MainTest {
  AutoCloseable openMocks;

  @BeforeEach
  void setUp() {
    openMocks = MockitoAnnotations.openMocks(this);
  }

  @Mock Javalin javalinMock;

  @Mock CorsPluginConfig.CorsRule corsRulesMock;

  @Test
  void testGetEnv() {
    doAnswer(
            invocation -> {
              assert invocation.getArgument(0, String.class).startsWith("http://localhost");
              return invocation.callRealMethod();
            })
        .when(corsRulesMock)
        .allowHost(anyString());

    doAnswer(
            invocation -> {
              assert invocation.getArgument(0, Integer.class).equals(8080);
              return invocation.callRealMethod();
            })
        .when(javalinMock)
        .start(anyInt());

    Main.main(new String[] {});
  }
}
