const express = require("express");
const { WebSocketServer } = require("ws");

const app = express();
app.use(express.json());

const receivedMessages = [];

// WebSocket server
const wss = new WebSocketServer({
  port: 9000,
  handleProtocols: (protocols, request) => {
    if (protocols.includes("ocpp1.6")) {
      return "ocpp1.6";
    }

    console.error("No supported subprotocol found");
    return false;
  },
});

wss.on("connection", (ws) => {
  console.log("WebSocket connection established");

  ws.on("message", (message) => {
    console.log("Message received:", message.toString());
    receivedMessages.push(message.toString());
  });

  ws.on("close", () => {
    console.log("WebSocket connection closed");
  });
});

// HTTP API to retrieve received WebSocket messages
app.get("/api/messages", (req, res) => {
  res.json({ messages: receivedMessages });
});

// HTTP API to delete all received WebSocket messages
app.delete("/api/messages", (req, res) => {
  receivedMessages.length = 0;
  res.json({ message: "All messages deleted successfully" });
});

// Start the HTTP server
const port = 9001;
const server = app.listen(port, () => {
  console.log(`HTTP server running on port ${port}`);
});

// Shutdown manually as it is much faster
const shutdown = () => {
  console.log("Shutting down...");

  // Close WebSocket server
  wss.close(() => {
    console.log("WebSocket server closed");
  });

  // Close HTTP server
  server.close(() => {
    console.log("HTTP server closed");
    process.exit(0);
  });
};

process.on("SIGINT", shutdown); // Handle Ctrl+C
process.on("SIGTERM", shutdown); // Handle termination signal
