package com.waitberry.webexample;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.TextView;

import com.waitberry.webexample.Clients.ResourceClient;
import com.waitberry.webexample.Clients.UIClient;
import com.waitberry.webexample.Listeners.DownloadListener;

import org.xwalk.core.XWalkActivity;
import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkFileChooser;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;

public class MainActivity extends XWalkActivity {
    public Boolean useAlternativeUrl = false;
    public String url = "file:///android_asset/index.html";
    public String alternativeNetworkUrl = "https://subs.ferreirapablo.com";

    public XWalkFileChooser fileChooser = null;
    public XWalkView webView = null;
    public ViewGroup loadingScreen = null;
    public ResourceClient resourceClient = null;
    public boolean isVisible = true;
    public int animationTime = 100;
    public boolean animationInProgress = false;
    public String JavascriptInterfaceAlias = "nativeWebView";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (XWalkView) findViewById(R.id.xWalkView);
        loadingScreen = (ViewGroup) findViewById(R.id.loadingScreen);
        showLoadingScreen();
    }

    public void showLoadingScreen() {
        loadingScreen.setVisibility(View.VISIBLE);
        Animation scaleAnimation = new AlphaAnimation(0, 1);
        scaleAnimation.setDuration(animationTime / 2);
        scaleAnimation.setInterpolator(new LinearInterpolator());
        ImageView logo = (ImageView)loadingScreen.findViewById(R.id.logo);
        logo.startAnimation(scaleAnimation);
    }

    public void setLoadingText(String text) {
        TextView loadingText = (TextView) loadingScreen.findViewById(R.id.loadingText);
        loadingText.setText(text);
    }

    public void openUrlInBrowser(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        this.startActivity(browserIntent);
    }

    public void hideLoadingScreen() {
        if(loadingScreen.getVisibility() == View.GONE || animationInProgress) {
            return;
        }
        animationInProgress = true;
        Animation scaleAnimation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(animationTime);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setInterpolator(new AnticipateInterpolator());
        ImageView logo = (ImageView)loadingScreen.findViewById(R.id.logo);
        logo.startAnimation(scaleAnimation);


        Animation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(animationTime);
        alphaAnimation.setStartOffset(animationTime);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loadingScreen.setVisibility(View.GONE);
                animationInProgress = false;
            }
        });

        loadingScreen.startAnimation(alphaAnimation);
    }

    private int getPackageVersionCode() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return 0;
        }
    }

    private void updateClientStatus() {
        if(resourceClient == null) {
            return;
        }

        resourceClient.sendClientCode("if(!window."+ JavascriptInterfaceAlias +") { window."+ JavascriptInterfaceAlias +" = {}; }");
        if(isVisible) {
            resourceClient.sendClientCode("window."+ JavascriptInterfaceAlias +".visible = true");
        } else {
            resourceClient.sendClientCode("window."+ JavascriptInterfaceAlias +".visible = false");
        }
        resourceClient.sendClientCode("window."+ JavascriptInterfaceAlias +".packageName = '" + getPackageName() + "'");
        resourceClient.sendClientCode("window."+ JavascriptInterfaceAlias +".versionCode = " + getPackageVersionCode());
    }

    public void triggerBlobDownload(String blobUrl) {
        resourceClient.sendClientCode("root.downloadManager.download('" + blobUrl + "')");
    }

    @Override
    protected void onResume() {
        updateClientStatus();
        super.onResume();
    }

    @Override
    protected void onPause() {
        updateClientStatus();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (fileChooser != null) {
            fileChooser.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onXWalkReady() {
        XWalkPreferences.setValue(XWalkPreferences.ENABLE_JAVASCRIPT, true);
        XWalkPreferences.setValue(XWalkPreferences.ENABLE_EXTENSIONS, true);
        XWalkPreferences.setValue(XWalkPreferences.SPATIAL_NAVIGATION, true);

        XWalkCookieManager cookieManager = new XWalkCookieManager();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptFileSchemeCookies(true);

        UIClient uiClient = new UIClient(this, webView);
        resourceClient = new ResourceClient(this, webView);

        DownloadListener downloadListener = new DownloadListener(this);
        webView.clearSslPreferences();
        webView.setResourceClient(resourceClient);
        webView.setUIClient(uiClient);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        webView.setDownloadListener(downloadListener);

        webView.addJavascriptInterface(new Object()
        {
            @org.xwalk.core.JavascriptInterface
            public void onLoad()
            {
                webView.post(() -> {
                    updateClientStatus();
                    resourceClient.sendClientCode("document.dispatchEvent(new Event(\"renderuichanges\"));");
                });
            }

            @org.xwalk.core.JavascriptInterface
            public void refreshCachedVersion()
            {
                webView.post(() -> {
                    webView.clearCache(true);
                    webView.reload(XWalkView.RELOAD_IGNORE_CACHE);
                });
            }

            @org.xwalk.core.JavascriptInterface
            public void downloadBase64(String base64, String filename)
            {
                downloadListener.createAndSaveFileFromBase64Url(base64, filename);
            }
        }, JavascriptInterfaceAlias);

        resourceClient.load(useAlternativeUrl ? alternativeNetworkUrl : url);
        resourceClient.onFinished = () -> {
            hideLoadingScreen();
            updateClientStatus();
            resourceClient.sendClientCode("if(document.readyState === 'complete') { if(window."+ JavascriptInterfaceAlias +") { window."+ JavascriptInterfaceAlias +".onLoad(); }; } else { document.addEventListener(\"load\", function() { if(window."+ JavascriptInterfaceAlias +") { window."+ JavascriptInterfaceAlias +".onLoad(); }; } ) }");
        };
    }
}
