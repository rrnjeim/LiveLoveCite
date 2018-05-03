package fr.livelovecite.activities;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
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
import fr.livelovecite.setup.BackendSettings;

import static android.R.id.list;

public class ActivitiesActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Activity> totalActivities = new ArrayList<>();
    private List<Activity> activities;
    private ActivityAdapter activityAdapter;

    TextView noyet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.activities));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noyet = findViewById(R.id.nothingYet);
        noyet.setText(getString(R.string.no_activities_yet));
        noyet.setVisibility(View.INVISIBLE);
        noyet.setEnabled(false);
        noyet.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {onRefresh(); noyet.setEnabled(false);}});

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        Backendless.initApp(this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);
        Backendless.setUrl(BackendSettings.SERVER_URL);

        final ListView myListView = findViewById(list);
        activityAdapter = new ActivityAdapter(this,R.layout.activities_item_list, totalActivities);
        myListView.setAdapter(activityAdapter);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                loadActivities();
            }
        });
    }
    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        loadActivities();
        swipeRefreshLayout.setRefreshing(false);}

    private void addMoreActivities(List<Activity> nextPage) {
        totalActivities.addAll( nextPage);
        activityAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }
        public void loadActivities(){
        swipeRefreshLayout.setRefreshing(true);

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setSortBy( "created DESC" );
        Backendless.Data.of(Activity.class).find(queryBuilder, new AsyncCallback<List<Activity>>(){
            @Override
            public void handleResponse(List<Activity> activitiesCollection) {
                if (activitiesCollection==null  || activitiesCollection.size()==0){
                    swipeRefreshLayout.setRefreshing(false);
                    noyet.setVisibility(View.VISIBLE);
                    noyet.setEnabled(false);
                }
                else{
                    activityAdapter.clear();
                    noyet.setVisibility(View.INVISIBLE);
                    noyet.setEnabled(false);
                    activities=activitiesCollection;
                    addMoreActivities(activities);
                    activityAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void handleFault( BackendlessFault backendlessFault ) {
                swipeRefreshLayout.setRefreshing(false);
                if (getBaseContext()!=null)
                    Toast.makeText( ActivitiesActivity.this,getString(R.string.no_connection) , Toast.LENGTH_SHORT ).show();
                if(activities==null || activities.size()==0 ){
                    noyet.setText(R.string.retry);
                    noyet.setVisibility(View.VISIBLE);
                    noyet.setEnabled(true);
                }
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_animation, R.anim.push_right_out);
    }
}
