import React, { useEffect, useState } from 'react';
import { fetchSentMessages, fetchReceivedMessages } from './RetrieveLogMessage';

const ShowLogMessages = () => {
  const [sentMessages, setSentMessages] = useState([]);
  const [receivedMessages, setReceivedMessages] = useState([]);

  // Parse and format the message for display
  const parseMessage = (message) => {
    try {
      const parsedMessage = JSON.parse(message); // Parse the string into an array
      const [NumId, userId, messageType, payload] = parsedMessage;

      return (
        <div key={userId} style={{ marginBottom: '20px' }}>
          <p>
            <strong>User ID:</strong> {userId}
          </p>
          <p>
            <strong>Message Type:</strong> {messageType}
          </p>
          <p>
            <strong>Number ID:</strong> {NumId}
          </p>
          <div>
            {Object.entries(payload).map(([key, value]) => (
              <p key={key}>
                <strong>{key}:</strong> {value}
              </p>
            ))}
          </div>
        </div>
      );
    } catch (error) {
      console.error('Error parsing message:', error);
      return <p>Error parsing message</p>;
    }
  };

  // Fetch messages on component mount
  useEffect(() => {
    const getSentMessages = async () => {
      const data = await fetchSentMessages();
      setSentMessages(data);
    };

    const getReceivedMessages = async () => {
      const data = await fetchReceivedMessages();
      setReceivedMessages(data);
    };

    getSentMessages();
    getReceivedMessages();
  }, []);

  return (
    <div>
      <h1>OCPP Messages</h1>

      {/* Sent Messages Section */}
      <div>
        <h2>Sent Messages</h2>
        <div
          style={{
            border: '1px solid #ccc',
            borderRadius: '8px',
            padding: '10px',
            maxHeight: '300px',
            width: '400px',
            overflowY: 'auto', // Enable scrolling
            backgroundColor: '#f9f9f9',
          }}
        >
          {sentMessages.length > 0 ? (
            sentMessages.map((message) => parseMessage(message))
          ) : (
            <p style={{ textAlign: 'center' }}>No sent messages found</p>
          )}
        </div>
      </div>

      {/* Received Messages Section */}
      <div style={{ marginTop: '20px' }}>
        <h2>Received Messages</h2>
        <div
          style={{
            border: '1px solid #ccc',
            borderRadius: '8px',
            padding: '10px',
            maxHeight: '300px',
            width: '400px',
            overflowY: 'auto', // Enable scrolling
            backgroundColor: '#f9f9f9',
          }}
        >
          {receivedMessages.length > 0 ? (
            receivedMessages.map((message) => parseMessage(message))
          ) : (
            <p style={{ textAlign: 'center' }}>No received messages found</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default ShowLogMessages;
