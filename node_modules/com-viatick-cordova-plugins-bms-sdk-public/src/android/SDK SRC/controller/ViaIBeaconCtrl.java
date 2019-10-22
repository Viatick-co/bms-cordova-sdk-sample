package com.viatick.bmsandroidsdk.controller;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;

import com.viatick.bmsandroidsdk.helper.ViaBeaconHelper;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil;
import com.viatick.bmsandroidsdk.model.ViaIBeacon;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.apache.cordova.CallbackContext;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by zanyk on 30/4/18.
 */

public class ViaIBeaconCtrl extends Service implements BeaconConsumer {
    private BeaconManager beaconManager;
    private LocalBroadcastManager broadcaster;
    private ViaBmsUtil.ViaIBeaconRegion region;
    private final IBinder mBinder = new LocalBinder();
    private boolean mBound = false;
    private ViaBmsUtil.ViaSetting settings;

    private List<ViaIBeacon> viaIBeacons = new ArrayList<>();
    ViaBmsCtrl.ViaBmsCtrlCallback viaBmsCtrlCallback;
    ViaBmsCtrl.BeaconServiceConnectCallback beaconServiceConnectCallback;

    private static final String TAG = "[VIATICK]";
    public boolean isRanging = false;

    public ViaIBeaconCtrl(ViaBmsCtrl.ViaBmsCtrlCallback viaBmsCtrlCallback) {
        this.viaBmsCtrlCallback = viaBmsCtrlCallback;
    }

    public void setViaBmsCtrlCallback(ViaBmsCtrl.ViaBmsCtrlCallback viaBmsCtrlCallback) {
        this.viaBmsCtrlCallback = viaBmsCtrlCallback;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public ViaIBeaconCtrl getService() {
            // Return this instance of ViaBeaconService so clients can call public methods
            return ViaIBeaconCtrl.this;
        }
    }

    ViaIBeaconCtrl () {
    }


    public void initiate(ViaBmsUtil.ViaIBeaconRegion ibeacon_region, ViaBmsUtil.ViaSetting settings, ViaBmsCtrl.BeaconServiceConnectCallback beaconServiceConnectCallback) throws RemoteException {
        this.beaconServiceConnectCallback = beaconServiceConnectCallback;
        region = ibeacon_region;
        this.settings = settings;

        // Initiate the BeaconManager
        beaconManager = BeaconManager.getInstanceForApplication(this);
        if (beaconManager != null && !beaconManager.isBound(this)) {
            beaconManager.getBeaconParsers().add(new BeaconParser().
                    setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        }

        if (!beaconManager.isBound(this)) {
            // Bind to this BeaconConsumer
            beaconManager.bind(this);
        } else {
            beaconServiceConnectCallback.onServiceConnected();
        }
    }

    public void startRange(CallbackContext callbackContext) throws RemoteException {
        isRanging = true;

        Region beaconRegion =
                new Region(ViaBmsUtil.ViaConstants.MY_RANGING_UNIQUE_ID, Identifier.parse(region.uuid),
                        region.major == 0 ? null : Identifier.parse(String.valueOf(region.major)),
                        region.minor == 0 ? null : Identifier.parse(String.valueOf(region.minor)));

        beaconManager.startRangingBeaconsInRegion(beaconRegion);

        callbackContext.success();
    }

    public void stopRange(CallbackContext callbackContext) throws RemoteException {
        isRanging = false;

        Region beaconRegion =
                new Region(ViaBmsUtil.ViaConstants.MY_RANGING_UNIQUE_ID, Identifier.parse(region.uuid),
                        region.major == 0 ? null : Identifier.parse(String.valueOf(region.major)),
                        region.minor == 0 ? null : Identifier.parse(String.valueOf(region.minor)));

        beaconManager.stopRangingBeaconsInRegion(beaconRegion);

        callbackContext.success();
    }

    public void pauseRange() throws RemoteException {
        Region beaconRegion =
                new Region(ViaBmsUtil.ViaConstants.MY_RANGING_UNIQUE_ID, Identifier.parse(region.uuid),
                        region.major == 0 ? null : Identifier.parse(String.valueOf(region.major)),
                        region.minor == 0 ? null : Identifier.parse(String.valueOf(region.minor)));

        beaconManager.stopRangingBeaconsInRegion(beaconRegion);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onBeaconServiceConnect() {
        try {
            beaconServiceConnectCallback.onServiceConnected();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (!settings.enableBackground) {
                    try {
                        if(ViaBeaconHelper.isAppIsInBackground(getApplicationContext())) {
                            pauseRange();
                            return;
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    processBeacon(beacons);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
//                Log.i(TAG, "Entered a region");
            }

            @Override
            public void didExitRegion(Region region) {
//                Log.i(TAG, "Exited a region");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
//                try {
//                    startRange();
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
            }
        });
    }

    private void processBeacon(Collection<Beacon> beacons) throws JSONException {
//        Log.i(TAG, "CURRENT_SIZE_BEACON: " + Integer.toString(beacons.size()));

        for (ViaIBeacon viaBeacon: viaIBeacons) {
            viaBeacon.setDisappearIdx(viaBeacon.getDisappearIdx() + 1);
        }

        for (Beacon beacon: beacons) {
            ViaIBeacon viaBeacon = new ViaIBeacon(beacon,200,false,0);

            int index = indexOf(viaIBeacons, viaBeacon);

//            Log.i(TAG, "idex: " + index + ", viaIBeacons size: " + viaIBeacons.size());
            if (index == -1) {
                viaIBeacons.add(viaBeacon);

                // Site request
//                Log.i(TAG, "Discover: " + viaBeacon.getiBeacon().getId3());
                viaBmsCtrlCallback.discover(viaBeacon);
            } else {
                viaIBeacons.get(index).setiBeacon(beacon);
                viaIBeacons.get(index).setDisappearIdx(0);
            }
        }

        // attendance request and tracking request
        viaBmsCtrlCallback.rangeBeacons(viaIBeacons);
    }

    private void processEnter () throws JSONException {
        viaBmsCtrlCallback.rangeBeacons(viaIBeacons);
    }

    private int indexOf(List<ViaIBeacon> viaIBeacons, ViaIBeacon viaBeacon) {
        int index = -1;

        for (int i = 0;i < viaIBeacons.size();i++) {
            if (viaIBeacons.get(i).same(viaBeacon)) {
                index = i;
                break;
            }
        }

        return index;
    }

    public boolean getMBound () {
        return mBound;
    }

    public void setMBound (boolean mBound) {
        this.mBound = mBound;
    }
}
