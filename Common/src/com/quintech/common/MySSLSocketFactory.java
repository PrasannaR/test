package com.quintech.common;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

import com.quintech.common.ILog.Type;

public class MySSLSocketFactory extends SSLSocketFactory { 
    SSLContext sslContext = SSLContext.getInstance("TLS"); 
    String TAG = "MySSLSocketFactory";
    
    public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException { 
        super(truststore); 
 
        X509TrustManager tm = new X509TrustManager() { 
        	@Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { 
			    for (java.security.cert.X509Certificate Cert : chain)
			    {
			    	String CAName = Cert.getIssuerDN().getName();				    
			    	AbstractConstants.log.add(TAG, Type.Debug, "verifyTrust SSL CA : " + CAName);
			    }
            } 
        	@Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { 
			    for (java.security.cert.X509Certificate Cert : chain)
			    {
			    	String CAName = Cert.getIssuerDN().getName();				    
			    	AbstractConstants.log.add(TAG, Type.Debug, "verifyTrust SSL CA : " + CAName);
			    }
            } 
 
            public X509Certificate[] getAcceptedIssuers() { 
                return null; 
            } 
        }; 
 
        sslContext.init(null, new TrustManager[] { tm }, null); 
    } 
 
    @Override 
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException { 
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose); 
    } 
 
    @Override 
    public Socket createSocket() throws IOException { 
        return sslContext.getSocketFactory().createSocket(); 
    } 
} 
