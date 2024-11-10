package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.OCCPMessage;

public class BootNotification  extends OCCPMessage {
    public String chargePointVendor;
    public String chargePointModel;
    public String chargePointSerialNumber;
    public String chargeBoxSerialNumber;
    public String firmwareVersion;
    public String iccid;
    public String imsi;
    public String meterType;
    public String meterSerialNumber;

    public BootNotification(String chargePointVendor,
                            String chargePointModel,
                            String chargePointSerialNumber,
                            String chargeBoxSerialNumber,
                            String firmwareVersion,
                            String iccid,
                            String imsi,
                            String meterType,
                            String meterSerialNumber) {
        this.chargePointVendor = chargePointVendor;
        this.chargePointModel = chargePointModel;
        this.chargePointSerialNumber = chargePointSerialNumber;
        this.chargeBoxSerialNumber = chargeBoxSerialNumber;
        this.firmwareVersion = firmwareVersion;
        this.iccid = iccid;
        this.imsi = imsi;
        this.meterType = meterType;
        this.meterSerialNumber = meterSerialNumber;
    }


}
