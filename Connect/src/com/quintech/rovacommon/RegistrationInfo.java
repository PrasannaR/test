package com.quintech.rovacommon;

import java.util.Map;

import com.quintech.rovacommon.PIMSettingsInfo.PIMAuthType;

public class RegistrationInfo {
	   public PIMAuthType authenticationType;
	   public EmailSettings emailset;
	   public VPNSettingsInfo vpnInfo;
	   public SecurityProfileInfo secProfInfo;
	   public PIMSettingsInfo pimSettingInfo;
	   public DeviceInfo deviceInfo;
	   public Map<String,String> proxyInfo;
	   public BrowserSettingsInfo browsersettings;
}
