package com.viatick.bmsandroidsdk.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.viatick.bmsandroidsdk.controller.ViaIBeaconCtrl;

public class ViaBeaconRebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent viaBeaconServiceIntent = new Intent(context, ViaIBeaconCtrl.class);
        context.startService(viaBeaconServiceIntent);
    }
}
