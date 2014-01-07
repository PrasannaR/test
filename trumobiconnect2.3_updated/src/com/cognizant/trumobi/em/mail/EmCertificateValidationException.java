

package com.cognizant.trumobi.em.mail;

public class EmCertificateValidationException extends EmMessagingException {
    public static final long serialVersionUID = -1;

    public EmCertificateValidationException(String message) {
        super(message);
    }

    public EmCertificateValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}