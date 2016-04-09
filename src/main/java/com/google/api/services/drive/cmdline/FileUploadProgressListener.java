package com.google.api.services.drive.cmdline;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

import java.io.IOException;
import java.text.NumberFormat;

/**
 * The File Upload Progress Listener.
 *
 */
public class FileUploadProgressListener implements MediaHttpUploaderProgressListener {

  private double uploadProgress = 0.0;

  @Override
  public void progressChanged(MediaHttpUploader uploader) throws IOException {
    switch (uploader.getUploadState()) {
      case INITIATION_STARTED:
        View.header2("Upload Initiation has started.");
        break;
      case INITIATION_COMPLETE:
        View.header2("Upload Initiation is Complete.");
        break;
      case MEDIA_IN_PROGRESS:
        View.header2("Upload is In Progress: "
            + NumberFormat.getPercentInstance().format(uploader.getProgress()));
        uploadProgress = uploader.getProgress();
        break;
      case MEDIA_COMPLETE:
        View.header2("Upload is Complete!");
        break;
    }
  }

  public double getUploadProgress() {
    return uploadProgress;
  }
}
