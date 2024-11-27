import ButtonBase from './ButtonBase';

class HeartbeatButton extends ButtonBase {
  constructor() {
    super('Heartbeat', '/api/message/heartbeat'); // Set button name and endpoint
  }

  // Temporarily add console.log and reuse the base class logic
  postRequest(data) {
    console.log('HeartbeatButton postRequest triggered.');
    return super.postRequest(data); // Reuse the base class postRequest
  }
}

export default HeartbeatButton;