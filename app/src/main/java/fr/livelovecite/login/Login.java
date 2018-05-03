package fr.livelovecite.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.livelovecite.MainActivity;
import fr.livelovecite.push.MaisonAdminLoginActivity;
import fr.livelovecite.setup.BackendSettings;
import fr.livelovecite.setup.LoadingCallback;
import fr.livelovecite.setup.PrefUtils;
import fr.livelovecite.R;
import fr.livelovecite.setup.Validator;

public class Login extends AppCompatActivity {

    private static final int REGISTER_REQUEST_CODE = 1;
    public static boolean isKill;
    private LinearLayout linearLayout;
    private RelativeLayout relativeLayout;

    // facebook
    CallbackManager callbackManager;
    private String fbAccessToken = null;
    private LoginButton loginFacebookButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        isKill=false;
        Backendless.initApp(this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);
        Backendless.setUrl(BackendSettings.SERVER_URL);

        linearLayout = findViewById(R.id.linearLayout);
        relativeLayout = findViewById(R.id.relativeLayoutBTNS);
        // facebook
        Button loginButton = findViewById(R.id.loginBTN);
        loginFacebookButton = findViewById( R.id.button_FacebookLogin );


        makeTermsAndConditionsLink();

        callbackManager = configureFacebookSDKLogin();
        if (AccessToken.getCurrentAccessToken() != null) {
            fbAccessToken = AccessToken.getCurrentAccessToken().getToken();
        }
        loginButton.setOnClickListener(createLoginButtonListener());

