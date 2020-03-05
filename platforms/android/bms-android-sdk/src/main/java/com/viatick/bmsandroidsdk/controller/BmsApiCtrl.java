package com.viatick.bmsandroidsdk.controller;

import android.util.Log;

import com.viatick.bmsandroidsdk.helper.BmsEnvironment;
import com.viatick.bmsandroidsdk.model.BleBeacon;
import com.viatick.bmsandroidsdk.model.IBeacon;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil.MinisiteType;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil.ViaCustomer;
import com.viatick.bmsandroidsdk.model.ViaMinisite;
import com.viatick.bmsandroidsdk.model.ViaZone;
import com.viatick.bmsandroidsdk.model.ViaZoneBeacon;
import com.viatick.bmsandroidsdk.response.SdkInfoResponse;
import com.viatick.bmsandroidsdk.response.SdkTokenResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BmsApiCtrl {
    private static final String TAG = "[VIATICK]";
    private static final MediaType APPLICATION_JSON
            = MediaType.parse("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient();
    private static final Request.Builder requestBuilder = new Request.Builder();

    private static String API_URL = "https://bms-api.viatick.com";
    private static String API_ENDPOINT = API_URL + "/dev";
    private static final String SDK_TOKEN_PATH = "/sdk/oauth2/token";
    private static final String SDK_PATH = "/api/restful";
    private static final String CORE_TRACKING_PATH = "/api/core/tracking";
    private static final String CORE_MQTT_TRACKING_PATH = "/api/core/mqtt/tracking";

    private static final String GRANT_TYPE_CREDENTIAL = "client_credentials";

    public static void initApi(BmsEnvironment environment) {
        switch (environment) {
            case DEV:
                API_ENDPOINT = API_URL + "/dev";
                break;
            case PROD:
                API_ENDPOINT = API_URL + "/main";
                break;
            case CHINA:
                API_ENDPOINT = API_URL + "/cn";
                break;
        }
    }

    public static SdkTokenResponse getSdkToken(String sdkKey) {
        String url = API_ENDPOINT + SDK_TOKEN_PATH;

        Headers headers = new Headers.Builder()
                .add("grant_type", GRANT_TYPE_CREDENTIAL)
                .add("scope", sdkKey).build();

        RequestBody body = RequestBody.create(APPLICATION_JSON, "{}");
        Request request = requestBuilder.headers(headers).url(url).post(body).build();


        try {
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();

            if (success) {
                String bodyResponse = response.body().string();
                JSONObject responseObject = new JSONObject(bodyResponse);
                String token = responseObject.getString("access_token");
                long expirationTime = System.currentTimeMillis() + 3000000;

                return new SdkTokenResponse(token, expirationTime);
            }

        } catch (Exception e) {
        }

        return null;
    }

    public static SdkInfoResponse getSdkInfo(String sdkToken) {
        String url = API_ENDPOINT + SDK_PATH;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("field", "sdkGetInfo");
            jsonObject.put("authorization", "Bearer " + sdkToken);
        } catch (Exception e) {
        }

        RequestBody body = RequestBody.create(APPLICATION_JSON, jsonObject.toString());
        Request request = requestBuilder.url(url).post(body).build();

        try {
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();

            if (success) {
                String bodyResponse = response.body().string();

                JSONObject responseObject = new JSONObject(bodyResponse);

                Log.i(TAG, "responseObject: " + responseObject);

                if (!responseObject.isNull("sdkBroadcastUUID")) {
                    String sdkBroadcastUUID = responseObject.getString("sdkBroadcastUUID");
                    ViaBmsCtrl.SETTING.setBeaconRegionUUID(sdkBroadcastUUID);
                }

                if (!responseObject.isNull("distance")) {
                    JSONObject rangeObject = responseObject.getJSONObject("distance");

                    Log.i(TAG, "distanceObject: " + rangeObject);
                    if (!rangeObject.isNull("iBeacon")) {
                        JSONArray iBeaconArr = rangeObject.getJSONArray("iBeacon");

                        for (int i = 0;i < iBeaconArr.length();i++) {
                            JSONObject iBeaconObject = null;
                            try {
                                iBeaconObject = iBeaconArr.getJSONObject(i);

                                String uuid = iBeaconObject.getString("uuid");
                                int major = iBeaconObject.getInt("major");
                                int minor = iBeaconObject.getInt("minor");
                                double distance = iBeaconObject.getDouble("distance");

                                BleBeacon iBeacon = new BleBeacon(uuid, major, minor, distance);

                                String key = iBeacon.getUuid().toLowerCase() + "-" + iBeacon.getMajor() + "-" + iBeacon.getMinor();

                                Log.i(TAG, "key: " + key);
                                ViaBmsCtrl.OWNED_BEACONS.put(key, iBeacon);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                int fetchRate = responseObject.getInt("fetchRate");
                String apiKeyHash = responseObject.getString("apiKeyHash");
                HashMap<Integer, ViaZone> zones = new HashMap<>();

                if (!responseObject.isNull("range")) {
                    JSONObject rangeObject = responseObject.getJSONObject("range");

                    if (!rangeObject.isNull("iBeacon")) {
                        JSONObject iBeaconObject = rangeObject.getJSONObject("iBeacon");
                        if (!iBeaconObject.isNull("uuid")) {
                            String uuidRegion = iBeaconObject.getString("uuid");

                            if (!responseObject.isNull("zones")) {
                                JSONArray zoneArray = responseObject.getJSONArray("zones");

                                int len = zoneArray.length();
                                for (int i = 0; i < len; i++) {
                                    JSONObject zoneObject = zoneArray.getJSONObject(i);

                                    int zoneId = zoneObject.getInt("zoneId");
                                    String name = null;
                                    if (zoneObject.isNull("name")) {
                                        name = zoneObject.getString("name");;
                                    }
                                    String remark = null;
                                    if (zoneObject.isNull("remark")) {
                                        remark = zoneObject.getString("remark");
                                    }
                                    String image = null;
                                    if (zoneObject.isNull("image")) {
                                        image = zoneObject.getString("image");
                                    }

                                    List<ViaZoneBeacon> beacons = new ArrayList<>();

                                    if (!zoneObject.isNull("iBeacons")) {
                                        JSONArray beaconArray = zoneObject.getJSONArray("iBeacons");

                                        for (int j = 0, beaconLen = beaconArray.length(); j < beaconLen; j++) {
                                            JSONObject beaconObject = beaconArray.getJSONObject(j);

                                            int deviceId = beaconObject.getInt("deviceId");
                                            String uuid = beaconObject.getString("uuid");
                                            int major = beaconObject.getInt("major");
                                            int minor = beaconObject.getInt("minor");

                                            ViaZoneBeacon beacon = new ViaZoneBeacon(deviceId, uuid, major, minor);
                                            beacons.add(beacon);
                                        }
                                    }

                                    zones.put(zoneId, new ViaZone(zoneId, name, remark, image, beacons));
                                }
                            }

                            return new SdkInfoResponse(fetchRate, uuidRegion, apiKeyHash, zones);
                        } else {
                            return new SdkInfoResponse(fetchRate, null, apiKeyHash, zones);
                        }
                    } else {
                        return new SdkInfoResponse(fetchRate, null, apiKeyHash, zones);
                    }
                } else {
                    return new SdkInfoResponse(fetchRate, null, apiKeyHash, zones);
                }
            }

        } catch (Exception e) {
        }

        return null;
    }

    public static ViaCustomer processCustomer(String sdkToken, String identifier, String phone, String email,
                                                  String remark, String os, List<ViaZone> authorizedZones,
                                              Boolean broadcasting) {

        String url = API_ENDPOINT + SDK_PATH;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("field", "sdkProcessCustomer");
            jsonObject.put("authorization", "Bearer " + sdkToken);

            JSONObject argumentObject = new JSONObject();
            argumentObject.put("identifier", identifier);
            argumentObject.put("phone", phone);
            argumentObject.put("email", email);
            argumentObject.put("remark", remark);
            argumentObject.put("os", os);

            if (authorizedZones != null) {
                JSONArray zoneArray = new JSONArray();

                for (ViaZone viaZone: authorizedZones) {
                    int zoneId = viaZone.getZoneId();

                    JSONObject zoneObj = new JSONObject();
                    zoneObj.put("zoneId", zoneId);

                    zoneArray.put(zoneObj);
                }

                argumentObject.put("authorizedZones", zoneArray);
            }

            argumentObject.put("broadcasting", broadcasting);

            jsonObject.put("arguments", argumentObject);
        } catch (Exception e) {
        }

        RequestBody body = RequestBody.create(APPLICATION_JSON, jsonObject.toString());
        Request request = requestBuilder.url(url).post(body).build();

        try {
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();

            if (success) {
                String bodyResponse = response.body().string();

                JSONObject responseObject = new JSONObject(bodyResponse);
                Log.i(TAG, "responseObject: " + responseObject);

                if (!responseObject.isNull("customerId")) {
                    int customerId = responseObject.getInt("customerId");

                    String uuid = null;
                    int major = 0;
                    int minor = 0;

                    if (!responseObject.isNull("uuid")) {
                        uuid = responseObject.getString("uuid");
                        major = responseObject.getInt("major");
                        minor = responseObject.getInt("minor");
                    }
                    return new ViaCustomer(customerId, identifier, email, phone, remark, os, uuid, major, minor);
                }
            }
        } catch (Exception e) {
        }

        return null;
    }

    public static ViaMinisite getMinisite(String sdkToken, String uuid, int major, int minor) {
        String apiURL = API_ENDPOINT + SDK_PATH;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("field", "sdkSite");
            jsonObject.put("authorization", "Bearer " + sdkToken);

            JSONObject argumentObject = new JSONObject();
            argumentObject.put("id1", uuid);
            argumentObject.put("id2", major);
            argumentObject.put("id3", minor);
            jsonObject.put("arguments", argumentObject);
        } catch (Exception e) {
        }

        RequestBody body = RequestBody.create(APPLICATION_JSON, jsonObject.toString());
        Request request = requestBuilder.url(apiURL).post(body).build();

        try {
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();

            if (success) {
                String bodyResponse = response.body().string();

                JSONObject responseObject = new JSONObject(bodyResponse);
                int id = responseObject.getInt("minisiteId");
                String url = responseObject.getString("url");
                String title = responseObject.getString("title");
                String description = responseObject.getString("description");

                String deepLink = null;
                if (!responseObject.isNull("deepLinkAndroid")) {
                    deepLink = responseObject.getString("deepLinkAndroid");
                }

                String cover = null;
                if (!responseObject.isNull("cover")) {
                    JSONObject coverObject = responseObject.getJSONObject("cover");
                    cover = coverObject.getString("url");
                }

                String notificationImage = null;
                if (!responseObject.isNull("notificationImage")) {
                    JSONObject notificationObject = responseObject.getJSONObject("notificationImage");
                    notificationImage = notificationObject.getString("url");
                }

                int beacon = responseObject.getInt("beacon");

                MinisiteType type = MinisiteType.ADVERTISEMENT;

                if (!responseObject.isNull("coupon")) {
                    type = MinisiteType.COUPON;
                } else if (deepLink != null) {
                    type = MinisiteType.DEEP_LINK;
                } else if (!responseObject.isNull("voting")) {
                    type = MinisiteType.VOTING;
                } else if (!responseObject.isNull("polling")) {
                    type = MinisiteType.POLLING;
                }

                return new ViaMinisite(id, title, description, cover, url, type,
                        deepLink, notificationImage, beacon);
            }
        } catch (Exception e) {
        }

        return null;
    }

    public static Integer createSessionLog(String sdkToken, int customer, int minisite, int beacon) {
        String apiURL = API_ENDPOINT + SDK_PATH;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("field", "createSessionLog");
            jsonObject.put("authorization", "Bearer " + sdkToken);

            JSONObject inputObject = new JSONObject();
            inputObject.put("customer", customer);
            inputObject.put("minisite", minisite);
            inputObject.put("beacon", beacon);

            JSONObject argumentObject = new JSONObject();
            argumentObject.put("input", inputObject);

            jsonObject.put("arguments", argumentObject);
        } catch (Exception e) {
        }

        RequestBody body = RequestBody.create(APPLICATION_JSON, jsonObject.toString());
        Request request = requestBuilder.url(apiURL).post(body).build();

        try {
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();

            if (success) {
                String bodyResponse = response.body().string();

                JSONObject responseObject = new JSONObject(bodyResponse);
                int sessionLogId = responseObject.getInt("sessionLogId");

                return sessionLogId;
            }
        } catch (Exception e) {
        }

        return null;
    }

    public static void updateSessionLog(String sdkToken, int sessionLogId) {
        String apiURL = API_ENDPOINT + SDK_PATH;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("field", "updateSessionLog");
            jsonObject.put("authorization", "Bearer " + sdkToken);

            JSONObject inputObject = new JSONObject();
            inputObject.put("sessionLogId", sessionLogId);
            inputObject.put("ended", true);

            JSONObject argumentObject = new JSONObject();
            argumentObject.put("input", inputObject);
            jsonObject.put("arguments", argumentObject);
        } catch (Exception e) {
        }

        RequestBody body = RequestBody.create(APPLICATION_JSON, jsonObject.toString());
        Request request = requestBuilder.url(apiURL).post(body).build();

        try {
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();
//
//            if (success) {
//                String bodyResponse = response.body().string();
//
//                JSONObject responseObject = new JSONObject(bodyResponse);
//            }
        } catch (Exception e) {
        }

//        return null;
    }

    public static boolean checkVoting(String sdkToken, int customer, int minisite) {
        String apiURL = API_ENDPOINT + SDK_PATH;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("field", "getVotingTokens");
            jsonObject.put("authorization", "Bearer " + sdkToken);

            JSONObject argumentObject = new JSONObject();
            argumentObject.put("customer", customer);
            argumentObject.put("minisite", minisite);

            jsonObject.put("arguments", argumentObject);
        } catch (Exception e) {
        }

        RequestBody body = RequestBody.create(APPLICATION_JSON, jsonObject.toString());
        Request request = requestBuilder.url(apiURL).post(body).build();

        try {
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();

            if (success) {
                String bodyResponse = response.body().string();

                JSONArray responseObject = new JSONArray(bodyResponse);

                return responseObject.length() <= 0;
            }
        } catch (Exception e) {
        }

        return false;
    }

    public static boolean checkPolling(String sdkToken, int customer, int minisite) {
        String apiURL = API_ENDPOINT + SDK_PATH;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("field", "getPollingTokens");
            jsonObject.put("authorization", "Bearer " + sdkToken);

            JSONObject argumentObject = new JSONObject();
            argumentObject.put("customer", customer);
            argumentObject.put("minisite", minisite);

            jsonObject.put("arguments", argumentObject);
        } catch (Exception e) {
        }

        RequestBody body = RequestBody.create(APPLICATION_JSON, jsonObject.toString());
        Request request = requestBuilder.url(apiURL).post(body).build();

        try {
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();

            if (success) {
                String bodyResponse = response.body().string();

                JSONArray responseObject = new JSONArray(bodyResponse);
                return responseObject.length() <= 0;
            }
        } catch (Exception e) {
        }

        return false;
    }

    public static void coreTracking(String apiKey, String uuid, int major, int minor, String identifier,
                                     String phone, String email, String remark, double distance) {
        String apiURL = API_ENDPOINT + CORE_TRACKING_PATH;

        Headers headers = new Headers.Builder()
                .add("Api-Key", apiKey).build();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("major", major);
            jsonObject.put("minor", minor);
            jsonObject.put("identifier", identifier);
            jsonObject.put("phone", phone);
            jsonObject.put("email", email);
            jsonObject.put("remark", remark);
            jsonObject.put("distance", distance);
            jsonObject.put("data", new JSONObject());
        } catch (Exception e) {
        }

        RequestBody body = RequestBody.create(APPLICATION_JSON, jsonObject.toString());
        Request request = requestBuilder.headers(headers).url(apiURL).post(body).build();

        try {
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();
        } catch (Exception e) {
        }
    }

    public static void coreTrackingWithMQTT(String apiKey, String uuid, int major, int minor, String identifier,
                                    String phone, String email, String remark, double distance) {

        String url = API_ENDPOINT + CORE_MQTT_TRACKING_PATH;

        Headers headers = new Headers.Builder()
                .add("Api-Key", apiKey).build();

        Log.i(TAG, "coreTrackingWithMQTT: " + url + " " + apiKey);


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("major", major);
            jsonObject.put("minor", minor);
            jsonObject.put("identifier", identifier);
            jsonObject.put("phone", phone);
            jsonObject.put("email", email);
            jsonObject.put("remark", remark);
            jsonObject.put("distance", distance);
        } catch (Exception e) {
        }

        RequestBody body = RequestBody.create(APPLICATION_JSON, jsonObject.toString());
        Request request = requestBuilder.headers(headers).url(url).post(body).build();

        try {
            Response response = client.newCall(request).execute();

            Log.i(TAG, "coreTrackingWithMQTT response: " + response.body().string());
            boolean success = response.isSuccessful();
        } catch (Exception e) {
        }

    }

    public static List<Integer> getAuthorizedZones(String sdkToken, String identifier) {
        List<Integer> ids = new ArrayList<>();

        String apiURL = API_ENDPOINT + SDK_PATH;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("field", "sdkGetCustomer");
            jsonObject.put("authorization", "Bearer " + sdkToken);

            JSONObject argumentObject = new JSONObject();
            argumentObject.put("identifier", identifier);

            jsonObject.put("arguments", argumentObject);
        } catch (Exception e) {
        }

        RequestBody body = RequestBody.create(APPLICATION_JSON, jsonObject.toString());
        Request request = requestBuilder.url(apiURL).post(body).build();

        try {
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();

            if (success) {
                String bodyResponse = response.body().string();
                JSONObject responseObject = new JSONObject(bodyResponse);

                if (!responseObject.isNull("authorizedZones")) {
                    JSONArray zoneArray = responseObject.getJSONArray("authorizedZones");

                    for (int i = 0, len = zoneArray.length(); i < len; i++) {
                        JSONObject zoneObj = zoneArray.getJSONObject(i);

                        int zoneId = zoneObj.getInt("zoneId");
                        ids.add(zoneId);
                    }
                }
            }
        } catch (Exception e) {
        }

        return ids;
    }

    public static Integer checkin(String sdkToken, int customer, String time) {
        String apiURL = API_ENDPOINT + SDK_PATH;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("field", "createAttendance");
            jsonObject.put("authorization", "Bearer " + sdkToken);

            JSONObject inputObject = new JSONObject();
            inputObject.put("customer", customer);
            inputObject.put("checkIn", time);

            JSONObject argumentObject = new JSONObject();
            argumentObject.put("input", inputObject);

            jsonObject.put("arguments", argumentObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(APPLICATION_JSON, jsonObject.toString());
        Request request = requestBuilder.url(apiURL).post(body).build();

        try {
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();


            if (success) {
                String bodyResponse = response.body().string();
                JSONObject responseObject = new JSONObject(bodyResponse);

                if (!responseObject.isNull("attendanceId")) {
                    return responseObject.getInt("attendanceId");
                }
            }
        } catch (Exception e) {
        }

        return null;
    }

    public static Integer createCustomerAlert(String sdkToken, int customer, String uuid,
                                              int major, int minor) {
        String apiURL = API_ENDPOINT + SDK_PATH;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("field", "createCustomerAlert");
            jsonObject.put("authorization", "Bearer " + sdkToken);

            JSONObject inputObject = new JSONObject();
            inputObject.put("customer", customer);
            inputObject.put("uuid", uuid);
            inputObject.put("major", major);
            inputObject.put("minor", minor);
            inputObject.put("type", "proximity");

            JSONObject argumentObject = new JSONObject();
            argumentObject.put("input", inputObject);

            jsonObject.put("arguments", argumentObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(APPLICATION_JSON, jsonObject.toString());
        Request request = requestBuilder.url(apiURL).post(body).build();

        try {
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();


            if (success) {
                String bodyResponse = response.body().string();
                JSONObject responseObject = new JSONObject(bodyResponse);

                if (!responseObject.isNull("customerAlertId")) {
                    return responseObject.getInt("customerAlertId");
                }
            }
        } catch (Exception e) {
        }

        return null;
    }


    public static boolean checkout(String sdkToken, int attendanceId, String time) {
        List<Integer> ids = new ArrayList<>();

        String apiURL = API_ENDPOINT + SDK_PATH;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("field", "updateAttendance");
            jsonObject.put("authorization", "Bearer " + sdkToken);

            JSONObject inputObject = new JSONObject();
            inputObject.put("attendanceId", attendanceId);
            inputObject.put("checkOut", time);

            JSONObject argumentObject = new JSONObject();
            argumentObject.put("input", inputObject);

            jsonObject.put("arguments", argumentObject);
        } catch (Exception e) {
        }

        RequestBody body = RequestBody.create(APPLICATION_JSON, jsonObject.toString());
        Request request = requestBuilder.url(apiURL).post(body).build();

        try {
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();

            if (success) {
                String bodyResponse = response.body().string();
                JSONObject responseObject = new JSONObject(bodyResponse);

                return !responseObject.isNull("attendanceId");
            }
        } catch (Exception e) {
        }

        return false;
    }

    public static ViaMinisite deviceSite(String sdkToken, String serial, String code,
                                         boolean hasLocation, double latitude, double longitude) {
        String apiURL = API_ENDPOINT + SDK_PATH;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("field", "deviceSite");
            jsonObject.put("authorization", "Bearer " + sdkToken);

            JSONObject argumentObject = new JSONObject();
            argumentObject.put("serial", serial);
            argumentObject.put("code", code);

            if (hasLocation) {
                argumentObject.put("latitude", latitude);
                argumentObject.put("longitude", longitude);
            }

            jsonObject.put("arguments", argumentObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(APPLICATION_JSON, jsonObject.toString());
        Request request = requestBuilder.url(apiURL).post(body).build();

        try {
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();

            if (success) {
                String bodyResponse = response.body().string();
                Log.i(TAG, "bodyResponse response: " + bodyResponse);
                JSONObject responseObject = new JSONObject(bodyResponse);

                int id = responseObject.getInt("minisiteId");
                String url = responseObject.getString("url");
                String title = responseObject.getString("title");
                String description = responseObject.getString("description");

                String deepLink = null;
                if (!responseObject.isNull("deepLinkAndroid")) {
                    deepLink = responseObject.getString("deepLinkAndroid");
                }

                String cover = null;
                if (!responseObject.isNull("cover")) {
                    JSONObject coverObject = responseObject.getJSONObject("cover");
                    cover = coverObject.getString("url");
                }

                String notificationImage = null;
                if (!responseObject.isNull("notificationImage")) {
                    JSONObject notificationObject = responseObject.getJSONObject("notificationImage");
                    notificationImage = notificationObject.getString("url");
                }

                return new ViaMinisite(id, title, description, cover, url, null,
                        deepLink, notificationImage, -1);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
