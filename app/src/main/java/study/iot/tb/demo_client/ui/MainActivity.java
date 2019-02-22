package study.iot.tb.demo_client.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import study.iot.tb.demo_client.R;
import study.iot.tb.demo_client.mqtt.MqttService;

public class MainActivity extends AppCompatActivity {

    private List<Fragment> mFragments;
    private TabLayout myTab;
    private ViewPager mViewPager;

    private ContentPagerAdapter contentAdapter;
    private String TAG="MainActivity";
    static  public MqttService mqttService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new
                    String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        initView();
//        Intent intent = new Intent(this, MqttService.class);
//        startService(intent);

    }


    private void initView() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        myTab = findViewById(R.id.mytab);
        mViewPager = findViewById(R.id.vp_content);
        initContent();
        initTab();
    }

    private void initTab(){
        myTab.setupWithViewPager(mViewPager);
        }

    private void initContent(){
        contentAdapter = new ContentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(contentAdapter);


    }

    protected void onStart() {
        super.onStart();

    }


    }

