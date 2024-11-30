# Integration-Test Dockerfile

# Use a Cypress image with browsers included
FROM cypress/included:12.15.0

# Set the working directory
WORKDIR /app

# Install dependencies
COPY package.json ./
RUN npm install

# Copy test code into the container
COPY . .

# Run Cypress tests
CMD ["npm", "test"]
