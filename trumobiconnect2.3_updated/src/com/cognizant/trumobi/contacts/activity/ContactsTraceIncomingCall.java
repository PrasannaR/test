package com.cognizant.trumobi.contacts.activity;

import com.cognizant.trubox.contacts.db.ContactsConsts;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


public class ContactsTraceIncomingCall extends BroadcastReceiver {
	Context pcontext;
	String result = "";
	final String servicefunction = "EmailCALL";
	static String incomingNumber = "";


	private int ringerMode=0;
	AudioManager maudio;
	public Uri recieveUri;

	MediaPlayer mMediaPlayer;
	static final String ACTION = "android.intent.action.PHONE_STATE";
	public void onReceive(Context context, Intent intent) {
		pcontext = context;

		try {
		
			mMediaPlayer = new MediaPlayer();
			TelephonyManager tmgr = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			MyPhoneStateListener PhoneListener = new MyPhoneStateListener();			
			tmgr.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE);	
			
		} catch (Exception e) {
			Log.e("Phone Receive Error", " " + e);
		}

	}

	private class MyPhoneStateListener extends PhoneStateListener {
		public void onCallStateChanged(int state, String incomingNumber) {
			// Log.d("MyPhoneListener",state+"   incoming no:"+incomingNumber);

			if (state == 1) {
				String msg = "New Phone Call Event. Incomming Number : "+incomingNumber;
				/*int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(pcontext, msg, duration);
				toast.show();*/
				
				
				checkdb(incomingNumber);
				
				if(incomingNumber.equals("+919789502974"))
				{	
				}else
				{
				}

			}
			
			if (state == 0) 
			{			
				mMediaPlayer.stop();
			}
					
		}
	}


public void checkdb(String incomingNumber)
{
	
	//Original query
	final String[] selectionArgs = { "%" + incomingNumber + "%" };

	Cursor selectedValueCursor = pcontext.getContentResolver().query(
			ContactsConsts.CONTENT_URI_CONTACTS, null,
			ContactsConsts.CONTACT_PHONE + " LIKE ?", selectionArgs, null);
	
	//Toast.makeText(pcontext, "Checking..phone number avail in Contacts app??", Toast.LENGTH_SHORT).show();
	
	if(selectedValueCursor!=null)
	{
	
	if(selectedValueCursor.moveToFirst())
	{
		
		//Toast.makeText(pcontext, "AVAILABLE", Toast.LENGTH_SHORT).show();		
		//System.out.println("Incoming number" + incomingNumber+"is available in contacts db");		
		String IncomingNumberRingtonePath= selectedValueCursor.getString(selectedValueCursor
				.getColumnIndex(ContactsConsts.CONTACT_RINGTONE_PATH));		
		//System.out.println("*****Path of the incoming*****" + IncomingNumberRingtonePath);		
		customringtone(IncomingNumberRingtonePath);
		
	}else
	{
		//Toast.makeText(pcontext, "NOT available", Toast.LENGTH_SHORT).show();
	}
	selectedValueCursor.close();
	}
	
}
	
	
	public void customringtone(String IncomingNumberRingtonePath)
	{
		try {
			//Toast.makeText(pcontext, "I am inside custom ringtone", Toast.LENGTH_SHORT).show();
			
			
			Uri ConvertedToUri;
			
			
			
			if (mMediaPlayer.isPlaying())
			 {
				mMediaPlayer.stop();
			ConvertedToUri = Uri.parse(IncomingNumberRingtonePath);
			mMediaPlayer.setDataSource(pcontext, ConvertedToUri);
			final AudioManager audioManager = (AudioManager) pcontext.getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) {
			 mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			 mMediaPlayer.setLooping(true);
			 mMediaPlayer.prepare();
			 mMediaPlayer.start();
			}
	
			 }
			
			} catch(Exception e) {
			}   
	}
	

}