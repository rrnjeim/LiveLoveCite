package fr.livelovecite.search;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
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
public class SearchEventsFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {
    private boolean isLoadingItems = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Event> events;
    private List<EventInteraction> totalEvents = new ArrayList<>();
    private EventInteraction eventInteraction;
    public EventListAdapter eventAdapter;
    public Boolean loadEvent=false;
    BackendlessUser currentUser = new BackendlessUser();
    TextView noyet;
    EditText searchText;
    String filterString=null;
    int itemsFound;
    String comparator="";

    public SearchEventsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_events, container, false);

        searchText = ((AppCompatActivity ) getContext()).findViewById(R.id.searchText);
        currentUser= Backendless.UserService.CurrentUser();
        if (currentUser==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            currentUser = gson.fromJson(json, BackendlessUser.class);
        }

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        noyet = rootView.findViewById(R.id.nothingYet);
        noyet.setText(R.string.no_results);
        noyet.setVisibility(View.INVISIBLE);

        final ListView myListView = rootView.findViewById(list);
        if(getContext()!=null)
            eventAdapter = new EventListAdapter(getContext(),R.layout.events_item_list, totalEvents);
        myListView.setAdapter(eventAdapter);

        swipeRefreshLayout.setOnRefreshListener(this);

        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(searchText.getText().length()==3 && !searchText.getText().toString().equals(comparator)){
                    filterString = searchText.getText().toString();
                    comparator=filterString;
                    loadEvent=true;
                    loadEvents();
                }
                if (totalEvents.size()!=0)
                    eventAdapter.getFilter().filter(s);
            }

        });

        // Inflate the layout for this fragment
        return rootView;
    }
    private boolean needToLoadItems(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        return !isLoadingItems && totalItemCount != 0 && totalItemCount - (visibleItemCount + firstVisibleItem) < visibleItemCount / 2;
    }
    private void addMoreEvents(List<Event> nextPage) {
        itemsFound=nextPage.size();
        for(final Event event : nextPage){
            // Check if user is GOING
            DataQueryBuilder queryBuilder = DataQueryBuilder.create();
            queryBuilder.setWhereClause( "ownerId = '" + currentUser.getUserId() + "' AND eventID = '"+event.getObjectId()+"'" );
            Backendless.Persistence.of(Going.class).find(queryBuilder, new AsyncCallback<List<Going>>() {
                @Override
                public void handleResponse(List<Going> goingList) {
                    eventInteraction=new EventInteraction();
                    eventInteraction.setEvent(event);
                    if (goingList.size() >=1)
                        eventInteraction.setIsParticipating(true);
                    if (getContext()!=null){
                        totalEvents.add(eventInteraction);
                    }
                    itemsFound--;
                    if (itemsFound<=1){ swipeRefreshLayout.setRefreshing(false);
                        loadEvent=false;}
                }
                @Override
                public void handleFault(BackendlessFault backendlessFault) {}
            });
            /////////////////////////
        }
    }
    @Override
    public void onRefresh() {swipeRefreshLayout.setRefreshing(false);}

    public void loadEvents() {
        if(loadEvent) {
            swipeRefreshLayout.setRefreshing(true);
            Calendar calendar = Calendar.getInstance();
            int thisYear = calendar.get(Calendar.YEAR);
            int thisMonth = calendar.get(Calendar.MONTH) + 1; // Months are from 0 - 11
            int thisDay = calendar.get(Calendar.DAY_OF_MONTH);
            String today = "" + thisMonth + "/" + thisDay + "/" + thisYear;

            DataQueryBuilder queryBuilder = DataQueryBuilder.create();
            if(getActivity()!=null){
                queryBuilder.setWhereClause( "Endtime >= '" + today + "' " +
                        "AND Verified = true " +
                        "AND Private = false " +
                        "AND Title LIKE '%"+filterString+"%'  " );
                queryBuilder.addSortBy("Starttime ASC");
                Backendless.Data.of(Event.class).find(queryBuilder, new AsyncCallback<List<Event>>() {
                    @Override
                    public void handleResponse(List<Event> eventsList) {
                        if(eventsList.size()!=0){
                            totalEvents.clear();
                            eventAdapter.clear();
                            eventAdapter.notifyDataSetChanged();
                            noyet.setVisibility(View.INVISIBLE);
                            events = eventsList;
                            addMoreEvents(events);
                        }
                        else {
                            totalEvents.clear();
                            eventAdapter.clear();
                            eventAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                            noyet.setVisibility(View.VISIBLE);
                        }

                    }
                    @Override
                    public void handleFault( BackendlessFault backendlessFault ) {
                        if (getContext()!=null)
                            Toast.makeText( getContext(), backendlessFault.getMessage() , Toast.LENGTH_SHORT ).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }
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

        String tmp = searchText.getText().toString();
        if(tmp.length()>=3 && loadEvent){
            comparator= ""+tmp.charAt(0)+tmp.charAt(1)+tmp.charAt(2);
            searchText.setText(tmp);
            searchText.setSelection(searchText.getText().length());
        }

    }
}