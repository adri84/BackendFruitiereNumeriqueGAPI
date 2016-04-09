package com.google.api.services.drive.cmdline;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;

/**
 * The File Download Progress Listener.
 *
 */
public class FileDownloadProgressListener implements MediaHttpDownloaderProgressListener {

  private double downloadProgress = 0.0;

  @Override
  public void progressChanged(MediaHttpDownloader downloader) {
    switch (downloader.getDownloadState()) {
      case MEDIA_IN_PROGRESS:
        View.header2("Download is in progress: " + downloader.getProgress());
        downloadProgress = downloader.getProgress();
        break;
      case MEDIA_COMPLETE:
        View.header2("Download is Complete!");
        break;
    }
  }

  public double getDownloadProgress() {
    return downloadProgress;
  }
}
