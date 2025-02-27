// ChargerFrame.js
import React, { useEffect, useState } from 'react';
import { Button } from './Button';
import ChargingButton from './buttons/ChargingButton';
import '../styles/styles.css';
import { pollChargerData } from './ChargerLabels';
import RebootButton from './buttons/RebootButton';
import ErrorMenu from './ErrorMenu';
import ConfigGear from './ConfigIdTagUrl';
import PropTypes from 'prop-types';

function ChargerFrame({ chargerID }) {
  const [data, setData] = useState([]);

  useEffect(() => {
    // Start polling charger data every 5 seconds and state every 1 second
    const intervalId = pollChargerData(chargerID, setData, 5000, 1000);

    // Clean up the polling when the component unmounts
    return () => clearInterval(intervalId);
  }, [chargerID]);

  // Extract the simulator state
  const stateItem = data.find((item) => item.label === 'State');
  const stateValue = stateItem && stateItem.value ? stateItem.value : '';

  // Determine if the simulator is online
  const isOnline = Boolean(stateItem && !stateValue.includes('Offline'));

  // Determine if the simulator is ready
  const isActive = Boolean(
    stateItem &&
      !stateValue.includes('PoweredOff') &&
      !stateValue.includes('BootingUp') &&
      !stateValue.includes('Unknown')
  );

  return (
    <div className="charger-frame">
      <div className="charger-name">
        <p style={{ margin: '0', textAlign: 'left' }}>
          <strong>Charger {chargerID}</strong>
        </p>
      </div>

      {/* Display the charger details and conditionally render the charging button */}
      <div className="content-and-button">
        <div className="text-content-container">
          {data.map((item, index) => (
            <div key={index} className="text-content">
              <p>
                <strong>{item.label}:</strong> {item.value}
              </p>
            </div>
          ))}
        </div>
        {isActive && (
          <ChargingButton chargerID={chargerID} stateValue={stateValue} />
        )}
      </div>
      <RebootButton chargerID={chargerID} />
      <Button chargerID={chargerID} isOnline={isOnline} isActive={isActive} />
      <div className="config-button-container">
        <ConfigGear chargerID={chargerID} />
      </div>
      <ErrorMenu chargerID={chargerID} />
    </div>
  );
}

ChargerFrame.propTypes = {
  chargerID: PropTypes.number.isRequired,
};

export default ChargerFrame;
