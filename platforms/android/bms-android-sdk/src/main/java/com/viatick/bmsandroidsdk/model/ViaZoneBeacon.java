package com.viatick.bmsandroidsdk.model;

public class ViaZoneBeacon {

    private int deviceId;
    private String uuid;
    private int major;
    private int minor;

    public ViaZoneBeacon(int deviceId, String uuid, int major, int minor) {
        this.deviceId = deviceId;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

}
