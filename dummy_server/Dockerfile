# Dummy-Server Dockerfile

FROM node:18

# Set the working directory
WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm install

# Copy the server code
COPY . .

# Expose the WebSocket and HTTP ports
EXPOSE 9000 9001

# Start the server
CMD ["node", "DummyServer.js"]
