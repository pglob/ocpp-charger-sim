# Frontend Dockerfile

# Use the official Node.js image
FROM node:18

# Set the working directory
WORKDIR /app

# Copy package.json and install dependencies
COPY package.json ./
RUN npm install

# Copy the rest of the application source code
COPY . .

# Set the PORT environment variable
ENV PORT=${FRONTEND_PORT}

# Expose the application port
EXPOSE ${FRONTEND_PORT}

# Start the application
CMD ["npm", "start"]
