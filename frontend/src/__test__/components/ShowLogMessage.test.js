import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import ShowLogMessages from '../../../src/components/ShowLogMessage';
import {
  fetchSentMessages,
  fetchReceivedMessages,
} from '../../../src/components/RetrieveLogMessage';

// 🛠️ Mock RetrieveLogMessage module
jest.mock('../../../src/components/RetrieveLogMessage', () => ({
  fetchSentMessages: jest.fn(),
  fetchReceivedMessages: jest.fn(),
}));

describe('ShowLogMessages Component', () => {
  beforeEach(() => {
    jest.clearAllMocks(); // Reset mocks before each test
  });

  test('displays parsed sent messages correctly', async () => {
    // Ensure mock returns valid JSON
    fetchSentMessages.mockResolvedValue([
      JSON.stringify([
        '2025-02-06T10:13:48.257996862Z',
        2,
        'e107da87-f7ac-4d0f-835f-fb4667d76530',
        'BootNotification',
        {
          chargePointVendor: 'SimulareVendor',
          chargePointModel: 'ModelForPSUCapstone',
          chargePointSerialNumber: 'CPSN123456789012345',
          chargeBoxSerialNumber: 'CBSN123456789012345',
          firmwareVersion: 'FW_1.0.0_Version_2024',
          iccid: 'ICCID1234567890123',
          imsi: 'IMSI1234567890123',
          meterType: 'MeterType123',
          meterSerialNumber: 'MSN123456789012345',
        },
      ]),
    ]);

    fetchReceivedMessages.mockResolvedValue([]); // Empty received messages

    render(<ShowLogMessages />);

    // Wait for mock data to be displayed
    await waitFor(() => {
      expect(screen.getByText(/BootNotification/)).toBeInTheDocument();
      expect(screen.getByText(/Call/)).toBeInTheDocument();
    });
  });

  test('displays parsed received messages correctly', async () => {
    fetchReceivedMessages.mockResolvedValue([
      JSON.stringify([
        'Authorize',
        '2025-02-06T10:13:48.567685934Z',
        3,
        '0f962691-6d91-41fd-890f-58fe293b79a7',
        {
          status: 'Accepted',
          currentTime: '2025-02-01T20:53:32.486Z',
          interval: 30,
        },
      ]),
    ]);

    fetchSentMessages.mockResolvedValue([]); // Empty sent messages

    render(<ShowLogMessages />);

    await waitFor(() => {
      expect(screen.getByText(/Authorize/)).toBeInTheDocument();
      expect(screen.getByText(/Response/)).toBeInTheDocument();
    });
  });

  test('handles invalid JSON messages gracefully', async () => {
    fetchSentMessages.mockResolvedValue(['invalid JSON']);
    fetchReceivedMessages.mockResolvedValue(['invalid JSON']);

    render(<ShowLogMessages />);

    await waitFor(() => {
      expect(screen.getByText(/Error parsing message/)).toBeInTheDocument();
      expect(
        screen.getByText(/Error parsing received message/)
      ).toBeInTheDocument();
    });
  });

  test('renders nested payload details correctly', async () => {
    // Create a deeply nested payload for a received message
    const nestedPayload = {
      idTagInfo: {
        status: 'Accepted',
        extra: {
          nestedKey: 'nestedValue',
        },
      },
      anotherKey: ['item1', { subItem: 'subValue' }],
    };

    fetchReceivedMessages.mockResolvedValue([
      JSON.stringify([
        'DeepNestedMessage',
        '2025-02-06T10:15:00.000Z',
        3,
        'unique-nested-message-id',
        nestedPayload,
      ]),
    ]);

    fetchSentMessages.mockResolvedValue([]); // No sent messages

    render(<ShowLogMessages />);

    // Wait for the message to be rendered
    await waitFor(() => {
      expect(screen.getByText(/DeepNestedMessage/)).toBeInTheDocument();
    });

    // Click the message to expand the nested details
    fireEvent.click(screen.getByText(/DeepNestedMessage/));

    // Verify that the nested details are rendered
    await waitFor(() => {
      expect(screen.getByText(/nestedKey/)).toBeInTheDocument();
      expect(screen.getByText(/nestedValue/)).toBeInTheDocument();
      expect(screen.getByText(/item1/)).toBeInTheDocument();
      expect(screen.getByText(/subItem/)).toBeInTheDocument();
      expect(screen.getByText(/subValue/)).toBeInTheDocument();
    });
  });
});
