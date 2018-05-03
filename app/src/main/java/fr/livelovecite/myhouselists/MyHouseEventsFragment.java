package fr.livelovecite.myhouselists;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.QueryOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import fr.livelovecite.homelists.EventInteraction;
import fr.livelovecite.homelists.EventListAdapter;
import fr.livelovecite.R;
import fr.livelovecite.uplaods.Event;
import fr.livelovecite.uplaods.Going;

import static android.R.id.list;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyHouseEventsFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener{
    private boolean isLoadingItems = false;
    private List<Event> events;
    private List<EventInteraction> totalEvents = new ArrayList<>();
    private EventInteraction eventInteraction;
    public EventListAdapter eventAdapter;
    BackendlessUser currentUser;
    SwipeRefreshLayout swipeRefreshLayout;
    public boolean loadEvent=true;
    int i;
    TextView noyet;

    public MyHouseEventsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_events, container, false);

        final ListView myListView = rootView.findViewById(list);
        if(getContext()!=null)
            eventAdapter = new EventListAdapter(getContext(),R.layout.events_item_list, totalEvents);
        myListView.setAdapter(eventAdapter);

        noyet= rootView.findViewById(R.id.nothingYet);
        noyet.setVisibility(View.INVISIBLE);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        /*
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(loadEvent)
                    loadEvents();
            }
        });

/*
        myListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (needToLoadItems(firstVisibleItem, visibleItemCount, totalItemCount)) {
                    isLoadingItems = true;

                    events.nextPage(new AsyncCallback<List<Event>>() {
                        @Override
                        public void handleResponse(List<Event> nextPage) {
                            if (nextPage.size()!=0) swipeRefreshLayout.setRefreshing(true);
                            events = nextPage;
                            addMoreEvents(nextPage);
                            isLoadingItems = false;
                        }
                        @Override
                        public void handleFault(BackendlessFault fault) {swipeRefreshLayout.setRefreshing(false);}
                    });
                }
            }
        });
*/
        return rootView;
    }
    private boolean needToLoadItems(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        return !isLoadingItems && totalItemCount != 0 && totalItemCount - (visibleItemCount + firstVisibleItem) < visibleItemCount / 2;
    }
    private void addMoreEvents(List<Event> nextPage) {
        i=nextPage.size();
        for(final Event event : nextPage){
            // Check if user is GOING
            DataQueryBuilder queryBuilder = DataQueryBuilder.create();
            queryBuilder.setWhereClause("ownerId = '" + currentUser.getUserId() + "' " +
                    "AND eventID = '"+event.getObjectId()+"'");

            Backendless.Persistence.of(Going.class).find(queryBuilder, new AsyncCallback<List<Going>>() {
                @Override
                public void handleResponse(List<Going> goingList) {
                    eventInteraction=new EventInteraction();
                    eventInteraction.setEvent(event);
                    if (goingList.size() >=1)
                        eventInteraction.setIsParticipating(true);

                    if (getContext()!=null){
                        totalEvents.add(eventInteraction);
                        Collections.sort(totalEvents, EventInteraction.Comparators.StartDate);
                    }

                    i--;
                    if(i<=1) {
                        eventAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                        loadEvent=false;
                    }
                }
                @Override
                public void handleFault(BackendlessFault backendlessFault) {i--;}
            });
            /////////////////////////
        }
    }
    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {swipeRefreshLayout.setRefreshing(false);}

    public void loadEvents(){
        swipeRefreshLayout.setRefreshing(true);
        currentUser = Backendless.UserService.CurrentUser();
        if (currentUser==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getContext());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            currentUser = gson.fromJson(json, BackendlessUser.class);
        }

        Calendar calendar = Calendar.getInstance();
        int thisYear = calendar.get(Calendar.YEAR);
        int thisMonth = calendar.get(Calendar.MONTH) + 1; // Months are from 0 - 11
        int thisDay = calendar.get(Calendar.DAY_OF_MONTH);
        String today = "" + thisMonth + "/" + thisDay + "/" + thisYear;
        String maison = currentUser.getProperty("maison").toString();
        if(maison.contains("'")) {
            String split[] = maison.split("'");
            maison=split[1];
        }

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause( "Maison LIKE '%"+maison+"%' " +
                "AND Endtime >= '"+ today +"' " +
                "AND Verified = true" );
        queryBuilder.addSortBy("Starttime ASC");
        Backendless.Data.of(Event.class).find(queryBuilder, new AsyncCallback<List<Event>>() {
            @Override
            public void handleResponse(List<Event> eventsList) {
                if(eventsList.size()!=0){
                    noyet.setVisibility(View.INVISIBLE);
                    eventAdapter.clear();
                    events = eventsList;
                    addMoreEvents(eventsList);
                }
                else{
                    noyet.setText(R.string.no_events_yet);
                    noyet.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
            @Override
            public void handleFault( BackendlessFault backendlessFault ) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText( getContext(), R.string.no_connection , Toast.LENGTH_SHORT ).show();}
        });
    }
    @Override
    public void onResume() {
        super.onResume();

        currentUser= Backendless.UserService.CurrentUser();
        if (currentUser==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            currentUser = gson.fromJson(json, BackendlessUser.class);
        }
    }
}
