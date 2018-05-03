package fr.livelovecite.sidemenu;


import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.livelovecite.slidingtab.PagerSlidingTabStrip;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;


import fr.livelovecite.homelists.EventsFragment;
import fr.livelovecite.homelists.HomeListAdapter;

import fr.livelovecite.MainActivity;
import fr.livelovecite.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment{
    PagerSlidingTabStrip tabsStrip;
    public HomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        ((AppCompatActivity )getContext()).getSupportActionBar().setTitle(getString(R.string.home));

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        final ViewPager viewPager = rootView.findViewById(R.id.viewpager);
        viewPager.setAdapter(new HomeListAdapter(getActivity().getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(2);

        // Give the PagerSlidingTabStrip the ViewPager
        tabsStrip = rootView.findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);

        // Floating Action Menu
        final FloatingActionMenu fab = rootView.findViewById(R.id.floatingActionButton);
        fab.setClosedOnTouchOutside(true);

        FloatingActionButton fabTram = rootView.findViewById(R.id.tramFAB);
        FloatingActionButton fabVelib = rootView.findViewById(R.id.velibFAB);
        FloatingActionButton fabSearch = rootView.findViewById(R.id.searchFAB);
        final FloatingActionButton fabFilter = rootView.findViewById(R.id.filterFAB);

        fabTram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.close(true);
                ((MainActivity )getActivity()).showTramPopup();
            }
        });
        fabVelib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.close(true);
                ((MainActivity)getActivity()).showVelibPopup();
            }
        });

        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.close(true);
                Intent SearchActivity = new Intent( getActivity(), fr.livelovecite.search.SearchActivity.class );
                startActivity( SearchActivity );
                getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
            }
        });

        fabFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.close(true);
                if(viewPager.getCurrentItem()==0){
                    Fragment page = getActivity()
                            .getSupportFragmentManager()
                            .findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + viewPager.getCurrentItem());
                    ((EventsFragment)page).filterEventsOnString();
                }
            }
        });
        return rootView;
    }
}