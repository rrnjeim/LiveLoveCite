package fr.livelovecite.push;


import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.backendless.messaging.PublishOptions;
import com.backendless.push.BackendlessPushService;

import fr.livelovecite.setup.PrefUtils;

public class PushService extends BackendlessPushService {

    @Override
    public void onRegistered(Context context, String registrationId ) {System.out.println("device registered --> "+registrationId);}

    @Override
    public void onUnregistered( Context context, Boolean unregistered ) {System.out.println("device unregistered");}

    @Override
    public boolean onMessage(final Context context, Intent intent ) {

        Log.i("PushIntent", intentToString(intent));
        if(intent.getStringExtra( PublishOptions.ANDROID_TICKER_TEXT_TAG).equals("llc_admin_message")){
            AdminMessagePush adminMessagePush = new AdminMessagePush();
            adminMessagePush.setTag("llc_admin_message");
            adminMessagePush.setDate(java.util.Calendar.getInstance().getTime());
            adminMessagePush.setTitleMessage(intent.getStringExtra( PublishOptions.ANDROID_CONTENT_TEXT_TAG ));
            adminMessagePush.setContentMessage(intent.getExtras().getString("message"));
            adminMessagePush.setMessagesListToPrefs(getApplicationContext(),adminMessagePush);

            int unreadMSGS= Integer.valueOf(PrefUtils.getFromPrefs(getApplicationContext(),PrefUtils.PREFS_NOTIF_UNREAD_MESSAGES,"0"));
            PrefUtils.saveToPrefs(getApplicationContext(),PrefUtils.PREFS_NOTIF_UNREAD_MESSAGES,String.valueOf(unreadMSGS+1));
        }
        else if(intent.getStringExtra( PublishOptions.ANDROID_TICKER_TEXT_TAG).equals("llc_admin_image")){
            AdminMessagePush adminMessagePush = new AdminMessagePush();
            adminMessagePush.setTag("llc_admin_image");
            adminMessagePush.setDate(java.util.Calendar.getInstance().getTime());
            adminMessagePush.setTitleMessage(intent.getStringExtra( PublishOptions.ANDROID_CONTENT_TEXT_TAG ));
            adminMessagePush.setContentMessage(intent.getExtras().getString("message"));
            adminMessagePush.setMessagesListToPrefs(getApplicationContext(),adminMessagePush);

            int unreadMSGS= Integer.valueOf(PrefUtils.getFromPrefs(getApplicationContext(),PrefUtils.PREFS_NOTIF_UNREAD_MESSAGES,"0"));
            PrefUtils.saveToPrefs(getApplicationContext(),PrefUtils.PREFS_NOTIF_UNREAD_MESSAGES,String.valueOf(unreadMSGS+1));
        }
        else if(intent.getStringExtra( PublishOptions.ANDROID_TICKER_TEXT_TAG).equals("llc_app_update"))
            PrefUtils.saveToPrefs(getApplicationContext(),PrefUtils.PREFS_NOTIF_APP_UPDATE, intent.getExtras().getString("message"));

            // When returning 'true', default Backendless onMessage implementation will be executed.
        // The default implementation displays the notification in the Android Notification Center.
        // Returning false, cancels the execution of the default implementation.
        return true;
    }

    @Override
    public void onError( Context context, String message ) {Log.e("Push Error", message);}

    public static String intentToString(Intent intent) {
        if (intent == null)
            return "";

        StringBuilder stringBuilder = new StringBuilder("action: ")
                .append(intent.getAction())
                .append(" data: ")
                .append(intent.getDataString())
                .append(" extras: ")
                ;
        for (String key : intent.getExtras().keySet())
            stringBuilder.append(key).append("=").append(intent.getExtras().get(key)).append(" ");

        return stringBuilder.toString();

    }
}

// "android-ticker-text":"ticker text", "android-content-title":"content title", "android-content-text":"content text"