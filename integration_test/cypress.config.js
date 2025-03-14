const { defineConfig } = require("cypress");
const path = require("path");
const fs = require('fs');

module.exports = defineConfig({
  e2e: {
    specPattern: "integration_test/tests/**/*.cy.js",
    baseUrl: "http://frontend:3030",
    supportFile: false,
    env: {
      DUMMY_URL: "http://dummy-server:9001/api",
    },
    setupNodeEvents(on, config) {
      on('task', {
        readFile(filePath) {
          const fullFilePath = path.resolve(filePath);  // Get the absolute path
          const content = fs.readFileSync(fullFilePath, 'utf8');  // Read file content
          return content;
        },

        log(message) {
          console.log(message);
          return null;
        },
      });

      return config;
    },
  },
});
