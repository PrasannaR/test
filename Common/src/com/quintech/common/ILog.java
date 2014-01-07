package com.quintech.common;


public interface ILog 
{
	final static String TAG = "Log";
	final static String APPLICATION_TAG = "Verizon Stadium Wi-Fi";

	
	public enum Type
	{
		Fatal,
		Verbose,
		Error,
		Debug,
		Warn,
		Info,
		None
	}
	
	
	
	public boolean initialize();

	
	public void add(String tag, Type type, String message, Exception error);

	
	public void add(String tag, Type type, String message);
}
