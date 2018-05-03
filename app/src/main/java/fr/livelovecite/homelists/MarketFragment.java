package fr.livelovecite.homelists;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.livelovecite.R;
import fr.livelovecite.uplaods.Market;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarketFragment extends ListFragment
        implements SwipeRefreshLayout.OnRefreshListener{
    SwipeRefreshLayout swipeRefreshLayout;
    List<Market> totalItems = new ArrayList<>();
    ListView myListView;
    public MarketListAdapter itemAdapter;
    DataQueryBuilder queryBuilder;
    boolean loadMarket=true, isLoadingItems=false;
    TextView retryConnecting;
    public MarketFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_items, container, false);

        initializeUI(rootView);

        buildQuery();

        myListView.setOnScrollListener( new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged( AbsListView view, int scrollState ) {}

            @Override
            public void onScroll( AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount ) {
                if( needToLoadItems( firstVisibleItem, visibleItemCount, totalItemCount ) ) {
                    isLoadingItems = true;
                    Backendless.Data.of( Market.class ).find(queryBuilder, new AsyncCallback<List<Market>>() {
                        @Override
                        public void handleResponse(List<Market> response) {
                            if (response.size()!=0)swipeRefreshLayout.setRefreshing(true);

                            addMoreItems(response);

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
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout3);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(this);
            /*
     * Showing Swipe Refresh animation on activity create
     * As animation won't start on onCreate, post runnable is used
     */
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(loadMarket)
                    loadItems();
            }
        });


        myListView = rootView.findViewById(android.R.id.list);
        if(getContext()!=null)
            itemAdapter = new MarketListAdapter(getContext(), R.layout.market_item_list, totalItems);
        myListView.setAdapter(itemAdapter);

        retryConnecting = rootView.findViewById(R.id.nothingYet);
        retryConnecting.setEnabled(false);
        retryConnecting.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onRefresh();}});
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        loadItems();
    }
    public void loadItems() {
        if(loadMarket) {
            try {
                swipeRefreshLayout.setRefreshing(true);
            }catch (NullPointerException e){e.printStackTrace();}

            buildQuery();

            final AsyncCallback<List<Market>> callback = new AsyncCallback<List<Market>>() {
                @Override
                public void handleResponse(List<Market> itemsList) {
                    if( itemsList.size() != 0 ) {
                        retryConnecting.setVisibility(View.INVISIBLE);
                        retryConnecting.setEnabled(false);

                        addMoreItems(itemsList);
                        //PREPARE NEXT PAGE
                        queryBuilder.prepareNextPage();
                    }
                }
                @Override
                public void handleFault( BackendlessFault backendlessFault ) {
                    if(getContext()!=null)
                        Toast.makeText( getContext(),R.string.no_connection, Toast.LENGTH_SHORT ).show();
                    if(totalItems.size() == 0){
                        retryConnecting.setText(R.string.retry);
                        retryConnecting.setVisibility(View.VISIBLE);
                        retryConnecting.setEnabled(true);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
            };
            loadMarket=false;
            Backendless.Data.of( Market.class ).find( callback );
        }
    }
    private void buildQuery(){
        if(totalItems.size()!=0) totalItems.clear();
        itemAdapter.notifyDataSetChanged();
        loadMarket=true;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, - 1);

        queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause("created at or after '"+ calendar.getTime()+"'" + " AND Verified = true");
        queryBuilder.setSortBy("created DESC");

        System.out.println("Query output = "+ queryBuilder.getWhereClause());
    }
    private boolean needToLoadItems( int firstVisibleItem, int visibleItemCount, int totalItemCount ) {
        return !isLoadingItems && totalItemCount != 0 && totalItemCount - (visibleItemCount + firstVisibleItem) < visibleItemCount / 2;
    }
    private void addMoreItems( List<Market> nextPage ) {
        totalItems.addAll(nextPage);
        itemAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
