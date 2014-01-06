

package com.cognizant.trumobi.em.service;

/**
 * Definitions of service status codes returned to IEmailServiceCallback's status method
 */
public interface EmEmailServiceStatus {
    public static final int SUCCESS = 0;
    public static final int IN_PROGRESS = 1;

    public static final int MESSAGE_NOT_FOUND = 0x10;
    public static final int ATTACHMENT_NOT_FOUND = 0x11;
    public static final int FOLDER_NOT_DELETED = 0x12;
    public static final int FOLDER_NOT_RENAMED = 0x13;
    public static final int FOLDER_NOT_CREATED = 0x14;
    public static final int REMOTE_EXCEPTION = 0x15;
    public static final int LOGIN_FAILED = 0x16;
    public static final int SECURITY_FAILURE = 0x17;
    public static final int ACCOUNT_UNINITIALIZED = 0x18;

    // Maybe we should automatically retry these?
    public static final int CONNECTION_ERROR = 0x20;
}
