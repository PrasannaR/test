

package com.cognizant.trumobi.em.mail;

import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.em.Email;

public abstract class EmSender {
    protected static final int SOCKET_CONNECT_TIMEOUT = 10000;

    private static final HashMap<String, EmSender> sSenders = new HashMap<String, EmSender>();

    /**
     * Static named constructor.  It should be overrode by extending class.
     * Because this method will be called through reflection, it can not be protected.
     */
    public static EmSender newInstance(Context context, String uri)
            throws EmMessagingException {
        throw new EmMessagingException("Sender.newInstance: Unknown scheme in " + uri);
    }

    private static EmSender instantiateSender(Context context, String className, String uri)
        throws EmMessagingException {
        Object o = null;
        try {
            Class<?> c = Class.forName(className);
            // and invoke "newInstance" class method and instantiate sender object.
            java.lang.reflect.Method m =
                c.getMethod("newInstance", Context.class, String.class);
            o = m.invoke(null, context, uri);
        } catch (Exception e) {
            Log.d(Email.LOG_TAG, String.format(
                    "exception %s invoking %s.newInstance.(Context, String) method for %s",
                    e.toString(), className, uri));
            throw new EmMessagingException("can not instantiate Sender object for " + uri);
        }
        if (!(o instanceof EmSender)) {
            throw new EmMessagingException(
                    uri + ": " + className + " create incompatible object");
        }
        return (EmSender) o;
    }
    
    /**
     * Find Sender implementation consulting with sender.xml file.
     */
    private static EmSender findSender(Context context, int resourceId, String uri)
            throws EmMessagingException {
        EmSender sender = null;
        try {
            XmlResourceParser xml = context.getResources().getXml(resourceId);
            int xmlEventType;
            // walk through senders.xml file.
            while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT) {
                if (xmlEventType == XmlResourceParser.START_TAG &&
                    "sender".equals(xml.getName())) {
                    String scheme = xml.getAttributeValue(null, "scheme");
                    if (uri.startsWith(scheme)) {
                        // found sender entry whose scheme is matched with uri.
                        // then load sender class.
                        String className = xml.getAttributeValue(null, "class");
                        sender = instantiateSender(context, className, uri);
                    }
                }
            }
        } catch (XmlPullParserException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        }
        return sender;
    }

    public synchronized static EmSender getInstance(Context context, String uri)
            throws EmMessagingException {
       EmSender sender = sSenders.get(uri);
       if (sender == null) {
           sender = findSender(context, R.xml.em_senders_product, uri);
           if (sender == null) {
               sender = findSender(context, R.xml.em_senders, uri);
           }

           if (sender != null) {
               sSenders.put(uri, sender);
           }
       }

       if (sender == null) {
            throw new EmMessagingException("Unable to locate an applicable Transport for " + uri);
       }

       return sender;
    }
    
    /**
     * Get class of SettingActivity for this Sender class.
     * @return Activity class that has class method actionEditOutgoingSettings(). 
     */
    public Class<? extends android.app.Activity> getSettingActivityClass() {
        // default SettingActivity class
        return com.cognizant.trumobi.em.activity.setup.EmAccountSetupOutgoing.class;
    }

    public abstract void open() throws EmMessagingException;
    
    public String validateSenderLimit(long messageId) {
        return null;
    }

    /**
     * Check message has any limitation of Sender or not.
     * 
     * @param messageId the message that will be checked.
     * @throws LimitViolationException
     */
    public void checkSenderLimitation(long messageId) throws LimitViolationException {
    }
    
    public static class LimitViolationException extends EmMessagingException {
        public final int mMsgResourceId;
        public final long mActual;
        public final long mLimit;
        
        private LimitViolationException(int msgResourceId, long actual, long limit) {
            super(UNSPECIFIED_EXCEPTION);
            mMsgResourceId = msgResourceId;
            mActual = actual;
            mLimit = limit;
        }
        
        public static void check(int msgResourceId, long actual, long limit)
            throws LimitViolationException {
            if (actual > limit) {
                throw new LimitViolationException(msgResourceId, actual, limit);
            }
        }
    }
    
    public abstract void sendMessage(long messageId) throws EmMessagingException;

    public abstract void close() throws EmMessagingException;
}
