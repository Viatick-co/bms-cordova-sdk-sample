package com.viatick.bmsandroidsdk.helper;

import org.json.JSONObject;

import java.io.Serializable;

public class ViaInterfaces {

    //define callback interface
    public interface ViaCallbackInterface {
        void doWhenResponse(JSONObject result);
    }

    public interface MinisiteActivityListener extends Serializable {
        void nextSite();
    }
}
