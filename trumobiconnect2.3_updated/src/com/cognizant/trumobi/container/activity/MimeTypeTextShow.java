package com.cognizant.trumobi.container.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.container.Utils.OutlookPreference;
import com.cognizant.trumobi.container.Utils.UtilList;
import com.cognizant.trumobi.em.activity.EmMessageCompose;
import com.cognizant.trumobi.em.activity.EmMessageView;

public class MimeTypeTextShow extends TruMobiBaseActivity {

	WebView webview;
	String strCntent;
	String filePath;
	TextView fileName;
	String extension;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.i("NEW","==== onCreate "+UtilList.fileName);
		setContentView(R.layout.con_activity_mime);
		webview = (WebView) findViewById(R.id.viewertextView);
		
		fileName = (TextView) findViewById(R.id.con_mime_activity_file_name);
		
		filePath = OutlookPreference.getInstance(this).getValue("FilePath", "");
		extension = OutlookPreference.getInstance(this).getValue("Extension", ""); 
		if (!filePath.equalsIgnoreCase("")) {

			fileName.setText(UtilList.fileName);
			StringBuilder sb = new StringBuilder("<html><body>");

			if(extension.equalsIgnoreCase("html") ||extension.equalsIgnoreCase("htm"))
				webview.loadUrl("file:///"+filePath);
			else
			{
			sb.append(getLoadImage(filePath));
			sb.append("</body></html>");
			/*webview.loadData(sb.toString(), "text/html", "UTF-8");*/
			//try {
				//webview.loadData(URLEncoder.encode(sb.toString(),"utf-8").replaceAll("\\+"," "), "text/html", "utf-8");
				webview.loadDataWithBaseURL("file:///"+getFilesDir().getAbsolutePath()+"/",sb.toString(),"text/html", "UTF-8"
						,null);
			/*} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			}
			webview.getSettings().setBuiltInZoomControls(true);
			webview.getSettings().setDefaultZoom(ZoomDensity.FAR);
			webview.getSettings().setSupportZoom(true);
			webview.setWebViewClient(new CustomWebViewClient());
		} else {

			finish();

		}
		
	}
	
	private String getLoadImage(String filepath) {
		Log.i("NEW","==== getDataFromFileLoc "+UtilList.fileName);
		
		StringBuilder sb = new StringBuilder();
		String everything = null;
		
		//Bitmap BitmapOfMyImage=BitmapFactory.decodeFile(filepath);  

		sb.append("<img src=\""+ filepath + "\">");
		/*sb.append("<img src=\\\"file:///"+filepath+"\\\">");*/
		everything = sb.toString();
		return everything;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		try{
			Log.i("NEW","==== onConfigurationChanged "+UtilList.fileName);
			fileName.setText(UtilList.fileName);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private String getDataFromFileLoc(String filepath) {
		Log.i("NEW","==== getDataFromFileLoc "+UtilList.fileName);
		String line;
		StringBuilder sb;
		BufferedReader br = null;
		String everything = null;
		try {

			// file path to be modified
			br = new BufferedReader(new FileReader(filepath));
			sb = new StringBuilder();
			line = br.readLine();

			while (line != null) {
				sb.append("<p>");
				sb.append(line);
				sb.append("</p>");
				sb.append("\n");
				line = br.readLine();
			}
			everything = sb.toString();
		} catch (Exception e) {

		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Log.i("Text ", ": "+everything);
		return everything;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try{
			//UtilList.fileName ="";
			File file = new File(filePath);
			
			boolean deleted = file.delete();
			Log.i("NEW", "From mimetype destroy "+deleted);
			
		}catch(Exception e){
			Log.i("NEW", "From mimetype destroy "+e.toString());
		}
		
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent launchActivity = new Intent(this, AttachmentListActivity.class);
		launchActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Intent returnIntent = new Intent();

		returnIntent
				.setClassName("com.cognizant.seccontainerapp.activity",
						"com.cognizant.seccontainerapp.activity.AttachmentListActivity");
		Log.e("From Open ",
				"----------> "
						+ getIntent().getBooleanExtra("FromCheckActivity",
								false));

		if (getIntent().getBooleanExtra("FromCheckActivity", false)) {
			startActivity(launchActivity);
			finish();
		} else {
			finish();
		}
	}
	
	public void startEventAction(View viewID) {

		switch (viewID.getId()) {
		
		case R.id.con_mime_activity_file_icon:

		case R.id.con_mime_activity_btnback:
			finish();
			break;

		case R.id.con_mime_activity_btn_connect:

			Log.i("NEW", "FFFFFFFFFFFFFFFFF");
			Intent dataDeleted = new Intent();
			dataDeleted.putExtra("Broadcast",
					"Kill");
			dataDeleted
					.setAction("Restricted_App");
			sendBroadcast(dataDeleted);
			finish();
			break;
		}
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	private class CustomWebViewClient extends WebViewClient {
		/**
		 * This is intended to mirror the operation of the original (see
		 * android.webkit.CallbackProxy) with one addition of intent flags
		 * "FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET". This improves behavior when
		 * sublaunching other apps via embedded URI's.
		 * 
		 * We also use this hook to catch "mailto:" links and handle them
		 * locally.
		 */
		private void openWebApp(String binary_source_path) {
			if (!binary_source_path.startsWith("http://")
					&& !binary_source_path.startsWith("https://")) {
				binary_source_path = "http://" + binary_source_path;
			}

			Intent openWebAppIntent = new Intent("com.cognizant.trumobi.securebrowser");
	        if (openWebAppIntent != null) {
	               openWebAppIntent.putExtra("toBrowser",binary_source_path);
	               try {
	            	   startActivity(openWebAppIntent);
	            	   
	               }catch (ActivityNotFoundException ex) {
	               }
	        } 
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// hijack mailto: uri's and handle locally
			if (url != null && url.toLowerCase().startsWith("mailto:")) {
				return true;
			}

			// Handle most uri's via intent launch
			boolean result = false;
			try {
				openWebApp(url);
				result = true;
			} catch (ActivityNotFoundException ex) {
			}
			return result;
		}
	}
}
