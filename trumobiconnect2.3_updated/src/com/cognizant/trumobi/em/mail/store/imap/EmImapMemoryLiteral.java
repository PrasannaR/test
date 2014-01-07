

package com.cognizant.trumobi.em.mail.store.imap;

import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.EmFixedLengthInputStream;
import com.cognizant.trumobi.em.EmUtility;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Subclass of {@link EmImapString} used for literals backed by an in-memory byte array.
 */
public class EmImapMemoryLiteral extends EmImapString {
    private byte[] mData;

    /* package */ EmImapMemoryLiteral(EmFixedLengthInputStream in) throws IOException {
        // We could use ByteArrayOutputStream and IOUtils.copy, but it'd perform an unnecessary
        // copy....
        mData = new byte[in.getLength()];
        int pos = 0;
        while (pos < mData.length) {
            int read = in.read(mData, pos, mData.length - pos);
            if (read < 0) {
                break;
            }
            pos += read;
        }
        if (pos != mData.length) {
            Log.w(Email.LOG_TAG, "");
        }
    }

    @Override
    public void destroy() {
        mData = null;
        super.destroy();
    }

    @Override
    public String getString() {
        return EmUtility.fromAscii(mData);
    }

    @Override
    public InputStream getAsStream() {
        return new ByteArrayInputStream(mData);
    }

    @Override
    public String toString() {
        return String.format("{%d byte literal(memory)}", mData.length);
    }
}
