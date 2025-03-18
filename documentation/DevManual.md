# Overview

Many of the components below use Lombok to auto-generate getter and setter functions for their class variables. A class’s constructor can retrieve references to needed components using these getters.

## Charger
### backend/src/main/java/com/sim_backend/charger/*
The `Charger` is the top-level component for the simulated OCPP EV charger. It uses a `ChargerStateMachine` to manage charger statuses (e.g., `BootingUp`, `Available`, `Faulted`) and an `ElectricalTransition` object to track and control charging parameters. Communication with the central system is done through an `OCPPWebSocketClient`, while a `TransactionHandler` manages charging sessions. The `ConfigurationRegistry` tracks non-OCPP and OCPP configurations.

The `Charger` uses observers for some OCPP operations.

The main processing loop of the `Charger` runs in the `ChargerLoop` thread. This loop is responsible for checking scheduled messages and sending messages in the queue.

On a reboot, any active charging session is terminated by the `TransactionHandler`, and the `ChargerStateMachine` transitions to the `PoweredOff` state. The `ChargerLoop` is then signaled to stop, and its thread is interrupted and joined. The `OCPPWebSocketClient` is closed, and after a brief delay, the boot sequence is re-executed, reinitializing all components and observers. Old components and observers are garbage collected to ensure no stale references remain between terminated and newly initialized components.

The components below are owned by a single `Charger` instance. `Charger` instances are created and booted in **backend/src/main/java/com/sim_backend/Main.java**.

## State Machine
### backend/src/main/java/com/sim_backend/state/*
The `ChargerStateMachine` is responsible for managing the state of the simulated charger. It maintains a `currentState` (such as `PoweredOff`, `BootingUp`, `Available`, `Preparing`, `Charging`, `Faulted`, or `Unavailable`) and enforces valid state transitions through a map. 

State changes can be performed atomically using the `checkAndTransition` method, or directly via the `transition` method, which throws an exception if the requested transition is not allowed.

A class implementing the `StateObserver` interface can register for any state change, through the `onStateChanged` method. 

## Transaction Handler
### backend/src/main/java/com/sim_backend/transactions/*
The `TransactionHandler` manages charging sessions for the simulated charger. It coordinates start and stop operations using two handlers, `StartTransactionHandler` for initiating a start transaction and `StopTransactionHandler` for initiating a stop transaction.

When starting a charge, there is a transition from `Available` to `Preparing`. Then, an `Authorize` request is sent. If the authorization is accepted, the `idTag` stored in `TransactionHandler` is updated and a `StartTransaction` is sent. If the `StartTransaction` is accepted, there is a transition into `Charging`, and a `MeterValues` is sent.

When stopping a charge, an `Authorize` request is sent if the input `idTag` differs from the current `idTag`. If the authorization is accepted or the `idTag` matches, a `StopTransaction` and `MeterValues` are sent, and there is a transition from `Charging` to `Available`. 

The `transactionId` in `TransactionHandler` will equal -1 if no transaction is in progress.

## Configuration Registry
### backend/src/main/java/com/sim_backend/config/*
The `ConfigurationRegistry` stores the configuration and identification details for the simulated charge point. It stores properties such as the authorization `idTag`, the `centralSystemUrl` for connecting to the central system, and other hardware identifiers (vendor, model, serial numbers).

It also stores OCPP-specific configurations.

## Electrical
### backend/src/main/java/com/sim_backend/electrical/*
The `ElectricalTransition` class tracks the electrical state during a charging session. It tracks parameters such as voltage, current, power, and energy consumption. It is considered "On" when the state is `Charging`. It starts a new session by setting the nominal voltage and timestamp, and ending a session by accumulating the energy consumed into a lifetime total. The `ElectricalTransition` object assumes a 240V, split-phase connection.

The `ChargingProfileHandler` manages the charging profiles that determine the current limit during a transaction.

It is important to note that electrical values are not calculated proactively, they are only calculated when retrieved. The Frontend makes regular calls (every 5 seconds) to `currentImport`, giving charging profiles a resolution of about 5 seconds.

## Observers
### backend/src/main/java/com/sim_backend/websockets/observers/*
The observers in this folder are generally simple and do not maintain much state.

Some notes:
- `ChangeAvailabilityObserver` tracks the `Available`/`Unavailable` state through reboots.
- `MeterValuesObserver` has the `sendMeterValues` method which can be called elsewhere as needed.
- `StatusNotificationObserver` will send a `StatusNotification` on every transition into a valid OCPP state. It also has a `sendStatusNotification` method which can be called elsewhere as needed.

## OCPP WebSocket Client
### backend/src/main/java/com/sim_backend/websockets/OCPPWebSocketClient.java
The `OCPPWebSocketClient` is an extension of Java's WebSocket client designed specifically for handling OCPP communication.

`OCPPWebSocketClient` contains a `MessageQueue` and a `MessageScheduler` to manage the timing and ordering of messages. The `MessageQueue` supports both normal and priority message addition. The `MessageScheduler` is responsible for tasks like heartbeat management and time synchronization with the Central System.

