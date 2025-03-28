// Authorize button logic extending the base button class
import ButtonBase from './ButtonBase';

class AuthorizeButton extends ButtonBase {
  constructor(chargerID) {
    super('Authorize', `/api/${chargerID}/message/authorize`); // Set button name and endpoint
  }

  // Custom logic for Authorize button
  postRequest(data) {
    console.log('AuthorizeButton postRequest triggered.');
    return super.postRequest(data);
  }
}

export default AuthorizeButton;
