package fr.livelovecite.uplaods;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.messaging.DeliveryOptions;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.backendless.messaging.PushBroadcastMask;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import fr.livelovecite.R;
import fr.livelovecite.setup.BackendSettings;

public class UploadEventsActivity extends AppCompatActivity {

    BackendlessUser user = new BackendlessUser();
    Toolbar toolbar=null;
    String startDate, endDate;
    Spinner houseList;
    EditText eventTitle, eventDescription;
    TextView eventDateStart, eventTimeStart,eventDateEnd,eventTimeEnd, eventPriceExt, eventPriceInt;
    ImageView eventImage;
    ImageButton uploadBTN, deleteBTN;
    Switch payableSwitch, privacySwitch;
    Boolean privacy,imageChanged=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_events);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.new_event));

        eventTitle = findViewById( R.id.eventTitle );
        eventDescription = findViewById( R.id.ItemDescription);
        eventImage = findViewById(R.id.uploadEventImageBUZZ);
        ImageButton btnSelect = findViewById(R.id.addImageBTN);
        houseList = findViewById(R.id.houseList);
        final ArrayAdapter adapter = ArrayAdapter.createFromResource(UploadEventsActivity.this, R.array.locations_array, R.layout.spinner_item_uploads);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        houseList.setAdapter(adapter);

        eventDateStart = findViewById(R.id.eventDateStart);
        eventTimeStart = findViewById(R.id.eventTimeStart);
        eventDateEnd = findViewById(R.id.eventDateEnd);
        eventTimeEnd = findViewById(R.id.eventTimeEnd);

        eventPriceExt = findViewById(R.id.eventPriceExt);
        eventPriceInt = findViewById(R.id.eventPriceInt);

        payableSwitch = findViewById(R.id.PayableSwitch);
        privacySwitch = findViewById(R.id.PrivacySwitch);

        uploadBTN = findViewById(R.id.doneBTN);
        deleteBTN = findViewById(R.id.deleteBTN);
        deleteBTN.setVisibility(View.GONE);

        Backendless.initApp(UploadEventsActivity.this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);
        user = Backendless.UserService.CurrentUser();
        if (user==null){
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this.getApplicationContext());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("CurrentUser", "");
            user = gson.fromJson(json, BackendlessUser.class);
        }
        ///////////////////////////////////////////////////////////////////////
        if(TextUtils.equals(getIntent().getStringExtra("canEdit"),"YES")) {
            eventTitle.setText(getIntent().getStringExtra("Title"));
            houseList.setSelection(adapter.getPosition(getIntent().getStringExtra("Location")));
            eventImage.setBackgroundResource(android.R.color.transparent);
            String pp = (getIntent().getStringExtra("Image"));
            if(pp!=null)
                Picasso.with(UploadEventsActivity.this).load(pp).into(eventImage);
            eventTimeStart.setText(getIntent().getStringExtra("Starttime"));
            eventDateStart.setText(getIntent().getStringExtra("Startdate"));
            eventTimeEnd.setText(getIntent().getStringExtra("Endtime"));
            eventDateEnd.setText(getIntent().getStringExtra("Enddate"));
            eventDescription.setText(getIntent().getStringExtra("Description"));

            if(getIntent().getStringExtra("PriceInt").equals("0")){
                payableSwitch.setChecked(false);
                privacySwitch.setChecked(false);
                eventPriceExt.setText(null);
                eventPriceInt.setText(null);
            }
            else if(!getIntent().getStringExtra("PriceInt").equals("0")){
                payableSwitch.setChecked(true);
                eventPriceInt.setText(getIntent().getStringExtra("PriceInt"));
                if(getIntent().getStringExtra("PriceExt").equals("0")){
                    privacySwitch.setChecked(false);
                    eventPriceExt.setText(null);
                }
                else {
                    privacySwitch.setChecked(true);
                    eventPriceExt.setText(getIntent().getStringExtra("PriceExt"));
                }
            }

            privacy=getIntent().getBooleanExtra("Private",true);
            privacySwitch.setChecked(!privacy);

            deleteBTN.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle(getString(R.string.edit_event));

        }
        ///////////////////////////////////////////////////////////////////////
        final EditText fbLink = findViewById(R.id.fbLink);
        fbLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbLink.getText().clear();
            }
        });
        fbLink.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                final Uri uri = Uri.parse(s.toString());
                String[] segments = uri.getPath().split("/");
                final String idStr = segments[segments.length - 1];

                if(s.length()!=0 && !s.toString().equals("") && Patterns.WEB_URL.matcher(s.toString()).matches() && URLUtil.isValidUrl(s.toString())){
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "name, cover, description, end_time, start_time, place");
                    new GraphRequest(
                            AccessToken.getCurrentAccessToken(), idStr,
                            parameters,
                            HttpMethod.GET,
                            new GraphRequest.Callback() {
                                public void onCompleted(GraphResponse response) {
                                    Log.i("Facebook Json", response.toString());
                                    if(response.getError() != null){
                                        Toast.makeText(UploadEventsActivity.this,"Invalid Facebook Event URL",Toast.LENGTH_LONG).show();
                                        fbLink.setText(null);

                                        return;
                                    }
                                    try {
                                        JSONObject mainObject= response.getJSONObject();
                                        String name = mainObject.getString("name");
                                        if(name!=null && !name.equals(""))eventTitle.setText(name);
                                        eventDescription.setText(mainObject.getString("description"));

                                        //Start time
                                        Calendar cal = Calendar.getInstance();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",Locale.US);
                                        try {
                                            cal.setTime(sdf.parse(mainObject.getString("start_time")));

                                            String month =""+(cal.get(Calendar.MONTH)+1);
                                            if(month.length()==1) month ="0"+month;

                                            String day = ""+cal.get(Calendar.DAY_OF_MONTH);
                                            if(day.length()==1)day="0"+day;

                                            eventDateStart.setText(month+"/"+ day+"/"+ cal.get(Calendar.YEAR));

                                            String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
                                            if(hour.length()==1)hour="0"+hour;
                                            String minute = String.valueOf(cal.get(Calendar.MINUTE));
                                            if(minute.length()==1)minute="0"+minute;
                                            eventTimeStart.setText(hour+":"+minute);
                                        } catch (ParseException e) {e.printStackTrace();}

                                        //End time
                                        Calendar cal2 = Calendar.getInstance();
                                        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
                                        try {
                                            cal2.setTime(sdf2.parse(mainObject.getString("end_time")));

                                            String month =""+(cal2.get(Calendar.MONTH)+1);
                                            if(month.length()==1) month ="0"+month;

                                            String day = ""+cal2.get(Calendar.DAY_OF_MONTH);
                                            if(day.length()==1)day="0"+day;

                                            eventDateEnd.setText(month+"/"+ day+"/"+ cal2.get(Calendar.YEAR));

                                            String hour = String.valueOf(cal2.get(Calendar.HOUR_OF_DAY));
                                            if(hour.length()==1)hour="0"+hour;
                                            String minute = String.valueOf(cal2.get(Calendar.MINUTE));
                                            if(minute.length()==1)minute="0"+minute;
                                            eventTimeEnd.setText(hour+":"+minute);
                                        } catch (ParseException e) {e.printStackTrace();}

                                        JSONObject coverObject = mainObject.getJSONObject("cover");
                                        Picasso.with(UploadEventsActivity.this).load(coverObject.getString("source")).into(eventImage);

                                        JSONObject placeObject = mainObject.getJSONObject("place");
                                        String location = locationFromFacebookPlace(placeObject.getString("name"));
                                        houseList.setSelection(adapter.getPosition(location));
                                    }catch (JSONException e){e.printStackTrace();}
                                }
                            }
                    ).executeAsync();
                }
                else fbLink.getText().clear();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        ///////////////////////////////////////////////////////////////////////
        if(payableSwitch.isChecked()){
            eventPriceInt.setEnabled(true);
            eventPriceInt.setHint(getString(R.string.price));
        }
        else {
            eventPriceInt.setEnabled(false);
            eventPriceInt.setText(null);
            eventPriceInt.setHint("Free!");
        }
        payableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if(isChecked){
                    eventPriceInt.setHint(getString(R.string.price));
                    eventPriceInt.setEnabled(true);
                    eventPriceExt.setHint(getString(R.string.price_for_exteriors));
                    if(privacySwitch.isChecked())
                        eventPriceExt.setEnabled(true);
                }
                else {
                    eventPriceExt.setText(null);
                    eventPriceExt.setHint("N/A");
                    eventPriceExt.setEnabled(false);
                    eventPriceInt.setEnabled(false);
                    eventPriceInt.setText(null);
                    eventPriceInt.setHint("Free!");
                }
            }
        });
        if(!payableSwitch.isChecked()){
            eventPriceExt.setText(null);
            eventPriceExt.setHint("N/A");
            eventPriceExt.setEnabled(false);
        }
        else if(payableSwitch.isChecked()){
            if(privacySwitch.isChecked()) {
                eventPriceExt.setHint(getString(R.string.price_for_exteriors));
                eventPriceExt.setEnabled(true);
            }
            else {
                eventPriceExt.setEnabled(false);
                eventPriceExt.setText(null);
                eventPriceExt.setHint("N/A");
            }
        }
        privacySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (!payableSwitch.isChecked()) {
                    eventPriceExt.setText(null);
                    eventPriceExt.setHint("N/A");
                    eventPriceExt.setEnabled(false);
                } else if (payableSwitch.isChecked()) {
                    if (isChecked) {
                        eventPriceExt.setHint(getString(R.string.price_for_exteriors));
                        eventPriceExt.setEnabled(true);
                    } else {
                        eventPriceExt.setEnabled(false);
                        eventPriceExt.setText(null);
                        eventPriceExt.setHint("N/A");
                    }
                }
            }
        });


        View.OnClickListener uploadButtonClickListener = createUploadButtonClickListener();
        uploadBTN.setOnClickListener( uploadButtonClickListener );

        View.OnClickListener selectImageClickListener = createSelectImageClickListener();
        btnSelect.setOnClickListener( selectImageClickListener );
        eventImage.setOnClickListener( selectImageClickListener );

        View.OnClickListener deleteEventClickListener = createDeleteEventListener();
        deleteBTN.setOnClickListener(deleteEventClickListener);

        final Calendar calendar = Calendar.getInstance();
        final int thisYear = calendar.get(Calendar.YEAR);
        final int thisMonth = calendar.get(Calendar.MONTH);
        final int thisDay = calendar.get(Calendar.DAY_OF_MONTH);
        final int thisHour = calendar.get(Calendar.HOUR_OF_DAY);
        final int thisMinute = calendar.get(Calendar.MINUTE);

        final Calendar calendarStart= Calendar.getInstance();

        eventDateStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final DatePickerDialog datePickerDialog = new DatePickerDialog(UploadEventsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override

                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String date;
                        monthOfYear++;
                        if (monthOfYear<10)
                            date = "0" + monthOfYear;
                        else
                            date = "" + monthOfYear;
                        if (dayOfMonth<10)
                            date = date + "/0" + dayOfMonth + "/" + year;
                        else
                            date = date + "/" + dayOfMonth + "/" + year;

                        eventDateStart.setText(date);
                        eventDateEnd.setText("MM/DD/YYYY");
                        eventTimeEnd.setText("--:--");
                        calendarStart.set(Calendar.YEAR,year);
                        calendarStart.set(Calendar.MONTH,monthOfYear-1);
                        calendarStart.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                    }
                }, thisYear , thisMonth, thisDay);
                datePickerDialog.show();
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            }
        });

        eventTimeStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final TimePickerDialog timePickerDialog = new TimePickerDialog(UploadEventsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String mytime;
                        if (hourOfDay<10)
                            mytime = "0" + hourOfDay;
                        else
                            mytime = "" + hourOfDay;
                        if(minute<10)
                            mytime  = mytime + ":0" + minute ;
                        else
                            mytime = mytime + ":" + minute;

                        calendarStart.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calendarStart.set(Calendar.MINUTE,minute);

                        if (calendarStart.getTimeInMillis()>calendar.getTimeInMillis())
                            eventTimeStart.setText(mytime);
                        else
                            eventTimeStart.setText("--:--");
                        eventTimeEnd.setText("--:--");
                    }
                }, thisHour, thisMinute, true);
                timePickerDialog.show();
            }
        });


        final Calendar calendarEnd= Calendar.getInstance();
        eventDateEnd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(eventDateStart.getText().toString().equals("MM/DD/YYYY") || eventTimeStart.getText().toString().equals("--:--"))
                    return;
                final DatePickerDialog datePickerDialog = new DatePickerDialog(UploadEventsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String date;
                        monthOfYear++;
                        if (monthOfYear<10)
                            date = "0" + monthOfYear;
                        else
                            date = "" + monthOfYear;
                        if (dayOfMonth<10)
                            date = date + "/" + "0" + dayOfMonth + "/"+year;
                        else
                            date = date + "/" + dayOfMonth + "/" + year;

                        eventDateEnd.setText(date);
                        eventTimeEnd.setText("--:--");
                        calendarEnd.set(Calendar.YEAR,year);
                        calendarEnd.set(Calendar.MONTH,monthOfYear-1);
                        calendarEnd.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                    }
                }, calendarStart.get(Calendar.YEAR), calendarStart.get(Calendar.MONTH), calendarStart.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                datePickerDialog.getDatePicker().setMinDate(calendarStart.getTimeInMillis()-1000);

            }
        });

        eventTimeEnd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (eventTimeStart.getText().toString().equals("--:--") || eventDateStart.getText().toString().equals("MM/DD/YYYY"))
                    return;
                final TimePickerDialog timePickerDialog = new TimePickerDialog(UploadEventsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String mytime;
                        if (hourOfDay<10)
                            mytime = "0" + hourOfDay;
                        else
                            mytime = "" + hourOfDay;
                        if(minute<10)
                            mytime  = mytime + ":0" + minute ;
                        else
                            mytime = mytime + ":" + minute;

                        calendarEnd.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calendarEnd.set(Calendar.MINUTE,minute);
                        if (calendarEnd.getTimeInMillis()>calendarStart.getTimeInMillis())
                            eventTimeEnd.setText(mytime);
                        else
                            eventTimeEnd.setText("--:--");

                    }
                }, calendarStart.get(Calendar.HOUR_OF_DAY), calendarStart.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }
        });
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
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadEventsActivity.this);
        builder.setMessage(R.string.are_you_sure)
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
    public View.OnClickListener createUploadButtonClickListener() {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(TextUtils.equals(getIntent().getStringExtra("canEdit"),"YES"))
                    UpdateEvent();
                else if (CompletionValidator())
                    UploadNewEvent();
            }
        };
    }
    public View.OnClickListener createDeleteEventListener(){
        return new View.OnClickListener(){
            @Override
            public void onClick(View v) {DeleteEvent();}
        };
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri imageURI = data.getData();
                Picasso.with(UploadEventsActivity.this).load(imageURI).into(eventImage);
                eventImage.setBackgroundResource(android.R.color.transparent);
                imageChanged=true;
            }
        }
    }

    private boolean CompletionValidator() {
        if( eventTitle.getText().toString().equals("")) {
            eventTitle.setError( getString(R.string.required) );
            return false;}

        else if(houseList.getSelectedItem().toString().trim().equals(getString(R.string.select_location))){
            Toast.makeText(UploadEventsActivity.this, R.string.location_missing, Toast.LENGTH_LONG ).show();
            return false;}

        else if(eventTimeStart.getText().toString().equals("--:--")
                || eventDateStart.getText().toString().trim().equals("MM/DD/YYYY")
                || eventTimeEnd.getText().toString().trim().equals("--:--")
                || eventDateEnd.getText().toString().trim().equals("MM/DD/YYYY")) {
            Toast.makeText(UploadEventsActivity.this, R.string.invalid_date_time, Toast.LENGTH_LONG ).show();
            return false;}

        else if(payableSwitch.isChecked() && eventPriceInt.getText().toString().equals("")){
            eventPriceInt.setError(getString(R.string.required));
            return false;}
        else if (payableSwitch.isChecked()
                && privacySwitch.isChecked()
                && eventPriceExt.getText().toString().trim().equals("")){
            eventPriceExt.setError(getString(R.string.required));
            return false;
        }

        else if(eventDescription.getText().toString().equals("")) {
            eventDescription.setError( getString(R.string.required));
            return false;}

        else if(eventImage.getDrawable()==null) {
            Toast.makeText(UploadEventsActivity.this, R.string.image_required, Toast.LENGTH_LONG ).show();
            return false;}

        return true;
    }
    private void hasChanged(Event response) {
        startDate = eventDateStart.getText().toString() + " " + eventTimeStart.getText().toString() + ":00";
        endDate = eventDateEnd.getText().toString() + " " + eventTimeEnd.getText().toString() + ":00";
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        // an Event instance has been found by ObjectId
        response.setTitle(eventTitle.getText().toString());
        response.setMaison(String.valueOf(houseList.getSelectedItem()));
        try {
            Date date = format.parse(startDate);
            response.setStarttime(date);
        } catch (ParseException e) {e.printStackTrace();}
        try {
            Date date = format.parse(endDate);
            response.setEndtime(date);
        } catch (ParseException e) {e.printStackTrace();}

        response.setDescription(eventDescription.getText().toString());

        if(!payableSwitch.isChecked()) {
            response.setPriceInt("0");
            response.setPriceExt("0");
        }
        else if(payableSwitch.isChecked() && privacySwitch.isChecked()) {
            response.setPriceExt(eventPriceExt.getText().toString());
            response.setPriceInt(eventPriceInt.getText().toString());
        }
        else if(payableSwitch.isChecked() && !privacySwitch.isChecked()){
            response.setPriceExt("0");
            response.setPriceInt(eventPriceInt.getText().toString());
        }

        response.setPrivate(!privacySwitch.isChecked());

        if((boolean)user.getProperty("isAdmin")) response.setVerified(true);
        else response.setVerified(false);
    }

    private void UploadNewEvent() {
        hideSoftKeyBoard();
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadEventsActivity.this);
        builder.setMessage(getString(R.string.add_event)+" ?")
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final ProgressDialog progress = ProgressDialog.show(UploadEventsActivity.this, getString(R.string.working),
                                getString(R.string.uploading_image), true,false);
                        progress.show();
                        final Calendar calendar = Calendar.getInstance();
                        final int thisHour = calendar.get(Calendar.HOUR);
                        final int thisMinute = calendar.get(Calendar.MINUTE);
                        final int thisSecond = calendar.get(Calendar.SECOND);
                        final String fileName =""+user.getUserId()+thisHour+thisMinute+thisSecond+".jpeg";

                        Bitmap finalImage = scaleBitmap( ((BitmapDrawable)eventImage.getDrawable()).getBitmap());
                        // Upload the file
                        Backendless.Files.Android.upload( (finalImage),
                                Bitmap.CompressFormat.JPEG, 80, fileName, "myfiles",
                                new AsyncCallback<BackendlessFile>()
                                {
                                    @Override
                                    public void handleResponse( final BackendlessFile backendlessFile )
                                    {
                                        progress.setMessage(getString(R.string.uploading_info));

                                        Event event = new Event();
                                        event.setTitle(eventTitle.getText().toString());
                                        event.setDescription(eventDescription.getText().toString());
                                        event.setMaison(String.valueOf(houseList.getSelectedItem()));

                                        startDate = eventDateStart.getText().toString() + " " + eventTimeStart.getText().toString() + ":00";
                                        endDate = eventDateEnd.getText().toString() + " " + eventTimeEnd.getText().toString() + ":00";
                                        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss",Locale.US);
                                        try {
                                            Date date = format.parse(startDate);
                                            event.setStarttime(date);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        try {
                                            Date date = format.parse(endDate);
                                            event.setEndtime(date);}
                                        catch (ParseException e) {e.printStackTrace();}

                                        event.setPicture("https://api.backendless.com/709E3602-AABE-41E9-FF47-48B4C07F4700/DC935617-7AC5-AAA8-FF45-EC35472FAA00/files/myfiles/"+fileName);
                                        if(!payableSwitch.isChecked()) {
                                            event.setPriceInt("0");
                                            event.setPriceExt("0");
                                        }
                                        else if(payableSwitch.isChecked() && privacySwitch.isChecked()) {
                                            event.setPriceExt(eventPriceExt.getText().toString());
                                            event.setPriceInt(eventPriceInt.getText().toString());
                                        }
                                        else if(payableSwitch.isChecked() && !privacySwitch.isChecked()){
                                            event.setPriceExt("0");
                                            event.setPriceInt(eventPriceInt.getText().toString());
                                        }
                                        event.setToken(user.getUserId());
                                        event.setOwnerId(user.getUserId());
                                        event.setPrivate(!privacySwitch.isChecked());
                                        event.setOwnerName(user.getProperty("name").toString());
                                        event.setOwnerImage(user.getProperty("facebookId").toString());

                                        if((boolean)user.getProperty("isAdmin")) event.setVerified(true);

                                        // save object asynchronously
                                        Backendless.Persistence.save(event, new AsyncCallback<Event>() {
                                                    public void handleResponse(Event response) {
                                                        progress.dismiss();
                                                        if(!(boolean ) user.getProperty("isAdmin")) {
                                                            Toast.makeText(UploadEventsActivity.this, getString(R.string.waiting_approval), Toast.LENGTH_LONG).show();
                                                            Intent resultIntent = new Intent();
                                                            setResult(Activity.RESULT_OK, resultIntent);
                                                            finish();
                                                        }
                                                        else {
                                                            // Send push notification
                                                            String title = response.getTitle() +" was added by "+user.getProperty("name").toString();
                                                            String channel = "default";
                                                            if(!privacySwitch.isChecked()) channel=user.getProperty("maison").toString();

                                                            final ProgressDialog progressDialog = ProgressDialog.show(UploadEventsActivity.this, getString(R.string.working), "Sending notification...", true, false);
                                                            DeliveryOptions deliveryOptions = new DeliveryOptions();
                                                            deliveryOptions.setPushBroadcast(PushBroadcastMask.ALL);
                                                            PublishOptions publishOptions = new PublishOptions();

                                                            publishOptions.putHeader(PublishOptions.ANDROID_TICKER_TEXT_TAG, "open_event");
                                                            publishOptions.putHeader(PublishOptions.ANDROID_CONTENT_TITLE_TAG, getString(R.string.app_name));
                                                            publishOptions.putHeader(PublishOptions.ANDROID_CONTENT_TEXT_TAG, title);
                                                            publishOptions.putHeader(PublishOptions.IOS_ALERT_TAG, "open_event");
                                                            publishOptions.putHeader(PublishOptions.IOS_TITLE_TAG,getString(R.string.app_name) );
                                                            publishOptions.putHeader(PublishOptions.IOS_SUBTITLE_TAG, title);

                                                            Backendless.Messaging.publish(channel, "New event added", publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
                                                                @Override
                                                                public void handleResponse(MessageStatus response) {
                                                                    progressDialog.cancel();
                                                                    Log.i("MessageStatus", response.toString());
                                                                    Intent resultIntent = new Intent();
                                                                    setResult(Activity.RESULT_OK, resultIntent);
                                                                    finish();
                                                                }

                                                                @Override
                                                                public void handleFault(BackendlessFault fault) {
                                                                    Intent resultIntent = new Intent();
                                                                    setResult(Activity.RESULT_OK, resultIntent);
                                                                    finish();
                                                                    progressDialog.cancel();
                                                                    Toast.makeText(UploadEventsActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                                                                    Log.e("MessageStatus", fault.getMessage());

                                                                }
                                                            });
                                                        }

                                                    }

                                                    public void handleFault(BackendlessFault fault) {
                                                        progress.dismiss();
                                                        if(fault.getCode().equals("1168"))
                                                            Toast.makeText(UploadEventsActivity.this, "Please remove emoticons and smileys from texts and try again..", Toast.LENGTH_LONG).show();
                                                        else
                                                            Toast.makeText(UploadEventsActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();
                                                        Log.e("EventStatus", fault.getMessage()+" code: "+fault.getCode());
                                                    }
                                                }
                                        );
                                    }

                                    @Override
                                    public void handleFault( BackendlessFault backendlessFault ) {
                                        progress.dismiss();
                                        Toast.makeText( UploadEventsActivity.this, R.string.no_connection, Toast.LENGTH_SHORT ).show();
                                        Log.e("ImageStatus", backendlessFault.getMessage());

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
    private void UpdateEvent(){
        hideSoftKeyBoard();
        final ProgressDialog progress = ProgressDialog.show(UploadEventsActivity.this, getString(R.string.working), getString(R.string.updating_info), true, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(UploadEventsActivity.this);
        builder.setMessage(getString(R.string.update_event)+" ?")
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Backendless.Persistence.of(Event.class).findById(getIntent().getStringExtra("EventID"), new AsyncCallback<Event>() {
                            @Override
                            public void handleResponse(Event response) {
                                if (CompletionValidator()) {
                                    progress.show();
                                    hasChanged(response);
                                    Backendless.Persistence.save(response, new AsyncCallback<Event>() {
                                        @Override
                                        public void handleResponse(Event response) {
                                            // Change image
                                            if (imageChanged) {
                                                Uri uri = Uri.parse(response.getPicture());
                                                progress.setMessage(getString(R.string.updating_image));
                                                String[] segments = uri.getPath().split("/");
                                                String idStr = segments[segments.length - 1];
                                                Bitmap finalImage = scaleBitmap((( BitmapDrawable ) eventImage.getDrawable()).getBitmap());
                                                Backendless.Files.Android.upload(finalImage,
                                                        Bitmap.CompressFormat.JPEG, 80, idStr, "myfiles", true, new AsyncCallback<BackendlessFile>() {
                                                            @Override
                                                            public void handleResponse(BackendlessFile response) {
                                                                progress.dismiss();
                                                                if(!(boolean ) user.getProperty("isAdmin"))
                                                                    Toast.makeText(UploadEventsActivity.this, getString(R.string.waiting_approval) , Toast.LENGTH_LONG).show();
                                                                Intent resultIntent = new Intent();
                                                                setResult(Activity.RESULT_OK, resultIntent);
                                                                finish();
                                                            }

                                                            @Override
                                                            public void handleFault(BackendlessFault fault) {
                                                                progress.dismiss();
                                                                if(fault.getCode().equals("1168"))
                                                                    Toast.makeText(UploadEventsActivity.this, "Please remove emoticons and smileys from texts and try again..", Toast.LENGTH_LONG).show();
                                                                else
                                                                    Toast.makeText(UploadEventsActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();                                                            }
                                                        });
                                            }
                                            ///////////////
                                            else {
                                                progress.dismiss();
                                                Intent resultIntent = new Intent();
                                                setResult(Activity.RESULT_OK, resultIntent);
                                                finish();

                                            }
                                        }

                                        @Override
                                        public void handleFault(BackendlessFault fault) {
                                            progress.dismiss();
                                            Toast.makeText(UploadEventsActivity.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                ////////////////////////////////////////////
                            }
                            @Override
                            public void handleFault(BackendlessFault fault) {
                                progress.dismiss();
                                if(fault.getCode().equals("1168"))
                                    Toast.makeText(UploadEventsActivity.this, "Please remove emoticons and smileys from texts and try again..", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(UploadEventsActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();                            }
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
    private void DeleteEvent(){
        hideSoftKeyBoard();
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadEventsActivity.this);
        builder.setMessage(getString(R.string.delete_event)+" ?")
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final ProgressDialog progress = ProgressDialog.show(UploadEventsActivity.this, getString(R.string.working), getString(R.string.deleting_event), true, false);
                        progress.show();
                        Backendless.Persistence.of(Event.class).findById(getIntent().getStringExtra("EventID"), new AsyncCallback<Event>() {
                            @Override
                            public void handleResponse(Event response) {
                                final Uri uri = Uri.parse(response.getPicture());
                                Backendless.Persistence.of(Event.class).remove(response, new AsyncCallback<Long>() {
                                    public void handleResponse(Long response) {
                                        // Contact has been deleted. The response is a time in milliseconds when the object was deleted
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
                                                Toast.makeText(UploadEventsActivity.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    public void handleFault(BackendlessFault fault) {
                                        progress.dismiss();
                                        Toast.makeText(UploadEventsActivity.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                progress.dismiss();
                                Toast.makeText(UploadEventsActivity.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
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
        int maxWidth= eventImage.getWidth();
        int maxHeight = eventImage.getWidth();
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

    private String locationFromFacebookPlace(String location){
        location=location.toLowerCase();
        if (location.contains("espagne")) return "Collège d'Espagne";
        if (location.contains("britannique")) return "Collège Franco-britannique";
        if (location.contains("néerlendais")) return "Collège néerlendais";
        if (location.contains("haraucourt")) return "Donation HARAUCOURT";
        if (location.contains("abreu")) return "Fondation ABREU DE GRANCHER";
        if (location.contains("argentine")) return "Fondation Argentine";
        if (location.contains("avicenne")) return "Fondation Avicenne";
        if (location.contains("biermans")) return "Fondation BIERMANS-LAPÔTRE";
        if (location.contains("danoise")) return "Fondation danoise";
        if (location.contains("monaco")) return "Fondation de Monaco";
        if (location.contains("unis")) return "Fondation des États-Unis";
        if (location.contains("meurthe")) return "Fondation DEUTSCH DE LA MEURTHE";
        if (location.contains("hellénique")) return "Fondation hellénique";
        if (location.contains("paye")) return "Fondation Lucien PAYE";
        if (location.contains("suisse")) return "Fondation suisse";
        if (location.contains("lyon")) return "Fondation Victor LYON";
        if (location.contains("asie")) return "Maison de l'Asie du Sud Est";
        if (location.contains("inde")) return "Maison de l'Inde";
        if (location.contains("italie")) return "Maison de l'Italie";
        if (location.contains("tunisie")) return "Maison de la Tunisie";
        if (location.contains("norvège")) return "Maison de Norvège";
        if (location.contains("métiers")) return "Maison des Élèves Ingénieurs Arts et Métiers";
        if (location.contains("arméni")) return "Maison des Étudiants Arméniens";
        if (location.contains("canad")) return "Maison des Étudiants Canadiens";
        if (location.contains("suéd")) return "Maison des Étudiants Suédois";
        if (location.contains("alimentaires")) return "Maison des Industries Agricoles et Alimentaires";
        if (location.contains("provinces")) return "Maison des Provinces de France";
        if (location.contains("brésil")) return "Maison du Brésil";
        if (location.contains("cambodge")) return "Maison du Cambodge";
        if (location.contains("japon")) return "Maison du Japon";
        if (location.contains("liban")) return "Maison du Liban";
        if (location.contains("maroc")) return "Maison du Maroc";
        if (location.contains("mexique")) return "Maison du Mexique";
        if (location.contains("portugal")) return "Maison du Portugal - André de GOUVEIA";
        if (location.contains("heinrich")) return "Maison HEINRICH HEINE";
        if (location.contains("agroparistech")) return "Maison Internationale AgroParisTech (MINA)";
        if (location.contains("honnorat")) return "Résidence André HONNORAT";
        if (location.contains("lila")) return "Résidence Lila";
        if (location.contains("loire")) return "Résidence Quai de la Loire";
        if (location.contains("garric")) return "Résidence Robert GARRIC";

        return null;
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
