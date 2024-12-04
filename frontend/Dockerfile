# Frontend Dockerfile

# Use the official Node.js image
FROM node:18 AS build

# Set the working directory
WORKDIR /app

# Copy package.json and install dependencies
COPY package.json ./
RUN npm install

# Set CI environment variable to ensure Jest runs in non-interactive mode
ENV CI=true

# Copy the rest of the application source code
COPY . .

# Run the tests
FROM build AS test

RUN npm test

# === Run Stage ===
FROM node:18 AS run

# Set the working directory
WORKDIR /app

# Copy built files from the build stage
COPY --from=build /app .

# Set the PORT environment variable
ENV PORT=${FRONTEND_PORT}

# Expose the application port
EXPOSE ${FRONTEND_PORT}

# Start the application
CMD ["npm", "start"]

