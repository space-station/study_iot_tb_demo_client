
package study.iot.tb.demo_client.mqtt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

import study.iot.tb.demo_client.util.MsgHandler;

public class MqttUtil {
    private String deviceId;// deviceId

    public static final int MQTT_CONNECTED = 1000; // mqtt连接成功
    public static final int MQTT_CONNECTFAIL = 1001; // mqtt连接失败
    public static final int MQTT_DISCONNECT = 1002; // mqtt断开连接
    public static final int MQTT_CONNECTING = 1003;
    public static final int MQTT_SUBSCRIBED = 1010; // 订阅成功
    public static final int MQTT_SUBSCRIBEFAIL = 1011; // 订阅失败
    public static final int MQTT_MSG = 2001; // 接收到TEST消息
    public static final int MQTT_PUBLISHED = 2010; // 发布成功
    public static final int MQTT_PUBLISHFAIL = 2011; // 发布失败


    private MqttAndroidClient mqttClient; // mqtt客户端
    private MqttConnectOptions mOption; // mqtt设置
    private MqttCallback clientCallback; // 客户端回调
    private ArrayList<MsgHandler> listenerList = new ArrayList<MsgHandler>(); // 消息接收者
    private Context mContext;

    public final String TAG = MqttUtil.class.getSimpleName();

    public MqttUtil(Context context) {
        mContext = context;
    }

    // 初始化连接
    public void initMqtt(String devId, String userId, String password, String serverUrl) {
        deviceId = devId;
        Log.i(TAG, "devId="+devId);
        Log.i(TAG,"userID="+userId);
        Log.i(TAG, "password="+password);
        Log.i(TAG, "serverUrl="+serverUrl);
        mOption = new MqttConnectOptions();
        mOption.setUserName(userId);
        mOption.setPassword(password.toCharArray());
        mOption.setCleanSession(false);
        mOption.setConnectionTimeout(30000);
        mOption.setKeepAliveInterval(30000);

        clientCallback = new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connect lost ");
                if (cause != null) {
                    Log.i(TAG, cause.getMessage());
                    dispachEvent(MQTT_DISCONNECT);
                }
//                connect();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "message arrived, topic:" + topic + "  content:" + message);
                dispachMessage(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "deliveryComplete" + token);
            }
        };

        mqttClient = new MqttAndroidClient(mContext, serverUrl, deviceId);
        mqttClient.setCallback(clientCallback);
    }

    public void publish(String topic, byte[] payload,int Qos) {
        if(mqttClient==null){
            dispachEvent(MQTT_DISCONNECT);
        }
        else {
            if (mqttClient.isConnected()) {
                try {
                    MqttMessage msg = new MqttMessage();
                    //msg.setQos(1);
                    msg.setQos(Qos);
                    msg.setRetained(true);
                    msg.setPayload(payload);
                    Log.i(TAG, "publish, topic:" + topic);

                    mqttClient.publish(topic, msg, mContext, new IMqttActionListener() {

                        @Override
                        public void onSuccess(IMqttToken arg0) {
                            if (arg0 != null) {
                                Log.i(TAG, "publish success ");
                            }
                            dispachEvent(MQTT_PUBLISHED);
                        }

                        @Override
                        public void onFailure(IMqttToken arg0, Throwable arg1) {
                            Log.i(TAG, "publish fail " + arg0.getException() + "  " + arg1.getMessage());
                            dispachEvent(MQTT_PUBLISHFAIL);
                        }
                    });
                } catch (MqttException e) {
                    Log.e(TAG, "publish error:" + e);
                    dispachEvent(MQTT_PUBLISHFAIL);
                }
            } else {
                Log.i(TAG, "device is disconnected, reconnect now...");
                connect();
            }
        }
    }

//    public void publish(String topic, String payload) {
//        publish(topic, payload.getBytes());
//    }

    public void subscribe(String topic) {
        if (mqttClient.isConnected()) {
            try {
                mqttClient.subscribe(topic, 0, mContext, new IMqttActionListener() {

                    @Override
                    public void onSuccess(IMqttToken arg0) {
                        Log.i(TAG, "subscribe success ");
                        dispachEvent(MQTT_SUBSCRIBED);
                    }

                    @Override
                    public void onFailure(IMqttToken arg0, Throwable arg1) {
                        Log.i(TAG,
                                "subscribe fail " + arg0.getException() + "  " + arg1.getMessage());
                        dispachEvent(MQTT_SUBSCRIBEFAIL);
                    }
                });
            } catch (MqttException e) {
                Log.e(TAG, "subscribe error:" + e);
                dispachEvent(MQTT_SUBSCRIBEFAIL);
            }
        } else {
            Log.i(TAG, "device is disconnected, reconnect now...");
            connect();
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void connect() {
        if (mqttClient == null){
            return;
        }
        if (!mqttClient.isConnected() && isConnectNomarl()) {
            try {
                dispachEvent(MQTT_CONNECTING);
                mqttClient.connect(mOption, null, new IMqttActionListener() {

                    @Override
                    public void onSuccess(IMqttToken arg0) {
                        Log.i(TAG, "connect success ");
                        dispachEvent(MQTT_CONNECTED);
                    }

                    @Override
                    public void onFailure(IMqttToken arg0, Throwable arg1) {
                        Log.i(TAG, "connect fail " + arg1);
                        dispachEvent(MQTT_CONNECTFAIL);
                    }
                });
            } catch (MqttException e) {
                Log.e(TAG, "connect error:" + e);
                dispachEvent(MQTT_CONNECTFAIL);
            }
        }
    }

    public void disConnect() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            dispachEvent(MQTT_DISCONNECT);
        } catch (MqttException e) {
            Log.e(TAG, "disconnect error:" + e);
        }
    }

    public void addListener(MsgHandler msgHandler) {
        if (!listenerList.contains(msgHandler)) {
            listenerList.add(msgHandler);
        }
    }

    public void removeListener(MsgHandler msgHandler) {
        listenerList.remove(msgHandler);
    }

    public void removeAll() {
        listenerList.clear();
    }

    private void dispachMessage(String type, MqttMessage data) {
        for (MsgHandler msgHandler : listenerList)
        {
            msgHandler.onMessage(type, data);
        }
    }

    public void dispachEvent(int event) {
        for (MsgHandler msgHandler : listenerList)
        {
            msgHandler.onEvent(event);
        }
    }

    private boolean isConnectNomarl() {
        if(mContext==null){
            Log.i(TAG, "isConnectNomarl: kkkkkkkkkkkkkk");
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null ) {
            //String name = info.getTypeName();
            //Log.i(TAG, "current network name：" + name);
            return true;
        } else {
            Log.i(TAG, "no network is available");
            return false;
        }
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }


}
