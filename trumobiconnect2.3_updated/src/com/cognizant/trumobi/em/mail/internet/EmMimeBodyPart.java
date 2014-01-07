

package com.cognizant.trumobi.em.mail.internet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;

import com.cognizant.trumobi.em.mail.EmBody;
import com.cognizant.trumobi.em.mail.EmBodyPart;
import com.cognizant.trumobi.em.mail.EmMessagingException;

/**
 * TODO this is a close approximation of Message, need to update along with
 * Message.
 */
public class EmMimeBodyPart extends EmBodyPart {
    protected EmMimeHeader mHeader = new EmMimeHeader();
    protected EmMimeHeader mExtendedHeader;
    protected EmBody mBody;
    protected int mSize;

    // regex that matches content id surrounded by "<>" optionally.
    private static final Pattern REMOVE_OPTIONAL_BRACKETS = Pattern.compile("^<?([^>]+)>?$");
    // regex that matches end of line.
    private static final Pattern END_OF_LINE = Pattern.compile("\r?\n");

    public EmMimeBodyPart() throws EmMessagingException {
        this(null);
    }

    public EmMimeBodyPart(EmBody body) throws EmMessagingException {
        this(body, null);
    }

    public EmMimeBodyPart(EmBody body, String mimeType) throws EmMessagingException {
        if (mimeType != null) {
            setHeader(EmMimeHeader.HEADER_CONTENT_TYPE, mimeType);
        }
        setBody(body);
    }

    protected String getFirstHeader(String name) throws EmMessagingException {
        return mHeader.getFirstHeader(name);
    }

    public void addHeader(String name, String value) throws EmMessagingException {
        mHeader.addHeader(name, value);
    }

    public void setHeader(String name, String value) throws EmMessagingException {
        mHeader.setHeader(name, value);
    }

    public String[] getHeader(String name) throws EmMessagingException {
        return mHeader.getHeader(name);
    }

    public void removeHeader(String name) throws EmMessagingException {
        mHeader.removeHeader(name);
    }

    public EmBody getBody() throws EmMessagingException {
        return mBody;
    }

    public void setBody(EmBody body) throws EmMessagingException {
        this.mBody = body;
        if (body instanceof com.cognizant.trumobi.em.mail.EmMultipart) {
            com.cognizant.trumobi.em.mail.EmMultipart multipart = ((com.cognizant.trumobi.em.mail.EmMultipart)body);
            multipart.setParent(this);
            setHeader(EmMimeHeader.HEADER_CONTENT_TYPE, multipart.getContentType());
        }
        else if (body instanceof EmTextBody) {
            String contentType = String.format("%s;\n charset=utf-8", getMimeType());
            String name = EmMimeUtility.getHeaderParameter(getContentType(), "name");
            if (name != null) {
                contentType += String.format(";\n name=\"%s\"", name);
            }
            setHeader(EmMimeHeader.HEADER_CONTENT_TYPE, contentType);
            setHeader(EmMimeHeader.HEADER_CONTENT_TRANSFER_ENCODING, "base64");
        }
    }

    public String getContentType() throws EmMessagingException {
        String contentType = getFirstHeader(EmMimeHeader.HEADER_CONTENT_TYPE);
        if (contentType == null) {
            return "text/plain";
        } else {
            return contentType;
        }
    }

    public String getDisposition() throws EmMessagingException {
        String contentDisposition = getFirstHeader(EmMimeHeader.HEADER_CONTENT_DISPOSITION);
        if (contentDisposition == null) {
            return null;
        } else {
            return contentDisposition;
        }
    }

    public String getContentId() throws EmMessagingException {
        String contentId = getFirstHeader(EmMimeHeader.HEADER_CONTENT_ID);
        if (contentId == null) {
            return null;
        } else {
            // remove optionally surrounding brackets.
            return REMOVE_OPTIONAL_BRACKETS.matcher(contentId).replaceAll("$1");
        }
    }

    public String getMimeType() throws EmMessagingException {
        return EmMimeUtility.getHeaderParameter(getContentType(), null);
    }

    public boolean isMimeType(String mimeType) throws EmMessagingException {
        return getMimeType().equals(mimeType);
    }

    public void setSize(int size) {
        this.mSize = size;
    }

    public int getSize() throws EmMessagingException {
        return mSize;
    }

    /**
     * Set extended header
     * 
     * @param name Extended header name
     * @param value header value - flattened by removing CR-NL if any
     * remove header if value is null
     * @throws EmMessagingException
     */
    public void setExtendedHeader(String name, String value) throws EmMessagingException {
        if (value == null) {
            if (mExtendedHeader != null) {
                mExtendedHeader.removeHeader(name);
            }
            return;
        }
        if (mExtendedHeader == null) {
            mExtendedHeader = new EmMimeHeader(); 
        }
        mExtendedHeader.setHeader(name, END_OF_LINE.matcher(value).replaceAll(""));
    }

    /**
     * Get extended header
     * 
     * @param name Extended header name
     * @return header value - null if header does not exist
     * @throws EmMessagingException 
     */
    public String getExtendedHeader(String name) throws EmMessagingException {
        if (mExtendedHeader == null) {
            return null;
        }
        return mExtendedHeader.getFirstHeader(name);
    }

    /**
     * Write the MimeMessage out in MIME format.
     */
    public void writeTo(OutputStream out) throws IOException, EmMessagingException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out), 1024);
        mHeader.writeTo(out);
        writer.write("\r\n");
        writer.flush();
        if (mBody != null) {
            mBody.writeTo(out);
        }
    }
}
