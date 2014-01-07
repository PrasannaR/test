

package com.cognizant.trumobi.em.mail;

public abstract class EmBodyPart implements EmPart {
    protected EmMultipart mParent;

    public EmMultipart getParent() {
        return mParent;
    }
}
