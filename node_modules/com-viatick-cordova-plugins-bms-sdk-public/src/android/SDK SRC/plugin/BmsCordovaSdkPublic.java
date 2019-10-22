package com.viatick.bmsandroidsdk.plugin;
// The native Toast API
import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
// Cordova-required packages
import com.viatick.bmsandroidsdk.controller.ViaBmsCtrl;
import com.viatick.bmsandroidsdk.model.ViaZone;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

public class BmsCordovaSdkPublic extends CordovaPlugin implements ViaBmsCtrl.ViaBmsCtrlDelegate {
  ViaBmsCtrl viaBmsCtrl = new ViaBmsCtrl();
  CallbackContext initSdkCallback;
  CallbackContext initCustomerCallback;
  CallbackContext checkinCallback;
  CallbackContext checkoutCallback;
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
          viaBmsCtrl.initCustomer(args.getString(0), args.getString(1),
                  args.getString(2), this.zones);
          return true;
      } else if (action.equals("setting")) {
          viaBmsCtrl.settings(args.getBoolean(0), args.getBoolean(1),
                  args.getBoolean(2), args.getBoolean(3),
                  args.getString(4), args.getIntOrNull(5), args.getBoolean(6),
                  args.getBoolean(7), args.getBoolean(8), args.getBoolean(9),
                  args.getIntOrNull(10), args.getIntOrNull(11));

          callbackContext.success("");
          return true;
      } else if (action.equals("initSDK")) {
          initSdkCallback = callbackContext;
          viaBmsCtrl.initSdk(cordova.getActivity(), args.getString(0));
          return true;
      } else if (action.equals("startSDK")) {
          viaBmsCtrl.startBmsService();
          callbackContext.success();
          return true;
      } else if (action.equals("endSDK")) {
          viaBmsCtrl.stopBmsService();
          callbackContext.success();
          return true;
      } else if (action.equals("checkIn")) {
          checkinCallback = callbackContext;
          return true;
      } else if (action.equals("checkOut")) {
          checkoutCallback = callbackContext;
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
          initSdkCallback.ok();
      } else {
          initSdkCallback.error();
      }
  }

  // this method will be called after customer processing done
  @Override
  public void customerInited(boolean inited) {
      Log.d(TAG, "Customer Inited " + inited);
      if (inited) {
          initCustomerCallback.ok();
      } else {
          initCustomerCallback.error();
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

  // override this method
  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);

      // must put this line of code in after super.onRequestPermissionsResult
      ViaBmsCtrl.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
  }

  // override this method
  @Override
  public void onResume() {
      super.onResume();

      // must put this line of code in after super.onResume()
      ViaBmsCtrl.onResume();
  }
}
