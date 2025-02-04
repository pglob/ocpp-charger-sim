import "cypress-wait-until";
import { pollMessages, getNextMessage, deleteMessages, sendResponseFile } from "../../helpers/DummyServerHelper";
import { validateMessage, verifyCiStringField, verifyIntegerField, verifyTimestampField } from "../../helpers/AssertionHelper";

describe("OCPP charging test", () => {
  beforeEach(() => {
    cy.wait(5000); // Wait for 5 seconds to allow everything to sync up
    deleteMessages();
    cy.task("log", "Click Reboot");
    cy.visit("/");
    cy.contains("Reboot").click();
  });

  const verifyAuthorizePayload = (payload) => {
    verifyCiStringField(payload, "idTag", 20);
  };

  const verifyStartTransactionPayload = (payload, meterStopMin, meterStopMax) => {
    verifyIntegerField(payload, "connectorId", 1);
    verifyCiStringField(payload, "idTag", 20);
    verifyIntegerField(payload, "meterStart", 0, true, (value) => meterStopMin <= value && value <= meterStopMax);
    verifyTimestampField(payload, "timestamp", 60);
  };

  const verifyStopTransactionPayload = (payload, meterStopMin, meterStopMax) => {
    verifyIntegerField(payload, "transactionId", 5);
    verifyCiStringField(payload, "idTag", 20);
    verifyIntegerField(payload, "meterStop", 0, true, (value) => meterStopMin <= value && value <= meterStopMax);
    verifyTimestampField(payload, "timestamp", 60);
  };

  const verifyStopTransactionNoIdTagPayload = (payload, meterStopMin, meterStopMax) => {
    verifyIntegerField(payload, "transactionId", 5);
    expect(() => { verifyCiStringField(payload, "idTag", "", 0); }).to.throw();
    verifyIntegerField(payload, "meterStop", 0, true, (value) => meterStopMin <= value && value <= meterStopMax);
    verifyTimestampField(payload, "timestamp", 60);
  };  



  it("verifies the StartTransaction and StopTransaction operations", () => {
    // Send the accepted response file to the server
    sendResponseFile("tests/charging/AllAccepted.json");

    cy.task("log", "Wait for BootNotification and Heartbeat messages and discard them");
    pollMessages(20000, 100);
    getNextMessage();
    pollMessages(10000, 100);
    getNextMessage();

    cy.task("log", "Verify sequence when the all responses return \"Accepted\"");
    cy.contains("Start Charging").click();
    validateMessage("Authorize", verifyAuthorizePayload);
    validateMessage("StartTransaction", verifyStartTransactionPayload, [0, 0]);
    cy.contains("Stop Charging").click();
    validateMessage("StopTransaction", verifyStopTransactionPayload, [1, 10]);
  });



  it("verifies the StartTransaction and StopTransaction operations when idTags are invalid", () => {
    // Load the rejected response file
    sendResponseFile("tests/charging/Rejected.json");
  
    cy.task("log", "Wait for BootNotification and Heartbeat messages and discard them");
    pollMessages(20000, 100);
    getNextMessage();
    pollMessages(10000, 100);
    getNextMessage();
  
    cy.task("log", "Verify sequence when Authorize returns \"Invalid\"");
    cy.contains("Start Charging").click();
    validateMessage("Authorize", verifyAuthorizePayload);
    
    cy.task("log", "Verify sequence when StartTransaction returns \"Invalid\"");
    cy.contains("Start Charging").click();
    validateMessage("Authorize", verifyAuthorizePayload);
    validateMessage("StartTransaction", verifyStartTransactionPayload, [0, 0]);

    cy.task("log", "Verify sequence when StopTransaction returns \"Invalid\"");
    cy.contains("Start Charging").click();
    validateMessage("Authorize", verifyAuthorizePayload);
    validateMessage("StartTransaction", verifyStartTransactionPayload, [0, 0]);
    cy.wait(1000); // Wait for 1 second to allow charging to occur
    cy.contains("Stop Charging").click();
    validateMessage("StopTransaction", verifyStopTransactionPayload, [1, 10]);
  
    cy.task("log", "Verify sequence when the all responses return \"Accepted\"");
    cy.contains("Start Charging").click();
    validateMessage("Authorize", verifyAuthorizePayload);
    validateMessage("StartTransaction", verifyStartTransactionPayload, [1, 10]);
    cy.wait(1000); // Wait for 1 second to allow charging to occur
    cy.contains("Stop Charging").click();
    validateMessage("StopTransaction", verifyStopTransactionPayload, [2, 10]);
  });
  


  it("verifies the StartTransaction and StopTransaction operations when a Reboot occurs", () => {
    // Load the rejected response file
    sendResponseFile("tests/charging/RebootDuringCharge.json");
  
    cy.task("log", "Wait for BootNotification and Heartbeat messages and discard them");
    pollMessages(20000, 100);
    getNextMessage();
    pollMessages(10000, 100);
    getNextMessage();

    cy.task("log", "Verify sequence when a Reboot occurs after StartTransaction");
    cy.contains("Start Charging").click();
    validateMessage("Authorize", verifyAuthorizePayload);
    validateMessage("StartTransaction", verifyStartTransactionPayload, [0, 0]);
    cy.wait(1000); // Wait for 1 second to allow charging to occur

    cy.task("log", "Click Reboot");
    cy.contains("Reboot").click();
    validateMessage("StopTransaction", verifyStopTransactionNoIdTagPayload, [1, 10]);

    cy.task("log", "Wait for BootNotification and Heartbeat messages and discard them");
    pollMessages(20000, 100);
    getNextMessage();
    pollMessages(10000, 100);
    getNextMessage();

    cy.task("log", "Verify sequence is normal after a reboot and meter value resets");
    cy.contains("Start Charging").click();
    validateMessage("Authorize", verifyAuthorizePayload);
    validateMessage("StartTransaction", verifyStartTransactionPayload, [0, 10]);
    cy.wait(1000); // Wait for 1 second to allow charging to occur
    cy.contains("Stop Charging").click();
    validateMessage("StopTransaction", verifyStopTransactionPayload, [1, 10]);
  });
});
