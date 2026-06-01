package omni.com.newtaipeisdk;

import static omni.com.newtaipeisdk.NewTaipeiSDKActivity.WEBVIEW_TITLE;
import static omni.com.newtaipeisdk.NewTaipeiSDKActivity.WEBVIEW_URL;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    private WebView wv;
    private ProgressDialog mProgressDialog;
    private String url;
    private String title;
    private TextView titleTV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);

        titleTV = findViewById(R.id.title);
        findViewById(R.id.activity_webview_fl_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wv.canGoBack()) {
                    wv.goBack();
                } else {
                    finish();
                }
            }
        });

        wv = findViewById(R.id.activity_webview_wv);
        url = getIntent().getStringExtra(WEBVIEW_URL);
        title = getIntent().getStringExtra(WEBVIEW_TITLE);

        titleTV.setText(title);

        loadWebView(url);
    }

    private void loadWebView(String url) {

        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setSupportZoom(true);
//        wv.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
//        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setDomStorageEnabled(true);
//        wv.getSettings().setDatabaseEnabled(true);
//        wv.getSettings().setAppCacheEnabled(true);
//        wv.setWebChromeClient(new WebChromeClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            wv.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        wv.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Build.VERSION.SDK_INT < 26) {
                    view.loadUrl(url);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, final String url) {
            }
        });
        wv.loadUrl(url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (wv.canGoBack()) {
                        wv.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
