package fr.livelovecite.login;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import fr.livelovecite.R;
import fr.livelovecite.setup.BackendSettings;
import fr.livelovecite.setup.LoadingCallback;
import fr.livelovecite.setup.Validator;

/**
 * Handles registration flow.
 */
public class RegistrationActivity extends AppCompatActivity {
    private ViewSwitcher viewSwitcher;
    private ProgressDialog progress;
    private boolean isNext = false;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_register );

        Backendless.initApp( this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.register));

        viewSwitcher = findViewById(R.id.viewSwitcher); // get the reference of ViewSwitcher
        // Declare in and out animations and load them using AnimationUtils class
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        // set the animation type to ViewSwitcher
        viewSwitcher.setInAnimation(in);
        viewSwitcher.setOutAnimation(out);

        // ClickListener for NEXT button
        // When clicked on Button ViewSwitcher will switch between views
        // The current view will go out and next view will come in with specified animation
        Button btnNext = findViewById(R.id.nextBTN);
        btnNext.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                EditText nameField = findViewById( R.id.nameField );
                EditText emailField = findViewById( R.id.emailField );

                CharSequence name = nameField.getText();
                CharSequence email = emailField.getText();

                if(Validator.isNameValid(RegistrationActivity.this,name)
                        && Validator.isEmailValid(RegistrationActivity.this, email)){
                    isNext=true;
                    viewSwitcher.showNext();
                }
            }
        });

        Button registerButton = findViewById( R.id.finishBTN );
        View.OnClickListener registerButtonClickListener = createRegisterButtonClickListener();

        registerButton.setOnClickListener( registerButtonClickListener );
    }

    /**
     * Validates the values, which user entered on registration screen.
     * Shows Toast with a warning if something is wrong.
     *
     * @param name            user's name
     * @param email           user's email
     * @param password        user's password
     * @param passwordConfirm user's password confirmation
//     * @param maison          user's house of residency
     * @return true if all values are OK, false if something is wrong
     */
    public boolean isRegistrationValuesValid( CharSequence name, CharSequence email, CharSequence password,
                                              CharSequence passwordConfirm ) {
        return Validator.isNameValid( this, name )
                && Validator.isEmailValid( this, email )
                && Validator.isPasswordValid( this, password )
                && isPasswordsMatch( password, passwordConfirm );
    }

    /**
     * Determines whether password and password confirmation are the same.
     * Displays Toast with a warning if not.
     *
     * @param password        password
     * @param passwordConfirm password confirmation
     * @return true if password and password confirmation match, else false
     */
    public boolean isPasswordsMatch( CharSequence password, CharSequence passwordConfirm ) {
        if( !TextUtils.equals( password, passwordConfirm ) ) {
            Toast.makeText( this, R.string.passwords_unmatch, Toast.LENGTH_LONG ).show();
            return false;
        }

        return true;
    }

    /**
     * Sends a request to Backendless to register user.
     * @param name                 user's name
     * @param email                user's email
     * @param password             user's password
//     * @param maison               user's maison
     * @param registrationCallback a callback, containing actions to be executed on request result
     */
    public void registerUser(String name ,String email, String password,
                             AsyncCallback<BackendlessUser> registrationCallback ) {
        BackendlessUser user = new BackendlessUser();
        user.setEmail( email );
        user.setPassword( password );
        user.setProperty( "name", name );
        user.setProperty("facebookId","pas_de_token");


        //Backendless handles password hashing by itself, so we don't need to send hash instead of plain text
        Backendless.UserService.register( user, registrationCallback );
    }

    /**
     * Creates a callback, containing actions to be executed on registration request result.
     * Sends result intent containing registered user's email to calling activity on success.
     *
     * @return a callback, containing actions to be executed on registration request result
     */
    public LoadingCallback<BackendlessUser> createRegistrationCallback() {
        return new LoadingCallback<BackendlessUser>( this, getString(R.string.creating_account) ) {
            @Override
            public void handleResponse( final BackendlessUser registeredUser ) {
                progress.dismiss();

                AlertDialog alertDialog = new AlertDialog.Builder(RegistrationActivity.this).create();
                alertDialog.setTitle("Check your inbox!!");
                alertDialog.setMessage(getString(R.string.successfully_registered));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent registrationResult = new Intent();
                                registrationResult.putExtra( BackendlessUser.EMAIL_KEY, registeredUser.getEmail() );
                                setResult( RESULT_OK, registrationResult );
                                RegistrationActivity.this.finish();
                            }
                        });
                alertDialog.show();
            }
            @Override
            public void handleFault(BackendlessFault fault){
                progress.dismiss();
                if(fault.getCode().equals("3033"))
                    Toast.makeText(RegistrationActivity.this, "Email already in use, please choose another address", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(RegistrationActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();}
        };
    }

    /**
     * Creates a listener, which proceeds with registration on button click.
     *
     * @return a listener, handling registration button click
     */
    public View.OnClickListener createRegisterButtonClickListener() {
        return new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                EditText nameField = findViewById( R.id.nameField );
                EditText emailField = findViewById( R.id.emailField );
                EditText passwordField = findViewById( R.id.passwordField );
                EditText passwordConfirmField = findViewById( R.id.passwordConfirmField );


                CharSequence name = nameField.getText();
                CharSequence email = emailField.getText();
                CharSequence password = passwordField.getText();
                CharSequence passwordConfirmation = passwordConfirmField.getText();

                if( isRegistrationValuesValid( name, email, password, passwordConfirmation ) ) {
                    progress = ProgressDialog.show(RegistrationActivity.this, null,
                            getString(R.string.working), true,false);

                    LoadingCallback<BackendlessUser> registrationCallback = createRegistrationCallback();
                    registerUser( name.toString(), email.toString() ,password.toString(), registrationCallback );
                }
            }
        };
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
        if(isNext){
            viewSwitcher.showPrevious();
            isNext=false;
        }
        else {
            super.onBackPressed();
            overridePendingTransition(R.anim.no_animation, R.anim.push_right_out);
        }
    }
}