import React, { useState } from 'react';
import PropTypes from 'prop-types'; // Import PropTypes
import AuthorizeButton from '../components/buttons/AuthorizeButton';
import BootButton from '../components/buttons/BootButton';
import HeartbeatButton from '../components/buttons/HeartbeatButton';
import ButtonBase from './buttons/ButtonBase';
import '../styles/styles.css';

// Implementation of the button component, which includes a drop-down menu and an online/offline button
function Button({ isOnline, setIsOnline }) {
  const [openDropdown, setOpenDropdown] = useState(false); // Track dropdown open/close state

  // Create instances of each button class
  const buttons = [
    new AuthorizeButton(),
    new BootButton(),
    new HeartbeatButton(),
  ];

  // Handle Bring Online/Take Offline button click behavior
  const handleOnlineOffline = () => {
    const endpoint = isOnline ? '/api/state/offline' : '/api/state/online';
    const buttonName = isOnline ? 'Take Offline' : 'Bring Online';

    // Use the base class for online/offline behavior
    const onlineOfflineButton = new ButtonBase(buttonName, endpoint);
    onlineOfflineButton.postRequest(); // Call the placeholder postRequest
    setIsOnline(!isOnline);
  };

  // Handle Dropdown menu open/close
  const handleDropdown = () => {
    setOpenDropdown(!openDropdown);
  };

  return (
    <div className="button-container">
      {isOnline && (
        <>
          {/* Dropdown toggle button */}
          <button onClick={handleDropdown} className="dropdown-toggle">
            {openDropdown ? 'Close Menu' : 'Send Messages'}
          </button>

          {/* Dropdown menu */}
          {openDropdown && (
            <div className="dropdown-menu">
              {/* Render buttons dynamically */}
              {buttons.map((button) => (
                <button
                  key={button.name}
                  onClick={() => button.postRequest()} // Trigger specific button's postRequest
                  className="dropdown-button"
                >
                  {button.name}
                </button>
              ))}
            </div>
          )}
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
  isOnline: PropTypes.bool.isRequired, // 'isOnline' should be a boolean
  setIsOnline: PropTypes.func.isRequired, // 'setIsOnline' should be a function
};

export { Button };
