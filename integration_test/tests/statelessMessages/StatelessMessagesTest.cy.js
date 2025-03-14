// TODO:
// This test is commented out as breaking changes were made in the backend.
// This feature may be modified in the future, so this test may be removed.

/*
import "cypress-wait-until";
import { pollMessages, getNextMessage, deleteMessages } from "../../helpers/DummyServerHelper";
import { verifyCiStringField, verifyCallFields, } from "../../helpers/AssertionHelper";

describe("Stateless OCPP message test", () => {
  beforeEach(() => {
    // Navigate to Send Messages dropdown
    cy.visit("/");
    cy.contains("Send Messages").click();

    deleteMessages();
    cy.wait(2000); // Wait for 2 seconds to allow everything to sync up
  });

  it("clicks the Heartbeat button and verifies the sent OCPP message", () => {
    // Click the Heartbeat button
    cy.contains("Heartbeat").click();

    // Wait until messages are received
    pollMessages(5000, 1000);

    // Verify the HeartBeat message
    getNextMessage().then((messageArray) => {
      verifyCallFields(messageArray, "Heartbeat");
      expect(messageArray[3]).to.deep.eq({}); // Verify empty payload
    });
  });

  it("clicks the Authorize button and verifies the sent OCPP message", () => {
    // Click the Authorize button
    cy.contains("Authorize").click();

    // Wait until messages are received
    pollMessages(5000, 1000);

    // Verify the Authorize message
    getNextMessage().then((messageArray) => {
      verifyCallFields(messageArray, "Authorize");

      const payload = messageArray[3];
      verifyCiStringField(payload, "idTag", 20);
    });
  });

  it("clicks the Boot button and verifies the sent OCPP message", () => {
    // Click the Boot button
    cy.contains("Boot").click();

    // Wait until messages are received
    pollMessages(5000, 1000);

    // Verify the BootNotification message
    getNextMessage().then((messageArray) => {
      verifyCallFields(messageArray, "BootNotification");

      const payload = messageArray[3];
      verifyCiStringField(payload, "chargePointVendor", 20);
      verifyCiStringField(payload, "chargePointModel", 20);
      verifyCiStringField(payload, "chargePointSerialNumber", 25);
      verifyCiStringField(payload, "chargeBoxSerialNumber", 25);
      verifyCiStringField(payload, "firmwareVersion", 50);
      verifyCiStringField(payload, "iccid", 20);
      verifyCiStringField(payload, "imsi", 20);
      verifyCiStringField(payload, "meterType", 25);
      verifyCiStringField(payload, "meterSerialNumber", 25);
    });
  });
});
*/