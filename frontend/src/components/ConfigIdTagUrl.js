import React, { useState } from 'react';
import { FaCog, FaTimes } from 'react-icons/fa';
import '../styles/styles.css';

function ConfigGear() {
  const [showConfigModal, setShowConfigModal] = useState(false);
  const [idTag, setIdTag] = useState('');
  const [centralSystemUrl, setCentralSystemUrl] = useState('');
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState(''); // "success" or "error"

  // Fetch the current configuration
  const fetchConfig = async () => {
    try {
      const response = await fetch(
        `${process.env.REACT_APP_BACKEND_URL}/api/get-idtag-csurl`,
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );
      if (!response.ok) {
        setMessage('Failed to fetch configration values.');
        setMessageType('error');
        throw new Error('Failed to fetch configuration');
      }
      const config = await response.json();
      setIdTag(config.idTag);
      setCentralSystemUrl(config.centralSystemUrl);
    } catch (error) {
      console.error('Error fetching configuration:', error);
    }
  };

  // Handle updating the configuration
  const updateConfig = async () => {
    try {
      const response = await fetch(
        `${process.env.REACT_APP_BACKEND_URL}/api/update-idtag-csurl`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            idTag,
            centralSystemUrl,
          }),
        }
      );

      if (!response.ok) {
        setMessage('Failed to update configuration.');
        setMessageType('error');
        throw new Error('Failed to update configuration');
      }
    } catch (error) {
      console.error('Error updating configuration:', error);
    }
  };

  // Handle modal opening and fetching config
  const handleClick = async () => {
    await fetchConfig();
    setShowConfigModal(true);
  };

  // Handle update action
  const handleUpdate = async () => {
    if (!idTag || !centralSystemUrl) {
      setMessage('Both idTag and Central System URL are required.');
      setMessageType('error');
      return;
    }
    try {
      await updateConfig();
      setShowConfigModal(false); // Close modal immediately after success
      setMessage('');
    } catch (error) {
      console.error('Error updating configuration:', error);
    }
  };

  return (
    <div>
      <div onClick={handleClick} className="config-button">
        <FaCog /> {/* Gear icon */}
      </div>

      {/* Modal for configuration */}
      {showConfigModal && (
        <div className="config-modal-overlay">
          <div className="config-modal-content">
            <h2 className="config-heading">Update Configuration</h2>

            {/* Display Message */}
            {message && (
              <div className={`config-message ${messageType}`}>
                {message}
                <button
                  className="config-message-close"
                  onClick={() => setMessage('')}
                >
                  <FaTimes />
                </button>
              </div>
            )}

            <label>
              idTag:
              <input
                type="text"
                value={idTag}
                onChange={(e) => setIdTag(e.target.value)}
                maxLength={20}
                className="config-input"
              />
            </label>
            <br />
            <label>
              Central System URL:
              <input
                type="text"
                value={centralSystemUrl}
                onChange={(e) => setCentralSystemUrl(e.target.value)}
                className="config-input"
              />
            </label>
            <br />
            <button onClick={handleUpdate} className="config-button-update">
              Update
            </button>
            <button
              onClick={() => setShowConfigModal(false)}
              className="config-button-exit"
            >
              Exit
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default ConfigGear;
