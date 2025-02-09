import React, { useState } from 'react';
import '../styles/styles.css';

const ErrorMenu = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [feedback, setFeedback] = useState('');
  const [useCurrentTimestamp, setUseCurrentTimestamp] = useState(false);
  // State for the error details
  const [connectorId, setConnectorId] = useState('1');
  const [errorCode, setErrorCode] = useState('');
  const [info, setInfo] = useState('');
  const [status, setStatus] = useState('');
  const [timestamp, setTimestamp] = useState('');
  const [vendorId, setVendorId] = useState('');
  const [vendorErrorCode, setVendorErrorCode] = useState('');

  const openMenu = () => {
    setIsOpen(true);
    setFeedback('');
  };

  const closeMenu = () => {
    setIsOpen(false);
    setFeedback('');
  };

  const resetForm = () => {
    setConnectorId('1');
    setErrorCode('');
    setInfo('');
    setStatus('');
    setTimestamp('');
    setVendorId('');
    setVendorErrorCode('');
  };

  const stopPropagation = (e) => {
    e.stopPropagation();
  };

  const submit = (e) => {
    e.preventDefault();
    const payload = {
      connectorId,
      errorCode,
      info,
      status,
      timestamp,
      vendorId,
      vendorErrorCode,
    };

    fetch(`${process.env.REACT_APP_BACKEND_URL}/api/state/status`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })
      .then((response) => {
        if (!response.ok) {
          return response.text().then((text) => {
            setFeedback('missing required fields, Please Try again');
            return Promise.reject(text);
          });
        }
        return response.text();
      })
      .then((data) => {
        setFeedback(data);
      })
      .catch((error) => {
        console.error('Error:', error);
      });

    setTimeout(() => {
      closeMenu();
      resetForm();
    }, 5000);
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
            <h3>StatusNotification payload creater </h3>
            {feedback && <div className="feedback">{feedback}</div>}
            <form onSubmit={submit}>
              <div className="form-item">
                <label>Connector ID (0 or 1):</label>
                <select
                  value={connectorId}
                  onChange={(e) => setConnectorId(e.target.value)}
                >
                  <option value="1">1</option>
                  <option value="0">0</option>
                </select>
              </div>

              <div className="form-item">
                <label>Error Code:</label>
                <select
                  value={errorCode}
                  onChange={(e) => setErrorCode(e.target.value)}
                >
                  <option value="">please select the error code</option>
                  <option value="ConnectorLockFailure">
                    ConnectorLockFailure
                  </option>
                  <option value="EVCommunicationError">
                    EVCommunicationError
                  </option>
                  <option value="GroundFailure">GroundFailure</option>
                  <option value="HighTemperature">HighTemperature</option>
                  <option value="InternalError">InternalError</option>
                  <option value="LocalListConflict">LocalListConflict</option>
                  <option value="NoError">NoError</option>
                  <option value="OtherError">OtherError</option>
                  <option value="OverCurrentFailure">OverCurrentFailure</option>
                  <option value="OverVoltage">OverVoltage</option>
                  <option value="PowerMeterFailure">PowerMeterFailure</option>
                  <option value="PowerSwitchFailure">PowerSwitchFailure</option>
                  <option value="ReaderFailure">ReaderFailure</option>
                  <option value="ResetFailure">ResetFailure</option>
                  <option value="UnderVoltage">UnderVoltage</option>
                  <option value="WeakSignal">WeakSignal</option>
                </select>
              </div>

              <div className="form-item">
                <label>Info:</label>
                <input
                  type="text"
                  value={info}
                  onChange={(e) => setInfo(e.target.value)}
                  placeholder="an optional info"
                />
              </div>

              <div className="form-item">
                <label>Status:</label>
                <select
                  value={status}
                  onChange={(e) => setStatus(e.target.value)}
                >
                  <option value="">please select the status</option>
                  <option value="Available">Available</option>
                  <option value="Preparing">Preparing</option>
                  <option value="Charging">Charging</option>
                  <option value="SuspendedEVSE">SuspendedEVSE</option>
                  <option value="SuspendedEV">SuspendedEV</option>
                  <option value="Finishing">Finishing</option>
                  <option value="Faulted">Faulted</option>
                  <option value="Unavailable">Unavailable</option>
                  <option value="Reserved">Reserved</option>
                </select>
              </div>

              <div className="form-item">
                <label>
                  <input
                    type="checkbox"
                    checked={useCurrentTimestamp}
                    onChange={(e) => {
                      setUseCurrentTimestamp(e.target.checked);
                      if (e.target.checked) {
                        const now = new Date();
                        const timestampStr =
                          now.toISOString().slice(0, 16) + ':00Z';
                        setTimestamp(timestampStr);
                      } else {
                        setTimestamp('');
                      }
                    }}
                  />
                  Use current timestamp
                </label>
              </div>

              <div className="form-item">
                <label>Vendor ID:</label>
                <input
                  type="text"
                  value={vendorId}
                  onChange={(e) => setVendorId(e.target.value)}
                  placeholder="please enter the vendor ID"
                />
              </div>

              <div className="form-item">
                <label>Vendor Error Code:</label>
                <input
                  type="text"
                  value={vendorErrorCode}
                  onChange={(e) => setVendorErrorCode(e.target.value)}
                  placeholder="please enter the vendor error code"
                />
              </div>

              <button type="submit" className="submit-button">
                Send StatusNotification
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ErrorMenu;
