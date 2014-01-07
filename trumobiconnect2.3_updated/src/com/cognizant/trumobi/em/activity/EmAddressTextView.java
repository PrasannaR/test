

package com.cognizant.trumobi.em.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.MultiAutoCompleteTextView;

import com.cognizant.trumobi.R;

/**
 * This is a MultiAutoCompleteTextView which sets the error state (@see
 * TextView.setError) when email address validation fails.
 */
class EmAddressTextView extends MultiAutoCompleteTextView {
	private class ForwardValidator implements Validator {
		private Validator mValidator = null;

		public CharSequence fixText(CharSequence invalidText) {
			mIsValid = false;
			return invalidText;
		}

		public boolean isValid(CharSequence text) {
			return mValidator != null ? mValidator.isValid(text) : true;
		}

		public void setValidator(Validator validator) {
			mValidator = validator;
		}
	}

	private boolean mIsValid = true;
	private final ForwardValidator mInternalValidator = new ForwardValidator();

	public EmAddressTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		super.setValidator(mInternalValidator);
	}

	@Override
	public void setValidator(Validator validator) {
		mInternalValidator.setValidator(validator);
	}

	@Override
	public void performValidation() {
		mIsValid = true;
		super.performValidation();
		markError(!mIsValid);
	}

	private void markError(boolean enable) {
		if (enable) {
			setError(getContext().getString(
					R.string.message_compose_error_invalid_email));
		} else {
			setError(null);
		}
	}
}
