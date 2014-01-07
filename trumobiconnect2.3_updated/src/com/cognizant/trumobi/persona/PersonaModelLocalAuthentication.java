package com.cognizant.trumobi.persona;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.PersonaPimSettingsDbHelper;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;

public class PersonaModelLocalAuthentication {
	
	
	private static String TAG = PersonaModelLocalAuthentication.class.getName();
	private Context mContext;
	PersonaCommonfunctions mCommonfunctions;
	String  mPassword, mDomain, mServer;
	
	public PersonaModelLocalAuthentication(Context context){
		
		this.mContext = context;
		mCommonfunctions = new PersonaCommonfunctions(context);
	}
	
	
	private long getTodayMidnightTime() {
		PersonaLog.d(TAG,">>> getTodayMidnightTime >>>>");
		Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		mCalendar.set(mCalendar.get(Calendar.YEAR),mCalendar.get(Calendar.MONTH),mCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		Date mTodayDateMidnight = mCalendar.getTime();
		PersonaLog.d(TAG,"<<< getTodayMidnightTime <<<<<");
		return mTodayDateMidnight.getTime();
	}
	
	
	public boolean isNotOlderPassword(Bundle bundle) {
		
		PersonaLog.d(TAG,">>> isNotOlderPassword >>>>");
		String[] projection,  whereArgs;
		String where , sortOrder;
		ArrayList<String> history = new ArrayList<String>();
		String password = bundle.getString("pwd");
		String pwdToBeReplaced = bundle.getString("presentPwdToBeReplaced");
		String mUsername = new SharedPreferences(mContext).getString(
				"trumobi_username", "");
		int numOfHistoryRestricted = 0;
		
		projection = new String[] { PersonaPimSettingsDbHelper.PWD_CHANGE_HISTORY };
		where = PersonaPimSettingsDbHelper.USERNAME_PWDSETTINGS+ " =?";
		whereArgs = new String[] { mUsername};
		Cursor cursor = mContext.getContentResolver().query(PersonaPimSettingsProvider.TABLE_PWDSETTINGS_URI,
				projection, where, whereArgs, null);
		if(cursor.getCount() > 0) {
			cursor.moveToFirst();
			numOfHistoryRestricted = cursor.getInt(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PWD_CHANGE_HISTORY));
			//Since current password validation is done sdk, history should be history -1
			numOfHistoryRestricted = numOfHistoryRestricted - 1;
		}
		cursor.close();
		 projection = new String [] { PersonaPimSettingsDbHelper.PASSWORD };
		 sortOrder = PersonaPimSettingsDbHelper.PASSWORD_CREATED_DATE + " DESC" ;
		 where = PersonaPimSettingsDbHelper.USERNAME_PWD_HISTORY+ " =?";
		 cursor = mContext.getContentResolver().query(PersonaPimSettingsProvider.TABLE_PWD_HISTORY_URI,
					projection, where, whereArgs, sortOrder);
		 if (cursor.moveToFirst()) {
				
				for(int i = 0;i < numOfHistoryRestricted ; i++) {
					if(!cursor.isAfterLast()) {
					history.add(cursor.getString(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PASSWORD)));
					cursor.moveToNext();
					}
					else {
						break;
					}
				}
			}
		 cursor.close();
		 PersonaLog.d(TAG, "------- history ----- " + history);
		 if(history.contains(password)) {
				return false;
		 }
		 saveUpdatedPwdResetTime(mUsername); //Removed Context - in this new change
			// Current pwd should not be saved in history.. old password should be saved
		 updatePasswordHistoryTable(pwdToBeReplaced);
		 PersonaLog.d(TAG,"<<< isNotOlderPassword <<<<<");
		return true;
	
	}

	
	/**
	 *	save password created date: 	
	 * 
	 *  Method to save the credentials.
	 *  
	 */
	//Removed Context - in this new change
	private void saveUpdatedPwdResetTime(String username) {
		PersonaLog.d(TAG,">>> saveCredential >>>>");
		String selection = PersonaPimSettingsDbHelper.USERNAME_PWDSETTINGS + " =?";
		String[] selectionArgs = { username };
		ContentValues values = new ContentValues();
		values.put(PersonaPimSettingsDbHelper.CREATED_DATE, String.valueOf(getTodayMidnightTime()));
		
		mContext.getContentResolver().update(PersonaPimSettingsProvider.TABLE_PWDSETTINGS_URI,
				values, selection, selectionArgs);
		
		PersonaLog.d(TAG,"<<< saveCredential <<<<<");
	}
	
	
	/**
	 *	saveUserDetails: 	
	 * 
	 *  Method to save user details.
	 *  
	 */

	public void saveUserDetails(Bundle bundle) {
		PersonaLog.d(TAG,">>> saveUserDetails >>>>");
		TruBoxDatabase.initialiseTruBoxDatabase(mContext);
		String mUsername = bundle.getString("username");
		String whereArgs[] = {mUsername };
		String where = PersonaPimSettingsDbHelper.USER_NAME + " = ? ";
		ContentValues values = new ContentValues();
		values.put(PersonaPimSettingsDbHelper.USER_NAME, mUsername);
		Cursor cursor = mContext.getContentResolver().query(PersonaPimSettingsProvider.TABLE_USERDETAILS_URI,
				null, where, whereArgs, null);
		if(cursor.getCount() == 0) {
			mContext.getContentResolver().insert(PersonaPimSettingsProvider.TABLE_USERDETAILS_URI, values);
		}
		
		cursor.close();
		// To verify values are updated. Can be removed after freeze
		
		cursor = mContext.getContentResolver().query(PersonaPimSettingsProvider.TABLE_USERDETAILS_URI,
				null, where, whereArgs, null);
		if (cursor.moveToFirst()) {
			PersonaLog.d(
					TAG,
					"After inserting values in USERDETAILS table  "
							+ cursor.getString(cursor.getColumnIndex(PersonaPimSettingsDbHelper.USER_NAME)) + "  ");
		}
		cursor.close();
		
		// To verify values are updated. Can be removed after freeze.. ends
		
			
			
			
	}


	/**
	 * saveInRoutineCheckDB
	 * 	
	 * Save the routine check in DB.
	 * 
	 */
	public void saveInRoutineCheckDB(Bundle bndle) {
		PersonaLog.d(TAG,">>> saveInRoutineCheckDB >>>>");
		int numOfTrialsAllowed  = bndle.getInt("numoftrialsallowed", 1);
		int mExpiryInDays = bndle.getInt("expiryInDays",PersonaConstants.NUMBER_OF_DAYS_TO_EXPIRE);
		String authType = bndle.getString("authorizationtype"); // Default value for auth type is Basic authentication which is 2.
		String[] projection = { PersonaExternalDBHelper.COL0 };
		String where = PersonaExternalDBHelper.COL0 + " = ?";
		String[] whereArgs = { "1" };
		ContentValues values = new ContentValues();
		values.put(PersonaExternalDBHelper.COL1, "");
		values.put(PersonaExternalDBHelper.COL2, "");
		values.put(PersonaExternalDBHelper.COL3, authType);
		values.put(PersonaExternalDBHelper.COL4, numOfTrialsAllowed);
		values.put(PersonaExternalDBHelper.COL5, PersonaConstants.PIN_SAVED);
		values.put(PersonaExternalDBHelper.COL6, mExpiryInDays);
		
		Cursor cursor = mContext.getContentResolver().query(
				PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI,
				projection, where, whereArgs, null);

		if (cursor.getCount() == 0) {
			ContentResolver cr;
			cr = mContext.getContentResolver();
			cr.insert(PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI,
					values);
		} else {

			mContext.getContentResolver().update(
					PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI,
					values, where, whereArgs);
		}
		cursor.close();
		PersonaLog.i(TAG,"----------getcount ------- " + cursor.getCount());
		
		// To verify values are updated. Can be removed after freeze
			Cursor c = mContext.getContentResolver().query(
					PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI,
					null, where, whereArgs, null);
			if(c.moveToFirst()) {
				while(!c.isAfterLast()) {
			PersonaLog.i(TAG, c.getString(c.getColumnIndex(PersonaExternalDBHelper.COL1)));
			PersonaLog.i(TAG, c.getString(c.getColumnIndex(PersonaExternalDBHelper.COL2)));
			PersonaLog.i(TAG, c.getString(c.getColumnIndex(PersonaExternalDBHelper.COL3)));
			PersonaLog.i(TAG, c.getInt(c.getColumnIndex(PersonaExternalDBHelper.COL4)) + "");
			PersonaLog.i(TAG, c.getString(c.getColumnIndex(PersonaExternalDBHelper.COL5)));
			PersonaLog.i(TAG, c.getString(c.getColumnIndex(PersonaExternalDBHelper.COL6)));
			c.moveToNext();
			}
			}
			c.close();
			// To verify values are updated. Can be removed after freeze ends
		
	}

		public void savePIMsettings(Bundle bndle)
		{
			PersonaLog.d(TAG,">>> savePIMsettings >>>>");
			int mLocalAuthtype = bndle.getInt("localauthtype");
			int mExpiryInDays = bndle.getInt("expiryindays");
			int mPwdLocktimeInMins = bndle.getInt("pwdlocktimeinmins");
			int mPwdLength=bndle.getInt("pwdlength");
		    int mNumOfTrialsAllowed = bndle.getInt("numoftrialsallowed");
		    int mChangeHistory = bndle.getInt("changehistory");
		    String mUsername = bndle.getString("username");
		    String where = PersonaPimSettingsDbHelper.USERNAME_PWDSETTINGS + " = ?" ;
		    String whereArgs[] = {mUsername};
		    ContentValues values = new ContentValues();
		    
		    values.put(PersonaPimSettingsDbHelper.USERNAME_PWDSETTINGS, mUsername);
		    values.put(PersonaPimSettingsDbHelper.PWD_TYPE,String.valueOf(mLocalAuthtype));
		    values.put(PersonaPimSettingsDbHelper.PWD_EXPIRY_DURATION,String.valueOf(mExpiryInDays));
		    values.put(PersonaPimSettingsDbHelper.PWD_LOCK_TIME,String.valueOf(mPwdLocktimeInMins));
		    values.put(PersonaPimSettingsDbHelper.PWD_LENGTH,String.valueOf(mPwdLength));
		    values.put(PersonaPimSettingsDbHelper.PWD_NUM_TRIAL,String.valueOf(mNumOfTrialsAllowed));
		    values.put(PersonaPimSettingsDbHelper.PWD_CHANGE_HISTORY,String.valueOf(mChangeHistory));
		    values.put(PersonaPimSettingsDbHelper.CREATED_DATE,String.valueOf(getTodayMidnightTime()));
		    values.put(PersonaPimSettingsDbHelper.UPDATED_AUTH_TYPE,String.valueOf(mLocalAuthtype));
		    values.put(PersonaPimSettingsDbHelper.UPDATED_AUTH_LENGTH,String.valueOf(mPwdLength));
		    
			Cursor cursor = mContext.getContentResolver().query(PersonaPimSettingsProvider.TABLE_PWDSETTINGS_URI,
					null, where, whereArgs, null);
			if(cursor.getCount() == 0) {
				mContext.getContentResolver().insert(PersonaPimSettingsProvider.TABLE_PWDSETTINGS_URI,values);
			}
			else {
				mContext.getContentResolver().update(PersonaPimSettingsProvider.TABLE_PWDSETTINGS_URI,values,where,whereArgs);
			}
			cursor.close();
			// To verify values are updated. Can be removed after freeze
			
			cursor = mContext.getContentResolver().query(PersonaPimSettingsProvider.TABLE_PWDSETTINGS_URI,
					null, where, whereArgs, null);
			if (cursor.moveToFirst()) {
				while (!cursor.isAfterLast()) {
					PersonaLog.d(
							TAG,
							"After inserting values in PWDSETTINGS table  "
									+ cursor.getString(cursor.getColumnIndex(PersonaPimSettingsDbHelper.USER_ID)) + " -userid  "
									+ cursor.getString(cursor.getColumnIndex(PersonaPimSettingsDbHelper.USERNAME_PWDSETTINGS)) + " -username  "
									+ cursor.getString(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PWD_TYPE)) + " -type  "
									+ cursor.getString(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PWD_EXPIRY_DURATION)) + " -duration "
									+ cursor.getString(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PWD_LOCK_TIME)) + " -locktime "
									+ cursor.getString(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PWD_NUM_TRIAL)) + " -numtrial "
									+ cursor.getString(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PWD_CHANGE_HISTORY)) + " -history "
									+ cursor.getString(cursor.getColumnIndex(PersonaPimSettingsDbHelper.CREATED_DATE)) + " -created date ");
					cursor.moveToNext();
				}
			}
			cursor.close();
			// To verify values are updated. Can be removed after freeze ends
			
			
			
		}

		public Bundle updatePIMSettings(Bundle bundle) {
			
		  	Bundle returnBundle = checkIfPwdSettingsChanged(bundle);
			return returnBundle;
		}
		
		private Bundle checkIfPwdSettingsChanged(Bundle bundle) {
			
			int currentPwdType = 0, currentPwdlength = 0;
			int currentExpiryDays = 0, currentNumberOfTrials = 0, currentChangeHistory = 0, currentPwdLockTime =0;
			int updatedPwdType = 0, updatedPwdLength = 0;
			int updatedExpiryDays = 0, updatedNumberOfTrials = 0, updatedChangeHistory = 0, updatedPwdLockTime =0;
			String where, username;
			String[] whereArgs;
			
			updatedPwdType = bundle.getInt("mLocalAuthtype");
			updatedPwdLength = bundle.getInt("mPwdLength");
			updatedExpiryDays = bundle.getInt("mExpiryInDays");
			updatedNumberOfTrials = bundle.getInt("mNumOfTrialsAllowed");
			updatedChangeHistory = bundle.getInt("mChangeHistory");
			updatedPwdLockTime = bundle.getInt("mPwdLocktimeInMins");
			username = bundle.getString("username");
			
			where =  PersonaPimSettingsDbHelper.USERNAME_PWDSETTINGS + " =?" ;
			whereArgs = new String[] {username};
			Cursor cursor = mContext.getContentResolver().query(PersonaPimSettingsProvider.TABLE_PWDSETTINGS_URI,
					null, where, whereArgs, null);
			PersonaLog.d(TAG, "---------- checkIfPwdSettingsChanged whereArgs------------" + whereArgs +  "-------- whereArgs length" 
					+ whereArgs.length  + "--------- whereArgs[0]" + whereArgs[0] +  "---------- cursor.getcount --------- " + cursor.getCount());
			if (cursor.moveToFirst()) {
				while(!cursor.isAfterLast()) {
					currentPwdType = cursor.getInt(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PWD_TYPE));
					currentPwdlength = cursor.getInt(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PWD_LENGTH));
					currentExpiryDays =  cursor.getInt(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PWD_EXPIRY_DURATION));
					currentNumberOfTrials = cursor.getInt(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PWD_NUM_TRIAL)); 
					currentChangeHistory = cursor.getInt(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PWD_CHANGE_HISTORY));
					currentPwdLockTime = cursor.getInt(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PWD_LOCK_TIME));
					String user = cursor.getString(cursor.getColumnIndex(PersonaPimSettingsDbHelper.USERNAME_PWDSETTINGS));
					 PersonaLog.d(TAG,  "------ inside if checkIfPwdSettingsChanged after reading current values from db --------- ");
					 PersonaLog.d(TAG, "-------- mLocalAuthtype -------" + currentPwdType);
					 PersonaLog.d(TAG, "-------- mNumOfTrialsAllowed -------" + currentNumberOfTrials);
					 PersonaLog.d(TAG,	"-------- mExpiryInDays -------" +currentExpiryDays);
					 PersonaLog.d(TAG,  "-------- mPwdLength -------"+ currentPwdlength);
					 PersonaLog.d(TAG,	"-------- mPwdLocktimeInMins -------" +currentPwdLockTime);
					 PersonaLog.d(TAG,	"-------- mChangeHistory -------" +currentChangeHistory);
					 PersonaLog.d(TAG,	"-------- user -------" + user);
					 cursor.moveToNext();
				}
				
			}
			
			cursor.close();
			 PersonaLog.d(TAG,  "------ checkIfPwdSettingsChanged after reading current values from db --------- ");
			 PersonaLog.d(TAG, "-------- mLocalAuthtype -------" + currentPwdType);
			 PersonaLog.d(TAG, "-------- mNumOfTrialsAllowed -------" + currentNumberOfTrials);
			 PersonaLog.d(TAG,	"-------- mExpiryInDays -------" +currentExpiryDays);
			 PersonaLog.d(TAG,  "-------- mPwdLength -------"+ currentPwdlength);
			 PersonaLog.d(TAG,	"-------- mPwdLocktimeInMins -------" +currentPwdLockTime);
			 PersonaLog.d(TAG,	"-------- mChangeHistory -------" +currentChangeHistory);
			 PersonaLog.d(TAG,  "------ checkIfPwdSettingsChanged updated value from rova sdk --------- ");
			 PersonaLog.d(TAG, "-------- mLocalAuthtype -------" + updatedPwdType);
			 PersonaLog.d(TAG, "-------- mNumOfTrialsAllowed -------" + updatedNumberOfTrials);
			 PersonaLog.d(TAG,	"-------- mExpiryInDays -------" +updatedExpiryDays);
			 PersonaLog.d(TAG,  "-------- mPwdLength -------"+ updatedPwdLength);
			 PersonaLog.d(TAG,	"-------- mPwdLocktimeInMins -------" +updatedPwdLockTime);
			 PersonaLog.d(TAG,	"-------- mChangeHistory -------" +updatedChangeHistory);
			 
			boolean passwordResetRequired = false;
			if((currentPwdType != updatedPwdType) || (currentPwdlength != updatedPwdLength)) {
				passwordResetRequired = true;
			}
			if((currentExpiryDays != updatedExpiryDays) || (currentNumberOfTrials != updatedNumberOfTrials) ||
					(currentChangeHistory != updatedChangeHistory) || (currentPwdLockTime != updatedPwdLockTime) || passwordResetRequired) {
				// even when one of the above setting is changed update all values at one shot. So that there will be no 
				// individual updates required
				saveUpdatedPIMSettings(bundle);
			}
			Bundle returnBundle = new Bundle();
			returnBundle.putInt("updatedPwdType",updatedPwdType);
			returnBundle.putInt("updatedPwdLength",updatedPwdLength);
			returnBundle.putBoolean("passwordResetRequired",passwordResetRequired);
			return returnBundle;
		}
		
		private void saveUpdatedPIMSettings(Bundle bundle) {
			
			int updatedExpiryDays = 0, updatedNumberOfTrials = 0, updatedChangeHistory = 0, updatedPwdLockTime =0;
			int updatedPwdType = 0, updatedPwdLength =0;
			String mUsername, where;
			String whereArgs[];
			
			updatedPwdType = bundle.getInt("mLocalAuthtype");
			updatedPwdLength = bundle.getInt("mPwdLength");
			updatedExpiryDays = bundle.getInt("mExpiryInDays");
			updatedNumberOfTrials = bundle.getInt("mNumOfTrialsAllowed");
			updatedChangeHistory = bundle.getInt("mChangeHistory");
			updatedPwdLockTime = bundle.getInt("mPwdLocktimeInMins");
			mUsername = bundle.getString("username");
			
			ContentValues values = new ContentValues();
			values.put(PersonaPimSettingsDbHelper.PWD_EXPIRY_DURATION, String.valueOf(updatedExpiryDays));
			values.put(PersonaPimSettingsDbHelper.PWD_NUM_TRIAL, String.valueOf(updatedNumberOfTrials));
			values.put(PersonaPimSettingsDbHelper.PWD_CHANGE_HISTORY,String.valueOf(updatedChangeHistory));
			values.put(PersonaPimSettingsDbHelper.PWD_LOCK_TIME,String.valueOf(updatedPwdLockTime));
			values.put(PersonaPimSettingsDbHelper.UPDATED_AUTH_TYPE, String.valueOf(updatedPwdType));
			values.put(PersonaPimSettingsDbHelper.UPDATED_AUTH_LENGTH, String.valueOf(updatedPwdLength));
			
			where = PersonaPimSettingsDbHelper.USERNAME_PWDSETTINGS + " =?";
			whereArgs = new String[] {mUsername};
			mContext.getContentResolver().update(PersonaPimSettingsProvider.TABLE_PWDSETTINGS_URI,
					values, where, whereArgs);
			
			// To verify values are updated. Can be removed after freeze
			
			Cursor cursor = mContext.getContentResolver().query(PersonaPimSettingsProvider.TABLE_PWDSETTINGS_URI,
					null, where, whereArgs,null);
			if (cursor.moveToFirst()) {
				while (!cursor.isAfterLast()) {
					int existingPwdType = cursor.getInt(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PWD_TYPE));
					int existingPwdLength = cursor.getInt(cursor.getColumnIndex(PersonaPimSettingsDbHelper.PWD_LENGTH));
					int mLocalAuthtype = cursor.getInt(cursor.getColumnIndex(PersonaPimSettingsDbHelper.UPDATED_AUTH_TYPE));
					int mPwdLength = cursor.getInt(cursor.getColumnIndex(PersonaPimSettingsDbHelper.UPDATED_AUTH_LENGTH));
					PersonaLog.d("PersonaLocalAuthentication",
							"After updating values in PWDSETTINGS table  "
									+ "Current auth type " + existingPwdType + "  "
									+ "Current auth length " + existingPwdLength + "  "
									+ "Updated auth type " + mLocalAuthtype + "  "
									+ "Updated auth length " + mPwdLength + "  "
									+ "For user " + cursor.getString(cursor.getColumnIndex(PersonaPimSettingsDbHelper.USERNAME_PWDSETTINGS)) + "  "
									);
				cursor.moveToNext();	
				
				}
			}
			cursor.close();
			// To verify values are updated. Can be removed after freeze ends
			
			updateOtherSettingsInExternalDB(bundle);
		
		}
		
		private void updateOtherSettingsInExternalDB(Bundle bundle) {
			int updatedExpiryDays = 0, updatedNumberOfTrials = 0;
			updatedExpiryDays = bundle.getInt("mExpiryInDays");
			String[] projection = { PersonaExternalDBHelper.COL0 };
			updatedNumberOfTrials = bundle.getInt("mNumOfTrialsAllowed");
			String where = PersonaExternalDBHelper.COL0 + " = ?";
			String[] selectionArgs = {"1"};
			ContentValues values = new ContentValues();
			values.put(PersonaExternalDBHelper.COL4, updatedNumberOfTrials);
			values.put(PersonaExternalDBHelper.COL6, updatedExpiryDays);
			Cursor cursor = mContext.getContentResolver().query(
					PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI,
					projection, where, selectionArgs, null);
			if (cursor.getCount() != 0) {
				mContext.getContentResolver().update(PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI,
						values, where, selectionArgs);
			} 
			if(cursor!=null)
			cursor.close();
		}


		//once user resets password after pim settings update, pim settingsdb should be updated
		public void updateCurrentPimSettings(Bundle bundle) {
			
			int mLocalAuthtype, mPwdLength;
			String mUsername,  where;
			mUsername = new SharedPreferences(mContext).getString("trumobi_username", "");
			where = PersonaPimSettingsDbHelper.USERNAME_PWDSETTINGS + " =? ";
			String[] whereArgs = { mUsername }; 
			
			mLocalAuthtype = bundle.getInt("localauthtype");
			mPwdLength=bundle.getInt("pwdlength");
		   
		    ContentValues values = new ContentValues();
		    values.put(PersonaPimSettingsDbHelper.PWD_TYPE, String.valueOf(mLocalAuthtype));
		    values.put(PersonaPimSettingsDbHelper.PWD_LENGTH, String.valueOf(mPwdLength)); 
		    
		    mContext.getContentResolver().update(PersonaPimSettingsProvider.TABLE_PWDSETTINGS_URI,
		    		values , where, whereArgs);
		    mCommonfunctions.saveCredentialsDetailsForExpiryScreen(mLocalAuthtype,mPwdLength);
			
		}
		
			
		public void updatePasswordHistoryTable(String password) {
		String username;
		long createdDate;
		username = new SharedPreferences(mContext).getString("trumobi_username", "");
		createdDate = System.currentTimeMillis();
		ContentValues values = new ContentValues();
		values.put(PersonaPimSettingsDbHelper.USERNAME_PWD_HISTORY, username);
		values.put(PersonaPimSettingsDbHelper.PASSWORD, password);
		values.put(PersonaPimSettingsDbHelper.PASSWORD_CREATED_DATE, String.valueOf(createdDate));
		 mContext.getContentResolver().insert(PersonaPimSettingsProvider.TABLE_PWD_HISTORY_URI, values);
		
		}



	
}
