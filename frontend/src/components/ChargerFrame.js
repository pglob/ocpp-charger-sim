import React, { useState } from 'react';
import { Button } from './Button';
import '../styles/styles.css';

function ChargerFrame() {
  const [isOnline, setIsOnline] = useState(true); // Track online/offline state

  const textContent = [
    { label: 'State', value: isOnline ? 'Available' : 'Offline' },
    { label: 'Meter Value', value: '123456 Wh' },
    { label: 'Max Current', value: '40A' },
    { label: 'Current Flow', value: '0A' },
  ];

  return (
    <div className="charger-frame">
      <div className="charger-name">
        <p style={{ margin: '0', textAlign: 'left' }}>
          <strong>Charger Name:</strong> Sample A
        </p>
      </div>

      {/*the data displayed below for State, Meter value etc are temporary, will replace
    with real data later*/}
      {textContent.map((item, index) => (
        <div key={index} className="text-item">
          {/* Smaller margin */}
          <p className="text-item">
            {/* Left-aligned text */}
            <strong>{item.label}:</strong> {item.value}
          </p>
        </div>
      ))}
      {isOnline && (
        <>
          <button className="button">Plug in Vehicle</button>
          <button className="button">Send custom message</button>
        </>
      )}
      <Button isOnline={isOnline} setIsOnline={setIsOnline} />
    </div>
  );
}

export default ChargerFrame;
