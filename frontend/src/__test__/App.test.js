import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import App from '../App';

describe('App component', () => {
  // Ensure the backend URL is defined for testing
  beforeAll(() => {
    const backendHost = window.location.hostname === 'localhost' 
        ? 'http://localhost:8080' 
        : `http://${window.location.hostname}:8080`;

      process.env.REACT_APP_BACKEND_URL = backendHost;
  });

  afterEach(() => {
    jest.clearAllMocks(); // Reset mocks after each test
  });

  it('displays an error message when backend response is not "Ok"', async () => {
    // Mock fetch to simulate a backend response that is not "Ok"
    jest.spyOn(global, 'fetch').mockResolvedValue({
      ok: true,
      text: jest.fn().mockResolvedValue('Hello from the backend!'),
    });

    render(<App />);

    // Wait for the error message to appear
    const errorMessage = await waitFor(() => screen.getByTestId('message'));

    expect(errorMessage).toHaveTextContent(
      `ERROR: Backend at ${process.env.REACT_APP_BACKEND_URL} was not reachable`
    );
  });

  it('does not display an error message when backend returns "Ok"', async () => {
    // Mock fetch to simulate a successful backend response ("Ok")
    jest.spyOn(global, 'fetch').mockResolvedValue({
      ok: true,
      text: jest.fn().mockResolvedValue('Ok'),
    });

    render(<App />);

    // Wait to ensure that if an error message were to appear it would be caught.
    await waitFor(() => {
      expect(screen.queryByTestId('message')).not.toBeInTheDocument();
    });
  });
});
