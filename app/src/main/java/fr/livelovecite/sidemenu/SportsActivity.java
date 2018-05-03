package fr.livelovecite.sidemenu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import fr.livelovecite.R;

public class SportsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sports);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.sports));
        Button backBTN = findViewById(R.id.backBTN);
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {onBackPressed();}});


        TextView sportsEmail = findViewById(R.id.sportsEmail);
        sportsEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/html");
                i.putExtra(android.content.Intent.EXTRA_EMAIL  ,  new String[]{"sports@ciup.fr"});
                i.putExtra(Intent.EXTRA_TEXT   , "\n\nSent from Live Love Cité mobile app");
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.send_email)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SportsActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView sportsListPDF = findViewById(R.id.sportsListPDF);
        sportsListPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent menuIntent = new Intent(new Intent(SportsActivity.this, RestoMenuWebViewActivity.class));
                menuIntent.putExtra("title","Sports Brochure");
                menuIntent.putExtra("needsDocs", true);
                menuIntent.putExtra("url", "http://www.ciup.fr/wp-content/uploads/2017/10/CIUP_sportsFR_17_18-MAJ_pages.pdf");
                startActivity(menuIntent);
                overridePendingTransition( R.anim.push_left_in, R.anim.no_animation);
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
        super.onBackPressed();
        overridePendingTransition(R.anim.no_animation, R.anim.push_right_out);
    }
}
