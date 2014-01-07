package com.cognizant.trumobi.container.fragments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.container.Utils.OutlookPreference;
import com.cognizant.trumobi.container.Utils.UtilList;

@SuppressLint("NewApi")
public class ViewerFragment extends Fragment implements OnItemClickListener {

	ViewerListener mViewerListener;
	static String filePath = "";

	static String filePathCompare = "";

	public static ViewerFragment newInstance(Bundle args) {
		filePathCompare = args.getString("FilePath");
		ViewerFragment viewFrament = new ViewerFragment();
		viewFrament.setArguments(args);
		return viewFrament;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.con_activity_tab_mime, container,
				false);

		TextView fileHeader = (TextView) view
				.findViewById(R.id.container_file_name);
		
		fileHeader.setTypeface(UtilList.getTextTypeFaceNormal(getActivity()));
		fileHeader.setText(UtilList.fileName);
		
		WebView webview = (WebView) view.findViewById(R.id.viewertextView);

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append(getDataFromFileLoc(OutlookPreference.getInstance(
				getActivity()).getValue("FilePath", "")));
		sb.append("</body></html>");
		//System.out.println("WEBWIEW DATA-------------------->" + sb.toString());
		/*webview.loadData(sb.toString(), "text/html", "UTF-8");*/
		try {
			webview.loadData(URLEncoder.encode(sb.toString(),"utf-8").replaceAll("\\+"," "), "text/html", "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		webview.getSettings().setBuiltInZoomControls(true);
		webview.getSettings().setDefaultZoom(ZoomDensity.FAR);
		webview.getSettings().setSupportZoom(true);
		webview.getSettings().setDisplayZoomControls(true);
		webview.getSettings().setDisplayZoomControls(true);

		return view;
	}

	private String getDataFromFileLoc(String filepath) {
		filePath = filepath;
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
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Log.i("Text ", ": " + everything);
		return everything;
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("ViewerFragmentPdf", "onDestroy");
		try {
			Log.i("ViewerFragmentPdf", "From openfile frag destroy " + filePath
					+ "   " + filePathCompare);

			if (!(filePath.equalsIgnoreCase(filePathCompare))) {
				File file = new File(filePath);

				boolean deleted = file.delete();
				Log.i("ViewerFragmentPdf", "From openfile frag destroy "
						+ deleted);
			}

		} catch (Exception e) {

			Log.i("ViewerFragmentPdf",
					"From openfile frag destroy " + e.toString());

		}
	}

}
