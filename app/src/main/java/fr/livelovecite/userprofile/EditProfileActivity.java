package fr.livelovecite.userprofile;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.IDataStore;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import fr.livelovecite.login.Login;
import fr.livelovecite.login.SplashActivity;
import fr.livelovecite.R;
import fr.livelovecite.setup.BackendSettings;
import fr.livelovecite.setup.PrefUtils;
import fr.livelovecite.setup.Validator;

public class EditProfileActivity extends AppCompatActivity {
    BackendlessUser user = new BackendlessUser();
    Spinner nationalityList;
    EditText nameField, mobileNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.edit_profile));

        Backendless.initApp( this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);
        user = Backendless.UserService.CurrentUser();
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this.getApplicationContext());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            user = gson.fromJson(json, BackendlessUser.class);

        ImageView userProfilePic = this.findViewById(R.id.profile_image);
        if(user.getProperty("facebookId") == null || TextUtils.equals((String)user.getProperty("facebookId"),"pas_de_token"))
            userProfilePic.setImageResource(R.drawable.default_user_icon);
        else
            Picasso.with(this)
                    .load("https://graph.facebook.com/" + user.getProperty("facebookId") + "/picture?type=large")
                    .placeholder( R.drawable.default_user_icon )
                    .into(userProfilePic);

        ArrayAdapter adapterNationality = ArrayAdapter.createFromResource(this, R.array.countriesList, R.layout.spinner_item_uploads);
        adapterNationality.setDropDownViewResource(R.layout.spinner_dropdown_item);
        nationalityList = findViewById(R.id.nationalityList);
        nationalityList.setAdapter(adapterNationality);

        TextView emailField = findViewById(R.id.emailField);
        emailField.setEnabled(false);

        if(user.getEmail()!=null &&  Patterns.EMAIL_ADDRESS.matcher( user.getEmail() ).matches())
            emailField.setText(user.getEmail());
        else if(user.getProperty("email_bis")!=null)
            emailField.setText(user.getProperty("email_bis").toString());

        TextView houseField = findViewById(R.id.houseField);
        houseField.setText(user.getProperty("maison").toString());

        nameField = findViewById( R.id.nameField );
        nameField.setText(user.getProperty("name").toString());

        mobileNum = findViewById(R.id.mobileField);
        if(user.getProperty("mobile")!=null && user.getProperty("mobile").toString().length()!=0)
            mobileNum.setText(user.getProperty("mobile").toString());

        nationalityList.setSelection(adapterNationality.getPosition(user.getProperty("Nationality").toString()));


        Button backBTN = findViewById(R.id.backBTN);

        assert backBTN != null;
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                finish();
            }
        } );

        Button updateProfile = findViewById(R.id.updateBTN);
        assert updateProfile != null;
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if(!nameField.getText().toString().equals("")
                        && !nationalityList.getSelectedItem().toString().equals(getString(R.string.nationality_prompt))
                        && !mobileNum.getText().toString().equals(""))
                    updateUserInfo();
                else {
                    if(nameField.getText().toString().equals(""))
                        nameField.setError(getString(R.string.name_required));
                    else if (nationalityList.getSelectedItem().toString().equals(getString(R.string.nationality_prompt)))
                        Toast.makeText(EditProfileActivity.this,getString(R.string.nationality_required), Toast.LENGTH_LONG).show();
                    else if
                            (mobileNum.getText().toString().equals("")) mobileNum.setError(getString(R.string.number_required));
                }

            }
        } );

        final Button deleteAccountBTN = findViewById(R.id.deleteAccountBTN);

        assert deleteAccountBTN != null;
        deleteAccountBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setMessage(getString(R.string.delete_account))
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                final ProgressDialog progress = ProgressDialog.show(EditProfileActivity.this, getString(R.string.working),
                                        getString(R.string.delete_account)+" ?", true,false);
                                progress.show();
                                deleteUserAccount();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        } );

        TextView forgotPassword = findViewById(R.id.forgotpassword);
        if (!user.getProperty("facebookId").toString().equals("pas_de_token"))
           forgotPassword.setVisibility(View.GONE);

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Dialog dialog = new Dialog(EditProfileActivity.this);
                dialog.setContentView(R.layout.dialog_reset_password);
                dialog.setTitle(getString(R.string.forgot_password));
                dialog.setCancelable(true);

                final EditText email = dialog.findViewById(R.id.emailField);
                email.setText(user.getEmail());
                dialog.findViewById(R.id.sendBTN).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(Validator.isEmailValid(EditProfileActivity.this, email.getText())) {
                            Backendless.UserService.restorePassword( email.getText().toString(), new AsyncCallback<Void>() {
                                public void handleResponse( Void response ) {
                                    Toast.makeText(EditProfileActivity.this, "Email sent", Toast.LENGTH_LONG).show();
                                }
                                public void handleFault( BackendlessFault fault ) {
                                    Toast.makeText(EditProfileActivity.this, "Failed to send Email", Toast.LENGTH_LONG).show();
                                }
                            });
                            dialog.dismiss();
                        }
                        else {
                            email.getText().clear();
                            Toast.makeText(EditProfileActivity.this, getString(R.string.inavlid_email), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                dialog.show();
            }
        });
    }

    private void updateUserInfo(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setMessage(getString(R.string.edit_profile)+" ?")
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final ProgressDialog progress = ProgressDialog.show(EditProfileActivity.this, getString(R.string.working),
                                getString(R.string.updating_info), true, false);
                        progress.show();
                        Backendless.Data.of(BackendlessUser.class).findById(user.getObjectId(), new AsyncCallback<BackendlessUser>() {
                            @Override
                            public void handleResponse(final BackendlessUser currentUser) {
                                currentUser.setProperty("name", nameField.getText().toString());
                                currentUser.setProperty("Nationality", nationalityList.getSelectedItem().toString());
                                currentUser.setProperty("mobile", mobileNum.getText().toString());
                                // Update user info
                                Backendless.UserService.update(currentUser, new AsyncCallback<BackendlessUser>() {
                                    public void handleResponse(BackendlessUser user) {
                                        progress.dismiss();

                                        Intent intent = new Intent(EditProfileActivity.this, SplashActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        progress.dismiss();
                                        Toast.makeText(EditProfileActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            ///////////////////////////////////////////////////////////////
                            public void handleFault(BackendlessFault fault) {
                                progress.dismiss();
                                Toast.makeText(EditProfileActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void deleteUserAccount(){
        String id = Backendless.UserService.CurrentUser().getObjectId();

        final IDataStore<BackendlessUser> dataStore = Backendless.Data.of( BackendlessUser.class );

        dataStore.findById(id, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse( BackendlessUser backendlessUser ) {
                dataStore.remove( backendlessUser, new AsyncCallback<Long>() {
                    @Override
                    public void handleResponse( Long aLong ) {
                        PrefUtils.saveToPrefs(EditProfileActivity.this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, "");
                        PrefUtils.saveToPrefs(EditProfileActivity.this, PrefUtils.PREFS_LOGIN_PASSWORD_KEY, "");
                        PrefUtils.saveToPrefs(EditProfileActivity.this, PrefUtils.PREFS_LOGIN_IDFACEBOOK_KEY, "");
                        Intent SignOut = new Intent(EditProfileActivity.this,Login.class );
                        SignOut.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity( SignOut );
                        finish();
                    }

                    @Override
                    public void handleFault( BackendlessFault backendlessFault ) {}
                } );
            }

            @Override
            public void handleFault( BackendlessFault backendlessFault ) {
                Toast.makeText(EditProfileActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();
            }
        } );
    }
}
