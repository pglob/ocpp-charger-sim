import { pollMessages, getNextMessage } from "../../helpers/DummyServerHelper";

// Helper function to get and verify a messsages contents
function validateMessage(messageType, verifyFn, verifyArgs = [], timeout = 10000, interval = 100) {
  pollMessages(timeout, interval);
  return getNextMessage().then((messageArray) => {
    verifyCallFields(messageArray, messageType);
    verifyFn(messageArray[3], ...verifyArgs);
  });
}

// Helper function to verify fields with CiString types
function verifyCiStringField(payload, fieldName, maxLength, optional = false) {
  if (!optional) {
    expect(payload).to.have.property(fieldName); // Fail if field is missing
  }
  if (payload[fieldName] !== undefined) {
    expect(payload[fieldName]).to.be.a("string");
    expect(payload[fieldName]).to.have.length.within(1, maxLength);
  }
}

// Helper function to verify fields with integer types
function verifyIntegerField(payload, fieldName, expectedValue, optional = false, customCompareFn = null) {
  if (!optional) {
    expect(payload).to.have.property(fieldName); // Fail if field is missing
  }
  
  if (payload[fieldName] !== undefined) {
    expect(payload[fieldName]).to.be.a("number");
    expect(Number.isInteger(payload[fieldName])).to.be.true;
    
    if (customCompareFn != null) {
      expect(customCompareFn(payload[fieldName])).to.be.true;
    } else {
      expect(payload[fieldName]).to.equal(expectedValue);
    }
  }
}

// Helper function to verify timestamp fields
function verifyTimestampField(payload, fieldName, allowedDiffSeconds, optional = false, customCompareFn = null) {
  if (!optional) {
    expect(payload).to.have.property(fieldName); // Fail if field is missing
  }

  if (payload[fieldName] !== undefined) {
    const timestamp = payload[fieldName];

    // Verify the format using a regular expression
    const isoRegex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$/;
    expect(timestamp).to.match(isoRegex);

    // Convert the timestamp to a Date object and check validity
    const date = new Date(timestamp);
    expect(date.toString()).to.not.equal("Invalid Date");

    // Compare the timestamp against the current time
    const now = new Date();
    const diffSeconds = Math.abs(now.getTime() - date.getTime()) / 1000;
    expect(diffSeconds).to.be.at.most(allowedDiffSeconds);

    if (customCompareFn != null) {
      expect(customCompareFn(date)).to.be.true;
    }
  }
}

// Helper function to verify other fields in Calls (MessageTypeId, UniqueId, Action)
function verifyCallFields(messageArray, action) {
  expect(messageArray[0]).to.eq(2);
  expect(messageArray[1]).to.have.length.within(1, 36);
  expect(messageArray[2]).to.eq(action);
}

export { validateMessage, verifyCiStringField, verifyIntegerField, verifyTimestampField, verifyCallFields };
