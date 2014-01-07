

package com.cognizant.trumobi.em;

import com.cognizant.trumobi.em.mail.EmAddress;

import android.widget.AutoCompleteTextView.Validator;

public class EmEmailAddressValidator implements Validator {
    public CharSequence fixText(CharSequence invalidText) {
        return "";
    }

    public boolean isValid(CharSequence text) {
        return EmAddress.parse(text.toString()).length > 0;
    }
}
