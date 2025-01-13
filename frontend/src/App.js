import React, { useEffect, useState } from 'react';
// import { Button } from './components/Button';
import ChargerFrame from './components/ChargerFrame';

function App() {
  const [message, setMessage] = useState('');

  // Hook to fetch data from backend when the component mounts
  useEffect(() => {
    fetch(`${process.env.REACT_APP_BACKEND_URL}/api/test`)
      .then((response) => response.text())
      .then((data) => setMessage(data))
      .catch((error) => console.error('Error fetching test message:', error));
  }, []);

  // Render the component
  return (
    <div>
      <h1>Frontend</h1>
      <p data-testid="message">Message from backend: {message}</p>
      <ChargerFrame />
    </div>
  );
}

export default App;
