ApplicationUpdater updater = new ApplicationUpdater(this);

updater.downloadAndInstall(
    "https://example.com/latest-release.apk",
    "update.apk",
    new ApplicationUpdater.UpdateListener() {
        @Override
        public void onProgress(int percent) {
            progressBar.setProgress(percent);
        }

        @Override
        public void onSuccess(File file) {
            Toast.makeText(MainActivity.this, "Download complete", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(Exception e) {
            Toast.makeText(MainActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    });
