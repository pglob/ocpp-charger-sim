import "cypress-wait-until";
import { pollMessages, getNextMessage, deleteMessages, sendResponseFile } from "../../helpers/DummyServerHelper";
import { verifyTimestampField, verifyEnumField, verifyIntegerField, verifyCiStringField, verifyCallFields } from "../../helpers/AssertionHelper";

let INTERVAL = 5;

describe("OCPP boot sequence test", () => {
  beforeEach(() => {
    cy.wait(5000); // Wait for 5 seconds to allow everything to sync up
    deleteMessages();
    cy.task("log", "Click Reboot");
    cy.visit("/");
    cy.contains("Reboot").click();
  });

  const verifyBootNotificationPayload = (payload) => {
    verifyCiStringField(payload, "chargePointVendor", 20);
    verifyCiStringField(payload, "chargePointModel", 20);
    verifyCiStringField(payload, "chargePointSerialNumber", 25);
    verifyCiStringField(payload, "chargeBoxSerialNumber", 25);
    verifyCiStringField(payload, "firmwareVersion", 50);
    verifyCiStringField(payload, "iccid", 20);
    verifyCiStringField(payload, "imsi", 20);
    verifyCiStringField(payload, "meterType", 25);
    verifyCiStringField(payload, "meterSerialNumber", 25);
  };

  const verifyStatusNotificationPayload = (payload) => {
    verifyIntegerField(payload, "connectorId", 1);
    verifyEnumField(payload, "errorCode", "NoError");
    verifyEnumField(payload, "status", "Available");
    verifyTimestampField(payload, "timestamp", 60);
  };

  const verifyMessagesWithInterval = (messageType, iterations) => {
    let lastTimestamp = null;

    for (let i = 0; i < iterations; i++) {
      // Wait until the next message is received
      pollMessages(11000, 500);

      getNextMessage().then((messageArray) => {
        verifyCallFields(messageArray, messageType);

        if (messageType === "Heartbeat") {
          expect(messageArray[3]).to.deep.eq({}); // Heartbeat payload should be empty
        } else if (messageType === "BootNotification") {
          verifyBootNotificationPayload(messageArray[3]);
        }

        const currentTimestamp = Date.now();

        // If it's not the first message, check the interval
        if (lastTimestamp) {
          const interval = (currentTimestamp - lastTimestamp) / 1000; // in seconds
          expect(interval).to.be.closeTo(INTERVAL, 1); // Allow 1 second of tolerance
        }

        // Update the timestamp for the next iteration
        lastTimestamp = currentTimestamp;
      });
    }
  };


  
  it("verifies the BootNotification operation when accepted and heartbeat every 5 seconds", () => {
    // Send the accepted response file to the server
    sendResponseFile("tests/bootSequence/ResponseAccepted.json");

    // Wait until BootNotification message is received
    pollMessages(20000, 100);

    // Verify the BootNotification message
    getNextMessage().then((messageArray) => {
      verifyCallFields(messageArray, "BootNotification");
      verifyBootNotificationPayload(messageArray[3]);
    });

    // Wait until the StatusNotification message is received
    pollMessages(10000, 100);

    // Verify the StatusNotification message
    getNextMessage().then((messageArray) => {
      verifyCallFields(messageArray, "StatusNotification");
      verifyStatusNotificationPayload(messageArray[3]);
    });

    // Check Heartbeat messages with 5-second intervals
    verifyMessagesWithInterval("Heartbeat", 5);
  });



  it("verifies the BootNotification operation when rejected", () => {
    // Send the rejected response file to the server
    sendResponseFile("tests/bootSequence/ResponseRejected.json");

    // Wait until the BootNotification message is received
    pollMessages(20000, 100);

    // Verify the BootNotification message
    getNextMessage().then((messageArray) => {
      verifyCallFields(messageArray, "BootNotification");
      verifyBootNotificationPayload(messageArray[3]);
    });

    // Check BootNotification messages with 5-second retry times
    verifyMessagesWithInterval("BootNotification", 3);

    // Wait until the StatusNotification message is received
    pollMessages(10000, 100);

    // Verify the StatusNotification message
    getNextMessage().then((messageArray) => {
      verifyCallFields(messageArray, "StatusNotification");
      verifyStatusNotificationPayload(messageArray[3]);
    });

    // Check Heartbeat messages with 10-second intervals
    verifyMessagesWithInterval("Heartbeat", 3);
  });
});
