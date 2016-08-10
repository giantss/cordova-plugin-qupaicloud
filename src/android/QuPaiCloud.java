package com.giantss.qupaicloud;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;

public class QuPaiCloud extends CordovaPlugin{


    @Override
    public boolean execute(String action, CordovaArgs args,
                           final CallbackContext callbackContext) throws JSONException {

        if (action.equals("upLoadVideo")) {

            return  true;

        }
        return super.execute(action, args, callbackContext);
    }
}
