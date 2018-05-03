package fr.livelovecite.sidemenu;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import fr.livelovecite.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutUsActivity extends AppCompatActivity {
    public static String FACEBOOK_URL = "https://www.facebook.com/livelovecite";
    public static String FACEBOOK_PAGE_ID = "Live Love Cite";


    public AboutUsActivity() {
        // Required empty public constructor
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_about_us);

        ImageView ralphPP = findViewById(R.id.ralphPP);
        ImageView aliPP = findViewById(R.id.aliPP);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.about_us));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Picasso.with(this)
                .load("https://graph.facebook.com/10156628847680048/picture?type=large")
                .placeholder(R.drawable.default_user_icon)
                .into(ralphPP);
        Picasso.with(this)
                .load("https://graph.facebook.com/10153346237746962/picture?type=large")
                .placeholder(R.drawable.default_user_icon)
                .into(aliPP);

        Button ralphContact = findViewById(R.id.ralphContact);
        Button aliContact = findViewById(R.id.aliContact);
        ralphContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence contacts[] = new CharSequence[]{getString(R.string.send_email), "LinkedIn"};

                AlertDialog.Builder builder = new AlertDialog.Builder(AboutUsActivity.this);
                builder.setTitle("Ralph");
                builder.setItems(contacts, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0)
                            sendEmail("ralph.r.njeim@hotmail.com", "Ralph");
                        else if (which == 1)
                            openLinkedIn("ralph-noujeim-415625aa");
                    }
                });
                builder.show();
            }
        });

        aliContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence contacts[] = new CharSequence[]{getString(R.string.send_email), "LinkedIn"};

                AlertDialog.Builder builder = new AlertDialog.Builder(AboutUsActivity.this);
                builder.setTitle("Ali");
                builder.setItems(contacts, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0)
                            sendEmail("alisrour_94@hotmail.com", "Ali");
                        else if (which == 1)
                            openLinkedIn("ali-srour-5998a3aa");
                    }
                });
                builder.show();
            }
        });


        ImageView fbBTN = findViewById(R.id.fbBTN);
        ImageView instaBTN = findViewById(R.id.instaBTN);
        ImageView snapBTN = findViewById(R.id.snapBTN);

        fbBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                String facebookUrl = getFacebookPageURL(AboutUsActivity.this);
                facebookIntent.setData(Uri.parse(facebookUrl));
                startActivity(facebookIntent);
            }
        });

        instaBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://instagram.com/_u/livelovecite");
                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
                likeIng.setPackage("com.instagram.android");
                try {
                    startActivity(likeIng);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://instagram.com/livelovecite")));
                }

            }
        });

        snapBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nativeAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://snapchat.com/add/livelovecite"));
                startActivity(nativeAppIntent);
            }
        });
    }

    //method to get the right URL to use in the intent
    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                return "fb://page/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_animation, R.anim.push_right_out);
    }

    private void sendEmail(String email, String name) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/html");
        i.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
        i.putExtra(Intent.EXTRA_SUBJECT, "Hello " + name + "\n");
        i.putExtra(Intent.EXTRA_TEXT, "\n\nSent from Live Love Cité mobile app");
        try {
            startActivity(Intent.createChooser(i, getString(R.string.send_email)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openLinkedIn(String name) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://" + name));
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.isEmpty()) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.linkedin.com/profile/view?id=" + name));
        }
        startActivity(intent);
    }
}
//    private void testingPush(){
//        // Send push notification
//        String title = "TestingPush";
//
//        final ProgressDialog progressDialog = ProgressDialog.show(AboutUsActivity.this, getString(R.string.working), "Sending notification...", true, false);
//        DeliveryOptions deliveryOptions = new DeliveryOptions();
//        deliveryOptions.setPushBroadcast(PushBroadcastMask.ALL);
//
//        PublishOptions publishOptions = new PublishOptions();
//        publishOptions.putHeader(PublishOptions.ANDROID_TICKER_TEXT_TAG, "llc_admin_message");
//        publishOptions.putHeader(PublishOptions.ANDROID_CONTENT_TITLE_TAG, getString(R.string.app_name));
//        publishOptions.putHeader(PublishOptions.ANDROID_CONTENT_TEXT_TAG, title);
//        publishOptions.putHeader(PublishOptions.MESSAGE_TAG, "llc_admin_message");
//        publishOptions.putHeader(PublishOptions.IOS_ALERT_TAG, title);
//
//        String messageToSend = "testing push notif 2";
//
//        Backendless.Messaging.publish("Buzz6493", messageToSend, publishOptions, deliveryOptions, new BackendlessCallback<MessageStatus>() {
//            @Override
//            public void handleResponse(MessageStatus response) {
//                progressDialog.cancel();
//            }
//
//            @Override
//            public void handleFault(BackendlessFault fault) {
//                progressDialog.cancel();
//            }
//        });
//    }
//}
//"android-ticker-text":"llc_admin_message", "android-content-title":"Live Love Cité", "android-content-text":"Sunday Funday.. Enjoy your day! <3"
