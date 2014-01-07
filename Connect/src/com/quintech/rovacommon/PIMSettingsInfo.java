package com.quintech.rovacommon;

public class PIMSettingsInfo 
{
    public enum PIMAuthType {

        None(0),
        Certificate(1),
        Basic(2);

        private int mValue;

        private PIMAuthType(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static PIMAuthType fromInt(int i) {
            for (PIMAuthType s : values()) {
                if (s.getValue() == i) {
                    return s;
                }
            }
            return None;
        }
        
    }
    
	public enum SecurityResolution {

        Nothing(0),
        Warn(1),
        Block(2),
        Wipe(3);

        private int mValue;

        private SecurityResolution(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static SecurityResolution fromInt(int i) {
            for (SecurityResolution s : values()) {
                if (s.getValue() == i) {
                    return s;
                }
            }
            return Nothing;
        }
    }
	   
	public enum PasswordType {

       NotSet(0),
       Numeric(1),
       Alphabetic(2),
       AlphaNumeric(3),
       Symbolic(4);

       private int mValue;

       private PasswordType(int value) {
           mValue = value;
       }

       public int getValue() {
           return mValue;
       }

       public static PasswordType fromInt(int i) {
           for (PasswordType s : values()) {
               if (s.getValue() == i) {
                   return s;
               }
           }
           return NotSet;
       }
	}
	 
	public enum PasswordExpiration 
	{
       NotSet(0),
       Week(7),
       Month(30),
       ThreeMonths(90),
       SixMonths(180),
       Year(365);

       private int mValue;

       private PasswordExpiration(int value) {
           mValue = value;
       }

       public int getValue() {
           return mValue;
       }

       public static PasswordExpiration fromInt(int i) {
           for (PasswordExpiration s : values()) {
               if (s.getValue() == i) {
                   return s;
               }
           }
           return NotSet;
       }
	}
   
	public enum PasswordLockTime 
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

       private PasswordLockTime(int value) {
           mValue = value;
       }

       public int getValue() {
           return mValue;
       }

       public static PasswordLockTime fromInt(int i) {
           for (PasswordLockTime s : values()) {
               if (s.getValue() == i) {
                   return s;
               }
           }
           return NotSet;
       }
	}
	
	
	//General Settings:
	public PIMAuthType AuthMode;
	public Boolean bShortcuts;
	public Boolean bWidgets;
	
	//Password Settings:
	public Boolean bPasswordPIN;
	public Integer nMaxPasswordTries;
	public Integer nPasswordLength;
	public PasswordType Passwordtype;
	public Boolean bPasswordSimpleAllowed;
	public Integer nPasswordComplexCharsRequired;
	public PasswordExpiration PasswordExpires;
	public Integer nPasswordHistory;
	public PasswordLockTime PasswordFailLockout;
	
	//Security Settings:
	public Boolean bCopyPaste;
	public SecurityResolution SecuritySIMRemove;
	public SecurityResolution SecurityDebugMode;
	public SecurityResolution SecurityRoot;
	public SecurityResolution SecurityIME;
	
	//Email Settings:
	public String sEmailSignature;	//367712, Changed scope from default to public
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
	
	//Proxy enabled
	public Boolean bisProxyBasedRoutingEnabled;
}
