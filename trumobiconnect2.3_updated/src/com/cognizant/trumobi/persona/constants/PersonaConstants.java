package com.cognizant.trumobi.persona.constants;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import android.os.Environment;

import com.cognizant.trumobi.persona.net.PersonaAllAppsListDetails;

public class PersonaConstants {


	public static final String LIVE = "";
	public static final String STAGING = "https://trumobiuat.cognizant.com/";
	public static final String ACKNOWLEDGEMENT_PUSH_NOTIFICATION = STAGING
			+ "AndroidMDM.svc/AcknowledgePushNotification";
	public static final String IS_DEVICE_REGISTERED = STAGING
			+ "MIMAccess.svc/IsDeviceRegistered";
	public static final String DOWNLOAD_CERTIFICATE_PERSONA = STAGING
			+"UserService.svc/GetUserCertificateWithDeviceUID";
	public static final String AUTHENTICATION_SERVICE_PERSONA=STAGING
			+"AuthenticationService.svc/RegDeviceWithPersona";
	public static final String FORGET_PIN_SERVICE=STAGING
			+"AuthenticationService.svc/AppStoreAuthenticate";
	public static final String ROVA_TRUMOBI_LOGIN_SERVICE=STAGING
			+ "AuthenticationService.svc/AppStoreWebAuthenticate";

	// WEBSERVICE SPECIFIC

	public static final int RESPONSEOK = 200;
	public static final int RESPONSEOK2 = 201;
	public static final int RESPONSERROR = 1;
	public static final int RESPONSEAUTHFAILURE = 401;
	public static final int RESPONSE500 = 500;
	public static final int RESULTOK1 = 2111;
	public static final int RESULTOK2 = 2112;
	public static final int RESULTFAIL1 = 2113;
	public static final int RESULTFAIL2 = 2114;
	public static final int CONNECTION_TIMEOUT=0;
	public static final int SERVICE_UNAVAILABLE_ERRORCODE = 1106;
	public static final int INVALIDLOGINCREDENTIALS = 1007;
	public static final int SERVERNOTREACHABLE = 9999;

	public static final String charSetName= "iso-8859-1";
	

	//Authentication
	
	public static final String appstore_user_id="appstore_user_id";
	public static final String appstore_password="appstore_password";
	public static final String  DEVICE_NOT_ENROLLED_ERRORCODE = "1124";
	
	
	//Policy Download
	public static final String postContentType="Content-type";
	public static final String postContentTypeValue="application/json";
	public static final String postHeader="Accept";
	public static final String charFormat="iso-8859-1";
	public static final String jsonMessageStatus="message_status";
	public static final String jsonMessageStatusSuccess="Success";
	public static final String jsonMessageStatusFailure="Failure";
	public static final String policyFile="TruBox_Default_Profile";
	public static final String bufferError="Buffer error";
	public static final String bufferErrorDesc="Error converting Result";
	
	public static final String entityCharset="UTF-8";
	public static final String responseStatusCode = "responseStatusCode";

	public static final String responseMessage ="Response";
	public static final String isAuthRequired = "isADAuthRequired";
	public static final String result="result";
	public static final String ownerid = "ownerid";
	
	public static final String displayMessage="display_message";
	public static final String appstoreErrorCode="appstore_error_code";
	public static final String messageStatus="message_status";
	
	
	public static final String ERROR_DESC="ErrorDesc";

	public static final String appstoreSessionId="appstore_session_id";

	public static final String appstoreInformation="appstore_information";
	
	public static final String SENDER_ID="513559811502";
	public static final String PERSONAPREFERENCESFILE="PersonaPreferences";
	public static final String GCM_REGISTRATION_ID="GcmRegistrationId";
	
	
	public static final String mandatoryAppsList = "mandatoryappslist.txt";
	public static final String GCMError = "GCMError";
	public static final String emptyString=" ";
	public static final String appsInstallOnresume = "installAppsOnResume";
	
	
	
