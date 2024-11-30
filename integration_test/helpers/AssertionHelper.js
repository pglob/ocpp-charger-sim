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

// Helper function to verify other fields in Calls (MessageTypeId, UniqueId, Action)
function verifyCallFields(messageArray, action) {
  expect(messageArray[0]).to.eq(2);
  expect(messageArray[1]).to.have.length.within(1, 36);
  expect(messageArray[2]).to.eq(action);
}

export { verifyCiStringField, verifyCallFields };
