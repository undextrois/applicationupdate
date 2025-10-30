package com.yourdomain.android.updater;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A standalone, reusable application updater.
 *
 * Usage:
 *  ApplicationUpdater updater = new ApplicationUpdater(context);
 *  updater.downloadAndInstall(
 *      "https://example.com/app-latest.apk",
 *      "update.apk",
 *      new ApplicationUpdater.UpdateListener() {
 *          @Override
 *          public void onProgress(int percent) { ... }
 *          @Override
 *          public void onSuccess(File file) { ... }
 *          @Override
 *          public void onError(Exception e) { ... }
 *      });
 */
public class ApplicationUpdater {

    private static final String TAG = "ApplicationUpdater";

    private final Context context;

    public interface UpdateListener {
        @MainThread void onProgress(int percent);
        @MainThread void onSuccess(File file);
        @MainThread void onError(Exception e);
    }

    public ApplicationUpdater(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Downloads an update from the given URL and installs it when finished.
     *
     * @param urlString   The download URL
     * @param fileName    The name of the APK file to save
     * @param listener    Progress and status callback
     */
    public void downloadAndInstall(@NonNull String urlString,
                                   @NonNull String fileName,
                                   @NonNull UpdateListener listener) {

        new Thread(() -> {
            try {
                File file = downloadFile(urlString, fileName, listener);

                // Success
                runOnUi(() -> {
                    listener.onSuccess(file);
                    promptInstall(file);
                });

            } catch (Exception e) {
                Log.e(TAG, "Download failed", e);
                runOnUi(() -> listener.onError(e));
            }
        }).start();
    }

    /**
     * Downloads a file to the app's external files directory.
     */
    @WorkerThread
    private File downloadFile(String urlString, String fileName, UpdateListener listener) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(15000);
        connection.setRequestMethod("GET");
        connection.connect();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }

        int fileLength = connection.getContentLength();
        File outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (outputDir == null) {
            throw new IOException("External storage not available");
        }

        File outputFile = new File(outputDir, fileName);

        try (BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
             FileOutputStream output = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[8192];
            long total = 0;
            int count;
            while ((count = input.read(buffer)) != -1) {
                total += count;
                output.write(buffer, 0, count);

                if (fileLength > 0) {
                    int progress = (int) (total * 100 / fileLength);
                    runOnUi(() -> listener.onProgress(progress));
                }
            }
        } finally {
            connection.disconnect();
        }

        return outputFile;
    }

    /**
     * Launches the system installer for the downloaded APK.
     */
    private void promptInstall(File file) {
        try {
            Uri fileUri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Failed to install update", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Install failed", e);
        }
    }

    /**
     * Helper to safely run code on the main (UI) thread.
     */
    private void runOnUi(Runnable action) {
        android.os.Handler handler = new android.os.Handler(context.getMainLooper());
        handler.post(action);
    }
}
