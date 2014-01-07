package com.cognizant.trumobi.tfm;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class TFDocLoader {
    
    public static final int VIEWER_CALLBACK = 10001;
    
    public static final int SUCCESS = 0;
    public static final int NOT_SUPPORTED_TYPE = -1;
    public static final int NOT_FOUND = -2;
    
    private Activity mActivity = null;
    
    private TFLauncher mViewer = null;
    
    private String mFilepath = null;
    
    private TFDocLoader(Activity activity) {
        this.mActivity = activity;
    }
    
    public static TFDocLoader createInstance(Activity activity) {
        return new TFDocLoader(activity);
    }
    
    public void setDocProperties(String filePath) {
        this.mFilepath = filePath;
    }

    public int launchViewer() {
        String filePath = mFilepath;
        
        Uri uri = getUriFor(filePath);
        if (uri == null) {
            return NOT_FOUND;
        }
        
        Intent intent = getIntentFor(uri);
        
        mViewer = new TFLauncher(this.mActivity, intent);
        if (mViewer == null) {
            return NOT_SUPPORTED_TYPE;
        }
        return mViewer.launch();
    }
    
    private Uri getUriFor(String filePath) {
        Uri result = null;
        
        File file = new File(filePath);
        if (!file.exists()) {
            return result;
        }
        
        result = Uri.fromFile(file);
        
        return result;
    }
    
    private Intent getIntentFor(Uri uri) {
        return new Intent(Intent.ACTION_VIEW, uri);
    }
    
}
