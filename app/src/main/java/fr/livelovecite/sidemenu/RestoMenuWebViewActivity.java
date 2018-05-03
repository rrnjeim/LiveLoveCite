package fr.livelovecite.sidemenu;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import fr.livelovecite.R;

public class RestoMenuWebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resto_menu_web_view);

        Toolbar toolbar = findViewById(R.id.toolbar4);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String title;
        if (!getIntent().getStringExtra("title").isEmpty()) {
            title = getIntent().getStringExtra("title");
            getSupportActionBar().setTitle(title);
        }

        WebView webView = findViewById(R.id.restoMenuWebView);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        final ProgressDialog pd = ProgressDialog.show(this, "", getString(R.string.loading),true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if(pd!=null && pd.isShowing()) {
                    pd.dismiss();
                }
            }
        });
        if(getIntent().getBooleanExtra("needsDocs",true)){
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl("https://docs.google.com/gview?embedded=true&url="+getIntent().getStringExtra("url"));
        }else
            webView.loadUrl(getIntent().getStringExtra("url"));
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
