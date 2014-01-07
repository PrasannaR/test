package com.cognizant.trumobi.messenger.sms;

import java.util.HashMap;

import com.cognizant.trumobi.R;
import com.google.common.base.Strings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class SmsChipsMultiAutoCompleteTextview extends
		MultiAutoCompleteTextView implements OnItemClickListener {

	private final String TAG = "ChipsMultiAutoCompleteTextview";
	public Context mcontext;// onClickItemSpan
	public boolean chipFlag = false;
	interface SelectItemContact {
		void selectContactPos(int pos);
	};

	// Constructor
	public SmsChipsMultiAutoCompleteTextview(Context context) {
		super(context);
		mcontext = context;
		init(context);
	}

	// Constructor
	public SmsChipsMultiAutoCompleteTextview(Context context, AttributeSet attrs) {
		super(context, attrs);
		mcontext = context;
		init(context);
	}

	// Constructor
	public SmsChipsMultiAutoCompleteTextview(Context context,
			AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mcontext = context;
		init(context);
	}

	// set listeners for item click and text change
	public void init(Context context) {
		setOnItemClickListener(this);
		addTextChangedListener(textWather);
		setThreshold(1);
	}

	// TextWatcher, If user type any country name and press comma then following
	// code will regenerate chips
	private TextWatcher textWather = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (count >= 1) {
				if (s.charAt(start) == ','){
					chipFlag = true;
					setChips(); // generate chips
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			if(!chipFlag){
				if(s.toString().contains(",")){
					setChips();
				}
			}
		}
	};

	public HashMap<String, SmsContactBean> listHashMap;

	@Override
	protected void onFocusChanged(boolean focused, int direction,
			Rect previouslyFocusedRect) {

		if (!focused) {
			int i = this.getSelectionEnd();
			String c = this.getText().toString().trim();
			if (c.length() == 1 && c.charAt(c.length()-1) != ',')
				this.append(",");
			else if (!c.isEmpty() && c.charAt(c.length()-1) != ',')
				this.append(",");
			else if (!c.isEmpty() && getText().toString().contains(","))
				this.append("");
		}
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}
	
	public void setStringChips(String value) {
		System.out.println("From Contact App String");
		System.out.println("ChipTexttttttttttttt String : "+value.toString());
		if (value.contains(",")) // check comman in string
		{

			SpannableStringBuilder ssb = new SpannableStringBuilder(value);
			// split string wich comma
			String chips[] = value.toString().trim().split(",");
			int x = 0;
			// loop will generate ImageSpan for every country name separated by
			// comma
			for (String c : chips) {
				System.out.println("Insssssssssssidddddddddeeeeeeeeeee For  String: "+c);
				if(!Strings.isNullOrEmpty(c.trim())){
				// inflate chips_edittext layout
					System.out.println("Insssssssssssidddddddddeeeeeeeeeee IIIIIIIFFFFFFFF");
				LayoutInflater lf = (LayoutInflater) getContext()
						.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				TextView textView = (TextView) lf.inflate(
						R.layout.sms_chips_edittext, null);
				// setContactImage(textView, c);//SET IMAGE
				textView.setText(c); // set text
				// setFlags(textView, c); // set flag image
				// capture bitmapt of genreated textview
				int spec = MeasureSpec.makeMeasureSpec(0,
						MeasureSpec.UNSPECIFIED);
				textView.measure(spec, spec);
				textView.layout(0, 0, textView.getMeasuredWidth(),
						textView.getMeasuredHeight());
				Bitmap b = Bitmap.createBitmap(textView.getWidth(),
						textView.getHeight(), Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(b);
				canvas.translate(-textView.getScrollX(), -textView.getScrollY());
				textView.draw(canvas);
				textView.setDrawingCacheEnabled(true);
				Bitmap cacheBmp = textView.getDrawingCache();
				Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
				textView.destroyDrawingCache(); // destory drawable
				// create bitmap drawable for imagespan
				BitmapDrawable bmpDrawable = new BitmapDrawable(viewBmp);
				bmpDrawable.setBounds(0, 0, bmpDrawable.getIntrinsicWidth(),
						bmpDrawable.getIntrinsicHeight());
				// create and set imagespan
				ClickableSpan clickSpan = new ClickableSpan() {

					@Override
					public void onClick(View view) {

					}

				};
				ssb.setSpan(clickSpan, x, x + c.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				ssb.setSpan(new ImageSpan(bmpDrawable), x, x + c.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				x = x + c.length() + 1;
				}
			}
			// set chips span
			setText(ssb);
			// move cursor to last
			setSelection(value.length()-1);
		}
	}
	// This function has whole logic for chips generate
	public void setChips() {
		chipFlag = true;
		if (getText().toString().contains(",")) // check comman in string
		{
			
			SpannableStringBuilder ssb = new SpannableStringBuilder(getText());
			// split string wich comma
			String chips[] = getText().toString().trim().split(",");
			int x = 0;
			// loop will generate ImageSpan for every country name separated by
			// comma
			for (String c : chips) {
				// inflate chips_edittext layout
				LayoutInflater lf = (LayoutInflater) getContext()
						.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				TextView textView = (TextView) lf.inflate(
						R.layout.sms_chips_edittext, null);
				// setContactImage(textView, c);//SET IMAGE
				textView.setText(c); // set text
				// setFlags(textView, c); // set flag image
				// capture bitmapt of genreated textview
				int spec = MeasureSpec.makeMeasureSpec(0,
						MeasureSpec.UNSPECIFIED);
				textView.measure(spec, spec);
				textView.layout(0, 0, textView.getMeasuredWidth(),
						textView.getMeasuredHeight());
				Bitmap b = Bitmap.createBitmap(textView.getWidth(),
						textView.getHeight(), Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(b);
				canvas.translate(-textView.getScrollX(), -textView.getScrollY());
				textView.draw(canvas);
				textView.setDrawingCacheEnabled(true);
				Bitmap cacheBmp = textView.getDrawingCache();
				Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
				textView.destroyDrawingCache(); // destory drawable
				// create bitmap drawable for imagespan
				BitmapDrawable bmpDrawable = new BitmapDrawable(viewBmp);
				bmpDrawable.setBounds(0, 0, bmpDrawable.getIntrinsicWidth(),
						bmpDrawable.getIntrinsicHeight());
				// create and set imagespan
				ClickableSpan clickSpan = new ClickableSpan() {

					@Override
					public void onClick(View view) {

					}

				};
				ssb.setSpan(clickSpan, x, x + c.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				ssb.setSpan(new ImageSpan(bmpDrawable), x, x + c.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				x = x + c.length() + 1;
				}
			
			// set chips span
			setText(ssb);
			// move cursor to last
			setSelection(getText().length()-1);
		}
	}

	public boolean isContactFromDB = false;

	@Override
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		isContactFromDB = true;
		SelectItemContact sel = (SelectItemContact) mcontext;
		sel.selectContactPos(position);
		setChips(); // call generate chips when user select any item from auto
					// complete
	}


}
