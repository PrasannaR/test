

package com.cognizant.trumobi.em.mail.transport;

import com.cognizant.trumobi.em.Email;

import android.util.Log;

import java.util.ArrayList;

/**
 * A class to keep last N of lines sent to the server and responses received from the server.
 * They are sent to logcat when {@link #logLastDiscourse} is called.
 *
 * <p>This class is used to log the recent network activities when a response parser crashes.
 */
public class EmDiscourseLogger {
    private final int mBufferSize;
    private String[] mBuffer;
    private int mPos;
    private final StringBuilder mReceivingLine = new StringBuilder(100);

    public EmDiscourseLogger(int bufferSize) {
        mBufferSize = bufferSize;
        initBuffer();
    }

    private void initBuffer() {
        mBuffer = new String[mBufferSize];
    }

    /** Add a single line to {@link #mBuffer}. */
    private void addLine(String s) {
        mBuffer[mPos] = s;
        mPos++;
        if (mPos >= mBufferSize) {
            mPos = 0;
        }
    }

    private void addReceivingLineToBuffer() {
        if (mReceivingLine.length() > 0) {
            addLine(mReceivingLine.toString());
            mReceivingLine.delete(0, Integer.MAX_VALUE);
        }
    }

    /**
     * Store a single byte received from the server in {@link #mReceivingLine}.  When LF is
     * received, the content of {@link #mReceivingLine} is added to {@link #mBuffer}.
     */
    public void addReceivedByte(int b) {
        if (0x20 <= b && b <= 0x7e) { // Append only printable ASCII chars.
            mReceivingLine.append((char) b);
        } else if (b == '\n') { // LF
            addReceivingLineToBuffer();
        } else if (b == '\r') { // CR
        } else {
            final String hex = "00" + Integer.toHexString(b);
            mReceivingLine.append("\\x" + hex.substring(hex.length() - 2, hex.length()));
        }
    }

    /** Add a line sent to the server to {@link #mBuffer}. */
    public void addSentCommand(String command) {
        addLine(command);
    }

    /** @return the contents of {@link #mBuffer} as a String array. */
    /* package for testing */ String[] getLines() {
        addReceivingLineToBuffer();

        ArrayList<String> list = new ArrayList<String>();

        final int start = mPos;
        int pos = mPos;
        do {
            String s = mBuffer[pos];
            if (s != null) {
                list.add(s);
            }
            pos = (pos + 1) % mBufferSize;
        } while (pos != start);

        String[] ret = new String[list.size()];
        list.toArray(ret);
        return ret;
    }

    /**
     * Log the contents of the {@link mBuffer}, and clears it out.  (So it's okay to call this
     * method successively more than once.  There will be no duplicate log.)
     */
    public void logLastDiscourse() {
        String[] lines = getLines();
        if (lines.length == 0) {
            return;
        }

        Log.w(Email.LOG_TAG, "Last network activities:");
        for (String r : getLines()) {
            Log.w(Email.LOG_TAG, r);
        }
        initBuffer();
    }
}
