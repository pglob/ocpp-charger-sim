// ChargingButton.js
import React from 'react';
import PropTypes from 'prop-types';
import ButtonBase from './ButtonBase';

class ChargingButtonLogic extends ButtonBase {
  constructor(chargerID, stateValue) {
    if (stateValue.includes('Charging')) {
      // If already charging, offer to stop
      super('Stop Charging', `/api/${chargerID}/transaction/stop-charge`);
    } else if (stateValue.includes('Available')) {
      // If available, offer to start charging
      super('Start Charging', `/api/${chargerID}/transaction/start-charge`);
    } else if (stateValue.includes('Faulted')) {
      // If Faulted, offer to clear the fault
      super('Clear Fault', `/api/${chargerID}/charger/clear-fault`);
    } else {
      super('Unavailable', '');
    }
  }
}

const ChargingButton = ({ chargerID, stateValue }) => {
  // Hide the button if the state is not "Charging", "Available" or "Faulted"
  if (
    !stateValue ||
    (!stateValue.includes('Charging') &&
      !stateValue.includes('Available') &&
      !stateValue.includes('Faulted'))
  ) {
    return null;
  }

  const chargingButton = new ChargingButtonLogic(chargerID, stateValue);

  // Define the click handler that uses the logic from ButtonBase
  const handleClick = async () => {
    try {
      await chargingButton.postRequest();
    } catch (error) {
      console.error('Error processing the charge action:', error);
    }
  };

  return (
    <button className="square-button" onClick={handleClick}>
      {chargingButton.name}
    </button>
  );
};

// Define PropTypes for the component
ChargingButton.propTypes = {
  stateValue: PropTypes.string.isRequired,
  chargerID: PropTypes.number.isRequired,
};

export default ChargingButton;