	public static LinkedList<PersonaAllAppsListDetails> appsDetails;
	public static ArrayList<String> packageName=new ArrayList<String>();
	public static ArrayList<String> appURL=new ArrayList<String>();
	public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory() + "/ConnectDownloads/";
	public static final String EMAIL_PUSH = "EmailPush";
	public static File cacheDir;
	public static final String CERTIFICATE_REQUEST_ID = "certificate_request_id";
	public static final String APPSTORE_USER_USERIMAGEBYTE = "user_image_byte";
	public static final String APPSTORE_ICON_BINARY = "app_icon_binary";
	public static final String USER_EMAIL = "userEmail";
	public static final String EMAIL_SETUP_INITIATED="Email Setup Initiated";
	public static final int mDMExecuted = 1;
	public static final String MSG_SUCCESS = "Success";
	public static final String COMMAND_UID = "command_uid";
	public static final String COMMENTS = "comments";
	public static final String DEVICE_ID = "device_id";
	public static final String PUSH_COMMAND_STATUS = "push_command_status";
	public static final String REGISTRATION_ID = "registration_id";
	
	public static final String EMAIL_PACKAGE="com.cognizant.email";
	public static final String EMAIL_PACKAGE_DEV="com.cognizant.email.dev";
	

	public static final String CONTACTS_PACKAGE="com.cognizant.trumobi.contacts";
	
	public static final String CALENDAR_PACKAGE="com.sample.calendar";


	public static final String FILE_VIEWER_PACKAGE="com.cognizant.seccontainerapp.activity";
	
	public static final String SECURE_BROWSER_PACKAGE="com.cognizant.trumobi.securebrowser";
	
	public static final String CONNECT_PACKAGE="com.enterprise.connect";
	
	public static final String STR_TABLET = "Tablet";
	public static final String STR_PHONE = "Phone";
	
	
	/**
	 * Constants used for ROVA-SDK implementation
	 */
	
	public static final int AUTH_TYPE_NUMERIC = 1;
	public static final int AUTH_TYPE_ALPHABETS = 2;
	public static final int AUTH_TYPE_ALPHANUMERIC = 3;
	public static final int PIN_BASED_AUTH = 5;
	public static final int mAuthTypeNone=0;
	public static final int mAuthTypeBasic=1;
	public static final int mAuthTypeCertificate=2;
	
	public static final int NUMBER_OF_TRIALS_ALLOWED = 5;
	public static final int NUMBER_OF_DAYS_TO_EXPIRE = 10;
	public static final int LOCAL_CREDENTIALS_LENGTH = 4;
	public static final int LOG_OUT_PERIOD = 60;
	
	public static final int SIMCARD_REMOVAL_EVENT = 1;
	public static final int DEVICE_ROOTED_EVENT = 2;
	public static final int IME_NOT_WHITELISTED = 3;
	public static final int INCORRECT_LOGIN_ATTEMPTS = 4;
	public static final int DEVICE_DEBUGGER_ON = 5;
	public static final int PIN_RESET_SUCCESS = 6;
	public static final int SHOW_PIN_EXPIRY = 7;
	public static final int SHOW_LAUNCHER_SCREEN = 8;
	public static final int APPLICATION_HACKED = 9;

	public static final int DO_NOTHING = 0;
	public static final int WARN_USER = 1;
	public static final int BLOCK_DEVICE = 2;
	public static final int WIPE_APP = 3;
	
	public static final boolean PASSPHRASE_AUTH = false;
	public static final boolean PIN_AUTH = true;
	
	public static enum PACKAGE_NAME {
		EMAIL_PACKAGE,
		EMAIL_PACKAGE_DEV,
		CONTACTS_PACKAGE,
		CALENDAR_PACKAGE,
		FILE_VIEWER_PACKAGE,
		SECURE_BROWSER_PACKAGE}
	
	/*public static final String mExchangeServer="asyncmail.cognizant.com";
	public static final String mExchangeDomain="emcslab.com";*/

	public static final String mExchangeServer="trubox.cognizant.com";
	public static final String mExchangeDomain="truboxmdmdev.com";
	
}