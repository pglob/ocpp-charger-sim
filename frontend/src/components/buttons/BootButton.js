import ButtonBase from './ButtonBase';

class BootButton extends ButtonBase {
  constructor(chargerID) {
    super('Boot', `/api/${chargerID}/message/boot`); // Set button name and endpoint
  }

  // Temporarily add console.log and reuse the base class logic
  postRequest(data) {
    console.log('BootButton postRequest triggered.');
    return super.postRequest(data); // Reuse the base class postRequest
  }
}

export default BootButton;
