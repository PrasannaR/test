/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognizant.trumobi;


import java.io.File;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;

import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;
import com.cognizant.trumobi.persona.utils.PersonaMDMCommandProcessingThread;
import com.cognizant.trumobi.persona.utils.PersonaMDMHashTable;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

    private static final String TAG = "PersonaGCMIntentService";
    PersonaMDMCommandProcessingThread mdmCommandProcessingThread;
	String LOG_TAG = GCMBaseIntentService.class.getSimpleName();
	GCMIntentService gcmIntentService;
	Context mGCMcontext;
    public GCMIntentService() {
        super(PersonaConstants.SENDER_ID);
        PersonaLog.d(LOG_TAG, "******** GCMIntent cns ");
		gcmIntentService = this;
		mdmCommandProcessingThread = new PersonaMDMCommandProcessingThread();
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        PersonaLog.d(TAG, "Device registered: regId = " + registrationId);
        PersonaCommonfunctions.displayMessage(context, getString(R.string.gcm_registered));
    //    ServerUtilities.register(context, registrationId);
        PersonaCommonfunctions.saveRegistrationId(getApplicationContext(),registrationId);
    }

   

	@Override
    protected void onUnregistered(Context context, String registrationId) {
        PersonaLog.i(TAG, "Device unregistered");
        PersonaCommonfunctions.displayMessage(context, getString(R.string.gcm_unregistered));
        if (GCMRegistrar.isRegisteredOnServer(context)) {
       //     ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            PersonaLog.i(TAG, "Ignoring unregister callback");
        }
        PersonaCommonfunctions.saveRegistrationId(getApplicationContext(),"GCMError");
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        PersonaLog.i(TAG, "Received message");
      //  String message = getString(R.string.gcm_message);
       // PersonaCommonfunctions.displayMessage(context, message);
        // notifies user
      
    	PersonaMDMCommandProcessingThread.getInstance(context);

		mGCMcontext = context;
		try {
			if (String.valueOf(mdmCommandProcessingThread.getState()).equals(
					"NEW"))
				mdmCommandProcessingThread.start();
		} catch (Exception e) {

		}
		synchronized (mdmCommandProcessingThread) {

			String text = intent.getStringExtra("message");

			//MDMPushCommand mdmPushCommand = new MDMPushCommand(context);
			String message = "", email_id = "";
			int certificateNumber;
			
			String uid;
			if (text != null) {
				PersonaLog.d(LOG_TAG, "" + text);
				String test = (text.replace("||", ","));
				String[] temp = test.split(",");

				if (temp[0] != null) {
					PersonaLog.d(LOG_TAG, "" + temp[0]);
					message = temp[0];
					//mdmPushCommand.setPushMessage(message);
					PersonaMDMHashTable.mHashMessage = message;
				}

				switch (temp.length) {

				case 3:

				{
					// Log.i(TAG, "has 3 segments...");

				}

					break;
				case 4: {

			/*		if (temp[2] != null
							&& (test.contains(PersonaApplicationConstants.REMOVE_PROFILE))) {

						PersonaMDMHashTable.profileName = temp[2];
						Log.e("profileName: ", temp[2]);

						if (temp[3] != null) {

							Log.e("profileName:uid ", temp[3]);
							uid = temp[3];
							PersonaMDMHashTable.mHashUid = uid;
							mdmCommandProcessingThread.notify();

						}

					}*/

			

					 if (temp[1] != null
							&& (test.contains(PersonaConstants.EMAIL_PUSH))) {
						certificateNumber = Integer.parseInt(temp[1]);
						PersonaMDMHashTable.mHashCertificateno = certificateNumber;

						if (temp[3] != null) {

							uid = temp[3];
							PersonaMDMHashTable.mHashUid = uid;

							android.accounts.Account[] accounts = AccountManager
									.get(mGCMcontext).getAccounts();

							if (temp[2] != null) {
								email_id = temp[2];
								PersonaMDMHashTable.mHashEmailId = email_id;

								String filename = email_id + "_"
										+ certificateNumber + ".pfx";
								File f = new File(filename);
								boolean account_present = false;

								for (android.accounts.Account account : accounts) {

									if (account.name.equalsIgnoreCase(email_id)) {
										account_present = true;
									}
								}

								if (!account_present & !f.exists()) {
									mdmCommandProcessingThread.notify();
								}
							}
						}

					}
					break;
				}

				case 2: {
					if (temp[1] != null) {
						uid = temp[1];

						PersonaMDMHashTable.mHashUid = uid;

						mdmCommandProcessingThread.notify();
					}
					break;
				}

				case 1: {
					// For MDMPush
					PersonaMDMHashTable.hashTable.put("empty", message);

					PersonaMDMHashTable.mHashUid = "empty";

					mdmCommandProcessingThread.notify();
					break;
				}

				}
			}
		}
        
        
    }

   /* @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        PersonaCommonfunctions.displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }*/

    @Override
    public void onError(Context context, String errorId) {
        PersonaLog.i(TAG, "Received error: " + errorId);
        PersonaCommonfunctions.saveRegistrationId(getApplicationContext(),"GCMError");
        PersonaCommonfunctions.displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // PersonaLog message
        PersonaLog.i(TAG, "Received recoverable error: " + errorId);
      //  displayMessage(context, getString(R.string.gcm_recoverable_error,
               // errorId));
        PersonaCommonfunctions.saveRegistrationId(getApplicationContext(),"GCMError");
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
/*    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.pr_ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.pr_app_name);
        Intent notificationIntent = new Intent(context, PersonaMainActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }*/

}
