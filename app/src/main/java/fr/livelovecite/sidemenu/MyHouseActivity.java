package fr.livelovecite.sidemenu;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import fr.livelovecite.slidingtab.PagerSlidingTabStrip;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import fr.livelovecite.FullscreenActivity;
import fr.livelovecite.R;
import fr.livelovecite.setup.BackendSettings;
import fr.livelovecite.uplaods.Maisons;

public class MyHouseActivity extends AppCompatActivity {
    BackendlessUser user = new BackendlessUser();
    String houseNumber="N/A", houseEmail="N/A", houseAddress="N/A", houseCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_house);

        Backendless.setUrl("http://api.backendless.com");
        Backendless.initApp(this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user = Backendless.UserService.CurrentUser();
        if (user==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(MyHouseActivity.this);
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            user = gson.fromJson(json, BackendlessUser.class);
        }

        getSupportActionBar().setTitle(user.getProperty("maison").toString());

        final ImageView houseImage = findViewById(R.id.myHouseImage);
        houseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(houseCover.isEmpty()) return;

                Intent fullScreenIntent = new Intent(MyHouseActivity.this, FullscreenActivity.class);
                fullScreenIntent.putExtra("Image", houseCover);
                startActivity(fullScreenIntent);
            }
        });

        final Button contactBTN = findViewById(R.id.contactMaison);
        contactBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(MyHouseActivity.this);
                dialog.setContentView(R.layout.dialog_maison_contact);
                dialog.setCancelable(true);
                try {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                }catch (NullPointerException e){e.printStackTrace();}
                dialog.show();

                TextView tvEmail = dialog.findViewById(R.id.tv_email);
                tvEmail.setText(houseEmail);
                tvEmail.findViewById(R.id.tv_email).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(houseEmail.equals("N/A")) return;

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("text/html");
                        i.putExtra(android.content.Intent.EXTRA_EMAIL  , new String[]{houseEmail});
                        i.putExtra(Intent.EXTRA_SUBJECT, user.getProperty("name").toString());
                        i.putExtra(Intent.EXTRA_TEXT   , "\n\nSent from Live Love Cite Android App");
                        try {
                            startActivity(Intent.createChooser(i, getString(R.string.send_email)));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(MyHouseActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                TextView tvCall = dialog.findViewById(R.id.tv_call);
                tvCall.setText(houseNumber);
                tvCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(houseNumber.equals("N/A")) return;

                        String uri = "tel:" + houseNumber.trim() ;
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse(uri));
                        startActivity(intent);
                    }
                });

                TextView tvAddress = dialog.findViewById(R.id.tv_address);
                tvAddress.setText(houseAddress);
                tvAddress.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(houseAddress.equals("N/A")) return;

                        try{
                            String daddr="" , myMaison=user.getProperty("maison").toString();
                            JSONObject obj = new JSONObject(loadJSONFromAsset());
                            JSONArray m_jArry = obj.getJSONArray("Maisons");
                            for(int i=0;i<m_jArry.length();i++) {
                                if(myMaison.equals(m_jArry.getJSONObject(i).get("name").toString())){
                                    daddr=m_jArry.getJSONObject(i).get("geo").toString();
                                }
                            }

                            Uri gmmIntentUri = Uri.parse("google.navigation:q="+daddr+"&mode=w");
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        }
                        catch (JSONException e) {e.printStackTrace();}
                    }
                });
            }
        });

        String maison = user.getProperty("maison").toString();
        if(maison.contains("'")) {
            String split[] = maison.split("'");
            maison=split[1];
        }

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause("Maison LIKE '%"+maison+"%'" );


            Backendless.Data.of(Maisons.class).find(queryBuilder, new AsyncCallback<List<Maisons>>() {
                @Override
                public void handleResponse(final List<Maisons> maisonsList) {
                    if(maisonsList.size()!=0){
                        if(maisonsList.get(0).getEmail()!=null
                                && !TextUtils.equals(maisonsList.get(0).getEmail(), "N/A")) {
                            houseEmail = maisonsList.get(0).getEmail();

                        }
                        if(maisonsList.get(0).getAdresse()!=null
                                && !TextUtils.equals(maisonsList.get(0).getAdresse(), "N/A")) {
                            houseAddress = maisonsList.get(0).getAdresse();
                        }

                        if(maisonsList.get(0).getPhone()!=null
                                && !TextUtils.equals(maisonsList.get(0).getPhone(),"N/A")){
                            houseNumber = maisonsList.get(0).getPhone();
                        }

                        if(maisonsList.get(0).getPicture()!=null){
                            houseCover = maisonsList.get(0).getPicture();
                            Picasso.with(MyHouseActivity.this)
                                    .load(houseCover)
                                    .placeholder(R.drawable.cover)
                                    .into(houseImage);
                        }

                        SharedPreferences appSharedPrefs = PreferenceManager
                                .getDefaultSharedPreferences(MyHouseActivity.this);
                        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(maisonsList.get(0));
                        prefsEditor.putString("UserMaison", json);
                        prefsEditor.apply();
                    }

                }
            @Override
            public void handleFault(BackendlessFault backendlessFault ) {
                SharedPreferences appSharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(MyHouseActivity.this);
                Gson gson = new Gson();
                String json = appSharedPrefs.getString("UserMaison", "");
                Maisons currentMaison = gson.fromJson(json, Maisons.class);
                if(currentMaison!=null){
                    houseNumber = currentMaison.getPhone();
                    houseEmail = currentMaison.getEmail();
                    houseAddress = currentMaison.getAdresse();
                    houseCover = currentMaison.getPicture();
                }
                else
                    Toast.makeText( getBaseContext(), R.string.no_connection , Toast.LENGTH_SHORT ).show();
            }
        });

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = findViewById(R.id.viewpagerHouse);
        viewPager.setAdapter(new MyHouseAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(2);

        PagerSlidingTabStrip tabsStrip;
        // Give the PagerSlidingTabStrip the ViewPager
        tabsStrip = findViewById(R.id.tabHouse);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);
    }
    public String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = getAssets().open("maisonGeo");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
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
    public void onResume(){
        super.onResume();
        user= Backendless.UserService.CurrentUser();
        if (user==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this.getBaseContext());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            user = gson.fromJson(json, BackendlessUser.class);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_animation, R.anim.push_right_out);
    }
}
