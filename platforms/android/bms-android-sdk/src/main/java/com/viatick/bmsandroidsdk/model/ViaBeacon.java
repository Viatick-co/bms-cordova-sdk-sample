package com.viatick.bmsandroidsdk.model;

public class ViaBeacon {
  private BleBeacon bleBeacon;
  private long lastSeen;

  public ViaBeacon() {}

  public ViaBeacon(BleBeacon bleBeacon, long lastSeen) {
    this.bleBeacon = bleBeacon;
    this.lastSeen = lastSeen;
  }

  public BleBeacon getBleBeacon() {
    return bleBeacon;
  }

  public void setBleBeacon(BleBeacon bleBeacon) {
    this.bleBeacon = bleBeacon;
  }

  public long getLastSeen() {
    return lastSeen;
  }

  public void setLastSeen(long lastSeen) {
    this.lastSeen = lastSeen;
  }
}
