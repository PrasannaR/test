

package com.cognizant.trumobi.em.mail.transport;

import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.mail.EmAddress;
import com.cognizant.trumobi.em.mail.EmAuthenticationFailedException;
import com.cognizant.trumobi.em.mail.EmCertificateValidationException;
import com.cognizant.trumobi.em.mail.EmMessagingException;
import com.cognizant.trumobi.em.mail.EmSender;
import com.cognizant.trumobi.em.mail.EmTransport;
import com.cognizant.trumobi.em.provider.EmEmailContent.Message;

import android.content.Context;
import android.util.Config;
import android.util.Log;
import android.util.Base64;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLException;

/**
 * This class handles all of the protocol-level aspects of sending messages via SMTP.
 */
public class EmSmtpSender extends EmSender {

    private final Context mContext;
    private EmTransport mTransport;
    private String mUsername;
    private String mPassword;

    /**
     * Static named constructor.
     */
    public static EmSender newInstance(Context context, String uri) throws EmMessagingException {
        return new EmSmtpSender(context, uri);
    }

    /**
     * Allowed formats for the Uri:
     * smtp://user:password@server:port
     * smtp+tls+://user:password@server:port
     * smtp+tls+trustallcerts://user:password@server:port
     * smtp+ssl+://user:password@server:port
     * smtp+ssl+trustallcerts://user:password@server:port
     *
     * @param uriString the Uri containing information to configure this sender
     */
    private EmSmtpSender(Context context, String uriString) throws EmMessagingException {
        mContext = context;
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException use) {
            throw new EmMessagingException("Invalid SmtpTransport URI", use);
        }

        String scheme = uri.getScheme();
        if (scheme == null || !scheme.startsWith("smtp")) {
            throw new EmMessagingException("Unsupported protocol");
        }
        // defaults, which can be changed by security modifiers
        int connectionSecurity = EmTransport.CONNECTION_SECURITY_NONE;
        int defaultPort = 587;
        // check for security modifiers and apply changes
        if (scheme.contains("+ssl")) {
            connectionSecurity = EmTransport.CONNECTION_SECURITY_SSL;
            defaultPort = 465;
        } else if (scheme.contains("+tls")) {
            connectionSecurity = EmTransport.CONNECTION_SECURITY_TLS;
        }
        boolean trustCertificates = scheme.contains("+trustallcerts");

        mTransport = new EmMailTransport("SMTP");
        mTransport.setUri(uri, defaultPort);
        mTransport.setSecurity(connectionSecurity, trustCertificates);

