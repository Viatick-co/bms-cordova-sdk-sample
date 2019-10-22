package com.viatick.bmsandroidsdk.helper;

import org.json.JSONObject;

public class ViaInterfaces {

    //define callback interface
    public interface ViaCallbackInterface {
        void doWhenResponse(JSONObject result);
    }
}
