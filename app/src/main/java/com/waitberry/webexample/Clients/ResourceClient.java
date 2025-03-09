package com.waitberry.webexample.Clients;

import android.net.http.SslError;
import android.util.Log;
import android.webkit.ValueCallback;

import com.waitberry.webexample.MainActivity;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkNavigationItem;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

public class ResourceClient extends XWalkResourceClient {
    private final XWalkView webview;

    private final MainActivity _activity;

    public boolean isActive = true;

    public Runnable onFinished = null;


    public ResourceClient(MainActivity activity, XWalkView view) {
        super(view);

        this.webview = view;
        _activity = activity;
    }

    @Override
    public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
        if(url.startsWith(_activity.url) || url.startsWith(_activity.alternativeNetworkUrl)) {
            return false;
        }

        _activity.openUrlInBrowser(url);
        return true;
    }

    @Override
    public void onLoadStarted(XWalkView view, String url) {
        if (!isActive) {
            return;
        }

        try {
            JSONObject obj = new JSONObject();
            addNavigationItemDetails(obj);
            obj.put("type", "loadstart");
            obj.put("url", url);

            onNavigationEvent(obj);
        } catch (JSONException ignored) {}
    }

    @Override
    public void onReceivedLoadError(XWalkView view, int errorCode, String description, String failingUrl) {
        if (!isActive) {
            return;
        }

        Log.w("onReceivedLoadError", "errorCode: " + errorCode + " description: " + description + " failingUrl: " + failingUrl);
        this.load("file:///android_asset/index.html");
    }

    @Override
    public void onLoadFinished(XWalkView view, String url) {
        if (!isActive) {
            return;
        }

        if(this.onFinished != null) {
            this.onFinished.run();
        }

        try {
            JSONObject obj = new JSONObject();
            addNavigationItemDetails(obj);
            obj.put("type", "loadstop");
            obj.put("url", url);

            onNavigationEvent(obj);
        } catch (JSONException ignored) {

        }
    }

    @Override
    public void onProgressChanged(XWalkView view, int progressInPercent) {
        if (!isActive) {
            return;
        }

        try {
            JSONObject obj = new JSONObject();
            obj.put("type", "loadprogress");
            obj.put("progress", progressInPercent);
            _activity.setLoadingText("Loading " + progressInPercent + "%");
            onNavigationEvent(obj);
        } catch (JSONException ignored) {}
    }

    @Override
    public void onReceivedSslError(XWalkView view, ValueCallback<Boolean> callback, SslError error) {
        callback.onReceiveValue(true);
    }

    public JSONObject getNavigationItemDetails() {
        XWalkNavigationHistory navigationHistory = webview.getNavigationHistory();

        if (navigationHistory.size() < 1) {
            return null;
        }

        return getNavigationItemDetails(navigationHistory.getCurrentItem());
    }

    public JSONObject getNavigationItemDetails(XWalkNavigationItem navigationItem) {
        JSONObject obj = new JSONObject();
        addNavigationItemDetails(navigationItem, obj);
        return obj;
    }

    private JSONObject addNavigationItemDetails(XWalkNavigationItem navigationItem, JSONObject obj) {
        if (navigationItem == null) {
            return obj;
        }

        try {
            obj.put("navigationUrl", navigationItem.getUrl());
            obj.put("navigationOriginalUrl", navigationItem.getOriginalUrl());
            obj.put("navigationTitle", navigationItem.getTitle());
        } catch (JSONException ignored) {}

        return obj;
    }

    private JSONObject addNavigationItemDetails(JSONObject obj) {
        XWalkNavigationHistory navigationHistory = webview.getNavigationHistory();

        try {
            obj.put("navigationHasPrev", navigationHistory.canGoBack());
            obj.put("navigationHasNext", navigationHistory.canGoForward());
        } catch (JSONException ignored) {}

        if (navigationHistory.size() < 1) {
            return obj;
        }

        XWalkNavigationItem navigationItem = navigationHistory.getCurrentItem();
        return addNavigationItemDetails(navigationItem, obj);
    }

    public void broadcastNavigationItemDetails() {
        try {
            JSONObject obj = new JSONObject();
            addNavigationItemDetails(obj);
            obj.put("type", "status");

            onNavigationEvent(obj);
        } catch (JSONException ignored) {}
    }

    public void onNavigationEvent(JSONObject obj) {
        triggerJavascriptHandler("onNavigationEvent", obj);
    }

    public void triggerJavascriptHandler(String handlerName, JSONObject obj) {
        webview.evaluateJavascript("window." + handlerName + " && window." + handlerName + "(" + obj + ")", null);
    }

    public void sendClientCode(String code) {
        webview.evaluateJavascript(code, null);
    }

    public void goPrev() {
        webview.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
    }

    public void goNext() {
        webview.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.FORWARD,1);
    }

    public void reload(boolean forceReload) {
        webview.reload(forceReload ? XWalkView.RELOAD_IGNORE_CACHE : XWalkView.RELOAD_NORMAL);
    }

    public void load(String url) {
        webview.loadUrl(url);
    }
}