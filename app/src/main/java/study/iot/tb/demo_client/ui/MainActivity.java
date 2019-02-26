package study.iot.tb.demo_client.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import study.iot.tb.demo_client.R;
import study.iot.tb.demo_client.mqtt.MqttService;

public class MainActivity extends AppCompatActivity {

    private List<Fragment> mFragments;
    private TabLayout myTab;
    private ViewPager mViewPager;

    private ContentPagerAdapter contentAdapter;
    private String TAG = "MainActivity";
    public MqttService mqttService;
    public boolean mIsServiceBinded = false;
    private boolean mIsServiceConnected = false;
    public BroadcastReceiver mReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new
                    String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        initView();
        Intent intent = new Intent(this, MqttService.class);
        startService(intent);

    }


    private void initView() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        myTab = findViewById(R.id.mytab);
        mViewPager = findViewById(R.id.vp_content);
        initContent();
        initTab();
    }

    private void initTab() {
        myTab.setupWithViewPager(mViewPager);
    }

    private void initContent() {
        contentAdapter = new ContentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(contentAdapter);


    }

    protected void onStart() {
        super.onStart();
        mIsServiceBinded = bindService(new Intent(this, MqttService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);

        if (!mIsServiceBinded) {
            Log.e(TAG, "onStart, cannot bind Mqtt service");
            finish();
            return;
        }

        if (null == mqttService) {
            Log.d(TAG, "mqttService is null===onStart");
        }

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (contentAdapter.getCurrentFragment() instanceof RestFragment) {
                    Log.i(TAG, "current fragment: RestFragment");
                    ((RestFragment) contentAdapter.getCurrentFragment()).updateStatus(intent);
                } else {
                    Log.i(TAG, "current fragment: MqttFragment");
                    ((MqttFragment)contentAdapter.getCurrentFragment()).updateStatus(intent);
                }

            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("MQTT_CONNECTION_MESSAGE");
        registerReceiver(mReceiver, filter);

    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        /**
         * called by system when bind service
         */

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "enter in ===onServiceConnected()===");
            mIsServiceConnected = true;
            mqttService = ((MqttService.ServiceBinder) service).getService();
            if (null == mqttService) {
                Log.e(TAG, "onServiceConnected, mqttService is null. Going to finsh.");
                finish();
                return;
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mIsServiceConnected = false;
        }
    };





    @Override
    public void onStop() {
        if (mIsServiceBinded) {
            unbindService(mServiceConnection);
            mIsServiceBinded = false;
        }
        unregisterReceiver(mReceiver);
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}