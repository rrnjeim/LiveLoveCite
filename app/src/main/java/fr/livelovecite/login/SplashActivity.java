package fr.livelovecite.login;

import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.livelovecite.MainActivity;
import fr.livelovecite.R;
import fr.livelovecite.setup.BackendSettings;
import fr.livelovecite.setup.PrefUtils;

public class SplashActivity extends AppCompatActivity {
    ImageView splashImage;
    private String fbAccessToken = null;

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Backendless.initApp(this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);
        Backendless.setUrl(BackendSettings.SERVER_URL);

        splashImage = findViewById(R.id.splashImage);

        if (AccessToken.getCurrentAccessToken() != null) {
            fbAccessToken = AccessToken.getCurrentAccessToken().getToken();
        }
        performLogin();

        splashImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performLogin();
            }
        });

    }
    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        //Checking if the previous activity is launched on branch Auto deep link.
        if(requestCode == getResources().getInteger(R.integer.AutoDeeplinkRequestCode)){
            //Decide here where  to navigate  when an auto deep linked activity finishes.
            //For e.g. Go to HomeActivity or a  SignUp Activity.
            startActivity(new Intent( SplashActivity.this, MainActivity.class ));
            finish();
        }
    }

    private void performLogin(){
        splashImage.setEnabled(false);

        // To retrieve values back
        String loggedInUserName = PrefUtils.getFromPrefs(SplashActivity.this, PrefUtils.PREFS_LOGIN_USERNAME_KEY,"");
        String loggedInUserPassword = PrefUtils.getFromPrefs(SplashActivity.this, PrefUtils.PREFS_LOGIN_PASSWORD_KEY,"");
        String loggedInFacebook = PrefUtils.getFromPrefs(SplashActivity.this, PrefUtils.PREFS_LOGIN_IDFACEBOOK_KEY, "");

        if (!TextUtils.equals( loggedInUserName, "" ) && !TextUtils.equals( loggedInUserPassword, "" )) {
            //Automatic signin
            Backendless.UserService.login( loggedInUserName, loggedInUserPassword, new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser backendlessUser ) {

                    SharedPreferences appSharedPrefs = PreferenceManager
                            .getDefaultSharedPreferences(SplashActivity.this);
                    SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(backendlessUser);
                    prefsEditor.putString("CurrentUser", json);
                    prefsEditor.apply();

                    if(backendlessUser.getProperty("maison")!=null) {
                        startActivity(new Intent( SplashActivity.this, MainActivity.class ));
                        finish();

                    }
                    else {
                        Intent completeProfileIntent = new Intent( getBaseContext(), CompleteProfileActivity.class );
                        completeProfileIntent.putExtra("UserId", backendlessUser.getUserId());
                        startActivity(completeProfileIntent);
                        finish();
                    }
                }
                @Override
                public void handleFault( BackendlessFault backendlessFault ){
                    splashImage.setEnabled(true);
                    showSnackbar(findViewById(R.id.splashImage), getString(R.string.no_connection) , Snackbar.LENGTH_INDEFINITE);

                }
            }, true);

        }
        else if(TextUtils.equals( loggedInFacebook, "Yes" ) && fbAccessToken!=null){

            Backendless.UserService.loginWithFacebookSdk(fbAccessToken, new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser backendlessUser ) {

                    SharedPreferences appSharedPrefs = PreferenceManager
                            .getDefaultSharedPreferences(SplashActivity.this);
                    SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(backendlessUser);
                    prefsEditor.putString("CurrentUser", json);
                    prefsEditor.apply();


                    if(backendlessUser.getProperty("maison")!=null) {
                        startActivity(new Intent( SplashActivity.this, MainActivity.class ));
                        finish();

                    }
                    else {
                        Intent completeProfileIntent = new Intent( getBaseContext(), CompleteProfileActivity.class );
                        completeProfileIntent.putExtra("UserId", backendlessUser.getUserId());
                        startActivity(completeProfileIntent);
                        finish();
                    }
                }
                @Override
                public void handleFault( BackendlessFault backendlessFault ){
                    splashImage.setEnabled(true);
                    //showSnackbar(findViewById(R.id.splashImage), getString(R.string.no_connection) , Snackbar.LENGTH_INDEFINITE);
                    showSnackbar(findViewById(R.id.splashImage), fbAccessToken+backendlessFault.getMessage() , Snackbar.LENGTH_INDEFINITE);
                }
            },true );
        }
        else {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        }
    }
    public void showSnackbar(View view, String message, int duration) {
        // Create snackbar
        final Snackbar snackbar = Snackbar.make(view, message, duration);

        // Set an action on it, and a handler
        snackbar.setAction("Refresh", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
}

