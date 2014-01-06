package com.cognizant.trumobi.calendar.view;

import android.content.ContentValues;
import android.graphics.RectF;

public class CalendarEventRect {
	RectF mRect;
	ContentValues mContentValues;

	public CalendarEventRect(RectF rectdraw, ContentValues contentValues) {
		this.mRect = rectdraw;
		this.mContentValues = contentValues;
	}

	public RectF getRect() {
		return this.mRect;
	}

	public ContentValues getContentValues() {
		return this.mContentValues;
	}

}
