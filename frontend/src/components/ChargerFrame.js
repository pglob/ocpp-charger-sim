import React, { useState } from 'react';
import { Button } from './Button';

function ChargerFrame() {
  const [isOnline, setIsOnline] = useState(true); // Track online/offline state

  const textContent = [
    { label: 'State', value: isOnline ? 'Available' : 'Offline' },
    { label: 'Meter Value', value: '123456 Wh' },
    { label: 'Max Current', value: '40A' },
    { label: 'Current Flow', value: '0A' },
  ];

  return (
    <div
      style={{
        width: '300px',
        border: '2px solid #000',
        borderRadius: '8px',
        padding: '20px',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'flex-start',
        alignItems: 'center',
        backgroundColor: '#f9f9f9',
      }}
    >
      <div
        style={{
          marginBottom: '20px', // Larger gap for Charger Name
          fontSize: '18px', // Larger font for Charger Name
          fontWeight: 'bold', // Bold Charger Name
        }}
      >
        <p style={{ margin: '0', textAlign: 'left' }}>
          <strong>Charger Name:</strong> Sample A
        </p>
      </div>

      {/*the data displayed below for State, Meter value etc are temporary, will replace
    with real data later*/}
      {textContent.map((item, index) => (
        <div key={index} style={{ marginBottom: '5px' }}>
          {/* Smaller margin */}
          <p style={{ margin: '0', textAlign: 'left' }}>
            {/* Left-aligned text */}
            <strong>{item.label}:</strong> {item.value}
          </p>
        </div>
      ))}
      {isOnline && (
        <>
          <button
            style={{
              width: '200px',
              padding: '10px',
              margin: '10px 0',
              border: '2px solid #000',
              borderRadius: '8px',
              backgroundColor: '#fff',
              cursor: 'pointer',
            }}
          >
            Plug in Vehicle
          </button>
          <button
            style={{
              width: '200px',
              padding: '10px',
              margin: '10px 0',
              border: '2px solid #000',
              borderRadius: '8px',
              backgroundColor: '#fff',
              cursor: 'pointer',
            }}
          >
            Send custom message
          </button>
        </>
      )}
      <Button isOnline={isOnline} setIsOnline={setIsOnline} />
    </div>
  );
}

export default ChargerFrame;
