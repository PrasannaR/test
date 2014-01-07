package com.cognizant.trumobi.commonabstractclass;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.cognizant.trumobi.container.Utils.OutlookPreference;

@SuppressLint("NewApi")
public class TruMobBaseEditText extends EditText {

	EditText custEdit = null;
	StyleCallback _callBack = null;
	boolean isLongClicked = false;
	Context mContext;

	public TruMobBaseEditText(Context context) {
		super(context);
		Log.i("EditText", "TruMobBaseEditText 1 ");
		init(context);
	}

	public TruMobBaseEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.i("EditText", "TruMobBaseEditText 2 ");
		init(context);
	}

	public TruMobBaseEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Log.i("EditText", "TruMobBaseEditText 3 ");
		init(context);
	}

	void init(Context _ctx) {

		int policyType = 2;

		switch (policyType) {
		case 0:
		case 1:
		default:
			break;
		case 2:
			
			try{
			int androidOS = VERSION.SDK_INT;
			
			Log.i("androidOS ", "androidOS "+androidOS);
			
			if(androidOS >= 11){
			
			_callBack = new StyleCallback();
			mContext = _ctx;
			setCustomSelectionActionModeCallback(_callBack);
			setOnLongClickListener(obj);
			// setOnFocusChangeListener(focus);
			}
			}catch(Exception e){
				
			}
			break;
		}

	}

	@SuppressWarnings("unchecked")
	public static <T extends View> T getView(View parent, int viewId) {

		Log.i("EditText", "getview ");
		return (T) checkView(parent.findViewById(viewId));
	}

	private static View checkView(View v) {
		Log.i("EditText", "checkview ");

		if (v == null) {
			throw new IllegalArgumentException("View doesn't exist");
		}
		return v;
	}

	OnLongClickListener obj = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {

			try {
				EditText custEdit = (EditText) v;
				int start = custEdit.getSelectionStart();
				int end = custEdit.getSelectionEnd();

				Log.i("EditText", "onLongClick  " + v.getId());

				SpannableStringBuilder ssb = new SpannableStringBuilder(
						custEdit.getText());
				String copied = ssb.subSequence(start, end).toString();

				if (custEdit.getText().length() == end) {
					// startActionMode(new PasteCallback());
					return false;
				} else
					return false;
			} catch (Exception e) {

			}
			return false;
		}
	};

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	class StyleCallback implements ActionMode.Callback {

		EditText custEdit = null;

		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			Log.d("EditText", "onCreateActionMode");
			int id = getId();
			Log.i("EditText", "TruMobBaseEditText create action  " + id);
			custEdit = (EditText) findViewById(id);

			menu.removeItem(android.R.id.paste);
			menu.add("PASTE");

			return true;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			try {
				Log.d("EditText",
						String.format("onActionItemClicked item=%s/%d",
								item.toString(), item.getItemId()));

				int start = custEdit.getSelectionStart();
				int end = custEdit.getSelectionEnd();
				SpannableStringBuilder ssb = new SpannableStringBuilder(
						custEdit.getText());
				String copied = "";

				switch (item.getItemId()) {

				case android.R.id.copy:
					copied = ssb.subSequence(start, end).toString();

					OutlookPreference.getInstance(mContext).setValue(
							"PIMPOLICIES", copied);
					Log.d("EditText", " copied text " + copied);
					onDestroyActionMode(mode);
					break;

				case android.R.id.cut:
					copied = ssb.subSequence(start, end).toString();
					OutlookPreference.getInstance(mContext).setValue(
							"PIMPOLICIES", copied);
					String cut = ssb.delete(start, end).toString().trim();
					custEdit.setText(cut);

					Log.d("EditText", " cut text " + copied);
					onDestroyActionMode(mode);

					break;

				case android.R.id.paste:

					Log.d("EditText", " paste text ");
					copied = ssb.subSequence(start, end).toString();
					String replace = OutlookPreference.getInstance(mContext)
							.getValue("PIMPOLICIES", "");
					ssb.replace(start, end, replace);
					custEdit.setText(ssb.toString().trim());

					int textLength = custEdit.getText().length();
					custEdit.setSelection(textLength, textLength);
					onDestroyActionMode(mode);

					break;

				case android.R.id.selectAll:
					custEdit.setSelection(0, custEdit.getText().length());
					break;

				default:
					Log.d("EditText", " paste text ");
					copied = ssb.subSequence(start, end).toString();
					String replaceCust = OutlookPreference
							.getInstance(mContext).getValue("PIMPOLICIES", "");
					ssb.replace(start, end, replaceCust);
					custEdit.setText(ssb.toString().trim());

					int textLengthCust = custEdit.getText().length();
					custEdit.setSelection(textLengthCust, textLengthCust);

					onDestroyActionMode(mode);

					break;
				}
			} catch (Exception e) {

			}
			return true;
		}

		public void onDestroyActionMode(ActionMode mode) {
			Log.i("EditText", "onDestroyActionMode called ");

		}
	}

	OnFocusChangeListener focus = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {

			try{
			Log.i("EditText", "onFocusChange " + v.getId() + "   " + hasFocus);
			if (hasFocus)
				startActionMode(_callBack);
			}catch(Exception e){
				
			}

		}
	};

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	class PasteCallback implements ActionMode.Callback {

		EditText custEdit = null;

		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			Log.d("EditText", "onCreateActionMode");
			int id = getId();
			Log.i("EditText", "TruMobBaseEditText create action  " + id);
			custEdit = (EditText) findViewById(id);

			menu.add("PASTE");

			return true;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return true;
		}

		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			Log.d("EditText",
					String.format("onActionItemClicked item=%s/%d",
							item.toString(), item.getItemId()));

			int start = custEdit.getSelectionStart();
			int end = custEdit.getSelectionEnd();
			SpannableStringBuilder ssb = new SpannableStringBuilder(
					custEdit.getText());
			String copied = "";

			switch (item.getItemId()) {

			default:
				Log.d("EditText", " paste text ");
				copied = ssb.subSequence(start, end).toString();
				String replaceCust = OutlookPreference.getInstance(mContext)
						.getValue("PIMPOLICIES", "");
				ssb.replace(start, end, replaceCust);
				custEdit.setText(ssb.toString().trim());

				int textLengthCust = custEdit.getText().length();
				custEdit.setSelection(textLengthCust, textLengthCust);

				onDestroyActionMode(mode);

				break;
			}
			return true;
		}

		public void onDestroyActionMode(ActionMode mode) {
			Log.i("EditText", "onDestroyActionMode called ");

		}
	}

	@Override
	public boolean onTextContextMenuItem(int id) {

		try {
			int editid = getId();
			Log.i("EditText", "TruMobBaseEditText create action  " + editid);
			EditText custEdit = (EditText) findViewById(editid);
			int start = custEdit.getSelectionStart();
			int end = custEdit.getSelectionEnd();
			SpannableStringBuilder ssb = new SpannableStringBuilder(
					custEdit.getText());
			String copied = "";
			switch (id) {
			case android.R.id.paste:

				Log.i("EditText", "TruMobBaseEditText create action  "
						+ ssb.toString().trim());
				String replace = OutlookPreference.getInstance(mContext)
						.getValue("PIMPOLICIES", "");
				ssb.replace(start, end, replace);
				Log.i("EditText", "TruMobBaseEditText create action  "
						+ ssb.toString().trim() + "  "
						+ custEdit.getText().toString());
				custEdit.setText(ssb.toString().trim());

				int textLength = custEdit.getText().length();
				custEdit.setSelection(textLength, textLength);

				break;

			case android.R.id.copy:
				copied = ssb.subSequence(start, end).toString();

				OutlookPreference.getInstance(mContext).setValue("PIMPOLICIES",
						copied);
				Log.d("EditText", " copied text " + copied);
				break;

			case android.R.id.cut:
				copied = ssb.subSequence(start, end).toString();
				OutlookPreference.getInstance(mContext).setValue("PIMPOLICIES",
						copied);
				String cut = ssb.delete(start, end).toString().trim();
				custEdit.setText(cut);

				Log.d("EditText", " cut text " + copied);

				break;

			case android.R.id.selectAll:
				custEdit.setSelection(0, custEdit.getText().length());
				break;

			default:
				break;
			}
		} catch (Exception e) {

		}
		return true;
	}

}
