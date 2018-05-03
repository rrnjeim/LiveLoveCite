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
import android.widget.ProgressBar;
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

import fr.livelovecite.homelists.JobListAdapter;
import fr.livelovecite.R;
import fr.livelovecite.uplaods.Job;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchJobsFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener{
private boolean isLoadingItems = false, loadJob=false;
private SwipeRefreshLayout swipeRefreshLayout;
private List<Job> jobs;
    private List<Job> totalJobs = new ArrayList<>();

    public JobListAdapter jobAdapter;
    BackendlessUser currentUser = new BackendlessUser();
    TextView noyet;
    EditText searchText;
    String filterString=null;
    int itemsFound;
    String comparator="", comparator2="";
    ProgressBar progressBar;



    public SearchJobsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_jobs, container, false);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout2);

        searchText = ((AppCompatActivity ) getContext()).findViewById(R.id.searchText);
        noyet = rootView.findViewById(R.id.nothingYet);
        noyet.setText(R.string.no_results);
        noyet.setVisibility(View.INVISIBLE);

        currentUser= Backendless.UserService.CurrentUser();
        if (currentUser==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            currentUser = gson.fromJson(json, BackendlessUser.class);
        }

        progressBar = rootView.findViewById(R.id.progressBar);

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
                    loadJob=true;
                    loadJobs();
                }
                    jobAdapter.getFilter().filter(s);
            }

        });


        final ListView myListView = rootView.findViewById(android.R.id.list);
        if(getContext()!=null)
            jobAdapter = new JobListAdapter(getContext(),R.layout.jobs_item_list ,totalJobs);
        myListView.setAdapter(jobAdapter);
/*
        myListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (needToLoadItems(firstVisibleItem, visibleItemCount, totalItemCount)) {
                    isLoadingItems = true;

                    jobs.nextPage(new AsyncCallback<List<Job>>() {
                        @Override
                        public void handleResponse(List<Job> nextPage) {
                            if (nextPage.size()!=0)swipeRefreshLayout.setRefreshing(true);

                            jobs = nextPage;
                            addMoreJobs(nextPage);
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
    private void addMoreJobs(List<Job> nextPage) {
        itemsFound=nextPage.size();
        for (final Job job: nextPage ){
            totalJobs.add(job);
            itemsFound --;
            if (itemsFound<=1) {
                loadJob=false;
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {swipeRefreshLayout.setRefreshing(false);}
    public void loadJobs() {
        if(loadJob) {
            swipeRefreshLayout.setRefreshing(true);

            DataQueryBuilder queryBuilder = DataQueryBuilder.create();
            queryBuilder.setWhereClause( "Titre LIKE '%"+filterString+"%' " +
                    "OR Description LIKE '%"+filterString+"%' " +
                    "AND Verified = true");
            queryBuilder.addSortBy("created DESC");
            Backendless.Data.of(Job.class).find(queryBuilder, new AsyncCallback<List<Job>>() {
                @Override
                public void handleResponse(List<Job> jobsList) {
                    if(jobsList.size()==0){
                        totalJobs.clear();
                        jobAdapter.clear();
                        jobAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                        noyet.setVisibility(View.VISIBLE);
                    }
                    else{
                        noyet.setVisibility(View.INVISIBLE);
                        totalJobs.clear();
                        jobAdapter.clear();
                        jobAdapter.notifyDataSetChanged();
                        jobs = jobsList;
                        addMoreJobs(jobs);
                    }
                }
                @Override
                public void handleFault( BackendlessFault backendlessFault ) {
                    Toast.makeText( getContext(), R.string.no_connection , Toast.LENGTH_SHORT ).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
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
        if(tmp.length()>=3 && loadJob){
            comparator= ""+tmp.charAt(0)+tmp.charAt(1)+tmp.charAt(2);
            searchText.setText(tmp);
            searchText.setSelection(searchText.getText().length());
        }

    }

}

