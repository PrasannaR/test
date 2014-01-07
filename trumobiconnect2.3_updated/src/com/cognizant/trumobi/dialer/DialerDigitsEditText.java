package com.cognizant.trumobi.dialer;

import android.content.Context;
import android.graphics.Rect;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText;

public class DialerDigitsEditText extends TruMobBaseEditText {

	public DialerDigitsEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		setInputType(getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		setOnLongClickListener(obj);
	}

	@Override
	protected void onFocusChanged(boolean focused, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		final InputMethodManager imm = ((InputMethodManager) getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE));
		if (imm != null && imm.isActive(this)) {
			imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final boolean ret = super.onTouchEvent(event);

		final InputMethodManager imm = ((InputMethodManager) getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE));
		if (imm != null && imm.isActive(this)) {
			imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
		}

		return ret;
	}

	@Override
	public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {
		if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {

			final int added = event.getAddedCount();
			final int removed = event.getRemovedCount();
			final int length = event.getBeforeText().length();
			if (added > removed) {
				event.setRemovedCount(0);
				event.setAddedCount(1);
				event.setFromIndex(length);
			} else if (removed > added) {
				event.setRemovedCount(1);
				event.setAddedCount(0);
				event.setFromIndex(length - 1);
			} else {
				return;
			}
		} else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {

			return;
		}
		super.sendAccessibilityEventUnchecked(event);
	}

	OnLongClickListener obj = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {

			final InputMethodManager imm = ((InputMethodManager) getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE));
			if (imm != null && imm.isActive(v)) {
				imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
			}

			try {
				EditText custEdit = (EditText) v;

				if (custEdit.getText().length() == custEdit.getSelectionEnd()) {
					return false;
				} else
					return false;
			} catch (Exception e) {

			}
			return false;
		}
	};

}
