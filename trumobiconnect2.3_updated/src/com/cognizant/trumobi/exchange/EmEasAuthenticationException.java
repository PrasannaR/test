

package com.cognizant.trumobi.exchange;

import java.io.IOException;

/**
 * Use this to be able to distinguish login (authentication) failures from other I/O
 * exceptions during a sync, as they are handled very differently.
 */
public class EmEasAuthenticationException extends IOException {
    private static final long serialVersionUID = 1L;

    EmEasAuthenticationException() {
        super();
    }
}
