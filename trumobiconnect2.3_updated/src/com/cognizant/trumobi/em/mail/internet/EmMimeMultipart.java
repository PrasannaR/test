

package com.cognizant.trumobi.em.mail.internet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.cognizant.trumobi.em.mail.EmBodyPart;
import com.cognizant.trumobi.em.mail.EmMessagingException;
import com.cognizant.trumobi.em.mail.EmMultipart;

public class EmMimeMultipart extends EmMultipart {
    protected String mPreamble;

    protected String mContentType;

    protected String mBoundary;

    protected String mSubType;

    public EmMimeMultipart() throws EmMessagingException {
        mBoundary = generateBoundary();
        setSubType("mixed");
    }

    public EmMimeMultipart(String contentType) throws EmMessagingException {
        this.mContentType = contentType;
        try {
            mSubType = EmMimeUtility.getHeaderParameter(contentType, null).split("/")[1];
            mBoundary = EmMimeUtility.getHeaderParameter(contentType, "boundary");
            if (mBoundary == null) {
                throw new EmMessagingException("MultiPart does not contain boundary: " + contentType);
            }
        } catch (Exception e) {
            throw new EmMessagingException(
                    "Invalid MultiPart Content-Type; must contain subtype and boundary. ("
                            + contentType + ")", e);
        }
    }

    public String generateBoundary() {
        StringBuffer sb = new StringBuffer();
        sb.append("----");
        for (int i = 0; i < 30; i++) {
            sb.append(Integer.toString((int)(Math.random() * 35), 36));
        }
        return sb.toString().toUpperCase();
    }

    public String getPreamble() throws EmMessagingException {
        return mPreamble;
    }

    public void setPreamble(String preamble) throws EmMessagingException {
        this.mPreamble = preamble;
    }

    @Override
    public String getContentType() throws EmMessagingException {
        return mContentType;
    }

    public void setSubType(String subType) throws EmMessagingException {
        this.mSubType = subType;
        mContentType = String.format("multipart/%s; boundary=\"%s\"", subType, mBoundary);
    }

    public void writeTo(OutputStream out) throws IOException, EmMessagingException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out), 1024);

        if (mPreamble != null) {
            writer.write(mPreamble + "\r\n");
        }

        for (int i = 0, count = mParts.size(); i < count; i++) {
            EmBodyPart bodyPart = (EmBodyPart)mParts.get(i);
            writer.write("--" + mBoundary + "\r\n");
            writer.flush();
            bodyPart.writeTo(out);
            writer.write("\r\n");
        }

        writer.write("--" + mBoundary + "--\r\n");
        writer.flush();
    }

    public InputStream getInputStream() throws EmMessagingException {
        return null;
    }

    public String getSubTypeForTest() {
        return mSubType;
    }
}
