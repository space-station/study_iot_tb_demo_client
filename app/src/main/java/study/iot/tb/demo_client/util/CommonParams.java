package study.iot.tb.demo_client.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class CommonParams {


    private static String TAG = "CommonParams";




    public enum msgStatus{
        PUBLISH_SUCCESS,
        PUBLISH_FAILED,
        PUBLISH_NONE,//for publish not return
    }

    public enum mqttConnectStatus{
        CONNECT_SUCCESS,
        CONNECT_FAILED,
        CONNECT_DISCONNECT,
        CONNECT_NONE,//for publish not return
    }



}

