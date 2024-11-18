import React, { useState } from 'react';

const messages = [
  { name: 'Authorize', endpoint: '/api/message/authorize' },
  { name: 'Boot', endpoint: '/api/message/boot' },
  { name: 'Heartbeat', endpoint: '/api/message/heartbeat' },
];

//Generic method where each button POST request to the specified endpoint
function postRequest(endpoint, button) {
  return fetch(endpoint, {
    method: 'POST', //Add header and body after clarifying the request content
  })
    .then((response) => {
      if (response.ok) {
        console.log(`${button} button ${endpoint} request successful!`);
        return response; // Request successful, return response for further processing
      } else {
        console.error(
          `${button} button ${endpoint} request failed with status:`,
          response.status
        );
        throw new Error(`Request failed with status ${response.status}`); //If the request fails, an exception is thrown
      }
    })
    .catch((error) => {
      console.error(`Error with ${endpoint}:`, error);
      //Catch and log the exception
    });
}

//Implementation of the button component, which includes a drop-down menu and an online/offline button
function Button() {
  const [isOnline, setIsOnline] = useState(false);
  const [openDropdown, setOpenDropdown] = useState(false);

  const handleOnlineOffline = () => {
    const endpoint = isOnline ? '/api/state/offline' : '/api/state/online';
    const buttonName = isOnline ? 'Take Offline' : 'Bring Online';
    postRequest(endpoint, buttonName).finally(() => {
      setIsOnline(!isOnline);
    });
  }; //Handle Bring online/Take Offline button click behavior, In offline state, the drop-down menu will not be displayed, which is in line with the example in the requirements document.

  const handleDropdown = () => {
    setOpenDropdown(!openDropdown);
  }; //Handle Dropdown menu open/close

  return (
    <div style={{ position: 'relative' }}>
      {isOnline && (
        <>
          <button onClick={handleDropdown} style={{ marginRight: '10px' }}>
            {openDropdown ? 'Close Menu' : 'Send Messages'}
          </button>
          {openDropdown && (
            <div
              style={{
                position: 'absolute',
                top: '100%',
                border: '1px solid #ddd',
                padding: '10px',
                marginTop: '10px',
                boxShadow: '0px 8px 16px rgba(0, 0, 0, 0.2)',
                display: 'flex',
                flexDirection: 'column',
              }}
            >
              {messages.map((message) => (
                <button
                  key={message.name}
                  onClick={() => postRequest(message.endpoint, message.name)}
                  style={{ marginBottom: '8px' }}
                >
                  {message.name}
                </button>
              ))}
            </div>
          )}
        </>
      )}

      <button onClick={handleOnlineOffline} style={{ marginTop: '10px' }}>
        {isOnline ? 'Take Offline' : 'Bring Online'}
      </button>
    </div>
  );
}

export { Button };
