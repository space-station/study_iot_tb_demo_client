package study.iot.tb.demo_client.mqtt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import study.iot.tb.demo_client.util.CommonParams;

public class MqttService extends Service {

    private String TAG="MqttService";
    public MqttUtil mqttUtil;
    public String mDeviceId;
    public String mDeivceToken;
    private String mServerAddress;
    private String mPassword;
    private Context mContext;
    public String data_info;
    private volatile CommonParams.mqttConnectStatus mqttIsConnect = CommonParams.mqttConnectStatus.CONNECT_NONE;
    private CommonParams.msgStatus pubStatus = CommonParams.msgStatus.PUBLISH_NONE;
    private int m_connectTimes = 0;
    private final IBinder mBinder = new ServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "enter in func onBind");
        return mBinder;
    }

    public void initMqtt(String DeviceId,String DeivceToken,String Password,String ServerAddress) {
        Log.i(TAG, "mqttConnect:");
        mDeviceId= DeviceId;
        mDeivceToken=DeivceToken;
        mServerAddress=ServerAddress;
        mPassword=Password;
        if(mqttUtil==null){
            mqttUtil = new MqttUtil(mContext);
            Log.i(TAG, "initMqtt: kkkkkkkkkkkkkkkkk" + mqttUtil);
        }
        mqttUtil.initMqtt(mDeviceId,mDeivceToken,mPassword,mServerAddress);
        mqttUtil.addListener(msgHandler);
    }


    public void mqttConnect() {
        
        Log.i(TAG, "mqttConnect:");
        mqttUtil.connect();
    }

    public void mqttPublish(String publishTopic, String publishPayload,int Qos) {
        Log.i(TAG, "mqttPublish:");
        mqttUtil.publish(publishTopic,publishPayload.getBytes(),Qos);

    }

    public void mqttSubscribe(String subscribeTopic) {
        Log.i(TAG, "mqttSubscribe:");
        mqttUtil.subscribe(subscribeTopic);

    }

    public class ServiceBinder extends Binder {
        public MqttService getService() {

            return MqttService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        //mqttUtil = new MqttUtil(getApplicationContext());
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        mqttUtil = new MqttUtil(mContext);
        Log.i(TAG, "onStartCommand: init mqttUtil" + mqttUtil);
        if(mqttUtil==null){
            Log.i(TAG, "onStartCommand: kkkkkkkkkkkkkkkkkkkk");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind: --------");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: -------");
        super.onDestroy();
    }

    private MsgHandler msgHandler = new MsgHandler() {
        @Override
        public void onMessage(String type, MqttMessage data) {
            data_info=data.toString();
            Intent intent=new Intent("MQTT_CONNECTION_MESSAGE");
            intent.putExtra("type_info", type);
            intent.putExtra("data_info", data_info);
            intent.putExtra("status", MqttUtil.MQTT_MSG);
            sendBroadcast(intent);
        }

        @Override
        public void onEvent(int event) {
            switch (event) {
                case MqttUtil.MQTT_CONNECTED:
                    mqttIsConnect = CommonParams.mqttConnectStatus.CONNECT_SUCCESS;
                    Log.i(TAG, "mqtt connect succ");
                    break;
                case MqttUtil.MQTT_CONNECTING:
                    break;
                case MqttUtil.MQTT_CONNECTFAIL:
                    mqttIsConnect = CommonParams.mqttConnectStatus.CONNECT_FAILED;
                    Log.i(TAG, "mqtt connect failed");
                    break;
                case MqttUtil.MQTT_DISCONNECT:
                    mqttIsConnect = CommonParams.mqttConnectStatus.CONNECT_DISCONNECT;
                    if(m_connectTimes < 10) {
                        mqttConnect();
                        m_connectTimes++;
                    }
                    Log.i(TAG, "mqtt disconnect!!!!!!!!!!!!!");
                    break;
                case MqttUtil.MQTT_PUBLISHED:
                    pubStatus = CommonParams.msgStatus.PUBLISH_SUCCESS;
                    Log.i(TAG, "mqtt published succ");
                    break;
                case MqttUtil.MQTT_PUBLISHFAIL:
                    pubStatus = CommonParams.msgStatus.PUBLISH_FAILED;
                    Log.i(TAG, "mqtt published fail");
                    break;
                case MqttUtil.MQTT_SUBSCRIBED:
                    Log.i(TAG, "mqtt subscribed");
                    break;
                case MqttUtil.MQTT_SUBSCRIBEFAIL:
                    Log.i(TAG, "mqtt subscribed failed");
                    break;
                default:
                    break;
            }
            Intent intent=new Intent("MQTT_CONNECTION_MESSAGE");
            intent.putExtra("status", event);
            sendBroadcast(intent);
        }
    };

}

