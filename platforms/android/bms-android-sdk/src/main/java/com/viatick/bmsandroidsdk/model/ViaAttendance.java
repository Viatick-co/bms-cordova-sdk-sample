package com.viatick.bmsandroidsdk.model;


public class ViaAttendance {
    private Integer attendanceId;
    private long firstAttendance;
    private long attendanceTime;
    private ViaBmsUtil.AttendanceStatus status;

    public ViaAttendance() {
        this.firstAttendance = 0;
        this.attendanceTime = 0;
        this.status = ViaBmsUtil.AttendanceStatus.CHECKOUT;
    }

    public Integer getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Integer attendanceId) {
        this.attendanceId = attendanceId;
    }

    public long getFirstAttendance() {
        return firstAttendance;
    }

    public void setFirstAttendance(long firstAttendance) {
        this.firstAttendance = firstAttendance;
    }

    public long getAttendanceTime() {
        return attendanceTime;
    }

    public void setAttendanceTime(long attendanceTime) {
        this.attendanceTime = attendanceTime;
    }

    public ViaBmsUtil.AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(ViaBmsUtil.AttendanceStatus status) {
        this.status = status;
    }
}
