package fr.livelovecite.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.facebook.CallbackManager;
import com.google.gson.Gson;

import fr.livelovecite.R;
import fr.livelovecite.setup.BackendSettings;
import fr.livelovecite.setup.PrefUtils;
import fr.livelovecite.setup.Validator;

public class CompleteProfileActivity extends AppCompatActivity {
    EditText emailField;
    Spinner nationalityList, houseList;
    TextView phoneNumber;
    CheckBox checkTermsConditions;
    CallbackManager callbackManager;
    public static boolean isKill;
    BackendlessUser Khayynauser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.complete_profile);

        Khayynauser = Backendless.UserService.CurrentUser();

        Backendless.initApp(this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);
        callbackManager = CallbackManager.Factory.create();

        Button finishButton = findViewById(R.id.finishBTN);
        phoneNumber = findViewById(R.id.mobileField);

        emailField = findViewById(R.id.emailField);
        if(Patterns.EMAIL_ADDRESS.matcher( Khayynauser.getEmail() ).matches()){
            emailField.setEnabled(false);
            emailField.setText(Khayynauser.getEmail());
        }
        else{
            emailField.setText(null);
            emailField.setFocusable(true);
        }


        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.maison_arrays, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        houseList = findViewById(R.id.houseList);
        houseList.setAdapter(adapter);

        ArrayAdapter adapterNationality = ArrayAdapter.createFromResource(this, R.array.countriesList, R.layout.spinner_item);
        adapterNationality.setDropDownViewResource(R.layout.spinner_dropdown_item);
        nationalityList = findViewById(R.id.nationalityList);
        nationalityList.setAdapter(adapterNationality);

        checkTermsConditions = findViewById(R.id.checkBox);
        makeTermsAndConditionsLink();

        View.OnClickListener registerButtonClickListener = createRegisterButtonClickListener();

        finishButton.setOnClickListener(registerButtonClickListener);

    }

    public boolean isRegistrationValuesValid(CharSequence email, CharSequence maison) {
        return Patterns.EMAIL_ADDRESS.matcher( email ).matches()
                && Validator.isHouseSelected(this, maison)
                && Validator.hasAgreed(checkTermsConditions);
    }

    private View.OnClickListener createRegisterButtonClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence email = emailField.getText().toString();
                final CharSequence nationality = nationalityList.getSelectedItem().toString();
                final CharSequence number;
                if(phoneNumber.getText()!=null && phoneNumber.getText().toString().length()!=0)
                    number=phoneNumber.getText().toString();
                else
                    number="";
                final CharSequence house = houseList.getSelectedItem().toString();

                if(emailField.getText()==null || emailField.getText().toString().equals(""))
                    return;

                if (isRegistrationValuesValid(email, house)) {
                    final ProgressDialog progress = ProgressDialog.show(CompleteProfileActivity.this, getString(R.string.working),
                            getString(R.string.logging_in), true,false);
                    progress.show();

                    // Find the logged in user
                    Backendless.Data.of(BackendlessUser.class).findById(Khayynauser.getObjectId(), new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(final BackendlessUser currentUser) {
                            if(!Patterns.EMAIL_ADDRESS.matcher( currentUser.getEmail() ).matches())
                                currentUser.setProperty("email_bis", email);
                            currentUser.setProperty("Nationality", nationality);
                            currentUser.setProperty("maison", house);
                            currentUser.setProperty("mobile", number);
                            // Update user info
                            Backendless.UserService.update(currentUser, new AsyncCallback<BackendlessUser>() {
                                public void handleResponse(BackendlessUser user) {

                                    SharedPreferences appSharedPrefs = PreferenceManager
                                            .getDefaultSharedPreferences(CompleteProfileActivity.this);
                                    SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                                    Gson gson = new Gson();
                                    String json = gson.toJson(user);
                                    prefsEditor.putString("CurrentUser", json);
                                    prefsEditor.apply();
                                    PrefUtils.saveToPrefs(CompleteProfileActivity.this, PrefUtils.PREFS_LOGIN_IDFACEBOOK_KEY, "Yes");
                                    progress.dismiss();

                                    startActivity( new Intent( CompleteProfileActivity.this, SplashActivity.class ) );
                                    finish();
                                }
                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    progress.dismiss();
                                    Toast.makeText(CompleteProfileActivity.this, R.string.no_connection , Toast.LENGTH_LONG).show();}
                            });
                        }
                        ///////////////////////////////////////////////////////////////
                        public void handleFault(BackendlessFault fault) {
                            progress.dismiss();
                            Toast.makeText(CompleteProfileActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();}
                    });
                }
            }
        };
    }

    public void makeTermsAndConditionsLink() {
        SpannableString registrationPrompt = new SpannableString("I have read and agree to Terms and Conditions");

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
        super.onBackPressed();
        isKill=true;
    }
}