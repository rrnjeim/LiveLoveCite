package fr.livelovecite.push;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.util.List;

import fr.livelovecite.R;
import fr.livelovecite.setup.BackendSettings;
import fr.livelovecite.uplaods.Maisons;

public class MaisonAdminLoginActivity extends AppCompatActivity {
    private EditText passwordField;
    private Spinner houseList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maison_admin_login);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Admin");

        Backendless.initApp(this, BackendSettings.APP_ID, BackendSettings.SECRET_KEY);

        passwordField = findViewById(R.id.passwordField);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.maison_arrays, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        houseList = findViewById(R.id.houseList);
        houseList.setAdapter(adapter);


        findViewById( R.id.loginBTN ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String channel= houseList.getSelectedItem().toString();
                final String password = passwordField.getText().toString();

                if (password.equals("") || channel == null || channel.equals(getString(R.string.maison_prompt))){
                    Toast.makeText(MaisonAdminLoginActivity.this, "Veuillez remplir toutes les rubriques", Toast.LENGTH_SHORT).show();
                    return;
                }

                final ProgressDialog progressDialog = ProgressDialog.show(MaisonAdminLoginActivity.this, getString(R.string.working), "Authorisation en cours", true, false);
                // Check password for Maison
                String escapeChannel = channel.replace("'","''");
                DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                queryBuilder.setWhereClause("Maison LIKE '"+escapeChannel+ "' AND Password LIKE '"+password+"'");
                Backendless.Persistence.of(Maisons.class).find(queryBuilder, new AsyncCallback<List<Maisons>>() {
                    @Override
                    public void handleResponse(List<Maisons> maisonsBackendlessCollection) {
                        if (maisonsBackendlessCollection.size() > 0) {
                            Intent PushActivity = new Intent( MaisonAdminLoginActivity.this, PushActivity.class );
                            PushActivity.putExtra("Channel", channel);
                            startActivity( PushActivity );
                            finish();
                        }
                        else{
                            progressDialog.cancel();
                            Toast.makeText(MaisonAdminLoginActivity.this, "Accès non authorisé", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {Toast.makeText( MaisonAdminLoginActivity.this, R.string.no_connection , Toast.LENGTH_SHORT ).show();}

                });
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
