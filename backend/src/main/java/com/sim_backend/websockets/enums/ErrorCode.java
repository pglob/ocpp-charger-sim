package com.sim_backend.websockets.enums;

public enum ErrorCode {
  /** Requested Action is not known by receiver. */
  NotImplemented,
  /** Requested Action is recognized but not supported by the receiver. */
  NotSupported,
  /**
   * An internal error occurred and the receiver was not able to process the requested Action
   * successfully.
   */
  InternalError,
  /** Payload for Action is incomplete. */
  ProtocolError,
  /**
   * During the processing of Action a security issue occurred preventing receiver from completing
   * the Action successfully.
   */
  SecurityError,
  /** Payload for Action is syntactically incorrect or not conform the PDU structure for Action. */
  FormatViolation,
  /** Payload is syntactically correct but at least one field contains an invalid value. */
  PropertyConstraintViolation,
  /**
   * Payload for Action is syntactically correct but at least one of the fields violates occurrence
   * constraints.
   */
  OccurenceConstraintViolation,
  /**
   * Payload for Action is syntactically correct but at least one of the fields violates data type
   * constraints (e.g. “somestring”: 12)
   */
  TypeConstraintViolation,
  /** Any other error not covered by the previous ones */
  GenericError
}
