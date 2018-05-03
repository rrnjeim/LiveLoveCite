package fr.livelovecite.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminMessagePush implements Serializable{
    private String titleMessage;
    private String contentMessage;
    private Date date;
    private String tag;


    String getTitleMessage() {
        return titleMessage;
    }

    void setTitleMessage(String titleMessage) {
        this.titleMessage = titleMessage;
    }

    public String getContentMessage() {
        return contentMessage;
    }

    void setContentMessage(String contentMessage) {
        this.contentMessage = contentMessage;
    }

    public Date getDate() {return date;}

    public void setDate(Date date) {this.date = date;}

    private List<AdminMessagePush> getMessagesListFromPrefs(Context context){
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = appSharedPrefs.getString("MessagesList", "");
        Type type = new TypeToken<List<AdminMessagePush>>(){}.getType();
        return gson.fromJson(json, type);
    }

    void  setMessagesListToPrefs(Context context, AdminMessagePush newMessage){
        List<AdminMessagePush> ListToSave = getMessagesListFromPrefs(context);
        if (ListToSave==null)
            ListToSave = new ArrayList<>();

        ListToSave.add(0,newMessage);

        Log.i("MessageListPrefs", ListToSave.toString());
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(ListToSave);
        prefsEditor.putString("MessagesList", json);
        prefsEditor.apply();
    }
    @Override
    public String toString() {
        return getTitleMessage()+" : "+getContentMessage();
    }

    public String getTag() {
        return tag;
    }

    void setTag(String tag) {
        this.tag = tag;
    }
}
