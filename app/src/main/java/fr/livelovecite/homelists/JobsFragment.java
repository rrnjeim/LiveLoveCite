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
import java.util.List;

import fr.livelovecite.R;
import fr.livelovecite.uplaods.Job;

/**
 * A simple {@link Fragment} subclass.
 */
public class JobsFragment extends ListFragment
        implements SwipeRefreshLayout.OnRefreshListener{
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Job> totalJobs = new ArrayList<>();
    ListView myListView;
    public JobListAdapter jobAdapter;
    DataQueryBuilder queryBuilder;
    boolean isLoadingItems=false;
    TextView retryConnecting;
    public JobsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_jobs, container, false);

        initializeUI(rootView);

        myListView.setOnScrollListener( new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged( AbsListView view, int scrollState ) {}

            @Override
            public void onScroll( AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount ) {
                if( needToLoadItems( firstVisibleItem, visibleItemCount, totalItemCount ) ) {
                    isLoadingItems = true;
                    Backendless.Data.of( Job.class ).find(queryBuilder, new AsyncCallback<List<Job>>() {
                        @Override
                        public void handleResponse(List<Job> response) {
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
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout2);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(this);
        /*
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                loadJobs();
            }
        });

        myListView = rootView.findViewById(android.R.id.list);
        if(getContext()!=null)
            jobAdapter = new JobListAdapter(getContext(),R.layout.jobs_item_list ,totalJobs);
        myListView.setAdapter(jobAdapter);

        retryConnecting = rootView.findViewById(R.id.nothingYet);
        retryConnecting.setEnabled(false);
        retryConnecting.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onRefresh();}});
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        loadJobs();
    }

    public void loadJobs() {
            try {
                swipeRefreshLayout.setRefreshing(true);
            }catch (NullPointerException e){e.printStackTrace();}

            buildQuery();

            final AsyncCallback<List<Job>> callback = new AsyncCallback<List<Job>>() {
                @Override
                public void handleResponse(List<Job> jobsList) {
                    if( jobsList.size() != 0 ) {
                        retryConnecting.setVisibility(View.INVISIBLE);
                        retryConnecting.setEnabled(false);

                        addMoreItems(jobsList);
                        //PREPARE NEXT PAGE
                        queryBuilder.prepareNextPage();
                    }
                }
                @Override
                public void handleFault( BackendlessFault backendlessFault ) {
                    if(getContext()!=null)
                        Toast.makeText( getContext(), R.string.no_connection , Toast.LENGTH_SHORT ).show();
                    if(totalJobs.size() == 0){
                        retryConnecting.setText(R.string.retry);
                        retryConnecting.setVisibility(View.VISIBLE);
                        retryConnecting.setEnabled(true);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
            };
            Backendless.Data.of( Job.class ).find( queryBuilder, callback );
    }

    private void buildQuery(){
        if(totalJobs.size()!=0) totalJobs.clear();
        jobAdapter.notifyDataSetChanged();

        queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause("Verified = true");
        queryBuilder.setSortBy("created DESC");
        queryBuilder.setPageSize(5);
    }

    private boolean needToLoadItems( int firstVisibleItem, int visibleItemCount, int totalItemCount ) {
        return !isLoadingItems && totalItemCount != 0 && totalItemCount - (visibleItemCount + firstVisibleItem) < visibleItemCount / 2;
    }

    private void addMoreItems( List<Job> nextPage ) {
        totalJobs.addAll(nextPage);
        jobAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
}