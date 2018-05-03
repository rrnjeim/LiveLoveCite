package fr.livelovecite.push;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import android.widget.EditText;

import android.widget.Toast;
import com.backendless.Backendless;

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.DeliveryOptions;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.backendless.messaging.PushBroadcastMask;

import fr.livelovecite.R;
import fr.livelovecite.setup.BackendSettings;

public class PushActivity extends AppCompatActivity {

    private EditText titleField, messageField;
    public void onCreate(Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_send_push );

        Backendless.initApp(this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);

        titleField = findViewById(R.id.titleField);
        messageField = findViewById( R.id.messageField );

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("Channel"));

        final String channel = getIntent().getStringExtra("Channel");

        findViewById( R.id.sendBTN ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                final String message = messageField.getText().toString();
                final String title = titleField.getText().toString();

                if (message.equals("") || title.equals("")){
                    Toast.makeText(PushActivity.this, "Contenu invalide", Toast.LENGTH_SHORT).show();

                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(PushActivity.this);
                builder.setMessage("Envoyer le message à "+channel+" ?")
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                final ProgressDialog progressDialog = ProgressDialog.show(PushActivity.this, getString(R.string.working), "Diffusion du message", true, false);


                                DeliveryOptions deliveryOptions = new DeliveryOptions();
                                deliveryOptions.setPushBroadcast(PushBroadcastMask.ALL);
                                PublishOptions publishOptions = new PublishOptions();

                                publishOptions.putHeader(PublishOptions.ANDROID_TICKER_TEXT_TAG, "llc_admin_message");
                                publishOptions.putHeader(PublishOptions.ANDROID_CONTENT_TITLE_TAG, getString(R.string.app_name));
                                publishOptions.putHeader(PublishOptions.ANDROID_CONTENT_TEXT_TAG, title);
                                publishOptions.putHeader(PublishOptions.IOS_TITLE_TAG, title);
                                publishOptions.putHeader(PublishOptions.IOS_SUBTITLE_TAG, message);
                                publishOptions.putHeader(PublishOptions.IOS_TITLE_TAG, getString(R.string.app_name));
                                Backendless.Messaging.publish(channel, message, publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
                                    @Override
                                    public void handleResponse(MessageStatus response) {
                                        progressDialog.dismiss();
                                        finish();
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        progressDialog.dismiss();
                                        Log.d("PushAdminException", fault.getMessage() +"-->"+ fault.getDetail());

                                        Toast.makeText(PushActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
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
        });

        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN );
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
        super.onBackPressed();
    }
}