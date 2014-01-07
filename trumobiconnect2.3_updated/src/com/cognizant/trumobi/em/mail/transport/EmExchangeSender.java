

package com.cognizant.trumobi.em.mail.transport;

import com.cognizant.trumobi.em.mail.EmSender;

import android.content.Context;

/**
 * Our Exchange service does not use the sender/store model.  This class exists for exactly one
 * purpose, which is to return "null" for getSettingActivityClass().
 */
public class EmExchangeSender extends EmSender {

    /**
     * Factory method.
     */
    public static EmSender newInstance(Context context, String uri) {
        return new EmExchangeSender(context, uri);
    }

    private EmExchangeSender(Context context, String _uri) {
    }

    @Override
    public void close() {
    }

    @Override
    public void open() {
    }

    @Override
    public void sendMessage(long messageId) {
    }

    /**
     * Get class of SettingActivity for this Sender class.
     * @return Activity class that has class method actionEditOutgoingSettings(), or null if
     * outgoing settings should not be presented (e.g. they're handled by the incoming settings
     * screen).
     */
    @Override
    public Class<? extends android.app.Activity> getSettingActivityClass() {
        return null;
    }

}
