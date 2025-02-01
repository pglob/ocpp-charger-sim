import React, { useEffect, useState } from 'react';
import { fetchSentMessages, fetchReceivedMessages } from './RetrieveLogMessage';

const ShowLogMessages = () => {
  const [sentMessages, setSentMessages] = useState([]);
  const [receivedMessages, setReceivedMessages] = useState([]);

  // State to manage expanded details
  const [expandedMessages, setExpandedMessages] = useState(new Set());
  // Helper function for mapping NumId to labels
  const getNumIdLabel = (NumId) => {
    return (
      {
        2: 'Call',
        3: 'Result',
        4: 'Error',
      }[NumId] || 'Unknown'
    );
  };

  // Helper function to toggle message details
  const handleToggleDetails = (userId) => {
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

  // Parse and format the message for display
  const parseMessage = (message) => {
    try {
      const parsedMessage = JSON.parse(message); // Parse the string into an array
      const [TimeStamp, NumId, userId, RequestType, payload] = parsedMessage;

      const formattedTime = new Date(TimeStamp).toLocaleString(); // Format timestamp to a readable string
      const numIdLabel = getNumIdLabel(NumId);
      return (
        <div
          key={userId}
          onClick={() => handleToggleDetails(userId)}
          style={{
            cursor: 'pointer',
            border: '1px solid #ccc',
            borderRadius: '8px',
            padding: '10px',
            marginBottom: '20px',
            backgroundColor: '#fff',
            boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
          }}
        >
          {/* Grouped Content */}
          <div
            style={{
              marginBottom: '10px',
              display: 'flex',
              alignItems: 'center',
              gap: '10px', // Space between timestamp, request type, and numIdLabel
            }}
          >
            <p style={{ margin: 0 }}>
              <span>{formattedTime} - </span>
              <strong>{RequestType}</strong>
            </p>

            {/* Message Number ID (numIdLabel) with a rectangular box */}
            <div
              style={{
                backgroundColor: '#E8F0FE',
                border: '1px solid #007BFF',
                borderRadius: '4px',
                padding: '5px 10px',
                fontWeight: 'bold',
                color: '#007BFF',
                textAlign: 'center',
              }}
            >
              {numIdLabel}
            </div>
          </div>
          {/* Expanded Details */}
          {expandedMessages.has(userId) && (
            <div
            // style={{
            //   marginTop: '10px',
            //   padding: '10px',
            //   border: '1px solid #ddd',
            //   borderRadius: '8px',
            //   backgroundColor: '#f9f9f9',
            // }}
            >
              <p>
                <strong>User ID:</strong> {userId}
              </p>
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

  const parseReceivedMessage = (message) => {
    try {
      const parseReceivedMessage = JSON.parse(message); // Parse the string into an array

      const [NumId, userId, payload] = parseReceivedMessage; // Extract relevant parts from array

      const { status, currentTime, interval } = payload; // Extract values

      const formattedTime = new Date(currentTime).toLocaleString();
      const numIdLabel = getNumIdLabel(NumId);

      return (
        <div
          key={userId}
          onClick={() => handleToggleDetails(userId)}
          style={{
            cursor: 'pointer',
            border: '1px solid #ccc',
            borderRadius: '8px',
            padding: '10px',
            marginBottom: '10px',
            backgroundColor: '#fff',
            boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
          }}
        >
          <div
            style={{
              marginBottom: '10px',
              display: 'flex',
              alignItems: 'center',
              gap: '10px', // Space between the message details and numIdLabel
            }}
          >
            {/* Main message display */}
            <p style={{ margin: 0 }}>
              <span>{formattedTime} - </span>
              <strong>{status}</strong>
            </p>

            {/* Message Number ID (numIdLabel) with a rectangular box */}
            <div
              style={{
                backgroundColor: '#E8F0FE',
                border: '1px solid #007BFF',
                borderRadius: '4px',
                padding: '5px 10px',
                fontWeight: 'bold',
                color: '#007BFF',
                textAlign: 'center',
              }}
            >
              {numIdLabel}
            </div>
          </div>
          {/* Expanded Details: Message ID & Interval (Hidden until clicked) */}
          {expandedMessages.has(userId) && (
            <div
            // style={{
            //   marginTop: '10px',
            //   padding: '10px',
            //   border: '1px solid #ddd',
            //   borderRadius: '8px',
            //   backgroundColor: '#f9f9f9',
            // }}
            >
              <p>
                <strong>User ID:</strong> {userId}
              </p>
              <p>
                <strong>Interval:</strong> {interval} seconds
              </p>
            </div>
          )}
        </div>
      );
    } catch (error) {
      console.error('Error parsing received message:', error);
      return <p>Error parsing received message</p>;
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
    const timeA = JSON.parse(a)[2].currentTime;
    const timeB = JSON.parse(b)[2].currentTime;

    return new Date(timeB) - new Date(timeA);
  });

  return (
    <div>
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
            sortedReceivedMessages.map((message) =>
              parseReceivedMessage(message)
            )
          ) : (
            <p style={{ textAlign: 'center' }}>No received messages found</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default ShowLogMessages;
