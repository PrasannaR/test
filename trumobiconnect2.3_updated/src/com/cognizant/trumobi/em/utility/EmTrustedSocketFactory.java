package com.cognizant.trumobi.em.utility;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.params.HttpParams;

import com.cognizant.trumobi.em.activity.EmWelcome;
import com.cognizant.trumobi.exchange.provider.EmExchangeData;
import com.cognizant.trumobi.log.EmailLog;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class EmTrustedSocketFactory implements LayeredSocketFactory {
	private SSLSocketFactory mSocketFactory;
	private org.apache.http.conn.ssl.SSLSocketFactory mSchemeSocketFactory;
	
	private Context mContext;
	
	public EmTrustedSocketFactory(String host, boolean secure, Context context)
			throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, CertificateException, IOException,
			UnrecoverableKeyException {
		mContext = context;


		EmailLog.d("", "Trusted host: " + host);
		EmailLog.d("", "To be secure : " + secure);
		SSLContext sslContext = SSLContext.getInstance("TLS");
		if (!EmWelcome.useCertBasedSetup)
			sslContext.init(null, null, null);

		KeyStore client = KeyStore.getInstance("PKCS12");

		// =================================================================//
		// Find the directory for the SD Card using the API
		/*
		 * File sdcard = Environment.getExternalStorageDirectory(); //Get the
		 * .pfx file File file = new File(sdcard,"/Download/parasu.pfx");
		 * FileInputStream fis = new FileInputStream(file); InputStream is = new
		 * BufferedInputStream(fis); ByteArrayOutputStream bos = new
		 * ByteArrayOutputStream(); byte[] b = new byte[1024]; int bytesRead;
		 * while ((bytesRead = is.read(b)) != -1) { bos.write(b, 0, bytesRead);
		 * } byte[] bytes = bos.toByteArray(); is.close();
		 */
		// =================================================================//
		if (EmWelcome.useCertBasedSetup) {
			Context thisContext = mContext;// (EmailWelcomeActivity.appContext
																	// == null)?
																	// local:EmailWelcomeActivity.appContext;

			EmExchangeData mExchangeData = EmExchangeData.getInstance(thisContext); // NaGa

			mExchangeData.setCertificateData(thisContext);

			if (mExchangeData.getPfxbyteArray() != null) {
				EmailLog.d("ExchangeData.getPfxbyteArray()  :::", ""
						+ mExchangeData.getPfxbyteArray());

				InputStream clientStream = new ByteArrayInputStream(
						mExchangeData.getPfxbyteArray());
				client.load(clientStream, mExchangeData.getPfxPass()
						.toCharArray());

				KeyManagerFactory keyMgr = KeyManagerFactory
						.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				keyMgr.init(client, "".toCharArray());
				try {

					sslContext.init(keyMgr.getKeyManagers(),
							new TrustManager[] { EmTrustManagerFactory.get(
									host, secure) }, new SecureRandom());
				} catch (ExceptionInInitializerError e) {
					e.printStackTrace();
				}
				catch (NoClassDefFoundError e) {
					e.printStackTrace();
				}
			}
		} else {
			try {

				sslContext.init(null,
						new TrustManager[] { EmTrustManagerFactory.get(host,
								secure) }, new SecureRandom());

			} catch (ExceptionInInitializerError e) {
				e.printStackTrace();
			} catch (NoClassDefFoundError e) {
				e.printStackTrace();
			}
		}
		mSocketFactory = sslContext.getSocketFactory();
		mSchemeSocketFactory = org.apache.http.conn.ssl.SSLSocketFactory
				.getSocketFactory();
		mSchemeSocketFactory
				.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);

	
	}

	public Socket connectSocket(Socket sock, String host, int port,
			InetAddress localAddress, int localPort, HttpParams params)
			throws IOException, UnknownHostException, ConnectTimeoutException {
		return mSchemeSocketFactory.connectSocket(sock, host, port,
				localAddress, localPort, params);
	}

	public Socket createSocket() throws IOException {
		return mSocketFactory.createSocket();
	}

	public boolean isSecure(Socket sock) throws IllegalArgumentException {
		return mSchemeSocketFactory.isSecure(sock);
	}

	public Socket createSocket(final Socket socket, final String host,
			final int port, final boolean autoClose) throws IOException,
			UnknownHostException {
		SSLSocket sslSocket = (SSLSocket) mSocketFactory.createSocket(socket,
				host, port, autoClose);
		
		return sslSocket;
	}
}
