package com.waitberry.webexample.Clients;

import android.net.Uri;
import android.webkit.ValueCallback;

import com.waitberry.webexample.MainActivity;

import org.xwalk.core.XWalkFileChooser;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

public class UIClient extends XWalkUIClient {
    private final MainActivity _activity;

    public UIClient(MainActivity activity, XWalkView view) {
        super(view);
        _activity = activity;
    }

    @Override
    public void openFileChooser(XWalkView view, ValueCallback<Uri> uploadFile, String acceptType, String capture) {
        if (_activity.fileChooser == null) {
            _activity.fileChooser = new XWalkFileChooser(_activity);
        }
        _activity.fileChooser.showFileChooser(uploadFile, acceptType, capture);
    }
}
