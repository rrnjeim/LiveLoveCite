package fr.livelovecite.push;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;

import fr.livelovecite.R;

public class PushMessageDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_message_details);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.details));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView messageTitle = findViewById(R.id.title);
        TextView messageTime = findViewById(R.id.time);
        TextView messageContent = findViewById(R.id.content);


        AdminMessagePush messagePush;
        messagePush = (AdminMessagePush ) getIntent().getSerializableExtra("Message Detail");

        if(messagePush!=null){
            messageTitle.setText(messagePush.getTitleMessage());
            if(messagePush.getDate()!=null){
                Calendar cal = new GregorianCalendar();
                cal.setTime(messagePush.getDate());
                String hourOfDay = ""+cal.get(Calendar.HOUR_OF_DAY);
                if(Integer.valueOf(hourOfDay)<10) hourOfDay="0"+hourOfDay;
                String minute = ""+cal.get(Calendar.MINUTE);
                if(Integer.valueOf(minute)<10) minute="0"+minute;

                String time = cal.get(Calendar.DAY_OF_MONTH)+" "
                        + DateFormat.format("MMM",cal.getTimeInMillis())+" "
                        +cal.get(Calendar.YEAR) +" @ "
                        +hourOfDay +":"
                        +minute;
                messageTime.setText(time);
            }
            else {
                findViewById(R.id.timeImage).setVisibility(View.GONE);
                findViewById(R.id.time).setVisibility(View.GONE);
            }

            messageContent.setText(messagePush.getContentMessage());
        }
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
}
