package com.cognizant.trumobi.externaladapter;

import com.quintech.rovacommon.PIMSettingsInfo.SecurityResolution;

/**
 *  FileName : ExternalPIMSettingsInfo
 * 
 *  Desc : 
 * 
 * 
 *  KeyCode 				Author				Date						Desc
 * 
 */

public class ExternalPIMSettingsInfo {

	
	public enum ExternalPIMAuthType {

        None(0),
        Certificate(1),
        Basic(2);

        private int mValue;

        private ExternalPIMAuthType(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }
        
        public static ExternalPIMAuthType fromInt(int i) {
            for (ExternalPIMAuthType s : values()) {
                if (s.getValue() == i) {
                    return s;
                }
            }
            return None;
        }
        
    }
	
	public enum ExternalSecurityResolution {

        Nothing(0),
        Warn(1),
        Block(2),
        Wipe(3);

        private int mValue;

        private ExternalSecurityResolution(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static ExternalSecurityResolution fromInt(int i) {
            for (ExternalSecurityResolution s : values()) {
                if (s.getValue() == i) {
                    return s;
                }
            }
            return Nothing;
        }
    }
	
	
	public enum ExternalPasswordType{
		
		   NotSet(0),
	       Numeric(1),
	       Alphabetic(2),
	       AlphaNumeric(3),
	       Symbolic(4);
		   
		   private int mValue;
		   
		   private ExternalPasswordType(int value) {
			   mValue = value;
		   }
		   
		   public int getValue(){
			   return mValue;
		   }
		   
		   public static ExternalPasswordType fromInt(int i){
			   
			   for(ExternalPasswordType s: values()) {
				   if(s.getValue() == i) {
					   return s;
				   }
			   }
			   return NotSet;
		   }

	}
	
	
	public enum ExternalPasswordExpiration 
	{
       NotSet(0),
       Week(7),
       Month(30),
       ThreeMonths(90),
       SixMonths(180),
       Year(365);

       private int mValue;

       private ExternalPasswordExpiration(int value) {
           mValue = value;
       }

       public int getValue() {
           return mValue;
       }

       public static ExternalPasswordExpiration fromInt(int i) {
           for (ExternalPasswordExpiration s : values()) {
               if (s.getValue() == i) {
                   return s;
               }
           }
           return NotSet;
       }
	}
   
	public enum ExternalPasswordLockTime 
	{
       NotSet(0),
       Fifteen(15),
       Thirty(30),
       Minute(60),
       TwoMinutes(180),
       FiveMinutes(300),
       TenMinutes(600),
       QuarterHour(900),
       HalfHour(1800),
       Hour(3600);

       private int mValue;

       private ExternalPasswordLockTime(int value) {
           mValue = value;
       }

       public int getValue() {
           return mValue;
       }

       public static ExternalPasswordLockTime fromInt(int i) {
           for (ExternalPasswordLockTime s : values()) {
               if (s.getValue() == i) {
                   return s;
               }
           }
           return NotSet;
       }
	}
	
	

	
	//General Settings:
	public ExternalPIMAuthType AuthMode;
	public Boolean bShortcuts;
	public Boolean bWidgets;
	
	//Password Settings:
	public Boolean bPasswordPIN;
	public Integer nMaxPasswordTries;
	public Integer nPasswordLength;
	public ExternalPasswordType Passwordtype;
	public Boolean bPasswordSimpleAllowed;
	public Integer nPasswordComplexCharsRequired;
	public ExternalPasswordExpiration PasswordExpires;
	public Integer nPasswordHistory;
	public ExternalPasswordLockTime PasswordFailLockout;
	
	//Security Settings:
	public Boolean bCopyPaste;
	public ExternalSecurityResolution SecuritySIMRemove;
	public ExternalSecurityResolution SecurityDebugMode;
	public ExternalSecurityResolution SecurityRoot;
	public ExternalSecurityResolution SecurityIME;
	
	//Email Settings:
	public String sEmailSignature;  //ROVA_POLICY_CHECK - 26Dec2013 , changed from default access to Public
	public Boolean bEmailDownloadAttachments;
	public Integer nMaxAttachmentSize;
	
	//ApplicationSettings:
	public Boolean bShowEmail;
	public Boolean bShowContact;
	public Boolean bShowCalendar;
	public Boolean bShowBrowser;
	public Boolean bShowTXTMessage;
	public Boolean bShowDialer;
	public Boolean bShowSupport;
	
	public Boolean bisProxyBasedRoutingEnabled; // NEW_ADDITION_BROWSER -  290767, 11/11/2013
	
	public ExternalPIMSettingsInfo(){
		
	}
}
