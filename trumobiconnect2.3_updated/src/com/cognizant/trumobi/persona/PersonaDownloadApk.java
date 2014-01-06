package com.cognizant.trumobi.persona;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.net.PersonaGetJsonInterface;

public class PersonaDownloadApk extends AsyncTask<String, Integer, Integer> {

	String apkUrl, destinationFilePath, fileName;
	int https_status_code, downLoadStatus;
	Context mContext;
	ArrayList<String> appsList;
	String[] appName;
	PersonaGetJsonInterface callingActivity;
	int currentAppIndexForDownload, appsListSize;
	PersonaMyProgressDialog personaMyProgressDialog;

	public PersonaDownloadApk(String url, Context context, int currentAppIndex,
			int appsListSize, ArrayList<String> appsList,
			PersonaGetJsonInterface localAuthenticationActivity) {
		this.apkUrl = url;
		mContext = context;
		this.currentAppIndexForDownload = currentAppIndex;
		this.appsListSize = appsListSize;
		this.callingActivity = localAuthenticationActivity;
		this.appsList = appsList;

	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		/*
		 * personaMyProgressDialog=new PersonaMyProgressDialog(mContext);
		 * personaMyProgressDialog.setTitle
		 * (R.string.device_provisiong_message_one+"\n"+
		 * R.string.device_provisiong_message_two); personaMyProgressDialog.show();
		 */
	}

	@Override
	protected Integer doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		try {

			URL url = new URL(apkUrl);
			URLConnection connection = url.openConnection();
			connection.connect();

			fileName = apkUrl.substring(apkUrl.lastIndexOf('/') + 1,
					apkUrl.length());
			File downloadFilePath = new File(PersonaConstants.DOWNLOAD_PATH);
			if (!downloadFilePath.exists())
				downloadFilePath.mkdirs();
			destinationFilePath = PersonaConstants.DOWNLOAD_PATH + fileName;
			File apk = new File(destinationFilePath);
			if (!apk.exists()) {
				if (connection instanceof HttpURLConnection) {
					HttpURLConnection httpConnection = (HttpURLConnection) connection;

					https_status_code = httpConnection.getResponseCode();
					httpConnection.setConnectTimeout(10000);
					httpConnection.setReadTimeout(10000);
					httpConnection.getResponseMessage();

					PersonaLog.d("AppsDetailInfoScreen", "https_status_code: "
							+ https_status_code + " responsemssg : "
							+ httpConnection.getResponseMessage());

					// do something with code .....
				} else {
					System.err.println("error - not a http request!");
				}
				if (https_status_code == PersonaConstants.RESPONSEOK) {
					InputStream input = new BufferedInputStream(
							url.openStream());
					OutputStream output;
					output = new FileOutputStream(apk);
					PersonaLog.d("PersonaDownloadApk", "fileName is .... " + fileName);
					PersonaLog.d("DownloadApkpath", Environment
							.getExternalStorageDirectory().toString());

					byte data[] = new byte[1024 * 4];
				//	long total = 0;
					int count;
					while ((count = input.read(data)) != -1) {
						//total += count;
						// publishing the progress....
						// publishProgress((int) (total * 100 / fileLength));
						output.write(data, 0, count);
						// PersonaLog.d("AppsDetailInfoScreen", "COUNT VALUE :" +
						// count);
						// PersonaLog.d("AppsDetailInfoScreen", "TOTAL VALUE :" +
						// total);
					}

					downLoadStatus = R.string.msg_download_complete;
					output.flush();
					output.close();
					input.close();
					PersonaLog.e("PersonaDownloadApk", "Download of APK complete" + fileName);
				}

				else if (https_status_code == 500) {

					downLoadStatus = R.string.msg_download_error;
				}

			} else {
				PersonaLog.e("PersonaDownloadApk", "File already downloaded in SD card");
			}
			PersonaLog.e("personaDownloadApk", appsListSize + "appslistsize" + appsList
					+ "appsList");
	/*		if (appsListSize == -1) {
				PersonaLog.e("DownloadTask", "fileName" + fileName);
				appName = new String[1];
				appName[0] = fileName;
				PersonaLog.i("DownloadTask", "installing this app");
				callingActivity.onRemoteCallComplete(appName, 0);

			} else {
				if (currentAppIndexForDownload == (appsListSize - 1)) {
					callingActivity.onRemoteCallComplete(appsList, 0);

					PersonaLog.i("DownloadTask", "finishedAllAppsDownload");
				}
			}*/

		}

		catch (SocketTimeoutException e) {

			e.printStackTrace();
			return 901;

		} catch (ConnectTimeoutException e) {

			e.printStackTrace();
			return 901;

		}
		
		catch (Exception e) {
			e.printStackTrace();
			return 901;

		}
		return appsListSize;
	}

	@Override
	protected void onPostExecute(Integer result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		
		if (result == -1) {
			PersonaLog.e("DownloadTask", "fileName" + fileName);
			appName = new String[1];
			appName[0] = fileName;
			PersonaLog.i("DownloadTask", "installing this app");
			callingActivity.onRemoteCallComplete(appName, 0);

		} else {
			if (currentAppIndexForDownload == (appsListSize - 1)) {
				callingActivity.onRemoteCallComplete(appsList, 0);

				PersonaLog.i("DownloadTask", "finishedAllAppsDownload");
			}
		}
		
		
		switch (result) {
		case 901:
			PersonaLog.i("DownloadApkTask", "Download stopped. Connection failed");
			Toast.makeText(mContext, "Please connect to Internet",
					Toast.LENGTH_LONG).show();
			((Activity) mContext).finish();

		}

	}

	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		super.onCancelled();
		PersonaLog.i("PersonaDownloadApk Task", "Cancelled");
	}

}
