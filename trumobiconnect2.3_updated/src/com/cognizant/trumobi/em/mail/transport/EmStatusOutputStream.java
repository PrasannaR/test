

package com.cognizant.trumobi.em.mail.transport;

import com.cognizant.trumobi.em.Email;

import android.util.Log;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class EmStatusOutputStream extends FilterOutputStream {
    private long mCount = 0;
    
    public EmStatusOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int oneByte) throws IOException {
        super.write(oneByte);
        mCount++;
        if (Email.LOGD) {
            if (mCount % 1024 == 0) {
                Log.v(Email.LOG_TAG, "# " + mCount);
            }
        }
    }
}
