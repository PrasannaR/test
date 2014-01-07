

package com.cognizant.trumobi.em.mail.internet;

import com.cognizant.trumobi.em.EmUtility;
import com.cognizant.trumobi.em.mail.EmMessagingException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class EmMimeHeader {
    /**
     * Application specific header that contains Store specific information about an attachment.
     * In IMAP this contains the IMAP BODYSTRUCTURE part id so that the ImapStore can later
     * retrieve the attachment at will from the server.
     * The info is recorded from this header on LocalStore.appendMessages and is put back
     * into the MIME data by LocalStore.fetch.
     */
    public static final String HEADER_ANDROID_ATTACHMENT_STORE_DATA = "X-Android-Attachment-StoreData";
    /**
     * Application specific header that is used to tag body parts for quoted/forwarded messages.
     */
    public static final String HEADER_ANDROID_BODY_QUOTED_PART = "X-Android-Body-Quoted-Part";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HEADER_CONTENT_ID = "Content-ID";

    /**
     * Fields that should be omitted when writing the header using writeTo()
     */
    private static final String[] WRITE_OMIT_FIELDS = {
//        HEADER_ANDROID_ATTACHMENT_DOWNLOADED,
//        HEADER_ANDROID_ATTACHMENT_ID,
        HEADER_ANDROID_ATTACHMENT_STORE_DATA
    };

    protected final ArrayList<Field> mFields = new ArrayList<Field>();

    public void clear() {
        mFields.clear();
    }

    public String getFirstHeader(String name) throws EmMessagingException {
        String[] header = getHeader(name);
        if (header == null) {
            return null;
        }
        return header[0];
    }

    public void addHeader(String name, String value) throws EmMessagingException {
        mFields.add(new Field(name, value));
    }

    public void setHeader(String name, String value) throws EmMessagingException {
        if (name == null || value == null) {
            return;
        }
        removeHeader(name);
        addHeader(name, value);
    }

    public String[] getHeader(String name) throws EmMessagingException {
        ArrayList<String> values = new ArrayList<String>();
        for (Field field : mFields) {
            if (field.name.equalsIgnoreCase(name)) {
                values.add(field.value);
            }
        }
        if (values.size() == 0) {
            return null;
        }
        return values.toArray(new String[] {});
    }

    public void removeHeader(String name) throws EmMessagingException {
        ArrayList<Field> removeFields = new ArrayList<Field>();
        for (Field field : mFields) {
            if (field.name.equalsIgnoreCase(name)) {
                removeFields.add(field);
            }
        }
        mFields.removeAll(removeFields);
    }

    /**
     * Write header into String
     * 
     * @return CR-NL separated header string except the headers in writeOmitFields
     * null if header is empty
     */
    public String writeToString() {
        if (mFields.size() == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Field field : mFields) {
            if (!EmUtility.arrayContains(WRITE_OMIT_FIELDS, field.name)) {
                builder.append(field.name + ": " + field.value + "\r\n");
            }
        }
        return builder.toString();
    }
    
    public void writeTo(OutputStream out) throws IOException, EmMessagingException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out), 1024);
        for (Field field : mFields) {
            if (!EmUtility.arrayContains(WRITE_OMIT_FIELDS, field.name)) {
                writer.write(field.name + ": " + field.value + "\r\n");
            }
        }
        writer.flush();
    }

    private static class Field {
        final String name;
        final String value;

        public Field(String name, String value) {
            this.name = name;
            this.value = value;
        }
        
        @Override
        public String toString() {
            return name + "=" + value;
        }
    }

    @Override
    public String toString() {
        return (mFields == null) ? null : mFields.toString();
    }
}
