package fr.livelovecite;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import fr.livelovecite.activities.ActivitiesActivity;
import fr.livelovecite.ads.AdActivity;
import fr.livelovecite.ads.Ads;
import fr.livelovecite.homelists.EventDetailActivity;

import fr.livelovecite.login.Login;
import fr.livelovecite.login.SplashActivity;
import fr.livelovecite.login.TermsAndConditionsActivity;
import fr.livelovecite.push.AdminMessagePush;
import fr.livelovecite.push.PushMessageDetails;
import fr.livelovecite.push.PushMessagesListActivity;
import fr.livelovecite.setup.BackendSettings;
import fr.livelovecite.setup.LoaderHelper;
import fr.livelovecite.setup.PrefUtils;
import fr.livelovecite.sidemenu.BiblioActivity;
import fr.livelovecite.sidemenu.CiteUnieWebViewActivity;
import fr.livelovecite.sidemenu.HomeFragment;
import fr.livelovecite.sidemenu.MyHouseActivity;
import fr.livelovecite.sidemenu.NewAboutUsActivity;
import fr.livelovecite.sidemenu.RestaurantsMenuActivity;
import fr.livelovecite.sidemenu.SportsActivity;
import fr.livelovecite.sidemenu.TheatreDeLaCiteActivity;
import fr.livelovecite.uplaods.UplaodJobActivity;
import fr.livelovecite.uplaods.UploadEventsActivity;
import fr.livelovecite.uplaods.UploadItemSellActivity;
import fr.livelovecite.userprofile.ResidentInfo;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BackendlessUser user= new BackendlessUser();
    private NavigationView navigationView = null;

    private TimerTask doAsynchronousTask;
    boolean doubleBackToExitPressedOnce = false;
    int itemSelected;

    Button notifCount;
    private Menu mOptionsMenu;

    @Override
    public void onStart() {
        super.onStart();
        Branch branch = Branch.getInstance(getApplicationContext());

        branch.initSession(new Branch.BranchReferralInitListener(){
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user clicked -> was re-directed to this app
                    // params will be empty if no data found
                    Intent eventDetailIntent = new Intent(MainActivity.this, EventDetailActivity.class);
                    try{
                        eventDetailIntent.putExtra("Title", referringParams.getString("Title").replace("\\n","\n"));
                        eventDetailIntent.putExtra("Location", referringParams.getString("Location"));

                        SimpleDateFormat output = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.US);
                        SimpleDateFormat input = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                        String calstart="", calend="";
                        try {
                            String startdate =String.valueOf(referringParams.getString("CalendarStart"));
                            calstart = output.format(input.parse(startdate));
                        } catch (ParseException e) {e.printStackTrace();}
                        try {
                            String startdate =String.valueOf(referringParams.getString("CalendarEnd"));
                            calend = output.format(input.parse(startdate));
                        } catch (ParseException e) {e.printStackTrace();}

                        eventDetailIntent.putExtra("CalendarStart", calstart);
                        eventDetailIntent.putExtra("CalendarEnd", calend);
                        eventDetailIntent.putExtra("Description", referringParams.getString("Description").replace("\\n","\n"));
                        eventDetailIntent.putExtra("Image",referringParams.getString("Image"));

                        BackendlessUser backendlessUser = Backendless.UserService.CurrentUser() ;
                        if(backendlessUser.getProperty("maison").toString().equals(referringParams.getString("Location")))
                            eventDetailIntent.putExtra("Price", referringParams.getString("PriceInt"));
                        else
                            eventDetailIntent.putExtra("Price", referringParams.getString("PriceExt"));

                        String buzzstart="", buzzend="";
                        SimpleDateFormat input1 = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.US);
                        SimpleDateFormat output1 = new SimpleDateFormat("dd MMM yyyy @ HH:mm", Locale.US);
                        try {
                            String startdate =String.valueOf(calstart);
                            buzzstart = output1.format(input1.parse(startdate));
                        } catch (ParseException e) {e.printStackTrace();}

                        try {
                            String enddate = String.valueOf(calend);
                            buzzend = output1.format(input1.parse(enddate));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        eventDetailIntent.putExtra("Starttime", buzzstart);
                        eventDetailIntent.putExtra("Endtime", buzzend);

                        startActivity(eventDetailIntent);
                    }catch (JSONException e){e.printStackTrace();}

                } else {
                    Log.i("LiveLoveCite", error.getMessage());
                }
            }
        }, this.getIntent().getData(), this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the fragment initially
        HomeFragment homeFragment = new HomeFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, homeFragment);
        fragmentTransaction.commit();

        itemSelected=R.id.nav_home;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);

        //How to change elements in the header programatically
        View headerView = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        Backendless.setUrl("http://api.backendless.com");
        Backendless.initApp(this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);

        if (Backendless.UserService.CurrentUser()==null) {
            startActivity( new Intent( MainActivity.this, SplashActivity.class ) );
            finish();
        }
        else {

            user = Backendless.UserService.CurrentUser();
            ImageView userProfilePic = headerView.findViewById(R.id.profile_image);
            userProfilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, ResidentInfo.class));
                    overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
                }
            });

            if (user.getProperty("facebookId") == null || TextUtils.equals(( String ) user.getProperty("facebookId"), "pas_de_token"))
                userProfilePic.setImageResource(R.drawable.default_user_icon);
            else
                Picasso.with(this)
                        .load("https://graph.facebook.com/" + user.getProperty("facebookId") + "/picture?type=large")
                        .placeholder(R.drawable.default_user_icon)
                        .into(userProfilePic);

            final TextView userMail = headerView.findViewById(R.id.user_name);
            if (user.getProperty("name") != null)
                userMail.setText(( String ) user.getProperty("name"));
            userMail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.equals(userMail.getText().toString(), null))
                        startActivity(new Intent(MainActivity.this, ResidentInfo.class));
                    overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
                }
            });

            TextView userName = headerView.findViewById(R.id.user_email);
            if (user.getEmail() != null && Patterns.EMAIL_ADDRESS.matcher(user.getEmail()).matches())
                userName.setText(user.getEmail());
            else if (user.getProperty("email_bis") != null)
                userName.setText(user.getProperty("email_bis").toString());
            userName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, ResidentInfo.class));
                    overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
                }
            });

            // Register to channels
            List<String> channels = new ArrayList<>();
            channels.add("default");
            channels.add("testing6493");
            channels.add(user.getProperty("maison").toString());
            Calendar cal = Calendar.getInstance(); //Get the Calendar instance
            cal.add(Calendar.MONTH,4);//Three months from now
            Backendless.Messaging.registerDevice("32461687890", channels, cal.getTime());

            // Check if Tram or Vélib' or Nothing
            String VelibValue = PrefUtils.getFromPrefs(MainActivity.this, PrefUtils.PREFS_VELIB_STATION_ID, "0");
            if(!VelibValue.equals("0"))
                velibAsynchronousTask();
            else
                tramAsynchronousTask();

            // Current Ad on top
            getCurrentAd();

            int unreadMSGS= Integer.valueOf(PrefUtils.getFromPrefs(getApplicationContext(),PrefUtils.PREFS_NOTIF_UNREAD_MESSAGES,"0"));
            int appVersionMSGS= Integer.valueOf(PrefUtils.getFromPrefs(getApplicationContext(),PrefUtils.PREFS_NOTIF_APP_UPDATE,"0"));
            if( appVersionMSGS>getAppVersion(this)){
                Log.i("AppVersion", "fromPrefs = "+appVersionMSGS+" and "+"from app = "+getAppVersion(this));
                showUpdateAppDialog();
            }

            else if(unreadMSGS==1){
                List<AdminMessagePush> messagePushList;
                SharedPreferences appSharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(this);
                Gson gson = new Gson();
                String json = appSharedPrefs.getString("MessagesList", "");
                if(!json.isEmpty()){
                    Type type = new TypeToken<List<AdminMessagePush>>(){}.getType();
                    messagePushList = gson.fromJson(json, type);

                    if(messagePushList.get(0).getTag()==null || messagePushList.get(0).getTag().equals("llc_admin_message")) {
                        Intent messageDetailsIntent = new Intent(this, PushMessageDetails.class);
                        messageDetailsIntent.putExtra("Message Detail", messagePushList.get(0));
                        startActivity(messageDetailsIntent);
                    }
                    else if(messagePushList.get(0).getTag().equals("llc_admin_image")){
                        Intent fullScreenIntent = new Intent(this, FullscreenActivity.class);
                        fullScreenIntent.putExtra("Image", messagePushList.get(0).getContentMessage());
                        startActivity(fullScreenIntent);
                    }

                    PrefUtils.saveToPrefs(MainActivity.this,PrefUtils.PREFS_NOTIF_UNREAD_MESSAGES,"0");
                }

            }
            else if(unreadMSGS>1){
                startActivity(new Intent(this,PushMessagesListActivity.class));
                PrefUtils.saveToPrefs(MainActivity.this,PrefUtils.PREFS_NOTIF_UNREAD_MESSAGES,"0");
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        mOptionsMenu=menu;

        if(!PrefUtils.getFromPrefs(MainActivity.this,PrefUtils.PREFS_NOTIF_UNREAD_MESSAGES,"0").equals("0")){
            MenuItem item = menu.findItem(R.id.action_notification_messages);
            MenuItemCompat.setActionView(item, R.layout.feed_update_count);
            notifCount = (Button) MenuItemCompat.getActionView(item);
            notifCount.setText(PrefUtils.getFromPrefs(MainActivity.this,PrefUtils.PREFS_NOTIF_UNREAD_MESSAGES,"0"));

            notifCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, PushMessagesListActivity.class));

                    PrefUtils.saveToPrefs(MainActivity.this,PrefUtils.PREFS_NOTIF_UNREAD_MESSAGES,"0");

                    mOptionsMenu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_admin_message));
                    invalidateOptionsMenu();
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_notification_messages:
                startActivity(new Intent(MainActivity.this, PushMessagesListActivity.class));

                PrefUtils.saveToPrefs(MainActivity.this,PrefUtils.PREFS_NOTIF_UNREAD_MESSAGES,"0");

                mOptionsMenu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_admin_message));
                invalidateOptionsMenu();

                return true;

            case R.id.action_add:
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                String[] types = { getString(R.string.add_event), getString(R.string.add_job), getString(R.string.add_item_to_sell)};
                b.setItems(types, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        switch(which){
                            case 0:
                                if(!user.getProperty("maison").equals("Not a Resident")) {
                                    Intent eventPage = new Intent(MainActivity.this, UploadEventsActivity.class);
                                    startActivity(eventPage);
                                }
                                else
                                    showNotResidentAlertDialog();
                                break;
                            case 1:
                                Intent jobPage = new Intent(MainActivity.this, UplaodJobActivity.class );
                                startActivity(jobPage);
                                break;
                            case 2:
                                Intent sellPage = new Intent(MainActivity.this, UploadItemSellActivity.class );
                                startActivity(sellPage);
                                break;
                        }
                    }

                });
                b.show();
                return true;

            default:return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (item.getItemId() == R.id.nav_home && item.getItemId()!=itemSelected) {
            itemSelected=item.getItemId();

            HomeFragment homeFragment = new HomeFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,homeFragment);
            fragmentTransaction.commit();
        }
        else if (item.getItemId() == R.id.nav_activities) {
            navigationView.setCheckedItem(itemSelected);
            Intent Activities = new Intent( MainActivity.this, ActivitiesActivity.class );
            startActivity( Activities );
            overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
        }
        else if (item.getItemId() == R.id.nav_library) {
            navigationView.setCheckedItem(itemSelected);
            Intent Activities = new Intent( MainActivity.this, BiblioActivity.class );
            startActivity( Activities );
            overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
        }
        else if (item.getItemId() == R.id.nav_sports) {
            navigationView.setCheckedItem(itemSelected);
            Intent Activities = new Intent( MainActivity.this, SportsActivity.class );
            startActivity( Activities );
            overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
        }
        else if(item.getItemId() == R.id.nav_my_house) {
            navigationView.setCheckedItem(itemSelected);
            if(!user.getProperty("maison").equals("Not a Resident")){
                Intent MyHouse = new Intent( MainActivity.this, MyHouseActivity.class );
                startActivity( MyHouse );
                overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
            }
            else {
                showNotResidentAlertDialog();
                navigationView.setCheckedItem(R.id.nav_home);
            }
        }
        else if (item.getItemId() == R.id.nav_go_to) {

            Dialog dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.goto_layout);
            dialog.setCancelable(true);
            dialog.show();

            final Spinner myLocationSpinner = dialog.findViewById(R.id.FromList);
            final Spinner myDestinationSpinner = dialog.findViewById(R.id.ToList);
            ArrayAdapter adapter1 = ArrayAdapter.createFromResource(this, R.array.maison_arrays_mylocation, R.layout.spinner_item_uploads);
            ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.locations_array, R.layout.spinner_item_uploads);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            adapter1.setDropDownViewResource(R.layout.spinner_dropdown_item);
            myLocationSpinner.setAdapter(adapter1);
            myDestinationSpinner.setAdapter(adapter);

            dialog.findViewById(R.id.openGoogleMapsBTN).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.i("GoTo Button", "Go to button clicked");
                    if (!TextUtils.equals(myDestinationSpinner.getSelectedItem().toString(), getString(R.string.select_location)))
                        openGoogleMaps(myLocationSpinner, myDestinationSpinner);
                }
            });
        }
        else if (item.getItemId() == R.id.nav_restaurant_menu) {
            navigationView.setCheckedItem(itemSelected);

            Intent Resto = new Intent( MainActivity.this, RestaurantsMenuActivity.class );
            startActivity( Resto );
            overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
        }
        else if (item.getItemId() == R.id.nav_theatre_cite) {
            navigationView.setCheckedItem(itemSelected);
            Intent Theatre = new Intent( MainActivity.this, TheatreDeLaCiteActivity.class );
            startActivity( Theatre );
            overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
        }
        else if (item.getItemId() == R.id.nav_cite_unie) {
            navigationView.setCheckedItem(itemSelected);
            Intent CiteUnie = new Intent( MainActivity.this, CiteUnieWebViewActivity.class );
            startActivity( CiteUnie );
            overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
        }
        else if (item.getItemId() == R.id.nav_jardin_du_monde) {
            navigationView.setCheckedItem(itemSelected);
            Intent JardinDuMonde = new Intent(Intent.ACTION_VIEW);
            String facebookUrl = getJardinDuMondeFacebookPageURL(this);
            JardinDuMonde.setData(Uri.parse(facebookUrl));
            startActivity( JardinDuMonde );
            overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
        }
        else if (item.getItemId() == R.id.nav_velo_volant) {
            navigationView.setCheckedItem(itemSelected);
            Intent LeVeloVolant = new Intent(Intent.ACTION_VIEW);
            String facebookUrl = getVeloVolantFacebookPageURL(this);
            LeVeloVolant.setData(Uri.parse(facebookUrl));
            startActivity( LeVeloVolant );
            overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
        }
        else if (item.getItemId() == R.id.nav_about_us) {
            navigationView.setCheckedItem(itemSelected);
            Intent AboutUs = new Intent( MainActivity.this, NewAboutUsActivity.class );
            startActivity( AboutUs );
            overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
        }
        else if (item.getItemId() == R.id.nav_bug_report) {
            String version=" ";
            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                version = "version "+pInfo.versionName + " code "+pInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {e.printStackTrace();}
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/html");
            i.putExtra(android.content.Intent.EXTRA_EMAIL  ,  new String[]{"livelovecite@gmail.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "Bug Report - Android "+version);
            i.putExtra(Intent.EXTRA_TEXT   , "\n\nSent from Live Love Cité mobile app");
            try {
                startActivity(Intent.createChooser(i, getString(R.string.send_email)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (item.getItemId()==R.id.nav_facebook){
            Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
            String facebookUrl = getFacebookPageURL(this);
            facebookIntent.setData(Uri.parse(facebookUrl));
            startActivity(facebookIntent);
        }
        else if (item.getItemId()==R.id.nav_insta){
            Uri uri = Uri.parse("http://instagram.com/_u/livelovecite");
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
            likeIng.setPackage("com.instagram.android");
            try {
                startActivity(likeIng);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://instagram.com/livelovecite")));
            }
        }
        else if(item.getItemId() == R.id.nav_snap){
            Intent nativeAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://snapchat.com/add/livelovecite"));
            startActivity(nativeAppIntent);
        }
        else if(item.getItemId() == R.id.nav_share) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Live Love Cité");
            String sAux = "https://livelovecite.app.link/G8iBfRu1Hx";
// "Download Live Love Cité:\n\n\n";
//            sAux = sAux + "for Android--> https://play.google.com/store/apps/details?id=fr.livelovecite\n\n";
//            sAux = sAux + "for iOS--> https://itunes.apple.com/fr/app/live-love-cite/id1142669265?mt=8";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, getString(R.string.share)));
        }
        else if(item.getItemId() == R.id.nav_term_conditions){
            Intent registrationIntent = new Intent( this, TermsAndConditionsActivity.class );
            startActivity( registrationIntent);
            overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
        }

        return true;
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
            navigationView.setCheckedItem(itemSelected);
        if(Login.isKill)
            finish();
    }

    @Override
    public void onBackPressed() {
         DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }
    //method to get the right URL to use in the intent
    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String FACEBOOK_URL = "https://www.facebook.com/livelovecite";
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                String FACEBOOK_PAGE_ID = "1687757774883993";
                return "fb://page/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }
    public String getJardinDuMondeFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String FACEBOOK_URL = "https://www.facebook.com/jardin.du.monde.ciup/";
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                String FACEBOOK_PAGE_ID = "1484737121830225";
                return "fb://page/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }
    public String getVeloVolantFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String FACEBOOK_URL = "https://www.facebook.com/levelovolant/";
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                String FACEBOOK_PAGE_ID = "504732753040805";
                return "fb://page/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }
    private void openGoogleMaps(Spinner myLocationSpinner, Spinner myDestinationSpinner){
        String saddr="0", daddr="0" ,
                myLoc = myLocationSpinner.getSelectedItem().toString(),
                myDest=myDestinationSpinner.getSelectedItem().toString();
        try {
            String loadedJSONString = LoaderHelper.parseFileToString(this, "maisonGeo");
            JSONObject obj = new JSONObject(loadedJSONString);
            JSONArray m_jArry = obj.getJSONArray("Maisons");
            for(int i=0;i<m_jArry.length();i++) {
                if(myLoc.equals(m_jArry.getJSONObject(i).get("name").toString()))
                    saddr=m_jArry.getJSONObject(i).get("geo").toString();
                if(myDest.equals(m_jArry.getJSONObject(i).get("name").toString()))
                    daddr=m_jArry.getJSONObject(i).get("geo").toString();
            }

            Uri gmmIntentUri;
            if(myLocationSpinner.getSelectedItem().toString().equals(getString(R.string.startpoint_prompt)))
                gmmIntentUri = Uri.parse("google.navigation:q="+daddr+"&mode=w");
            else
                gmmIntentUri= Uri.parse("http://maps.google.com/maps?saddr="+saddr
                        +"&daddr="+daddr+"");
            // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            // Make the Intent explicit by setting the Google Maps package
            mapIntent.setPackage("com.google.android.apps.maps");

            // Attempt to start an activity that can handle the Intent
            startActivity(mapIntent);
            startActivity(mapIntent);

        } catch (JSONException e) {e.printStackTrace();}
    }


    // TRANSPORT HORRAIRE
    @NonNull
    private String readIt(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line ;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    @NonNull
    private String fetchHorraires(String myUrl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            return readIt(is);

        } finally {
            if (is != null ) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class FetchHorraireTramAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return fetchHorraires(urls[0]);
            } catch (IOException e) {
                return "Unable to fetch horraires. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.i("RATP", result);

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject jsonResponse= jsonObject.getJSONObject("result");
                JSONArray jsonSchedules = jsonResponse.getJSONArray("schedules");

                String stationFromPrefs = PrefUtils.getFromPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_STATION_ID,"0");
                String station="Cité Universitaire";
                if(stationFromPrefs.equals("montsouris")) station="Montsouris";
                else if(stationFromPrefs.equals("cite+universitaire")) station="Cité Universitaire";
                String dest = jsonSchedules.getJSONObject(0).getString("destination");

                String timeLeft = jsonSchedules.getJSONObject(0).getString("message");
                timeLeft =timeLeft+", " +jsonSchedules.getJSONObject(1).getString("message");

                if(getBaseContext()!=null){
                    try {

                        TextView horraireTV = findViewById(R.id.horraireTV);
                        horraireTV.setText(station + " ⇨ " + dest + " : " + timeLeft);
                        ImageView bikeIconToTram = findViewById(R.id.tram_icon);
                        bikeIconToTram.setImageDrawable(getDrawable(R.drawable.ic_tram));
                        findViewById(R.id.tram_card).setVisibility(View.VISIBLE);
                        horraireTV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showTramPopup();
                            }
                        });
                    }catch (NullPointerException e){e.printStackTrace();}
                }

                }
            catch (JSONException e) {
                // No servie
                if(getBaseContext()!=null){
                    try {

                        TextView horraireTV = findViewById(R.id.horraireTV);
                        horraireTV.setText("N/A");
                        CardView tramCard = findViewById(R.id.tram_card);
                        tramCard.setVisibility(View.VISIBLE);
                        ImageView bikeIconToTram = findViewById(R.id.tram_icon);
                        bikeIconToTram.setImageDrawable(getDrawable(R.drawable.ic_tram));
                        tramCard.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showTramPopup();
                            }
                        });
                    }catch (NullPointerException e1){e1.printStackTrace();}
                }
                e.printStackTrace();}
        }
    }
    private class FetchAvailabilityVelibAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return fetchHorraires(urls[0]);
            } catch (IOException e) {
                return "Unable to fetch availability. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.i("Velib JSON response", result);

            try {
                JSONObject jsonMain = new JSONObject(result);
                JSONArray recordsArray = jsonMain.getJSONArray("records");
                JSONObject zeroObject = recordsArray.getJSONObject(0);

                JSONObject fieldsObject = zeroObject.getJSONObject("fields");
                //TODO make sure of numbikes and docs
                String available = fieldsObject.getString("numbikesavailable");
                String parkings = fieldsObject.getString("numdocksavailable");
                String name = fieldsObject.getString("name");
                if(getBaseContext()!=null){
                    try {

                        TextView horraireTV = findViewById(R.id.horraireTV);
                        horraireTV.setText(name+": "+available + " bikes, " + parkings + " parkings");
                        ImageView tramIconToBike = findViewById(R.id.tram_icon);
                        tramIconToBike.setImageDrawable(getDrawable(R.drawable.ic_bike));
                        findViewById(R.id.tram_card).setVisibility(View.VISIBLE);
                        horraireTV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showVelibPopup();
                            }
                        });
                    }catch (NullPointerException e){e.printStackTrace();}
                }

            }
            catch (JSONException e) {
                // No servie
                if(getBaseContext()!=null){
                    try {

                        TextView horraireTV = findViewById(R.id.horraireTV);
                        horraireTV.setText("N/A");
                        CardView tramCard = findViewById(R.id.tram_card);
                        tramCard.setVisibility(View.VISIBLE);
                        ImageView tramIconToBike = findViewById(R.id.tram_icon);
                        tramIconToBike.setImageDrawable(getDrawable(R.drawable.ic_bike));
                        tramCard.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showVelibPopup();
                            }
                        });
                    }catch (NullPointerException e1){e1.printStackTrace();}
                }
                e.printStackTrace();}
        }
    }

    public void tramAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        if(doAsynchronousTask!=null) doAsynchronousTask.cancel();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            // Horraire RATP
                            String stationID = PrefUtils.getFromPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_STATION_ID,"cite+universitaire"); //< 461 CiteU , 468 Montsouris
                            String directionID = PrefUtils.getFromPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_DIRECTION_ID,"A"); //<41 Pont du Garigliano, 40 Porte de Vincenne

                            if(getBaseContext()!=null)
                                if(stationID.equals("0") && directionID.equals("0"))
                                    findViewById(R.id.tram_card).setVisibility(View.GONE);
                                else {
                                    Log.d("RATP-URL", "https://api-ratp.pierre-grimaud.fr/v3/schedules/tramways/3a/"+stationID+"/"+directionID);
                                    new MainActivity.FetchHorraireTramAsync().execute("https://api-ratp.pierre-grimaud.fr/v3/schedules/tramways/3a/"+stationID+"/"+directionID);
                                }

                        } catch (Exception e) {e.printStackTrace();}
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 45000); //execute in every 45000 ms
    }
    public void velibAsynchronousTask(){
        final Handler handler = new Handler();
        Timer timer = new Timer();
        if(doAsynchronousTask!=null) doAsynchronousTask.cancel();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            // JCDecaux
                            String stationID = PrefUtils.getFromPrefs(MainActivity.this, PrefUtils.PREFS_VELIB_STATION_ID,"14015"); //< CiteU

                            if(getBaseContext()!=null)
                                if(stationID.equals("0"))
                                    findViewById(R.id.tram_card).setVisibility(View.GONE);
                                else {
                                    Log.d("Velib Link","https://opendata.paris.fr/api/records/1.0/search/?dataset=velib-disponibilite-en-temps-reel&q=" + stationID);
                                    new MainActivity.FetchAvailabilityVelibAsync().execute("https://opendata.paris.fr/api/records/1.0/search/?dataset=velib-disponibilite-en-temps-reel&q=" + stationID);
                                }
                        } catch (Exception e) {e.printStackTrace();}
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 45000); //execute in every 45000 ms
    }

    public void showTramPopup(){
        AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
        b.setTitle("Tram T3a");
        String[] types = {"Cité Universitaire ⇨ Porte de Vincennes",
                "Cité Universitaire ⇨ Pont du Garigliano",
                "Montsouris ⇨ Porte de Vincennes",
                "Montsouris ⇨ Pont du Garigliano",
                getString(R.string.hide)
        };
        b.setItems(types, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                switch(which){
                    case 0:
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_STATION_ID,"cite+universitaire"); //< 461 CiteU , 468 Montsouris
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_DIRECTION_ID,"A"); //<41 Pont du Garigliano, 40 Porte de Vincenne
                        tramAsynchronousTask();
                        break;
                    case 1:
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_STATION_ID,"cite+universitaire"); //< 461 CiteU , 468 Montsouris
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_DIRECTION_ID,"R"); //<41 Pont du Garigliano, 40 Porte de Vincenne
                        tramAsynchronousTask();
                        break;
                    case 2:
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_STATION_ID,"montsouris"); //< 461 CiteU , 468 Montsouris
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_DIRECTION_ID,"A"); //<41 Pont du Garigliano, 40 Porte de Vincenne
                        tramAsynchronousTask();
                        break;
                    case 3:
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_STATION_ID,"montsouris"); //< 461 CiteU , 468 Montsouris
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_DIRECTION_ID,"R"); //<41 Pont du Garigliano, 40 Porte de Vincenne
                        tramAsynchronousTask();
                        break;
                    case 4:
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_STATION_ID,"0"); //< Null
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_DIRECTION_ID,"0"); //< Null
                        findViewById(R.id.tram_card).setVisibility(View.GONE);
                        doAsynchronousTask.cancel();
                        break;
                }
            }

        });
        //Disable JCDecaux
        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_VELIB_STATION_ID,"0");
        b.show();
    }
    public void showVelibPopup(){
        AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
        b.setTitle("Vélib'");
        String[] types = {"Cité Universitaire",
//                "Stade Charléty",
//                "Porte d'Arcueil",
                "Jourdan Le Brix et Mesnin",
                getString(R.string.hide)
        };
        b.setItems(types, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                switch(which){
                    case 0:
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_VELIB_STATION_ID,"14015"); //< 14015 CitéU
                        velibAsynchronousTask();
                        break;
/*                    case 1:
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_VELIB_STATION_ID,"14014"); //< Maison du  Maroc
                        velibAsynchronousTask();
                        break;
                    case 2:
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_VELIB_STATION_ID,"14124"); //< Maison de la Tunisie
                        velibAsynchronousTask();
                        break;*/
                    case 1:
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_VELIB_STATION_ID,"13105968"); //< Collège Néerlandais
                        velibAsynchronousTask();
                        break;
                    case 2:
                        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_VELIB_STATION_ID,"0"); //< Null
                        findViewById(R.id.tram_card).setVisibility(View.GONE);
                        doAsynchronousTask.cancel();
                        break;
                }
            }

        });

        //Disable Velib
        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_STATION_ID,"0"); //< Null
        PrefUtils.saveToPrefs(MainActivity.this, PrefUtils.PREFS_TRANS_DIRECTION_ID,"0"); //< Null
        b.show();
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void getCurrentAd(){
        Backendless.Persistence.of( Ads.class ).findFirst(new AsyncCallback<Ads>() {
            @Override
            public void handleResponse(final Ads response ) {
                if(response.getIsValid()) {
                    TextView adTitle = findViewById(R.id.adTitleTV);
                    adTitle.setText(response.getTitle());
                    CardView adCard = findViewById(R.id.ad_card);
                    adCard.setVisibility(View.VISIBLE);
                    adCard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Increment nb clicked
                            response.setNbClick(response.getNbClick()+1);
                            Backendless.Persistence.of(Ads.class).save(response, new AsyncCallback<Ads>() {
                                @Override
                                public void handleResponse(Ads ads) {
                                    Log.d("SuccessNbClick", ads.toString());
                                }

                                @Override
                                public void handleFault(BackendlessFault backendlessFault) {
                                    Log.e("FailedNbClickw/hError", backendlessFault.toString());
                                }
                            });
                            // Start Intent
                            Intent adDetailsActivity = new Intent(MainActivity.this, AdActivity.class);
                            adDetailsActivity.putExtra("AdDetails", response);
                            startActivity(adDetailsActivity);
                        }
                    });
                }
                else
                    findViewById(R.id.ad_card).setVisibility(View.GONE);

            }
            @Override
            public void handleFault( BackendlessFault fault ) {
                findViewById(R.id.ad_card).setVisibility(View.GONE);
                Log.e("Ad Fetch Error", fault.getMessage());
            }
        });
    }

    private void showUpdateAppDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New update available!")
                .setMessage("Update application for better performance and new features ;)")
                .setCancelable(false)
                .setPositiveButton("UPDATE", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {

                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                })
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void showNotResidentAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Access Denied!")
                .setMessage("Your are not a Resident")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}