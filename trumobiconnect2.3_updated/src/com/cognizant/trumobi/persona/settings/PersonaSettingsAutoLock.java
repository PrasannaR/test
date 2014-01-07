package com.cognizant.trumobi.persona.settings;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiTimerClass;
import com.cognizant.trumobi.log.PersonaLog;

import com.cognizant.trumobi.persona.PersonaMainActivity;

import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;

/**
 * 
 * @author 290778 stopped timer when Enter Pin screen in foreground resolved
 *         delayed launch of Lock screen, 1749
 * 
 */
public class PersonaSettingsAutoLock extends DialogFragment {

	ListView mListView;

	AlertDialog.Builder builder;
	private ArrayAdapter<String> mAdapter;

	private static String TAG = PersonaSettingsAutoLock.class.getName();

	private ArrayList<String> timerList;

	private ArrayAdapter<ExternalLockTime> mAutoLockAdapter;
	TextView autoLockTimeLabel;
	ImageView mTitleBarImageView;

	private enum ExternalLockTime {
		Never("Never"), Fifteen("15 sec"), Thirty("30 sec"), Minute("1 minute"), TwoMinutes(
				"2 minutes"), FiveMinutes("5 minutes"), TenMinutes("10 minutes"), QuarterHour(
				"15 minutes"), HalfHour("30 minutes"), Hour("1 hour");

		private String mValue;

		private ExternalLockTime(String value) {
			mValue = value;
		}

		public String getValue() {
			return mValue;
		}

		@Override
		public String toString() {
		
			return mValue;
		}

		public int getLockTimeIndex(String value) {
			int ret = 0;

			return ret;
		}

		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		PersonaLog.d(TAG, "===== In onCreateView ====");

		View v1 = inflater.inflate(R.layout.pr_autolock_list_container,
				container, false);

		mListView = (ListView) v1.findViewById(android.R.id.list);
		mTitleBarImageView = (ImageView) v1
				.findViewById(R.id.pr_change_pin_title_bar_icon);
		
		PersonaLog.d(TAG, "===== View Returned ====");
		return v1;

	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PersonaLog.d(TAG, "===== In onCreate ====");

	}

	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		PersonaLog.d(TAG, "===== In onActivityCreated ====");

		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		TextView autoLockLabel = (TextView) getView().findViewById(
				R.id.prautoLockLabel);
		if(getActivity().getResources().getInteger(R.integer.deviceType) == 1) {
			TextView autoLockTitle =(TextView) getView().findViewById(R.id.pr_settings_autolock_title);
			autoLockTitle.setVisibility(View.INVISIBLE);
		}
		autoLockLabel.setText("Autolock the application");
		autoLockTimeLabel = (TextView) getView().findViewById(
				R.id.prautoLocktimeLabel);

		if (PersonaMainActivity.isCertificateBasedAuth) {
			String lastSavedAutoLockTime = new SharedPreferences(getActivity())
					.getString("selected_autolocktime", "15 minutes");
			autoLockTimeLabel.setText(lastSavedAutoLockTime);
		} else {
			String lastSavedAutoLockTime = new SharedPreferences(getActivity())
					.getString("selected_autolocktime", "1 minute");
			autoLockTimeLabel.setText(lastSavedAutoLockTime);
		}
		ImageView navigation = (ImageView) getView().findViewById(
				R.id.navigationArrow);
		navigation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				

				builder = new AlertDialog.Builder(getActivity());
				builder.setNegativeButton(R.string.pr_cancel_action,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.cancel();
							}

						});

				int previouslySetTime = new SharedPreferences(getActivity())
						.getInt("selected_position", 3);
				builder.setSingleChoiceItems(R.array.personaAutoLock,
						previouslySetTime,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int position) {
								// The 'position' argument contains the index
								// position
								// of the selected item

								saveSelectedLockTime(mAutoLockAdapter
										.getItem(position));

								dialog.cancel();
							}

						});
				AlertDialog dialog = builder.create();

				dialog.show();

			}
		});

		mAutoLockAdapter = new ArrayAdapter<ExternalLockTime>(getActivity(),
				R.layout.pr_settings_autolock_list_layout,
				ExternalLockTime.values());

			
		mTitleBarImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 goTolauncherHome();	
			}
		});

	}
	
	public PersonaSettingsAutoLock() {
		// TODO Auto-generated constructor stub
	}

	
	private void saveSelectedLockTime(
			PersonaSettingsAutoLock.ExternalLockTime eLockTime) {
		PersonaLog.d(TAG, "===== saveSelectedLockTime ====" + eLockTime.mValue);
		PersonaLog.d(TAG,
				"===== saveSelectedLockTime ====" + eLockTime.toString());
		PersonaLog.d(TAG,
				"===== saveSelectedLockTime ====" + eLockTime.getValue());
		PersonaLog.d(TAG, "====== selected Position==" + eLockTime.ordinal());

		PersonaCommonfunctions prcmnFn = new PersonaCommonfunctions(
				getActivity());

		
		long autoLockTimeInteger = prcmnFn
				.convertAutoLockTimerStringToInt(eLockTime.toString());
		PersonaLog.d(TAG, "===== Value of autoLockTimeInteger"
				+ autoLockTimeInteger);
		autoLockTimeLabel.setText(eLockTime.toString());

		new SharedPreferences(getActivity()).edit()
				.putString("selected_autolocktime", eLockTime.toString())
				.commit();
		
		new SharedPreferences(getActivity()).edit()
				.putLong("selected_autolocktime_l", autoLockTimeInteger)
				.commit();
		new SharedPreferences(getActivity()).edit()
				.putInt("selected_position", eLockTime.ordinal()).commit();
		TruMobiTimerClass.userInteractedStopTimer();
		TruMobiTimerClass.startTimerTrigger(getActivity());
	}

	@Override
	public void onResume() {
		
		super.onResume();
		PersonaLog.d(TAG, "===== onResume =====");

	}

	@Override
	public void setInitialSavedState(SavedState state) {
		
		super.setInitialSavedState(state);

	}


	private void goTolauncherHome(){
		
		Intent intent =new Intent(getActivity(),PersonaLauncher.class);
		startActivity(intent);
		getActivity().finish();
	}
}
