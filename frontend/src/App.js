import React, { useEffect, useState } from 'react';
import ChargerFrame from './components/ChargerFrame';
import ShowLogMessages from './components/ShowLogMessage';
import './styles/styles.css';

function App() {
  const [status, setStatus] = useState('loading');

  useEffect(() => {
    fetch(`${process.env.REACT_APP_BACKEND_URL}/api/test`)
      .then((response) => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.text();
      })
      .then((data) => {
        if (data === 'Ok') {
          setStatus('success');
        } else {
          setStatus('failure');
        }
      })
      .catch((error) => {
        console.error('Error fetching test message:', error);
        setStatus('failure');
      });
  }, []);

  return (
    <div>
      <h1>OCPP Charger Simulator</h1>
      {status === 'failure' && (
        <b data-testid="message">
          ERROR: Backend at {process.env.REACT_APP_BACKEND_URL} was not
          reachable
        </b>
      )}
      <div className="charger-frames-container">
        <div>
          <ChargerFrame />
          <ShowLogMessages />
        </div>
      </div>
    </div>
  );
}

export default App;
