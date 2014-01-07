
/**
 * 
 * KEYCODE			AUTHOR		PURPOSE
 * EAS PROTOCOL		371990		Fixed to support the EAS protocol version 12.1
 * USER_SETTINGS_INFO_CHG		367712		To get EmailAddress from Server
 */

package com.cognizant.trumobi.exchange;

import android.util.Log;

/**
 * Constants used throughout the EAS implementation are stored here.
 *
 */
public class EmEas {
    // For debugging
    public static boolean WAIT_DEBUG = false;   // DO NOT CHECK IN WITH THIS SET TO TRUE
    public static boolean DEBUG = true;         // DO NOT CHECK IN WITH THIS SET TO TRUE

    // The following two are for user logging (the second providing more detail)
    public static boolean USER_LOG = true;     // DO NOT CHECK IN WITH THIS SET TO TRUE
    public static boolean PARSER_LOG = false;   // DO NOT CHECK IN WITH THIS SET TO TRUE
    public static boolean FILE_LOG = false;     // DO NOT CHECK IN WITH THIS SET TO TRUE

    public static final int DEBUG_BIT = 1;
    public static final int DEBUG_EXCHANGE_BIT = 2;
    public static final int DEBUG_FILE_BIT = 4;

    public static final String VERSION = "0.3";
    public static final String ACCOUNT_MAILBOX_PREFIX = "__eas";

    // Define our default protocol version as 2.5 (Exchange 2003)
    public static final String SUPPORTED_PROTOCOL_EX2003 = "2.5";
    public static final double SUPPORTED_PROTOCOL_EX2003_DOUBLE = 2.5;
    public static final String SUPPORTED_PROTOCOL_EX2007 = "12.0";
    public static final double SUPPORTED_PROTOCOL_EX2007_DOUBLE = 12.0;
    public static final String SUPPORTED_PROTOCOL_EX2007_SP1 = "12.1"; // 371990 Support 12.1 EAS Protocol
	public static final double SUPPORTED_PROTOCOL_EX2007_SP1_DOUBLE = 12.1; // 371990 Support 12.1 EAS Protocol
	//USER_SETTINGS_INFO_CHG, -->
	public static final String SUPPORTED_PROTOCOL_EX2010 = "14.0";
	public static final double SUPPORTED_PROTOCOL_EX2010_DOUBLE = 14.0;
	public static final String SUPPORTED_PROTOCOL_EX2010_SP1 = "14.1";
	public static final double SUPPORTED_PROTOCOL_EX2010_SP1_DOUBLE = 14.1;
	//USER_SETTINGS_INFO_CHG, <--
    public static final String DEFAULT_PROTOCOL_VERSION = SUPPORTED_PROTOCOL_EX2003;
	public static final String EXCHANGE_ACCOUNT_MANAGER_TYPE = "com.cognizant.trumobi.exchange";
    // From EAS spec
    //                Mail Cal
    // 0 No filter    Yes  Yes
    // 1 1 day ago    Yes  No
    // 2 3 days ago   Yes  No
    // 3 1 week ago   Yes  No
    // 4 2 weeks ago  Yes  Yes
    // 5 1 month ago  Yes  Yes
    // 6 3 months ago No   Yes
    // 7 6 months ago No   Yes

    public static final String FILTER_ALL = "0";
    public static final String FILTER_1_DAY = "1";
    public static final String FILTER_3_DAYS = "2";
    public static final String FILTER_1_WEEK = "3";
    public static final String FILTER_2_WEEKS = "4";
    public static final String FILTER_1_MONTH = "5";
    public static final String FILTER_3_MONTHS = "6";
    public static final String FILTER_6_MONTHS = "7";
    public static final String BODY_PREFERENCE_TEXT = "1";
    public static final String BODY_PREFERENCE_HTML = "2";

    // For EAS 12, we use HTML, so we want a larger size than in EAS 2.5
    public static final String EAS12_TRUNCATION_SIZE = "200000";
    // For EAS 2.5, truncation is a code; the largest is "7", which is 100k
    public static final String EAS2_5_TRUNCATION_SIZE = "7";

    public static final int FOLDER_STATUS_OK = 1;
    public static final int FOLDER_STATUS_INVALID_KEY = 9;

    public static final int EXCHANGE_ERROR_NOTIFICATION = 0x10;

    public static void setUserDebug(int state) {
        // DEBUG takes precedence and is never true in a user build
        if (!DEBUG) {
            USER_LOG = (state & DEBUG_BIT) != 0;
            PARSER_LOG = (state & DEBUG_EXCHANGE_BIT) != 0;
            FILE_LOG = (state & DEBUG_FILE_BIT) != 0;
            if (FILE_LOG || PARSER_LOG) {
                USER_LOG = true;
            }
            Log.d("Eas Debug", "Logging: " + (USER_LOG ? "User " : "") +
                    (PARSER_LOG ? "Parser " : "") + (FILE_LOG ? "File" : ""));
        }
     }
}
