

package com.cognizant.trumobi.em.mail.transport;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A simple OutputStream that does nothing but count how many bytes are written to it and
 * makes that count available to callers.
 */
public class EmCountingOutputStream extends OutputStream {
    private long mCount;

    public EmCountingOutputStream() {
    }

    public long getCount() {
        return mCount;
    }

    @Override
    public void write(int oneByte) throws IOException {
        mCount++;
    }
}
