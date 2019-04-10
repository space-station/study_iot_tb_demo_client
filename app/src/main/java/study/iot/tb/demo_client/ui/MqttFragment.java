package study.iot.tb.demo_client.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import study.iot.tb.demo_client.service.DemoService;

import study.iot.tb.demo_client.mqtt.MqttUtil;

import static android.content.Context.MODE_PRIVATE;
import study.iot.tb.demo_client.R;

public class MqttFragment extends TabFragments {

    private static final String TAG = "MqttFragment";
    private EditText mDeviceid_edittext;
    private EditText mUsername_edittext;
    private EditText mPassword_edittext;
    private EditText mServeraddress_edittext;
    private EditText mPort_edittext;
    private EditText mPublishTopic_edittext;
    private EditText mPublishPayload_edittext;
    private EditText mSubscribeTopic_edittext;
    private TextView mResponse_textview;
    private Button mConnect_button;
    private Button mPublish_button;
    private Button mSusbscribe_button;
    private Button mUpload_attribute_button;
    private Button mRequest_attribute_button;
    private Button mSubscribe_attribute_button;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    public String mDeivceToken;
    public String mDeviceId;
    public String mServerAddress;
    public String mPort;
    public String mPassword;
    public String mPublishTopic;
    public String mPublishPayload;
    public String mSubscribeTopic;
    private int Qos;
    private String status;
    public MqttUtil mqttUtil;
    public DemoService mService;
    private boolean mIsServiceBinded = false;
    private boolean mIsServiceConnected = false;
    private boolean mIsMqttConnected = false;

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            SharedPreferences token_info = this.getContext().getSharedPreferences("deviceInfo", MODE_PRIVATE);
            mDeviceId= token_info.getString("device_id","");
            mDeivceToken=token_info.getString("device_token", "");
            mDeviceid_edittext.setText(mDeviceId);
            mUsername_edittext.setText(mDeivceToken);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view= inflater.inflate(R.layout.mqtt_layout,container,false);
        mDeviceid_edittext=view.findViewById(R.id.device_id);
        mUsername_edittext= view.findViewById(R.id.username);
        mPassword_edittext= view.findViewById(R.id.password);
        mServeraddress_edittext=view.findViewById(R.id.server);
        mPort_edittext=view.findViewById(R.id.port);
        mResponse_textview=view.findViewById(R.id.mqtt_info);
        mConnect_button=view.findViewById(R.id.connect_button);
        mPublish_button=view.findViewById(R.id.publish_button);
        mSusbscribe_button=view.findViewById(R.id.subscribe_button);
        mPublishTopic_edittext=view.findViewById(R.id.publish_text);
        mPublishPayload_edittext=view.findViewById(R.id.publish_payload);
        radioGroup=view.findViewById(R.id.radio_group);
        radioButton=view.findViewById(radioGroup.getCheckedRadioButtonId());
        mSubscribeTopic_edittext=view.findViewById(R.id.subscribe_text);
        mUpload_attribute_button=view.findViewById(R.id.upload_attribute_button);
        mRequest_attribute_button=view.findViewById(R.id.request_attribute_button);
        mSubscribe_attribute_button=view.findViewById(R.id.subscribe_attribute_button);
        mqttUtil = new MqttUtil(this.getContext());
        SharedPreferences token_info = this.getContext().getSharedPreferences("deviceInfo", MODE_PRIVATE);
        mDeviceId= token_info.getString("device_id","");
        mDeivceToken=token_info.getString("device_token", "");
        //mDeviceid_edittext.setText(mDeviceId);
        //mUsername_edittext.setText(mDeivceToken);
        mPassword_edittext.setText("");
        mService = ((MainActivity) getActivity()).demoService;
        String data1="{ \"key\":\"ca_application\",\"value\":\"{\\\"lon\\\":123" +
                ",\\\"lat\\\":456"+"}\"}";
        String data2="{ \"method\":\"setGpio\",\"params\":\"{\\\"pin\\\":123" +
                ",\\\"value\\\":456"+"}\"}";
        mPublishPayload_edittext.setText(data2);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButton=(getActivity().getWindow().getDecorView()).findViewById(radioGroup.getCheckedRadioButtonId());
            }
        });



        mConnect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DemoService service = getService();
                            if(service == null){
                                Log.i(TAG, "run: nulllllllll");
                            } else {
                            mqttConnect();
                            }
                        }
                    }).start();
                }

        });

        mPublish_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsMqttConnected) {
                    mResponse_textview.setText("mqtt disconnect,pls connect first");
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mqttPublish();
                        }
                    }).start();

                }
            }
        });

        mSusbscribe_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsMqttConnected) {
                    mResponse_textview.setText("mqtt disconnect,pls connect first");
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mqttSubscribe();
                        }
                    }).start();

                }
            }
        });

        mUpload_attribute_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsMqttConnected) {
                    mResponse_textview.setText("mqtt disconnect,pls connect first");
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mService.uploadAttribute();
                        }
                    }).start();

                }
            }
        });

        mRequest_attribute_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsMqttConnected) {
                    mResponse_textview.setText("mqtt disconnect,pls connect first");
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mService.requestAttribute();
                        }
                    }).start();

                }
            }
        });

        mSubscribe_attribute_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsMqttConnected) {
                    mResponse_textview.setText("mqtt disconnect,pls connect first");
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mService.subscribeAttribute();
                        }
                    }).start();

                }
            }
        });

        return view;

    }


    private void mqttConnect(){
        Log.i(TAG, "mqtt_connect:");
        mDeviceId=String.valueOf(mDeviceid_edittext.getText());
        mServerAddress=String.valueOf(mServeraddress_edittext.getText());
        mPort=String.valueOf(mPort_edittext.getText());
        mServerAddress=mServerAddress+":"+mPort;
        mDeivceToken=String.valueOf(mUsername_edittext.getText());
        mPassword=String.valueOf(mPassword_edittext.getText());
        if(mDeviceId.isEmpty()||mDeivceToken.isEmpty()){
            status="pls input DeviceID and Username";
        }
        else{
            if (mService == null) {
                Log.i(TAG, "mqttConnect: mService is null");
                return;
            } else {
                mService.initMqtt(mDeviceId, mDeivceToken, mPassword, mServerAddress);
                mService.mqttConnect();
            }
        }

    }
    private void mqttPublish(){
        Log.i(TAG, "mqttPublish: ");
        mPublishTopic=String.valueOf(mPublishTopic_edittext.getText());
        mPublishPayload= String.valueOf(mPublishPayload_edittext.getText());
        int value=Integer.parseInt((radioButton.getText()).toString());
        switch (value) {
            case 0:
                Qos = 0;
                break;
            case 1:
                Qos = 1;
                break;
            case 2:
                Qos = 2;
                break;
            default:
                Qos = 1;
                break;
        }
        Log.i(TAG, "Qos====== " + Qos);
        if (mService == null) {
            Log.i(TAG, "mqttPublish: mService is null");
            return;
        } else {
            mService.mqttPublish(mPublishTopic, mPublishPayload, Qos);
        }
    }

    private void mqttSubscribe() {
        Log.i(TAG, "mqttSubscribe: ");
        mSubscribeTopic = String.valueOf(mSubscribeTopic_edittext.getText());
        if (mService == null) {
            Log.i(TAG, "mqttSubscribe: mService is null");
            return;
        } else {
            mService.mqttSubscribe(mSubscribeTopic);
        }
        //mqttUtil.subscribe(mSubscribeTopic);
    }


    @Override
    public void updateStatus(Intent intent) {
        Log.i(TAG, "updateStatus: Mqtt-------------------");
        int event = intent.getIntExtra("status", 10);
        switch (event) {
            case MqttUtil.MQTT_CONNECTED:
                mResponse_textview.setText("mqtt connect succ");
                mIsMqttConnected = true;
                break;
            case MqttUtil.MQTT_CONNECTING:
                break;
            case MqttUtil.MQTT_CONNECTFAIL:
                mResponse_textview.setText("mqtt connect failed");
                break;
            case MqttUtil.MQTT_DISCONNECT:
                mResponse_textview.setText("mqtt disconnect!!!");
                break;
            case MqttUtil.MQTT_PUBLISHED:
                mResponse_textview.setText("mqtt published succ");
                break;
            case MqttUtil.MQTT_PUBLISHFAIL:
                mResponse_textview.setText("mqtt published fail");
                break;
            case MqttUtil.MQTT_SUBSCRIBED:
                mResponse_textview.setText("mqtt subscribed");
                break;
            case MqttUtil.MQTT_SUBSCRIBEFAIL:
                mResponse_textview.setText("mqtt subscribed failed");
                break;
            case MqttUtil.MQTT_MSG:
                Log.i(TAG, "------------------");
                String type = "type:" + intent.getStringExtra("type_info");
                String msg = "message:" + intent.getStringExtra("data_info");
                mResponse_textview.setText(type + "\n" + msg);
            default:
                break;
        }

    }
    public DemoService getService(){
        if (mService == null) {
            mService = ((MainActivity) getActivity()).demoService;
        }
        return mService;
    }
}
