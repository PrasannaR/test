

package com.cognizant.trumobi.common.security;

/**
 * Provides the MD5 hash encryption.
 */
public class EmMd5MessageDigest extends EmMessageDigest
{
    // ptr to native context
    private int mNativeMd5Context;
    
    public EmMd5MessageDigest()
    {
        init();
    }
    
    public byte[] digest(byte[] input)
    {
        update(input);
        return digest();
    }

    private native void init();
    public native void update(byte[] input);  
    public native byte[] digest();
    native public void reset();
}
