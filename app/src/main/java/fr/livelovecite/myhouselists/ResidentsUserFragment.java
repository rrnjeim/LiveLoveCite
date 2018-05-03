package fr.livelovecite.myhouselists;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import fr.livelovecite.R;
import fr.livelovecite.setup.LoadingCallback;

import static android.R.id.list;


/**
 * A simple {@link Fragment} subclass.
 */
public class ResidentsUserFragment extends Fragment {
    private List<BackendlessUser> totalUsers = new ArrayList<>();
    private List<BackendlessUser> users;
    private ResidentsUserListAdapter residentsUserListAdapter;
    private boolean isLoadingItems = false;
    ProgressBar progressBar;
    TextView noyet;
    public boolean loadUsers=true;


    public ResidentsUserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_residents_user, container, false);

        final ListView myListView = rootView.findViewById(list);
        progressBar  = rootView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        noyet= rootView.findViewById(R.id.nothingYet);
        noyet.setVisibility(View.INVISIBLE);

        if(getContext()!=null)
            residentsUserListAdapter = new ResidentsUserListAdapter(getContext(),R.layout.users_item_list, totalUsers);
        myListView.setAdapter(residentsUserListAdapter);

        if(loadUsers)
            loadUsers();

/*
        myListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (needToLoadItems(firstVisibleItem, visibleItemCount, totalItemCount)) {
                    isLoadingItems = true;

                    users.nextPage(new AsyncCallback<List<BackendlessUser>>() {
                        @Override
                        public void handleResponse(List<BackendlessUser> nextPage) {
                            users = nextPage;
                            addMoreEvents(nextPage);
                            isLoadingItems = false;
                        }
                        @Override
                        public void handleFault(BackendlessFault fault) {}
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
    private void addMoreEvents(List<BackendlessUser> nextPage) {
        totalUsers.addAll(nextPage);
        residentsUserListAdapter.notifyDataSetChanged();
        loadUsers=false;
    }

    public void loadUsers(){
        BackendlessUser currentUser;
        currentUser = Backendless.UserService.CurrentUser();
        if (currentUser==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getContext());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            currentUser = gson.fromJson(json, BackendlessUser.class);
        }
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.addSortBy("name ASC");
        String maison = currentUser.getProperty("maison").toString();
        if(maison.contains("'")) {
            String split[] = maison.split("'");
            maison=split[1];
        }
        queryBuilder.setWhereClause("Maison LIKE '%"+ maison+"%' AND email NOT LIKE '"+currentUser.getEmail()+"'");
        Backendless.Data.of(BackendlessUser.class).find(queryBuilder, new LoadingCallback<List<BackendlessUser>>(getContext()) {
            @Override
            public void handleResponse(List<BackendlessUser> usersList) {
                progressBar.setVisibility(View.INVISIBLE);

                if (usersList.size()!=0){
                users = usersList;
                addMoreEvents(usersList);
                residentsUserListAdapter.notifyDataSetChanged();
                }
                else{
                    noyet.setText(R.string.no_neighboors_yet);
                    noyet.setVisibility(View.VISIBLE);
                }

            }
            @Override
            public void handleFault( BackendlessFault backendlessFault ) {
                Toast.makeText( getContext(), R.string.no_connection , Toast.LENGTH_SHORT ).show();

            }
        });
    }
}