Message parsing is handled using Gson, which converts JSON messages into structured data. The client differentiates between OCPP requests, responses, and errors by call IDs.

The `OCPPWebSocketClient` supports TLS connections. When the connection URI uses the "wss" scheme, it sets up an SSL context with a custom socket factory to ensure that the SNI property is set.

The client also maintains maps of listeners for both received and pushed messages.

When errors are detected, the `WebSocketClient` can send CallErrors in response to bad Call messages, or notify registered listeners in response to a CallError from the Central System.

The `OCPPWebSocketClient` can be turned "Offline" if desired. This stops messages from sending, stops WebSocket ping pong messages, and drops received messages.

## Message Queue
### backend/src/main/java/com/sim_backend/websockets/MessageQueue.java
The `MessageQueue` manages the queuing and tracking of OCPP messages to be sent over the WebSocket connection. It removes duplicate messages and supports both standard and priority message addition.

Call messages are not sent unless a CallResult has been received for the previous Call.

Messages are wrapped as timed objects so that the queue can detect timeouts.

## Message Scheduler
### backend/src/main/java/com/sim_backend/websockets/MessageScheduler.java
The `MessageScheduler` maintains a synchronized time using an `OCPPTime` object and schedules tasks to be executed through the `OCPPWebSocketClient`. It synchronizes its clock with the Central System via the `synchronizeTime` method.

The `MessageScheduler` allows for the management of jobs (either the sending of an `OCPPMessage` or generic `Runnable`s).



# New Development

### Implementing a new OCPP operation
Adding a new OCPP operation will require registering an observer with the `OCPPWebSocketClient`. An observer can register for a specific message type with the `onReceiveMessage` method. Message classes are stored in **backend/src/main/java/com/sim_backend/websockets/messages**.
An observer can unregister with the `deleteOnReceiveMessage` method and can add timeout handling with the `onTimeout` method.

An observer can also register for state changes with the `ChargerStateMachine` through the `onStateChanged` method.

Observers should be instantiated in the `boot` method of `Charger`. If they require a class-wide reference in the `Charger` object, ensure they are set to null in the `reboot` method.

It is important that an observer stores references to `Charger` components in their constructor, instead of retrieving them as needed with getters. This will prevent any threading issues when rebooting.

### Adding a new OCPP configuration
To add a new OCPP configuration, it needs to be declared in `ConfigurationRegistry`. It also needs cases added in `GetConfigurationObserver` and `ChangeConfigurationObserver`. 

If the configuration should be loadable from the command line, the property needs to be added in the `loadConfiguration` method in `ConfigurationRegistry`. It also needs to be passed in through **backend/Dockerfile** and **docker-compose.yml** 

### Adding a new OCPP state
To add a new OCPP state, it must be declared in `ChargerState` (**backend/src/main/java/com/sim_backend/state/ChargerState.java**) and the transition map, `validTransitions`, in **backend/src/main/java/com/sim_backend/state/ChargerStateMachine.java** must be updated. 

# Testing

## Unit
Frontend unit tests use Jest and are stored in **frontend/src/__test__**.

Backend unit tests use JUnit and Mockito and are stored in **backend/src/test**. When writing new backend unit tests, use `TestOCPPWebSocketClient` to prevent `connectBlocking` and `reconnectBlocking` calls from eating test time.

## Integration
Integration tests use Cypress, a headless Electron browser, and a dummy server to verify the messages sent by the charger.

Only 1 charger is currently enabled during testing.

### Dummy Server
The dummy server located in **dummy_server** is an HTTP (port 9000) and WebSocket server (port 9001).

The HTTP server (**dummy_server/ConfigurationHttpServer.js**) has endpoints that allow for the retrieval of messages that the WebSocket server receives, and configure the response payloads of the WebSocket server.

The WebSocket server sends OCPP CallResult messages back to the charger. When the `NORMAL_MODE` environment variable is set to anything, it will use responses defined in **dummy_server/NormalModeResponses.json**. Any `currentTime` field in a message gets its value set to the current time. When `NORMAL_MODE` is unset, responses must be configured by the `api/uploadResponse` HTTP endpoint.

This is an example of an uploaded response file:
> { \
    "a1":{"status":"Accepted", "currentTime":"X", "interval":5}, \
    "b1":{}, \
    "c5":{"currentTime":"X"} \
}

These files must be in valid JSON. Responses are sent sequentially. The above file would send a CallResult message with the 1st payload (value of a1) in response to the 1st message. The 2nd message would receive a response with an empty payload (value in b1). The next 5 messages would receive a payload containing the current time (value in c5).

### Integration tests
The AssertionHelper.js file (**integration_test/helpers/AssertionHelper.js**) has various functions used for verifying OCPP message contents. The DummyServerHelper.js file (**integration_test/helpers/DummyServerHelper.js**)  has functions for interacting with the dummy server.

Test logging can be done through a Cypress task:
```
    cy.task("log", "<Log message>");
```
