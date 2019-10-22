package com.viatick.bmsandroidsdk.controller;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.viatick.bmsandroidsdk.helper.ViaInterfaces;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by zanyk on 27/4/18.
 */

public class ViaApiCtrl {
    private static final String TAG = "[VIATICK]";
    public String SDK_ENDPOINT = "https://bms.viatick.com/bms/sdk/v1";
    public String APP_HANDSHAKE = "/app/handshake";
    public String CORE_CUSTOMER = "/core/customer";
    public String API_ENDPOINT = "https://bms.viatick.com/bms/api/v1";
    public String CORE_SITE = "/core/site";
    public String CORE_ATTENDANCE = "/core/attendance";
    public String CORE_TRACKING = "/core/tracking";

    public ViaApiCtrl () {};

    public void sendGetRequest (RequestQueue queue, String url,
                                final ViaInterfaces.ViaCallbackInterface callbackInterface,
                                JSONObject input, List<String> params, final Map<String, String> headers) throws JSONException {
        if (input != null) {
            Log.i("INPUT",input.toString());
        }

        String actualUrl = url;
        for (String param: params) {
            actualUrl += "/" + param;
        }

        Iterator inputkeys = input.keys();

        actualUrl += "?";

        boolean isFirstKey = true;
        while (inputkeys.hasNext()) {
            String key = (String) inputkeys.next();
            if (!isFirstKey) {
                actualUrl += "&" + key + "=" + input.get(key);
            } else {
                actualUrl += key + "=" + input.get(key);
            }
        }

        // Request a string response from the provided URL.
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                actualUrl , input,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            Log.println(Log.INFO,"JSON_RESPONSE",response.toString());

                            callbackInterface.doWhenResponse(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.i(TAG, "Error: " + error.networkResponse.data);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };

        /*
         * Retry (max. 2 times) in case of timeout
         */
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,2,1));
        // Add the request to the RequestQueue.
        queue.add(request);
    }

    public void sendPostRequest (RequestQueue queue, String url,
                                final ViaInterfaces.ViaCallbackInterface callbackInterface,
                                JSONObject input, List<String> params, final Map<String, String> headers) {
        if (input != null) {
            Log.i("INPUT",input.toString());
        }

        String actualUrl = url;
        for (String param: params) {
            actualUrl += "/" + param;
        }

        // Request a string response from the provided URL.
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                actualUrl , input,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            Log.println(Log.INFO,"JSON_RESPONSE",response.toString());

                            callbackInterface.doWhenResponse(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };

        /*
         * Retry (max. 2 times) in case of timeout
         */
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,2,1));
        // Add the request to the RequestQueue.
        queue.add(request);
    }

    public void sendPutRequest (RequestQueue queue, String url,
                                 final ViaInterfaces.ViaCallbackInterface callbackInterface,
                                 JSONObject input, List<String> params, final Map<String, String> headers) {
        if (input != null) {
            Log.i("INPUT",input.toString());
        }

        String actualUrl = url;
        for (String param: params) {
            actualUrl += "/" + param;
        }

        // Request a string response from the provided URL.
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT,
                actualUrl, input,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            Log.println(Log.INFO,"JSON_RESPONSE",response.toString());

                            callbackInterface.doWhenResponse(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };

        /*
         * Retry (max. 2 times) in case of timeout
         */
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,2,1));
        // Add the request to the RequestQueue.
        queue.add(request);
    }
}
