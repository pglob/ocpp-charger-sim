import React, { useState } from 'react';
import { FaCog } from 'react-icons/fa';
import '../styles/styles.css';

function ConfigGear() {
  const [showConfigModal, setShowConfigModal] = useState(false);
  const [idTag, setIdTag] = useState('');
  const [centralSystemUrl, setCentralSystemUrl] = useState('');

  // Fetch the current configuration (idTag, centralSystemUr
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
        throw new Error('Failed to update configuration');
      }

      const result = await response.text();
      console.log(result); // Success message
      alert('Configuration updated successfully');
    } catch (error) {
      console.error('Error updating configuration:', error);
    }
  };

  // When the button is clicked, it fetches the current configuration and shows the modal
  const handleClick = async () => {
    await fetchConfig();
    setShowConfigModal(true);
  };

  // Handle the update action
  const handleUpdate = async () => {
    if (!idTag || !centralSystemUrl) {
      alert('Both idTag and Central System URL are required.');
      return;
    }

    try {
      await updateConfig();
      setShowConfigModal(false); // Close the modal after update
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
