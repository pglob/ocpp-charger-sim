package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.OCPPMessage;
import com.sim_backend.websockets.OCPPMessageInfo;

/**
 * A OCCP Boot Notification Message.
 */

@OCPPMessageInfo(messageName = "BootNotification")
public final class BootNotification  extends OCPPMessage {

    /**
     * The Charge Point's Vendor.
     */

    public final String chargePointVendor;

    /**
     * The Charge Point's Model.
     */
    public final String chargePointModel;

    /**
     * The Charge Point's Serial Number.
     */
    public final String chargePointSerialNumber;

    /**
     * The Charge Box's Serial Number.
     */
    public final String chargeBoxSerialNumber;

    /**
     * Firmware Version.
     */
    public final String firmwareVersion;

    /**
     * ICCID.
     */
    public final String iccid;

    /**
     * IMSI.
     */
    public final String imsi;

    /**
     * Meter Type.
     */
    public final String meterType;

    /**
     * Meter Serial Number.
      */
    public final String meterSerialNumber;

    /**
     * Create a Boot Notification Message.
     * @param chargePVendor The Charge Point Vendor.
     * @param chargePModel The Charge Point Model.
     * @param chargePSN THe Charge Point Serial Number.
     * @param chargeboxSN The Charge Box Serial Number.
     * @param fwversion The Firmware Version.
     * @param inICCID The ICCID.
     * @param inIMSI The IMSI.
     * @param mType The Meter Type.
     * @param mSN The Meter Serial Number.
     */
    public BootNotification(final String chargePVendor,
                            final String chargePModel,
                            final String chargePSN,
                            final String chargeboxSN,
                            final String fwversion,
                            final String inICCID,
                            final String inIMSI,
                            final String mType,
                            final String mSN) {
        this.chargePointVendor = chargePVendor;
        this.chargePointModel = chargePModel;
        this.chargePointSerialNumber = chargePSN;
        this.chargeBoxSerialNumber = chargeboxSN;
        this.firmwareVersion = fwversion;
        this.iccid = inICCID;
        this.imsi = inIMSI;
        this.meterType = mType;
        this.meterSerialNumber = mSN;
    }

}
