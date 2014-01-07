

package com.cognizant.trumobi.em.mail.internet;

import com.cognizant.trumobi.em.mail.EmBody;
import com.cognizant.trumobi.em.mail.EmMessagingException;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class EmTextBody implements EmBody {
    String mBody;

    public EmTextBody(String body) {
        this.mBody = body;
    }

    public void writeTo(OutputStream out) throws IOException, EmMessagingException {
        byte[] bytes = mBody.getBytes("UTF-8");
        out.write(Base64.encode(bytes, Base64.CRLF));
    }

    /**
     * Get the text of the body in it's unencoded format.
     * @return
     */
    public String getText() {
        return mBody;
    }

    /**
     * Returns an InputStream that reads this body's text in UTF-8 format.
     */
    public InputStream getInputStream() throws EmMessagingException {
        try {
            byte[] b = mBody.getBytes("UTF-8");
            return new ByteArrayInputStream(b);
        }
        catch (UnsupportedEncodingException usee) {
            return null;
        }
    }
}
