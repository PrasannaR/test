

package com.cognizant.trumobi.em.mail;

import java.util.Date;
import java.util.HashSet;

public abstract class EmMessage implements EmPart, EmBody {
    public static final EmMessage[] EMPTY_ARRAY = new EmMessage[0];

    public enum RecipientType {
        TO, CC, BCC,
    }

    protected String mUid;

    private HashSet<EmFlag> mFlags = null;

    protected Date mInternalDate;

    protected EmFolder mFolder;

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        this.mUid = uid;
    }

    public EmFolder getFolder() {
        return mFolder;
    }

    public abstract String getSubject() throws EmMessagingException;

    public abstract void setSubject(String subject) throws EmMessagingException;

    public Date getInternalDate() {
        return mInternalDate;
    }

    public void setInternalDate(Date internalDate) {
        this.mInternalDate = internalDate;
    }

    public abstract Date getReceivedDate() throws EmMessagingException;

    public abstract Date getSentDate() throws EmMessagingException;

    public abstract void setSentDate(Date sentDate) throws EmMessagingException;

    public abstract EmAddress[] getRecipients(RecipientType type) throws EmMessagingException;

    public abstract void setRecipients(RecipientType type, EmAddress[] addresses)
            throws EmMessagingException;

    public void setRecipient(RecipientType type, EmAddress address) throws EmMessagingException {
        setRecipients(type, new EmAddress[] {
            address
        });
    }

    public abstract EmAddress[] getFrom() throws EmMessagingException;

    public abstract void setFrom(EmAddress from) throws EmMessagingException;

    public abstract EmAddress[] getReplyTo() throws EmMessagingException;

    public abstract void setReplyTo(EmAddress[] from) throws EmMessagingException;

    public abstract EmBody getBody() throws EmMessagingException;

    public abstract String getContentType() throws EmMessagingException;

    public abstract void addHeader(String name, String value) throws EmMessagingException;

    public abstract void setHeader(String name, String value) throws EmMessagingException;

    public abstract String[] getHeader(String name) throws EmMessagingException;

    public abstract void removeHeader(String name) throws EmMessagingException;

    // Always use these instead of getHeader("Message-ID") or setHeader("Message-ID");
    public abstract void setMessageId(String messageId) throws EmMessagingException;
    public abstract String getMessageId() throws EmMessagingException;

    public abstract void setBody(EmBody body) throws EmMessagingException;

    public boolean isMimeType(String mimeType) throws EmMessagingException {
        return getContentType().startsWith(mimeType);
    }

    private HashSet<EmFlag> getFlagSet() {
        if (mFlags == null) {
            mFlags = new HashSet<EmFlag>();
        }
        return mFlags;
    }

    /*
     * TODO Refactor Flags at some point to be able to store user defined flags.
     */
    public EmFlag[] getFlags() {
        return getFlagSet().toArray(new EmFlag[] {});
    }

    /**
     * Set/clear a flag directly, without involving overrides of {@link #setFlag} in subclasses.
     * Only used for testing.
     */
    public final void setFlagDirectlyForTest(EmFlag flag, boolean set) throws EmMessagingException {
        if (set) {
            getFlagSet().add(flag);
        } else {
            getFlagSet().remove(flag);
        }
    }

    public void setFlag(EmFlag flag, boolean set) throws EmMessagingException {
        setFlagDirectlyForTest(flag, set);
    }

    /**
     * This method calls setFlag(Flag, boolean)
     * @param flags
     * @param set
     */
    public void setFlags(EmFlag[] flags, boolean set) throws EmMessagingException {
        for (EmFlag flag : flags) {
            setFlag(flag, set);
        }
    }

    public boolean isSet(EmFlag flag) {
        return getFlagSet().contains(flag);
    }

    public abstract void saveChanges() throws EmMessagingException;

    @Override
    public String toString() {
        return getClass().getSimpleName() + ':' + mUid;
    }
}