        String[] userInfoParts = mTransport.getUserInfoParts();
        if (userInfoParts != null) {
            mUsername = userInfoParts[0];
            if (userInfoParts.length > 1) {
                mPassword = userInfoParts[1];
            }
        }
    }

    /**
     * For testing only.  Injects a different transport.  The transport should already be set
     * up and ready to use.  Do not use for real code.
     * @param testTransport The Transport to inject and use for all future communication.
     */
    /* package */ void setTransport(EmTransport testTransport) {
        mTransport = testTransport;
    }

    @Override
    public void open() throws EmMessagingException {
        try {
            mTransport.open();

            // Eat the banner
            executeSimpleCommand(null);

            String localHost = "localhost";
            // Try to get local address in the X.X.X.X format.
            InetAddress localAddress = mTransport.getLocalAddress();
            if (localAddress != null) {
                localHost = localAddress.getHostAddress();
            }
            String result = executeSimpleCommand("EHLO " + localHost);

            /*
             * TODO may need to add code to fall back to HELO I switched it from
             * using HELO on non STARTTLS connections because of AOL's mail
             * server. It won't let you use AUTH without EHLO.
             * We should really be paying more attention to the capabilities
             * and only attempting auth if it's available, and warning the user
             * if not.
             */
            if (mTransport.canTryTlsSecurity()) {
                if (result.contains("-STARTTLS")) {
                    executeSimpleCommand("STARTTLS");
                    mTransport.reopenTls();
                    /*
                     * Now resend the EHLO. Required by RFC2487 Sec. 5.2, and more specifically,
                     * Exim.
                     */
                    result = executeSimpleCommand("EHLO " + localHost);
                } else {
                    if (Config.LOGD && Email.DEBUG) {
                        Log.d(Email.LOG_TAG, "TLS not supported but required");
                    }
                    throw new EmMessagingException(EmMessagingException.TLS_REQUIRED);
                }
            }

            /*
             * result contains the results of the EHLO in concatenated form
             */
            boolean authLoginSupported = result.matches(".*AUTH.*LOGIN.*$");
            boolean authPlainSupported = result.matches(".*AUTH.*PLAIN.*$");

            if (mUsername != null && mUsername.length() > 0 && mPassword != null
                    && mPassword.length() > 0) {
                if (authPlainSupported) {
                    saslAuthPlain(mUsername, mPassword);
                }
                else if (authLoginSupported) {
                    saslAuthLogin(mUsername, mPassword);
                }
                else {
                    if (Config.LOGD && Email.DEBUG) {
                        Log.d(Email.LOG_TAG, "No valid authentication mechanism found.");
                    }
                    throw new EmMessagingException(EmMessagingException.AUTH_REQUIRED);
                }
            }
        } catch (SSLException e) {
            if (Config.LOGD && Email.DEBUG) {
                Log.d(Email.LOG_TAG, e.toString());
            }
            throw new EmCertificateValidationException(e.getMessage(), e);
        } catch (IOException ioe) {
            if (Config.LOGD && Email.DEBUG) {
                Log.d(Email.LOG_TAG, ioe.toString());
            }
            throw new EmMessagingException(EmMessagingException.IOERROR, ioe.toString());
        }
    }

    @Override
    public void sendMessage(long messageId) throws EmMessagingException {
        close();
        open();

        Message message = Message.restoreMessageWithId(mContext, messageId);
        if (message == null) {
            throw new EmMessagingException("Trying to send non-existent message id="
                    + Long.toString(messageId));
        }
        EmAddress from = EmAddress.unpackFirst(message.mFrom);
        EmAddress[] to = EmAddress.unpack(message.mTo);
        EmAddress[] cc = EmAddress.unpack(message.mCc);
        EmAddress[] bcc = EmAddress.unpack(message.mBcc);

        try {
            executeSimpleCommand("MAIL FROM: " + "<" + from.getAddress() + ">");
            for (EmAddress address : to) {
                executeSimpleCommand("RCPT TO: " + "<" + address.getAddress() + ">");
            }
            for (EmAddress address : cc) {
                executeSimpleCommand("RCPT TO: " + "<" + address.getAddress() + ">");
            }
            for (EmAddress address : bcc) {
                executeSimpleCommand("RCPT TO: " + "<" + address.getAddress() + ">");
            }
            executeSimpleCommand("DATA");
            // TODO byte stuffing
            EmRfc822Output.writeTo(mContext, messageId,
                    new EmEOLConvertingOutputStream(mTransport.getOutputStream()), true, false);
            executeSimpleCommand("\r\n.");
        } catch (IOException ioe) {
            throw new EmMessagingException("Unable to send message", ioe);
        }
    }

    /**
     * Close the protocol (and the transport below it).
     *
     * MUST NOT return any exceptions.
     */
    @Override
    public void close() {
        mTransport.close();
    }

    /**
     * Send a single command and wait for a single response.  Handles responses that continue
     * onto multiple lines.  Throws MessagingException if response code is 4xx or 5xx.  All traffic
     * is logged (if debug logging is enabled) so do not use this function for user ID or password.
     *
     * @param command The command string to send to the server.
     * @return Returns the response string from the server.
     */
    private String executeSimpleCommand(String command) throws IOException, EmMessagingException {
        return executeSensitiveCommand(command, null);
    }

    /**
     * Send a single command and wait for a single response.  Handles responses that continue
     * onto multiple lines.  Throws MessagingException if response code is 4xx or 5xx.
     *
     * @param command The command string to send to the server.
     * @param sensitiveReplacement If the command includes sensitive data (e.g. authentication)
     * please pass a replacement string here (for logging).
     * @return Returns the response string from the server.
     */
    private String executeSensitiveCommand(String command, String sensitiveReplacement)
            throws IOException, EmMessagingException {
        if (command != null) {
            mTransport.writeLine(command, sensitiveReplacement);
        }

        String line = mTransport.readLine();

        String result = line;

        while (line.length() >= 4 && line.charAt(3) == '-') {
            line = mTransport.readLine();
            result += line.substring(3);
        }

        if (result.length() > 0) {
            char c = result.charAt(0);
            if ((c == '4') || (c == '5')) {
                throw new EmMessagingException(result);
            }
        }

        return result;
    }


//    C: AUTH LOGIN
//    S: 334 VXNlcm5hbWU6
//    C: d2VsZG9u
//    S: 334 UGFzc3dvcmQ6
//    C: dzNsZDBu
//    S: 235 2.0.0 OK Authenticated
//
//    Lines 2-5 of the conversation contain base64-encoded information. The same conversation, with base64 strings decoded, reads:
//
//
//    C: AUTH LOGIN
//    S: 334 Username:
//    C: weldon
//    S: 334 Password:
//    C: w3ld0n
//    S: 235 2.0.0 OK Authenticated

    private void saslAuthLogin(String username, String password) throws EmMessagingException,
        EmAuthenticationFailedException, IOException {
        try {
            executeSimpleCommand("AUTH LOGIN");
            executeSensitiveCommand(
                    Base64.encodeToString(username.getBytes(), Base64.NO_WRAP),
                    "/username redacted/");
            executeSensitiveCommand(
                    Base64.encodeToString(password.getBytes(), Base64.NO_WRAP),
                    "/password redacted/");
        }
        catch (EmMessagingException me) {
            if (me.getMessage().length() > 1 && me.getMessage().charAt(1) == '3') {
                throw new EmAuthenticationFailedException(me.getMessage());
            }
            throw me;
        }
    }

    private void saslAuthPlain(String username, String password) throws EmMessagingException,
            EmAuthenticationFailedException, IOException {
        byte[] data = ("\000" + username + "\000" + password).getBytes();
        data = Base64.encode(data, Base64.NO_WRAP);
        try {
            executeSensitiveCommand("AUTH PLAIN " + new String(data), "AUTH PLAIN /redacted/");
        }
        catch (EmMessagingException me) {
            if (me.getMessage().length() > 1 && me.getMessage().charAt(1) == '3') {
                throw new EmAuthenticationFailedException(me.getMessage());
            }
            throw me;
        }
    }
}
