import React, { useState } from 'react';
import '../styles/styles.css';

const errorCodes = [
  'ConnectorLockFailure',
  'EVCommunicationError',
  'GroundFailure',
  'HighTemperature',
  'InternalError',
  'LocalListConflict',
  'NoError',
  'OtherError',
  'OverCurrentFailure',
  'OverVoltage',
  'PowerMeterFailure',
  'PowerSwitchFailure',
  'ReaderFailure',
  'ResetFailure',
  'UnderVoltage',
  'WeakSignal',
];

const statusOptions = [
  'Available',
  'Preparing',
  'Charging',
  'SuspendedEVSE',
  'SuspendedEV',
  'Finishing',
  'Faulted',
  'Unavailable',
  'Reserved',
];

const ErrorMenu = ({ chargerID }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [feedback, setFeedback] = useState('');
  const [feedbacktype, setFeedbackType] = useState(false);
  const [useCurrentTimestamp, setUseCurrentTimestamp] = useState(false);
  const [loading, setLoading] = useState(false);
  // State for the error details
  const [connectorId, setConnectorId] = useState('1');
  const [errorCode, setErrorCode] = useState('');
  const [info, setInfo] = useState('');
  const [status, setStatus] = useState('');
  const [vendorId, setVendorId] = useState('');
  const [vendorErrorCode, setVendorErrorCode] = useState('');

  const openMenu = () => {
    setIsOpen(true);
    setFeedback('');
  };

  const closeMenu = () => {
    setIsOpen(false);
    setFeedback('');
    setLoading(false);
  };

  const resetForm = () => {
    setConnectorId('1');
    setErrorCode('');
    setInfo('');
    setStatus('');
    setVendorId('');
    setVendorErrorCode('');
    setUseCurrentTimestamp(false);
    setLoading(false);
  };

  const stopPropagation = (e) => {
    e.stopPropagation();
  };

  const submit = (e) => {
    e.preventDefault();
    setLoading(true); //submit effect

    let payload = {
      connectorId,
      errorCode,
      status,
    };

    // Conditionally add optional fields
    if (useCurrentTimestamp) payload.timestamp = '';
    if (info.trim()) payload.info = info;
    if (vendorId.trim()) payload.vendorId = vendorId;
    if (vendorErrorCode.trim()) payload.vendorErrorCode = vendorErrorCode;

    // Submit the form data to the backend
    fetch(
      `${process.env.REACT_APP_BACKEND_URL}/api/${chargerID}/state/status`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      }
    )
      .then((response) => {
        if (!response.ok) {
          return response.text().then((text) => {
            setFeedback('Missing required fields, Please try again');
            setFeedbackType(false);
            setLoading(false); //if fail, stop loading
            return Promise.reject(text);
          });
        }
        return response.text();
      })
      .then((data) => {
        setFeedback(data);
        setFeedbackType(true);
        setTimeout(() => {
          closeMenu();
          resetForm();
        }, 2500);
      })
      .catch((error) => {
        console.error('Error:', error);
      });
  };

  return (
    <div className="error-menu-container">
      <button className="dropdown-toggle" onClick={openMenu}>
        Simulate exceptions
      </button>
      {isOpen && (
        <div className="modal-overlay" onClick={closeMenu}>
          <div className="modal-content" onClick={stopPropagation}>
            <button className="modal-close-button" onClick={closeMenu}>
              X
            </button>
            <h3>StatusNotification Payload Creator </h3>
            {feedback && (
              <div className={`feedback ${feedbacktype ? 'success' : 'error'}`}>
                {feedback}
              </div>
            )}
            <form onSubmit={submit}>
              <div className="form-item">
                <label>Connector ID (required):</label>
                <select
                  value={connectorId}
                  onChange={(e) => setConnectorId(e.target.value)}
                >
                  <option value="1">1</option>
                  <option value="0">0</option>
                </select>
              </div>

              <div className="form-item">
                <label>Error Code (required):</label>
                <select
                  value={errorCode}
                  onChange={(e) => setErrorCode(e.target.value)}
                >
                  <option value="">select an error code</option>
                  {errorCodes.map((code) => (
                    <option key={code} value={code}>
                      {code}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-item">
                <label>Info:</label>
                <input
                  type="text"
                  value={info}
                  onChange={(e) => setInfo(e.target.value)}
                  placeholder="enter any info"
                />
              </div>

              <div className="form-item">
                <label>Status (required):</label>
                <select
                  value={status}
                  onChange={(e) => setStatus(e.target.value)}
                >
                  <option value="">select a status </option>
                  {statusOptions.map((status) => (
                    <option key={status} value={status}>
                      {status}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-item">
                <label>
                  <input
                    type="checkbox"
                    checked={useCurrentTimestamp}
                    onChange={(e) => setUseCurrentTimestamp(e.target.checked)}
                  />
                  Include current timestamp
                </label>
              </div>

              <div className="form-item">
                <label>Vendor ID:</label>
                <input
                  type="text"
                  value={vendorId}
                  onChange={(e) => setVendorId(e.target.value)}
                  placeholder="enter a vendor ID"
                />
              </div>

              <div className="form-item">
                <label>Vendor Error Code:</label>
                <input
                  type="text"
                  value={vendorErrorCode}
                  onChange={(e) => setVendorErrorCode(e.target.value)}
                  placeholder="enter a vendor error code"
                />
              </div>

              <button
                type="submit"
                className="submit-button"
                disabled={loading}
                style={{ opacity: loading ? 0.5 : 1 }} //change the opacity of the button when loading
              >
                {loading ? 'Submitting...' : 'Send StatusNotification'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ErrorMenu;
