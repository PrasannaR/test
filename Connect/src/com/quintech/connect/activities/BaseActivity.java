package com.quintech.connect.activities;
	 

import com.quintech.common.ILog.Type;
import com.quintech.connect.*;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
	 

public class BaseActivity extends Activity 
{
	private static String TAG = "BaseActivity";

	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		try
		{
			super.onCreate(savedInstanceState);
			
			// ensure log file is initialized
			Constants.getLog().initialize();
	
			// use custom title bar
	        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	        setContentView(R.layout.connect);
	        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title_bar);

			Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/VerizonApex-Medium.otf");
			TextView tv = (TextView) findViewById(R.id.TextViewHeader);
			
			// set custom font
			tv.setTypeface(tf);
			
			// set default text
			tv.setText(getResources().getStringArray(R.array.app_name)[Constants.getApplicationBuildType().getValue()].toUpperCase());
			
			
			// eliminate color banding
		    Window window = getWindow();
		    window.setFormat(PixelFormat.RGBA_8888);
		    window.getDecorView().getBackground().setDither(true);
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "onCreate", e);
		}
	}
}
	 
