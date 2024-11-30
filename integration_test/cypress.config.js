const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2e: {
    specPattern: "integration_test/tests/**/*.cy.js",
    baseUrl: "http://frontend:3030",
    supportFile: false,
    env: {
      DUMMY_URL: "http://dummy-server:9001/api",
    },
  },
});
