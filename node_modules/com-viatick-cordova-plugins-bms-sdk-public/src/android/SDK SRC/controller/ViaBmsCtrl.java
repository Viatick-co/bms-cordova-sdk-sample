package com.viatick.bmsandroidsdk.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.viatick.bmsandroidsdk.helper.ViaBeaconHelper;
import com.viatick.bmsandroidsdk.helper.ViaInterfaces;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil;
import com.viatick.bmsandroidsdk.model.ViaIBeacon;
import com.viatick.bmsandroidsdk.model.ViaMinisite;
import com.viatick.bmsandroidsdk.view.MinisiteActivity;
import com.viatick.bmsandroidsdk.view.MinisiteMenuActivity;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by zanyk on 27/4/18.
 */

public class ViaBmsCtrl {

    private static final String TAG = "[VIATICK]";
    private String SDK_KEY;
    private String API_KEY;
    private ViaBmsUtil.ViaSetting SETTING = new ViaBmsUtil.ViaSetting();
    private ViaBmsUtil.ViaCustomer CUSTOMER = new ViaBmsUtil.ViaCustomer();
    private ViaBmsUtil.ViaIBeaconRegion IBEACON_REGION = new ViaBmsUtil.ViaIBeaconRegion();
    private ViaBmsUtil.ViaEddystoneRegion EDDY_REGION = new ViaBmsUtil.ViaEddystoneRegion();
    private ViaBmsUtil.ViaFetchInterval FETCH_INTERVAL = new ViaBmsUtil.ViaFetchInterval();

    ViaApiCtrl viaApiCtrl = new ViaApiCtrl();

    private RequestQueue queue;
    private LocalBroadcastManager broadcaster;
    private int notificationCount = 0;
    private ArrayList<ViaMinisite> MINISITES = new ArrayList<>();
    private Context context;

    public HashMap<Long,Integer> miniSiteIdMapping = new HashMap<>();
    public HashMap<Long,Integer> notiIdMapping = new HashMap<>();
    public HashMap<Long,Integer> beaconPushMapping = new HashMap<>();

    ViaIBeaconCtrl viaIBeaconCtrl;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
//            Log.i(TAG, "Service Connected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ViaIBeaconCtrl.LocalBinder binder = (ViaIBeaconCtrl.LocalBinder) service;
            viaIBeaconCtrl = binder.getService();
            viaIBeaconCtrl.setMBound(true);
            viaIBeaconCtrl.setViaBmsCtrlCallback(new ViaBmsCtrlCallback());

            onViaBeaconServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            viaIBeaconCtrl.setMBound(false);
        }
    };

    public void onViaBeaconServiceConnected() {

    }

    public void initSdk(final Activity context, String sdkKey, final CallbackContext callbackContext) {
        try {
            this.context = context;
            SDK_KEY = sdkKey;
            queue = Volley.newRequestQueue(context);
            // Init LocalBroadcastManager
            broadcaster = LocalBroadcastManager.getInstance(context);

            permissionCheck(context);

            Intent viaBeaconIntent = new Intent(context, ViaIBeaconCtrl.class);
            context.startService(viaBeaconIntent);

            context.bindService(viaBeaconIntent, mConnection, BIND_AUTO_CREATE);

            JSONObject inputJson = new JSONObject();
            inputJson.put("_", (Calendar.getInstance().getTime().getTime()));
            Map<String, String> headers = new HashMap<String, String>();
            headers.put(ViaBmsUtil.ViaHeaderKey.SDK_KEY, sdkKey);

            viaApiCtrl.sendGetRequest(queue, viaApiCtrl.SDK_ENDPOINT + viaApiCtrl.APP_HANDSHAKE, new ViaInterfaces.ViaCallbackInterface() {
                @Override
                public void doWhenResponse(JSONObject result) {
                    try {
                        initSdkHandler(context, result, callbackContext);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error(e.getMessage());
                    }
                }
            }, inputJson, new ArrayList<String>(), headers);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    public void permissionCheck (Activity activity) {
        int accessLocationCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (accessLocationCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ViaBmsUtil.ViaConstants.MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        } else if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    ViaBmsUtil.ViaConstants.MY_REQUEST_READ_PHONE_STATE);
        } else {

        }
    }

    public void onRequestPermissionsResult(Activity activity, int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ViaBmsUtil.ViaConstants.MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted!
                    int readphoneStateCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE);

                    if (readphoneStateCheck != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, ViaBmsUtil.ViaConstants.MY_REQUEST_READ_PHONE_STATE);
                    } else {
                        ViaBmsUtil.ViaPermission.allowReadPhoneState = true;
                    }

                } else {
                    // permission denied!
                }
                return;
            }
            case ViaBmsUtil.ViaConstants.MY_REQUEST_READ_PHONE_STATE: {
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    ViaBmsUtil.ViaPermission.allowReadPhoneState = true;
                } else {
                    ViaBmsUtil.ViaPermission.allowReadPhoneState = false;
                }
                break;
            }
            default:
                break;
        }
    }

    public void settings(boolean alert, boolean background, boolean site, boolean attendance, boolean tracking,
                         CallbackContext callbackContext) {
        SETTING.enableAlert = alert;
        SETTING.enableBackground = background;
        SETTING.enableSite = site;
        SETTING.enableAttendance = attendance;
        SETTING.enableTracking = tracking;

        callbackContext.success("");
    }

    public void initCustomer(String identifier, String phone, String email, CallbackContext callbackContext) {
        CUSTOMER.identifier = identifier;
        CUSTOMER.email = email;
        CUSTOMER.phone = phone;

        callbackContext.success("");
    }

    public void initSdkHandler(Context context, JSONObject data, CallbackContext callbackContext) throws JSONException {
        API_KEY = (String) data.get(ViaBmsUtil.ViaKey.APIKEY);
//        Log.i(TAG, "API_KEY: " + API_KEY);

        JSONObject range = (JSONObject) data.get(ViaBmsUtil.ViaKey.RANGE);

        if (range != null) {
            JSONObject ibeacon = (JSONObject) range.get(ViaBmsUtil.ViaKey.IBEACON);

            if (ibeacon != null) {
                String uuid = (String) ibeacon.get(ViaBmsUtil.ViaKey.UUID);
//                Log.i(TAG,"UUID: " + uuid);
                IBEACON_REGION.uuid = uuid;

                if (ibeacon.has(ViaBmsUtil.ViaKey.MAJOR)) {
                    int major = (int) ibeacon.get(ViaBmsUtil.ViaKey.MAJOR);
                    IBEACON_REGION.major = major;
                }

                if (ibeacon.has(ViaBmsUtil.ViaKey.MINOR)) {
                    int minor = (int) ibeacon.get(ViaBmsUtil.ViaKey.MINOR);
                    IBEACON_REGION.minor = minor;
                }
            }

            JSONObject eddystone = (JSONObject) range.get(ViaBmsUtil.ViaKey.EDDYSTONE);

            if (eddystone != null) {
                if (eddystone.has(ViaBmsUtil.ViaKey.NAMESPACE)) {
                    String namespace = (String) eddystone.get(ViaBmsUtil.ViaKey.NAMESPACE);
                    EDDY_REGION.namespace = namespace;
                }

                if (eddystone.has(ViaBmsUtil.ViaKey.INSTANCE)) {
                    String instance = (String) eddystone.get(ViaBmsUtil.ViaKey.INSTANCE);
                    EDDY_REGION.instance = instance;
                }
            }
        }

        JSONObject fetchRate = (JSONObject) data.get(ViaBmsUtil.ViaKey.FETCHRATE);

        if (fetchRate != null) {
            int attendance = (int) fetchRate.get(ViaBmsUtil.ViaKey.ATTENDANCE);
            int tracking = (int) fetchRate.get(ViaBmsUtil.ViaKey.TRACKING);
            FETCH_INTERVAL.attendance = attendance;
            FETCH_INTERVAL.tracking = tracking;
        }

        exchangeCustomer(context);

        callbackContext.success("");
    }

     public void exchangeCustomer(Context context) throws JSONException {
        JSONObject inputJson = new JSONObject();
        inputJson.put(ViaBmsUtil.ViaKey.IDENTIFIER, CUSTOMER.identifier == "" ? getDeviceId(context) : CUSTOMER.identifier);
        inputJson.put(ViaBmsUtil.ViaKey.PHONE, CUSTOMER.phone);
        inputJson.put(ViaBmsUtil.ViaKey.EMAIL, CUSTOMER.email);


         String name = Build.DEVICE;
         String model = getDeviceName().replaceAll("-", "");
         String system = ViaBmsUtil.ViaValue.ANDROID;
         String version = System.getProperty("os.version").replaceAll("-","");
         inputJson.put(ViaBmsUtil.ViaKey.REMARK, name + " : " + model + " : " + system + " : " + version);

         String url = viaApiCtrl.SDK_ENDPOINT + viaApiCtrl.CORE_CUSTOMER;

         Map<String, String> headers = new HashMap<String, String>();
         headers.put(ViaBmsUtil.ViaHeaderKey.SDK_KEY, SDK_KEY);

         viaApiCtrl.sendPostRequest(queue, viaApiCtrl.SDK_ENDPOINT + viaApiCtrl.CORE_CUSTOMER, new ViaInterfaces.ViaCallbackInterface() {
             @Override
             public void doWhenResponse(JSONObject result) {
                 try {
                     exchangeCustomerHandler(result);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }, inputJson, new ArrayList<String>(), headers);
     }

    public void exchangeCustomerHandler(JSONObject data) throws JSONException {
        CUSTOMER.customerId = (int) data.get(ViaBmsUtil.ViaKey.CUSTOMERID);
        CUSTOMER.identifier = (String) data.get(ViaBmsUtil.ViaKey.IDENTIFIER);
        CUSTOMER.email = (String) data.get(ViaBmsUtil.ViaKey.EMAIL);
        CUSTOMER.phone = (String) data.get(ViaBmsUtil.ViaKey.PHONE);
        CUSTOMER.remark = (String) data.get(ViaBmsUtil.ViaKey.REMARK);
    }

    public void startBmsService(CallbackContext callbackContext){
        try {
            viaIBeaconCtrl.initiate(IBEACON_REGION, SETTING, new BeaconServiceConnectCallback(callbackContext));
        } catch (Exception e) {
            e.printStackTrace();

            callbackContext.error(e.getMessage());
        }
    }

    public void stopBmsService(CallbackContext callbackContext) {
        try {
            viaIBeaconCtrl.stopRange(callbackContext);
            MINISITES.clear();
            SETTING.isModal = false;

            callbackContext.success("");
        } catch (Exception e) {
            e.printStackTrace();

            callbackContext.error(e.getMessage());
        }
    }

    public void siteRequest(ViaIBeacon viaIBeacon) throws JSONException {
        JSONObject inputJson = new JSONObject();
        inputJson.put("_", (Calendar.getInstance().getTime().getTime()));

//        Log.i(TAG, "Site Request: " + viaIBeacon.getiBeacon().getId1().toString());

        List<String> params = new ArrayList<>();
        params.add(viaIBeacon.getiBeacon().getId1().toUuid().toString());
        params.add(String.valueOf(viaIBeacon.getiBeacon().getId2().toInt()));
        params.add(String.valueOf(viaIBeacon.getiBeacon().getId3().toInt()));

        Map<String, String> headers = new HashMap<String, String>();
        headers.put(ViaBmsUtil.ViaHeaderKey.API_KEY, API_KEY);

        viaApiCtrl.sendGetRequest(queue, viaApiCtrl.API_ENDPOINT + viaApiCtrl.CORE_SITE, new ViaInterfaces.ViaCallbackInterface() {
            @Override
            public void doWhenResponse(JSONObject result) {
                try {
                    siteRequestHandler(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, inputJson, params, headers);
    }

    public void siteRequestHandler(JSONObject data) throws JSONException {
        String title = (String) data.get(ViaBmsUtil.ViaKey.TITLE);
        String description = (String) data.get(ViaBmsUtil.ViaKey.DESCRIPTION);
        String url = (String) data.get(ViaBmsUtil.ViaKey.URL);

        String coverUrl = "";
        if (data.has(ViaBmsUtil.ViaKey.COVER)) {
            JSONObject cover = (JSONObject) data.get(ViaBmsUtil.ViaKey.COVER);

            coverUrl = (String) cover.get(ViaBmsUtil.ViaKey.URL);
        }
        String type = (String) data.get(ViaBmsUtil.ViaKey.TYPE);
        Log.i(TAG, "type: " + type);
        ViaMinisite viaMinisite = new ViaMinisite(title,description,coverUrl,url,type);
        MINISITES.add(viaMinisite);

        if (SETTING.enableAlert) {
            scheduleNotification(context, title, description, url, type);

        }
        openMinisiteMenu(context);
    }

    public void attendanceRequest(List<ViaIBeacon> beacons) throws JSONException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(ViaBmsUtil.ViaHeaderKey.API_KEY, API_KEY);

        List<String> params = new ArrayList<>();

        for (ViaIBeacon beacon: beacons) {
            double distance = beacon.getiBeacon().getDistance();

            if (distance > 0) {
                JSONObject inputJson = new JSONObject();
                inputJson.put(ViaBmsUtil.ViaKey.UUID, beacon.getiBeacon().getId1().toUuid().toString());
                inputJson.put(ViaBmsUtil.ViaKey.MAJOR, beacon.getiBeacon().getId2().toInt());
                inputJson.put(ViaBmsUtil.ViaKey.MINOR, beacon.getiBeacon().getId3().toInt());
                inputJson.put(ViaBmsUtil.ViaKey.DISTANCE, distance);
                inputJson.put(ViaBmsUtil.ViaKey.IDENTIFIER, CUSTOMER.identifier);
                inputJson.put(ViaBmsUtil.ViaKey.PHONE, CUSTOMER.phone);
                inputJson.put(ViaBmsUtil.ViaKey.EMAIL, CUSTOMER.email);
                inputJson.put(ViaBmsUtil.ViaKey.REMARK, CUSTOMER.remark);
                inputJson.put(ViaBmsUtil.ViaKey.DATA, new JSONObject());

                viaApiCtrl.sendPostRequest(queue, viaApiCtrl.API_ENDPOINT + viaApiCtrl.CORE_ATTENDANCE, new ViaInterfaces.ViaCallbackInterface() {
                    @Override
                    public void doWhenResponse(JSONObject result) {
                        try {
                            attendanceRequestHandler(result);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, inputJson, params, headers);
            }
        }
    }

    public void attendanceCheckoutRequest(List<ViaIBeacon> beacons) throws JSONException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(ViaBmsUtil.ViaHeaderKey.API_KEY, API_KEY);

        List<String> params = new ArrayList<>();

        for (ViaIBeacon beacon: beacons) {
            double distance = beacon.getiBeacon().getDistance();

            if (beacon.getDisappearIdx() == 10) {
                JSONObject inputJson = new JSONObject();
                inputJson.put(ViaBmsUtil.ViaKey.UUID, beacon.getiBeacon().getId1().toUuid().toString());
                inputJson.put(ViaBmsUtil.ViaKey.MAJOR, beacon.getiBeacon().getId2().toInt());
                inputJson.put(ViaBmsUtil.ViaKey.MINOR, beacon.getiBeacon().getId3().toInt());
                inputJson.put(ViaBmsUtil.ViaKey.DISTANCE, distance);
                inputJson.put(ViaBmsUtil.ViaKey.IDENTIFIER, CUSTOMER.identifier);
                inputJson.put(ViaBmsUtil.ViaKey.PHONE, CUSTOMER.phone);
                inputJson.put(ViaBmsUtil.ViaKey.EMAIL, CUSTOMER.email);
                inputJson.put(ViaBmsUtil.ViaKey.REMARK, CUSTOMER.remark);
                inputJson.put(ViaBmsUtil.ViaKey.DATA, new JSONObject());

                viaApiCtrl.sendPutRequest(queue, viaApiCtrl.API_ENDPOINT + viaApiCtrl.CORE_ATTENDANCE, new ViaInterfaces.ViaCallbackInterface() {
                    @Override
                    public void doWhenResponse(JSONObject result) {
                        try {
                            attendanceRequestHandler(result);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, inputJson, params, headers);
            }
        }
    }

    public void attendanceRequestHandler(JSONObject data) throws JSONException {
        int attendanceId = data.getInt(ViaBmsUtil.ViaKey.ATTENDANCEID);
        String date = data.getString(ViaBmsUtil.ViaKey.DATE);

//        Log.i(TAG, "attendanceId: " + attendanceId + ", date: " + date);
    }

    public void trackingRequest(List<ViaIBeacon> beacons) throws JSONException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(ViaBmsUtil.ViaHeaderKey.API_KEY, API_KEY);

        List<String> params = new ArrayList<>();

        for (ViaIBeacon beacon: beacons) {
            double distance = beacon.getiBeacon().getDistance();

            if (distance > 0) {
                JSONObject inputJson = new JSONObject();
                inputJson.put(ViaBmsUtil.ViaKey.UUID, beacon.getiBeacon().getId1().toUuid().toString());
                inputJson.put(ViaBmsUtil.ViaKey.MAJOR, beacon.getiBeacon().getId2().toInt());
                inputJson.put(ViaBmsUtil.ViaKey.MINOR, beacon.getiBeacon().getId3().toInt());
                inputJson.put(ViaBmsUtil.ViaKey.DISTANCE, distance);
                inputJson.put(ViaBmsUtil.ViaKey.IDENTIFIER, CUSTOMER.identifier);
                inputJson.put(ViaBmsUtil.ViaKey.PHONE, CUSTOMER.phone);
                inputJson.put(ViaBmsUtil.ViaKey.EMAIL, CUSTOMER.email);
                inputJson.put(ViaBmsUtil.ViaKey.REMARK, CUSTOMER.remark);
                inputJson.put(ViaBmsUtil.ViaKey.DATA, new JSONObject());

                viaApiCtrl.sendPostRequest(queue, viaApiCtrl.API_ENDPOINT + viaApiCtrl.CORE_TRACKING, new ViaInterfaces.ViaCallbackInterface() {
                    @Override
                    public void doWhenResponse(JSONObject result) {
                        try {
                            trackingRequestHandler(result);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, inputJson, params, headers);
            }
        }
    }

    public void trackingRequestHandler(JSONObject data) throws JSONException {
        int trackingId = data.getInt(ViaBmsUtil.ViaKey.TRACKINGID);
        double distance = data.getDouble(ViaBmsUtil.ViaKey.DISTANCE);
        String date = data.getString(ViaBmsUtil.ViaKey.DATE);

//        Log.i(TAG, "trackingId: " + trackingId + ", distance: " + distance + ", date: " + date);
    }

    public void openMinisiteMenu (Context context) {
        if(!SETTING.isModal) {
            Intent minisiteMenuIntent = new Intent(context, MinisiteMenuActivity.class);
            minisiteMenuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            minisiteMenuIntent.putParcelableArrayListExtra("viaMinisites", MINISITES);
            minisiteMenuIntent.putExtra("API_KEY", API_KEY);
            context.startActivity(minisiteMenuIntent);

            SETTING.isModal = true;
        } else {
            ViaBeaconHelper.sendMinisiteMenuUpdate(broadcaster,MINISITES);
        }
    }

    @SuppressLint("MissingPermission")
    public static String getDeviceId(Context context) {
        if (ViaBmsUtil.ViaPermission.allowReadPhoneState) {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            final String tmDevice, tmSerial, androidId;

            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

            UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
            String deviceId = deviceUuid.toString();

            return deviceId;
        } else {
            return ViaBmsUtil.ViaValue.UNRECOGNIZED_USER;
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }

        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

//        String phrase = "";
        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
//                phrase += Character.toUpperCase(c);
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
//            phrase += c;
            phrase.append(c);
        }

        return phrase.toString();
    }

    public void scheduleNotification (Context context, String title, String text, String url, String type) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentText(text)
                .setAutoCancel(true);

        if (url != null) {
            Intent intent = new Intent(context, MinisiteActivity.class);

            intent.putExtra("url", url);
            intent.putExtra("title", title);
            intent.putExtra("type", type);
            intent.putExtra("customerId", CUSTOMER.customerId);
            intent.putExtra("API_KEY", API_KEY);
            PendingIntent minisiteIntent = PendingIntent.getActivity(context, 0, intent, 0);
            mBuilder.setContentIntent(minisiteIntent);
        }

        if (title != null) {
            mBuilder.setContentTitle(title);
        } else {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            int stringId = applicationInfo.labelRes;
            String appName = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
            mBuilder.setContentTitle(appName);
        }

        // Set Notification Icon if there's any
        try {
            int icon = context.getApplicationInfo().icon;
            mBuilder.setSmallIcon(icon);
        } catch (Exception e) {
            mBuilder.setSmallIcon(android.R.drawable.sym_def_app_icon);
        }

        NotificationManager mNotificationManager =
                (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(notificationCount, mBuilder.build());
        notificationCount += 1;
    }

    public void onResume(CallbackContext callbackContext) {
        try {
//            Log.i(TAG, "resuming");
            if (viaIBeaconCtrl != null && viaIBeaconCtrl.isRanging && !SETTING.enableBackground) {
                viaIBeaconCtrl.startRange(callbackContext);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public class ViaBmsCtrlCallback {
        public void discover (ViaIBeacon viaIBeacon) throws JSONException {
            if (SETTING.enableSite) {
                siteRequest(viaIBeacon);
            }
        }

        public void rangeBeacons (List<ViaIBeacon> viaIBeacons) throws JSONException {
            if (SETTING.enableAttendance) {
                attendanceRequest(viaIBeacons);
                attendanceCheckoutRequest(viaIBeacons);
            }

            if (SETTING.enableTracking) {
                trackingRequest(viaIBeacons);
            }
        }
    }

    class BeaconServiceConnectCallback {
        private CallbackContext callbackContext;

        BeaconServiceConnectCallback (CallbackContext callbackContext) {
            this.callbackContext = callbackContext;
        }

        public void onServiceConnected () throws RemoteException {
            viaIBeaconCtrl.startRange(callbackContext);
        }
    }
}

