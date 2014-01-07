package com.cognizant.trumobi.container.Utils;

public class Constants
{
	// Splash
	public static final long SLEEP_TIME = 3;
	public static final long WAIT_TO_SAVE_DRAFT = 30;

	// OutlookPreference
	public static final String PREF_APP_SHARED_PREFS = "SecurityContainer";
	public static final String PREF_EMAIL_ADDR = "EmailAddr";
	public static final String PREF_EMAIL_PASSWORD = "EmailPassword";
	public static final String PREF_DOMAIN = "Domain";
	public static final String PREF_SETTINGS_PASSWORD = "SettingsPassword";
	public static final String PREF_SERVER_LINK = "ServerLink";
	public static final String PREF_APPLICATION_STATE = "Launched";
	
	public static int TOTAL_MAIL_COUNT;
    
    public static int REQ_TYPE = 0;

	// Login
	public static final String EMAIL_ADDR = "EmailAddr";
	public static final String USER_ID = "UserID";
	public static final String PASSWORD = "Password";

	// Message
	public static final String MSG_PATTERN_NOT_MATCHED = "Pattern doesn't match";
	public static final String MSG_VALID_CREDENTIAL = "Please enter valid Credentials";
	public static final String MSG_EMPTY_CERTIFICATE = "Certificate Not Found";
	public static final String MSG_CHECK_CERTIFICATE = "Please check certificate";

	//New Mail
	public static final String MSG_VALID_ADDRESS = "Please enter valid address";
	public static final String MSG_SEND_WITH_NO_SUBJECT = "Send without Subject?";
	public static final String MSG_SEND_WITH_NO_BODY = "Send without BODY?";
	public static final String MSG_MAIL_SENT = "Mail Sent Successfully";
	public static final String MSG_MAIL_NOT_SENT = "Mail not Sent";
	public static final String MSG_MAIL_DRAFT_SAVE = "Saving Draft";
	public static final String MSG_MAIL_DRAFT_SAVED = "Draft Saved";
	public static final String MSG_MARKED_IMPORTANT = "Marked as Important";
	public static final String MSG_UNMARKED_IMPORTANT = "Unmarked Importance";

	// Setting
	public static final String FILE_NOT_FOUND = "No Certificates available in SDcard";


	// Dialog
	public static final String CHOOSE_CERTIFICATE = "Choose Certificate";
	public static final String LOADING_EMAIL = "Loading Emails....";
	public static final String SYNC_MAIL = "Syncing Mails";
	// Error Message In Toast
	public static final String EXCGANGE_SERVER_ERROR = "Please enter values";
	public static final String VALID_CREDENTIAL = "Please enter valid Credentials";
	public static final String ERR_RESPONSE_FAILED = "Error in Response try again  ";

	// Patter Matcher
	public static final String EMAIL_ADDRESS_FORMATT = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
	public static final String SERVER_ADDRESS_FORMATT = "^http\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(//\\S*)?$";
	public static final String DOMAIN_FORMATT = "/";
	
	
	//String Tags For Parsing MailBox
	public static final String ITEM = "itemid";
	public static final String PARENT_FOLDER = "parentfolderid";
	public static final String SUBJECT = "subject";
	public static final String DATE_TIME_RECEIVED = "datetimereceived";
	public static final String SIZE = "size";
	public static final String DATE_TIME_SENT = "datetimesent";
	public static final String DATE_TIME_CREATED = "datetimecreated";
	public static final String DISPLAY_CC = "displaycc";
	public static final String DISPLAY_TO = "displayto";
	public static final String HAS_ATTACHMENTS = "HasAttachments";
	public static final String NAME = "name";
	public static final String EMAIL_ADDRESS = "emailaddress";
	public static final String IS_READ = "isread";
	public static final String IMPORTANCE = "importance";
	
	//String Tags For Parsing MailBoxCount
	public static final String FOLDER = "folderid";
	public static final String TOTAL_COUNT = "totalcount";
	public static final String UNREAD_COUNT = "unreadcount";
	
	// Network state
    public static final String WIFI = "WIFI";
    public static final String CHECK_NETWORK_CONNECTION = "Kindly plese check for your network connection";
    public static final String CONNECT_TO_WIFI = "Connect To wifi network for quick response";

  //Common Function
    public static final String KEYSTORE_FILES = "/keystorefiles/";
    
    // Sync mail
    public static final String LOGIN_SUCCESS = "loginSuccess";
    public static final String EMPTY_SUBJECT = "NO SUBJECT";
    public static int REQUEST_TYPE ;
    public static int SYNC_TYPE ;
    public static final String DELETE_MSG = "Are you sure,you want to delete?";
    
    //HttpStatusCode
    public static int statusCode;
    
    //Device Type
    public static final String DEVICE_TYPE="Android";
    
    //JSON Respose Type
    public static final String postContentType="Content-type";
	public static final String postContentTypeValue="application/json";
	public static final String postHeader="Accept";
	public static final String responseMessage="ErrorMessage";
	public static final String IsValidRequest="IsValidRequest";
	public static final String UserId="UserId";
	
	//CertificateVerificationURL
	public static final String certificateURL="https://trubox.cognizant.com/ValidationService/ValidationService.svc/ValidateCertificate";
    
   
}
