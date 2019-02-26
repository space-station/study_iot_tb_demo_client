package study.iot.tb.demo_client.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

public class ContentPagerAdapter extends FragmentPagerAdapter {

    private Context context;

    private RestFragment restFragment;
    private MqttFragment mqttFragment;
    private Fragment mCurrentFragment;

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        mCurrentFragment = (Fragment) object;
        super.setPrimaryItem(container, position, object);

    }

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
            default:
                type = "Rest";
                break;
        }
        return TabFragments.newInstance(type);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        //用来显示标签的内容
        switch (position) {
            case 0:
                return "Rest";
            case 1:
                return "Mqtt";
            default:
                return "Rest";
        }
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }



}
