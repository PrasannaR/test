

package com.cognizant.trumobi.em.mail;

import java.util.ArrayList;

public abstract class EmMultipart implements EmBody {
    protected EmPart mParent;

    protected ArrayList<EmBodyPart> mParts = new ArrayList<EmBodyPart>();

    protected String mContentType;

    public void addBodyPart(EmBodyPart part) throws EmMessagingException {
        mParts.add(part);
    }

    public void addBodyPart(EmBodyPart part, int index) throws EmMessagingException {
        mParts.add(index, part);
    }

    public EmBodyPart getBodyPart(int index) throws EmMessagingException {
        return mParts.get(index);
    }

    public String getContentType() throws EmMessagingException {
        return mContentType;
    }

    public int getCount() throws EmMessagingException {
        return mParts.size();
    }

    public boolean removeBodyPart(EmBodyPart part) throws EmMessagingException {
        return mParts.remove(part);
    }

    public void removeBodyPart(int index) throws EmMessagingException {
        mParts.remove(index);
    }

    public EmPart getParent() throws EmMessagingException {
        return mParent;
    }

    public void setParent(EmPart parent) throws EmMessagingException {
        this.mParent = parent;
    }
}
