package com.waitberry.webexample.Listeners;

import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.waitberry.webexample.MainActivity;

import org.xwalk.core.XWalkDownloadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadListener extends XWalkDownloadListener {
    private final MainActivity activity;

    public DownloadManager manager;

    public DownloadListener(MainActivity activity) {
        super(activity.getApplicationContext());
        this.activity = activity;
        this.manager = (DownloadManager) this.activity.getSystemService(Context.DOWNLOAD_SERVICE);;
    }

    public static String getFileNameFromURL(String url) {
        if (url == null) {
            return "";
        }
        try {
            URL resource = new URL(url);
            String host = resource.getHost();
            if (!host.isEmpty() && url.endsWith(host)) {
                // handle ...example.com
                return "";
            }
        }
        catch(MalformedURLException e) {
            return "";
        }

        int startIndex = url.lastIndexOf('/') + 1;
        int length = url.length();

        // find end index for ?
        int lastQMPos = url.lastIndexOf('?');
        if (lastQMPos == -1) {
            lastQMPos = length;
        }

        // find end index for #
        int lastHashPos = url.lastIndexOf('#');
        if (lastHashPos == -1) {
            lastHashPos = length;
        }

        // calculate the end index
        int endIndex = Math.min(lastQMPos, lastHashPos);
        return url.substring(startIndex, endIndex);
    }

    public String getDownloadedMessage(String directory, String fileName) {

        if(directory.equals(Environment.DIRECTORY_PICTURES)) {
            return "Picture downloaded, check your pictures folder for " + fileName + ".";
        }

        if(directory.equals(Environment.DIRECTORY_MOVIES)) {
            return "Video downloaded, check your movies/videos folder for " + fileName + ".";
        }

        if(directory.equals(Environment.DIRECTORY_MUSIC)) {
            return "Audio downloaded, check your music folder for " + fileName + ".";
        }

        return "Download complete, check your downloads folder for " + fileName + ".";
    }

    public String createAndSaveFileFromBase64Url(String url, String fileName) {

        Context context = activity.getApplicationContext();

        String mimetype = url.substring(url.indexOf(":") + 1, url.indexOf("/"));

        String directory = Environment.DIRECTORY_DOWNLOADS;
        if(mimetype.contains("image")) {
            directory = Environment.DIRECTORY_PICTURES;
        } else if(mimetype.contains("video")) {
            directory = Environment.DIRECTORY_MOVIES;
        } else if(mimetype.contains("audio")) {
            directory = Environment.DIRECTORY_MUSIC;
        };

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String filetype = url.substring(url.indexOf("/") + 1, url.indexOf(";"));
        String filename = !fileName.isEmpty() ? fileName : mimetype + "_" + (int)(Math.random() * 99999) + "." + filetype;
        File file = new File(path, filename);
        try {
            Toast downloadingToast = Toast.makeText(context, "Downloading...", Toast.LENGTH_LONG);
            downloadingToast.show();
            if(!path.exists())
                path.mkdirs();
            if(!file.exists())
                file.createNewFile();

            String base64EncodedString = url.substring(url.indexOf(",") + 1);
            byte[] decodedBytes = Base64.decode(base64EncodedString, Base64.DEFAULT);
            OutputStream os = new FileOutputStream(file);
            os.write(decodedBytes);
            os.close();
            downloadingToast.cancel();

            //Tell the media scanner about the new file so that it is immediately available to the user.
            MediaScannerConnection.scanFile(context,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });

            Toast.makeText(context, getDownloadedMessage(directory, fileName), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.w("ExternalStorage", "Error writing " + file, e);
            Toast.makeText(context, "Error downloading file.", Toast.LENGTH_LONG).show();
        }

        return file.toString();
    }

    @Override
    public void onDownloadStart(String url,
                                String userAgent,
                                String contentDisposition,
                                String mimetype,
                                long contentLength) {


        if (url.startsWith("blob")) {  //when url is base64 encoded data
            activity.triggerBlobDownload(url);
            return;
        }

        if (url.startsWith("data:")) {  //when url is base64 encoded data
            createAndSaveFileFromBase64Url(url, "");
            return;
        }

        activity.openUrlInBrowser(url);
    }
}
