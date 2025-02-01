import React, { useEffect, useState } from 'react';
import { fetchSentMessages, fetchReceivedMessages } from './RetrieveLogMessage';
import '../styles/styles.css';

const ShowLogMessages = () => {
  const [sentMessages, setSentMessages] = useState([]);
  const [receivedMessages, setReceivedMessages] = useState([]);

  // State to manage expanded details
  const [expandedMessages, setExpandedMessages] = useState(new Set());

  // State for dropdown menu selection (log message type)
  const [logMessageType, setLogMessageType] = useState('default');

  // Helper function for mapping NumId to labels
  const getNumIdLabel = (NumId) => {
    return (
      {
        2: 'Call',
        3: 'Respone',
        4: 'Error',
      }[NumId] || 'Unknown'
    );
  };

  // Function to determine styling based on NumId
  const getNumIdStyle = (NumId) => {
    switch (NumId) {
      case 2:
        return 'num-id-call';
      case 3:
        return 'num-id-result';
      case 4:
        return 'num-id-error';
      default:
        return 'num-id-unknown';
    }
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
      const formattedTime = new Date(TimeStamp).toLocaleTimeString('en-US', {
        hour: 'numeric',
        minute: '2-digit',
        second: '2-digit',
        hour12: true,
      });

      const numIdLabel = getNumIdLabel(NumId);
      const numIdStyle = getNumIdStyle(NumId); // Apply dynamic styling

      return (
        <div
          key={userId}
          onClick={() => handleToggleDetails(userId)}
          className="log-message-container"
        >
          {/* Grouped Content */}
          <div className="log-message-header">
            <p>
              <span>{formattedTime} - </span>
              <strong>{RequestType}</strong>
            </p>

            {/* Message Number ID (numIdLabel) with a rectangular box */}
            <div className={`num-id-label ${numIdStyle}`}>{numIdLabel}</div>
          </div>
          {/* Expanded Details */}
          {expandedMessages.has(userId) && (
            <div>
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
      const [NumId, userId, payload] = parseReceivedMessage;
      const { status, currentTime, interval } = payload;
      const formattedTime = new Date(currentTime).toLocaleTimeString('en-US', {
        hour: 'numeric',
        minute: '2-digit',
        second: '2-digit',
        hour12: true,
      });

      const numIdLabel = getNumIdLabel(NumId);
      const numIdStyle = getNumIdStyle(NumId); // Apply dynamic styling

      return (
        <div
          key={userId}
          onClick={() => handleToggleDetails(userId)}
          className="log-message-container"
        >
          <div className="log-message-header">
            {/* Main message display */}
            <p>
              <span>{formattedTime} - </span>
              <strong>{status}</strong>
            </p>

            {/* Message Number ID (numIdLabel) with a rectangular box */}
            <div className={`num-id-label ${numIdStyle}`}>{numIdLabel}</div>
          </div>
          {/* Expanded Details: Message ID & Interval (Hidden until clicked) */}
          {expandedMessages.has(userId) && (
            <div>
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
      {/* Dropdown Menu for Selecting Log Messages */}
      <div className="dropdown-container">
        <label htmlFor="logMessageType"> </label>
        <select
          id="logMessageType"
          value={logMessageType}
          onChange={(e) => setLogMessageType(e.target.value)}
        >
          <option value="default">Log Message</option>
          <option value="sent">Sent Messages</option>
          <option value="received">Received Messages</option>
        </select>
      </div>

      {/* Conditionally Render Based on Dropdown Selection */}
      {logMessageType === 'sent' && (
        <div className="message-list-container">
          {sortedSentMessages.length > 0 ? (
            sortedSentMessages.map((message) => parseMessage(message))
          ) : (
            <p className="no-messages">No sent messages found</p>
          )}
        </div>
      )}

      {logMessageType === 'received' && (
        <div className="message-list-container">
          {sortedReceivedMessages.length > 0 ? (
            sortedReceivedMessages.map((message) =>
              parseReceivedMessage(message)
            )
          ) : (
            <p className="no-messages">No received messages found</p>
          )}
        </div>
      )}
    </div>
  );
};

export default ShowLogMessages;
