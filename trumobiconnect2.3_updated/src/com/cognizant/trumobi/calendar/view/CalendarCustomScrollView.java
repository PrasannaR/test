package com.cognizant.trumobi.calendar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class CalendarCustomScrollView extends ScrollView {

	public CalendarCustomScrollView(Context context) {
		super(context);
		this.setOverScrollMode(OVER_SCROLL_NEVER);
	}

	public CalendarCustomScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOverScrollMode(OVER_SCROLL_NEVER);
	}

	public CalendarCustomScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.setOverScrollMode(OVER_SCROLL_NEVER);
	}

	public interface CalendarCustomScrollViewListener {
		void onScrollChanged(CalendarCustomScrollView v, int l, int t,
				int oldl, int oldt);
	}

	private CalendarCustomScrollViewListener mOnScrollViewListener;

	public void setOnScrollViewListener(CalendarCustomScrollViewListener l) {
		this.mOnScrollViewListener = l;
	}

	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		try {
			mOnScrollViewListener.onScrollChanged(this, l, t, oldl, oldt);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		super.onScrollChanged(l, t, oldl, oldt);
	}

}
