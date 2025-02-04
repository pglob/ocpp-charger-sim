const MESSAGE_LENGTH_URL = Cypress.env("DUMMY_URL") + "/messageLength";
const MESSAGES_URL = Cypress.env("DUMMY_URL") + "/messages";
const UPLOAD_RESPONSE_URL = Cypress.env("DUMMY_URL") + "/uploadResponse";
const path = require("path");
const fs = require('fs');

// Wait until dummy server has received WebSocket messages
const pollMessages = (timeout = 5000, interval = 1000) => {
  cy.waitUntil(
    () => {
      return cy.request("GET", MESSAGE_LENGTH_URL).then((response) => {
        return parseInt(response.body.message) > 0;
      });
    },
    {
      timeout: timeout,
      interval: interval,
    }
  );
};

// Retrieve all WebSocket messages sent to the dummy server
const fetchMessages = () => {
  return cy.request("GET", MESSAGES_URL).then((response) => {
    expect(response.status).to.eq(200);
    return response.body.message;
  });
};

// Retrieve the next WebSocket message sent to the dummy server
const getNextMessage = () => {
  return fetchMessages().then((message) => {
    if (message.length === 0) {
      throw new Error("No messages found");
    }
    return message;
  });
};

// Delete all WebSocket messages from the dummy server
const deleteMessages = () => {
  return cy.request("DELETE", MESSAGES_URL).then((response) => {
    expect(response.status).to.eq(200);
    expect(response.body.message).to.eq("All messages deleted successfully");
  });
};

// Send a response file to the server
const sendResponseFile = (relativeFilePath) => {
  return cy.task('readFile', relativeFilePath).then((fileContent) => {

    return cy.request({
      method: "POST",
      url: UPLOAD_RESPONSE_URL,
      body: JSON.parse(fileContent),  // Send the parsed JSON content
      failOnStatusCode: false,
      headers: {
        "Content-Type": "application/json",
      },
    }).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body.message).to.eq("Response file content stored successfully");
    });
  });
};

export { pollMessages, fetchMessages, getNextMessage, deleteMessages, sendResponseFile };
