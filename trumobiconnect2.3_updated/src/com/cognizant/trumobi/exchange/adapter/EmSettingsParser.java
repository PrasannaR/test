package com.cognizant.trumobi.exchange.adapter;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

import com.cognizant.trumobi.exchange.EmEasSyncService;
import com.cognizant.trumobi.log.EmailLog;

/**
 * Parse the result of a Settings command.
 * 
 * We only send the Settings command in EAS 14.0 after sending a Provision
 * command for the first time. parse() returns true in the normal case; false if
 * access to the account is denied due to the actual settings (e.g. if a
 * particular device type isn't allowed by the server)
 */

/**
 * 
 * KEYCODE			    		AUTHOR		PURPOSE
 * USER_SETTINGS_INFO_CHG		367712		To get EmailAddress from Server
 */

public class EmSettingsParser extends EmParser {
	private final EmEasSyncService mService;

	public static String startDate = "", endDate = "", replyMessage = "";
	public static boolean oofEnabled = false, oofStatusOkay = false;
	private String userEmailAddress = null;	//USER_SETTINGS_INFO_CHG

	public EmSettingsParser(InputStream in, EmEasSyncService service)
			throws IOException {
		super(in);
		mService = service;
	}

	private static void setdefaultVelues() {
		oofEnabled = oofStatusOkay = false;
		startDate = endDate = replyMessage = "";
	}
	
	//USER_SETTINGS_INFO_CHG, -->
	public String getuserEmailAddress() {
		return userEmailAddress;
	}
	//USER_SETTINGS_INFO_CHG, <--
	
	@Override
	public boolean parse() throws IOException {
		boolean res = false;
		setdefaultVelues();
		if (nextTag(START_DOCUMENT) != EmTags.SETTINGS_SETTINGS) {
			throw new IOException();
		}
		while (nextTag(START_DOCUMENT) != END_DOCUMENT) {
			if (tag == EmTags.SETTINGS_STATUS) {
				int status = getValueInt();

				if (status == 1) {
					oofStatusOkay = res = true;
				} else {
					// Access denied = 3; others should never be seen
					oofStatusOkay = res = false;
				}
				
			} else if (tag == EmTags.SETTINGS_DEVICE_INFORMATION) {
				parseDeviceInformation();
			}
			//USER_SETTINGS_INFO_CHG, -->
			else if (tag == EmTags.SETTINGS_USER_INFORMATION) {
				res = parseUserInformation();
			}
			//USER_SETTINGS_INFO_CHG, <--
			else {
				if (tag == EmTags.SETTINGS_OOF) {
					EmailLog.e("parseparseparse",
							"parseOofSettingsparseOofSettingsparseOofSettings");
					parseOofSettings();
				} else
					skipTag();
			}
		}
		return res;
	}

	private void parseOofSettings() throws IOException {
		// TODO Auto-generated method stub
		EmailLog.e("parseOofSettings", "parseOofSettings");
		if (tag == EmTags.SETTINGS_OOF) {

			while (nextTag(EmTags.SETTINGS_OOF) != END) {

				if (tag == EmTags.SETTINGS_GET) {
					while (nextTag(EmTags.SETTINGS_GET) != END) {
						EmailLog.e("parseOofSettings", "getoofsettings");
						getoofsettings();
					}
				}
				if (tag == EmTags.SETTINGS_STATUS) {

					if (getValueInt() == 1)
						oofStatusOkay = true;
					else
						oofStatusOkay = false;
				}

			}

		} else {
			skipTag();
		}
	}

