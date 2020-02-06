package com.viatick.bmsandroidsdk.model;


public class ViaProximity {
    private Integer proximityId;
    private long startTime;
    private BleBeacon beacon;
    private ViaBmsUtil.AttendanceStatus status;
    private long proximityTime;

    public ViaProximity() {
        this.startTime = 0;
        this.beacon = null;
        this.status = ViaBmsUtil.AttendanceStatus.CHECKOUT;
        this.proximityTime = 0;
    }

    public Integer getProximityId() {
        return proximityId;
    }

    public void setProximityId(Integer proximityId) {
        this.proximityId = proximityId;
    }

    public BleBeacon getBeacon() {
        return beacon;
    }

    public void setBeacon(BleBeacon beacon) {
        this.beacon = beacon;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public ViaBmsUtil.AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(ViaBmsUtil.AttendanceStatus status) {
        this.status = status;
    }

    public long getProximityTime() {
        return proximityTime;
    }

    public void setProximityTime(long proximityTime) {
        this.proximityTime = proximityTime;
    }
}
