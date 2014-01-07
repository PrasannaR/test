

package com.cognizant.trumobi.exchange.adapter;

import com.cognizant.trumobi.exchange.EmEasSyncService;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parse the result of a MeetingRequest command.
 */
public class EmMeetingResponseParser extends EmParser {
    private EmEasSyncService mService;

    public EmMeetingResponseParser(InputStream in, EmEasSyncService service) throws IOException {
        super(in);
        mService = service;
    }

    public void parseResult() throws IOException {
        while (nextTag(EmTags.MREQ_RESULT) != END) {
            if (tag == EmTags.MREQ_STATUS) {
                int status = getValueInt();
                if (status != 1) {
                    mService.userLog("Error in meeting response: " + status);
                }
            } else if (tag == EmTags.MREQ_CAL_ID) {
                mService.userLog("Meeting response calendar id: " + getValue());
            } else {
                skipTag();
            }
        }
    }

    @Override
    public boolean parse() throws IOException {
        boolean res = false;
        if (nextTag(START_DOCUMENT) != EmTags.MREQ_MEETING_RESPONSE) {
            throw new IOException();
        }
        while (nextTag(START_DOCUMENT) != END_DOCUMENT) {
            if (tag == EmTags.MREQ_RESULT) {
                parseResult();
            } else {
                skipTag();
            }
        }
        return res;
    }
}

