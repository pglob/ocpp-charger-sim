# Integration-Test Dockerfile

# Use the official Cypress image with Node.js
FROM cypress/included:12.15.0

# Set the working directory
WORKDIR /app

# Copy package.json and install dependencies (if you have additional dependencies)
COPY package.json ./
RUN npm install

# Copy test code into the container
COPY . .

# Run Cypress tests
CMD ["npm", "test"]
