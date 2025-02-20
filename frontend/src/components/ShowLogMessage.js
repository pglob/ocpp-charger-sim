import React, { useEffect, useState } from 'react';
import { fetchSentMessages, fetchReceivedMessages } from './RetrieveLogMessage';
import PropTypes from 'prop-types';
import '../styles/styles.css';

const ShowLogMessages = ({ chargerID }) => {
  const [sentMessages, setSentMessages] = useState([]);
  const [receivedMessages, setReceivedMessages] = useState([]);

  // State to manage expanded details
  const [expandedSentMessages, setExpandedSentMessages] = useState(new Set());
  const [expandedReceivedMessages, setExpandedReceivedMessages] = useState(
    new Set()
  );

  // Helper function for mapping NumId to labels
  const getNumIdLabel = (NumId) => {
    return (
      {
        2: 'Call',
        3: 'Response',
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
  const handleToggleDetails = (userId, type) => {
    if (type === 'sent') {
      setExpandedSentMessages((prevExpanded) => {
        const updated = new Set(prevExpanded);
        if (updated.has(userId)) {
          updated.delete(userId);
        } else {
          updated.add(userId);
        }
        return updated;
      });
    } else if (type === 'received') {
      setExpandedReceivedMessages((prevExpanded) => {
        const updated = new Set(prevExpanded);
        if (updated.has(userId)) {
          updated.delete(userId);
        } else {
          updated.add(userId);
        }
        return updated;
      });
    }
  };

  // Recursive helper function to render nested fields
  const renderDeep = (value) => {
    if (value === null || typeof value !== 'object') {
      return String(value);
    }
    if (Array.isArray(value)) {
      return (
        <ul>
          {value.map((item, index) => (
            <li key={index}>{renderDeep(item)}</li>
          ))}
        </ul>
      );
    }
    // Render message by iterating over their fields
    return (
      <ul>
        {Object.entries(value).map(([subKey, subValue]) => (
          <li key={subKey}>
            <strong>{subKey}:</strong> {renderDeep(subValue)}
          </li>
        ))}
      </ul>
    );
  };

  // Parse and format the sent message for display
  const parseMessage = (message) => {
    try {
      const parsedMessage = JSON.parse(message); // Parse the string into an array
      const [TimeStamp, NumId, messageId, messageName, payload] = parsedMessage;
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
          key={messageId}
          onClick={() => handleToggleDetails(messageId, 'sent')}
          className="log-message-container"
        >
          {/* Grouped Content */}
          <div className="log-message-header">
            <p>
              <span>{formattedTime} - </span>
              <strong>{messageName}</strong>
            </p>

            {/* Message Number ID (numIdLabel) with a rectangular box */}
            <div className={`num-id-label ${numIdStyle}`}>{numIdLabel}</div>
          </div>
          {/* Expanded Details */}
          {expandedSentMessages.has(messageId) && (
            <div className="message-list">
              <p>
                <strong>Message ID:</strong> {messageId}
              </p>
              {Object.entries(payload).map(([key, value]) => (
                <p key={key}>
                  <strong>{key}:</strong> {renderDeep(value)}
                </p>
              ))}
            </div>
          )}
        </div>
      );
    } catch (error) {
      return <p>Error parsing message</p>;
    }
  };

  // Parse and format the received message for display
  const parseReceivedMessage = (message) => {
    try {
      const parsedMessage = JSON.parse(message); // Parse the string into an array
      const [messageName, TimeStamp, NumId, messageId, payload] = parsedMessage;
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
          key={messageId}
          onClick={() => handleToggleDetails(messageId, 'received')}
          className="log-message-container"
        >
          <div className="log-message-header">
            {/* Main message display */}
            <p>
              <span>{formattedTime} - </span>
              <strong>{messageName}</strong>
            </p>

            {/* Message Number ID (numIdLabel) with a rectangular box */}
            <div className={`num-id-label ${numIdStyle}`}>{numIdLabel}</div>
          </div>
          {/* Expanded Details: Message ID & Interval (Hidden until clicked) */}
          {expandedReceivedMessages.has(messageId) && (
            <div className="message-list">
              <p>
                <strong>Message ID:</strong> {messageId}
              </p>
              {Object.entries(payload).map(([key, value]) => (
                <p key={key}>
                  <strong>{key}:</strong>{' '}
                  {key === 'interval' ? (
                    <>{renderDeep(value)} seconds</>
                  ) : (
                    renderDeep(value)
                  )}
                </p>
              ))}
            </div>
          )}
        </div>
      );
    } catch (error) {
      return <p>Error parsing received message</p>;
    }
  };

  // Polling function to fetch data
  const pollMessages = () => {
    const fetchData = async () => {
      const sentData = await fetchSentMessages(chargerID);
      const receivedData = await fetchReceivedMessages(chargerID);
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
  }, [chargerID]);

  // Sort sent and received messages by timestamp, descending (most recent first)
  const sortedSentMessages = sentMessages.sort((a, b) => {
    const timeA = JSON.parse(a)[0];
    const timeB = JSON.parse(b)[0];
    return new Date(timeB) - new Date(timeA);
  });

  const sortedReceivedMessages = receivedMessages.sort((a, b) => {
    const timeA = JSON.parse(a)[1];
    const timeB = JSON.parse(b)[1];
    return new Date(timeB) - new Date(timeA);
  });

  return (
    <div>
      {/* Sent Messages Section */}
      <div className="round-rect-container">Sent Messages</div>
      <div className="message-list-container">
        {sortedSentMessages.length > 0 ? (
          sortedSentMessages.map((message, index) => (
            <div key={index}>{parseMessage(message)}</div>
          ))
        ) : (
          <p className="no-messages">No sent messages found</p>
        )}
      </div>

      {/* Received Messages Section */}
      <div className="round-rect-container">Received Messages</div>
      <div className="message-list-container">
        {sortedReceivedMessages.length > 0 ? (
          sortedReceivedMessages.map((message, index) => (
            <div key={index}>{parseReceivedMessage(message)}</div>
          ))
        ) : (
          <p className="no-messages">No received messages found</p>
        )}
      </div>
    </div>
  );
};

ShowLogMessages.propTypes = {
  chargerID: PropTypes.number.isRequired,
};
export default ShowLogMessages;
