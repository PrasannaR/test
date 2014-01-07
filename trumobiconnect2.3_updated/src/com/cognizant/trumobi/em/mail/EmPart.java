

package com.cognizant.trumobi.em.mail;

import java.io.IOException;
import java.io.OutputStream;

public interface EmPart extends EmFetchable {
    public void addHeader(String name, String value) throws EmMessagingException;

    public void removeHeader(String name) throws EmMessagingException;

    public void setHeader(String name, String value) throws EmMessagingException;

    public EmBody getBody() throws EmMessagingException;

    public String getContentType() throws EmMessagingException;

    public String getDisposition() throws EmMessagingException;

    public String getContentId() throws EmMessagingException;

    public String[] getHeader(String name) throws EmMessagingException;

    public void setExtendedHeader(String name, String value) throws EmMessagingException;

    public String getExtendedHeader(String name) throws EmMessagingException;

    public int getSize() throws EmMessagingException;

    public boolean isMimeType(String mimeType) throws EmMessagingException;

    public String getMimeType() throws EmMessagingException;

    public void setBody(EmBody body) throws EmMessagingException;

    public void writeTo(OutputStream out) throws IOException, EmMessagingException;
}
