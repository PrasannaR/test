

package com.cognizant.trumobi.em.mail.internet;

import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.mail.EmBody;
import com.cognizant.trumobi.em.mail.EmMessagingException;

import org.apache.commons.io.IOUtils;

import android.util.Base64;
import android.util.Base64OutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A Body that is backed by a temp file. The Body exposes a getOutputStream method that allows
 * the user to write to the temp file. After the write the body is available via getInputStream
 * and writeTo one time. After writeTo is called, or the InputStream returned from
 * getInputStream is closed the file is deleted and the Body should be considered disposed of.
 */
public class EmBinaryTempFileBody implements EmBody {
    private File mFile;

    /**
     * An alternate way to put data into a BinaryTempFileBody is to simply supply an already-
     * created file.  Note that this file will be deleted after it is read.
     * @param filePath The file containing the data to be stored on disk temporarily
     */
    public void setFile(String filePath) {
        mFile = new File(filePath);
    }

    public OutputStream getOutputStream() throws IOException {
        mFile = File.createTempFile("body", null, Email.getTempDirectory());
        mFile.deleteOnExit();
        return new FileOutputStream(mFile);
    }

    public InputStream getInputStream() throws EmMessagingException {
        try {
            return new BinaryTempFileBodyInputStream(new FileInputStream(mFile));
        }
        catch (IOException ioe) {
            throw new EmMessagingException("Unable to open body", ioe);
        }
    }

    public void writeTo(OutputStream out) throws IOException, EmMessagingException {
        InputStream in = getInputStream();
        Base64OutputStream base64Out = new Base64OutputStream(
            out, Base64.CRLF | Base64.NO_CLOSE);
        IOUtils.copy(in, base64Out);
        base64Out.close();
        mFile.delete();
    }

    class BinaryTempFileBodyInputStream extends FilterInputStream {
        public BinaryTempFileBodyInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            super.close();
            mFile.delete();
        }
    }
}
