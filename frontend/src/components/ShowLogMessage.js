import React, { useEffect, useState } from 'react';
import { fetchSentMessages, fetchReceivedMessages } from './RetrieveLogMessage';

const ShowLogMessages = () => {
  const [sentMessages, setSentMessages] = useState([]);
  const [receivedMessages, setReceivedMessages] = useState([]);

  // State to manage expanded details
  const [expandedMessages, setExpandedMessages] = useState(new Set());

  // Parse and format the message for display
  const parseMessage = (message) => {
    try {
      const parsedMessage = JSON.parse(message); // Parse the string into an array
      const [TimeStamp, NumId, userId, messageType, payload] = parsedMessage;

      const formattedTime = new Date(TimeStamp).toLocaleString(); // format timestamp to a readable string

      const handleToggleDetails = () => {
        setExpandedMessages((prevExpanded) => {
          const updated = new Set(prevExpanded);
          if (updated.has(userId)) {
            updated.delete(userId);
          } else {
            updated.add(userId);
          }
          return updated;
        });
      };

      return (
        <div key={userId} style={{ marginBottom: '20px' }}>
          <p>
            <strong>Time:</strong> {formattedTime}
          </p>
          <p>
            <strong>Message Type:</strong> {messageType}
          </p>
          <p>
            <strong>Number ID:</strong> {NumId}
          </p>

          {/* Show "Show Details" button to expand the rest */}
          <button onClick={handleToggleDetails}>
            {expandedMessages.has(userId) ? 'Hide Details' : 'Show Details'}
          </button>

          {/* Show extra details when expanded */}
          {expandedMessages.has(userId) && (
            <div style={{ marginTop: '10px', paddingLeft: '20px' }}>
              {Object.entries(payload).map(([key, value]) => (
                <p key={key}>
                  <strong>{key}:</strong> {value}
                </p>
              ))}
            </div>
          )}
        </div>
      );
    } catch (error) {
      console.error('Error parsing message:', error);
      return <p>Error parsing message</p>;
    }
  };

  // Polling function to fetch data
  const pollMessages = () => {
    const fetchData = async () => {
      const sentData = await fetchSentMessages();
      const receivedData = await fetchReceivedMessages();
      setSentMessages(sentData);
      setReceivedMessages(receivedData);
    };

    fetchData(); // Fetch once when the component mounts

    const interval = setInterval(() => {
      fetchData(); // Refetch every 10 seconds (or set your preferred interval)
    }, 10000);

    return interval;
  };

  // Fetch messages on component mount and start polling
  useEffect(() => {
    const interval = pollMessages();

    // Clear the interval when the component unmounts to avoid memory leaks
    return () => clearInterval(interval);
  }, []);

  // Sort sent and received messages by timestamp, descending (most recent first)
  const sortedSentMessages = sentMessages.sort((a, b) => {
    const timeA = JSON.parse(a)[0];
    const timeB = JSON.parse(b)[0];
    return new Date(timeB) - new Date(timeA);
  });

  const sortedReceivedMessages = receivedMessages.sort((a, b) => {
    const timeA = JSON.parse(a)[0];
    const timeB = JSON.parse(b)[0];
    return new Date(timeB) - new Date(timeA);
  });

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
            maxHeight: '150px',
            width: '400px',
            overflowY: 'auto', // Enable scrolling
            backgroundColor: '#f9f9f9',
          }}
        >
          {sortedSentMessages.length > 0 ? (
            sortedSentMessages.map((message) => parseMessage(message))
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
            maxHeight: '150px',
            width: '400px',
            overflowY: 'auto', // Enable scrolling
            backgroundColor: '#f9f9f9',
          }}
        >
          {sortedReceivedMessages.length > 0 ? (
            sortedReceivedMessages.map((message) => parseMessage(message))
          ) : (
            <p style={{ textAlign: 'center' }}>No received messages found</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default ShowLogMessages;
