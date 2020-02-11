package com.viatick.bmsandroidsdk.model;

public class IBeacon {
    private String uuid;
    private int major;
    private int minor;
    private double distance;

    public IBeacon(String uuid, int major, int minor) {
        this.uuid = uuid.toUpperCase();
        this.major = major;
        this.minor = minor;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid.toUpperCase();
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getKey() {
        return this.uuid + "-" + this.major + "-" + this.minor;
    }
}
