package com.viatick.bmsandroidsdk.model;

import org.altbeacon.beacon.Beacon;

/**
 * Created by zanyk on 27/4/18.
 */

public class ViaIBeacon {
    private Beacon iBeacon;
    private double maxDistance;
    private boolean isRequested;
    private int disappearIdx;

    public ViaIBeacon() {

    }

    public ViaIBeacon(Beacon iBeacon, double maxDistance, boolean isRequested, int disappearIdx) {
        this.iBeacon = iBeacon;
        this.maxDistance = maxDistance;
        this.isRequested = isRequested;
        this.disappearIdx = disappearIdx;
    }

    public Beacon getiBeacon() {
        return iBeacon;
    }

    public void setiBeacon(Beacon iBeacon) {
        this.iBeacon = iBeacon;
    }

    public boolean isRequested() {
        return isRequested;
    }

    public void setRequested(boolean requested) {
        isRequested = requested;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public int getDisappearIdx() {
        return disappearIdx;
    }

    public void setDisappearIdx(int disappearIdx) {
        this.disappearIdx = disappearIdx;
    }

    public boolean same (ViaIBeacon viaIBeacon) {
        try {
            return viaIBeacon.getiBeacon().getId1().equals(iBeacon.getId1())
                    && viaIBeacon.getiBeacon().getId2().equals(iBeacon.getId2())
                    && viaIBeacon.getiBeacon().getId3().equals(iBeacon.getId3());
        } catch (Exception e) {
            return false;
        }
    }
}
