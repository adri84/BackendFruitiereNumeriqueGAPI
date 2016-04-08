package com.google.api.services.drive.cmdline;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class DriveTools {

  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private static final String APPLICATION_NAME = "Backend_Fruitière_Numérique/1.0";
  public static final String VILLAGE_MEDIAS = "VisiteTablette";
  public static final String CHATEAU_MEDIAS = "VisiteChateau";
  private static final String APP_ZIP = "application/zip";
  public static final String ZIP_EXT = ".zip";
  public static final String MEDIAS = "medias";
  public static final String UPLOAD_FILE_PATH_VILLAGE = MEDIAS + "/VisiteTablette.zip";
  public static final String UPLOAD_FILE_PATH_CHATEAU = MEDIAS + "/VisiteChateau.zip";
  public static final String DIR_FOR_DOWNLOADS = MEDIAS;
  public static final java.io.File UPLOAD_FILE_VILLAGE = new java.io.File(UPLOAD_FILE_PATH_VILLAGE);
  public static final java.io.File UPLOAD_FILE_CHATEAU = new java.io.File(UPLOAD_FILE_PATH_CHATEAU);
  /** Directory to store user credentials. */
  private static final java.io.File DATA_STORE_DIR = new java.io.File(
      System.getProperty("user.home"), ".store/drive_sample");

  /**
   * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
   * globally shared instance across your application.
   */
  private static FileDataStoreFactory dataStoreFactory;

  /** Global instance of the HTTP transport. */
  private static HttpTransport httpTransport;

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  /** Global Drive API client. */
  private static Drive drive;

  /** Authorizes the installed application to access user's protected data. */
  private static Credential authorize() throws Exception {
    // load client secrets
	FileInputStream fis = new FileInputStream("res/client_secrets.json");
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY,
            new InputStreamReader(fis));
	fis.close();
    if (clientSecrets.getDetails().getClientId().equals("") || clientSecrets.getDetails().getClientSecret().equals("")) {
      System.out.println("Enter Client ID and Secret from https://code.google.com/apis/console/?api=drive "
              + "into res/client_secrets.json");
      System.exit(1);
    }
    // set up authorization code flow
    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
            Collections.singleton(DriveScopes.DRIVE_FILE)).setDataStoreFactory(dataStoreFactory).build();
    // authorize
    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }

  public static boolean auth() {
    try {
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
      // authorization
      Credential credential = authorize();
      // set up the global Drive instance
      drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
      return true;
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return false;
  }
  
  public static void upload(String file, String fileName) {
    try {
      if(auth()) {
	      // run commands
	
    	  java.io.File f = new java.io.File(file);
    	  View.header1("Deleting old files from Drive");
    	  deleteFile(fileName);
    	  
	      View.header1("Starting Resumable Media Upload");
	      uploadFile(false, f);

	      View.header1("Success!");
      }
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
  
  public static void download(String fileName) {
    try {
      if(auth()) {
	      // run commands
    	  
	      View.header1("Starting Resumable Media Download");
	      File file = searchFileByFile(fileName);
	      if(file != null)
	    	  downloadFile(false, file);
	      else
	    	  View.header1("Error, file not found");

	      View.header1("Success!");
      }
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static void rename(String pattern, String name) {
    try {
      if(auth()) {
	      // run commands
	
	      View.header1("Updating Uploaded File Name");
	      ArrayList<String> file = searchFile(pattern);
	      if(!file.isEmpty())
	    	  updateFileWithTestSuffix(file.get(0), name);
	      else
	    	  View.header1("Error, file not found");
	      
	      View.header1("Success!");
      }
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
  
  /** Uploads a file using either resumable or direct media upload. */
  private static File uploadFile(boolean useDirectUpload, java.io.File uploadFile) throws IOException {
    File fileMetadata = new File();
    fileMetadata.setTitle(uploadFile.getName());

    FileContent mediaContent = new FileContent(APP_ZIP, uploadFile);

    Drive.Files.Insert insert = drive.files().insert(fileMetadata, mediaContent);
    MediaHttpUploader uploader = insert.getMediaHttpUploader();
    uploader.setDirectUploadEnabled(useDirectUpload);
    uploader.setProgressListener(new FileUploadProgressListener());
    return insert.execute();
  }
  
  /** Permanently delete file list, skipping the trash.  */
  private static void delete(ArrayList<String> fileId) {
	if(!fileId.isEmpty()) {
	    try {
	      for(String singleFileId : fileId) {
	    	  drive.files().delete(singleFileId).execute();
	      }
	    } catch (IOException e) {
	      System.out.println("An error occurred: " + e);
	    }
	}
  }

  /**
   * Search and delete file list matching the pattern.
   *
   * @param pattern Filename to match to delete.
   */
  private static void deleteFile(String pattern) {
	delete(searchFile(pattern));
  }


  /** Updates the name of the uploaded file. */
  private static File updateFileWithTestSuffix(String id, String name) throws IOException {
    File fileMetadata = new File();
    fileMetadata.setTitle(name);

    Drive.Files.Update update = drive.files().update(id, fileMetadata);
    return update.execute();
  }

  /** Downloads a file using either resumable or direct media download. */
  private static void downloadFile(boolean useDirectDownload, File file) throws IOException {
    // create parent directory (if necessary)
    java.io.File parentDir = new java.io.File(DIR_FOR_DOWNLOADS);
    if (!parentDir.exists() && !parentDir.mkdirs()) {
      throw new IOException("Unable to create parent directory");
    }
    OutputStream out = new FileOutputStream(new java.io.File(parentDir, file.getTitle()));

    MediaHttpDownloader downloader =
        new MediaHttpDownloader(httpTransport, drive.getRequestFactory().getInitializer());
    downloader.setDirectDownloadEnabled(useDirectDownload);
    downloader.setProgressListener(new FileDownloadProgressListener());
    downloader.download(new GenericUrl(file.getDownloadUrl()), out);
    out.close();
  }
  
  /** Search files by pattern : title, name, description ... */
  private static ArrayList<String> searchFile(String pattern) {
	  String pageToken = null;
	  ArrayList<String> match = new ArrayList<String>();
	  do {
	      FileList result;
		try {
			result = drive.files().list().setQ("fullText contains '" + pattern + "'").execute();
	      for(File file: result.getItems()) {
	          System.out.printf("Found file: %s (%s)\n", file.getTitle(), file.getId());
	          match.add(file.getId());
	      }
	      pageToken = result.getNextPageToken();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  } while (pageToken != null);
	  return match;
  }
  
  /** Search files by pattern : title, name, description ... */
  private static File searchFileByFile(String pattern) {
	  String pageToken = null;
	  do {
	      FileList result;
		try {
			result = drive.files().list().setQ("fullText contains '" + pattern + "'").execute();
			if(!result.getItems().isEmpty())
				return result.getItems().get(0);
	      pageToken = result.getNextPageToken();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  } while (pageToken != null);
	  return null;
  }
}
