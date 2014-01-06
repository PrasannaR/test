

package com.cognizant.trumobi.em.mail;

public class EmNoSuchProviderException extends EmMessagingException {
    public static final long serialVersionUID = -1;

    public EmNoSuchProviderException(String message) {
        super(message);
    }

    public EmNoSuchProviderException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
