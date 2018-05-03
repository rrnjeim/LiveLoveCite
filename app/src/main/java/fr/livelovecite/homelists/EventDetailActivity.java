package fr.livelovecite.homelists;

import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import fr.livelovecite.FullscreenActivity;
import fr.livelovecite.R;
import fr.livelovecite.setup.LoaderHelper;
import io.branch.referral.Branch;

public class EventDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("Title"));

        Button backBTN = findViewById(R.id.backBTN);

        assert backBTN != null;
        backBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.no_animation, R.anim.push_right_out);
            }
        });

        ImageView eventImage = findViewById(R.id.eventImageDetail);

        TextView eventLocation = findViewById(R.id.eventLocationDetail);
        eventLocation.setText(getIntent().getStringExtra("Location"));

        eventLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{
                    String daddr="" , myDest=getIntent().getStringExtra("Location");
                    String loadedJSONString = LoaderHelper.parseFileToString(EventDetailActivity.this, "maisonGeo");
                    JSONObject obj = new JSONObject(loadedJSONString);
                    JSONArray m_jArry = obj.getJSONArray("Maisons");
                    for(int i=0;i<m_jArry.length();i++) {
                        if(myDest.equals(m_jArry.getJSONObject(i).get("name").toString())){
                            daddr=m_jArry.getJSONObject(i).get("geo").toString();
                        }
                    }
                    Uri gmmIntentUri;
                    if(!daddr.equals(""))
                        gmmIntentUri = Uri.parse("google.navigation:q="+daddr+"&mode=w");
                    else
                        gmmIntentUri = Uri.parse("geo:0,0?q="+myDest);

                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
                catch (JSONException e) {e.printStackTrace();}
            }
        });

        TextView eventStart = findViewById(R.id.eventStartDetail);
        FloatingActionButton fabCalendarAdd = findViewById(R.id.fabCalendar);

        if (eventStart != null)
            eventStart.setText(getIntent().getStringExtra("Starttime"));
        fabCalendarAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");

                intent.putExtra(CalendarContract.Events.TITLE, getIntent().getStringExtra("Title"));
                intent.putExtra(CalendarContract.Events.DESCRIPTION, getIntent().getStringExtra("Description"));
                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, getIntent().getStringExtra("Location"));

                Calendar c = new GregorianCalendar();
                SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.getDefault());
                try {
                    Date date = format.parse(getIntent().getStringExtra("CalendarStart"));
                    c.setTime(date);
                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, c.getTimeInMillis());
                } catch (ParseException e) {e.printStackTrace();}


                try {
                    Date date = format.parse(getIntent().getStringExtra("CalendarEnd"));
                    c.setTime(date);
                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, c.getTimeInMillis());
                }
                catch (ParseException e){e.printStackTrace();}


                startActivity(intent);
            }
        });

        TextView eventEnd = findViewById(R.id.eventEndDetail);
        eventEnd.setText(getIntent().getStringExtra("Endtime"));

        TextView eventPrice = findViewById(R.id.eventPrice);
        if(eventPrice!=null && !getIntent().getStringExtra("Price").equals("0") && !getIntent().getStringExtra("Price").equals(""))
            eventPrice.setText(getIntent().getStringExtra("Price")+ " €");//+ " €"
        else
            eventPrice.setText("Free!");

        TextView eventDescription = findViewById(R.id.eventDescriptionDetail);
        if (eventDescription != null){
            eventDescription.setText(getIntent().getStringExtra("Description"));
        eventDescription.setMovementMethod(LinkMovementMethod.getInstance());
        }


        final String imageurl = getIntent().getStringExtra("Image");
        Picasso.with(this)
                .load(imageurl)
                .into(eventImage);
        eventImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(EventDetailActivity.this, FullscreenActivity.class);
                fullScreenIntent.putExtra("Image", imageurl);
                startActivity(fullScreenIntent);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (Branch.isAutoDeepLinkLaunch(this)) {
            try {
                String autoDeeplinkedValue = Branch.getInstance().getLatestReferringParams().getString("event_detail");
                System.out.println("Launched by Branch on auto deep linking --> "+autoDeeplinkedValue);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Launched by normal application flow");
        }
    }
}
