package fr.livelovecite.push;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import fr.livelovecite.R;

public class PushMessagesListActivity extends AppCompatActivity {

    List<AdminMessagePush> messagePushList = new ArrayList<>();
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_messages_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.notice));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = findViewById(android.R.id.list);

        TextView noyet= findViewById(R.id.nothingYet);
        noyet.setVisibility(View.INVISIBLE);

        loadMessages();

        PushMessagesListAdapter adapter = new PushMessagesListAdapter(this,R.layout.messages_item_list, messagePushList);
        listView.setAdapter(adapter);

        if(messagePushList.size()==0){
            noyet.setText(R.string.no_messages);
            noyet.setVisibility(View.VISIBLE);
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
    public void onBackPressed(){
        super.onBackPressed();
    }

    private void loadMessages(){
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = appSharedPrefs.getString("MessagesList", "");
        if(!json.isEmpty()){
            Type type = new TypeToken<List<AdminMessagePush>>(){}.getType();
            messagePushList = gson.fromJson(json, type);
        }
    }

}
