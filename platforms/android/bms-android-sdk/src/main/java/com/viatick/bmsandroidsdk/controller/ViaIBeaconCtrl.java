//package com.viatick.bmsandroidsdk.controller;
//
//import android.app.Notification;
//import android.app.Service;
//import android.content.Intent;
//import android.os.Binder;
//import android.os.Build;
//import android.os.IBinder;
//import android.os.RemoteException;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//
//import com.viatick.bmsandroidsdk.helper.ViaBeaconHelper;
//import com.viatick.bmsandroidsdk.model.ViaBmsUtil;
//import com.viatick.bmsandroidsdk.model.ViaIBeacon;
//import com.viatick.bmsandroidsdk.model.ViaSetting;
//
//import org.altbeacon.beacon.Beacon;
//import org.altbeacon.beacon.BeaconConsumer;
//import org.altbeacon.beacon.BeaconManager;
//import org.altbeacon.beacon.BeaconParser;
//import org.altbeacon.beacon.Identifier;
//import org.altbeacon.beacon.MonitorNotifier;
//import org.altbeacon.beacon.RangeNotifier;
//import org.altbeacon.beacon.Region;
//import org.json.JSONException;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
///**
// * Created by zanyk on 30/4/18.
// */
//
//public class ViaIBeaconCtrl extends Service implements BeaconConsumer {
//    private BeaconManager beaconManager;
//    private LocalBroadcastManager broadcaster;
//    private ViaBmsUtil.ViaIBeaconRegion region;
//    private final IBinder mBinder = new LocalBinder();
//    private boolean mBound = false;
//    private ViaSetting settings;
//
//    private List<ViaIBeacon> viaIBeacons = new ArrayList<>();
//    private iBeaconCtrlCallback delegate;
//    ViaBmsCtrl.BeaconServiceConnectCallback beaconServiceConnectCallback;
//
//    private static final String TAG = "[VIATICK]";
//    public boolean isRanging = false;
//
//    public void setViaBmsCtrlCallback(iBeaconCtrlCallback delegate) {
//        this.delegate = delegate;
//    }
//
//    /**
//     * Class used for the client Binder.  Because we know this service always
//     * runs in the same process as its clients, we don't need to deal with IPC.
//     */
//    public class LocalBinder extends Binder {
//        public ViaIBeaconCtrl getService() {
//            // Return this instance of ViaBeaconService so clients can call public methods
//            return ViaIBeaconCtrl.this;
//        }
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Notification notification = new NotificationCompat.Builder(this, ViaBmsCtrl.BMS_SDK_CHANNEL_ID)
//                    .setContentTitle("")
//                    .setContentText("").build();
//
//            startForeground(1, notification);
//        }
//    }
//
//
//    public void initiate(ViaBmsUtil.ViaIBeaconRegion ibeacon_region, ViaSetting settings, ViaBmsCtrl.BeaconServiceConnectCallback beaconServiceConnectCallback) throws RemoteException {
//        this.beaconServiceConnectCallback = beaconServiceConnectCallback;
//        region = ibeacon_region;
//        this.settings = settings;
//
//        // Initiate the BeaconManager
//        beaconManager = BeaconManager.getInstanceForApplication(this);
//        if (beaconManager != null && !beaconManager.isBound(this)) {
//            beaconManager.setForegroundBetweenScanPeriod(100l);
//            beaconManager.setBackgroundBetweenScanPeriod(100l);
//            beaconManager.setForegroundScanPeriod(3000l);
//            beaconManager.setBackgroundScanPeriod(3000l);
//            beaconManager.getBeaconParsers().add(new BeaconParser().
//                    setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
//        }
//
//        if (!beaconManager.isBound(this)) {
//            // Bind to this BeaconConsumer
//            beaconManager.bind(this);
//        } else {
//            beaconServiceConnectCallback.onServiceConnected();
//        }
//    }
//
//    public void startRange() throws RemoteException {
//        isRanging = true;
//
//        Region beaconRegion =
//                new Region(ViaBmsUtil.ViaConstants.MY_RANGING_UNIQUE_ID, Identifier.parse(region.uuid),
//                        region.major == 0 ? null : Identifier.parse(String.valueOf(region.major)),
//                        region.minor == 0 ? null : Identifier.parse(String.valueOf(region.minor)));
//
//        beaconManager.startRangingBeaconsInRegion(beaconRegion);
//    }
//
//    public void stopRange() throws RemoteException {
//        isRanging = false;
//
//        Region beaconRegion =
//                new Region(ViaBmsUtil.ViaConstants.MY_RANGING_UNIQUE_ID, Identifier.parse(region.uuid),
//                        region.major == 0 ? null : Identifier.parse(String.valueOf(region.major)),
//                        region.minor == 0 ? null : Identifier.parse(String.valueOf(region.minor)));
//
//        beaconManager.stopRangingBeaconsInRegion(beaconRegion);
//    }
//
//    public void pauseRange() throws RemoteException {
//        Region beaconRegion =
//                new Region(ViaBmsUtil.ViaConstants.MY_RANGING_UNIQUE_ID, Identifier.parse(region.uuid),
//                        region.major == 0 ? null : Identifier.parse(String.valueOf(region.major)),
//                        region.minor == 0 ? null : Identifier.parse(String.valueOf(region.minor)));
//
//        beaconManager.stopRangingBeaconsInRegion(beaconRegion);
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return mBinder;
//    }
//
//    @Override
//    public void onBeaconServiceConnect() {
//        try {
//            beaconServiceConnectCallback.onServiceConnected();
//        } catch (RemoteException e) {
//        }
//
//        beaconManager.setRangeNotifier(new RangeNotifier() {
//            @Override
//            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
//                if (!settings.isEnableBackground()) {
//                    try {
//                        if (ViaBeaconHelper.isAppIsInBackground(getApplicationContext())) {
//                            pauseRange();
//                            return;
//                        }
//                    } catch (RemoteException e) {
//                    }
//                }
//
//                try {
//                    processBeacon(beacons);
//                } catch (JSONException e) {
//                }
//            }
//        });
//
//        beaconManager.setMonitorNotifier(new MonitorNotifier() {
//            @Override
//            public void didEnterRegion(Region region) {
////                Log.i(TAG, "Entered a region");
//            }
//
//            @Override
//            public void didExitRegion(Region region) {
////                Log.i(TAG, "Exited a region");
//            }
//
//            @Override
//            public void didDetermineStateForRegion(int state, Region region) {
////                try {
////                    startRange();
////                } catch (RemoteException e) {
////                    e.printStackTrace();
////                }
//            }
//        });
//    }
//
//    private void processBeacon(Collection<Beacon> beacons) throws JSONException {
////        Log.i(TAG, "CURRENT_SIZE_BEACON: " + Integer.toString(beacons.size()));
//
//        for (ViaIBeacon viaBeacon : viaIBeacons) {
//            viaBeacon.setDisappearIdx(viaBeacon.getDisappearIdx() + 1);
//
//        }
//
//        List<ViaIBeacon> rangeBeacons = new ArrayList();
//
//        for (Beacon beacon : beacons) {
//            ViaIBeacon viaBeacon = new ViaIBeacon(beacon, 200, false, 0);
//
//            int index = indexOf(viaIBeacons, viaBeacon);
//
////            Log.i(TAG, "idex: " + index + ", viaIBeacons size: " + viaIBeacons.size());
//            if (index == -1) {
//
//                // Site request
////                Log.i(TAG, "Discover: " + viaBeacon.getiBeacon().getId3());
//                delegate.discover(viaBeacon);
//
//                viaIBeacons.add(viaBeacon);
//            }
//
//            rangeBeacons.add(viaBeacon);
//        }
//
//        // attendance request and tracking request
//        delegate.rangeBeacons(rangeBeacons);
//    }
//
//    private void processEnter() throws JSONException {
//        delegate.rangeBeacons(viaIBeacons);
//    }
//
//    private int indexOf(List<ViaIBeacon> viaIBeacons, ViaIBeacon viaBeacon) {
//        int index = -1;
//
//        for (int i = 0; i < viaIBeacons.size(); i++) {
//            if (viaIBeacons.get(i).same(viaBeacon)) {
//                index = i;
//                break;
//            }
//        }
//
//        return index;
//    }
//
//    public boolean getMBound() {
//        return mBound;
//    }
//
//    public void setMBound(boolean mBound) {
//        this.mBound = mBound;
//    }
//
//
//    public interface iBeaconCtrlCallback {
//
//        void discover(ViaIBeacon viaIBeacon);
//
//        void rangeBeacons(List<ViaIBeacon> viaIBeacons);
//
//    }
//}
