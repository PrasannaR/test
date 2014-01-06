

package com.cognizant.trumobi.em.mail;

public class EmAuthenticationFailedException extends EmMessagingException {
    public static final long serialVersionUID = -1;

    public EmAuthenticationFailedException(String message) {
        super(EmMessagingException.AUTHENTICATION_FAILED, message);
    }

    public EmAuthenticationFailedException(String message, Throwable throwable) {
        super(message, throwable);
        mExceptionType = EmMessagingException.AUTHENTICATION_FAILED;
     }
}
