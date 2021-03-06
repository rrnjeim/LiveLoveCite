package fr.livelovecite.userprofile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import fr.livelovecite.slidingtab.PagerSlidingTabStrip;

import fr.livelovecite.R;


class MyUplaodsAdapter extends FragmentPagerAdapter implements PagerSlidingTabStrip.IconTabProvider{
    private static final int[] ICONS = new int[] {
            R.drawable.ic_event_start,
            R.drawable.ic_jobs,
            R.drawable.ic_market,
    };

    MyUplaodsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return ICONS.length;
    }

    @Override
    public Fragment getItem(int position)
    {

        switch (position) {
            case 0:
                return new MyEventsFragment();
            case 1:
                return new MyJobsFragment();
            case 2:
                return new MySaleItemsFragment();
            default:
                return null;
        }
    }
    @Override
    public int getPageIconResId(int position) {
        return ICONS[position];
    }
}
