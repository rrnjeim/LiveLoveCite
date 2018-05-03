package fr.livelovecite.sidemenu;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import fr.livelovecite.slidingtab.PagerSlidingTabStrip;

import fr.livelovecite.myhouselists.MyHouseEventsFragment;
import fr.livelovecite.myhouselists.ResidentsUserFragment;
import fr.livelovecite.R;


class MyHouseAdapter extends FragmentPagerAdapter implements PagerSlidingTabStrip.IconTabProvider{
    private static final int[] ICONS = new int[] {
            R.drawable.ic_neighbours,
            R.drawable.ic_event_start,
    };


    MyHouseAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return ICONS.length;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new ResidentsUserFragment();
            case 1:
                return new MyHouseEventsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getPageIconResId(int position) {
        return ICONS[position];
    }
}
