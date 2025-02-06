// Function to get sent messages
export const fetchSentMessages = async () => {
  try {
    const response = await fetch(
      `${process.env.REACT_APP_BACKEND_URL}/api/log/sentmessage`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      throw new Error('Failed to fetch sent messages');
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.log('Error fetching sent messages:', error);
    return [];
  }
};

// Function to get received messages
export const fetchReceivedMessages = async () => {
  try {
    const response = await fetch(
      `${process.env.REACT_APP_BACKEND_URL}/api/log/receivedmessage`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      throw new Error('Failed to fetch received messages');
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.log('Error fetching received messages:', error);
    return [];
  }
};
