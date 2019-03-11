package study.iot.tb.demo_client.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import study.iot.tb.demo_client.R;
import study.iot.tb.demo_client.rest.TbRestClient;
import study.iot.tb.demo_client.service.DemoService;

public class RestFragment extends TabFragments {
    private static final String TAG = "RestFragment";
    private EditText mUsername_editText;
    private EditText mPassword_editText;
    private EditText mServer_address_editText;
    private EditText mDevice_id_editText;
    private EditText mRequest_body_editText;
    private TextView mResponse_textView;
    private Button mLogin_button;
    private Button mCreateDevice_button;
    private String loginToken="";
    public String mDeviceId;
    public String mDeivceToken;
    private String response;
    private String tokeninfo;
    private String mUsername;
    private String mPassword;
    private String mServer_address;
    public static final int UPDATE_TOKEN=1;
    public static final int UPDATE_RESPONSE=2;
    public DemoService mService;

    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        View view= inflater.inflate(R.layout.rest_layout,container,false);
        mUsername_editText = view.findViewById(R.id.username);
        mPassword_editText = view.findViewById(R.id.password);
        mServer_address_editText = view.findViewById(R.id.server);
        mLogin_button = view.findViewById(R.id.login_button);
        mCreateDevice_button=view.findViewById(R.id.create_device);
        mResponse_textView = view.findViewById(R.id.response_info);
        mDevice_id_editText=view.findViewById(R.id.device_id_edittext);
        mRequest_body_editText=view.findViewById(R.id.request_body_edittext);
        String data1="{ \"Date\":\"today\",\"Time\":\"{\\\"Hour\\\":12" +
                ",\\\"Minutes\\\":56"+"}\"}";
        mRequest_body_editText.setText(data1);
        mUsername= String.valueOf(mUsername_editText.getText());
        mPassword= String.valueOf(mPassword_editText.getText());
        mServer_address=String.valueOf(mServer_address_editText.getText());
        ContentPagerAdapter contentAdapter = ((MainActivity) getActivity()).contentAdapter;
        Log.i(TAG, "onCreateView: "+mService);

        mLogin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DemoService service = getService();
                        if(service == null){
                            Log.i(TAG, "run: nulllllllll");
                        } else {
                            tokeninfo=service.logIn(mUsername,mPassword,mServer_address);
                        }
                    }
                }).start();

            }
        });

        mCreateDevice_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DemoService service = getService();
                        if (service != null) {
                            mService.createDevice(mServer_address);
                        }
                    }
                }).start();
            }
        });
        return view;
    }




    @Override
    public void updateStatus(Intent intent) {
        Log.i(TAG, "updateStatus: Rest+++++++++");
        int event = intent.getIntExtra("status", 10);
        Log.i(TAG, "updateStatus: event====="+event);
        switch (event) {
            case TbRestClient.HTTP_LOGINOK:
                mResponse_textView.setText("get login token successfully=="+intent.getStringExtra("login_token"));
                break;
            case TbRestClient.HTTP_UNAUTHORIZED:
                mResponse_textView.setText("user name or password error");
                break;
            case TbRestClient.HTTP_LOGINFAILED:
                mResponse_textView.setText("log in failed");
                break;
            case TbRestClient.HTTP_NO_TOKEN:
                mResponse_textView.setText("No Login Token!");
                break;
            case TbRestClient.HTTP_CREATEOK:
                mResponse_textView.setText("create device succ,deviceId="+intent.getStringExtra("deviceId"));
                break;
            case TbRestClient.HTTP_EXIST_DEVICE:
                mResponse_textView.setText("device exist");
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

