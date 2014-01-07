

package com.cognizant.trumobi.exchange;

import com.cognizant.trumobi.em.provider.EmEmailContent.Attachment;

/**
 * PartRequest is the EAS wrapper for attachment loading requests.  In addition to information about
 * the attachment to be loaded, it also contains the callback to be used for status/progress
 * updates to the UI.
 */
public class EmPartRequest extends EmRequest {
    public Attachment mAttachment;
    public String mDestination;
    public String mContentUriString;
    public String mLocation;

    public EmPartRequest(Attachment _att) {
        mMessageId = _att.mMessageKey;
        mAttachment = _att;
        mLocation = mAttachment.mLocation;
    }

    public EmPartRequest(Attachment _att, String _destination, String _contentUriString) {
        this(_att);
        mDestination = _destination;
        mContentUriString = _contentUriString;
    }
}
