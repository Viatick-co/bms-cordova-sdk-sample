package com.viatick.bmsandroidsdk.response;

public class SdkTokenResponse {

    private String token;
    private long expiration;

    public SdkTokenResponse() {}

    public SdkTokenResponse(String token, long expiration) {
        this.token = token;
        this.expiration = expiration;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

}
