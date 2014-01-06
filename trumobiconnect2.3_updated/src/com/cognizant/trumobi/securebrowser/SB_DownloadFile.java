package com.cognizant.trumobi.securebrowser;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import com.TruBoxSDK.TruboxFileEncryption;
import com.TruBoxSDK.TruboxFileEncryption.STORAGEMODE;
import com.cognizant.trumobi.container.Pojo.ChildMailbox;
import com.cognizant.trumobi.container.Utils.UpdateDB;

// File download
	public class SB_DownloadFile extends AsyncTask<String, Integer, String> {
		String PATH = "";
		String FILESIZE = "0";
		String CONTENTTYPE = "";
		String DOWNLOADEDFROM = "";
		String fName = "";
		// ///////////////////////////////////////////////////////////////////////////////////

		private Context mContext;
		private int NOTIFICATION_ID = 1;
		private Notification mNotification;
		private NotificationManager mNotificationManager;
		private Notification.Builder builder;
		private int incr;
		
		public SB_DownloadFile(Context appcontext){
			this.mContext = appcontext;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			boolean allowDownload = true;
			if ( allowDownload){
			Toast.makeText(mContext, "Starting download...",
					Toast.LENGTH_SHORT).show();
			}
			else{
			this.cancel(true);
			Toast.makeText(mContext, "File downloading is disabled by your organization.",
					Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected String doInBackground(String... sUrl) {
			int count;		
			File outputFile = null;
			InputStream input = null;
			HttpURLConnection connection = null;
			TruboxFileEncryption truboxFileEncryption = null;
			OutputStream output = null;
			try {
				URL downloadURL = new URL(sUrl[0]);
				
				// Added by 290767
				//Initialize a connection object to download URL
				connection = (HttpURLConnection) downloadURL.openConnection();
				
				//get cookies for the URL if any. Cookies must be sent along with request for mail attachements
				String cookies = CookieManager.getInstance().getCookie(downloadURL.toString());
  			    Log.d("Content Disposition", "All the cookies in a string:" + cookies);
				//set cookies for the http request
				connection.setRequestProperty("Cookie", cookies);
				
				//open a http connection
				connection.connect();
				
				// if connection successful start downloading, otherwise return
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                     return " as the linked file not found or moved";
				

                Log.d("Content Disposition", "http status:200 OK");
				
                //get content diposition value if any. It determines the filename of the attachment
				String contentDisposition = connection.getHeaderField("content-disposition");
				
				//determine the filename based on the content disposition if present otherwise use filename from url
				int filenamepos = -1;
				if ( contentDisposition != null ){
					filenamepos = contentDisposition.indexOf("filename") ;
				}
				
				if ( filenamepos != -1)
				{
					fName = contentDisposition.substring(filenamepos +1);
					filenamepos = fName.indexOf('=');
					SB_Log.sbD("Content Disposition","filenamepos:" + filenamepos + 1);
					fName = fName.substring(filenamepos + 1);
					//the filename might be url encoded
					try {
						fName = URLDecoder.decode(fName, "UTF-8");
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
					try {
						//accept only strings that has safe characters
					fName = fName.replaceAll("[^a-zA-Z0-9._()+= -]","");
					}
					catch (Exception e) {
						e.printStackTrace();
						}
				}
				SB_Log.sbD("Content Disposition","contentDisposition:"+contentDisposition +  "fileName:" + fName);
				if ( fName == "" || fName == null)
				{
					try
					{
					
					String[] path = downloadURL.getPath().split("/");
					fName = path[path.length - 1];
					}
					catch (Exception e) {
					e.printStackTrace();
					}
				}
				
				
				CONTENTTYPE = connection.getContentType();
				DOWNLOADEDFROM = connection.getURL().toString();
				// getting file length
				int lenghtOfFile = connection.getContentLength();
				// input stream to read file - with 8k buffer
				input = new BufferedInputStream(connection.getInputStream());
				// PATH =
				// File file = new File(PATH);
				File file = new File(mContext.getFilesDir().toString() + "/Downloads");
				if (!file.exists())
					file.mkdirs();

				String sb = "";
				java.util.Random sRandom = new java.util.Random();
				for (int i = 0; i < 24; i++) {
		            // We'll use a 5-bit range (0..31)
		            int value = sRandom.nextInt() & 31;
		            char c = "0123456789abcdefghijklmnopqrstuv".charAt(value);
		            sb = sb + c;
		        }
				SB_Log.sbD("Random:", sb);
				
				String rName = sb;
				
				outputFile = new File(file, rName);
				
				
				//TODO: replace code to get if the already exists
				// If the file to be downloaded already exists, rename the file
				/*for (int i = 1; i < 10000 && outputFile.exists(); i++) {

					int dotPosition = fName.lastIndexOf('.');
					if (dotPosition != -1) {
						outputFile = new File(file, fName.substring(0,
								dotPosition)
								+ "-"
								+ i
								+ fName.substring(dotPosition));
					} else {
						outputFile = new File(file, fName + "-" + i);
					}

				}*/
				PATH = outputFile.getAbsolutePath();
				//fName = outputFile.getName();

				Log.d("Content Disposition", "PATH" + PATH + "FNAME" + fName+"   "+rName);
				//outputFile = new File(file, fName);
				// Output stream to write file
				output = new FileOutputStream(outputFile);
				

				/*truboxFileEncryption = new TruboxFileEncryption(

						mContext, outputFile.getAbsolutePath(),
						STORAGEMODE.EXTERNAL);*/
				int len = 2*1024;

				byte data[] = new byte[len];
				long total = 0;
				while ((count = input.read(data)) != -1) {
					total += count;
					// publishing the progress....
					// After this onProgressUpdate will be called
					publishProgress((int) ((total * 100) / lenghtOfFile));
					//truboxFileEncryption.write(data);
					output.write(data, 0, count);
				}
				if ( total == 0 ) {
					return " for file:" + fName;
				}
				
				FILESIZE = String.valueOf(total);
				
				truboxFileEncryption = new TruboxFileEncryption(
						mContext, outputFile.getAbsolutePath(),
						STORAGEMODE.EXTERNAL);
				
				truboxFileEncryption.encryptFile();
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("Content Disposition", "exception" + e.getMessage());
				return " for file:" + fName;
			} finally {
				try {
                    if (truboxFileEncryption != null)
                    	truboxFileEncryption.close();
                    if (input != null)
                        input.close();
                    
                    
					output.flush();
                    output.close();
                } 
                catch (Exception ignored) { }

                if (connection != null)
                    connection.disconnect();
			}
			
			return "success";

		}

		/**
		 * Updating progress bar
		 * */

		@Override
		protected void onPostExecute(String result) {

			Log.i("chan", "result" + result);
			String status = "Download complete: " + fName;
			if ( result == "success" ) {
			
			List<ChildMailbox> data = new ArrayList<ChildMailbox>();
			ChildMailbox datum = new ChildMailbox();
			datum.setAttachmentName(fName);
			datum.setMIME_TYPE(CONTENTTYPE);
			Log.i("chan", "Size=== podt   " + FILESIZE + PATH + fName
					+ CONTENTTYPE + "download url" + DOWNLOADEDFROM);
			datum.setSize(FILESIZE);
			datum.setContent(PATH);
			datum.setDateTimeReceived(String.valueOf(System.currentTimeMillis()));
			datum.setEmailAddress(DOWNLOADEDFROM);
			data.add(datum);
			UpdateDB.storeInboxMessages(mContext, data);
			}
			else{
				status = "Download failed" + result;
			}

			
			Toast.makeText(mContext,
					status, Toast.LENGTH_SHORT).show();
			
		}
	}