package com.cognizant.trumobi.dialer.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;
import com.cognizant.trumobi.dialer.DialerDigitsEditText;
import com.cognizant.trumobi.dialer.DialerParentActivity;
import com.cognizant.trumobi.dialer.utils.DialerPhoneNumberFormatter;
import com.cognizant.trumobi.em.Email;

@SuppressLint("NewApi")
public class DialerDialPadFragment extends SherlockFragment implements
		OnClickListener, OnLongClickListener {

	private DialerDigitsEditText mDigits;
	private ImageButton mDelete;
	private boolean mWasEmptyBeforeTextChange;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		if (!ContactsUtilities.isTablet(Email.getAppContext())) {
			setRetainInstance(true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View dialerView = inflater.inflate(R.layout.dial_dialer, container,
				false);
		dialerView.setFilterTouchesWhenObscured(true);
		setHasOptionsMenu(true);
		mDigits = (DialerDigitsEditText) dialerView.findViewById(R.id.digits);
		mDigits.setKeyListener(DialerKeyListener.getInstance());
		mDigits.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				DialerParentActivity.dialNum = mDigits.getText().toString()
						.trim();

				if (mWasEmptyBeforeTextChange != TextUtils.isEmpty(s)) {
					final SherlockFragmentActivity activity = getSherlockActivity();
					if (activity != null) {
						activity.invalidateOptionsMenu();
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

				mWasEmptyBeforeTextChange = TextUtils.isEmpty(s);
			}

			@Override
			public void afterTextChanged(Editable s) {

				if (isDigitsEmpty()) {
					mDigits.setCursorVisible(false);
				}
			}
		});

		if (DialerParentActivity.dialNum != null) {
			mDigits.setText(DialerParentActivity.dialNum);
			mDigits.setSelection(mDigits.length());
		}

		mDelete = (ImageButton) dialerView.findViewById(R.id.deleteButton);
		mDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// mDigits.setSelection(mDigits.length());
				keyPressed(KeyEvent.KEYCODE_DEL);
			}
		});
		mDelete.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				mDigits.setText("");
				return true;
			}
		});
		DialerPhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(
				getSherlockActivity(), mDigits);
		setUpKeyPad(dialerView);
		// mDigits.setInputType(android.text.InputType.TYPE_CLASS_PHONE);

		mDigits.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isDigitsEmpty())
					mDigits.setCursorVisible(true);
				closePopup();

			}
		});
		
		
		
		return dialerView;
	}

	public void clearText() {
		if (!isDigitsEmpty()) {
			mDigits.setText("");
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	public void closePopup() {

		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mDigits.getWindowToken(), 0);
	}

	private void setUpKeyPad(View fragmentView) {
		View view = fragmentView.findViewById(R.id.one);
		view.setOnClickListener(this);
		fragmentView.findViewById(R.id.two).setOnClickListener(this);
		fragmentView.findViewById(R.id.three).setOnClickListener(this);
		fragmentView.findViewById(R.id.four).setOnClickListener(this);
		fragmentView.findViewById(R.id.five).setOnClickListener(this);
		fragmentView.findViewById(R.id.six).setOnClickListener(this);
		fragmentView.findViewById(R.id.seven).setOnClickListener(this);
		fragmentView.findViewById(R.id.eight).setOnClickListener(this);
		fragmentView.findViewById(R.id.nine).setOnClickListener(this);
		fragmentView.findViewById(R.id.star).setOnClickListener(this);

		view = fragmentView.findViewById(R.id.zero);
		view.setOnClickListener(this);
		view.setOnLongClickListener(this);
		fragmentView.findViewById(R.id.pound).setOnClickListener(this);

	}

	private void keyPressed(int keyCode) {
		KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
		mDigits.onKeyDown(keyCode, event);

		// If the cursor is at the end of the text we hide it.
		final int length = mDigits.length();
		if (length == mDigits.getSelectionStart()
				&& length == mDigits.getSelectionEnd()) {
			mDigits.setCursorVisible(false);
		}

	}

	private boolean isDigitsEmpty() {
		return mDigits.length() == 0;
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.one:
			keyPressed(KeyEvent.KEYCODE_1);
			return;

		case R.id.two: {

			keyPressed(KeyEvent.KEYCODE_2);
			return;
		}
		case R.id.three: {

			keyPressed(KeyEvent.KEYCODE_3);
			return;
		}
		case R.id.four: {

			keyPressed(KeyEvent.KEYCODE_4);
			return;
		}
		case R.id.five: {

			keyPressed(KeyEvent.KEYCODE_5);
			return;
		}
		case R.id.six: {

			keyPressed(KeyEvent.KEYCODE_6);
			return;
		}
		case R.id.seven: {

			keyPressed(KeyEvent.KEYCODE_7);
			return;
		}
		case R.id.eight: {

			keyPressed(KeyEvent.KEYCODE_8);
			return;
		}
		case R.id.nine: {

			keyPressed(KeyEvent.KEYCODE_9);
			return;
		}
		case R.id.zero: {

			keyPressed(KeyEvent.KEYCODE_0);
			return;
		}
		case R.id.pound: {

			keyPressed(KeyEvent.KEYCODE_POUND);
			return;
		}
		case R.id.star: {

			keyPressed(KeyEvent.KEYCODE_STAR);
			return;
		}
		case R.id.deleteButton: {
			keyPressed(KeyEvent.KEYCODE_DEL);
			return;
		}

		case R.id.digits: {

			return;
		}

		}
	}

	@Override
	public void onResume() {

		super.onResume();

	}

	@Override
	public boolean onLongClick(View v) {

		if (v.getId() == R.id.zero) {

			keyPressed(KeyEvent.KEYCODE_PLUS);

		}
		return true;
	}
}
