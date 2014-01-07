

package com.cognizant.trumobi.exchange;

public class EmIllegalHeartbeatException extends EmEasException {
    private static final long serialVersionUID = 1L;
    public final int mLegalHeartbeat;

    public EmIllegalHeartbeatException(int legalHeartbeat) {
        mLegalHeartbeat = legalHeartbeat;
    }
}
