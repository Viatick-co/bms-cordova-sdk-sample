package com.viatick.bmsandroidsdk.model;

public class BleBeacon {
  private String uuid;
  private int major;
  private int minor;
  private double accuracy;

  public BleBeacon() {
  }

  public BleBeacon(String uuid, int major, int minor, double accuracy) {
    this.uuid = uuid;
    this.major = major;
    this.minor = minor;
    this.accuracy = accuracy;
  }

  public String getKey() {
    return uuid + "-" + major + "-" + minor;
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

  public double getAccuracy() {
    return accuracy;
  }

  public void setAccuracy(double accuracy) {
    this.accuracy = accuracy;
  }

  @Override
  public String toString() {
    return this.uuid + " " + this.major + " " + this.minor + " - " + this.accuracy;
  }
}
