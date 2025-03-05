const { WebSocketServer } = require("ws");
const globals = require('./Global');
const fs = require("fs");

const startWebSocketServer = (port = 9000, receivedMessages) => {
  // Load predefined responses if its environment variable is set
  let normalModeResponses = null;
  if (process.env.NORMAL_MODE) {
    try {
      const filePath = "NormalModeResponses.json";
      normalModeResponses = JSON.parse(fs.readFileSync(filePath, "utf8"));
      console.log("Normal mode responses loaded");
    } catch (err) {
      console.error("Failed to load NormalModeResponses.json:", err);
    }
  }

  const wss = new WebSocketServer({
    port: port,
    // Only allow connections whose request URL is '/test'
    verifyClient: (info, done) => {
      if (info.req.url === "/test") {
        done(true);
      } else {
        done(false, 403, "Forbidden");
      }
    },
    handleProtocols: (protocols, request) => {
      if (protocols.has("ocpp1.6")) {
        return "ocpp1.6";
      }
  
      console.error("No supported subprotocol found");
      return false;
    },
  });

  wss.on("connection", (ws) => {
    console.log("WebSocket connection established");

    ws.on('message', async (message) => {
      console.log('Message received:', message.toString());
      const parsedMessage = JSON.parse(message.toString());
      receivedMessages.push(parsedMessage);
    
      const messageId = parsedMessage[1];
      const action = parsedMessage[2];
      let responseData;

      // Helper function for adding a delay
      const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

      // If we have loaded normal mode responses and there's a match, use it
      if (normalModeResponses && normalModeResponses[action]) {
        await delay(1000);  // Delay for 1 second to simulate network latency
        responseData = normalModeResponses[action];
      } else {
        responseData = globals.shiftResponseData();
      }
    
      if (responseData) {
        const updatedResponseData = { ...responseData };
    
        if ('currentTime' in updatedResponseData) {
          // Set the current time in UTC format
          updatedResponseData.currentTime = new Date().toISOString();
        }
    
        // Construct the response message
        const responseMessage = [
          3, // Message type for response
          messageId,
          updatedResponseData,
        ];
    
        // Send the response back
        ws.send(JSON.stringify(responseMessage));
        console.log("Response sent:", JSON.stringify(responseMessage, null, 2));
      }
    });
    
    ws.on("close", () => {
      console.log("WebSocket connection closed");
    });
  });

  console.log(`WebSocket server running on port ${port}`);
  return wss;
};

module.exports = { startWebSocketServer };
