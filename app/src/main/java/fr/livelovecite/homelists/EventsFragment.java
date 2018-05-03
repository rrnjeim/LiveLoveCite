package fr.livelovecite.homelists;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.Collections;
import java.util.List;

import fr.livelovecite.R;
import fr.livelovecite.uplaods.Event;
import fr.livelovecite.uplaods.Going;

import static android.R.id.list;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventsFragment extends ListFragment
        implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    public List<EventInteraction> totalEvents = new ArrayList<>();
    ListView myListView;
    public EventListAdapter eventAdapter;
    DataQueryBuilder queryBuilder;
    private BackendlessUser currentUser = new BackendlessUser();
    private String filterString ;
    boolean isLoadingItems = false;
    private TextView retryConnecting;
    public EventsFragment( ) {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_events, container, false);

        initializeUI(rootView);

        myListView.setOnScrollListener( new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged( AbsListView view, int scrollState ) {}

            @Override
            public void onScroll( AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount ) {
                if( needToLoadItems( firstVisibleItem, visibleItemCount, totalItemCount ) ) {
                    isLoadingItems = true;
                    Backendless.Data.of( Event.class ).find(queryBuilder, new AsyncCallback<List<Event>>() {
                        @Override
                        public void handleResponse(List<Event> response) {
                            if (response.size()!=0)swipeRefreshLayout.setRefreshing(true);

                            addMoreEvents(response);

                            //PREPARE NEXT PAGE
                            queryBuilder.prepareNextPage();
                            isLoadingItems = false;
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            swipeRefreshLayout.setRefreshing(false);
                        }});}}});

        // Inflate the layout for this fragment
        return rootView;
    }

    private void initializeUI(View rootView) {
        currentUser=Backendless.UserService.CurrentUser();
        if (currentUser==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getContext());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            currentUser = gson.fromJson(json, BackendlessUser.class);
            this.onRefresh();
        }

        filterString=getString(R.string.newly_added);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(this);
        /*
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                    loadEvents();
            }
        });

        myListView = rootView.findViewById(list);
        if(getContext()!=null)
            eventAdapter = new EventListAdapter(getContext(),R.layout.events_item_list, totalEvents);
        myListView.setAdapter(eventAdapter);

        retryConnecting = rootView.findViewById(R.id.nothingYet);
        retryConnecting.setEnabled(false);
        retryConnecting.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onRefresh();}});

    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        loadEvents();
    }

    public void loadEvents() {
        try {
            swipeRefreshLayout.setRefreshing(true);
        }catch (NullPointerException e){e.printStackTrace();}

        buildQuery();

        final AsyncCallback<List<Event>> callback = new AsyncCallback<List<Event>>() {
            @Override
            public void handleResponse(List<Event> eventsList) {
                if( eventsList.size() != 0 ) {
                    retryConnecting.setVisibility(View.INVISIBLE);
                    retryConnecting.setEnabled(false);

                    addMoreEvents(eventsList);
                    //PREPARE NEXT PAGE
                    queryBuilder.prepareNextPage();
                }
            }
            @Override
            public void handleFault( BackendlessFault backendlessFault ) {
                if (getContext()!=null)
                    Toast.makeText( getContext(),backendlessFault.getMessage() + R.string.no_connection , Toast.LENGTH_SHORT ).show();

                if(totalEvents.size()==0){
                    retryConnecting.setText(R.string.retry);
                    retryConnecting.setVisibility(View.VISIBLE);
                    retryConnecting.setEnabled(true);
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        };
        if(getActivity()!=null) Backendless.Data.of( Event.class ).find( queryBuilder, callback );
    }

    private void buildQuery() {
        if(totalEvents.size()!=0) totalEvents.clear();
        eventAdapter.notifyDataSetChanged();

        String maison = currentUser.getProperty("maison").toString();
        if(maison.contains("'")) {
            String split[] = maison.split("'");
            maison=split[1];
        }
        queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause("Endtime at or after '"+Calendar.getInstance().getTime()+"'"
                +" AND Verified = true "
                +"AND (Private = false OR Maison LIKE '%"+maison+"%')");

        if(filterString.equals(getString(R.string.upcoming_events)))
            queryBuilder.setSortBy("Starttime ASC");
        else if(filterString.equals(getString(R.string.newly_added))){
            queryBuilder.setSortBy("Starttime ASC");
            queryBuilder.setSortBy("created ASC");
        }
        else if(filterString.equals(getString(R.string.most_popular)))
            queryBuilder.setSortBy("Participation DESC");
    }

    private boolean needToLoadItems( int firstVisibleItem, int visibleItemCount, int totalItemCount ) {
        return !isLoadingItems && totalItemCount != 0 && totalItemCount - (visibleItemCount + firstVisibleItem) < visibleItemCount / 2;
    }

    private void addMoreEvents(final List<Event> nextPage) {
        for(final Event event : nextPage){
            // Check if user is GOING
            DataQueryBuilder queryBuilder = DataQueryBuilder.create();
            queryBuilder.setWhereClause("ownerId = '" + currentUser.getUserId() + "' AND eventID = '"+event.getObjectId()+"'");
            Backendless.Data.of(Going.class).find(queryBuilder, new AsyncCallback<List<Going>>() {
                @Override
                public void handleResponse(List<Going> goingList) {
                    EventInteraction eventInteraction=new EventInteraction();
                    eventInteraction.setEvent(event);
                    if (goingList.size() >=1)
                        eventInteraction.setIsParticipating(true);

                    if (getContext()!=null){
                        totalEvents.add(eventInteraction);
                        if(filterString.equals(getString(R.string.upcoming_events)))
                            Collections.sort(totalEvents, EventInteraction.Comparators.StartDate);
                        else if(filterString.equals(getString(R.string.newly_added)))
                            Collections.sort(totalEvents, EventInteraction.Comparators.createdDate);
                        else if(filterString.equals(getString(R.string.most_popular)))
                            Collections.sort(totalEvents, EventInteraction.Comparators.Popularity);
                    }
                    eventAdapter.notifyDataSetChanged();
                }
                @Override
                public void handleFault(BackendlessFault backendlessFault) {}
            });
            //////////////////
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    public void filterEventsOnString(){
        if(getContext()!=null){
            final Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.filter_layout);
            dialog.setCancelable(true);
            dialog.show();

            final RadioGroup radioGroup = dialog.findViewById(R.id.RadioFilter);
            if(filterString.equals(getString(R.string.newly_added))) radioGroup.check(R.id.rd_2);
            else if(filterString.equals(getString(R.string.most_popular)) ) radioGroup.check(R.id.rd_3);
            else radioGroup.check(R.id.rd_1);

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId == R.id.rd_1)
                        filterString =getString(R.string.upcoming_events);
                    else if(checkedId ==  R.id.rd_2)
                        filterString =getString(R.string.newly_added);
                    else filterString =getString(R.string.most_popular);

                    // This will get the radiobutton that has changed in its check state
                    RadioButton checkedRadioButton = group.findViewById(checkedId);
                    // This puts the value (true/false) into the variable
                    boolean isChecked = checkedRadioButton.isChecked();
                    // If the radiobutton that has changed in check state is now checked...
                    if (isChecked) {
                        onRefresh();
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

}
