package com.viatick.bmsandroidsdk.controller;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.viatick.bmsandroidsdk.helper.BmsEnvironment;
import com.viatick.bmsandroidsdk.helper.ViaBeaconHelper;
import com.viatick.bmsandroidsdk.model.BleBeacon;
import com.viatick.bmsandroidsdk.model.IBeacon;
import com.viatick.bmsandroidsdk.model.ViaAttendance;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil.AttendanceStatus;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil.MinisiteType;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil.ViaCustomer;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil.ViaIBeaconRegion;
import com.viatick.bmsandroidsdk.model.ViaMinisite;
import com.viatick.bmsandroidsdk.model.ViaProximity;
import com.viatick.bmsandroidsdk.model.ViaSetting;
import com.viatick.bmsandroidsdk.model.ViaZone;
import com.viatick.bmsandroidsdk.model.ViaZoneBeacon;
import com.viatick.bmsandroidsdk.response.SdkInfoResponse;
import com.viatick.bmsandroidsdk.response.SdkTokenResponse;
import com.viatick.bmsandroidsdk.view.MinisiteActivity;
import com.viatick.bmsandroidsdk.view.MinisiteMenuActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by zanyk on 27/4/18.
 */

public class ViaBmsCtrl {
  private static final String TAG = "[VIATICK]";
  public static final String BMS_SDK_CHANNEL_ID = "BMS_SDK_CHANNEL";

  private final static String[] NECESSARY_PERMISSIONS = {
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.WAKE_LOCK,
    Manifest.permission.READ_PHONE_STATE
  };

  private static boolean sdkInited = false;
  private static boolean bmsRunning = false;
  public static boolean inMinisiteView = false;
  private static boolean inMinisiteAuto = false;
  private static boolean attendanceUpdating = false;
  private static HashMap<String, Boolean> proximityUpdating = new HashMap<>();
  public static boolean isModal = false;
  private static int currentSite = -1;
  private static String SDK_KEY;
  private static String SDK_TOKEN;
  private static long SDK_EXPIRATION_TIME = 0L;
  private static String API_KEY;
  public static final ViaSetting SETTING = new ViaSetting();
  public static ViaCustomer CUSTOMER;
  private static final ViaIBeaconRegion IBEACON_REGION = new ViaIBeaconRegion();
  private ViaBmsUtil.ViaEddystoneRegion EDDY_REGION = new ViaBmsUtil.ViaEddystoneRegion();
  private ViaBmsUtil.ViaFetchInterval FETCH_INTERVAL = new ViaBmsUtil.ViaFetchInterval();
  private static LocalBroadcastManager BROADCASTER;
  private static int notificationCount = 0;
  private static final ArrayList<ViaMinisite> MINISITES = new ArrayList();
  private static final ViaAttendance ATTENDANCE = new ViaAttendance();
  private static final HashMap<String, ViaProximity> PROXIMITY = new HashMap<>();
  private static HashMap<Integer, ViaZone> ZONES;
  private static final HashMap<String, ViaZoneBeacon> ASSIGNED_BEACONS = new HashMap();
  public static final HashMap<String, IBeacon> REQUESTED_DISTANCE_BEACONS = new HashMap<>();
  public static final HashMap<String, BleBeacon> OWNED_BEACONS = new HashMap<>();
  private static Context context;
  private static NotificationManager mNotificationManager;
  //  private static ViaIBeaconCtrl viaIBeaconCtrl;
  private static ViaBmsCtrl.ViaBmsCtrlDelegate viaBmsCtrlDelegate;
  private LocationManager locationManager;

  private static BeaconServiceCtrlConnection beaconServiceCtrlConnection = new BeaconServiceCtrlConnection();
  private static Messenger beaconServiceCtrlMessenger;
  private static Messenger beaconServiceCtrlReceiver = new Messenger(new BeaconServiceCtrlHandler());

  private static final Handler handler = new Handler(Looper.myLooper());
  private static final Runnable autoMinisiteRunable = new Runnable() {
    public void run() {
      ViaBmsCtrl.nextMinisite();
    }
  };
  private static final Runnable attendanceRunable = new Runnable() {
    public void run() {
      ViaBmsCtrl.checkAttendance();
      ViaBmsCtrl.checkProximity();
      ViaBmsCtrl.handler.postDelayed(this, 1000L);
    }
  };
//  private static final ServiceConnection mConnection = new ServiceConnection() {
//    public void onServiceConnected(ComponentName className, IBinder service) {
//      ViaIBeaconCtrl.LocalBinder binder = (ViaIBeaconCtrl.LocalBinder) service;
//      ViaBmsCtrl.viaIBeaconCtrl = binder.getService();
//      ViaBmsCtrl.viaIBeaconCtrl.setMBound(true);
//      ViaBmsCtrl.viaIBeaconCtrl.setViaBmsCtrlCallback(ViaBmsCtrl.iBeaconDelegate);
//    }
//
//    public void onServiceDisconnected(ComponentName arg0) {
//      ViaBmsCtrl.viaIBeaconCtrl.setMBound(false);
//    }
//  };

//  private static final iBeaconCtrlCallback iBeaconDelegate = new iBeaconCtrlCallback() {
//    public void discover(ViaIBeacon viaIBeacon) {
//      if (SETTING.isEnableSite()) {
//        ViaBmsCtrl.siteRequest(viaIBeacon);
//      }
//    }
//
//    public void rangeBeacons(List<ViaIBeacon> viaIBeacons) {
//      if (SETTING.isEnableDistance()) {
//        ViaBmsCtrl.updateBeaconDistance(viaIBeacons);
//      }
//
//      if (SETTING.isEnableAttendance()) {
//        ViaBmsCtrl.updateDeviceAttendance(viaIBeacons);
//      }
//
//
//      if (SETTING.isEnableTracking()) {
//        ViaBmsCtrl.trackingRequest(viaIBeacons);
//      }
//    }
//  };

  private static class BeaconServiceCtrlHandler extends Handler {
    private BeaconServiceCtrlHandler() {
    }

    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case BeaconServiceCtrl.DEVICE_DISCOVER_RESPONSE:
          if (SETTING.isEnableSite()) {
            BleBeacon device = (BleBeacon) msg.obj;
            ViaBmsCtrl.siteRequest(device);
          }
          break;

