const express = require("express");
const globals = require('./Global');
const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

const receivedMessages = [];
let responseData = null;  // Store the response content

// Endpoint to retrieve the number of received WebSocket messages
app.get("/api/messageLength", (req, res) => {
  res.json({ message: receivedMessages.length });
});

// Endpoint to retrieve next received WebSocket message
app.get("/api/messages", (req, res) => {
  res.json({ message: receivedMessages.shift() });
});

// Endpoint to delete all received WebSocket messages
app.delete("/api/messages", (req, res) => {
  receivedMessages.length = 0;
  res.json({ message: "All messages deleted successfully" });
});

// Endpoint to upload a response file content
app.post("/api/uploadResponse", (req, res) => {
  const responseData = req.body;

  if (!responseData) {
    return res.status(400).json({ error: "No response data received" });
  }

  /* Given the following example input:
  {
    "a3":{"key":"value",...},
    "b4":{"key":"value",...}...
  }

  Generate the following response list:
  [3 * {"key":"value",...}, 4 * {"key":"value",...}, ...]
  */
  Object.entries(responseData).forEach(([key, response]) => {
    const count = parseInt(key.replace(/^\D+/, ""), 10); // Parse out the number in the key
    for (let i = 0; i < count; i++) {
      globals.addResponseData(response);
    }
  });

  console.log("Configured response(s):", globals.getResponseData());

  res.json({ message: "Response file content stored successfully" });
});

// Endpoint to reset response data to null
app.post("/api/resetResponseData", (req, res) => {
  globals.setResponseData([]);
  
  console.log("Response data has been reset to null");
  res.json({ message: "Response data has been reset to null" });
});

// Start the HTTP server
const startHttpServer = (port = 9001) => {
  const server = app.listen(port, () => {
    console.log(`HTTP server running on port ${port}`);
  });

  return { server, receivedMessages, responseData };
};

module.exports = { startHttpServer, receivedMessages };
