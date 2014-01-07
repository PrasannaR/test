

package com.cognizant.trumobi.common.security;

/**
 * Provides the SHA-1 hash encyption.
 */
public class EmSha1MessageDigest extends EmMessageDigest
{
    // ptr to native context
    private int mNativeSha1Context;
    
    public EmSha1MessageDigest()
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
