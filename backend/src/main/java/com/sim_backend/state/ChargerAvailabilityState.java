package com.sim_backend.state;

import com.sim_backend.websockets.enums.AvailabilityType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChargerAvailabilityState {
  private final Map<Integer, AvailabilityType> status = new ConcurrentHashMap<>();

  /**
   * Change the availability type for a given connectorID.
   *
   * @param connectorID the connectorID to change, 0 for all of them.
   * @param availability the new availability.
   */
  public void changeAvailability(int connectorID, AvailabilityType availability) {
    if (connectorID != 0) {
      status.put(connectorID, availability);
    } else {
      status.replaceAll((k, v) -> availability);
    }
  }

  /**
   * Get the current availability status for a connectorID.
   *
   * @param connectorID the connectorID, cannot be 0
   * @return The current availability.
   */
  public AvailabilityType getAvailabilityStatus(int connectorID) {
    if (connectorID > 0) {
      return status.get(connectorID);
    }
    return null;
  }
}
