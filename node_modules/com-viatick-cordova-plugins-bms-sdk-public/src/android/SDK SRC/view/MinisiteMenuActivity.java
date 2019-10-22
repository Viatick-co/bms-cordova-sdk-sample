package com.viatick.bmsandroidsdk.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.viatick.bmsandroidsdk.helper.MinisiteMenuAdapter;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil;
import com.viatick.bmsandroidsdk.model.ViaMinisite;

import java.util.ArrayList;

public class MinisiteMenuActivity extends AppCompatActivity {

    private RecyclerView lv;
    private BroadcastReceiver receiver;
    private String API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(this.getResources().getIdentifier("activity_minisite_menu", "layout", this.getPackageName()));

        Bundle b = this.getIntent().getExtras();
        ArrayList<ViaMinisite> viaMinisites = b.getParcelableArrayList("viaMinisites");
        API_KEY = b.getString("API_KEY");

        lv = (RecyclerView) findViewById(this.getResources().getIdentifier("viaMinisiteList", "id", this.getPackageName()));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        lv.setLayoutManager(layoutManager);

        // allows for optimizations if all item views are of the same size:
        lv.setHasFixedSize(true);

        final MinisiteMenuAdapter minisiteMenuAdapter = new MinisiteMenuAdapter(this,viaMinisites,API_KEY);
        lv.setAdapter(minisiteMenuAdapter);

        // Receive broadcasted updated ViaMinisites from ViaBeaconService
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("BROADCAST", "received");
                Bundle b = intent.getExtras();
                ArrayList<ViaMinisite> viaMinisites = b.getParcelableArrayList(ViaBmsUtil.ViaConstants.MINISITE_MENU_UPDATE_CONTENT);
                minisiteMenuAdapter.updateValues(viaMinisites);
                minisiteMenuAdapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(ViaBmsUtil.ViaConstants.MINISITE_MENU_UPDATE_RESULT)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    protected void back(View view) {
        onBackPressed();
    }
}
