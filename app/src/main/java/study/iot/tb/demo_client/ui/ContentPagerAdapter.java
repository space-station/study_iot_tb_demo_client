package study.iot.tb.demo_client.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ContentPagerAdapter extends FragmentPagerAdapter {

    private Context context;

    private RestFragment restFragment;
    private MqttFragment mqttFragment;

    public ContentPagerAdapter(FragmentManager fm , Context context)
    {
        super(fm);
        this.context = context;
    }

    public ContentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        String type;
        switch (position) {
            case 0:
                type = "Rest";
                break;
            case 1:
                type = "Mqtt";
                break;
            case 2:
                type = "Result";
                break;
            default:
                type = "Rest";
                break;
        }
        return TabFragments.newInstance(type);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        //用来显示标签的内容
        switch (position) {
            case 0:
                return "Rest";
            case 1:
                return "Mqtt";
            case 2:
                return "Result";
            default:
                return "Rest";
        }
    }



}
