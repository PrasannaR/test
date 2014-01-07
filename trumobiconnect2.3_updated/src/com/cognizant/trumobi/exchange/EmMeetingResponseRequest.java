

package com.cognizant.trumobi.exchange;

/**
 * MeetingResponseRequest is the EAS wrapper for responding to meeting requests.
 */
public class EmMeetingResponseRequest extends EmRequest {
    public int mResponse;

    EmMeetingResponseRequest(long messageId, int response) {
        mMessageId = messageId;
        mResponse = response;
    }
}
