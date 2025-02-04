// ChargerLabels.js

const API_BASE_URL = process.env.REACT_APP_BACKEND_URL + '/api';

export const chargerLabels = [
  {
    label: 'State',
    endpoint: `${API_BASE_URL}/state`,
    transform: (text) => text.trim(),
    defaultValue: 'Unknown',
  },
  {
    label: 'Meter Value',
    endpoint: `${API_BASE_URL}/electrical/meter-value`,
    transform: (text) => `${text.trim()} KWh`,
    defaultValue: 'N/A KWh',
  },
  {
    label: 'Max Current',
    endpoint: `${API_BASE_URL}/electrical/max-current`,
    transform: (text) => `${text.trim()}A`,
    defaultValue: 'N/A A',
  },
  {
    label: 'Current Flow',
    endpoint: `${API_BASE_URL}/electrical/current-import`,
    transform: (text) => `${text.trim()}A`,
    defaultValue: 'N/A A',
  },
];

/**
 * Polls charger endpoints.
 */
export const pollChargerData = (
  callback,
  defaultInterval = 5000,
  stateInterval = 1000
) => {
  // This object holds the latest value for each label
  const results = {};

  // Call the callback with the current results
  const updateAndCallback = () => {
    const output = chargerLabels.map((item) => ({
      label: item.label,
      value: results[item.label],
    }));
    callback(output);
  };

  // Poll a given endpoint
  const pollItem = async (item) => {
    try {
      const response = await fetch(item.endpoint);
      if (!response.ok) {
        throw new Error(`Error fetching ${item.label}: ${response.statusText}`);
      }
      const text = await response.text();
      results[item.label] = item.transform(text);
      updateAndCallback();
    } catch (error) {
      results[item.label] = item.defaultValue;
      updateAndCallback();
    }
  };

  // For each endpoint, set a polling interval
  const intervalIds = chargerLabels.map((item) => {
    const interval = item.label === 'State' ? stateInterval : defaultInterval;
    // Poll immediately on start
    pollItem(item);
    // Return the interval ID so the polling can be stopped later if needed
    return setInterval(() => pollItem(item), interval);
  });

  return intervalIds;
};
