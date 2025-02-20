// RebootButton.js
import React from 'react';
import ButtonBase from './ButtonBase';

class RebootButtonLogic extends ButtonBase {
  constructor(chargerID) {
    super('Reboot', `/api/${chargerID}/charger/reboot`); // Set button name and endpoint
  }
}

const RebootButton = () => {
  // Instantiate the logic helper
  const rebootButton = new RebootButtonLogic();

  // Define the click handler that uses the logic from ButtonBase
  const handleClick = async () => {
    try {
      await rebootButton.postRequest();
    } catch (error) {
      console.error('Error rebooting:', error);
    }
  };

  return (
    <button className="button" onClick={handleClick}>
      {rebootButton.name}
    </button>
  );
};

export default RebootButton;
