package com.viatick.bmsandroidsdk.model;

import java.util.List;

public class ViaZone {

    private int zoneId;
    private String name;
    private String remark;
    private String image;
    private List<ViaZoneBeacon> beacons;

    public ViaZone() {}

    public ViaZone(int zoneId, String name, String remark, String image, List<ViaZoneBeacon> beacons) {
        this.zoneId = zoneId;
        this.name = name;
        this.remark = remark;
        this.image = image;
        this.beacons = beacons;
    }

    public int getZoneId() {
        return zoneId;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<ViaZoneBeacon> getBeacons() {
        return beacons;
    }

    public void setBeacons(List<ViaZoneBeacon> beacons) {
        this.beacons = beacons;
    }
}
