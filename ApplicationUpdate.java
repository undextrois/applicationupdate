package com.yourdomain.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

/**
 * ApplicationUpdate Activity
 * Handles downloading and updating the app from a specified URL.
 */
public class ApplicationUpdate extends Activity implements View.OnClickListener {

    private static final String TAG = "ApplicationUpdate";
    private static final String DOWNLOAD_URL = "http://www.webdunnit.com/";
    private static final String FILE_NAME = "home_page.jpg";
    private static final String APP_PATH = "/data/data/com.yourdomain.android/";

    private ProgressBar progressBar;
    private Handler uiHandler;
    private boolean isDownloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.application_update);

        progressBar = findViewById(R.id.progressBar); // Replace with actual ProgressBar in layout
        Button startButton = findViewById(R.id.startbtn);
        startButton.setOnClickListener(this);

        uiHandler = new Handler(Looper.getMainLooper());

        Log.i(TAG, "System properties: " + System.getProperties());
        Log.i(TAG, "Storage directory: " + Environment.getExternalStorageDirectory());
        Log.i(TAG, "Initialization complete.");
    }

    @Override
    public void onClick(View view) {
        if (isDownloading) {
            Toast.makeText(this, "Download already in progress", Toast.LENGTH_SHORT).show();
            return;
        }

        isDownloading = true;
        Toast.makeText(this, "Starting update download...", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Starting download from " + DOWNLOAD_URL);

        // Run the download in a background thread
        new Thread(() -> {
            try {
                // Perform download
                DownloadManager.DownloadFromUrl(DOWNLOAD_URL, FILE_NAME);

                // Update progress bar continuously
                while (DownloadManager.M_statusProgress < 100) {
                    int progress = DownloadManager.M_statusProgress;
                    uiHandler.post(() -> progressBar.setProgress(progress));
                    Thread.sleep(500);
                }

                // Once complete
                uiHandler.post(() -> {
                    progressBar.setProgress(100);
                    isDownloading = false;
                    showInstallDialog();
                });

            } catch (Exception e) {
                Log.e(TAG, "Download failed", e);
                uiHandler.post(() -> {
                    isDownloading = false;
                    Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    /**
     * Prompts the user to install the downloaded update.
     */
    private void showInstallDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Install Update")
                .setMessage("Do you want to install the downloaded update?")
                .setCancelable(true)
                .setPositiveButton("Install", (dialog, which) -> installUpdate())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Launches the Android package installer for the downloaded file.
     */
    private void installUpdate() {
        try {
            File file = new File(APP_PATH, FILE_NAME);
            Uri fileUri = Uri.fromFile(file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "Failed to launch installer", e);
            Toast.makeText(this, "Failed to install update", Toast.LENGTH_SHORT).show();
        }
    }
}
