

package com.cognizant.trumobi.exchange.adapter;

import com.cognizant.trumobi.exchange.EmEas;
import com.cognizant.trumobi.exchange.utility.EmFileLogger;

import android.content.ContentValues;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

public class EmSerializer {

    private static final String TAG = "Serializer";
    private boolean logging = true;    // DO NOT CHECK IN WITH THIS TRUE!

    private static final int NOT_PENDING = -1;
    private static final int BUFFER_SIZE = 16 * 1024;

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream buf = new ByteArrayOutputStream();

    String pending;
    int pendingTag = NOT_PENDING;
    int depth;
    String name;
    String[] nameStack = new String[20];

    Hashtable<String, Object> tagTable = new Hashtable<String, Object>();

    private int tagPage;

    public EmSerializer() {
        this(true);
    }

    public EmSerializer(boolean startDocument, boolean _logging) {
        this(true);
        logging = _logging;
    }

    public EmSerializer(boolean startDocument) {
        super();
        if (startDocument) {
            try {
                startDocument();
                //logging = Eas.PARSER_LOG;
            } catch (IOException e) {
                // Nothing to be done
            }
        } else {
            out.write(0);
        }
    }

    void log(String str) {
        int cr = str.indexOf('\n');
        if (cr > 0) {
            str = str.substring(0, cr);
        }
        Log.v(TAG, str);
        if (EmEas.FILE_LOG) {
            EmFileLogger.log(TAG, str);
        }
    }

    public void done() throws IOException {
        if (depth != 0) {
            throw new IOException("Done received with unclosed tags");
        }
        writeInteger(out, 0);
        out.write(buf.toByteArray());
        out.flush();
    }

    public void startDocument() throws IOException{
        out.write(0x03); // version 1.3
        out.write(0x01); // unknown or missing public identifier
        out.write(106);
    }

    public void checkPendingTag(boolean degenerated) throws IOException {
        if (pendingTag == NOT_PENDING)
            return;

        int page = pendingTag >> EmTags.PAGE_SHIFT;
        int tag = pendingTag & EmTags.PAGE_MASK;
        if (page != tagPage) {
            tagPage = page;
            buf.write(EmWbxml.SWITCH_PAGE);
            buf.write(page);
        }

        buf.write(degenerated ? tag : tag | 64);
        if (logging) {
            String name = EmTags.pages[page][tag - 5];
            nameStack[depth] = name;
            log("<" + name + '>');
        }
        pendingTag = NOT_PENDING;
    }

    public EmSerializer start(int tag) throws IOException {
        checkPendingTag(false);
        pendingTag = tag;
        depth++;
        return this;
    }

    public EmSerializer end() throws IOException {
        if (pendingTag >= 0) {
            checkPendingTag(true);
        } else {
            buf.write(EmWbxml.END);
            if (logging) {
                log("</" + nameStack[depth] + '>');
            }
        }
        depth--;
        return this;
    }

    public EmSerializer tag(int t) throws IOException {
        start(t);
        end();
        return this;
    }

    public EmSerializer data(int tag, String value) throws IOException {
        if (value == null) {
            Log.e(TAG, "Writing null data for tag: " + tag);
        }
        start(tag);
        text(value);
        end();
        return this;
    }

    @Override
    public String toString() {
        return out.toString();
    }

    public byte[] toByteArray() {
        return out.toByteArray();
    }

    public EmSerializer text(String text) throws IOException {
        if (text == null) {
            Log.e(TAG, "Writing null text for pending tag: " + pendingTag);
        }
        checkPendingTag(false);
        buf.write(EmWbxml.STR_I);
        writeLiteralString(buf, text);
        if (logging) {
            log(text);
        }
        return this;
    }
	public EmSerializer opaque(InputStream is, int length)
			throws IOException {
		checkPendingTag(false);
		out.write(EmWbxml.OPAQUE);
		writeInteger(out, length);
		if (logging) {
			log("Opaque, length: " + length);
		}
		// Now write out the opaque data in batches
		byte[] buffer = new byte[BUFFER_SIZE];
		while (length > 0) {
			int bytesRead = is.read(buffer, 0,
					(int) Math.min(BUFFER_SIZE, length));
			if (bytesRead == -1) {
				break;
			}
			out.write(buffer, 0, bytesRead);
			length -= bytesRead;
		}
		return this;
	}
    void writeInteger(OutputStream out, int i) throws IOException {
        byte[] buf = new byte[5];
        int idx = 0;

        do {
            buf[idx++] = (byte) (i & 0x7f);
            i = i >> 7;
        } while (i != 0);

        while (idx > 1) {
            out.write(buf[--idx] | 0x80);
        }
        out.write(buf[0]);
        if (logging) {
            log(Integer.toString(i));
        }
    }

    void writeLiteralString(OutputStream out, String s) throws IOException {
        byte[] data = s.getBytes("UTF-8");
        out.write(data);
        out.write(0);
    }

    void writeStringValue (ContentValues cv, String key, int tag) throws IOException {
        String value = cv.getAsString(key);
        if (value != null && value.length() > 0) {
            data(tag, value);
        }
    }
}
