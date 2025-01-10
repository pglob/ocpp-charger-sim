import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import App from '../App';

beforeEach(() => {
  global.fetch = jest.fn(() =>
    Promise.resolve({
      text: () => Promise.resolve('Helllo from the backend!'), // Mocked response
    })
  );
});

afterEach(() => {
  jest.clearAllMocks(); // Ensure mocks are reset after each test
});

describe('dummy test for App.js', () => {
  it('should display the message from the backend', async () => {
    render(<App />);

    // Wait for the expected text to appear
    await waitFor(() => {
      const messageElement = screen.getByTestId('message');
      expect(messageElement.textContent).toBe(
        'Message from backend: Helllo from the backend!'
      );
    });
  });
});
