package com.viatick.bmsandroidsdk.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.viatick.bmsandroidsdk.controller.BmsApiCtrl;
import com.viatick.bmsandroidsdk.controller.ViaBmsCtrl;
import com.viatick.bmsandroidsdk.helper.ViaBeaconHelper;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil.MinisiteType;

// To be modified

public class MinisiteActivity extends AppCompatActivity {

    private static final String TAG = "[VIATICK]";
    private WebView webView;

    private int minisiteId = 0;
    private String url;
    private String title;
    private int customerId;
    private MinisiteType type;
    private ShareActionProvider shareActionProvider;
    private boolean structureSite;

    private LocalBroadcastManager broadcaster;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
         * Display the progress in the activity title bar, like the browser app does.
         */
        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        super.onCreate(savedInstanceState);

        setContentView(this.getResources().getIdentifier("minisite_web_view", "layout", this.getPackageName()));

        webView = (WebView) findViewById(this.getResources().getIdentifier("minisiteWebView", "id", this.getPackageName()));

        broadcaster = LocalBroadcastManager.getInstance(this);

        Intent intent = this.getIntent();
        Bundle b = intent.getExtras();
        if (b.containsKey("structureSite")) {
            this.structureSite = b.getBoolean("structureSite");
        }
        updateWebView(b);


        // Receive broadcasted updated ViaMinisites from ViaBeaconService
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(ViaBmsUtil.ViaConstants.MINISITE_VIEW_UPDATE_INTENT)) {
                    Bundle b = intent.getExtras();
                    updateWebView(b);
                } else if (action.equals(ViaBmsUtil.ViaConstants.MINISITE_VIEW_CLOSE_INTENT)) {
                    finish();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ViaBmsUtil.ViaConstants.MINISITE_VIEW_UPDATE_INTENT);
        filter.addAction(ViaBmsUtil.ViaConstants.MINISITE_VIEW_CLOSE_INTENT);

        manager.registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    private void updateWebView(Bundle b) {
        if (minisiteId > 0) {
            ViaBmsCtrl.endSessionLog(minisiteId);
        }

        minisiteId = b.getInt("minisiteId");
        ViaBmsCtrl.getSessionLog(minisiteId);
        url = b.getString("url");
        title = b.getString("title");
        customerId = b.getInt("customerId");
        type = (MinisiteType) b.get("type");

        if (type == MinisiteType.COUPON) {
            FloatingActionButton shareButton = findViewById(this.getResources().getIdentifier("fab_share", "id", this.getPackageName()));
            shareButton.hide();
        }

        String actualUrl = url + "&cid=" + customerId;

        /*
         * Flip animation
         */
        overridePendingTransition(this.getResources().getIdentifier("fade_in", "anim", this.getPackageName()),
                this.getResources().getIdentifier("fade_out", "anim", this.getPackageName()));

        /*
         * Configure progress bar
         */
        final ProgressBar progressBar = (ProgressBar) findViewById(this.getResources().getIdentifier("progressbar", "id", this.getPackageName()));

        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        webView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        final Activity activity = this;
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);

                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);

                    ViaBeaconHelper.readMofidyAndExecuteJs(webView,activity);
                }
            }
        });

        webView.loadUrl(actualUrl);
    }

    protected void share (View view) {
        // The Intent you want to share
        final Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, title);
        i.putExtra(Intent.EXTRA_TEXT, url);

        startActivity(Intent.createChooser(i, "Send To"));
        updateShareLog();
    }

    private void updateShareLog () {
        try {
//            JSONObject inputParams = new JSONObject();
//            inputParams.put(ViaBmsUtil.ViaKey.CUSTOMERID, customerId);
//
//            List<String> params = new ArrayList<>();
//            params.add(url.substring(url.indexOf("?code=") +
//                    ("?code=").length(), url.length()));
//
//            Map<String, String> headers = new HashMap<String, String>();
//            headers.put(ViaBmsUtil.ViaHeaderKey.API_KEY, API_KEY);
//
//            Log.i("[VIATICK]", "customerId: " + customerId + ", apiKey: " + API_KEY);
//
//            viaApiCtrl.sendPutRequest(queue, viaApiCtrl.API_ENDPOINT + viaApiCtrl.CORE_SITE, new ViaInterfaces.ViaCallbackInterface() {
//                @Override
//                public void doWhenResponse(JSONObject result) {
//                    Log.i("[VIATICK]", "Shared");
//                }
//            }, inputParams, params, headers);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void back(View view) {
        if (this.structureSite) {
            ViaBmsCtrl.nextMinisite();
//            Intent nextSiteIntent = new Intent(ViaBmsUtil.ViaConstants.NEXT_MINISITE_VIEW_INTENT);
//////            broadcaster.sendBroadcast(nextSiteIntent);
        } else {
            onBackPressed();
        }
    }

    @Override
    protected void onDestroy () {
        if (minisiteId > 0) {
            ViaBmsCtrl.endSessionLog(minisiteId);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(this.getResources().getIdentifier("fade_in", "anim", this.getPackageName()),
                this.getResources().getIdentifier("fade_out", "anim", this.getPackageName()));
    }
}