        case BeaconServiceCtrl.DEVICES_ON_DISTANCE_RESPONSE:
          List<BleBeacon> devices = (List<BleBeacon>) msg.obj;

          Log.i(TAG, "BeaconServiceCtrl.DEVICES_ON_DISTANCE_RESPONSE: " + devices.toArray().toString());

          if (SETTING.isEnableDistance()) {
            ViaBmsCtrl.updateBeaconDistance(devices);
          }

          if (SETTING.isEnableAttendance()) {
            ViaBmsCtrl.updateDeviceAttendance(devices);
          }

          if (SETTING.isEnableTracking()) {
            ViaBmsCtrl.trackingRequest(devices);
          }

          if (SETTING.isProximityAlert()) {
            ViaBmsCtrl.updateProximity(devices);
          }

          break;

        case BeaconServiceCtrl.DEVICES_ON_ADVERTISING_STARTED:
          // Doing nothing for now
          break;
      }
    }
  }

  public ViaBmsCtrl() {
  }

  public static void setDelegate(ViaBmsCtrl.ViaBmsCtrlDelegate delegate) {
    viaBmsCtrlDelegate = delegate;
  }

  public static void settings(boolean alert, boolean background, boolean site,
                              ViaBmsUtil.MinisiteViewType minisitesView,
                              Integer autoSiteDuration, boolean tracking,
                              boolean enableMQTT, boolean attendance,
                              Integer checkinDuration, Integer checkoutDuration,
                              List<IBeacon> requestDistanceBeacons, BmsEnvironment bmsEnvironment,
                              Double beaconRegionRange, Boolean beaconRegionUUIDFilter, Boolean broadcasting, Boolean proximityAlert,
                              Integer proximityAlertTheshold, int scanMode) {
    SETTING.setEnableAlert(alert);
    SETTING.setEnableBackground(background);
    SETTING.setEnableSite(site);
    SETTING.setMinisitesView(minisitesView);
    if (autoSiteDuration != null && autoSiteDuration > 0) {
      SETTING.setAutoSiteDuration(autoSiteDuration);
    }

    SETTING.setEnableTracking(tracking);
    SETTING.setEnableMQTT(enableMQTT);
    SETTING.setEnableAttendance(attendance);
    if (checkinDuration != null && checkinDuration > 0) {
      SETTING.setCheckinDuration(checkinDuration);
    }

    if (checkoutDuration != null && checkoutDuration > 0) {
      SETTING.setCheckoutDuration(checkoutDuration);
    }

    if (bmsEnvironment != null) {
      SETTING.setBmsEnvironment(bmsEnvironment);
      BmsApiCtrl.initApi(bmsEnvironment);
    }

    if (beaconRegionRange != null & beaconRegionRange > 0) {
      SETTING.setBeaconRegionRange(beaconRegionRange);
    }

    if (beaconRegionUUIDFilter != null) {
      SETTING.setBeaconRegionUUIDFilter(beaconRegionUUIDFilter);
    }

    if (broadcasting != null) {
      SETTING.setBroadcasting(broadcasting);
    }

    SETTING.setProximityAlert(proximityAlert);

    if (proximityAlertTheshold != null && proximityAlertTheshold > 0) {
      SETTING.setProximityAlertThreshold(proximityAlertTheshold);
    }

    if (requestDistanceBeacons != null) {
      for (IBeacon aBeacon :
        requestDistanceBeacons) {

        String key = aBeacon.getKey();
        if (!REQUESTED_DISTANCE_BEACONS.containsKey(key)) {
          REQUESTED_DISTANCE_BEACONS.put(key, aBeacon);
        }
      }

      SETTING.setEnableDistance(true);
    }

    SETTING.setScanMode(scanMode);
  }

  private static void initedSdk(boolean inited) {
    if (inited) {
      sdkInited = inited;
      if (viaBmsCtrlDelegate != null) {
        List<ViaZone> zones = null;
        if (inited) {
          zones = new ArrayList(ZONES.values());
        } else {
          zones = new ArrayList();
        }

        viaBmsCtrlDelegate.sdkInited(inited, zones);
      }
    }

  }

  public static boolean isSdkInited() {
    return sdkInited;
  }

  private static String getToken() {
    long now = System.currentTimeMillis();
    if (SDK_EXPIRATION_TIME <= now) {
      SdkTokenResponse rp = BmsApiCtrl.getSdkToken(SDK_KEY);
      if (rp != null) {
        SDK_TOKEN = rp.getToken();
        SDK_EXPIRATION_TIME = rp.getExpiration();
      } else {
        SDK_TOKEN = null;
        SDK_EXPIRATION_TIME = 0L;
      }
    }

    return SDK_TOKEN;
  }

  public static void initSdk(Activity activityContext, String sdkKey) {
    if (!sdkInited) {
      context = activityContext;
      SDK_KEY = sdkKey;

      requestPermissions();

      (new LoadSdkInfoTask(new LoadSdkInfoTask.LoadSdkInfoListener() {
        public SdkInfoResponse load() {
          String token = ViaBmsCtrl.getToken();
          if (token != null) {
            SdkInfoResponse response = BmsApiCtrl.getSdkInfo(token);
            return response;
          } else {
            return null;
          }
        }

        public void onFinished(SdkInfoResponse response) {
          boolean inited = false;
          if (response != null) {
            ViaBmsCtrl.IBEACON_REGION.uuid = response.getUuidRegion();
            ViaBmsCtrl.API_KEY = response.getApiKey();
            ViaBmsCtrl.ZONES = response.getZones();
            ViaBmsCtrl.BROADCASTER = LocalBroadcastManager.getInstance(ViaBmsCtrl.context);

//
//            ViaBmsCtrl.context.bindService(viaBeaconIntent, ViaBmsCtrl.mConnection, BIND_AUTO_CREATE);
            if (Build.VERSION.SDK_INT >= 26) {
              ViaBmsCtrl.initNotiChannel();
            }

            Intent viaBeaconIntent = new Intent(ViaBmsCtrl.context, BeaconServiceCtrl.class);
            viaBeaconIntent.putExtra("backgroundTask", SETTING.isEnableBackground());

            if (SETTING.isEnableBackground()) {
              if (Build.VERSION.SDK_INT >= 26) {
                ViaBmsCtrl.context.startForegroundService(viaBeaconIntent);
              } else {
                ViaBmsCtrl.context.startService(viaBeaconIntent);
              }
            }

            inited = ViaBmsCtrl.context.bindService(viaBeaconIntent, beaconServiceCtrlConnection, Context.BIND_AUTO_CREATE);
          }

          if (!inited) ViaBmsCtrl.initedSdk(inited);
        }
      })).execute(new Void[0]);
    }

  }

  @RequiresApi(
    api = 26
  )
  private static void initNotiChannel() {
    mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    String id = "BMS_SDK_CHANNEL";
    int importance = NotificationManager.IMPORTANCE_HIGH;
    NotificationChannel mChannel = new NotificationChannel(id, "BMS_SDK_CHANNEL", importance);
    mNotificationManager.createNotificationChannel(mChannel);
  }

  private static List<String> checkPermissions() {
    List<String> notGrantedList = new ArrayList<>();

    for (String permission : NECESSARY_PERMISSIONS) {
      if (ContextCompat.checkSelfPermission(ViaBmsCtrl.context, permission)
        != PackageManager.PERMISSION_GRANTED) {
        notGrantedList.add(permission);
      }
    }

    return notGrantedList;
  }

  private static void requestPermissions() {
    List<String> notGrantedList = checkPermissions();
    int noGranted = notGrantedList.size();

    if (noGranted > 0) {
      String[] permissions = notGrantedList.toArray(new String[noGranted]);

      ActivityCompat.requestPermissions((Activity) ViaBmsCtrl.context, permissions, 69);
    }
  }

  public static void initCustomer(final String identifier, final String phone, final String email, final List<ViaZone> authorizedZones) {
    if (sdkInited) {
      (new ViaBmsCtrl.ProcessCustomerTask(new ViaBmsCtrl.ProcessCustomerTask.ProcessCustomerListener() {
        public ViaCustomer load() {
          String token = ViaBmsCtrl.getToken();
          if (token == null) {
            return null;
          } else {
            String name = Build.DEVICE;
            String model = ViaBmsCtrl.getDeviceName().replaceAll("-", "");
            String system = "Android";
            String version = System.getProperty("os.version").replaceAll("-", "");
            String remark = name + " : " + model + " : " + system + " : " + version;
            Boolean broadcasting = SETTING.isBroadcasting();
            ViaCustomer customer = BmsApiCtrl.processCustomer(token, identifier, phone, email, remark, system, authorizedZones, broadcasting);

            ViaBmsCtrl.ASSIGNED_BEACONS.clear();
            List<Integer> authorizedZonesx = BmsApiCtrl.getAuthorizedZones(token, customer.getIdentifier());
            Iterator var9 = authorizedZonesx.iterator();

            while (true) {
              ViaZone authorizedZone;
              do {
                if (!var9.hasNext()) {
                  return customer;
                }

                Integer zoneId = (Integer) var9.next();
                authorizedZone = (ViaZone) ViaBmsCtrl.ZONES.get(zoneId);
              } while (authorizedZone == null);

              List<ViaZoneBeacon> beacons = authorizedZone.getBeacons();
              Iterator var13 = beacons.iterator();

              while (var13.hasNext()) {
                ViaZoneBeacon aBeacon = (ViaZoneBeacon) var13.next();
                String key = aBeacon.getUuid() + "-" + aBeacon.getMajor() + "-" + aBeacon.getMinor();
                key = key.toUpperCase();
                ViaBmsCtrl.ASSIGNED_BEACONS.put(key, aBeacon);
              }
            }
          }
        }

        public void onFinished(ViaCustomer customer) {
          boolean inited = false;
          if (customer != null) {
            ViaBmsCtrl.CUSTOMER = customer;
            inited = true;
          }

          ViaBmsCtrl.viaBmsCtrlDelegate.customerInited(inited);
        }
      })).execute(new Void[0]);
    }

  }

  public static void startBmsService() {
    if (sdkInited && !bmsRunning) {
      bmsRunning = true;

      try {
//        viaIBeaconCtrl.initiate(IBEACON_REGION, SETTING, new ViaBmsCtrl.BeaconServiceConnectCallback());
        Message msg = Message.obtain(null, BeaconServiceCtrl.START_SCAN_REQUEST);
        msg.replyTo = beaconServiceCtrlReceiver;

        beaconServiceCtrlMessenger.send(msg);

        if (ViaBmsCtrl.CUSTOMER.getUuid() != null) {
          // broadcast corresponding iBeacon info
          msg = Message.obtain(null, BeaconServiceCtrl.START_ADVERTISE_REQUEST);
          try {
            beaconServiceCtrlMessenger.send(msg);
          } catch (RemoteException e) {
            e.printStackTrace();
          }
        }
      } catch (Exception ex) {
      }

      if (SETTING.isEnableAttendance()) {
        startAttendance();
      }
    }

  }

  public static void stopBmsService() {
    if (sdkInited && bmsRunning) {
      try {
        Message msg = Message.obtain(null, BeaconServiceCtrl.STOP_SCAN_REQUEST);
        Message msg2 = Message.obtain(null, BeaconServiceCtrl.STOP_ADVERTISE_REQUEST);

        beaconServiceCtrlMessenger.send(msg);
        beaconServiceCtrlMessenger.send(msg2);
      } catch (Exception var1) {
      }

      MINISITES.clear();
      ASSIGNED_BEACONS.clear();
      isModal = false;
      ATTENDANCE.setStatus(AttendanceStatus.CHECKOUT);
      if (SETTING.isEnableAttendance()) {
        stopAttendance();
      }

      bmsRunning = false;
    }

  }

  public static boolean isBmsRunning() {
    return bmsRunning;
  }

  private static void siteRequest(final BleBeacon viaIBeacon) {
    if (CUSTOMER != null) {
      (new ViaBmsCtrl.GettingMinisteTask(new ViaBmsCtrl.GettingMinisteTask.GettingMinisiteListener() {
        public ViaMinisite load() {
          String token = ViaBmsCtrl.getToken();
          if (token != null) {
            String uuid = viaIBeacon.getUuid();
            int major = viaIBeacon.getMajor();
            int minor = viaIBeacon.getMinor();
            ViaMinisite viaMinisite = BmsApiCtrl.getMinisite(token, uuid, major, minor);
            return ViaBmsCtrl.siteRequestHandler(viaMinisite);
          } else {
            return null;
          }
        }

        public void onFinished(ViaMinisite minisite) {
          if (minisite != null) {
            ViaBmsCtrl.newViaMinisite(minisite);
          }

        }
      })).execute(new Void[0]);
    }

  }

  private static ViaMinisite siteRequestHandler(ViaMinisite viaMinisite) {
    if (viaMinisite != null && !MINISITES.contains(viaMinisite)) {
      MinisiteType type = viaMinisite.getType();
      int minisiteId = viaMinisite.getId();
      boolean able;
      String token;
      if (type == MinisiteType.VOTING) {
        token = getToken();
        able = BmsApiCtrl.checkVoting(token, CUSTOMER.getCustomerId(), minisiteId);
      } else if (type == MinisiteType.POLLING) {
        token = getToken();
        able = BmsApiCtrl.checkPolling(token, CUSTOMER.getCustomerId(), minisiteId);
      } else {
        able = SETTING.getMinisitesView() == ViaBmsUtil.MinisiteViewType.LIST || type != MinisiteType.DEEP_LINK;
      }

      if (able) {
        return viaMinisite;
      }
    }

    return null;
  }

  private static void newViaMinisite(ViaMinisite viaMinisite) {
    MINISITES.add(viaMinisite);
    if (SETTING.isEnableAlert()) {
      String title = viaMinisite.getTitle();
      String description = viaMinisite.getDescription();
      String url = viaMinisite.getUrl();
      String image = viaMinisite.getNotificationImg();
      scheduleNotification(context, title, description, url, image);
    }

    if (SETTING.getMinisitesView() == ViaBmsUtil.MinisiteViewType.LIST) {
      openMinisiteMenu(context);
    } else {
      openAutoMinisite(context);
    }

  }

  public static void trackingRequest(final List<BleBeacon> beacons) {
    if (CUSTOMER != null) {
      (new ViaBmsCtrl.ApiTask(new ViaBmsCtrl.ApiTask.ApiTaskInterface() {
        public void load() {
          for (int i = 0, len = beacons.size(); i < len; i++) {
            BleBeacon beacon = beacons.get(i);

            double distance = beacon.getAccuracy();
            if (distance > 0.0D) {
              String uuid = beacon.getUuid();
              int major = beacon.getMajor();
              int minor = beacon.getMinor();

              String identifier = ViaBmsCtrl.CUSTOMER.getIdentifier();
              String phone = ViaBmsCtrl.CUSTOMER.getPhone();
              String email = ViaBmsCtrl.CUSTOMER.getEmail();
              String remark = ViaBmsCtrl.CUSTOMER.getRemark();
              if (SETTING.isEnableMQTT()) {
                BmsApiCtrl.coreTrackingWithMQTT(ViaBmsCtrl.API_KEY, uuid, major, minor, identifier, phone, email, remark, distance);
              } else {
                BmsApiCtrl.coreTracking(ViaBmsCtrl.API_KEY, uuid, major, minor, identifier, phone, email, remark, distance);
              }
            }
          }

        }
      })).execute(new Void[0]);
    }
  }

  public static void updateBeaconDistance(final List<BleBeacon> beacons) {
    (new ApiTask(new ApiTask.ApiTaskInterface() {
      public void load() {
        List<IBeacon> responseBeacons = new ArrayList<>();

        for (BleBeacon viaIBeacon : beacons) {
          double distance = viaIBeacon.getAccuracy();
          if (distance > 0.0D) {
            String key = viaIBeacon.getKey();
            key = key.toUpperCase();

            if (REQUESTED_DISTANCE_BEACONS.containsKey(key)) {
              IBeacon responseBeacon = REQUESTED_DISTANCE_BEACONS.get(key);
              responseBeacon.setDistance(distance);

              responseBeacons.add(responseBeacon);
            }
          }
        }

        viaBmsCtrlDelegate.onDistanceBeacons(responseBeacons);
      }
    })).execute(new Void[0]);
  }

  private static void openMinisiteMenu(Context context) {
    if (!isModal) {
      isModal = true;
      Intent minisiteMenuIntent = new Intent(context, MinisiteMenuActivity.class);
      minisiteMenuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      minisiteMenuIntent.putParcelableArrayListExtra("viaMinisites", MINISITES);
      minisiteMenuIntent.putExtra("API_KEY", API_KEY);
      minisiteMenuIntent.putExtra("customerId", CUSTOMER.getCustomerId());
      context.startActivity(minisiteMenuIntent);
    } else {
      ViaBeaconHelper.sendMinisiteMenuUpdate(BROADCASTER, MINISITES);
    }

  }

  private static void startAuto() {
    if (!inMinisiteAuto) {
      inMinisiteAuto = true;
      handler.postDelayed(autoMinisiteRunable, (long) (SETTING.getAutoSiteDuration() * 1000));
    }

  }

  public static void nextMinisite() {
    int noMinisites = MINISITES.size();
    boolean autoSite = SETTING.getAutoSiteDuration() > 0;
    if (noMinisites > 0 && currentSite < noMinisites - 1) {
      ++currentSite;
      updateMinisiteView();
      if (autoSite) {
        inMinisiteAuto = false;
        startAuto();
      }
    } else if (!autoSite) {
      Intent closeIntent = new Intent(ViaBmsUtil.ViaConstants.MINISITE_VIEW_CLOSE_INTENT);
      BROADCASTER.sendBroadcast(closeIntent);
      inMinisiteView = false;
      inMinisiteAuto = false;
    }

  }

  private static void updateMinisiteView() {
    Intent minisiteIntent = new Intent(ViaBmsUtil.ViaConstants.MINISITE_VIEW_UPDATE_INTENT);
    ViaMinisite viaMinisite = (ViaMinisite) MINISITES.get(currentSite);
    minisiteIntent.putExtra("minisiteId", viaMinisite.getId());
    minisiteIntent.putExtra("url", viaMinisite.getUrl());
    minisiteIntent.putExtra("title", viaMinisite.getTitle());
    minisiteIntent.putExtra("type", viaMinisite.getType());
    minisiteIntent.putExtra("customerId", CUSTOMER.getCustomerId());
    BROADCASTER.sendBroadcast(minisiteIntent);
  }

  private static void openAutoMinisite(Context context) {
    boolean autoSite = SETTING.getAutoSiteDuration() > 0;
    if (!inMinisiteView) {
      int noMinisites = MINISITES.size() - 1;
      if (noMinisites > currentSite) {
        inMinisiteView = true;
        ++currentSite;
        ViaMinisite viaMinisite = (ViaMinisite) MINISITES.get(currentSite);
        Intent minisiteIntent = new Intent(context.getApplicationContext(), MinisiteActivity.class);
        minisiteIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        minisiteIntent.putExtra("structureSite", true);
        minisiteIntent.putExtra("minisiteId", viaMinisite.getId());
        minisiteIntent.putExtra("url", viaMinisite.getUrl());
        minisiteIntent.putExtra("title", viaMinisite.getTitle());
        minisiteIntent.putExtra("type", viaMinisite.getType());
        minisiteIntent.putExtra("customerId", CUSTOMER.getCustomerId());
        context.startActivity(minisiteIntent);
      }
    } else if (autoSite) {
      startAuto();
    }
  }

  public static void openDeviceSite(String deviceSiteURL) {
    // Example: https://bms.viatick.com/link/?serial=TEST0000001&env=dev&key=df3ac43dd0ce4d11887edabdca7bfb6e071f800f09001d05df68ddb7747bd130
    int queryStringStartIndex = deviceSiteURL.indexOf("?");
    String prefix = deviceSiteURL.substring(0, queryStringStartIndex);

    Log.i(TAG, "prefix: " + prefix);

    if (!prefix.equals("https://bms.viatick.com/link/") && !prefix.equals("bms.viatick.com/link/")) {
      ViaBmsCtrl.viaBmsCtrlDelegate.deviceSiteLoaded(false, "INVALID_DEVICE_SITE_URL");
    } else {
      String serial = null;
      String env = "main";
      String key = null; // we don't actually need this key here since we're already opening through SDK
      String code = "NFCTAG1"; // default model code if not specified

      String suffix = deviceSiteURL.substring(queryStringStartIndex + 1, deviceSiteURL.length());
      int nextQuestionMarkIndex = suffix.indexOf("&");

      Log.i(TAG, "suffix: " + suffix);

      while (nextQuestionMarkIndex > -1) {
        Log.i(TAG, "nextQuestionMarkIndex: " + nextQuestionMarkIndex);
        int equalSignIndex = suffix.indexOf("=");
        Log.i(TAG, "equalSignIndex: " + equalSignIndex);

        if (equalSignIndex >= nextQuestionMarkIndex) {
          ViaBmsCtrl.viaBmsCtrlDelegate.deviceSiteLoaded(false, "INVALID_DEVICE_SITE_URL");
        } else {
          String keyStr = suffix.substring(0, equalSignIndex);
          String valueStr = suffix.substring(equalSignIndex + 1, nextQuestionMarkIndex);

          switch (keyStr) {
            case "serial":
              serial = valueStr;
              break;

            case "env":
              env = valueStr;
              break;

            case "key":
              key = valueStr;
              break;

            case "code":
              code = valueStr;

            default:
              break;
          }
        }

        suffix = suffix.substring(nextQuestionMarkIndex + 1, suffix.length());
        nextQuestionMarkIndex = suffix.indexOf("&");
      }

      Log.i(TAG, "serial: " + serial);
      Log.i(TAG, "env: " + env);
      Log.i(TAG, "code: " + code);
      Log.i(TAG, "key: " + key);

      boolean isValidEnvironment = true;
      switch (env) {
        case "main":
          if (!SETTING.getBmsEnvironment().equals(BmsEnvironment.PROD)) {
            isValidEnvironment = false;
          }
          break;

        case "dev":
          if (!SETTING.getBmsEnvironment().equals(BmsEnvironment.DEV)) {
            isValidEnvironment = false;
          }
          break;

        case "cn":
          if (!SETTING.getBmsEnvironment().equals(BmsEnvironment.CHINA)) {
            isValidEnvironment = false;
          }
          break;

        default:
          isValidEnvironment = false;
          break;
      }

      if (!isValidEnvironment) {
        ViaBmsCtrl.viaBmsCtrlDelegate.deviceSiteLoaded(false, "INVALID_BMS_ENVIRONMENT");
      } else {
        if (serial == null) {
          ViaBmsCtrl.viaBmsCtrlDelegate.deviceSiteLoaded(false, "INVALID_SERIAL_CODE");
        } else {

          String token = ViaBmsCtrl.getToken();
          if (token != null) {
            final String finalSerial = serial;
            final String finalCode = code;
            (new ViaBmsCtrl.ApiTask(new ViaBmsCtrl.ApiTask.ApiTaskInterface() {
              public void load() {
                String token = ViaBmsCtrl.getToken();

                Boolean hasLocation = true;
                double latitude = 0, longitude = 0;
                // Get Current Location
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                  hasLocation = false;
                } else {
                  LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

                  Location bestResult = null;
                  long bestTime = 0;
                  long minTime = 999999999;
                  float bestAccuracy = 999999999;

                  List<String> matchingProviders = locationManager.getAllProviders();
                  for (String provider: matchingProviders) {
                    Location location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
                      float accuracy = location.getAccuracy();
                      long time = location.getTime();

                      if ((time > minTime && accuracy < bestAccuracy)) {
                        bestResult = location;
                        bestAccuracy = accuracy;
                        bestTime = time;
                      }
                      else if (time < minTime &&
                              bestAccuracy == Float.MAX_VALUE && time > bestTime){
                        bestResult = location;
                        bestTime = time;
                      }
                    }
                  }

                  // Create a criteria object to retrieve provider
//                  Criteria criteria = new Criteria();
//                  // Get the name of the best provider
//                  String provider = locationManager.getBestProvider(criteria, true);
//
//                  Log.i(TAG, "provider: " + provider);
//                  Location myLocation = locationManager.getLastKnownLocation(provider);

                  Location myLocation = bestResult;

                  if (myLocation != null) {
                    //latitude of location
                    latitude = myLocation.getLatitude();

                    //longitude og location
                    longitude = myLocation.getLongitude();
                  }
                }

                Log.i(TAG, "latitude: " + latitude);
                Log.i(TAG, "longitude: " + longitude);

                ViaMinisite viaMinisite = BmsApiCtrl.deviceSite(token, finalSerial, finalCode, hasLocation, latitude, longitude);

                if (viaMinisite != null) {
                  Intent minisiteIntent = new Intent(context.getApplicationContext(), MinisiteActivity.class);
                  minisiteIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  minisiteIntent.putExtra("structureSite", true);
                  minisiteIntent.putExtra("minisiteId", viaMinisite.getId());
                  minisiteIntent.putExtra("url", viaMinisite.getUrl());
                  minisiteIntent.putExtra("title", viaMinisite.getTitle());
                  minisiteIntent.putExtra("type", viaMinisite.getType());
                  minisiteIntent.putExtra("customerId", CUSTOMER.getCustomerId());
                  context.startActivity(minisiteIntent);

                  ViaBmsCtrl.viaBmsCtrlDelegate.deviceSiteLoaded(true, null);
                } else {
                  ViaBmsCtrl.viaBmsCtrlDelegate.deviceSiteLoaded(false, "NO_MINISITE_SCHEDULE");
                }
              }
            })).execute(new Void[0]);
          } else {
            ViaBmsCtrl.viaBmsCtrlDelegate.deviceSiteLoaded(false, "SDK_NOT_INITIATED");
          }
        }
      }
    }
  }

  public static void getSessionLog(int minisiteId) {
    ViaMinisite existMinisite = null;
    int i = 0;

    for (int len = MINISITES.size(); i < len; ++i) {
      existMinisite = (ViaMinisite) MINISITES.get(i);
      if (existMinisite.getId() == minisiteId) {
        break;
      }
    }

    final ViaMinisite minisite = existMinisite;

    if (minisite != null) {
      (new ViaBmsCtrl.CreatingSessionTask(new ViaBmsCtrl.CreatingSessionTask.CreatingSessionListener() {
        public Integer load() {
          String token = ViaBmsCtrl.getToken();
          Integer logId = BmsApiCtrl.createSessionLog(token, ViaBmsCtrl.CUSTOMER.getCustomerId(), minisite.getId(), minisite.getBeacon());
          return logId;
        }

        public void onFinished(Integer logId) {
          if (logId != null) {
            minisite.setLogId(logId);
          }

        }
      })).execute(new Void[0]);
    }

  }

  public static void endSessionLog(final int minisiteId) {
    handler.post(new Runnable() {
      public void run() {
        ViaMinisite minisite = null;
        int i = 0;

        for (int len = ViaBmsCtrl.MINISITES.size(); i < len; ++i) {
          minisite = (ViaMinisite) ViaBmsCtrl.MINISITES.get(i);
          if (minisite.getId() == minisiteId) {
            break;
          }
        }

        if (minisite != null) {
          final Integer logId = minisite.getLogId();
          if (logId != null) {
            (new ViaBmsCtrl.ApiTask(new ViaBmsCtrl.ApiTask.ApiTaskInterface() {
              public void load() {
                String token = ViaBmsCtrl.getToken();
                BmsApiCtrl.updateSessionLog(token, logId);
              }
            })).execute(new Void[0]);
          }
        }

      }
    });
  }

  private static void startAttendance() {
    handler.postDelayed(attendanceRunable, 1000L);
  }

  private static void stopAttendance() {
    handler.removeCallbacks(attendanceRunable);
  }

  private static void checkAttendance() {
    final long now = System.currentTimeMillis();
    AttendanceStatus stt = ATTENDANCE.getStatus();
    final Integer attendanceId = ATTENDANCE.getAttendanceId();
    if (stt == AttendanceStatus.CHECKIN || stt == AttendanceStatus.PRE_CHECKIN) {
      long lastAttendanceTime = ATTENDANCE.getAttendanceTime();
      long dif = now - lastAttendanceTime;
      if (dif >= (long) (SETTING.getCheckoutDuration() * 1000)) {
        if (stt == AttendanceStatus.CHECKIN) {
          (new ViaBmsCtrl.ApiTask(new ViaBmsCtrl.ApiTask.ApiTaskInterface() {
            public void load() {
              if (attendanceId != null) {
                String token = ViaBmsCtrl.getToken();
                if (token != null) {
                  String time = ViaBmsCtrl.convertMilliToIso8601(now);
                  boolean checkout = BmsApiCtrl.checkout(token, attendanceId, time);
                  if (checkout) {
                    ViaBmsCtrl.viaBmsCtrlDelegate.checkout();
                  }
                }
              }

            }
          })).execute(new Void[0]);
        }

        ATTENDANCE.setStatus(AttendanceStatus.CHECKOUT);
      }
    }
  }

  private static void checkProximity() {
    Log.i(TAG, "checkProximity");
    for (String beaconKey: PROXIMITY.keySet()) {
      final long now = System.currentTimeMillis();
      ViaProximity beaconProximity = PROXIMITY.get(beaconKey);

      AttendanceStatus stt = beaconProximity.getStatus();
      final Integer proximityIdId = beaconProximity.getProximityId();
      if (stt == AttendanceStatus.CHECKIN || stt == AttendanceStatus.PRE_CHECKIN) {
        long lastProximityTime = beaconProximity.getProximityTime();
        long dif = now - lastProximityTime;
        if (dif >= (long) (60 * 1000)) {
          beaconProximity.setStatus(AttendanceStatus.CHECKOUT);
          PROXIMITY.put(beaconKey, beaconProximity);

          Log.i(TAG, "checkProximity checkout");
        }
      }
    }
  }

  private static void updateProximity(final List<BleBeacon> beacons) {
    Log.i(TAG, "updateProximity");
    synchronized (proximityUpdating) {
      for (final BleBeacon beacon: beacons) {
        if (CUSTOMER != null && (!proximityUpdating.containsKey(beacon.getKey()) || !proximityUpdating.get(beacon.getKey()))) {
          proximityUpdating.put(beacon.getKey(), true);
          final long now = System.currentTimeMillis();
          boolean checkin = false;

          double distance = beacon.getAccuracy();
          if (distance > 0.0D) {
            String key = beacon.getKey();
            key = key.toUpperCase();

            ViaProximity beaconProximity = PROXIMITY.get(beacon.getKey());
            if (beaconProximity == null) {
              beaconProximity = new ViaProximity();
            }

            AttendanceStatus stt = beaconProximity.getStatus();
            switch (stt) {
              case CHECKOUT:
                beaconProximity.setStatus(AttendanceStatus.PRE_CHECKIN);
                beaconProximity.setStartTime(now);
                break;
              case PRE_CHECKIN:
                long firstAttendance = beaconProximity.getStartTime();
                long dif = now - firstAttendance;
                if (dif >= (long) (SETTING.getProximityAlertThreshold() * 1000)) { // To be changed
                  beaconProximity.setStatus(AttendanceStatus.CHECKIN);
                  checkin = true;
                }
            }

            beaconProximity.setProximityTime(now);
            PROXIMITY.put(beacon.getKey(), beaconProximity);
          }

          Log.i(TAG, "updateProximity checkin: " + checkin);

          if (checkin) {
            (new ViaBmsCtrl.ApiTask(new ViaBmsCtrl.ApiTask.ApiTaskInterface() {
              public void load() {
                String token = ViaBmsCtrl.getToken();
                if (token != null) {
                  String time = ViaBmsCtrl.convertMilliToIso8601(now);
                  Integer proximityId = BmsApiCtrl.createCustomerAlert(token, ViaBmsCtrl.CUSTOMER.getCustomerId(),
                          beacon.getUuid(), beacon.getMajor(), beacon.getMinor());
                  Log.i(TAG, "updateProximity proximityId: " + proximityId);

                  if (proximityId != null) {

                    ViaProximity beaconProximity = PROXIMITY.get(beacon.getKey());
                    if (beaconProximity == null) {
                      beaconProximity = new ViaProximity();
                    }
                    beaconProximity.setProximityId(proximityId);

                    PROXIMITY.put(beacon.getKey(), beaconProximity);
                    Log.i("TAG", "Proximity alert logged!");
                  }
                }

                ViaBmsCtrl.proximityUpdating.put(beacon.getKey(), false);
              }
            })).execute(new Void[0]);
          } else {
            ViaBmsCtrl.proximityUpdating.put(beacon.getKey(), false);
          }
        }
      }
    }
  }

  private static void updateDeviceAttendance(List<BleBeacon> beacons) {
    if (CUSTOMER != null && !attendanceUpdating) {
      attendanceUpdating = true;
      final long now = System.currentTimeMillis();
      boolean checkin = false;

      for (int i = 0, len = beacons.size(); i < len; i++) {
        BleBeacon beacon = beacons.get(i);
        double distance = beacon.getAccuracy();
        if (distance > 0.0D) {
          String key = beacon.getKey();
          key = key.toUpperCase();

          ViaZoneBeacon assignedBeacon = ASSIGNED_BEACONS.get(key);
          if (assignedBeacon != null) {
            AttendanceStatus stt = ATTENDANCE.getStatus();
            switch (stt) {
              case CHECKOUT:
                ATTENDANCE.setStatus(AttendanceStatus.PRE_CHECKIN);
                ATTENDANCE.setFirstAttendance(now);
                break;
              case PRE_CHECKIN:
                long firstAttendance = ATTENDANCE.getFirstAttendance();
                long dif = now - firstAttendance;
                if (dif >= (long) (SETTING.getCheckinDuration() * 1000)) {
                  ATTENDANCE.setStatus(AttendanceStatus.CHECKIN);
                  checkin = true;
                }
            }

            ATTENDANCE.setAttendanceTime(now);
            break;
          }
        }
      }

      if (checkin) {
        (new ViaBmsCtrl.ApiTask(new ViaBmsCtrl.ApiTask.ApiTaskInterface() {
          public void load() {
            String token = ViaBmsCtrl.getToken();
            if (token != null) {
              String time = ViaBmsCtrl.convertMilliToIso8601(now);
              Integer attendanceId = BmsApiCtrl.checkin(token, ViaBmsCtrl.CUSTOMER.getCustomerId(), time);
              if (attendanceId != null) {
                ViaBmsCtrl.ATTENDANCE.setAttendanceId(attendanceId);
                ViaBmsCtrl.viaBmsCtrlDelegate.checkin();
              }
            }

            ViaBmsCtrl.attendanceUpdating = false;
          }
        })).execute(new Void[0]);
      } else {
        attendanceUpdating = false;
      }
    }
  }

  public static String getDeviceName() {
    String manufacturer = Build.MANUFACTURER;
    String model = Build.MODEL;
    return model.startsWith(manufacturer) ? capitalize(model) : capitalize(manufacturer) + " " + model;
  }

  private static String capitalize(String str) {
    if (TextUtils.isEmpty(str)) {
      return str;
    } else {
      char[] arr = str.toCharArray();
      boolean capitalizeNext = true;
      StringBuilder phrase = new StringBuilder();
      char[] var4 = arr;
      int var5 = arr.length;

      for (int var6 = 0; var6 < var5; ++var6) {
        char c = var4[var6];
        if (capitalizeNext && Character.isLetter(c)) {
          phrase.append(Character.toUpperCase(c));
          capitalizeNext = false;
        } else {
          if (Character.isWhitespace(c)) {
            capitalizeNext = true;
          }

          phrase.append(c);
        }
      }

      return phrase.toString();
    }
  }

  public static void scheduleNotification(Context context, String title, String text, String url, String image) {
    (new ViaBmsCtrl.NotificationTask(context, title, text, image)).execute(new Void[0]);
  }

  private static String convertMilliToIso8601(long milli) {
    Date date = new Date(System.currentTimeMillis());
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    String time = sdf.format(date);
    return time;
  }

  private static class NotificationTask extends AsyncTask<Void, Void, Bitmap> {
    private Context context;
    private String title;
    private String text;
    private String image;

    public NotificationTask(Context context, String title, String text, String image) {
      this.context = context;
      this.title = title;
      this.text = text;
      this.image = image;
    }

    protected Bitmap doInBackground(Void... voids) {
      if (this.image != null && !this.image.isEmpty()) {
        try {
          URL url = new URL(this.image);
          HttpURLConnection connection = (HttpURLConnection) url.openConnection();
          connection.setDoInput(true);
          connection.connect();
          InputStream in = connection.getInputStream();
          Bitmap myBitmap = BitmapFactory.decodeStream(in);
          return myBitmap;
        } catch (Exception var6) {
        }
      }

      return null;
    }

    protected void onPostExecute(Bitmap bitmap) {
      super.onPostExecute(bitmap);
      NotificationCompat.Builder mBuilder = (new NotificationCompat.Builder(this.context)).setContentText(this.text).setAutoCancel(true);
      if (this.title != null) {
        mBuilder.setContentTitle(this.title);
      } else {
        ApplicationInfo applicationInfo = this.context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String appName = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : this.context.getString(stringId);
        mBuilder.setContentTitle(appName);
      }

      try {
        int icon = this.context.getApplicationInfo().icon;
        mBuilder.setSmallIcon(icon);
      } catch (Exception var6) {
        mBuilder.setSmallIcon(17301651);
      }

      if (bitmap != null) {
        mBuilder.setLargeIcon(bitmap);
      }

      mBuilder.setChannelId("BMS_SDK_CHANNEL");
      ViaBmsCtrl.mNotificationManager.notify(ViaBmsCtrl.notificationCount, mBuilder.build());
      ViaBmsCtrl.notificationCount = ViaBmsCtrl.notificationCount + 1;
    }
  }

  public static class ApiTask extends AsyncTask<Void, Void, Void> {
    private ViaBmsCtrl.ApiTask.ApiTaskInterface apiTask;

    public ApiTask(ViaBmsCtrl.ApiTask.ApiTaskInterface apiTask) {
      this.apiTask = apiTask;
    }

    protected Void doInBackground(Void... voids) {
      this.apiTask.load();
      return null;
    }

    public interface ApiTaskInterface {
      void load();
    }
  }

  public static class CreatingSessionTask extends AsyncTask<Void, Void, Integer> {
    private ViaBmsCtrl.CreatingSessionTask.CreatingSessionListener listener;

    protected Integer doInBackground(Void... voids) {
      return this.listener.load();
    }

    protected void onPostExecute(Integer logId) {
      super.onPostExecute(logId);
      this.listener.onFinished(logId);
    }

    public CreatingSessionTask(ViaBmsCtrl.CreatingSessionTask.CreatingSessionListener listener) {
      this.listener = listener;
    }

    public interface CreatingSessionListener {
      Integer load();

      void onFinished(Integer var1);
    }
  }

  public static class GettingMinisteTask extends AsyncTask<Void, Void, ViaMinisite> {
    private ViaBmsCtrl.GettingMinisteTask.GettingMinisiteListener listener;

    protected ViaMinisite doInBackground(Void... voids) {
      return this.listener.load();
    }

    protected void onPostExecute(ViaMinisite minisite) {
      super.onPostExecute(minisite);
      this.listener.onFinished(minisite);
    }

    public GettingMinisteTask(ViaBmsCtrl.GettingMinisteTask.GettingMinisiteListener listener) {
      this.listener = listener;
    }

    public interface GettingMinisiteListener {
      ViaMinisite load();

      void onFinished(ViaMinisite var1);
    }
  }

  public static class ProcessCustomerTask extends AsyncTask<Void, Void, ViaCustomer> {
    private ViaBmsCtrl.ProcessCustomerTask.ProcessCustomerListener listener;

    protected ViaCustomer doInBackground(Void... voids) {
      return this.listener.load();
    }

    protected void onPostExecute(ViaCustomer customer) {
      super.onPostExecute(customer);
      this.listener.onFinished(customer);
    }

    public ProcessCustomerTask(ViaBmsCtrl.ProcessCustomerTask.ProcessCustomerListener listener) {
      this.listener = listener;
    }

    public interface ProcessCustomerListener {
      ViaCustomer load();

      void onFinished(ViaCustomer var1);
    }
  }

  public static class LoadSdkInfoTask extends AsyncTask<Void, Void, SdkInfoResponse> {
    private ViaBmsCtrl.LoadSdkInfoTask.LoadSdkInfoListener listener;

    protected SdkInfoResponse doInBackground(Void... voids) {
      return this.listener.load();
    }

    protected void onPostExecute(SdkInfoResponse sdkInfoResponse) {
      super.onPostExecute(sdkInfoResponse);
      this.listener.onFinished(sdkInfoResponse);
    }

    public LoadSdkInfoTask(ViaBmsCtrl.LoadSdkInfoTask.LoadSdkInfoListener listener) {
      this.listener = listener;
    }

    public interface LoadSdkInfoListener {
      SdkInfoResponse load();

      void onFinished(SdkInfoResponse var1);
    }
  }

  public interface ViaBmsCtrlDelegate {
    void sdkInited(boolean inited, List<ViaZone> authorizedBeacons);

    void customerInited(boolean inited);

    void checkin();

    void checkout();

    void onDistanceBeacons(List<IBeacon> beacons);

    void deviceSiteLoaded(boolean loaded, String error);
  }

  public static class BeaconServiceConnectCallback {
    public BeaconServiceConnectCallback() {
    }

    public void BeaconServiceConnectCallback() {
    }

    public void onServiceConnected() throws RemoteException {
//      ViaBmsCtrl.viaIBeaconCtrl.startRange();
    }
  }

  private static class BeaconServiceCtrlConnection implements ServiceConnection {
    private BeaconServiceCtrlConnection() {
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
      beaconServiceCtrlMessenger = new Messenger(service);
      ViaBmsCtrl.initedSdk(true);
    }

    public void onServiceDisconnected(ComponentName name) {
      beaconServiceCtrlMessenger = null;
    }
  }
}
