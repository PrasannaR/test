

package com.cognizant.trumobi.em.mail.transport;

import android.net.SSLCertificateSocketFactory;

import javax.net.ssl.SSLSocketFactory;

public class EmSSLUtils {
    private static SSLSocketFactory sInsecureFactory;
    private static SSLSocketFactory sSecureFactory;

    /**
     * Returns a {@link SSLSocketFactory}.  Optionally bypass all SSL certificate checks.
     *
     * @param insecure if true, bypass all SSL certificate checks
     */
    public synchronized static final SSLSocketFactory getSSLSocketFactory(boolean insecure) {
        if (insecure) {
            if (sInsecureFactory == null) {
                sInsecureFactory = SSLCertificateSocketFactory.getInsecure(0, null);
            }
            return sInsecureFactory;
        } else {
            if (sSecureFactory == null) {
                sSecureFactory = SSLCertificateSocketFactory.getDefault(0, null);
            }
            return sSecureFactory;
        }
    }
}
