package com.viatick.bmsandroidsdk.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.viatick.bmsandroidsdk.controller.ViaApiCtrl;
import com.viatick.bmsandroidsdk.helper.ViaBeaconHelper;
import com.viatick.bmsandroidsdk.helper.ViaInterfaces;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// To be modified

public class MinisiteActivity extends AppCompatActivity {

    private static final String TAG = "[VIATICK]";
    private WebView webView;
    private Date start;
    private Date end;
    private String url;
    private String title;
    private int customerId;
    private String type;
    private RequestQueue queue;
    private ShareActionProvider shareActionProvider;
    private ViaApiCtrl viaApiCtrl;

    private String API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
         * Display the progress in the activity title bar, like the browser app does.
         */
        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        super.onCreate(savedInstanceState);

        setContentView(this.getResources().getIdentifier("minisite_web_view", "layout", this.getPackageName()));

        // Instantiate the RequestQueue
        queue = Volley.newRequestQueue(getApplicationContext());
        viaApiCtrl = new ViaApiCtrl();

        Bundle b = this.getIntent().getExtras();
        url = b.getString("url");
        title = b.getString("title");
        customerId = b.getInt("customerId");
        API_KEY = b.getString("API_KEY");
        type = b.getString("type");

        if (type.equals("coupon")) {
            FloatingActionButton shareButton = findViewById(this.getResources().getIdentifier("fab_share", "id", this.getPackageName()));
            shareButton.hide();
        }

        String actualUrl = url + "&cid=" + customerId;
//        Log.i(TAG, actualUrl);

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

        webView = (WebView) findViewById(this.getResources().getIdentifier("minisiteWebView", "id", this.getPackageName()));
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

                    /*
                     * Start View Log tracking
                     */
                    start = new Date();

                    ViaBeaconHelper.readMofidyAndExecuteJs(webView,activity);
                }
            }
        });

        webView.loadUrl(actualUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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
            JSONObject inputParams = new JSONObject();
            inputParams.put(ViaBmsUtil.ViaKey.CUSTOMERID, customerId);

            List<String> params = new ArrayList<>();
            params.add(url.substring(url.indexOf("?code=") +
                    ("?code=").length(), url.length()));

            Map<String, String> headers = new HashMap<String, String>();
            headers.put(ViaBmsUtil.ViaHeaderKey.API_KEY, API_KEY);

            Log.i("[VIATICK]", "customerId: " + customerId + ", apiKey: " + API_KEY);

            viaApiCtrl.sendPutRequest(queue, viaApiCtrl.API_ENDPOINT + viaApiCtrl.CORE_SITE, new ViaInterfaces.ViaCallbackInterface() {
                @Override
                public void doWhenResponse(JSONObject result) {
                    Log.i("[VIATICK]", "Shared");
                }
            }, inputParams, params, headers);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void back(View view) {
        onBackPressed();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(this.getResources().getIdentifier("fade_in", "anim", this.getPackageName()),
                this.getResources().getIdentifier("fade_out", "anim", this.getPackageName()));
    }
}
