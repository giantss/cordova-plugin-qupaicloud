package com.giants.qupaicloud;

import android.content.Context;
import android.util.Log;

import com.duanqu.qupai.auth.AuthService;
import com.duanqu.qupai.auth.QupaiAuthListener;

import org.apache.cordova.CallbackContext;

public class Auth {

    private static Auth instance;

    public static Auth getInstance() {
        if (instance == null) {
            instance = new Auth();
        }
        return instance;
    }

    private static final String AUTHTAG = "QupaiAuth";

    /**
     * 鉴权 建议只调用一次,在demo里面为了测试调用了多次 得到accessToken，通常一个用户对应一个token
     * @param context
     * @param appKey    appkey
     * @param appsecret appsecret
     * @param space     space
     */
    public void initAuth(Context context ,final CallbackContext callbackContext, String appKey, String appsecret, String space){
        Log.e("Live", "space" + Contant.space);

        AuthService service = AuthService.getInstance();
        service.setQupaiAuthListener(new QupaiAuthListener() {
            @Override
            public void onAuthError(int errorCode, String message) {
                Log.e(AUTHTAG, "ErrorCode" + errorCode + "message" + message);
                callbackContext.error("鉴权失败");
            }

            @Override
            public void onAuthComplte(int responseCode, String responseMessage) {
                Log.e(AUTHTAG, "onAuthComplte" + responseCode + "message" + responseMessage);
                Contant.accessToken = responseMessage;
                callbackContext.success("鉴权成功");
            }
        });
        service.startAuth(context,appKey, appsecret, space);
    }

}
