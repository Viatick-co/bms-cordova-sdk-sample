package com.viatick.bmsandroidsdk.plugin;
// The native Toast API
import android.Manifest;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
// Cordova-required packages
import com.viatick.bmsandroidsdk.controller.ViaBmsCtrl;
import com.viatick.bmsandroidsdk.model.ViaZone;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil;
import com.viatick.bmsandroidsdk.model.IBeacon;
import com.viatick.bmsandroidsdk.helper.BmsEnvironment;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

public class BmsCordovaSdkPublic extends CordovaPlugin implements ViaBmsCtrl.ViaBmsCtrlDelegate {
  String TAG = "BmsCordovaSdkPublic";
  CallbackContext initSdkCallback;
  CallbackContext initCustomerCallback;
  CallbackContext checkinCallback;
  CallbackContext checkoutCallback;
  CallbackContext onDistanceBeaconsCallback;
  CallbackContext openDeviceSiteCallback;
  List<ViaZone> zones = new ArrayList<>();
  boolean isReady = false;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    // method to attach delegate
    // 4 callbacks
    // sdkInited
    // customerInited
    // if attendance is enable
    // checkin and checkout
    ViaBmsCtrl.setDelegate(this);
  };

  @Override
  public boolean execute(String action, JSONArray args,
    final CallbackContext callbackContext) throws JSONException {
      if (action.equals("initCustomer")) {
          initCustomerCallback = callbackContext;
          Log.d(TAG, "initCustomer: " + args.getString(0) + " " + args.getString(2)
          + " " + args.getString(1));
          ViaBmsCtrl.initCustomer(args.getString(0), args.getString(2),
                  args.getString(1), this.zones);
          return true;
      } else if (action.equals("setting")) {
          try {
            List<IBeacon> requestDistanceBeacons = new ArrayList<>();
            JSONArray iBeaconJsonArr = args.getJSONArray(10);

            for (int i = 0; i < iBeaconJsonArr.length(); i++) {
              JSONObject iBeaconJson = iBeaconJsonArr.getJSONObject(i);
              IBeacon iBeacon = new IBeacon(iBeaconJson.getString("uuid"),
              iBeaconJson.getInt("major"), iBeaconJson.getInt("minor"));
              requestDistanceBeacons.add(iBeacon);
              Log.i(TAG, "iBeaconJson: " + iBeaconJson.getString("uuid") + " "
              + iBeaconJson.getInt("major") + " " + iBeaconJson.getInt("minor")
              );
            }

            BmsEnvironment bmsEnvironment = BmsEnvironment.PROD;
            switch (args.getString(11)) {
              case "DEV":
                  bmsEnvironment = BmsEnvironment.DEV;
                  break;

              case "PROD":
                  bmsEnvironment = BmsEnvironment.PROD;
                  break;

              case "CHINA":
                  bmsEnvironment = BmsEnvironment.CHINA;
                  break;

              default:
                  bmsEnvironment = BmsEnvironment.PROD;
            }

            Log.d(TAG, "requestDistanceBeacons: " + requestDistanceBeacons);

            ViaBmsCtrl.settings(args.getBoolean(0), args.getBoolean(1),
                    args.getBoolean(2),
                    ((args.getString(3) == "AUTO") ? ViaBmsUtil.MinisiteViewType.AUTO :
                    ViaBmsUtil.MinisiteViewType.LIST), args.getInt(4), args.getBoolean(5),
                    args.getBoolean(6), args.getBoolean(7),
                    args.getInt(8), args.getInt(9), requestDistanceBeacons,
                    bmsEnvironment, args.getDouble(12), args.getBoolean(13), args.getBoolean(14),
                    args.getBoolean(15), args.getInt(16), args.get(17) != null ? args.getInt(17) : ScanSettings.SCAN_MODE_BALANCED);

            Log.d(TAG, "initSettings");

            callbackContext.success("");
          } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.toString());
          }

          return true;
      } else if (action.equals("initSDK")) {
          Log.d(TAG, "initSDK");
          initSdkCallback = callbackContext;
          ViaBmsCtrl.initSdk(cordova.getActivity(), args.getString(0));
          return true;
      } else if (action.equals("startSDK")) {
          Log.d(TAG, "startSDK");

          boolean sdkInited = ViaBmsCtrl.isSdkInited();
          boolean bmsRunning = ViaBmsCtrl.isBmsRunning();

          if (!bmsRunning && sdkInited) {
              Log.d(TAG, "Bms Starting");
              ViaBmsCtrl.startBmsService();
          }

          callbackContext.success("");

          return true;
      } else if (action.equals("endSDK")) {
          Log.d(TAG, "endSDK");

          ViaBmsCtrl.stopBmsService();
          callbackContext.success("");
          return true;
      } else if (action.equals("checkIn")) {
          checkinCallback = callbackContext;
          return true;
      } else if (action.equals("checkOut")) {
          checkoutCallback = callbackContext;
          return true;
      } else if (action.equals("onDistanceBeacons")) {
          onDistanceBeaconsCallback = callbackContext;
          return true;
      } else if (action.equals("openDeviceSite")) {
          openDeviceSiteCallback = callbackContext;
          ViaBmsCtrl.openDeviceSite(args.getString(0));

          return true;
      }

      return false;
  };

  // this method will be called after sdk initilization done
  // list of zones in the sdk application is passed here
  @Override
  public void sdkInited(boolean inited, List<ViaZone> zones) {
      Log.d(TAG, "Sdk inited " + inited);
      if (inited) {
          // this method must be called in order to enable attendance and tracking feature
          // authorizedZones is optional field
          // sdkInited callback will be called after initialization
          this.zones = zones;
          Log.d(TAG, "zones: " + zones);
          initSdkCallback.success("");
      } else {
          initSdkCallback.error("");
      }
  }

  // this method will be called after customer processing done
  @Override
  public void customerInited(boolean inited) {
      Log.d(TAG, "Customer Inited " + inited);
      if (inited) {
          initCustomerCallback.success("");
      } else {
          initCustomerCallback.error("");
      }
  }

  // this method will be called if customer checkin
  @Override
  public void checkin() {
      Log.d(TAG, "Checkin Callback");
      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "");
      pluginResult.setKeepCallback(true); // keep callback
      checkinCallback.sendPluginResult(pluginResult);
  }

  // this method will be called if customer checkin
  @Override
  public void checkout() {
      Log.d(TAG, "Checkout Callback");
      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "");
      pluginResult.setKeepCallback(true); // keep callback
      checkoutCallback.sendPluginResult(pluginResult);
  }

  // it is callback of request tracking distance
  @Override
  public void onDistanceBeacons(List<IBeacon> list) {
    Log.d(TAG, "onDistanceBeacons Callback");
    try {
      JSONArray listJson = new JSONArray();
      for (IBeacon iBeacon: list) {
          JSONObject iBeaconJson = new JSONObject();
          iBeaconJson.put("uuid", iBeacon.getUuid());
          iBeaconJson.put("major", iBeacon.getMajor());
          iBeaconJson.put("minor", iBeacon.getMinor());
          iBeaconJson.put("distance", iBeacon.getDistance());
          listJson.put(iBeaconJson);
      }

      Log.d(TAG, "Checkout Callback");
      if (onDistanceBeaconsCallback != null) {
          PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, listJson);
          pluginResult.setKeepCallback(true); // keep callback
          onDistanceBeaconsCallback.sendPluginResult(pluginResult);
      }
    } catch (Exception e) {
      e.printStackTrace();
      // Do nothing
    }
  }

  @Override
  public void deviceSiteLoaded(boolean loaded, String error) {
      Log.d(TAG, "Device site loaded Callback");
      if (openDeviceSiteCallback != null) {
          if (loaded) {
              PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "");
              pluginResult.setKeepCallback(true); // keep callback
              openDeviceSiteCallback.sendPluginResult(pluginResult);
          } else {
              PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, error);
              pluginResult.setKeepCallback(true); // keep callback
              openDeviceSiteCallback.sendPluginResult(pluginResult);
          }
      }
  }
}
