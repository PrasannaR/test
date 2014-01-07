package com.quintech.common;

public class ClientSettingInfo 
{
	public String settingName;
	public String value = "";
	public String Containerid;
	
	public ClientSettingInfo()
	{
		
	}
	
	public ClientSettingInfo(String settingName, String value, String containerid)
	{
		this.settingName = settingName;
		this.value = value;
		this.Containerid = containerid;
	}
}
