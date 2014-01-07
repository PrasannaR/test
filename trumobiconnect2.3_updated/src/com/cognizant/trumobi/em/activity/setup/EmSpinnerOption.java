

package com.cognizant.trumobi.em.activity.setup;

import android.widget.Spinner;

public class EmSpinnerOption {
    public final Object value;

    public final String label;

    public static void setSpinnerOptionValue(Spinner spinner, Object value) {
        for (int i = 0, count = spinner.getCount(); i < count; i++) {
            EmSpinnerOption so = (EmSpinnerOption)spinner.getItemAtPosition(i);
            if (so.value.equals(value)) {
                spinner.setSelection(i, true);
                return;
            }
        }
    }

    public EmSpinnerOption(Object value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
