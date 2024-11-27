// Base class for button logic, containing shared postRequest logic
class ButtonBase {
  constructor(name, endpoint) {
    this.name = name; // Button name
    this.endpoint = endpoint; // API endpoint URL
  }

  // Generic method for sending POST requests to the backend
  postRequest(data) {
    const url = `${process.env.REACT_APP_BACKEND_URL}${this.endpoint}`;

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
          return response.json(); // Return the parsed response
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
