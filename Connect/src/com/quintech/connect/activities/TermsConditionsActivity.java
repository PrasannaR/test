package com.quintech.connect.activities;
	 

import com.quintech.common.AbstractConstants.BuildType;
import com.quintech.common.ILog.Type;
import com.quintech.connect.*;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;
	 

public class TermsConditionsActivity extends BaseActivity 
{
	private static String TAG = "TermsConditionsActivity";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		try
		{
			super.onCreate(savedInstanceState);

	        setContentView(R.layout.terms_and_conditions);
	        
	        
	        // set header text
			TextView tvHeader = (TextView) findViewById(R.id.TextViewHeader);
			tvHeader.setText(getResources().getStringArray(R.array.terms_and_conditions)[Constants.getApplicationBuildType().getValue()].toUpperCase());
			
			
	        
	        final WebView webView = (WebView) findViewById(R.id.WebView1);
	        
	        
	        // show Verizon Wireless terms
	        if (Constants.getApplicationBuildType() == BuildType.VERIZON_WIRELESS)
	        	webView.loadData(getResources().getStringArray(R.array.terms_and_conditions_html)[Constants.getApplicationBuildType().getValue()], "text/html", "UTF-8");
	        
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "onCreate", e);
		}
	}
}
	 
