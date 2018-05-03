package fr.livelovecite.userprofile;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
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
import com.backendless.exceptions.BackendlessException;
import com.backendless.exceptions.BackendlessFault;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import fr.livelovecite.login.Login;
import fr.livelovecite.push.PushActivity;
import fr.livelovecite.R;
import fr.livelovecite.setup.BackendSettings;
import fr.livelovecite.setup.PrefUtils;
import fr.livelovecite.uplaods.UplaodJobActivity;
import fr.livelovecite.uplaods.UploadEventsActivity;
import fr.livelovecite.uplaods.UploadItemSellActivity;

public class ResidentInfo extends AppCompatActivity {
    private static final int UPDATED_UPLOADS_STATE=2;
    Toolbar toolbar = null;
    private BackendlessUser user = new BackendlessUser();
    PagerSlidingTabStrip tabsStrip;

    TextView userMail, userName, userMaison;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_info);

        toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){e.printStackTrace();}
        getSupportActionBar().setTitle(R.string.my_profile);


        Backendless.initApp(this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);
        user=Backendless.UserService.CurrentUser();
        if (user==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this.getApplicationContext());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            user = gson.fromJson(json, BackendlessUser.class);
        }

        initializeHeaderData();

        if(!(boolean)user.getProperty("isAdmin"))
            findViewById(R.id.adminMessageBTN).setVisibility(View.GONE);
        else {
            Button adminMessageBTN = findViewById(R.id.adminMessageBTN);
            adminMessageBTN.setVisibility(View.VISIBLE);
            adminMessageBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent PushActivityIntent = new Intent(ResidentInfo.this, PushActivity.class);
                    PushActivityIntent.putExtra("Channel", user.getProperty("maison").toString());
                    startActivity(PushActivityIntent );
                }
            });
        }

        Button editProfileBTN = findViewById(R.id.editProfileBTN);
        assert editProfileBTN != null;
        editProfileBTN.setOnClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick( View view ) {
            Intent editProfileActivity = new Intent( ResidentInfo.this, EditProfileActivity.class );
            editProfileActivity.putExtra("Name", user.getProperty("name").toString());
            editProfileActivity.putExtra("Nationality", user.getProperty("Nationality").toString());
            editProfileActivity.putExtra("Password", user.getPassword());
            if(user.getProperty("mobile")!=null && user.getProperty("mobile").toString().length()!=0)
                editProfileActivity.putExtra("Number", user.getProperty("mobile").toString());
            startActivity( editProfileActivity );
        }
    } );

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyUplaodsAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(2);

        // Give the PagerSlidingTabStrip the ViewPager
        tabsStrip = findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);
    }

    private void initializeHeaderData() {

        ImageView userProfilePic = this.findViewById(R.id.profile_image);
        if(user.getProperty("facebookId") == null || TextUtils.equals((String)user.getProperty("facebookId"),"pas_de_token"))
            userProfilePic.setImageResource(R.drawable.default_user_icon);
        else
            Picasso.with(this)
                    .load("https://graph.facebook.com/" + user.getProperty("facebookId") + "/picture?type=large")
                    .placeholder( R.drawable.default_user_icon )
                    .into(userProfilePic);
        userMail = this.findViewById(R.id.user_name);
        if(user.getProperty("name")!=null)
            userMail.setText((String) user.getProperty("name"));

        userName = this.findViewById(R.id.user_email);
        if(user.getEmail()!=null && Patterns.EMAIL_ADDRESS.matcher( user.getEmail() ).matches())
            userName.setText(user.getEmail());
        else if(user.getProperty("email_bis")!=null)
            userName.setText(user.getProperty("email_bis").toString());
        userMaison = this.findViewById(R.id.user_maison);
        if(user.getProperty("maison") !=null)
            userMaison.setText((String) user.getProperty("maison"));
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_profile_header, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_event:
                if(!user.getProperty("maison").equals("Not a Resident")) {
                Intent EventPage = new Intent(ResidentInfo.this, UploadEventsActivity.class );
                startActivityForResult( EventPage, UPDATED_UPLOADS_STATE );
                }
                else showNotResidentAlertDialog();
                return true;

            case R.id.action_job:
                Intent JobPage = new Intent(ResidentInfo.this, UplaodJobActivity.class );
                startActivityForResult( JobPage, UPDATED_UPLOADS_STATE );
                return true;

            case R.id.action_itemsell:
                Intent SellPage = new Intent(ResidentInfo.this, UploadItemSellActivity.class );
                startActivityForResult( SellPage, UPDATED_UPLOADS_STATE );
                return true;

            case R.id.action_logout:
                final ProgressDialog progress = ProgressDialog.show(ResidentInfo.this, getString(R.string.working), getString(R.string.logging_out), true,false);
                progress.show();
                Backendless.UserService.logout(new AsyncCallback<Void>() {
                    @Override
                    public void handleResponse(Void response) {
                        PrefUtils.saveToPrefs(ResidentInfo.this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, null);
                        PrefUtils.saveToPrefs(ResidentInfo.this, PrefUtils.PREFS_LOGIN_PASSWORD_KEY, null);
                        PrefUtils.saveToPrefs(ResidentInfo.this, PrefUtils.PREFS_LOGIN_IDFACEBOOK_KEY, null);
                        Intent SignOut = new Intent(ResidentInfo.this, Login.class );
                        SignOut.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        progress.dismiss();

                        try {
                            Backendless.Messaging.unregisterDevice();
                            startActivity( SignOut );
                            finish();
                        }
                        catch( BackendlessException | IllegalArgumentException exception ) {
                            System.out.println("Could'nt unsubscribe from push with error " + exception.getMessage());
                            startActivity( SignOut );
                            finish();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        progress.dismiss();
                        Toast.makeText(ResidentInfo.this, R.string.cannot_logout,Toast.LENGTH_SHORT).show();}
                });

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
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
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.no_animation, R.anim.push_right_out);
    }
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if( resultCode == RESULT_OK ) {
            switch( requestCode ) {
                case UPDATED_UPLOADS_STATE:
                    finish();
                    startActivity(getIntent());
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        user= Backendless.UserService.CurrentUser();
        if (user==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            user = gson.fromJson(json, BackendlessUser.class);
        }
        if(Login.isKill)
            finish();
    }
}
