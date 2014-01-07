package com.cognizant.trumobi.persona.utils;

import java.util.ArrayList;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaLocalAuthentication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * 
 * @author rudhra 
 * KeyCode 				Author			 Date 			Desc
 * 	Delete logic 		290778							Deletion in between the edit text handled
 * 	modified
 *  clear edittext		290778 			16/10/13 		clear edittext string on incorrect pin 
 * 	insert operation 	290778 			5/11/13 		Resolved Request focus issues and added insert options
 * Special Characters	290778			21/12/13		Special characters and new symbols added 
 */
public class Customkeyboard extends LinearLayout implements
		View.OnClickListener,View.OnLongClickListener {
	Context mcontext;
	ArrayList<Integer> idsList;
	EditText edtText;
	StringBuffer sbf;
	DoneButtonListener buttonListener;
	 private Vibrator myVib;
Button clearButtonalpha;
Button button;
	@SuppressLint("NewApi")
	public Customkeyboard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mcontext = context;
		 myVib = (Vibrator) mcontext.getSystemService(Context.VIBRATOR_SERVICE);
		PersonaLog.e("initUi called from 1nd constructor", "");
		initUi();
	}

	public Customkeyboard(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mcontext = context;
		 myVib = (Vibrator) mcontext.getSystemService(Context.VIBRATOR_SERVICE);
		PersonaLog.e("initUi called from 2nd constructor", "");
		initUi();
	}

	public Customkeyboard(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mcontext = context;
		 myVib = (Vibrator) mcontext.getSystemService(Context.VIBRATOR_SERVICE);
		PersonaLog.e("initUi called from 3nd constructor", "");
		initUi();
	}

	public static void getInstance(Activity activity) {

		return;
	}
	
	

	public void initUi() {

		sbf = new StringBuffer();
		// TODO Auto-generated method stub
		LayoutInflater layinf = (LayoutInflater) mcontext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vw = layinf.inflate(R.layout.pr_key_alphanumeric, null);

		addView(vw);

		LayoutParams lay_Param = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		LinearLayout lay_AlpaNum = (LinearLayout) vw;
		lay_AlpaNum.setLayoutParams(lay_Param);

		idsList = new ArrayList<Integer>();
		assing_ButtonIds(idsList, vw);
		for (int i = 0; i < idsList.size(); i++) {
			Button btn = (Button) findViewById(idsList.get(i));
			btn.setOnClickListener(this);
		}
		smallAlphabets();
		Button btn = (Button) findViewById(R.id.button31);
		btn.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.pr_key_caps_on_tap));
		btn.setTag("big");
		Button btn123 = (Button) findViewById(R.id.button41);
		btn123.setText("?123");
		
		clearButtonalpha =(Button)findViewById(R.id.button39);
		clearButtonalpha.setOnLongClickListener(this);
		// numberslAlphabits();
		// smallAlphabits();
		
	}

	public void setQwertyKeyBoard(boolean b) {
		
		PersonaLog.e("=======setQuertyKeypad======", ""+b);
		LinearLayout keyBoardTypeAlpha, keyBoardTypePin;
		keyBoardTypeAlpha = (LinearLayout) findViewById(R.id.keypad_alpha);
		keyBoardTypePin = (LinearLayout) findViewById(R.id.keypad_pinscreen);
		if (b) {
			keyBoardTypeAlpha.setVisibility(View.VISIBLE);
			keyBoardTypePin.setVisibility(View.GONE);
		} else {
			keyBoardTypeAlpha.setVisibility(View.GONE);
			keyBoardTypePin.setVisibility(View.VISIBLE);

		}

	}

	public void setEdit(final EditText edt) {
		// TODO Auto-generated method stub
		edtText = edt;
	/*	 edt.setOnFocusChangeListener(new OnFocusChangeListener() {
		
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		showCustomKeyboard(v);
		 if(hasFocus){
		
		PersonaLog.e("custom Keypad onFocus","true");
		
		}

		
	 }
		 });*/

		edt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PersonaLog.e("custom Keypad ONCLICK", "true");
				showCustomKeyboard(v);
			/*   button=new Button(mcontext) ;
			   String edtTextstring =edt.getText().toString();
			button.setText(edtTextstring);*/
				
				String edtTextstring =edtText.getText().toString();
				StringBuffer setStr = new StringBuffer();
				
				if (sbf != null && edtText != null) {
					sbf.delete(0, sbf.length());
					PersonaLog.e("after deleting", "sbf: "+sbf);
				}
				
				sbf=setStr.append(edtTextstring);
				
				
				
				/*if(sbf.length()!= edtText.getSelectionStart() ){
					int position = edtText.getSelectionStart();
					setStr=sbf.insert(position, str);
					edtText.setText(setStr);
					edtText.setSelection(position+1);
					}
					else{
						setStr = sbf.append(str);
						edtText.setText(setStr);
					}*/
				
			}
		});

		edt.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				showCustomKeyboard(v);
				return true;
			}
		});

		edt.setOnTouchListener(new OnTouchListener() {

			@Override
		public boolean onTouch(View v, MotionEvent event) {
//				// TODO Auto-generated method stub
			PersonaLog.e("custom Keypad on touch", "");
				EditText edittext = (EditText) v;
//				// int inType = edittext.getInputType(); // Backup the input
//				// type
//				// edittext.setInputType(InputType.TYPE_NULL); // Disable
//				// standard keyboard
				edittext.onTouchEvent(event); // Call native handler
//				// edittext.setInputType(inType);
				showCustomKeyboard(v);
			return true;
//
		}	});
