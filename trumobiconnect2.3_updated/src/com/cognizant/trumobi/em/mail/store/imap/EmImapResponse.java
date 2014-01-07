

package com.cognizant.trumobi.em.mail.store.imap;


/**
 * Class represents an IMAP response.
 */
public class EmImapResponse extends EmImapList {
    private final String mTag;
    private final boolean mIsContinuationRequest;

    /* package */ EmImapResponse(String tag, boolean isContinuationRequest) {
        mTag = tag;
        mIsContinuationRequest = isContinuationRequest;
    }

    /* package */ static boolean isStatusResponse(String symbol) {
        return     EmImapConstants.OK.equalsIgnoreCase(symbol)
                || EmImapConstants.NO.equalsIgnoreCase(symbol)
                || EmImapConstants.BAD.equalsIgnoreCase(symbol)
                || EmImapConstants.PREAUTH.equalsIgnoreCase(symbol)
                || EmImapConstants.BYE.equalsIgnoreCase(symbol);
    }

    /**
     * @return whether it's a tagged response.
     */
    public boolean isTagged() {
        return mTag != null;
    }

    /**
     * @return whether it's a continuation request.
     */
    public boolean isContinuationRequest() {
        return mIsContinuationRequest;
    }

    public boolean isStatusResponse() {
        return isStatusResponse(getStringOrEmpty(0).getString());
    }

    /**
     * @return whether it's an OK response.
     */
    public boolean isOk() {
        return is(0, EmImapConstants.OK);
    }

    /**
     * @return whether it's an {@code responseType} data response.  (i.e. not tagged).
     * @param index where {@code responseType} should appear.  e.g. 1 for "FETCH"
     * @param responseType e.g. "FETCH"
     */
    public final boolean isDataResponse(int index, String responseType) {
        return !isTagged() && getStringOrEmpty(index).is(responseType);
    }

    /**
     * @return Response code (RFC 3501 7.1) if it's a status response.
     *
     * e.g. "ALERT" for "* OK [ALERT] System shutdown in 10 minutes"
     */
    public EmImapString getResponseCodeOrEmpty() {
        if (!isStatusResponse()) {
            return EmImapString.EMPTY; // Not a status response.
        }
        return getListOrEmpty(1).getStringOrEmpty(0);
    }

    /**
     * @return Alert message it it has ALERT response code.
     *
     * e.g. "System shutdown in 10 minutes" for "* OK [ALERT] System shutdown in 10 minutes"
     */
    public EmImapString getAlertTextOrEmpty() {
        if (!getResponseCodeOrEmpty().is(EmImapConstants.ALERT)) {
            return EmImapString.EMPTY; // Not an ALERT
        }
        // The 3rd element contains all the rest of line.
        return getStringOrEmpty(2);
    }

    /**
     * @return Response text in a status response.
     */
    public EmImapString getStatusResponseTextOrEmpty() {
        if (!isStatusResponse()) {
            return EmImapString.EMPTY;
        }
        return getStringOrEmpty(getElementOrNone(1).isList() ? 2 : 1);
    }

    @Override
    public String toString() {
        String tag = mTag;
        if (isContinuationRequest()) {
            tag = "+";
        }
        return "#" + tag + "# " + super.toString();
    }

    @Override
    public boolean equalsForTest(EmImapElement that) {
        if (!super.equalsForTest(that)) {
            return false;
        }
        final EmImapResponse thatResponse = (EmImapResponse) that;
        if (mTag == null) {
            if (thatResponse.mTag != null) {
                return false;
            }
        } else {
            if (!mTag.equals(thatResponse.mTag)) {
                return false;
            }
        }
        if (mIsContinuationRequest != thatResponse.mIsContinuationRequest) {
            return false;
        }
        return true;
    }
}
