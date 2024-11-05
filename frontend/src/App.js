// Import React and necessary hooks from the React library
import React, { useEffect, useState } from 'react';

function App() {
  const [message, setMessage] = useState('');

  // hook to fetch data from the backend when the component mounts
  useEffect(() => {
    fetch(`${process.env.REACT_APP_BACKEND_URL}/api/test`)
      .then(response => response.text())
      .then(data => setMessage(data))
      .catch(error => console.error('Error fetching test message:', error));
  }, []);

  // Render the component
  return (
    <div>
      <h1>Frontend</h1>
      <p>Message from backend: {message}</p>
    </div>
  );
}

export default App;
