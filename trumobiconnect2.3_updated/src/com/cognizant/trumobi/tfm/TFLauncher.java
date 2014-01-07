package com.cognizant.trumobi.tfm;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

public class TFLauncher {

	private static final String TF_VIEWER_CATEGORY = "com.thinkfree.example.category.DEFAULT";

	protected Activity mActivity = null;

	protected Intent mIntent = null;

	/* package */TFLauncher(Activity activity, Intent intent) {
		mActivity = activity;
		mIntent = intent;
	}

	/* package */int launch() {
		Uri uri = mIntent.getData();
		mIntent.setDataAndType(uri, getMimeType(uri));

		// If you don't want to allow other apps can open the file,
		// declare unique 'category' under 'CalcViewerActivity' in
		// AndroidManifest.xml as defined in this example project.
		// When starting an activity, you need to add unique category to an
		// intent.
		// eg. com.thinkfree.example.category.DEFAULT
		mIntent.addCategory(TF_VIEWER_CATEGORY);
		try {
			mActivity.startActivityForResult(mIntent,
					TFDocLoader.VIEWER_CALLBACK);
		} catch (ActivityNotFoundException e) {
			return TFDocLoader.NOT_SUPPORTED_TYPE;
		}
		return TFDocLoader.SUCCESS;
	}

	private String getMimeType(Uri uri) {
		String filePath = uri.toString();
		int extIndex = filePath.lastIndexOf(".");
		String ext = filePath.substring(extIndex + 1);
		ext = ext.toLowerCase();
		return MimeTypeMapper.getMimeTypeFromExtension(ext);
	}

}
