package com.sim_backend.rest;

import com.sim_backend.Main;
import io.javalin.Javalin;

import io.javalin.http.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

public class RestIT {
    @Test
    public void testGet() {
        Javalin mock = mock(Javalin.class);
        doAnswer(invocation -> {
            Javalin app = mock.start(invocation.getArgument(0));
            return app;

        }).when(mock).start(anyInt());

        Main.main(new String[] {});
    }

}