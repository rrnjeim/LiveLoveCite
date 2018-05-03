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
import java.util.List;

import fr.livelovecite.homelists.MarketListAdapter;
import fr.livelovecite.R;
import fr.livelovecite.uplaods.Market;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchMarketFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener{
    private boolean isLoadingItems = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Market> items;
    private Market marketIteraction;
    private List<Market> totalItems = new ArrayList<>();
    public MarketListAdapter itemAdapter;
    BackendlessUser currentUser = new BackendlessUser();
    public boolean loadMarket=false;
    TextView noyet;
    EditText searchText;
    String filterString=null;
    int itemsFound;
    String comparator="", comparator2="";

    public SearchMarketFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_items, container, false);
        searchText = ((AppCompatActivity ) getContext()).findViewById(R.id.searchText);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout3);

        noyet = rootView.findViewById(R.id.nothingYet);
        noyet.setText(R.string.no_results);
        noyet.setVisibility(View.INVISIBLE);

        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if ((searchText.getText().length()==4 && !searchText.getText().toString().equals(comparator))) {
                    filterString = searchText.getText().toString();
                    comparator=filterString;
                    loadMarket=true;
                    loadItems();
                }
                 itemAdapter.getFilter().filter(s);
            }

        });

        final ListView myListView = rootView.findViewById(android.R.id.list);
        if(getContext()!=null)
            itemAdapter = new MarketListAdapter(getContext(), R.layout.market_item_list, totalItems);

        myListView.setAdapter(itemAdapter);

        swipeRefreshLayout.setOnRefreshListener(this);

/*
        myListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (needToLoadItems(firstVisibleItem, visibleItemCount, totalItemCount)) {
                    isLoadingItems = true;

                    items.nextPage(new AsyncCallback<List<Market>>() {
                        @Override
                        public void handleResponse(List<Market> nextPage) {
                            if (nextPage.size()!=0)swipeRefreshLayout.setRefreshing(true);

                            items = nextPage;
                            addMoreItems(nextPage);
                            isLoadingItems = false;
                        }
                        @Override
                        public void handleFault( BackendlessFault backendlessFault ) {swipeRefreshLayout.setRefreshing(false);}
                    });
                }
            }
        });
*/

        // Inflate the layout for this fragment
        return rootView;
    }
    private boolean needToLoadItems(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        return !isLoadingItems && totalItemCount != 0 && totalItemCount - (visibleItemCount + firstVisibleItem) < visibleItemCount / 2;
    }
    private void addMoreItems(List<Market> nextPage) {
        itemsFound=nextPage.size();
        for(final Market item : nextPage){
            //Fetch Owner user
            if(getContext()!=null){
                Backendless.Data.of(BackendlessUser.class).findById(item.getOwnerId(), new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser response) {
                        totalItems.add(item);
                        itemsFound--;
                        if(itemsFound<=1){
                            loadMarket=false;
                            swipeRefreshLayout.setRefreshing(false);}
                    }
                    @Override
                    public void handleFault( BackendlessFault backendlessFault ) {swipeRefreshLayout.setRefreshing(false);}
                });
            }
        }
    }
    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {swipeRefreshLayout.setRefreshing(false);}
    public void loadItems() {
        if(loadMarket) {
            swipeRefreshLayout.setRefreshing(true);
            currentUser = Backendless.UserService.CurrentUser();

            DataQueryBuilder queryBuilder = DataQueryBuilder.create();
            queryBuilder.setWhereClause( "title LIKE '%"+filterString+"%' " +
                    "OR tags LIKE '%"+filterString+"%' " +
                    "OR description LIKE '%"+filterString+"%' " +
                    "AND Verified = true");
            queryBuilder.addSortBy("created DESC");
            Backendless.Data.of(Market.class).find(queryBuilder, new AsyncCallback<List<Market>>() {
                @Override
                public void handleResponse(List<Market> itemsList) {
                    if(itemsList.size()==0){
                        totalItems.clear();
                        itemAdapter.clear();
                        itemAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                        noyet.setVisibility(View.VISIBLE);
                    }
                    else {
                        noyet.setVisibility(View.INVISIBLE);
                        totalItems.clear();
                        itemAdapter.clear();
                        itemAdapter.notifyDataSetChanged();
                        items = itemsList;
                        addMoreItems(items);
                    }
                }
                @Override
                public void handleFault( BackendlessFault backendlessFault ) {
                    Toast.makeText( getContext(), R.string.no_connection , Toast.LENGTH_SHORT ).show();
                    swipeRefreshLayout.setRefreshing(false);}
            });
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
        if(tmp.length()>=3 && loadMarket){
            comparator= ""+tmp.charAt(0)+tmp.charAt(1)+tmp.charAt(2);
            searchText.setText(tmp);
            searchText.setSelection(searchText.getText().length());
        }

    }
}
