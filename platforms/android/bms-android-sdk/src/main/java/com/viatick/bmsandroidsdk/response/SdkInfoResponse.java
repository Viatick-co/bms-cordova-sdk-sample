package com.viatick.bmsandroidsdk.response;

import com.viatick.bmsandroidsdk.model.ViaZone;

import java.util.HashMap;

public class SdkInfoResponse {
    private int fetchRate;
    private String uuidRegion;
    private String apiKey;
    private HashMap<Integer, ViaZone> zones;

    public SdkInfoResponse() {}

    public SdkInfoResponse(int fetchRate, String uuidRegion, String apiKey, HashMap<Integer, ViaZone> zones) {
        this.fetchRate = fetchRate;
        this.uuidRegion = uuidRegion;
        this.apiKey = apiKey;
        this.zones = zones;
    }

    public int getFetchRate() {
        return fetchRate;
    }

    public void setFetchRate(int fetchRate) {
        this.fetchRate = fetchRate;
    }

    public String getUuidRegion() {
        return uuidRegion;
    }

    public void setUuidRegion(String uuidRegion) {
        this.uuidRegion = uuidRegion;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public HashMap<Integer, ViaZone> getZones() {
        return zones;
    }

    public void setZones(HashMap<Integer, ViaZone> zones) {
        this.zones = zones;
    }
}
