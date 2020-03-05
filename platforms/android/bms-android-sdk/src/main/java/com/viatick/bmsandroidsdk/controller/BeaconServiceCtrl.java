package com.viatick.bmsandroidsdk.controller;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.viatick.bmsandroidsdk.model.BleBeacon;
import com.viatick.bmsandroidsdk.model.IBeacon;
import com.viatick.bmsandroidsdk.model.ViaBeacon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BeaconServiceCtrl extends Service {
  private final static String TAG = "BEACON_SERVICE_CTRL";
  public static final String BMS_SDK_CHANNEL_ID = "BMS_SDK_CHANNEL";

  private static final char[] hexDigits = "0123456789abcdef".toCharArray();

  private HashMap<String, ViaBeacon> devicesHashMap;

  public static final int START_SCAN_REQUEST = 1;
  public static final int STOP_SCAN_REQUEST = 2;
  public static final int START_ADVERTISE_REQUEST = 3;
  public static final int STOP_ADVERTISE_REQUEST = 4;

  public static final int DEVICE_DISCOVER_RESPONSE = 1;
  public static final int DEVICES_ON_DISTANCE_RESPONSE = 2;
  public static final int DEVICES_ON_ADVERTISING_STARTED = 3;

  private Messenger serviceMessenger;
  private Messenger scanSender;
  private Messenger advertiseSender;

  private boolean isScanning = false;
  private boolean isAdvertise = false;

  private HandlerThread handlerThread;
  private Handler handler;

  private BleScanCallback bleScanCallback;

  private final Handler schedulerHandler = new Handler(Looper.myLooper());
  private Runnable deviceScheduler;

  private AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
    @Override
    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
      super.onStartSuccess(settingsInEffect);

      BeaconServiceCtrl.this.onAdvertisingStarted(settingsInEffect);
    }

    @Override
    public void onStartFailure(int errorCode) {
      Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
      super.onStartFailure(errorCode);
    }
  };

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    Log.d(TAG, "Service On Bind");
    boolean backgroundTask = intent.getBooleanExtra("backgroundTask", false);
    if (backgroundTask && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Notification notification = new NotificationCompat.Builder(this, BMS_SDK_CHANNEL_ID)
        .setContentTitle("")
        .setContentText("").build();

      startForeground(1, notification);
    }

    return this.serviceMessenger.getBinder();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(TAG, "Service On Create");

    this.serviceMessenger = new Messenger(new MessageHandler(this));

    this.handlerThread = new HandlerThread("NrfConfigurationService", 1);
    this.handlerThread.start();
    this.handler = new Handler(this.handlerThread.getLooper());
  }

  @Override
  public void onDestroy() {
    this.stopScan();
    this.handlerThread.quit();

    super.onDestroy();
  }

  private void startScan(Messenger messenger, final Collection<IBeacon> iBeacons) {
    Log.d(TAG, "Service start called");
    if (!isScanning) {
      isScanning = true;
      this.scanSender = messenger;
      this.devicesHashMap = new HashMap<>();

      this.deviceScheduler = new Runnable() {
        @Override
        public void run() {
          schedulerHandler.postDelayed(this, 1000l);
          onDistance();
        }
      };
      this.schedulerHandler.postDelayed(this.deviceScheduler, 1000l);

      this.handler.post(new Runnable() {
        @Override
        public void run() {
          bleScanCallback = new BleScanCallback();

          BluetoothAdapter adapter = getBluetoothAdapter();

          ScanSettings scanSettings = new ScanSettings.Builder()
                  .setScanMode(ViaBmsCtrl.SETTING.getScanMode())
                  .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                  .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                  .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                  .setReportDelay(0)
                  .build();

          Log.i(TAG, "Scan mode: " + ViaBmsCtrl.SETTING.getScanMode());

          List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
          if (ViaBmsCtrl.SETTING.isBeaconRegionUUIDFilter()) {

              if (ViaBmsCtrl.SETTING.getBeaconRegionUUID() != null) {
                  scanFilters.add(ScanFilterUtils.getScanFilterUUID(ViaBmsCtrl.SETTING.getBeaconRegionUUID()));
              }

//              scanFilters.add(ScanFilterUtils.getScanFilterIBeacon());
          } else {
            for (IBeacon beacon: iBeacons) {
              scanFilters.add(ScanFilterUtils.getScanFilter(beacon));
            }
          }

          adapter.getBluetoothLeScanner().startScan(scanFilters, scanSettings, bleScanCallback);
        }
      });
    }
  }

  private void stopScan() {
    Log.d(TAG, "Service stop called");

    if (isScanning) {
      this.scanSender = null;

      this.handler.post(new Runnable() {
        @Override
        public void run() {
          schedulerHandler.removeCallbacks(deviceScheduler);

          BluetoothAdapter adapter = getBluetoothAdapter();
          adapter.getBluetoothLeScanner().stopScan(bleScanCallback);
        }
      });

      isScanning = false;
    }
  }

  private void startAdvertiseIBeacon(Messenger messenger, final String uuid, final int major,
                                     final int minor) {
    Log.d(TAG, "Advertise iBeacon called: " + uuid + ":" + major + ":" + minor);
    if (!isAdvertise) {
      isAdvertise = true;
      this.advertiseSender = messenger;

      this.handler.post(new Runnable() {
        @Override
        public void run() {
          BluetoothAdapter adapter = getBluetoothAdapter();

          byte[] payload = {(byte)0x02, (byte)0x15, // this makes it a iBeacon
                  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, // uuid
                  (byte)0x00, (byte)0x00,  // Major
                  (byte)0x00, (byte)0x00, // Minor
                  (byte)0xCB}; // Tx Power

          System.arraycopy(hexStringToByteArray(uuid.toLowerCase().replaceAll("-","")), 0, payload, 2, 16);

          // copy major into data array
          System.arraycopy(integerToByteArray(major), 0, payload, 18, 2);

          // copy minor into data array
          System.arraycopy(integerToByteArray(minor), 0, payload, 20, 2);


          AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
          dataBuilder.addManufacturerData(0x004C, payload); // 0x004c is for Apple inc.
          AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();

          settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
          settingsBuilder.setConnectable(false);

          adapter.getBluetoothLeAdvertiser().startAdvertising(settingsBuilder.build(), dataBuilder.build(), advertisingCallback);
        }
      });
    }
  }

  private void stopAdvertise() {
    Log.d(TAG, "Stop advertising called");

    if (isAdvertise) {
      this.advertiseSender = null;
      this.handler.post(new Runnable() {
        @Override
        public void run() {

          BluetoothAdapter adapter = getBluetoothAdapter();
          adapter.getBluetoothLeAdvertiser().stopAdvertising(null);
        }
      });

      isAdvertise = false;
    }
  }

  private BluetoothAdapter getBluetoothAdapter() {
    BluetoothManager bluetoothManager =
      (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

    return bluetoothManager.getAdapter();
  }

  private class MessageHandler extends Handler {

    private BeaconServiceCtrl service;

    private MessageHandler(BeaconServiceCtrl scanService) {
      this.service = scanService;
    }

    @Override
    public void handleMessage(Message msg) {
      int what = msg.what;
      Messenger replyTo = msg.replyTo;

      switch (what) {
        case START_SCAN_REQUEST:
          this.service.startScan(replyTo, ViaBmsCtrl.REQUESTED_DISTANCE_BEACONS.values());
          break;
        case STOP_SCAN_REQUEST:
          this.service.stopScan();
          break;
        case START_ADVERTISE_REQUEST:
          this.service.startAdvertiseIBeacon(replyTo, ViaBmsCtrl.CUSTOMER.getUuid(),
                  ViaBmsCtrl.CUSTOMER.getMajor(), ViaBmsCtrl.CUSTOMER.getMinor());
          break;
        case STOP_ADVERTISE_REQUEST:
          this.service.stopAdvertise();
          break;
        default:
          break;
      }
    }
  }

  private void onNewBeaconDiscover(BleBeacon beacon) {
    if (this.scanSender != null) {
      Message msg = Message.obtain(null, DEVICE_DISCOVER_RESPONSE);
      msg.obj = beacon;
      try {
        this.scanSender.send(msg);
      } catch (RemoteException e) {
      }
    }
  }

  private void onDistance() {
    long current = System.currentTimeMillis();

    List<BleBeacon> onDistanceBeacons = new ArrayList<>();

    synchronized (this.devicesHashMap) {
        Iterator deviceIterator = this.devicesHashMap.entrySet().iterator();
        while (deviceIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) deviceIterator.next();
            ViaBeacon aBeacon = (ViaBeacon) entry.getValue();

            long lastSeen = aBeacon.getLastSeen();
            long dif = current - lastSeen;

            if (dif <= 5000) {
                onDistanceBeacons.add(aBeacon.getBleBeacon());
            }
        }

        if (this.scanSender != null) {
            Message msg = Message.obtain(null, DEVICES_ON_DISTANCE_RESPONSE);
            msg.obj = onDistanceBeacons;
            try {
                this.scanSender.send(msg);
            } catch (RemoteException e) {
            }
        }
    }
  }

  private void onBleDiscovered(final ScanResult scanResult) {
    this.handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          BleBeacon beacon = getBeaconFromScanResult(scanResult);

          Log.d(TAG, "beacon: " + beacon.getUuid() + " " + beacon.getMajor() + " " + beacon.getMinor() + " " + beacon.getAccuracy());

          if (beacon != null) {

            String key = beacon.getKey();
            long current = System.currentTimeMillis();

            Log.i(TAG, "ViaBmsCtrl.SETTING.getBeaconRegionRange(): " + ViaBmsCtrl.SETTING.getBeaconRegionRange());
            Log.i(TAG, "comparsion 1: " + (ViaBmsCtrl.SETTING.getBeaconRegionRange() < beacon.getAccuracy()));
            Log.i(TAG, "ViaBmsCtrl.SETTING.getBeaconRegionUUID().toLowerCase(): " + ViaBmsCtrl.SETTING.getBeaconRegionUUID().toLowerCase());
            Log.i(TAG, "comparsion 2: " + !ViaBmsCtrl.SETTING.getBeaconRegionUUID().toLowerCase().equals(beacon.getUuid()));

            if (ViaBmsCtrl.SETTING.getBeaconRegionRange() > 0 && ViaBmsCtrl.SETTING.getBeaconRegionRange() < beacon.getAccuracy()) {
              // Outside of user-defined beacon region range, ignore
              return;
            }

            if (ViaBmsCtrl.SETTING.getBeaconRegionUUID() != null && !ViaBmsCtrl.SETTING.getBeaconRegionUUID().toLowerCase().equals(beacon.getUuid())) {
              // Not having beacon region UUID, ignore
              return;
            }

            if (ViaBmsCtrl.OWNED_BEACONS.containsKey(key)) {
              BleBeacon ownedBeacon = ViaBmsCtrl.OWNED_BEACONS.get(key);
              if (ownedBeacon.getAccuracy() > 0 && ownedBeacon.getAccuracy() < beacon.getAccuracy()) {
                // Outside of effective range, ignore
                return;
              }
            }

            Log.i(TAG, "beacon passed through");

            if (!devicesHashMap.containsKey(key)) {
              onNewBeaconDiscover(beacon);

              ViaBeacon newBeacon = new ViaBeacon(beacon, current);
              synchronized (devicesHashMap) {
                devicesHashMap.put(key, newBeacon);
              }
            } else {
              ViaBeacon existBeacon = new ViaBeacon(beacon, current);
              synchronized (devicesHashMap) {
                devicesHashMap.put(key, existBeacon);
              }
            }
          }
        } catch (Exception e) {
        }
      }
    });
  }

  private void onAdvertisingStarted(AdvertiseSettings advertiseSettings) {
    synchronized (this.devicesHashMap) {

      if (this.advertiseSender != null) {
        Message msg = Message.obtain(null, DEVICES_ON_ADVERTISING_STARTED);
        msg.obj = advertiseSettings;
        try {
          this.advertiseSender.send(msg);
        } catch (RemoteException e) {
        }
      }
    }
  }

  private static final class ScanFilterUtils
  {
    private static final int MANUFACTURER_ID = 76;

    private ScanFilterUtils()
    {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static ScanFilter getScanFilter(@NonNull final IBeacon beacon)
    {
      final ScanFilter.Builder builder = new ScanFilter.Builder();

      // the manufacturer data byte is the filter!
      final byte[] manufacturerData = new byte[]
              {
                      0,0,

                      // uuid
                      0,0,0,0,
                      0,0,
                      0,0,
                      0,0,0,0,0,0,0,0,

                      // major
                      0,0,

                      // minor
                      0,0,

                      0
              };

      // the mask tells what bytes in the filter need to match, 1 if it has to match, 0 if not
      final byte[] manufacturerDataMask = new byte[]
              {
                      0,0,

                      // uuid
                      1,1,1,1,
                      1,1,
                      1,1,
                      1,1,1,1,1,1,1,1,

                      // major
                      1,1,

                      // minor
                      1,1,

                      0
              };

      // copy UUID (with no dashes) into data array
      System.arraycopy(hexStringToByteArray(beacon.getUuid().replaceAll("-","")), 0, manufacturerData, 2, 16);

      // copy major into data array
      System.arraycopy(integerToByteArray(beacon.getMajor()), 0, manufacturerData, 18, 2);

      // copy minor into data array
      System.arraycopy(integerToByteArray(beacon.getMinor()), 0, manufacturerData, 20, 2);

      builder.setManufacturerData(
              MANUFACTURER_ID,
              manufacturerData,
              manufacturerDataMask);

      return builder.build();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static ScanFilter getScanFilterUUID(@NonNull final String uuid)
    {
      final ScanFilter.Builder builder = new ScanFilter.Builder();

      // the manufacturer data byte is the filter!
      final byte[] manufacturerData = new byte[]
              {
                      0,0,

                      // uuid
                      0,0,0,0,
                      0,0,
                      0,0,
                      0,0,0,0,0,0,0,0,

                      // major
                      0,0,

                      // minor
                      0,0,

                      0
              };

      // the mask tells what bytes in the filter need to match, 1 if it has to match, 0 if not
      final byte[] manufacturerDataMask = new byte[]
              {
                      0,0,

                      // uuid
                      1,1,1,1,
                      1,1,
                      1,1,
                      1,1,1,1,1,1,1,1,

                      // major
                      0,0,

                      // minor
                      0,0,

                      0
              };

      // copy UUID (with no dashes) into data array
      System.arraycopy(hexStringToByteArray(uuid.replaceAll("-","")), 0, manufacturerData, 2, 16);

      Log.i(TAG, "uuid: " + uuid);
      Log.i(TAG, "manufacturerData :" + Arrays.toString(hexStringToByteArray(uuid.toLowerCase().replace("-",""))));

      builder.setManufacturerData(
              MANUFACTURER_ID,
              manufacturerData,
              manufacturerDataMask);

      return builder.build();
    }

      @TargetApi(Build.VERSION_CODES.LOLLIPOP)
      public static ScanFilter getScanFilterIBeacon()
      {
          final ScanFilter.Builder builder = new ScanFilter.Builder();

          // the manufacturer data byte is the filter!
          final byte[] manufacturerData = new byte[]
                  {
                          0,0,

                          // uuid
                          0,0,0,0,
                          0,0,
                          0,0,
                          0,0,0,0,0,0,0,0,

                          // major
                          0,0,

                          // minor
                          0,0,

                          0
                  };

          // the mask tells what bytes in the filter need to match, 1 if it has to match, 0 if not
          final byte[] manufacturerDataMask = new byte[]
                  {
                          0,0,

                          // uuid
                          0,0,0,0,
                          0,0,
                          0,0,
                          0,0,0,0,0,0,0,0,

                          // major
                          0,0,

                          // minor
                          0,0,

                          0
                  };

          builder.setManufacturerData(
                  MANUFACTURER_ID,
                  manufacturerData,
                  manufacturerDataMask);

          return builder.build();
      }
  }

  private class BleScanCallback extends ScanCallback {

    public BleScanCallback() {
    }

    @Override
    public void onScanResult(int callbackType, ScanResult scanResult) {
      Log.i(TAG, "callbackType: " + callbackType);
      BeaconServiceCtrl.this.onBleDiscovered(scanResult);
    }
  }

  private static BleBeacon getBeaconFromScanResult(ScanResult scanResult) {
    BluetoothDevice device = scanResult.getDevice();
    String name = device.getName();
    byte[] scanRecord = scanResult.getScanRecord().getBytes();
//    BluetoothDevice device = scanResult.getDevice();
    int rssi = scanResult.getRssi();
    boolean beacon = false;

    StringBuilder sb = new StringBuilder(2 * scanRecord.length);
    for (byte b : scanRecord) {
      sb.append(hexDigits[(b >> 4 & 0xF)]).append(hexDigits[(b & 0xF)]);
    }

    String scanRecordAsHex = sb.toString();

    Log.i(TAG, "scanRecordAsHex: " + scanRecordAsHex);

    for (int i = 0; i < scanRecord.length; i++) {
      int payloadLength = unsignedByteToInt(scanRecord[i]);
      if ((payloadLength == 0) || (i + 1 >= scanRecord.length)) {
        break;
      }

      if (unsignedByteToInt(scanRecord[(i + 1)]) != 255) {
        i += payloadLength;
      } else {
        if (payloadLength == 26 || payloadLength == 27) {

          if ((unsignedByteToInt(scanRecord[(i + 2)]) == 76) &&
            (unsignedByteToInt(scanRecord[(i + 3)]) == 0) &&
            (unsignedByteToInt(scanRecord[(i + 4)]) == 2) &&
            (unsignedByteToInt(scanRecord[(i + 5)]) == 21)) {
              int startIndex = (i + 6) * 2;
            String proximityUUID = String.format("%s-%s-%s-%s-%s",
              new Object[]{scanRecordAsHex.substring(startIndex, startIndex + 8),
                scanRecordAsHex.substring(startIndex + 8, startIndex + 12),
                scanRecordAsHex.substring(startIndex + 12, startIndex + 16),
                scanRecordAsHex.substring(startIndex + 16, startIndex + 20),
                scanRecordAsHex.substring(startIndex + 20, startIndex + 32)});

            int major = unsignedByteToInt(scanRecord[(i + 22)]) * 256 + unsignedByteToInt(scanRecord[(i + 23)]);
            int minor = unsignedByteToInt(scanRecord[(i + 24)]) * 256 + unsignedByteToInt(scanRecord[(i + 25)]);
            int measuredPower = (int) scanRecord[(i + 26)];
            double accuracy = calculateAccuracy(rssi, measuredPower);

            return new BleBeacon(proximityUUID, major, minor, accuracy);
          }

//            Log.d(TAG, "Manufacturer specific data does not start with 0x4C000215");
          break;
        }

//          Log.d(TAG, "Manufacturer specific data should have 26 bytes length");
        break;
      }
    }

    return null;
  }

  private static int unsignedByteToInt(byte value) {
    return value & 0xFF;
  }

  private static double calculateAccuracy(int rssi, int measuredPower) {
    int RSSI = Math.abs(rssi);

    if (RSSI == 0.0D) {
      return -1.0D;
    }


    double ratio = RSSI * 1.0D / measuredPower;
    if (ratio < 1.0D) {
      return Math.pow(ratio, 8.0D);
    }

    double accuracy = 0.69976D * Math.pow(ratio, 7.7095D) + 0.111D;
    return accuracy;
  }

  private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
  }

  /**
   * Convert major or minor to hex byte[]. This is used to create a {@link android.bluetooth.le.ScanFilter}.
   *
   * @param value major or minor to convert to byte[]
   * @return byte[]
   */
  private static byte[] integerToByteArray(final int value)
  {
    final byte[] result = new byte[2];
    result[0] = (byte) (value / 256);
    result[1] = (byte) (value % 256);

    return result;
  }
}
