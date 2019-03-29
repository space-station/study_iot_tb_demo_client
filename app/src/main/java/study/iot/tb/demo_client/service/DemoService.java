package study.iot.tb.demo_client.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.web.client.RestClientException;
//import org.thingsboard.server.common.data.Device;
import study.iot.tb.demo_client.data.Device;
import study.iot.tb.demo_client.rest.TbRestClient;
import study.iot.tb.demo_client.util.MsgHandler;
import study.iot.tb.demo_client.mqtt.MqttUtil;
import study.iot.tb.demo_client.util.CommonParams;


public class DemoService extends Service {

    private String TAG="DemoService";
    public MqttUtil mqttUtil;
    public String mDeviceId;
    public String mDeivceToken;
    private String mServerAddress;
    private String mPassword;
    private Context mContext;
    public String data_info;
    public String mLoginToken;
    public String mTokenInfo;
    public String response;
    private volatile CommonParams.mqttConnectStatus mqttIsConnect = CommonParams.mqttConnectStatus.CONNECT_NONE;
    private CommonParams.msgStatus pubStatus = CommonParams.msgStatus.PUBLISH_NONE;
    private int m_connectTimes = 0;
    private final IBinder mBinder = new ServiceBinder();

    private TbRestClient tbRestClient;
    private Device device;

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
        public DemoService getService() {

            return DemoService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        //mqttUtil = new MqttUtil(getApplicationContext());
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        //httpUtil=new HttpUtil(mContext);
        tbRestClient=new TbRestClient(mContext);
        tbRestClient.addListener(msgHandler_rest);
        mqttUtil = new MqttUtil(mContext);
        mqttUtil.addListener(msgHandler_mqtt);
        Log.i(TAG, "onStartCommand: init Util");
        if(mqttUtil==null){
            Log.i(TAG, "onStartCommand: mqttUtil is null");
        }
        if(tbRestClient==null){
            Log.i(TAG, "onStartCommand: tbRestClient is null");
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

    private MsgHandler msgHandler_rest = new MsgHandler() {
        @Override
        public void onMessage(String type, MqttMessage data) {
//            data_info=data.toString();
//            Intent intent=new Intent("MQTT_CONNECTION_MESSAGE");
//            intent.putExtra("type_info", type);
//            intent.putExtra("data_info", data_info);
//            intent.putExtra("status", MqttUtil.MQTT_MSG);
//            sendBroadcast(intent);
        }

        @Override
        public void onEvent(int event) {
            Intent intent=new Intent("HTTP_CONNECTION_MESSAGE");
            Log.i(TAG, "onEvent event " + event);
            switch (event) {
                case TbRestClient.HTTP_LOGINOK:
                    Log.i(TAG, "http connect sucess");
                    mLoginToken=tbRestClient.getToken();
                    saveData("login_token",mLoginToken);
                    Log.i(TAG, "onEvent:logintoken========"+mLoginToken);
                    intent.putExtra("login_token",mLoginToken);
                    break;
                case TbRestClient.HTTP_UNAUTHORIZED:
                    Log.i(TAG, "login info error");
                    break;
                case TbRestClient.HTTP_LOGINFAILED:
                    Log.i(TAG, "http connect failed");
                    break;
                case TbRestClient.HTTP_NO_TOKEN:
                    Log.i(TAG, "no token");
                    break;
                case TbRestClient.HTTP_EXIST_DEVICE:
                    Log.i(TAG, "device exist");
                    break;
                default:
                    break;
            }
            intent.putExtra("status", event);
            LocalBroadcastManager localBroadcastManager=LocalBroadcastManager.getInstance(getBaseContext());
            localBroadcastManager.sendBroadcast(intent);
        }
    };

    private MsgHandler msgHandler_mqtt = new MsgHandler() {
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
            LocalBroadcastManager localBroadcastManager=LocalBroadcastManager.getInstance(getBaseContext());
            localBroadcastManager.sendBroadcast(intent);
        }
    };

    public String logIn(String username,String password,String server_address){
        //httpUtil.addListener(msgHandler);
        //mLoginToken=httpUtil.doLogin(username,password,server_address);
        tbRestClient.login(username,password,server_address);
        //mLoginToken=tbRestClient.getToken();
        Log.i(TAG, "logIn:Login successfully:LoginToken= "+mLoginToken);
        return mLoginToken;
    }

    public void saveData(String key,String data){
        SharedPreferences.Editor editor = getSharedPreferences("deviceInfo", MODE_PRIVATE).edit();
        editor.putString(key, data);
        editor.commit();
    }

    public void createDevice(String name, String type,String server_address){
        SharedPreferences token_info = getSharedPreferences("tokenInfo", MODE_PRIVATE);
        mLoginToken=token_info.getString("login_token", "");
        //String link_address= server_address.substring(0,25)+"/api/device";
        if(name==""){
            name = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        Log.i(TAG, "name=="+name);
        Log.i(TAG, "type=="+type);
        device=new Device(name,type);
        device = tbRestClient.createDevice(device,server_address);
        if(device!=null){
            mDeviceId= String.valueOf(device.getId().getId());
            Log.i(TAG, "device_id===="+mDeviceId);
            saveData("device_id",mDeviceId);
            mDeivceToken= String.valueOf(tbRestClient.getCredentials(device.getId().getId()).getCredentialsId());
            saveData("device_token",mDeivceToken);
            Intent intent=new Intent("HTTP_CONNECTION_MESSAGE");
            intent.putExtra("status", TbRestClient.HTTP_CREATEOK);
            intent.putExtra("deviceId",mDeviceId);
            Log.i(TAG, "createDevice: ============"+mDeviceId+"token="+mDeivceToken);
            intent.putExtra("device_token",mDeivceToken);
            LocalBroadcastManager localBroadcastManager=LocalBroadcastManager.getInstance(getBaseContext());
            localBroadcastManager.sendBroadcast(intent);
        }
    }


}

