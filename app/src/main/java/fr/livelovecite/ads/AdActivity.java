package fr.livelovecite.ads;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.squareup.picasso.Picasso;

import fr.livelovecite.FullscreenActivity;
import fr.livelovecite.R;

public class AdActivity extends AppCompatActivity {
    Ads ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button backBTN = findViewById(R.id.backBTN);

        assert backBTN != null;
        backBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.no_animation, R.anim.push_right_out);
            }
        });

        ImageView activityImage = findViewById(R.id.adImage);

        // USER
        BackendlessUser user = Backendless.UserService.CurrentUser();
        user.getProperty("name");
        TextView ownerName = findViewById(R.id.ownerName);
        ImageView ownerImage = findViewById(R.id.ownerImage);
        ownerName.setText((String)user.getProperty("name"));
        if(user.getProperty("facebookId") == null  || TextUtils.equals(user.getProperty("facebookId").toString(),"pas_de_token"))
            ownerImage.setImageResource(R.drawable.default_user_icon);
        else {
            final String pp = "https://graph.facebook.com/" +user.getProperty("facebookId") + "/picture?type=large";
            Picasso.with(this)
                    .load(pp)
                    .placeholder( R.drawable.default_user_icon)
                    .into(ownerImage);
        }

        // AD
        ad = (Ads) getIntent().getSerializableExtra("AdDetails");

        // Title on ActionBar
        getSupportActionBar().setTitle(ad.getCompanyName());

        // Load Image
        Picasso.with(this)
                .load(ad.getImage())
                .into(activityImage);
        activityImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(AdActivity.this, FullscreenActivity.class);
                fullScreenIntent.putExtra("Image", ad.getImage());
                startActivity(fullScreenIntent);
            }
        });

        // Location FAB - Hide if empty
        FloatingActionButton fabLocation = findViewById(R.id.fabLocation);
        if(ad.getCompanyAddress() == null && ad.getCompanyAddress().isEmpty())
            fabLocation.setVisibility(View.GONE);
        else
            fabLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q="+ad.getCompanyAddress());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });

        // Ad CardView
        TextView adTitle = findViewById(R.id.adTitleTV);
        adTitle.setText(ad.getTitle());
        TextView adContent = findViewById(R.id.adDescriptionTV);
        adContent.setText(ad.getDescription());

        Button moreBTN = findViewById(R.id.more_button);
        if(ad.getLink()!=null && !ad.getLink().isEmpty()){
            moreBTN.setText("About " + ad.getCompanyName());
           moreBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   try {

                       Intent i = new Intent(Intent.ACTION_VIEW);
                       i.setData(Uri.parse(ad.getLink()));
                       startActivity(i);
                   }catch (ActivityNotFoundException e){e.printStackTrace();}
                }
            });
        }
        else moreBTN.setVisibility(View.GONE);

//        // Company CardView
//        TextView companyTitle = (TextView ) findViewById(R.id.companyTitleTV);
//        companyTitle.setText(ad.getCompanyName());
//        TextView companyContent = (TextView ) findViewById(R.id.companyDescriptionTV);
//        companyContent.setText(ad.getCompanyDetails());
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
