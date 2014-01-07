package com.cognizant.trumobi.contacts.adapter;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.cognizant.trumobi.log.ContactsLog;

public class ContactsViewPager extends ViewPager {
    private boolean swipeable = true;

    public ContactsViewPager(Context context) {
           super(context);
    }

    public ContactsViewPager(Context context, AttributeSet attrs) {
           super(context, attrs);
    }

    // Call this method in your motion events when you want to disable or enable
    // It should work as desired.
    public void setSwipeable(boolean swipeable) {
           this.swipeable = swipeable;

           ContactsLog.d("ContactsViewPager", "setSwipeable " + swipeable);
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
           return (this.swipeable) ? super.onTouchEvent(arg0) : false;
    }

}
