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
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.QueryOptions;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fr.livelovecite.R;
import fr.livelovecite.uplaods.Event;
import fr.livelovecite.uplaods.UploadEventsActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyEventsFragment extends ListFragment {
    private List<Event> totalEvents = new ArrayList<>();
    BackendlessUser user = new BackendlessUser();
    public ProgressBar progress;
    public TextView noyet;
    private static final int UPDATED_UPLOADS_STATE=2;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_uplaods, container, false);

        progress= view.findViewById(R.id.progressMy);
        progress.setVisibility(View.VISIBLE);
        noyet= view.findViewById(R.id.nothingYet);
        noyet.setVisibility(View.INVISIBLE);
        user = Backendless.UserService.CurrentUser();

        Button addEventBTN= view.findViewById(R.id.addActionBTN);
        if(!user.getProperty("maison").equals("Not a Resident")) {
            addEventBTN.setText(getString(R.string.add_event));
            addEventBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent EventPage = new Intent(getActivity(), UploadEventsActivity.class);
                    getActivity().startActivityForResult(EventPage, UPDATED_UPLOADS_STATE);
                }

            });
        }
        else addEventBTN.setVisibility(View.GONE);

        LoadMyEvents();
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        final SimpleDateFormat input = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.US);
        final SimpleDateFormat outputDate = new SimpleDateFormat("MM/dd/yyyy",Locale.US);
        final SimpleDateFormat outputTime = new SimpleDateFormat("HH:mm", Locale.US);
        Intent eventDetailIntent = new Intent(getContext(), UploadEventsActivity.class);

        eventDetailIntent.putExtra("Title", totalEvents.get(position).getTitle());
        eventDetailIntent.putExtra("Location", totalEvents.get(position).getMaison());

        String startdate = String.valueOf(totalEvents.get(position).getStarttime());
        try {
            String buzzstartDate = outputDate.format((input.parse(startdate)));
            String buzzstartTime = outputTime.format((input.parse(startdate)));
            eventDetailIntent.putExtra("Starttime", buzzstartTime);
            eventDetailIntent.putExtra("Startdate", buzzstartDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        String enddate = String.valueOf(totalEvents.get(position).getEndtime());
        try {
            String buzzendDate = outputDate.format((input.parse(enddate)));
            String buzzendTime = outputTime.format((input.parse(enddate)));
            eventDetailIntent.putExtra("Endtime", buzzendTime);
            eventDetailIntent.putExtra("Enddate", buzzendDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        eventDetailIntent.putExtra("Description",totalEvents.get(position).getDescription());
        eventDetailIntent.putExtra("Image", totalEvents.get(position).getPicture());
        eventDetailIntent.putExtra("PriceInt", totalEvents.get(position).getPriceInt());
        eventDetailIntent.putExtra("PriceExt", totalEvents.get(position).getPriceExt());
        eventDetailIntent.putExtra("EventID", totalEvents.get(position).getObjectId());
        eventDetailIntent.putExtra("Private", totalEvents.get(position).getPrivate());
        eventDetailIntent.putExtra("canEdit", "YES");

        getActivity().startActivityForResult(eventDetailIntent, UPDATED_UPLOADS_STATE);
    }

    public void LoadMyEvents(){
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause( "ownerId LIKE '"+user.getUserId()+"'");
        queryBuilder.setSortBy("created DESC");
        Backendless.Data.of(Event.class).find(queryBuilder, new AsyncCallback<List<Event>>(){
            @Override
            public void handleResponse(List<Event> eventsBackendlessCollection) {


                ArrayList<String> eventsTitles = new ArrayList<>();

                for (Event e : eventsBackendlessCollection) {
                    eventsTitles.add(e.getTitle());
                }
                totalEvents=eventsBackendlessCollection;

                if(getActivity()!=null){
                    ListAdapter adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_expandable_list_item_1, eventsTitles);
                    setListAdapter(adapter);
                }

                progress.setVisibility(View.INVISIBLE);
                if(eventsBackendlessCollection.size()==0){
                    noyet.setText(R.string.no_events_yet);
                    noyet.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void handleFault(BackendlessFault fault) {}
        });
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
