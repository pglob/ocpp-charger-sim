// ChargerLabels.test.js
import { chargerLabels, pollChargerData } from '../../components/ChargerLabels';
import { waitFor } from '@testing-library/react';

describe('pollChargerData', () => {
  const REAL_BACKEND_URL = 'http://localhost:3000';

  beforeAll(() => {
    process.env.REACT_APP_BACKEND_URL = REAL_BACKEND_URL;
  });

  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
    jest.clearAllMocks();
  });

  it('polls endpoints and calls callback with transformed values on success', async () => {
    // Mock fetch to simulate a successful response for each endpoint
    global.fetch = jest.fn((url) => {
      if (url.endsWith('/state')) {
        return Promise.resolve({
          ok: true,
          text: () => Promise.resolve(' OK '), // Will be trimmed to "OK"
        });
      } else if (url.endsWith('/electrical/meter-value')) {
        return Promise.resolve({
          ok: true,
          text: () => Promise.resolve('123.45'), // Will become "123.45 KWh"
        });
      } else if (url.endsWith('/electrical/max-current')) {
        return Promise.resolve({
          ok: true,
          text: () => Promise.resolve('16'), // Will become "16A"
        });
      } else if (url.endsWith('/electrical/current-import')) {
        return Promise.resolve({
          ok: true,
          text: () => Promise.resolve('8'), // Will become "8A"
        });
      }
      return Promise.reject(new Error('Unknown endpoint'));
    });

    const callback = jest.fn();

    const intervals = pollChargerData('1', callback, 5000, 1000);

    // Advance timers to allow any pending tasks to resolve
    jest.advanceTimersByTime(0);

    // Wait for the callback to be called at least once
    await waitFor(() => expect(callback).toHaveBeenCalled());

    // Retrieve the last set of values passed to the callback
    const lastCallArgs = callback.mock.calls[callback.mock.calls.length - 1][0];

    // Expected transformed results
    const expected = [
      { label: 'State', value: 'OK' },
      { label: 'Meter Value', value: '123.45 KWh' },
      { label: 'Max Current', value: '16A' },
      { label: 'Current Flow', value: '8A' },
    ];
    expect(lastCallArgs).toEqual(expected);

    // Clear intervals to avoid side effects
    intervals.forEach(clearInterval);
  });

  it('falls back to default values when fetch fails', async () => {
    // Override fetch to simulate a network failure
    global.fetch = jest.fn(() => Promise.reject(new Error('Network error')));

    const callback = jest.fn();

    const intervals = pollChargerData('1', callback, 5000, 1000);

    jest.advanceTimersByTime(0);

    // Wait for the callback to be called
    await waitFor(() => expect(callback).toHaveBeenCalled());

    // Retrieve the last set of values passed to the callback
    const lastCallArgs = callback.mock.calls[callback.mock.calls.length - 1][0];

    // Expected fallback default values
    const expected = [
      { label: 'State', value: 'Unknown' },
      { label: 'Meter Value', value: 'N/A KWh' },
      { label: 'Max Current', value: 'N/A A' },
      { label: 'Current Flow', value: 'N/A A' },
    ];
    expect(lastCallArgs).toEqual(expected);

    intervals.forEach(clearInterval);
  });
});
