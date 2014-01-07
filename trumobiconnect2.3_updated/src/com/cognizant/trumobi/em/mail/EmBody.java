

package com.cognizant.trumobi.em.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface EmBody {
    public InputStream getInputStream() throws EmMessagingException;
    public void writeTo(OutputStream out) throws IOException, EmMessagingException;
}
