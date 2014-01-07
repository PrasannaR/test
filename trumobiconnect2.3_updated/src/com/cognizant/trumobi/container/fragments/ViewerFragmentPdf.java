package com.cognizant.trumobi.container.fragments;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.container.Utils.UtilList;

import cx.hell.android.lib.pagesview.PagesView;
import cx.hell.android.lib.pdf.PDF;
import cx.hell.android.pdfview.Bookmark;
import cx.hell.android.pdfview.Options;
import cx.hell.android.pdfview.PDFPagesProvider;


@SuppressLint("NewApi")
public class ViewerFragmentPdf extends Fragment implements OnItemClickListener{
	
	ViewerListener mViewerListener ;
	static String filePath = "";
	private PDF pdf = null;
	private PagesView pagesView = null;
	private int box = 2;
	SharedPreferences options;
	private int colorMode = Options.COLOR_MODE_NORMAL;
	private PDFPagesProvider pdfPagesProvider = null;
	private boolean history = true;
	static Intent   localIntent ;
	

	static String filePathCompare = "";

	
	public static ViewerFragmentPdf newInstance(Bundle args){
	
		ViewerFragmentPdf viewFrament = new ViewerFragmentPdf();
		viewFrament.setArguments(args);
		localIntent = new Intent();
	
		return viewFrament;
		
		
				
	}
	
	public void setIntent(Intent minIntent){
		Log.d("ViewerFragmentPdf", "setIntent");
		localIntent = minIntent;

		Uri uri = localIntent.getData();
		filePathCompare = uri.getPath();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		//mViewerListener = (ViewerListener)activity;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getActivity().getApplicationContext();
		
				
	
	  //  localIntent.setDataAndType(Uri.fromFile(new File(this.getActivity().getFilesDir() + "/"+ "temp.pdf")), "application/pdf");
	
		Options.setOrientation(getActivity());
		options = PreferenceManager.getDefaultSharedPreferences(this
		.getActivity());
		this.box = Integer.parseInt(options.getString(Options.PREF_BOX, "2"));
        DisplayMetrics metrics = new DisplayMetrics();
        this.getActivity().getWindowManager().getDefaultDisplay()
		.getMetrics(metrics);
        
        
       
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.con_activity_mime_pdf, container,
				false);



		if (pdf != null) {
			pdf.finalize();
		}

		Uri uri = localIntent.getData();
		filePath = uri.getPath();
		Log.d("ViewerFragmentPdf",
				"onCreateView " + !(new File(filePath).length() == 0));
		try {
			if (filePath != null && !(new File(filePath).length() == 0)) {


				LinearLayout lnr = (LinearLayout) view
						.findViewById(R.id.viewertextView);
				TextView fileHeader = (TextView) view
						.findViewById(R.id.container_file_name);
				
				fileHeader.setTypeface(UtilList.getTextTypeFaceNormal(getActivity()));
				fileHeader.setText(UtilList.fileName);
				
				this.pagesView = new PagesView(this.getActivity());
				lnr.addView(pagesView);
				startPDF(options);

				try {
					if (!this.pdf.isValid()) {
						this.getActivity().finish();
					}

					this.pdfPagesProvider.setExtraCache(1024 * 1024 * Options
							.getIntFromString(options,
									Options.PREF_EXTRA_CACHE, 0));
					this.pdfPagesProvider.setOmitImages(options.getBoolean(
							Options.PREF_OMIT_IMAGES, false));
				} catch (Exception e) {

				}

			} else {

			}
		} catch (Exception e) {



		}

		return view;
	}

	private void startPDF(SharedPreferences options) {
		Log.d("ViewerFragmentPdf", "startPDF");
		this.pdf = this.getPDF();

		if (this.pdf != null) {
			if (!this.pdf.isValid()) {
				// Log.v(TAG, "Invalid PDF");
				if (this.pdf.isInvalidPassword()) {
					// Toast.makeText(this, "This file needs a password",
					// Toast.LENGTH_SHORT).show();
				} else {
					// Toast.makeText(this, "Invalid PDF file",
					// Toast.LENGTH_SHORT).show();
				}
				return;
			}
			this.colorMode = Options.getColorMode(options);
			this.pdfPagesProvider = new PDFPagesProvider(this.getActivity(),
					pdf, options.getBoolean(Options.PREF_OMIT_IMAGES, false),
					options.getBoolean(Options.PREF_RENDER_AHEAD, true));
			pagesView.setPagesProvider(pdfPagesProvider);
			Bookmark b = new Bookmark(this.getActivity()
					.getApplicationContext()).open();
			pagesView.setStartBookmark(b, filePath);
			b.close();
		}
	}

	/**
	 * Return PDF instance wrapping file referenced by Intent. Currently reads

	 * all bytes to memory, in future local files should be passed to native

	 * code and remote ones should be downloaded to local tmp dir.
	 * 
	 * @return PDF instance
	 */
	private PDF getPDF() {

		Log.d("ViewerFragmentPdf", "getPDF");


		Uri uri = localIntent.getData();
		filePath = uri.getPath();
		if (uri.getScheme().equals("file")) {
			Log.d("Open File Activity", "Uri scheme is file");
			if (history) {
				// Recent recent = new Recent(this.getActivity());
				// recent.add(0, filePath);
				// recent.commit();
			}
			return new PDF(new File(filePath), this.box);
		} else if (uri.getScheme().equals("content")) {
			Log.d("Open File Activity", "Uri scheme is content");
			ContentResolver cr = this.getActivity().getContentResolver();
			FileDescriptor fileDescriptor;
			try {
				fileDescriptor = cr.openFileDescriptor(uri, "r")
						.getFileDescriptor();
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e); // TODO: handle errors
			}
			return new PDF(fileDescriptor, this.box);
		} else {
			throw new RuntimeException("don't know how to get filename from "
					+ uri);

		}
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Log.d("ViewerFragmentPdf", "onItemClick");


	}

	public void fragclearCacheMemory() {
		Log.d("ViewerFragmentPdf", "fragclearCacheMemory");

		try {
			if (this.pdfPagesProvider != null) {
				this.pdfPagesProvider.clearCacheBitmap();
			}
		} catch (Exception e) {

		}
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
