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
import fr.livelovecite.uplaods.Market;
import fr.livelovecite.uplaods.UploadItemSellActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MySaleItemsFragment extends ListFragment {

    private List<Market> totalItems = new ArrayList<>();
    BackendlessUser user = new BackendlessUser();
    ProgressBar progress;
    TextView noyet;
    private static final int UPDATED_UPLOADS_STATE=2;

    // Inflate the fragment layout we defined above for this fragment
    // Set the associated text for the title
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_uplaods, container, false);

        progress= view.findViewById(R.id.progressMy);
        progress.setVisibility(View.VISIBLE);
        noyet= view.findViewById(R.id.nothingYet);
        noyet.setVisibility(View.INVISIBLE);

        Button addEventBTN= view.findViewById(R.id.addActionBTN);
        addEventBTN.setText(getString(R.string.add_item_to_sell));
        addEventBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                Intent ItemPage = new Intent(getActivity(), UploadItemSellActivity.class );
                getActivity().startActivityForResult( ItemPage, UPDATED_UPLOADS_STATE );
            }

        });


        user = Backendless.UserService.CurrentUser();

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause( "ownerId LIKE '"+user.getUserId()+"'" );
        queryBuilder.setSortBy("created DESC");
        Backendless.Data.of(Market.class).find(queryBuilder, new LoadingCallback<List<Market>>(getContext()) {
            @Override
            public void handleResponse(List<Market> itemsList) {
                ArrayList<String> toSellTitles = new ArrayList<>();
                for (Market m : itemsList) {
                    toSellTitles.add(m.getTitle());
                }
                totalItems = itemsList;
                if(getActivity()!=null){
                    ListAdapter adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_expandable_list_item_1, toSellTitles);
                    setListAdapter(adapter);
                }
                progress.setVisibility(View.INVISIBLE);
                if(itemsList.size()==0){
                    noyet.setText(R.string.no_items_yet);
                    noyet.setVisibility(View.VISIBLE);
                }

                super.handleResponse(itemsList);
            }
        });
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent editItemDetailIntent = new Intent(getContext(),
                UploadItemSellActivity.class);

        editItemDetailIntent.putExtra("Title", totalItems.get(position).getTitle());
        editItemDetailIntent.putExtra("Price", totalItems.get(position).getPrice());
        editItemDetailIntent.putExtra("Tags", totalItems.get(position).gettags());
        editItemDetailIntent.putExtra("Description", totalItems.get(position).getDescription());
        editItemDetailIntent.putExtra("Image", totalItems.get(position).getPicture());
        editItemDetailIntent.putExtra("ShowNumber", totalItems.get(position).getShowNumber());
        editItemDetailIntent.putExtra("ItemID", totalItems.get(position).getObjectId());

        editItemDetailIntent.putExtra("canEdit", "YES");

        getActivity().startActivityForResult(editItemDetailIntent, UPDATED_UPLOADS_STATE);
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
