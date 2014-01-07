package com.cognizant.trumobi.persona.constants;

import android.os.Environment;

public class PersonaCertfifcateAuthConstants {

	public static final String LIVE = "";
	// public static final String STAGING = "https://trumobiuat.cognizant.com/";

	// Dev Instance
	// public static final String STAGING = "https://trumobidev.cognizant.com/";

	// UAT Instance
	// public static final String STAGING = "https://truboxuat.cognizant.com/";

	// M1C Instance
	// public static final String STAGING = "https://m1c.cognizant.com/";

	// Testing Instance
	public static final String STAGING = "https://trumobitesting.cognizant.com/";

	public static final String ACKNOWLEDGEMENT_CERTIFICATE_DOWNLOAD = STAGING
			+ "UserService.svc/AckPersonaCert";
	public static final String IS_DEVICE_REGISTERED = STAGING
			+ "MIMAccess.svc/IsDeviceRegistered";
	public static final String DOWNLOAD_CERTIFICATE_PERSONA = STAGING
			+ "UserService.svc/GetUserCertificateWithDeviceUID_Persona";
	/*
	 * public static final String DOWNLOAD_CERTIFICATE_PERSONA = STAGING
	 * +"UserService.svc/GetUserCertificateWithDeviceUID";
	 */
	public static final String AUTHENTICATION_SERVICE_PERSONA = STAGING
			+ "AuthenticationService.svc/RegDeviceWithPersona";
	public static final String ROUTINE_LOGIN_CHECK = STAGING
			+ "AuthenticationService.svc/AuthenticatePersona";
	public static final String EMAIL_RE_REQUEST_SERVICE = STAGING
			+ "UserService.svc/RequestEmailCertificateFromPersona";
	public static final String EMAIL_REVOKE_SERVICE = STAGING
			+ "UserService.svc/RevokePersonaCert";

	public static final String DOWNLOAD_PATH = Environment
			.getExternalStorageDirectory() + "/download/";
	public static final int CERT_FAILURE_EVENT = 11;

	public static final int WARN_USER = 1;
	public static final int RE_REQUEST_CERT = 2;
	public static final int REVOKE_EMAIL=1200;
	
	
	public static final int TRUHUB_NOT_INSTALLED=2010;

}
