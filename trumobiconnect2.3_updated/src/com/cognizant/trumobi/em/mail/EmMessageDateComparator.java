

package com.cognizant.trumobi.em.mail;

import java.util.Comparator;

public class EmMessageDateComparator implements Comparator<EmMessage> {
    public int compare(EmMessage o1, EmMessage o2) {
        try {
            if (o1.getSentDate() == null) {
                return 1;
            } else if (o2.getSentDate() == null) {
                return -1;
            } else
                return o2.getSentDate().compareTo(o1.getSentDate());
        } catch (Exception e) {
            return 0;
        }
    }
}
