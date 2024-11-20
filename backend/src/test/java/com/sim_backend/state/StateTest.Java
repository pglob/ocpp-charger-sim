package com.sim_backend.state;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import com.sim_backend.state.*;

public class StateTest {

    private SimulatorStateMachine testStateMachine;

    @BeforeEach
    void setUp() {
        testStateMachine = new SimulatorStateMachine();     
    }

    @Test
    // Initialize test
    void testInit() throws Exception{
        try{
            assert SimulatorState.PowerOff == testStateMachine.getCurrentState() : "Initialize Failed";
        } catch (AssertionError e){
            System.err.println("Catch : " + e.getMessage());
        }
    }

    // Booting test Valid Situation
    void testBooting() throws Exception{
        try{
            testStateMachine.transition(SimulatorState.BootingUp);
        } catch (AssertionError e){
            System.err.println("Catch : " + e.getMessage());
        }
    }

}