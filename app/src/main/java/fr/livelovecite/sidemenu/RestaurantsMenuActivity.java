package fr.livelovecite.sidemenu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import fr.livelovecite.R;

public class RestaurantsMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurants_menu);

        Toolbar toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Food Time!");

        ImageButton restoUBTN = findViewById(R.id.restoUBTN);
        restoUBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent menuIntent = new Intent(new Intent(RestaurantsMenuActivity.this, RestoMenuWebViewActivity.class));
                menuIntent.putExtra("title","Restaurant Universitaire");
                menuIntent.putExtra("needsDocs", false);
                menuIntent.putExtra("url", "http://www.ciup.fr/restaurant-slider/les-menus-14070/");
                startActivity(menuIntent);
                overridePendingTransition( R.anim.push_left_in, R.anim.no_animation);
            }
        });
        ImageButton ceBTN = findViewById(R.id.ceBTN);
        ceBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent menuIntent = new Intent(new Intent(RestaurantsMenuActivity.this, RestoMenuWebViewActivity.class));
                menuIntent.putExtra("title","Collège d'Espagne");
                menuIntent.putExtra("needsDocs", true);
                //menuIntent.putExtra("pdf", "http://www.colesp.org/images/pdfs/restaurante/menu.pdf");
                menuIntent.putExtra("url", "https://api.backendless.com/25A202B1-D496-55D7-FF27-C5C60463CD00/v1/files/MenuResto/Menu+de+la+semana.pdf");
                startActivity(menuIntent);
                overridePendingTransition( R.anim.push_left_in, R.anim.no_animation);
            }
        });

//        findViewById(R.id.marocBTN).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent fullScreenIntent = new Intent(RestaurantsMenuActivity.this, FullscreenActivity.class);
//                fullScreenIntent.putExtra("Image", "https://api.backendless.com/25A202B1-D496-55D7-FF27-C5C60463CD00/v1/files/MenuResto/Au+Pays+des+Delices.jpg");
//                startActivity(fullScreenIntent);
//            }
//        });
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
}
