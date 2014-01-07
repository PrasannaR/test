

package com.cognizant.trumobi.exchange;

/**
 * Requests for mailbox actions are handled by subclasses of this abstract class.
 * Two subclasses are now defined: PartRequest (attachment load) and MeetingResponseRequest
 * (respond to a meeting invitation)
 */
public abstract class EmRequest {
    public long mTimeStamp = System.currentTimeMillis();
    public long mMessageId;
}