	private void getoofsettings() throws IOException {
		EmailLog.e("getoofsettings", "getoofsettings "
				+ EmTags.SETTINGS_START_TIME + " tag " + tag);
		// TODO Auto-generated method stub
		switch (tag) {
		case EmTags.SETTINGS_OOF_STATE:
			int value = getValueInt();

			if (value == 1 || value == 2)
				oofEnabled = true;
			else
				oofEnabled = false;

			// EmailOofSettingsActivity.enableSettings(getValueInt());
			break;
		case EmTags.SETTINGS_START_TIME:
			EmailLog.e("getoofsettings", "SETTINGS_START_TIME");
			startDate = getValue();
			EmailLog.e("startDate", "startDate " + startDate);
			break;
		case EmTags.SETTINGS_END_TIME:
			EmailLog.e("getoofsettings", "SETTINGS_END_TIME");
			endDate = getValue();
			EmailLog.e("endDate", "endDate " + endDate);
			break;
		case EmTags.SETTINGS_OOF_MESSAGE:
			while (nextTag(EmTags.SETTINGS_OOF_MESSAGE) != END) {
				switch (tag) {
				case EmTags.SETTINGS_APPLIES_TO_INTERNAL:
					EmailLog.e("SETTINGS_APPLIES_TO_INTERNAL",
							"SETTINGS_APPLIES_TO_INTERNAL " + getValueInt());
					break;
				case EmTags.SETTINGS_ENABLED:
					EmailLog.e("SETTINGS_ENABLED", "SETTINGS_ENABLED "
							+ getValueInt());
					break;
				case EmTags.SETTINGS_REPLY_MESSAGE:
					EmailLog.e("getoofsettings", "SETTINGS_REPLY_MESSAGE");
					replyMessage = getValue();
					EmailLog.e("replyMessage", "replyMessage " + replyMessage);
					break;
				}
			}
			break;
		default:
			skipTag();
		}
	}

	public void parseDeviceInformation() throws IOException {
		while (nextTag(EmTags.SETTINGS_DEVICE_INFORMATION) != END) {
			if (tag == EmTags.SETTINGS_SET) {
				parseSet();
			} else {
				skipTag();
			}
		}
	}
	
	//USER_SETTINGS_INFO_CHG, -->
	public boolean parseUserInformation() throws IOException {
		boolean retVal = false;
		while (nextTag(EmTags.SETTINGS_USER_INFORMATION) != END) {
			if (tag == EmTags.SETTINGS_GET) {
				EmailLog.d("EmailSettingsParser", "===== SETTINGS_GET ====");
				retVal = parsereuInfo();
			} else {
				skipTag();
			}
		}
		return retVal;
	}
	
	public boolean parsereuInfo() throws IOException {
		EmailLog.d("EmailSettingsParser","==== In parseuInfo =====");
		boolean retVal = false;
		
		while (nextTag(EmTags.SETTINGS_GET) != END) {
			if (tag == EmTags.SETTINGS_EMAIL_ADDRESS) {
				EmailLog.d("EmailSettingsParser", "===== SETTINGS_EMAIL_ADDRESS ====");
				retVal = parseuseremail();
			} else if (tag == EmTags.SETTINGS_USER_ACCOUNTS) {
				EmailLog.d("EmailSettingsParser", "===== SETTINGS_USER_ACCOUNTS ====");
				while (nextTag(EmTags.SETTINGS_USER_ACCOUNTS) != END) {
					if (tag == EmTags.SETTINGS_USER_ACCOUNT) {
						EmailLog.d("EmailSettingsParser", "===== SETTINGS_USER_ACCOUNT ====");
						while (nextTag(EmTags.SETTINGS_USER_ACCOUNT) != END) {
							
							if (tag == EmTags.SETTINGS_EMAIL_ADDRESS) {
								EmailLog.d("EmailSettingsParser", "===== SETTINGS_EMAIL_ADDRESS ====");
								retVal = parseuseremail();
							} else {
								skipTag();
							}
						}
					} else {
						skipTag();
					}
				}
			} else {
				skipTag();
			}
		}
		return retVal;
	}
	
	public boolean parseuseremail() throws IOException {
		boolean retVal = false;
		while (nextTag(EmTags.SETTINGS_EMAIL_ADDRESS) != END) {
			if (tag == EmTags.SETTINGS_SMTP_ADDRESS
					|| tag == EmTags.SETTINGS_PRIMARY_SMTP_ADDRESS) {
				userEmailAddress = getValue();
				if(userEmailAddress != null)
					retVal = true;
			} else {
				skipTag();
			}
		}
		return retVal;
	}
	//USER_SETTINGS_INFO_CHG, <--
	public void parseSet() throws IOException {
		while (nextTag(EmTags.SETTINGS_SET) != END) {
			if (tag == EmTags.SETTINGS_STATUS) {
				mService.userLog("Set status = ", getValueInt());
			} else {
				skipTag();
			}
		}
	}
}
