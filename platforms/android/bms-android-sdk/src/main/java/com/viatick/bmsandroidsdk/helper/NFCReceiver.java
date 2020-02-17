package com.viatick.bmsandroidsdk.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.util.Log;

import com.viatick.bmsandroidsdk.controller.ViaBmsCtrl;

public class NFCReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        Intent viaBeaconServiceIntent = new Intent(context, ViaIBeaconCtrl.class);
//        context.startService(viaBeaconServiceIntent);
        Log.i("[VIATICK]", "NFCReceiver");
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];

                    Log.i("[VIATICK]", "message[i]: " + messages[i].toString());
                }
            }
        }
    }
}
