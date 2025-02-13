const { startHttpServer } = require("./ConfigurationHttpServer");
const { startWebSocketServer } = require("./WebSocketServer");

const httpPort = 9001;
const wsPort = 9000;

const { server: httpServer, receivedMessages } = startHttpServer(httpPort);
const webSocketServer = startWebSocketServer(wsPort, receivedMessages);

// Graceful shutdown
const shutdown = () => {
  console.log("Shutting down...");

  // Close WebSocket server
  webSocketServer.close(() => {
    console.log("WebSocket server closed");
  });

  // Close HTTP server
  httpServer.close(() => {
    console.log("HTTP server closed");
    process.exit(0);
  });
};

process.on("SIGINT", shutdown); // Handle Ctrl+C
process.on("SIGTERM", shutdown); // Handle termination signal
