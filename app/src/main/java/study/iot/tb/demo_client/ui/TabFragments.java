package study.iot.tb.demo_client.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;



public abstract class TabFragments extends Fragment {
    private static String TAG="TabFragments";
    public static Fragment newInstance(String type){
        if(type.equals("Rest")){
            Log.i(TAG, "newInstance: Rest");
            RestFragment fragment = new RestFragment();
            return fragment;
        }
        else{
            MqttFragment fragment = new MqttFragment();
            return fragment;
        }
    }

    public abstract void updateStatus(Intent intent);


}
