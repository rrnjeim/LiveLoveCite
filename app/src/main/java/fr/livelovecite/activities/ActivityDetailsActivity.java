package fr.livelovecite.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.livelovecite.FullscreenActivity;
import fr.livelovecite.R;
import fr.livelovecite.setup.LoaderHelper;

public class ActivityDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_activity);

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

        ImageView activityImage = findViewById(R.id.activityImageDetail);
        final String imageurl = getIntent().getStringExtra("Image");
        Picasso.with(this)
                .load(imageurl)
                .into(activityImage);
        activityImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(ActivityDetailsActivity.this, FullscreenActivity.class);
                fullScreenIntent.putExtra("Image", imageurl);
                startActivity(fullScreenIntent);
            }
        });


        TextView activityLocation = findViewById(R.id.activityLocationDetail);
        if (activityLocation != null)
            activityLocation.setText(getIntent().getStringExtra("Location"));
        assert activityLocation != null;
        activityLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{

                    String daddr="" , myDest=getIntent().getStringExtra("Location");
                    String loadedJSONString = LoaderHelper.parseFileToString(ActivityDetailsActivity.this, "maisonGeo");
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
        TextView activityOccurence = findViewById(R.id.activityOccurenceDetail);
        if (activityOccurence != null)
            activityOccurence.setText(getIntent().getStringExtra("Occurrence"));
        TextView activityDescription = findViewById(R.id.activityDescriptionDetail);
        if(activityDescription!=null)
            activityDescription.setText(getIntent().getStringExtra("Description"));

        Button fbBTN = findViewById(R.id.fbPageBTN);
        if(getIntent().getStringExtra("fbURL")==null || getIntent().getStringExtra("fbURL").equals("")){
            fbBTN.setVisibility(View.INVISIBLE);
            fbBTN.setClickable(false);
            fbBTN.setEnabled(false);
        }
            fbBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getIntent().getStringExtra("fbURL")==null || getIntent().getStringExtra("fbURL").equals(""))
                    return;
                Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                String facebookUrl = "fb://facewebmodal/f?href="+getIntent().getStringExtra("fbURL");
                facebookIntent.setData(Uri.parse(facebookUrl));
                startActivity(facebookIntent);

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
