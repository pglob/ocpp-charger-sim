import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

// Create a root DOM element for rendering the React application
const root = ReactDOM.createRoot(document.getElementById('root'));

root.render(
  // Enable StrictMode to help identify potential problems in an application
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