        TextView adminLogin = findViewById(R.id.adminLogin);
        adminLogin.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view ) {
                Intent AdminLoginActivity = new Intent( Login.this, MaisonAdminLoginActivity.class );
                startActivity( AdminLoginActivity );
            }
        } );

        Button emailButton = findViewById(R.id.emailBTN);
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayout.animate()
                        .translationX(-relativeLayout.getWidth())
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                relativeLayout.setVisibility(View.GONE);
                                linearLayout.setVisibility(View.VISIBLE);
                            }
                        });
            }
        });
        makeRegistrationLink();

        findViewById(R.id.forgotpassword).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Dialog dialog = new Dialog(Login.this);
                dialog.setContentView(R.layout.dialog_reset_password);
                dialog.setTitle(getString(R.string.forgot_password));
                dialog.setCancelable(true);

                final EditText email = dialog.findViewById(R.id.emailField);
                dialog.findViewById(R.id.sendBTN).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(Validator.isEmailValid(Login.this, email.getText())) {
                            Backendless.UserService.restorePassword( email.getText().toString(), new AsyncCallback<Void>() {
                                public void handleResponse( Void response ) {
                                    Toast.makeText(Login.this, "Email sent", Toast.LENGTH_LONG).show();
                                }
                                public void handleFault( BackendlessFault fault ) {
                                    if(fault.getCode().equals("3075"))
                                        Toast.makeText(Login.this, "Email linked with Facebook, email not sent", Toast.LENGTH_LONG).show();
                                    else
                                        Toast.makeText(Login.this, "Failed to send Email", Toast.LENGTH_LONG).show();

                                    Log.d("EmailConfirmation",fault.getCode()+" : "+fault.getMessage());
                                }
                            });
                            dialog.dismiss();
                        }
                        else {
                            email.getText().clear();
                            Toast.makeText(Login.this, getString(R.string.inavlid_email), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                dialog.show();
            }
        });
    }

    public void makeRegistrationLink() {
        SpannableString registrationPrompt = new SpannableString(getString(R.string.no_account_yet));

        ClickableSpan clickableSpan = new ClickableSpan()
        {
            @Override
            public void onClick( View widget ) {
                startRegistrationActivity();
            }
        };

        String linkText = getString(R.string.register);
        int linkStartIndex = registrationPrompt.toString().indexOf( linkText );
        int linkEndIndex = linkStartIndex + linkText.length();
        registrationPrompt.setSpan( clickableSpan, linkStartIndex, linkEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );

        TextView registerPromptView = findViewById( R.id.registerPromptText );
        registerPromptView.setText( registrationPrompt );
        registerPromptView.setMovementMethod( LinkMovementMethod.getInstance() );
    }

    /**
     * Sends a request for registration to RegistrationActivity,
     * expects for result in onActivityResult.
     */
    public void startRegistrationActivity() {
        Intent registrationIntent = new Intent( this, RegistrationActivity.class );
        startActivityForResult( registrationIntent, REGISTER_REQUEST_CODE );
        overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
    }

    /**
     * Sends a request to Backendless to log in user by email and password.
     *
     * @param email         user's email
     * @param password      user's password
     * @param loginCallback a callback, containing actions to be executed on request result
     */
    public void loginUser( String email, String password, AsyncCallback<BackendlessUser> loginCallback ) {
        Backendless.UserService.login( email, password, loginCallback,true);
    }

    /**
     * Creates a listener, which proceeds with login by email and password on button click.
     *
     * @return a listener, handling login button click
     */
    public View.OnClickListener createLoginButtonListener() {
        return new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                EditText emailField = findViewById( R.id.emailField );
                EditText passwordField = findViewById( R.id.passwordField );

                CharSequence email = emailField.getText();
                CharSequence password = passwordField.getText();

                if( isLoginValuesValid( email, password ) ) {
                    LoadingCallback<BackendlessUser> loginCallback = createLoginCallback();

                    loginCallback.showLoading();
                    loginUser( email.toString(), password.toString(), loginCallback );
                }
            }
        };
    }


    /**
     * Validates the values, which user entered on login screen.
     * Shows Toast with a warning if something is wrong.
     *
     * @param email    user's email
     * @param password user's password
     * @return true if all values are OK, false if something is wrong
     */
    public boolean isLoginValuesValid( CharSequence email, CharSequence password ) {
        return Validator.isEmailValid( this, email ) && Validator.isPasswordValid( this, password );
    }

    /**
     * Creates a callback, containing actions to be executed on login request result.
     * Starts Main activity on successful login.
     *
     * @return a callback, containing actions to be executed on login request result
     */
    public LoadingCallback<BackendlessUser> createLoginCallback() {
        return new LoadingCallback<BackendlessUser>( this, getString(R.string.logging_in),true) {
            @Override
            public void handleResponse( BackendlessUser loggedInUser ) {
                EditText emailField = findViewById( R.id.emailField );
                EditText passwordField = findViewById( R.id.passwordField );
                PrefUtils.saveToPrefs(Login.this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, emailField.getText().toString());
                PrefUtils.saveToPrefs(Login.this, PrefUtils.PREFS_LOGIN_PASSWORD_KEY, passwordField.getText().toString());

                SharedPreferences appSharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(Login.this);
                SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(loggedInUser);
                prefsEditor.putString("CurrentUser", json);
                prefsEditor.apply();

                if(loggedInUser.getProperty("maison")!=null) {
                    startActivity(new Intent( Login.this, MainActivity.class ));
                    finish();

                }
                else {
                    Intent completeProfileIntent = new Intent( Login.this, CompleteProfileActivity.class );
                    completeProfileIntent.putExtra("UserId", loggedInUser.getUserId());
                    startActivity(completeProfileIntent);
                    finish();
                }
            }
            @Override
            public void handleFault( BackendlessFault backendlessFault ){
                Toast.makeText( Login.this, "Invalid username or password" , Toast.LENGTH_SHORT ).show();
                hideLoading();
            }
        };
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        callbackManager.onActivityResult( requestCode, resultCode, data );
        if( resultCode == RESULT_OK ) {
            switch( requestCode ) {
                case REGISTER_REQUEST_CODE:
                    findViewById(R.id.linearLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.relativeLayoutBTNS).setVisibility(View.GONE);

                    String email = data.getStringExtra( BackendlessUser.EMAIL_KEY );
                    EditText emailField = findViewById( R.id.emailField );
                    emailField.setText( email );

                    EditText passwordField = findViewById( R.id.passwordField );
                    passwordField.requestFocus();

                    //ghayyir to waiting email verification
                    Toast.makeText( this, R.string.successfully_registered , Toast.LENGTH_LONG ).show();
            }
        }
    }

    private void loginToBackendlessWithFacebook() {
        final ProgressDialog progress = ProgressDialog.show(Login.this, getString(R.string.working),
               getString(R.string.logging_in), true,false);
       progress.show();
        Backendless.UserService.loginWithFacebookSdk(fbAccessToken, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse( BackendlessUser backendlessUser ) {
                SharedPreferences appSharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(Login.this);
                SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(backendlessUser);
                prefsEditor.putString("CurrentUser", json);
                prefsEditor.apply();

                if(backendlessUser.getProperty("maison")!=null) {
                    PrefUtils.saveToPrefs(Login.this, PrefUtils.PREFS_LOGIN_IDFACEBOOK_KEY, "Yes");
                    Intent loginIntent = new Intent( getBaseContext(), MainActivity.class );
                    startActivity(loginIntent);
                }
                else {
                    Intent completeProfileIntent = new Intent( getBaseContext(), CompleteProfileActivity.class );
                    completeProfileIntent.putExtra("UserId", backendlessUser.getUserId());
                    startActivity(completeProfileIntent);
                }
                progress.dismiss();
                finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                progress.dismiss();
                Toast.makeText(Login.this, R.string.cannot_connect_facebook, Toast.LENGTH_LONG).show();
            }
        }, true);
    }
    private CallbackManager configureFacebookSDKLogin() {
        loginFacebookButton.setReadPermissions("email", "public_profile");

        CallbackManager callbackManager = CallbackManager.Factory.create();

        // Callback registration
        loginFacebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(com.facebook.login.LoginResult loginResult) {
                fbAccessToken = loginResult.getAccessToken().getToken();
                loginToBackendlessWithFacebook();
            }

            @Override
            public void onCancel() {
                // App code
                Log.i("LoginProcess", "loginFacebookButton::onCancel");
                Toast.makeText(Login.this, "Facebook login process cancelled.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException exception) {
                fbAccessToken = null;
                String msg = exception.getMessage() + "\nCause:\n" + (exception.getCause() != null ? exception.getCause().getMessage() : "none");
                Toast.makeText(Login.this, msg, Toast.LENGTH_LONG).show();
            }
        });

        return callbackManager;
    }


    public void makeTermsAndConditionsLink() {
        SpannableString registrationPrompt = new SpannableString("By registering to Live Love Cité you agree to the Terms and Conditions");

        ClickableSpan clickableSpan = new ClickableSpan()
        {
            @Override
            public void onClick( View widget ) {startTermsAndConditionsActivity();
            }
        };

        String linkText = "Terms and Conditions";
        int linkStartIndex = registrationPrompt.toString().indexOf( linkText );
        int linkEndIndex = linkStartIndex + linkText.length();
        registrationPrompt.setSpan( clickableSpan, linkStartIndex, linkEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );

        TextView checkTermsConditions = findViewById(R.id.termsConditions);
        checkTermsConditions.setText( registrationPrompt );
        checkTermsConditions.setMovementMethod( LinkMovementMethod.getInstance() );
    }
    public void startTermsAndConditionsActivity() {
        Intent registrationIntent = new Intent( this, TermsAndConditionsActivity.class );
        startActivity( registrationIntent);
        overridePendingTransition(R.anim.push_left_in, R.anim.no_animation);
    }
    @Override
    public void onBackPressed() {
        if(findViewById(R.id.linearLayout).getVisibility()==View.VISIBLE){
            relativeLayout.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.GONE);
            relativeLayout.animate()
                    .translationX(0)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    });
        }
        else {
            super.onBackPressed();
            isKill=true;
            overridePendingTransition(R.anim.no_animation,R.anim.push_right_out);
        }
    }
}