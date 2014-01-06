package com.quintech.connect.activities;

import com.quintech.common.AbstractData.Flags;
import com.quintech.common.AbstractData.Settings;
import com.quintech.common.Credentials.PromptType;
import com.quintech.common.ILog.Type;
import com.quintech.connect.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;



public class CredentialPromptActivity extends BaseActivity 
{
	private static String TAG = "CredentialPromptActivity";
	public static String ACTION_ALERT_ENROLLMENT_POLICY_FAILURE = "com.quintech.connect.ACTION_ALERT_ENROLLMENT_POLICY_FAILURE";
	private static AlertDialog alertDialogEnrollmentPolicyFailure = null;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		 try
		 {
			 super.onCreate(savedInstanceState);
			 
			 setContentView(R.layout.networkcredentials);
			 
			 
			 // set header text
			 String headerText = getResources().getStringArray(R.array.credential_prompt_title)[Constants.getApplicationBuildType().getValue()].toUpperCase();
				 
			 if (Constants.getCredentials().promptTitle != null && !Constants.getCredentials().promptTitle.equals(""))
				headerText = Constants.getCredentials().promptTitle.toUpperCase();
			 
			 TextView tv = (TextView) findViewById(R.id.TextViewHeader);
			 tv.setText(headerText);
		
			 
			 final TextView tvUserID = (TextView) findViewById(R.id.UserID);
			 final TextView tvUserIDFormat = (TextView) findViewById(R.id.UserIDFormat);
			 final TextView tvUserIDFormatDescription = (TextView) findViewById(R.id.UserIDFormatDescription);
			 
			 final EditText txtUserIDText = (EditText) findViewById(R.id.txtUserID);
			 final TextView tvPasswordText = (TextView) findViewById(R.id.Password);
			 final EditText txtPasswordText = (EditText) findViewById(R.id.txtPassword);
			 final TextView tvUserDecoText = (TextView) findViewById(R.id.UserIDFormat);
			 final TextView txtUserDecoText = (TextView) findViewById(R.id.txtUserIDFormat);
			 final TextView tvUserDecoTextDescription = (TextView) findViewById(R.id.UserIDFormatDescription);
			 final CheckBox chkPasswordShow = (CheckBox) findViewById(R.id.chkShowPassword);
			 final Button buttonSave = (Button) findViewById(R.id.ButtonSave);
			 final Button buttonCancel = (Button) findViewById(R.id.ButtonCancel);
			 final Button buttonRegisterUrl = (Button) findViewById(R.id.ButtonRegisterUrl);
			 
			  
			 // set text labels
			 tvUserID.setText(getResources().getStringArray(R.array.enter_userid)[Constants.getApplicationBuildType().getValue()]);
			 tvUserIDFormat.setText(getResources().getStringArray(R.array.enter_userid_decoration_label)[Constants.getApplicationBuildType().getValue()]);
			 tvUserIDFormatDescription.setText(getResources().getStringArray(R.array.enter_userid_decoration_description)[Constants.getApplicationBuildType().getValue()]);
			 tvPasswordText.setText(getResources().getStringArray(R.array.enter_password)[Constants.getApplicationBuildType().getValue()]);
			 chkPasswordShow.setText(getResources().getStringArray(R.array.show_password)[Constants.getApplicationBuildType().getValue()]);
			 buttonSave.setText(getResources().getStringArray(R.array.save)[Constants.getApplicationBuildType().getValue()]);
			 buttonCancel.setText(getResources().getStringArray(R.array.cancel)[Constants.getApplicationBuildType().getValue()]);
			 buttonRegisterUrl.setText(getResources().getStringArray(R.array.register_new_account)[Constants.getApplicationBuildType().getValue()]);
			 
			 
			 // set labels -- do not set password values
			 if (Constants.getCredentials().promptType == PromptType.RovaPortalCredentials)
				 txtUserIDText.setText(Constants.getData().getSetting(Settings.SET_RovaPortalUserID));

			 else
				 txtUserIDText.setText(Constants.getData().getSetting(Settings.SET_DefaultWisprUserID));
				 
			 // set event
			 CheckBox chkShowPassword = (CheckBox)findViewById(R.id.chkShowPassword);
			 chkShowPassword.setOnCheckedChangeListener(new OnCheckedChangeListener()
			 {
				 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				 {
					try
					{
				        if (isChecked)
				        {				        					        	
				        	txtPasswordText.setTransformationMethod(null); 				        	
				        }
				        else
				        {				        	
				        	txtPasswordText.setTransformationMethod(new PasswordTransformationMethod()); 
				        }
					}
					catch (Exception e)
	           		{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
	           		}
				 }
			 });
				 	
				
			 // set form element visibility
			 if (Constants.getCredentials().promptType == PromptType.NetworkCredentials ||
					 Constants.getCredentials().promptType == PromptType.RovaPortalCredentials)
			 {
				tvPasswordText.setVisibility(View.VISIBLE);
				txtPasswordText.setVisibility(View.VISIBLE);
				tvUserID.setVisibility(View.VISIBLE);
				txtUserIDText.setVisibility(View.VISIBLE);
				tvUserDecoTextDescription.setVisibility(View.GONE);
				txtUserDecoText.setVisibility(View.GONE);
				tvUserDecoText.setVisibility(View.GONE);
				chkPasswordShow.setVisibility(View.VISIBLE);
				buttonRegisterUrl.setVisibility(View.GONE);
				
				// only show register URL for ROVA portal cred prompt
				if (Constants.getCredentials().promptType == PromptType.RovaPortalCredentials)
					buttonRegisterUrl.setVisibility(View.VISIBLE);
			 }
			 else if (Constants.getCredentials().promptType == PromptType.NetworkKey)
			 {
				tvPasswordText.setVisibility(View.VISIBLE);
				txtPasswordText.setVisibility(View.VISIBLE);
				tvUserID.setVisibility(View.GONE);
				txtUserIDText.setVisibility(View.GONE);
				tvUserDecoTextDescription.setVisibility(View.GONE);
				txtUserDecoText.setVisibility(View.GONE);
				tvUserDecoText.setVisibility(View.GONE);
				chkPasswordShow.setVisibility(View.VISIBLE);
				buttonRegisterUrl.setVisibility(View.GONE);
			 }
			 else if (Constants.getCredentials().promptType == PromptType.NetworkUsernameDecoration)
			 {
				tvPasswordText.setVisibility(View.GONE);
				txtPasswordText.setVisibility(View.GONE);
				tvUserID.setVisibility(View.GONE);
				txtUserIDText.setVisibility(View.GONE);
				tvUserDecoTextDescription.setVisibility(View.VISIBLE);
				txtUserDecoText.setVisibility(View.VISIBLE);
				tvUserDecoText.setVisibility(View.VISIBLE);
				chkPasswordShow.setVisibility(View.GONE);
				buttonRegisterUrl.setVisibility(View.GONE);
			 }
			 else
			 {
				// fire callback
				if (Constants.getCredentials().runnableCanceled != null)
					Constants.getCredentials().runnableCanceled.run();
				
				return;
			 }
			 
			 
			 // set button click events
			 buttonSave.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) 
				{
					try
					{
						Constants.getCredentials().userID = txtUserIDText.getText().toString();
						Constants.getCredentials().password = txtPasswordText.getText().toString();     
						Constants.getCredentials().decoration = txtUserDecoText.getText().toString().toUpperCase();
						
						Constants.getLog().add(TAG, Type.Info, "User saved credential from prompt.");
	   
						// save entered credentials (will be cleared if connection fails)
						if (Constants.getCredentials().promptType == PromptType.RovaPortalCredentials)
						{
							Constants.getData().setSetting(Settings.SET_RovaPortalUserID, Constants.getCredentials().userID, false);
							Constants.getData().setSetting(Settings.SET_RovaPortalPassword, Constants.getCredentials().password, false);
							
							// attempt configuration update with new credentials
							Intent intent = new Intent(Constants.applicationContext, TabHostActivity.class);
					    	intent.setAction(TabHostActivity.ACTION_UPDATE_CONFIGURATION);	
					    	startActivity(intent);
						}
						else
						{
							Constants.getData().setSetting(Settings.SET_DefaultWisprUserID, Constants.getCredentials().userID, false);
							Constants.getData().setSetting(Settings.SET_DefaultWisprPassword, Constants.getCredentials().password, false);
							Constants.getData().setSetting(Settings.SET_DefaultUserIdDecoration, Constants.getCredentials().decoration, false);
						}
						
						// fire callback
						if (Constants.getCredentials().runnableCompleted != null)
							Constants.getCredentials().runnableCompleted.run();
						
						// close activity
						finish();
					}
					catch (Exception e)
		           	{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
		           	}
				}
			 });
			 
			 
			 buttonCancel.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) 
				{
					try
					{
						Constants.getLog().add(TAG, Type.Info, "User canceled credential prompt.");
						
						// fire callback
						if (Constants.getCredentials().runnableCanceled != null)
							Constants.getCredentials().runnableCanceled.run();
						
						// close activity
						finish();
					}
					catch (Exception e)
		           	{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
		           	}
				}
			 });

			 
			 buttonRegisterUrl.setOnClickListener(new OnClickListener() 
			 {
				@Override
				public void onClick(View v) 
				{
					try
					{
						// launch registration url
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.getData().getSetting(Settings.SET_RovaPortalRegistrationUrl)));
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Constants.applicationContext.startActivity(intent);
					}
					catch (Exception e)
		           	{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
		           	}
				}
			});
		 }
		 
		 catch (Exception e)
		 {
			 Constants.getLog().add(TAG, Type.Error, "onCreate", e);
		 }
    }
	
	
	@Override
    protected void onNewIntent(Intent intent) 
    {
    	try
    	{
	    	super.onNewIntent(intent);
	    	
	    	
	    	// check for intent
	    	if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(ACTION_ALERT_ENROLLMENT_POLICY_FAILURE))
	    	{
	    		// clear notification
	    		Notifications.remove(Notifications.Type.ENROLLMENT_POLICY_FAILURE);
	    		
	    		// only show alert if policy check failed
	    		if (!Constants.getData().getFlag(Flags.FLG_EnrollmentPolicyResult))
	    		{
	    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    		builder.setMessage(Constants.getData().getSetting(Settings.SET_EnrollmentPolicyMessage));
		    	   	builder.setCancelable(false);
		    	   	builder.setNegativeButton("Exit Application", new DialogInterface.OnClickListener() 
					{           
			    	   public void onClick(DialogInterface dialog, int id) 
			    	   {       
			    		   	try
			    		   	{
				    		   // bring user back to home screen
				    		   Intent startMain = new Intent(Intent.ACTION_MAIN);
				    		   startMain.addCategory(Intent.CATEGORY_HOME);
				    		   startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				    		   startActivity(startMain);
			    		   	}
			    		   	catch (Exception e)
			           		{
				       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
				           	}
			    	   }
				 	});
		    	   	builder.setPositiveButton("Close Message", new DialogInterface.OnClickListener() 
					{           
			    	   public void onClick(DialogInterface dialog, int id) 
			    	   {       
		    		   		// do nothing
			    	   }
				 	});
		    	   	
		    	   	// close dialog if it is showing
		    	   	if (alertDialogEnrollmentPolicyFailure != null)
		    	   		alertDialogEnrollmentPolicyFailure.dismiss();
		    	    
		    	   	// create new dialog
		    	   	alertDialogEnrollmentPolicyFailure = builder.create();
		    	   	alertDialogEnrollmentPolicyFailure.setTitle(Constants.applicationContext.getResources().getStringArray(R.array.enrollment_policy_failure_title)[Constants.getApplicationBuildType().getValue()]);
		    	   	alertDialogEnrollmentPolicyFailure.show();	
	    		}
	    		
	    		return;
	    	}
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onNewIntent", e);
    	}
    }
}
