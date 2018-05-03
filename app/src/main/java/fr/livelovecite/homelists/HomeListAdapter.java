package fr.livelovecite.homelists;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


import fr.livelovecite.R;
import fr.livelovecite.slidingtab.PagerSlidingTabStrip;


public class HomeListAdapter  extends FragmentPagerAdapter implements PagerSlidingTabStrip.IconTabProvider{
    private static final int[] ICONS = new int[] {
            R.drawable.ic_event_start,
            R.drawable.ic_jobs,
            R.drawable.ic_market,
    };

    public HomeListAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return ICONS.length;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position)
    {

        switch (position) {
            case 0:
                return new EventsFragment();
            case 1:
                return new JobsFragment();
            case 2:
                return new MarketFragment();
            default:
                return null;
        }
    }

    @Override
    public int getPageIconResId(int position) {
        return ICONS[position];
    }
}
