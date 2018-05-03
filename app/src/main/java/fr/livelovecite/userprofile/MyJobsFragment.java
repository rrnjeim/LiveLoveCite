package fr.livelovecite.userprofile;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.persistence.DataQueryBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import fr.livelovecite.R;
import fr.livelovecite.setup.LoadingCallback;
import fr.livelovecite.uplaods.Job;
import fr.livelovecite.uplaods.UplaodJobActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyJobsFragment extends ListFragment {
    private List<Job> totalJobs = new ArrayList<>();
    BackendlessUser user = new BackendlessUser();
    ProgressBar progress;
    TextView noyet;
    private static final int UPDATED_UPLOADS_STATE=2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_uplaods, container, false);

        progress= view.findViewById(R.id.progressMy);
        progress.setVisibility(View.VISIBLE);
        noyet= view.findViewById(R.id.nothingYet);
        noyet.setVisibility(View.INVISIBLE);

        Button addJobBTN= view.findViewById(R.id.addActionBTN);
        addJobBTN.setText(getString(R.string.add_job));
        addJobBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                Intent JobPage = new Intent(getActivity(), UplaodJobActivity.class );
                getActivity().startActivityForResult( JobPage, UPDATED_UPLOADS_STATE );
            }

        });

        user = Backendless.UserService.CurrentUser();

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause("ownerId LIKE '"+user.getUserId()+"'");
        queryBuilder.setSortBy( "created DESC" );

        Backendless.Data.of(Job.class).find(queryBuilder, new LoadingCallback<List<Job>>(getContext()) {
            @Override
            public void handleResponse(List<Job> jobsList)
            {
                ArrayList<String> jobsTitles = new ArrayList<>();
                for (Job j : jobsList) {
                    jobsTitles.add(j.getTitre());
                }

                totalJobs = jobsList;

                if(getContext()!=null){
                    ListAdapter adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_expandable_list_item_1, jobsTitles);
                    setListAdapter(adapter);}
                progress.setVisibility(View.INVISIBLE);
                if(jobsList.size()==0){
                    noyet.setText(R.string.no_jobs_yet);
                    noyet.setVisibility(View.VISIBLE);
                }
                super.handleResponse(jobsList);
            }
        });
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent editJobDetailIntent = new Intent(getContext(),
                UplaodJobActivity.class);

        editJobDetailIntent.putExtra("Title", totalJobs.get(position).getTitre());
        editJobDetailIntent.putExtra("Address", totalJobs.get(position).getLocation());
        editJobDetailIntent.putExtra("Remuneration", totalJobs.get(position).getRemuneration());
        editJobDetailIntent.putExtra("ContactEmail", totalJobs.get(position).getEmailContact());
        editJobDetailIntent.putExtra("ContactNumber", totalJobs.get(position).getNumberContact());
        editJobDetailIntent.putExtra("Description", totalJobs.get(position).getDescription());
        editJobDetailIntent.putExtra("Image", totalJobs.get(position).getPicture());
        editJobDetailIntent.putExtra("JobID", totalJobs.get(position).getObjectId());

        editJobDetailIntent.putExtra("canEdit", "YES");

        getActivity().startActivityForResult(editJobDetailIntent,UPDATED_UPLOADS_STATE);

    }

    @Override
    public void onResume() {
        super.onResume();

        user= Backendless.UserService.CurrentUser();
        if (user==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            user = gson.fromJson(json, BackendlessUser.class);
        }
    }
}
