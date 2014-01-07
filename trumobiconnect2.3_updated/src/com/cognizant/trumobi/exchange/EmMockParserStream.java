

package com.cognizant.trumobi.exchange;

import java.io.IOException;
import java.io.InputStream;

/**
 * MockParserStream is an InputStream that feeds pre-generated data into various EasParser
 * subclasses.
 * 
 * After parsing is done, the result can be obtained with getResult
 *
 */
public class EmMockParserStream extends InputStream {
    int[] array;
    int pos = 0;
    Object value;

    EmMockParserStream (int[] _array) {
        array = _array;
    }

    @Override
    public int read() throws IOException {
        try {
            return array[pos++];
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("End of stream");
        }
    }

    public void setResult(Object _value) {
        value = _value;
    }

    public Object getResult() {
        return value;
    }
}
