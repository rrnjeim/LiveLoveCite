package fr.livelovecite.search;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import fr.livelovecite.slidingtab.PagerSlidingTabStrip;
import com.backendless.Backendless;

import fr.livelovecite.R;
import fr.livelovecite.setup.BackendSettings;

public class SearchActivity extends AppCompatActivity {
    EditText searchText;
    Button backBTN;
    PagerSlidingTabStrip tabsStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        Backendless.setUrl("http://api.backendless.com");
        Backendless.initApp(this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);

        searchText = findViewById(R.id.searchText);
        backBTN = findViewById(R.id.backBTN);

        backBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, R.anim.push_right_out);
            }
        });

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new SearchAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(2);

        // Give the PagerSlidingTabStrip the ViewPager
        tabsStrip = findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.push_right_out);

    }
}