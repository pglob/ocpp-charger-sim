// Base class for button logic, containing shared postRequest logic
class ButtonBase {
  constructor(name, endpoint) {
    this.name = name; // Button name
    this.endpoint = endpoint; // API endpoint URL
  }

  // Generic method for sending POST requests to the backend
  postRequest(data) {
    // TODO: Look into options for dynamically setting this url.
    // Something like `${window.location.protocol}//${window.location.hostname}:8080`.
    // The above works for external browsers, but not the internal docker network.
    const url = `http://localhost:8080${this.endpoint}`;

    return fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json', // Default request format
      },
      body: JSON.stringify(data || {}), // Request body
    })
      .then((response) => {
        if (response.ok) {
          console.log(
            `${this.name} button request to ${this.endpoint} successful!`
          );
          const contentType = response.headers.get('content-type');
          if (contentType && contentType.includes('application/json')) {
            return response.json(); // Return JSON
          } else {
            return response.text(); // Return text
          }
        } else {
          console.error(
            `${this.name} button request to ${this.endpoint} failed with status:`,
            response.status
          );
          throw new Error(`Request failed with status ${response.status}`);
        }
      })
      .catch((error) => {
        console.error(`Error with ${this.endpoint}:`, error);
        // Catch and log any exceptions
      });
  }
}

export default ButtonBase;
