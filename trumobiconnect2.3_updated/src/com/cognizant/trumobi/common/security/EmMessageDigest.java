

package com.cognizant.trumobi.common.security;

import java.security.NoSuchAlgorithmException;

/**
 * Base class for producing a message digest from different hash encryptions.
 */
public abstract class EmMessageDigest 
{    
    /**
     * Returns a digest object of the specified type.
     * 
     * @param algorithm  The type of hash function to use. Valid values are
     *                   <em>SHA-1</em> and <em>MD5</em>.
     * @return The respective MessageDigest object. Either a 
     *         {@link com.cognizant.trumobi.common.security.EmSha1MessageDigest} or
     *         {@link com.cognizant.trumobi.common.security.EmMd5MessageDigest} object.
     * @throws NoSuchAlgorithmException If an invalid <var>algorithm</var>
     *                                  is given.
     */
    public static EmMessageDigest getInstance(String algorithm) 
        throws NoSuchAlgorithmException
    {
        if (algorithm == null) {
            return null;
        }
        
        if (algorithm.equals("SHA-1")) {
            return new EmSha1MessageDigest();
        }
        else if (algorithm.equals("MD5")) {
            return new EmMd5MessageDigest();
        }
        
        throw new NoSuchAlgorithmException();
    }
    
    public abstract void update(byte[] input);    
    public abstract byte[] digest();
    
    /**
     * Produces a message digest for the given input.
     * 
     * @param input  The message to encrypt.
     * @return The digest (hash sum).
     */
    public abstract byte[] digest(byte[] input);
}
