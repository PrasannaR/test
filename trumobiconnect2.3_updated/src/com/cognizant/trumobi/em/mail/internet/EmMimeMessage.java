

package com.cognizant.trumobi.em.mail.internet;

import com.cognizant.trumobi.em.mail.EmAddress;
import com.cognizant.trumobi.em.mail.EmBody;
import com.cognizant.trumobi.em.mail.EmBodyPart;
import com.cognizant.trumobi.em.mail.EmMessage;
import com.cognizant.trumobi.em.mail.EmMessagingException;
import com.cognizant.trumobi.em.mail.EmPart;

import org.apache.james.mime4j.BodyDescriptor;
import org.apache.james.mime4j.ContentHandler;
import org.apache.james.mime4j.EOLConvertingInputStream;
import org.apache.james.mime4j.MimeStreamParser;
import org.apache.james.mime4j.field.DateTimeField;
import org.apache.james.mime4j.field.Field;

import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * An implementation of Message that stores all of its metadata in RFC 822 and
 * RFC 2045 style headers.
 *
 * NOTE:  Automatic generation of a local message-id is becoming unwieldy and should be removed.
 * It would be better to simply do it explicitly on local creation of new outgoing messages.
 */
public class EmMimeMessage extends EmMessage {
    private EmMimeHeader mHeader;
    private EmMimeHeader mExtendedHeader;
    
    // NOTE:  The fields here are transcribed out of headers, and values stored here will supercede
    // the values found in the headers.  Use caution to prevent any out-of-phase errors.  In
    // particular, any adds/changes/deletes here must be echoed by changes in the parse() function.
    private EmAddress[] mFrom;
    private EmAddress[] mTo;
    private EmAddress[] mCc;
    private EmAddress[] mBcc;
    private EmAddress[] mReplyTo;
    private Date mSentDate;
    private EmBody mBody;
    protected int mSize;
    private boolean mInhibitLocalMessageId = false;

    // Shared random source for generating local message-id values
    private static final java.util.Random sRandom = new java.util.Random();
    
    // In MIME, en_US-like date format should be used. In other words "MMM" should be encoded to
    // "Jan", not the other localized format like "Ene" (meaning January in locale es).
    // This conversion is used when generating outgoing MIME messages. Incoming MIME date
    // headers are parsed by org.apache.james.mime4j.field.DateTimeField which does not have any
    // localization code.
    private static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

    // regex that matches content id surrounded by "<>" optionally.
    private static final Pattern REMOVE_OPTIONAL_BRACKETS = Pattern.compile("^<?([^>]+)>?$");
    // regex that matches end of line.
    private static final Pattern END_OF_LINE = Pattern.compile("\r?\n");

    public EmMimeMessage() {
        mHeader = null;
    }

    /**
     * Generate a local message id.  This is only used when none has been assigned, and is
     * installed lazily.  Any remote (typically server-assigned) message id takes precedence.
     * @return a long, locally-generated message-ID value
     */
    private String generateMessageId() {
        StringBuffer sb = new StringBuffer();
        sb.append("<");
        for (int i = 0; i < 24; i++) {
            // We'll use a 5-bit range (0..31)
            int value = sRandom.nextInt() & 31;
            char c = "0123456789abcdefghijklmnopqrstuv".charAt(value);
            sb.append(c);
        }
        sb.append(".");
        sb.append(Long.toString(System.currentTimeMillis()));
        sb.append("@email.android.com>");
        return sb.toString();
    }

    /**
     * Parse the given InputStream using Apache Mime4J to build a MimeMessage.
     *
     * @param in
     * @throws IOException
     * @throws EmMessagingException
     */
    public EmMimeMessage(InputStream in) throws IOException, EmMessagingException {
        parse(in);
    }

