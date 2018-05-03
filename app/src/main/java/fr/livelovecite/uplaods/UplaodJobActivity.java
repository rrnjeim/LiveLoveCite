package fr.livelovecite.uplaods;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;


import java.util.Calendar;

import fr.livelovecite.R;
import fr.livelovecite.setup.BackendSettings;

public class UplaodJobActivity extends AppCompatActivity {
    BackendlessUser user = new BackendlessUser();

    public ImageView jobImage;
    EditText jobTitle, jobAddress, jobRemu, jobContactEmail, jobContactNumber, jobDescription;
    ImageButton uploadBTN, deleteBTN, selectImageBTN;
    ProgressDialog progress;
    Boolean imageChanged=false;
    Uri uri=null;
    String idStr = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uplaod_job);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.new_job_offer));

        jobTitle = findViewById(R.id.addJobTitle);
        jobAddress = findViewById(R.id.addJobAddress);
        jobRemu = findViewById(R.id.addJobRemu);
        jobContactEmail = findViewById(R.id.addContactEmail);
        jobContactNumber = findViewById(R.id.addContactNumber);

        jobDescription = findViewById(R.id.addJobDescription);

        jobImage = findViewById(R.id.addJobImage);
        selectImageBTN = findViewById(R.id.addImageBTN);

        uploadBTN = findViewById(R.id.doneBTN);
        deleteBTN = findViewById(R.id.deleteBTN);
        deleteBTN.setVisibility(View.GONE);

        Backendless.initApp(UplaodJobActivity.this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);
        user = Backendless.UserService.CurrentUser();
        if (user==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this.getApplicationContext());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            user = gson.fromJson(json, BackendlessUser.class);
        }

        if(TextUtils.equals(getIntent().getStringExtra("canEdit"),"YES")) {
            jobTitle.setText(getIntent().getStringExtra("Title"));
            jobAddress.setText(getIntent().getStringExtra("Address"));
            jobRemu.setText(getIntent().getStringExtra("Remuneration"));
            jobDescription.setText(getIntent().getStringExtra("Description"));
            jobContactEmail.setText(getIntent().getStringExtra("ContactEmail"));
            jobContactNumber.setText(getIntent().getStringExtra("ContactNumber"));

            deleteBTN.setVisibility(View.VISIBLE);
            jobImage.setBackgroundResource(android.R.color.transparent);
            String pp = (getIntent().getStringExtra("Image"));
            if(pp!=null)
                Picasso.with(UplaodJobActivity.this).load(pp).into(jobImage);
            else
            jobImage.setBackgroundResource(R.drawable.ic_image);
            getSupportActionBar().setTitle(getString(R.string.edit_job));

        }

        View.OnClickListener selectImageClickListener = createSelectImageClickListener();
        selectImageBTN.setOnClickListener( selectImageClickListener );
        jobImage.setOnClickListener( selectImageClickListener );

        View.OnClickListener uploadButtonClickListener = createUploadButtonClickListener();
        uploadBTN.setOnClickListener( uploadButtonClickListener );

        View.OnClickListener deleteJobOfferClickListener = createDeleteJobOfferListener();
        deleteBTN.setOnClickListener(deleteJobOfferClickListener);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(UplaodJobActivity.this);
        builder.setMessage(getString(R.string.are_you_sure))
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
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
    public View.OnClickListener createUploadButtonClickListener() {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(TextUtils.equals(getIntent().getStringExtra("canEdit"),"YES"))
                    UpdateJob();
                else if (CompletionValidator())
                    UploadNewJob();
            }
        };
    }
    public View.OnClickListener createDeleteJobOfferListener(){
        return new View.OnClickListener(){
            @Override
            public void onClick(View v)
            { deleteJob();
            }

        };
    }
    public View.OnClickListener createSelectImageClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");
                Intent chooserIntent = Intent.createChooser(pickIntent, getString(R.string.select_image));
                startActivityForResult(chooserIntent,1);
            }
        };
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri imageURI = data.getData();
                jobImage.setBackgroundResource(android.R.color.transparent);
                Picasso.with(UplaodJobActivity.this).load(imageURI).into(jobImage);
                imageChanged=true;
            }
        }
    }
    private boolean CompletionValidator() {
        if( jobTitle.getText().toString().trim().equals("")) {
            jobTitle.setError( getString(R.string.required));
            return false;}

        else if(jobAddress.getText().toString().trim().equals("")) {
            jobAddress.setError( getString(R.string.required) );
            return false;}

        else if(jobDescription.getText().toString().trim().equals("")) {
            jobDescription.setError( getString(R.string.required) );
            return false;}

        return true;
    }
    private void hasChanged(Job response) {
        // a Job instance has been found by ObjectId
        response.setTitre(jobTitle.getText().toString());
        response.setLocation(jobAddress.getText().toString());
        response.setEmailContact(jobContactEmail.getText().toString());
        response.setNumberContact(jobContactNumber.getText().toString());

        if(jobRemu.getText().toString().equals("") || jobRemu.getText()==null)
            response.setRemuneration("N/A");
        else response.setRemuneration(jobRemu.getText().toString());
        response.setDescription(jobDescription.getText().toString());

        if((boolean)user.getProperty("isAdmin")) response.setVerified(true);
        else response.setVerified(false);
    }

    private void UploadNewJob() {
        hideSoftKeyBoard();

        AlertDialog.Builder builder = new AlertDialog.Builder(UplaodJobActivity.this);
        builder.setMessage(getString(R.string.add_job)+" ?")
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progress = ProgressDialog.show(UplaodJobActivity.this, getString(R.string.working),
                                getString(R.string.uploading_info), true,false);
                        progress.show();
                        final Calendar calendar = Calendar.getInstance();
                        final int thisHour = calendar.get(Calendar.HOUR);
                        final int thisMinute = calendar.get(Calendar.MINUTE);
                        final int thisSecond = calendar.get(Calendar.SECOND);

                        Job job = new Job();
                        job.setOwnerName(user.getProperty("name").toString());
                        job.setOwnerImage(user.getProperty("facebookId").toString());

                        job.setTitre(jobTitle.getText().toString());
                        job.setLocation(jobAddress.getText().toString());
                        if(jobContactEmail.getText()!=null && !jobContactEmail.getText().toString().isEmpty())
                            job.setEmailContact(jobContactEmail.getText().toString());
                        if (jobContactNumber.getText()!=null && !jobContactNumber.getText().toString().isEmpty())
                            job.setNumberContact(jobContactNumber.getText().toString());


                        if(jobRemu.getText().toString().equals("") || jobRemu.getText()==null)
                            job.setRemuneration("N/A");
                        else job.setRemuneration(jobRemu.getText().toString());

                        job.setDescription(jobDescription.getText().toString());

                        final String fileName =""+user.getUserId()+thisHour+thisMinute+thisSecond+".jpeg";
                        if(jobImage.getDrawable()!=null)
                            job.setPicture("https://api.backendless.com/709E3602-AABE-41E9-FF47-48B4C07F4700/DC935617-7AC5-AAA8-FF45-EC35472FAA00/files/myfiles/"+fileName);
                        else job.setPicture(null);
                        job.setToken(user.getUserId());
                        job.setOwnerId(user.getUserId());

                        if((boolean)user.getProperty("isAdmin")) job.setVerified(true);

                        // save object asynchronously
                        Backendless.Persistence.save(job, new AsyncCallback<Job>() {

                                    public void handleResponse(Job response) {
                                        if(response.getPicture()==null){
                                            progress.dismiss();
                                            Intent resultIntent = new Intent();
                                            setResult(Activity.RESULT_OK, resultIntent);
                                            finish();
                                        }
                                        else {
                                            progress.setMessage(getString(R.string.uploading_image));
                                            //Upload job image
                                            Bitmap finalImage = scaleBitmap( ((BitmapDrawable)jobImage.getDrawable()).getBitmap());
                                            // Upload the file
                                            Backendless.Files.Android.upload(finalImage,
                                                    Bitmap.CompressFormat.JPEG, 50, fileName, "myfiles",
                                                    new AsyncCallback<BackendlessFile>()
                                                    {
                                                        @Override
                                                        public void handleResponse( final BackendlessFile backendlessFile ) {
                                                            progress.dismiss();
                                                            if(!(boolean ) user.getProperty("isAdmin"))
                                                                Toast.makeText(UplaodJobActivity.this, getString(R.string.waiting_approval) , Toast.LENGTH_LONG).show();
                                                            Intent resultIntent = new Intent();
                                                            setResult(Activity.RESULT_OK, resultIntent);
                                                            finish();
                                                        }

                                                        @Override
                                                        public void handleFault( BackendlessFault backendlessFault ) {
                                                            Toast.makeText( UplaodJobActivity.this, R.string.no_connection, Toast.LENGTH_SHORT ).show();
                                                            progress.dismiss();
                                                            Intent resultIntent = new Intent();
                                                            setResult(Activity.RESULT_OK, resultIntent);
                                                            finish();
                                                        }
                                                    });
                                        }
                                    }
                                    public void handleFault(BackendlessFault fault) {
                                        progress.dismiss();
                                        if(fault.getCode().equals("1168"))
                                            Toast.makeText(UplaodJobActivity.this, "Please remove emoticons and smileys from texts and try again..", Toast.LENGTH_LONG).show();
                                        else
                                            Toast.makeText(UplaodJobActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();
                                    }
                                }
                        );
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void UpdateJob(){
        hideSoftKeyBoard();
        AlertDialog.Builder builder = new AlertDialog.Builder(UplaodJobActivity.this);
        builder.setMessage(getString(R.string.update_joboffer)+" ?")
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final ProgressDialog progress = ProgressDialog.show(UplaodJobActivity.this, getString(R.string.working), getString(R.string.updating_info), true, false);
                        Backendless.Persistence.of( Job.class ).findById( getIntent().getStringExtra("JobID"), new AsyncCallback<Job>() {
                            @Override
                            public void handleResponse( Job response )
                            {
                                if(CompletionValidator()){
                                    progress.show();
                                    hasChanged(response);
                                    if(imageChanged) {
                                        if (response.getPicture() != null && response.getPicture().equals("")) {
                                            uri = Uri.parse(response.getPicture());
                                            String[] segments = uri.getPath().split("/");
                                            idStr = segments[segments.length - 1];
                                        } else {
                                            final Calendar calendar = Calendar.getInstance();
                                            final int thisHour = calendar.get(Calendar.HOUR);
                                            final int thisMinute = calendar.get(Calendar.MINUTE);
                                            final int thisSecond = calendar.get(Calendar.SECOND);
                                            idStr = "" + user.getUserId() + thisHour + thisMinute + thisSecond + ".jpeg";
                                            response.setPicture("https://api.backendless.com/709E3602-AABE-41E9-FF47-48B4C07F4700/DC935617-7AC5-AAA8-FF45-EC35472FAA00/files/myfiles/"+idStr);
                                        }
                                    }
                                    Backendless.Persistence.save( response, new AsyncCallback<Job>() {
                                        @Override
                                        public void handleResponse( Job response ) {
                                            // Change image
                                            if(imageChanged){
                                                progress.setMessage(getString(R.string.updating_image));
                                                Bitmap finalImage = scaleBitmap( ((BitmapDrawable)jobImage.getDrawable()).getBitmap());
                                                Backendless.Files.Android.upload(finalImage,
                                                        Bitmap.CompressFormat.JPEG, 50, idStr, "myfiles", true, new AsyncCallback<BackendlessFile>() {
                                                            @Override
                                                            public void handleResponse(BackendlessFile response) {

                                                            progress.dismiss();
                                                                if(!(boolean ) user.getProperty("isAdmin"))
                                                                    Toast.makeText(UplaodJobActivity.this, getString(R.string.waiting_approval) , Toast.LENGTH_LONG).show();
                                                            Intent resultIntent = new Intent();
                                                            setResult(Activity.RESULT_OK, resultIntent);
                                                            finish();
                                                        }

                                                        @Override
                                                        public void handleFault(BackendlessFault fault) {
                                                            progress.dismiss();
                                                            Toast.makeText( UplaodJobActivity.this, R.string.no_connection, Toast.LENGTH_SHORT ).show();
                                                        }
                                                    });
                                        }
                                            ///////
                                            else{
                                                progress.dismiss();
                                                if(!(boolean ) user.getProperty("isAdmin"))
                                                    Toast.makeText(UplaodJobActivity.this, getString(R.string.waiting_approval) , Toast.LENGTH_LONG).show();
                                                Intent resultIntent = new Intent();
                                                setResult(Activity.RESULT_OK, resultIntent);
                                                finish();
                                            }
                                        }
                                        @Override
                                        public void handleFault( BackendlessFault fault ) {
                                            progress.dismiss();
                                            Toast.makeText( UplaodJobActivity.this, R.string.no_connection, Toast.LENGTH_SHORT ).show();
                                        }
                                    } );
                                }
                            ////////////////////////////////////////////
                                }
                            @Override
                            public void handleFault( BackendlessFault fault )
                            {
                                progress.dismiss();
                                Toast.makeText( UplaodJobActivity.this, R.string.no_connection, Toast.LENGTH_SHORT ).show();
                            }
                        } );
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

    private void deleteJob() {
        hideSoftKeyBoard();
        AlertDialog.Builder builder = new AlertDialog.Builder(UplaodJobActivity.this);
        builder.setMessage(getString(R.string.delete_joboffer)+" ?")
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final ProgressDialog progress = ProgressDialog.show(UplaodJobActivity.this, getString(R.string.working), getString(R.string.deleting_job), true, false);
                        progress.show();

        Backendless.Persistence.of( Job.class ).findById( getIntent().getStringExtra("JobID"), new AsyncCallback<Job>() {
            @Override
            public void handleResponse(Job response) {
                if(response.getPicture()!=null && response.getPicture().equals(""))
                uri=  Uri.parse( response.getPicture() );
                Backendless.Persistence.of( Job.class ).remove( response, new AsyncCallback<Long>() {
                    public void handleResponse( Long response ) {
                        if(uri==null) {
                            progress.dismiss();
                            Intent resultIntent = new Intent();
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }
                        else {
                            // Delete image
                            progress.setMessage(getString(R.string.deleting_image));
                            String[] segments = uri.getPath().split("/");
                            String idStr = segments[segments.length - 1];

                            Backendless.Files.remove("myfiles/" + idStr, new AsyncCallback<Void>() {
                                @Override
                                public void handleResponse(Void response) {
                                    progress.dismiss();
                                    Intent resultIntent = new Intent();
                                    setResult(Activity.RESULT_OK, resultIntent);
                                    finish();
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    progress.dismiss();
                                    Toast.makeText(UplaodJobActivity.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
                                }
                            });
                            ///////////
                        }
                    }
                    public void handleFault( BackendlessFault fault ) {
                        progress.dismiss();
                        Toast.makeText( UplaodJobActivity.this, R.string.no_connection, Toast.LENGTH_SHORT ).show();
                    }
                } );
            }
            @Override
            public void handleFault( BackendlessFault fault ) {
            progress.dismiss();
            Toast.makeText( UplaodJobActivity.this, R.string.no_connection, Toast.LENGTH_SHORT ).show();
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
    private Bitmap scaleBitmap(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        int maxWidth= jobImage.getWidth();
        int maxHeight = jobImage.getWidth();
        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int)(height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int)(width / ratio);
        } else {
            // square
            height = maxHeight;
            width = maxWidth;
        }
        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }
    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
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

}
