# HTTP API Documentation

This document describes the purpose and functionality of each HTTP endpoint in the `MessageController`.

---

### POST `/api/{chargerId}/message/authorize`

- **Functionality**: Sends an authorization request. This endpoint constructs an `Authorize` message and pushes it to the backend via the WebSocket client.
- **Response**: Returns `"OK"` on success.

### POST `/api/{chargerId}/message/boot`

- **Functionality**: Sends a boot notification. Constructs a `BootNotification` message and pushes it to the backend via the WebSocket client.
- **Response**: Returns `"OK"` on success.

### POST `/api/{chargerId}/message/heartbeat`

- **Functionality**: Sends a heartbeat message. Constructs a `Heartbeat` message to indicate that the charging station is still online, and pushes it to the backend via the WebSocket client.
- **Response**: Returns `"OK"` on success.

### GET `/api/{chargerId}/state`

- **Functionality**: Retrieves the current state of the charging station. The endpoint extracts the current state from the state machine.
- **Response**: Returns the current state as a string.

### POST `/api/{chargerId}/state/status`

- **Functionality**: Sends a status notification. The request body must be in JSON format and include the connector ID and error code.
- **Response**: Returns `"OK"` on success; if fields are missing or invalid, a 400 status code and an error message are returned.

### POST `/api/{chargerId}/state/online`

- **Functionality**: Sets the charging station online.
- **Response**: Returns `"OK"` on success.

### POST `/api/{chargerId}/state/offline`

- **Functionality**: Sets the charging station offline.
- **Response**: Returns `"OK"` on success.

### POST `/api/{chargerId}/charger/reboot`

- **Functionality**: Reboots the charging station.
- **Response**: Returns `"OK"` on success; if a reboot is already in progress, returns a 503 status code with an error message.

### POST `/api/{chargerId}/charger/clear-fault`

- **Functionality**: Clears the fault status of the charging station.
- **Response**: Returns `"OK"` on success; returns a 500 status code on failure.

### GET `/api/{chargerId}/log/sentmessage`

- **Functionality**: Retrieves the log of messages sent by the charging station.
- **Response**: Returns a JSON array of sent messages.

### GET `/api/{chargerId}/log/receivedmessage`

- **Functionality**: Retrieves the log of messages received by the charging station.
- **Response**: Returns a JSON array of received messages.

### POST `/api/{chargerId}/transaction/start-charge`

- **Functionality**: Initiates a charging transaction. Calls the `startCharging()` method of the transaction handler using the `idTag` from the charging station's configuration and defaults to connector 1.
- **Response**: Returns `"OK"` on success.

### POST `/api/{chargerId}/transaction/stop-charge`

- **Functionality**: Stops the charging transaction. Calls the `stopCharging()` method of the transaction handler using the `idTag` from the charging station's configuration.
- **Response**: Returns `"OK"` on success.

### GET `/api/{chargerId}/electrical/meter-value`

- **Functionality**: Retrieves the meter reading. Returns the value of `EnergyActiveImportRegister` from the electrical component, formatted to 4 significant digits.
- **Response**: Returns the reading as a string.

### GET `/api/{chargerId}/electrical/max-current`

- **Functionality**: Retrieves the maximum allowed current of the charging station. Returns the maximum current value from the electrical component.
- **Response**: Returns the value as a string.

### GET `/api/{chargerId}/electrical/current-import`

- **Functionality**: Retrieves the current import value. Returns the current value from the electrical component.
- **Response**: Returns the value as a string.

### GET `/api/{chargerId}/get-idtag-csurl`

- **Functionality**: Retrieves the idTag and Central System Url parameters of the charging station.
- **Response**: Returns a JSON-formatted string.

### POST `/api/{chargerId}/update-idtag-csurl`

- **Functionality**: Updates the idTag and Central System Url parameters of the charging station.
- **Response**: Returns a success message on success.