    protected void parse(InputStream in) throws IOException, EmMessagingException {
        // Before parsing the input stream, clear all local fields that may be superceded by
        // the new incoming message.
        getMimeHeaders().clear();
        mInhibitLocalMessageId = true;
        mFrom = null;
        mTo = null;
        mCc = null;
        mBcc = null;
        mReplyTo = null;
        mSentDate = null;
        mBody = null;

        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new MimeMessageBuilder());
        parser.parse(new EOLConvertingInputStream(in));
    }

    /**
     * Return the internal mHeader value, with very lazy initialization.
     * The goal is to save memory by not creating the headers until needed.
     */
    private EmMimeHeader getMimeHeaders() {
        if (mHeader == null) {
            mHeader = new EmMimeHeader();
        }
        return mHeader;
    }

    @Override
    public Date getReceivedDate() throws EmMessagingException {
        return null;
    }

    @Override
    public Date getSentDate() throws EmMessagingException {
        if (mSentDate == null) {
            try {
                DateTimeField field = (DateTimeField)Field.parse("Date: "
                        + EmMimeUtility.unfoldAndDecode(getFirstHeader("Date")));
                mSentDate = field.getDate();
            } catch (Exception e) {

            }
        }
        return mSentDate;
    }

    @Override
    public void setSentDate(Date sentDate) throws EmMessagingException {
        setHeader("Date", DATE_FORMAT.format(sentDate));
        this.mSentDate = sentDate;
    }

    @Override
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

    public int getSize() throws EmMessagingException {
        return mSize;
    }

    /**
     * Returns a list of the given recipient type from this message. If no addresses are
     * found the method returns an empty array.
     */
    @Override
    public EmAddress[] getRecipients(RecipientType type) throws EmMessagingException {
        if (type == RecipientType.TO) {
            if (mTo == null) {
                mTo = EmAddress.parse(EmMimeUtility.unfold(getFirstHeader("To")));
            }
            return mTo;
        } else if (type == RecipientType.CC) {
            if (mCc == null) {
                mCc = EmAddress.parse(EmMimeUtility.unfold(getFirstHeader("CC")));
            }
            return mCc;
        } else if (type == RecipientType.BCC) {
            if (mBcc == null) {
                mBcc = EmAddress.parse(EmMimeUtility.unfold(getFirstHeader("BCC")));
            }
            return mBcc;
        } else {
            throw new EmMessagingException("Unrecognized recipient type.");
        }
    }

    @Override
    public void setRecipients(RecipientType type, EmAddress[] addresses) throws EmMessagingException {
        final int TO_LENGTH = 4;  // "To: "
        final int CC_LENGTH = 4;  // "Cc: "
        final int BCC_LENGTH = 5; // "Bcc: "
        if (type == RecipientType.TO) {
            if (addresses == null || addresses.length == 0) {
                removeHeader("To");
                this.mTo = null;
            } else {
                setHeader("To", EmMimeUtility.fold(EmAddress.toHeader(addresses), TO_LENGTH));
                this.mTo = addresses;
            }
        } else if (type == RecipientType.CC) {
            if (addresses == null || addresses.length == 0) {
                removeHeader("CC");
                this.mCc = null;
            } else {
                setHeader("CC", EmMimeUtility.fold(EmAddress.toHeader(addresses), CC_LENGTH));
                this.mCc = addresses;
            }
        } else if (type == RecipientType.BCC) {
            if (addresses == null || addresses.length == 0) {
                removeHeader("BCC");
                this.mBcc = null;
            } else {
                setHeader("BCC", EmMimeUtility.fold(EmAddress.toHeader(addresses), BCC_LENGTH));
                this.mBcc = addresses;
            }
        } else {
            throw new EmMessagingException("Unrecognized recipient type.");
        }
    }

    /**
     * Returns the unfolded, decoded value of the Subject header.
     */
    @Override
    public String getSubject() throws EmMessagingException {
        return EmMimeUtility.unfoldAndDecode(getFirstHeader("Subject"));
    }

    @Override
    public void setSubject(String subject) throws EmMessagingException {
        final int HEADER_NAME_LENGTH = 9;     // "Subject: "
        setHeader("Subject", EmMimeUtility.foldAndEncode2(subject, HEADER_NAME_LENGTH));
    }

    @Override
    public EmAddress[] getFrom() throws EmMessagingException {
        if (mFrom == null) {
            String list = EmMimeUtility.unfold(getFirstHeader("From"));
            if (list == null || list.length() == 0) {
                list = EmMimeUtility.unfold(getFirstHeader("Sender"));
            }
            mFrom = EmAddress.parse(list);
        }
        return mFrom;
    }

    @Override
    public void setFrom(EmAddress from) throws EmMessagingException {
        final int FROM_LENGTH = 6;  // "From: "
        if (from != null) {
            setHeader("From", EmMimeUtility.fold(from.toHeader(), FROM_LENGTH));
            this.mFrom = new EmAddress[] {
                    from
                };
        } else {
            this.mFrom = null;
        }
    }

    @Override
    public EmAddress[] getReplyTo() throws EmMessagingException {
        if (mReplyTo == null) {
            mReplyTo = EmAddress.parse(EmMimeUtility.unfold(getFirstHeader("Reply-to")));
        }
        return mReplyTo;
    }

    @Override
    public void setReplyTo(EmAddress[] replyTo) throws EmMessagingException {
        final int REPLY_TO_LENGTH = 10;  // "Reply-to: "
        if (replyTo == null || replyTo.length == 0) {
            removeHeader("Reply-to");
            mReplyTo = null;
        } else {
            setHeader("Reply-to", EmMimeUtility.fold(EmAddress.toHeader(replyTo), REPLY_TO_LENGTH));
            mReplyTo = replyTo;
        }
    }
    
    /**
     * Set the mime "Message-ID" header
     * @param messageId the new Message-ID value
     * @throws EmMessagingException
     */
    @Override
    public void setMessageId(String messageId) throws EmMessagingException {
        setHeader("Message-ID", messageId);
    }
    
    /**
     * Get the mime "Message-ID" header.  This value will be preloaded with a locally-generated
     * random ID, if the value has not previously been set.  Local generation can be inhibited/
     * overridden by explicitly clearing the headers, removing the message-id header, etc.
     * @return the Message-ID header string, or null if explicitly has been set to null
     */
    @Override
    public String getMessageId() throws EmMessagingException {
        String messageId = getFirstHeader("Message-ID");
        if (messageId == null && !mInhibitLocalMessageId) {
            messageId = generateMessageId();
            setMessageId(messageId);
        }
        return messageId;
    }

    @Override
    public void saveChanges() throws EmMessagingException {
        throw new EmMessagingException("saveChanges not yet implemented");
    }

    @Override
    public EmBody getBody() throws EmMessagingException {
        return mBody;
    }

    @Override
    public void setBody(EmBody body) throws EmMessagingException {
        this.mBody = body;
        if (body instanceof com.cognizant.trumobi.em.mail.EmMultipart) {
            com.cognizant.trumobi.em.mail.EmMultipart multipart = ((com.cognizant.trumobi.em.mail.EmMultipart)body);
            multipart.setParent(this);
            setHeader(EmMimeHeader.HEADER_CONTENT_TYPE, multipart.getContentType());
            setHeader("MIME-Version", "1.0");
        }
        else if (body instanceof EmTextBody) {
            setHeader(EmMimeHeader.HEADER_CONTENT_TYPE, String.format("%s;\n charset=utf-8",
                    getMimeType()));
            setHeader(EmMimeHeader.HEADER_CONTENT_TRANSFER_ENCODING, "base64");
        }
    }

    protected String getFirstHeader(String name) throws EmMessagingException {
        return getMimeHeaders().getFirstHeader(name);
    }

    @Override
    public void addHeader(String name, String value) throws EmMessagingException {
        getMimeHeaders().addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) throws EmMessagingException {
        getMimeHeaders().setHeader(name, value);
    }

    @Override
    public String[] getHeader(String name) throws EmMessagingException {
        return getMimeHeaders().getHeader(name);
    }

    @Override
    public void removeHeader(String name) throws EmMessagingException {
        getMimeHeaders().removeHeader(name);
        if ("Message-ID".equalsIgnoreCase(name)) {
            mInhibitLocalMessageId = true;
        }
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
     * Set entire extended headers from String
     * 
     * @param headers Extended header and its value - "CR-NL-separated pairs
     * if null or empty, remove entire extended headers
     * @throws EmMessagingException
     */
    public void setExtendedHeaders(String headers) throws EmMessagingException {
        if (TextUtils.isEmpty(headers)) {
            mExtendedHeader = null;
        } else {
            mExtendedHeader = new EmMimeHeader();
            for (String header : END_OF_LINE.split(headers)) {
                String[] tokens = header.split(":", 2);
                if (tokens.length != 2) {
                    throw new EmMessagingException("Illegal extended headers: " + headers);
                }
                mExtendedHeader.setHeader(tokens[0].trim(), tokens[1].trim());
            }
        }
    }

    /**
     * Get entire extended headers as String
     * 
     * @return "CR-NL-separated extended headers - null if extended header does not exist
     */
    public String getExtendedHeaders() {
        if (mExtendedHeader != null) {
            return mExtendedHeader.writeToString();
        }
        return null;
    }

    /**
     * Write message header and body to output stream
     * 
     * @param out Output steam to write message header and body.
     */
    public void writeTo(OutputStream out) throws IOException, EmMessagingException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out), 1024);
        // Force creation of local message-id
        getMessageId();
        getMimeHeaders().writeTo(out);
        // mExtendedHeader will not be write out to external output stream,
        // because it is intended to internal use.
        writer.write("\r\n");
        writer.flush();
        if (mBody != null) {
            mBody.writeTo(out);
        }
    }

    public InputStream getInputStream() throws EmMessagingException {
        return null;
    }

    class MimeMessageBuilder implements ContentHandler {
        private Stack stack = new Stack();

        public MimeMessageBuilder() {
        }

        private void expect(Class c) {
            if (!c.isInstance(stack.peek())) {
                throw new IllegalStateException("Internal stack error: " + "Expected '"
                        + c.getName() + "' found '" + stack.peek().getClass().getName() + "'");
            }
        }

        public void startMessage() {
            if (stack.isEmpty()) {
                stack.push(EmMimeMessage.this);
            } else {
                expect(EmPart.class);
                try {
                    EmMimeMessage m = new EmMimeMessage();
                    ((EmPart)stack.peek()).setBody(m);
                    stack.push(m);
                } catch (EmMessagingException me) {
                    throw new Error(me);
                }
            }
        }

        public void endMessage() {
            expect(EmMimeMessage.class);
            stack.pop();
        }

        public void startHeader() {
            expect(EmPart.class);
        }

        public void field(String fieldData) {
            expect(EmPart.class);
            try {
                String[] tokens = fieldData.split(":", 2);
                ((EmPart)stack.peek()).addHeader(tokens[0], tokens[1].trim());
            } catch (EmMessagingException me) {
                throw new Error(me);
            }
        }

        public void endHeader() {
            expect(EmPart.class);
        }

        public void startMultipart(BodyDescriptor bd) {
            expect(EmPart.class);

            EmPart e = (EmPart)stack.peek();
            try {
                EmMimeMultipart multiPart = new EmMimeMultipart(e.getContentType());
                e.setBody(multiPart);
                stack.push(multiPart);
            } catch (EmMessagingException me) {
                throw new Error(me);
            }
        }

        public void body(BodyDescriptor bd, InputStream in) throws IOException {
            expect(EmPart.class);
            EmBody body = EmMimeUtility.decodeBody(in, bd.getTransferEncoding());
            try {
                ((EmPart)stack.peek()).setBody(body);
            } catch (EmMessagingException me) {
                throw new Error(me);
            }
        }

        public void endMultipart() {
            stack.pop();
        }

        public void startBodyPart() {
            expect(EmMimeMultipart.class);

            try {
                EmMimeBodyPart bodyPart = new EmMimeBodyPart();
                ((EmMimeMultipart)stack.peek()).addBodyPart(bodyPart);
                stack.push(bodyPart);
            } catch (EmMessagingException me) {
                throw new Error(me);
            }
        }

        public void endBodyPart() {
            expect(EmBodyPart.class);
            stack.pop();
        }

        public void epilogue(InputStream is) throws IOException {
            expect(EmMimeMultipart.class);
            StringBuffer sb = new StringBuffer();
            int b;
            while ((b = is.read()) != -1) {
                sb.append((char)b);
            }
            // ((Multipart) stack.peek()).setEpilogue(sb.toString());
        }

        public void preamble(InputStream is) throws IOException {
            expect(EmMimeMultipart.class);
            StringBuffer sb = new StringBuffer();
            int b;
            while ((b = is.read()) != -1) {
                sb.append((char)b);
            }
            try {
                ((EmMimeMultipart)stack.peek()).setPreamble(sb.toString());
            } catch (EmMessagingException me) {
                throw new Error(me);
            }
        }

        public void raw(InputStream is) throws IOException {
            throw new UnsupportedOperationException("Not supported");
        }
    }
}