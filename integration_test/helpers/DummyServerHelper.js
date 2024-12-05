const MESSAGES_URL = Cypress.env("DUMMY_URL") + "/messages";

// Wait until dummy server has received Websocket messages
const pollMessages = (timeout = 5000, interval = 1000) => {
  cy.waitUntil(
    () => {
      return cy.request("GET", MESSAGES_URL).then((response) => {
        return response.body.messages.length > 0;
      });
    },
    {
      timeout: timeout,
      interval: interval,
    },
  );
};

// Retrieve all Websocket messages sent to the dummy server
const fetchMessages = () => {
  return cy.request("GET", MESSAGES_URL).then((response) => {
    expect(response.status).to.eq(200);
    return response.body.messages;
  });
};

// Retrieve the last Websocket message sent to the dummy server
const getLastMessage = () => {
  return fetchMessages().then((messages) => {
    if (messages.length === 0) {
      throw new Error("No messages found");
    }
    return JSON.parse(messages.slice(-1)[0]);
  });
};

// Delete all WebSocket messages from the dummy server
const deleteMessages = () => {
  return cy.request("DELETE", MESSAGES_URL).then((response) => {
    expect(response.status).to.eq(200);
    expect(response.body.message).to.eq("All messages deleted successfully");
  });
};

export { pollMessages, fetchMessages, getLastMessage, deleteMessages };
