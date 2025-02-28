import React from 'react';
import PropTypes from 'prop-types'; // Import PropTypes
import AuthorizeButton from '../components/buttons/AuthorizeButton';
import HeartbeatButton from '../components/buttons/HeartbeatButton';
import ButtonBase from './buttons/ButtonBase';
import '../styles/styles.css';

// Implementation of the button component, which includes a drop-down menu and an online/offline button
function Button({ chargerID, isOnline, isActive }) {
  // Create instances of each button class
  const buttons = [
    new AuthorizeButton(chargerID),
    new HeartbeatButton(chargerID),
  ];
  // Handle Bring Online/Take Offline button click behavior
  const handleOnlineOffline = async () => {
    const endpoint = isOnline
      ? `/api/${chargerID}/state/offline`
      : `/api/${chargerID}/state/online`;
    const buttonName = isOnline ? 'Take Offline' : 'Bring Online';

    // Use the base class for online/offline behavior
    const onlineOfflineButton = new ButtonBase(buttonName, endpoint);
    try {
      // Wait for the POST request to succeed (e.g., 200 OK) before changing the state
      await onlineOfflineButton.postRequest();
    } catch (error) {
      console.error('Failed to update online/offline state:', error);
    }
  };

  return (
    <div className="button-container">
      {isOnline && isActive && (
        <>
          <div>
            {/* Render buttons dynamically */}
            {buttons.map((button) => (
              <button
                key={button.name}
                onClick={() => button.postRequest()} // Trigger specific button's postRequest
                className="other-buttons"
              >
                {button.name}
              </button>
            ))}
          </div>
          {/* )} */}
        </>
      )}

      {/* Bring Online/Take Offline button */}
      <button onClick={handleOnlineOffline} className="dropdown-button">
        {isOnline ? 'Take Offline' : 'Bring Online'}
      </button>
    </div>
  );
}

Button.propTypes = {
  chargerID: PropTypes.number.isRequired, // 'chargerID' should be a number
  isOnline: PropTypes.bool.isRequired, // 'isOnline' should be a boolean
  isActive: PropTypes.bool.isRequired, // 'isActive' should be a boolean
};

export { Button };
