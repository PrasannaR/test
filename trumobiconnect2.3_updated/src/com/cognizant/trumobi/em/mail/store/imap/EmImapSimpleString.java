

package com.cognizant.trumobi.em.mail.store.imap;

import com.cognizant.trumobi.em.EmUtility;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Subclass of {@link EmImapString} used for non literals.
 */
public class EmImapSimpleString extends EmImapString {
    private String mString;

    /* package */  EmImapSimpleString(String string) {
        mString = (string != null) ? string : "";
    }

    @Override
    public void destroy() {
        mString = null;
        super.destroy();
    }

    @Override
    public String getString() {
        return mString;
    }

    @Override
    public InputStream getAsStream() {
        return new ByteArrayInputStream(EmUtility.toAscii(mString));
    }

    @Override
    public String toString() {
        // Purposefully not return just mString, in order to prevent using it instead of getString.
        return "\"" + mString + "\"";
    }
}