//
	}

	public void showCustomKeyboard(View v) {

		if (v != null)
			((InputMethodManager) mcontext
					.getSystemService(Activity.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	private void capitalAlphabets() {

		String[] caps = { "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P",
				"A", "S", "D", "F", "G", "H", "J", "K", "L", "", "Z", "X", "C",
				"V", "B", "N", "M", "", "?123", "@", ".com", "", ",", ".", "" };
		for (int i = 0; i < idsList.size(); i++) {
			Button btn = (Button) findViewById(idsList.get(i));
			btn.setText(caps[i]);
			btn.setTextColor(Color.WHITE);
			btn.setTextAppearance(mcontext, R.style.personaCustomKeyButtonStyle);
			//btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
		}
	}

	private void smallAlphabets() {

		String[] caps = { "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
				"a", "s", "d", "f", "g", "h", "j", "k", "l", "", "z", "x", "c",
				"v", "b", "n", "m", "", "?123", "@", ".com", "", ",", ".", "" };
		for (int i = 0; i < idsList.size(); i++) {
			Button btn = (Button) findViewById(idsList.get(i));
			btn.setText(caps[i]);
			btn.setTextColor(Color.WHITE);
			//btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
			btn.setTextAppearance(mcontext, R.style.personaCustomKeyButtonStyle);

		}
	}

	private void numbericalKeys() {

		String[] caps = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
				"#", "$", "%", "&", "*", "-", "+", "(", ")", ".","<", ">", "=",
				 ";", ",", "!", "?","", "?123", "/", ":", "", "\"", "_",
				"" };
		for (int i = 0; i < idsList.size(); i++) {
			Button btn = (Button) findViewById(idsList.get(i));
			btn.setText(caps[i]);
			btn.setTextColor(Color.WHITE);
			//btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
			btn.setTextAppearance(mcontext, R.style.personaCustomKeyButtonStyle);

		}
	}
	

	
	private void specialCharactersNexus() {

		String[] caps = { "~", "`", "|", "•", "√", "π", "÷", "×", "§", "Δ",
				"£", "¢", "€", "¥", "^", "°", "±", "{", "}", "", "\\", "©", "®",
				"™", "℅", "[", "]", "", "?123", "¡", "¿", "", ";", "!", "" };
		for (int i = 0; i < idsList.size(); i++) {
			Button btn = (Button) findViewById(idsList.get(i));
			btn.setText(caps[i]);
			btn.setTextColor(Color.WHITE);
			//btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
			btn.setTextAppearance(mcontext, R.style.personaCustomKeyButtonStyle);

		}
	}

	private void assing_ButtonIds(ArrayList<Integer> midsList, View lnr) {
		// TODO Auto-generated method stub

		LinearLayout firstRow = (LinearLayout) lnr.findViewById(R.id.firstrow);
		for (int i = 0; i < firstRow.getChildCount(); i++) {
			midsList.add(Integer.valueOf(firstRow.getChildAt(i).getId()));
		}

		LinearLayout secondRow = (LinearLayout) lnr
				.findViewById(R.id.secondrow);
		for (int i = 0; i < secondRow.getChildCount(); i++) {
			midsList.add(Integer.valueOf(secondRow.getChildAt(i).getId()));
		}

		LinearLayout thirdRow = (LinearLayout) lnr.findViewById(R.id.thirdrow);
		for (int i = 0; i < thirdRow.getChildCount(); i++) {
			midsList.add(Integer.valueOf(thirdRow.getChildAt(i).getId()));
		}

		LinearLayout fourthRow = (LinearLayout) lnr
				.findViewById(R.id.fourthrow);
		for (int i = 0; i < fourthRow.getChildCount(); i++) {
			midsList.add(Integer.valueOf(fourthRow.getChildAt(i).getId()));
		}

		setpinscreenClick();

	}

	private void setpinscreenClick() {
		// TODO Auto-generated method stub
		Button btn1 = (Button) findViewById(R.id.button_1);
		btn1.setOnClickListener(this);
		Button btn2 = (Button) findViewById(R.id.button_2);
		btn2.setOnClickListener(this);
		Button btn3 = (Button) findViewById(R.id.button_3);
		btn3.setOnClickListener(this);
		Button btn4 = (Button) findViewById(R.id.button_4);
		btn4.setOnClickListener(this);
		Button btn5 = (Button) findViewById(R.id.button_5);
		btn5.setOnClickListener(this);
		Button btn6 = (Button) findViewById(R.id.button_6);
		btn6.setOnClickListener(this);
		Button btn7 = (Button) findViewById(R.id.button_7);
		btn7.setOnClickListener(this);
		Button btn8 = (Button) findViewById(R.id.button_8);
		btn8.setOnClickListener(this);
		Button btn9 = (Button) findViewById(R.id.button_9);
		btn9.setOnClickListener(this);
		Button btnleft = (Button) findViewById(R.id.button_left);
		btnleft.setOnClickListener(this);
		btnleft.setOnLongClickListener(this);
		Button btn0 = (Button) findViewById(R.id.button_0);
		btn0.setOnClickListener(this);
		Button btnright = (Button) findViewById(R.id.button_right);
		btnright.setOnClickListener(this);
	
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		//PersonaLog.e("Inside onclick of Custom Keypad", "Onclick");
		myVib.vibrate(20);
		if ((v.getId() == R.id.button31) || (v.getId() == R.id.button41)
				||(v.getId() == R.id.button47)) {
			modifyKeypadDialogs(v);
		} else {
			addstringLogic(v);

		}
	}

	private void addstringLogic(View v) {

		StringBuffer setStr = new StringBuffer();
		Button btn = (Button) v;
		String str = btn.getText().toString();
		
		if (((v.getId() == R.id.button39) || (v.getId() == R.id.button_left))
				&& sbf.length() >= 1 && edtText.getSelectionStart() >= 1) {

			// setStr = sbf.deleteCharAt(sbf.length()-1);
			int position = edtText.getSelectionStart() - 1;
			setStr = sbf.deleteCharAt(position);
			
			edtText.setText(setStr);
			// edtText.setSelection(edtText.getText().length());
			
			edtText.setSelection(position);
		}
//		} else if (v.getId() == R.id.button_right) {
//			PersonaLog.d("==========Done button Hit========", "" + v.getId());
//			PersonaLocalAuthentication personaLocalAuthentication=new PersonaLocalAuthentication();
//			personaLocalAuthentication.doneButton();
//			
//
//		}
	//	insert operation
		else {
			
			
			if(sbf.length()!= edtText.getSelectionStart() ){
			int position = edtText.getSelectionStart();
			setStr=sbf.insert(position, str);
			edtText.setText(setStr);
			edtText.setSelection(position+1);
			}
			else{
				setStr = sbf.append(str);
				edtText.setText(setStr);
				edtText.setSelection(edtText.getText().length());
			}

		}

	}

	private void modifyKeypadDialogs(View id) {
		// TODO Auto-generated method stub
		if (id.getId() == R.id.button31) {
			TagModifyfunc(id);
		} else if (id.getId() == R.id.button41) {
			numberModifyfunc(id);
		} else if (id.getId() == R.id.button47) {
			addstringLogic(id);
		}
	}

	@SuppressWarnings("deprecation")
	private void numberModifyfunc(View v) {
		// TODO Auto-generated method stub

		if (((Button) v).getText().toString().equals("?123")) {
			numbericalKeys();
			Button btnBac = (Button) findViewById(R.id.button31);

			btnBac.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.pr_key_lightgrey));
			btnBac.setVisibility(View.INVISIBLE);
			//((Button) v).setText("abc");
			((Button) v).setText("sym");
		} else if (((Button) v).getText().toString().equals("abc")) {

			Button btn = (Button) findViewById(R.id.button31);
			btn.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.pr_key_caps_on_tap));
			btn.setTag("big");
			btn.setVisibility(View.VISIBLE);
			smallAlphabets();
			((Button) v).setText("?123");
		}
		else if (((Button) v).getText().toString().equals("sym")) {
			specialCharactersNexus();
			Button btnBac = (Button) findViewById(R.id.button31);

			btnBac.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.pr_key_lightgrey));
			btnBac.setVisibility(View.INVISIBLE);
			//((Button) v).setText("abc");
			((Button) v).setText("abc");
		}
	}

	@SuppressWarnings("deprecation")
	private void TagModifyfunc(View v) {
		// TODO Auto-generated method stub
		Button btn = (Button) v;
		if (btn.getText().toString().equalsIgnoreCase("?")) {
			addstringLogic(v);
			return;
		}

		if (v.getTag().toString().equals("big")) {

			v.setTag("small");
			btn.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.pr_key_caps_off_tap));
			capitalAlphabets();
		} else if (v.getTag().toString().equals("small")) {

			v.setTag("big");
			btn.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.pr_key_caps_on_tap));
			smallAlphabets();
		}

	}

	public interface DoneButtonListener {

		public void onDoneButtonClicked();

	}

	
	// clear edittext
	public void clearEditBox() {

		if (sbf != null && edtText != null) {
			sbf.delete(0, sbf.length());
		}
		edtText.setText("");
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		PersonaLog.e("onLongClick","onLongClick");
	if(v.getId()==R.id.button_left  ||v.getId()==R.id.button39){
		clearEditBox();
	}
		return true;
	}

}
