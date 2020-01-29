package com.viatick.bmsandroidsdk.model;

import com.viatick.bmsandroidsdk.helper.BmsEnvironment;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil.MinisiteViewType;

public class ViaSetting {

    private boolean enableAlert;
    private boolean enableBackground;
    private boolean enableSite;
    private MinisiteViewType minisitesView;
    private int autoSiteDuration;
    private boolean enableAttendance;
    private int checkinDuration;
    private int checkoutDuration;
    private boolean enableTracking;
    private boolean enableMQTT;
    private boolean enableDistance;
    private BmsEnvironment bmsEnvironment;

    public ViaSetting() {
        this.enableAlert = false;
        this.enableBackground = false;
        this.enableSite = false;
        this.minisitesView = ViaBmsUtil.MinisiteViewType.LIST;
        this.autoSiteDuration = 0;
        this.enableAttendance = false;
        this.checkinDuration = 15;
        this.checkoutDuration = 15;
        this.enableTracking = false;
        this.enableMQTT = true;
        this.enableDistance = false;
        this.bmsEnvironment = BmsEnvironment.DEV;
    }

    public boolean isEnableAlert() {
        return enableAlert;
    }

    public void setEnableAlert(boolean enableAlert) {
        this.enableAlert = enableAlert;
    }

    public boolean isEnableBackground() {
        return enableBackground;
    }

    public void setEnableBackground(boolean enableBackground) {
        this.enableBackground = enableBackground;
    }

    public boolean isEnableSite() {
        return enableSite;
    }

    public void setEnableSite(boolean enableSite) {
        this.enableSite = enableSite;
    }

    public MinisiteViewType getMinisitesView() {
        return minisitesView;
    }

    public void setMinisitesView(MinisiteViewType minisitesView) {
        this.minisitesView = minisitesView;
    }

    public int getAutoSiteDuration() {
        return autoSiteDuration;
    }

    public void setAutoSiteDuration(int autoSiteDuration) {
        this.autoSiteDuration = autoSiteDuration;
    }

    public boolean isEnableAttendance() {
        return enableAttendance;
    }

    public void setEnableAttendance(boolean enableAttendance) {
        this.enableAttendance = enableAttendance;
    }

    public int getCheckinDuration() {
        return checkinDuration;
    }

    public void setCheckinDuration(int checkinDuration) {
        this.checkinDuration = checkinDuration;
    }

    public int getCheckoutDuration() {
        return checkoutDuration;
    }

    public void setCheckoutDuration(int checkoutDuration) {
        this.checkoutDuration = checkoutDuration;
    }

    public boolean isEnableTracking() {
        return enableTracking;
    }

    public void setEnableTracking(boolean enableTracking) {
        this.enableTracking = enableTracking;
    }

    public boolean isEnableMQTT() {
        return enableMQTT;
    }

    public void setEnableMQTT(boolean enableMQTT) {
        this.enableMQTT = enableMQTT;
    }

    public BmsEnvironment getBmsEnvironment() {
        return bmsEnvironment;
    }

    public void setBmsEnvironment(BmsEnvironment bmsEnvironment) {
        this.bmsEnvironment = bmsEnvironment;
    }

    public void setEnableDistance(boolean enableDistance) {
        this.enableDistance = enableDistance;
    }


    public boolean isEnableDistance() {
        return enableDistance;
    }
}
