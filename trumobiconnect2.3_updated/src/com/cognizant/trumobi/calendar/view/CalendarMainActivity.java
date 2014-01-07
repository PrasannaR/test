package com.cognizant.trumobi.calendar.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.TruBoxSDK.SharedPreferences;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.internal.view.menu.ActionMenuView.LayoutParams;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.agendaView.CalendarAgendaFragment;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.calendar.util.CalendarUtility;
import com.cognizant.trumobi.calendar.view.CalendarDayViewFragment.OnViewRefreshListener;
import com.cognizant.trumobi.calendar.view.CalendarDayViewFragment.OnViewSelectedListener;
import com.cognizant.trumobi.calendar.view.CalendarMonthViewFragment.OnMonthListener;
import com.cognizant.trumobi.calendar.view.CalendarView.OnCalendarViewSelectedListener;
import com.cognizant.trumobi.calendar.view.CalendarWeekViewFragment.OnViewWeekRefreshListener;
import com.cognizant.trumobi.calendar.view.CalendarWeekViewFragment.OnViewWeekSelectedListener;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockFragmentBaseActivity;
import com.cognizant.trumobi.commonabstractclass.TruMobiTimerClass;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.CalendarLog;
//import com.cognizant.trumobi.calendar.adapter.Request;
//import com.cognizant.trumobi.calendar.provider.CertificateProvider;
//import com.cognizant.trumobi.calendar.service.EasSyncService;
//import com.cognizant.trumobi.calendar.service.EasSyncService.OnServiceCompletedWithError;
//import com.cognizant.trumobi.calendar.service.ExchangeData;

public class CalendarMainActivity extends
		TruMobiBaseSherlockFragmentBaseActivity implements
		OnViewSelectedListener, OnNavigationListener, OnViewRefreshListener,
		OnViewWeekSelectedListener, OnViewWeekRefreshListener, OnMonthListener,
		OnCalendarViewSelectedListener {

	private CalendarNavigationListAdapter mNavigationListAdapter;
	private ActionBar mActionBar;

	private String mPresentDay, mToday, mMonth;
	private int mItemPreviousPosition = 0;

	private boolean mIsmenuclick = false;
	private Menu mMenu;
	private MultiAutoCompleteTextView mMultiAutoCompleteTextView;
	private ArrayList<String> mList_events_history = new ArrayList<String>();
	private SharedPreferences mPreferences;
	InputMethodManager mInputManager;
	private CalendarView mCalendarView;
	private boolean mFlag_hide_control,misOrentationChanged=false;
	private TextView mLayout_today_text;
	private boolean mSearchFlag;
	private View mTodayView;
	private LinearLayout mSearchBackButtonLinearLayout;
	private String mCurrentView;
	private String mStartTime = "";
	DialogFragment newFragment;
	ProgressDialog mDialog;
	boolean is24Hrs;
	private String am_pm;
	private String finalTimeZone, minValue,oldPresentDay ="";
	private TextView txt_timeZone;
	private NotificationManager notificationManager;
	
	
	BroadcastReceiver receiveCompleteSync = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshActivity();

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPreferences = new SharedPreferences(Email.getAppContext());
		Email.setmSecurePrefs(mPreferences);/*
											 * getSharedPreferences(
											 * CalendarConstants
											 * .SETTINGS_PREF_NAME,
											 * MODE_PRIVATE);
											 */
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(102);
		CalendarUtility.setTimeZone();

		mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mPresentDay = Email.getNameOFMonth(Email.todayMonth) + " "
				+ Email.todayDate + ", " + Email.todayYear;
		mToday = Email.getNameOFMonth(Email.todayMonth) + " "
				+ Email.todayDate;
		mMonth = Email.getNameOFMonth(Email.todayMonth);
		Email.setPresentDate(Email.todayDate + " "
				+ Email.getNameOFMonth(Email.todayMonth) + " "
				+ Email.todayYear);

		mActionBar = getSupportActionBar();

		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		mActionBar.setDisplayHomeAsUpEnabled(false);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setIcon(R.drawable.cal_app_icon);
		if (savedInstanceState != null) {
			mCurrentView = savedInstanceState.getString("currentView");
		} else {
			mCurrentView = "DAY";
		}
		mNavigationListAdapter = new CalendarNavigationListAdapter(this,
				getResources().getStringArray(R.array.nav_list), mPresentDay,
				Email.getWeekDayOfDate(Email.todayDate + "/"
						+ Email.todayMonth + "/" + Email.todayYear), mToday,
				mMonth, Email.getWeekDays(), getSupportFragmentManager(),
				mCurrentView);
		mActionBar.setListNavigationCallbacks(mNavigationListAdapter,
				CalendarMainActivity.this);
		// loadingDialog = new ProgressDialog(this);

		LayoutInflater layoutInflater = getLayoutInflater();

		mTodayView = layoutInflater.inflate(R.layout.cal_today, null);
		RelativeLayout todayParentLayout = (RelativeLayout) mTodayView
				.findViewById(R.id.cal_today_layout);
		mLayout_today_text = (TextView) mTodayView
				.findViewById(R.id.today_menu_icon);
		mLayout_today_text.setText(String.valueOf(Email.todayDate));
		mActionBar.setCustomView(mTodayView);

		todayParentLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				onTodayEventMenuClick();
			}
		});

		Email.unregisterCalendarReceiver(receiveCompleteSync);
		IntentFilter filter = new IntentFilter(CalendarConstants.SYNC_COMPLETED);
		registerReceiver(receiveCompleteSync, filter);

		setContentView(R.layout.cal_main);
		
		txt_timeZone = (TextView)findViewById(R.id.txt_timezone);
		
		setTimeZoneOnTop();
		
		CalendarCommonFunction.checkBuildVersion();
		if (savedInstanceState == null) {
			Email.setmCurrentFragment(CalendarFragmentType.DAYVIEW);
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			CalendarDayViewFragment dayViewFragment = new CalendarDayViewFragment();
			ft.add(R.id.dayViewFragment, dayViewFragment,
					CalendarConstants.CALENDAR_DAY_VIEW);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.addToBackStack(CalendarConstants.CALENDAR_DAY_VIEW);
			ft.commit();
//			new CalendarTask().execute();
		} else {
			misOrentationChanged=true;
			oldPresentDay=savedInstanceState.getString("CurrentDay");
			mItemPreviousPosition = savedInstanceState.getInt(CalendarConstants.CALENDAR_NAVIGATION_POSITION);
			// if (!actionBar.isShowing())
			// actionBar.show();
			//
			// CalendarEditEvent editEventfragment = (CalendarEditEvent)
			// getSupportFragmentManager()
			// .findFragmentByTag("EditEventViewFragment");
			// if (editEventfragment != null)
			// if (editEventfragment.isVisible()) {
			// if (actionBar.isShowing()) {
			// // actionBar.hide();
			// launchNewEvent(true, editEventfragment.type);
			// // onBackPressed();
			// }
			// }
		}
		Intent intent = getIntent();
		if (intent != null) {
			Bundle getuserID = getIntent().getExtras();
			if (getuserID != null) {
				/*String fromNotification = intent.getStringExtra("fromNotification");
				if (fromNotification != null) {
					CalendarLog.d(CalendarConstants.Tag, "Notification click");
					boolean requirePinScreen =  new SharedPreferences(CalendarMainActivity.this).getBoolean(
							"showPinOnResume", false);
					if(!requirePinScreen){
						CalendarLog.d(CalendarConstants.Tag, "Notification click if");
						TruMobiTimerClass.startTimerTrigger(context);
						new SharedPreferences(CalendarMainActivity.this).edit().putBoolean("showPinOnResume", true).commit();
					}
				}*/
				String eventid = intent
						.getStringExtra(CalendarConstants.EVENT_ID);
				if (eventid != null) {
					ContentValues contentValues = CalendarDatabaseHelper
							.getEventContentValues(CalendarDatabaseHelper
									.getEventDetails(Integer.parseInt(eventid)));
					contentValues.put(CalendarConstants.CALENDAR_TYPE_KEY,
							CalendarConstants.CALENDAR_TYPE_CORPORATE);
					onEditEvent(contentValues, CalendarFragmentType.DAYVIEW);
				}

				String start_date = intent
						.getStringExtra(CalendarConstants.START_DATE);
				long start_time = intent.getLongExtra("start_time", 0);
				if (start_date != null) {
					SetStartTime(String.valueOf(start_time));
					mPresentDay = CalendarDatabaseHelper
							.getDateFormatForPresentDay(start_date);
					Email.setPresentDate(mPresentDay);
					String[] days = start_date.split("-");
					Email.todayDate = Integer.parseInt(days[2]);
				//	Email.todayDate = Integer.parseInt(days[2]);
					Email.todayMonth = Integer.parseInt(days[1]);
					Email.todayYear = Integer.parseInt(days[0]);
					callDayView();
				}

			}
		}
		if (CalendarCommonFunction.isTablet(Email.getAppContext())) {

			if (savedInstanceState != null) {
				mFlag_hide_control = savedInstanceState
						.getBoolean(CalendarConstants.FLAG_HIDE);
				if ((savedInstanceState
						.getString(CalendarConstants.FRAGMENT_TYPE)
						.equals((CalendarFragmentType.AGENDAVIEW).toString()))
						|| (savedInstanceState
								.getString(CalendarConstants.FRAGMENT_TYPE)
								.equals((CalendarFragmentType.MONTHVIEW)
										.toString()))
						|| (savedInstanceState
								.getBoolean(CalendarConstants.FLAG_HIDE))) {
					findViewById(R.id.displayDetail).setVisibility(
							LinearLayout.GONE);

				} else {
					callCalendarView();
				}

			} else {
				callCalendarView();
			}

		}
	}

	private class CalendarTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			if (mDialog == null) {
				mDialog = new ProgressDialog(CalendarMainActivity.this);
				mDialog.setMessage(" Loading.. Please Wait");
				mDialog.setCancelable(false);
				mDialog.show();
			} else if (mDialog != null) {
				mDialog.show();
			}

		}

		@Override
		protected Void doInBackground(Void... params) {
			Email.setmCurrentFragment(CalendarFragmentType.DAYVIEW);
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			CalendarDayViewFragment dayViewFragment = new CalendarDayViewFragment();
			ft.add(R.id.dayViewFragment, dayViewFragment,
					CalendarConstants.CALENDAR_DAY_VIEW);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.addToBackStack(CalendarConstants.CALENDAR_DAY_VIEW);
			ft.commit();
			return null;
		}

		@Override
		protected void onPostExecute(Void params) {
			super.onPostExecute(params);
			if (mDialog.isShowing())
				mDialog.dismiss();

		}

	}

	public void onURLSelected() {
		// if(detailPage){
		// CalendarDetailFragment detailFragment =
		// (CalendarDetailFragment)getSupportFragmentManager().findFragmentById(R.id.displayDetail);
		// detailFragment.addActionEvent();
		// }
		// else{
		// CalendarDetailFragment detailFragment = new CalendarDetailFragment();
		// detailFragment.addActionEvent();
		// FragmentTransaction ft =
		// getSupportFragmentManager().beginTransaction();
		// ft.replace(R.id.displayDetail, detailFragment, "Detail_Fragment");
		// ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		// ft.addToBackStack(null);
		// ft.commit();
		// }
	}

	public boolean onCreateOptionsMenu(Menu menu) {

		this.mMenu = menu;
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.calendar_menu, menu);
		return true;

	}

	// private boolean onClickSearch(final View view, MotionEvent event) {
	// // do something
	// if (!mMultiAutoCompleteTextView.getText().toString().equals("")) {
	// if (!mList_events_history.contains(mMultiAutoCompleteTextView
	// .getText().toString())) {
	// CalendarDatabaseHelper
	// .insertSearchQuery(mMultiAutoCompleteTextView.getText()
	// .toString());
	// }
	// // Check Search item matched Event in Event Table
	// // Set search query value
	// // And launch AgendaView
	// Email.setSearchQueryString(mMultiAutoCompleteTextView.getText()
	// .toString());
	// Email.setSearchFlag(true);
	// callAgendaView();
	// // Email.setmCurrentFragment(CalendarFragmentType.AGENDAVIEW);
	// }
	// event.setAction(MotionEvent.ACTION_CANCEL);
	// return false;
	// }

	/**
	 * Set Listener for Search
	 * 
	 * @param menu
	 */
	private void inflateSearchOption(Menu menu) {

		View searchview = null;
		if (CalendarCommonFunction.isSmallScreen(getApplicationContext())) {
			mSearchFlag = true;
			LayoutInflater layoutInflater = getLayoutInflater();
			searchview = layoutInflater.inflate(
					R.layout.cal_collapsible_edittext, null);
			// View v = (View) menu.findItem(R.id.itemSearch).getActionView();
			mActionBar.setNavigationMode(ActionBar.DISPLAY_SHOW_CUSTOM);
			com.actionbarsherlock.app.ActionBar.LayoutParams lp = new com.actionbarsherlock.app.ActionBar.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

			mActionBar.setCustomView(searchview, lp);

			mSearchBackButtonLinearLayout = (LinearLayout) searchview
					.findViewById(R.id.btn_back_search_actionbar_layout);
			if (mSearchBackButtonLinearLayout != null) {
				mSearchBackButtonLinearLayout
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								hideKeyboard();
								refreshActivity();
							}
						});
			}
		} else {
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			searchview = (View) menu.findItem(R.id.itemSearch).getActionView();

		}

		// View v = (View) menu.findItem(R.id.itemSearch).getActionView();
		mList_events_history = CalendarDatabaseHelper.getSearchHistory();
		mMultiAutoCompleteTextView = (MultiAutoCompleteTextView) searchview
				.findViewById(R.id.multiautocompletetextview);
		mMultiAutoCompleteTextView.setTextColor(Color.WHITE);
		// myMultiAutoCompleteTextView.clearFocus();
		// showKeyboard();
		// myMultiAutoCompleteTextView.requestFocus();
		// myMultiAutoCompleteTextView.setAdapter(new
		// ArrayAdapter<String>(this,R.layout.custom_auto_complete_textview,R.id.autoCompleteTextItem,list_events));
		CalendarSearchAdapter searchAdapter = new CalendarSearchAdapter(this,
				R.layout.cal_custom_auto_complete_textview,
				R.id.autoCompleteTextItem, mList_events_history);
		mMultiAutoCompleteTextView.setAdapter(searchAdapter);

		mMultiAutoCompleteTextView.setTokenizer(new CalendarSpaceTokenizer());
		mMultiAutoCompleteTextView
				.setOnTouchListener(new CalendarRightDrawableOnTouchListener(
						mMultiAutoCompleteTextView) {
					@Override
					public boolean onDrawableTouch(final MotionEvent event) {
						// hideKeyboard();
						// return onClickSearch(mMultiAutoCompleteTextView,
						// event);

						try {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									mMultiAutoCompleteTextView.getWindowToken(),
									0);

							// Save Search Item to History Table

							if (!mMultiAutoCompleteTextView.getText()
									.toString().equals("")) {
								if (!mList_events_history
										.contains(mMultiAutoCompleteTextView
												.getText().toString())) {
									CalendarDatabaseHelper
											.insertSearchQuery(mMultiAutoCompleteTextView
													.getText().toString());
								}
								// Check Search item matched Event in Event
								// Table
								// Set search query value
								// And launch AgendaView
								Email.setSearchQueryString(mMultiAutoCompleteTextView
										.getText().toString());
								Email.setSearchFlag(true);
								callAgendaView();
								mItemPreviousPosition=3;
								// Email.setmCurrentFragment(CalendarFragmentType.AGENDAVIEW);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						return true;
					}
				});

		mMultiAutoCompleteTextView
				.setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int arg1,
							KeyEvent arg2) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(
								mMultiAutoCompleteTextView.getWindowToken(), 0);
						// Save Search Item to History Table

						if (!v.getText().toString().equals("")) {
							if (!mList_events_history.contains(v.getText()
									.toString())) {
								CalendarDatabaseHelper.insertSearchQuery(v
										.getText().toString());
							}
							// Check Search item matched Event in Event Table
							// Set search query value
							// And launch AgendaView
							Email.setSearchQueryString(v.getText().toString());
							Email.setSearchFlag(true);
							callAgendaView();
							mItemPreviousPosition=3;
							// Email.setmCurrentFragment(CalendarFragmentType.AGENDAVIEW);
						}
						return true;
					}

				});
		mMultiAutoCompleteTextView.setText("");

	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		/*
		 * case R.id.item_today: // To Check
		 * CalendarLog.d(CalendarConstants.Tag,"Today");
		 * Email.setTodayEventFlag(true); onTodayEventMenuClick(); return true;
		 */
		
	    case R.id.item_today: // To Check
			 // CognizantEmail.setTodayEventFlag(true);
			  onTodayEventMenuClick();
		  return true;

		case R.id.item_connect:
			CalendarLog.d(CalendarConstants.Tag, "Connect");
			// finish();
			Intent intent = new Intent(this, PersonaLauncher.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			return true;
		case R.id.itemNewEvent:
			// do something when this button is pressed
			Intent eventIntent = new Intent(CalendarMainActivity.this,
					com.cognizant.trumobi.calendar.event.CalendarAddEvent.class);
			startActivity(eventIntent);
			return true;
		case R.id.itemRefresh:
			CalendarDatabaseHelper.manualSync();
			return true;
		case R.id.itemSearch:
			// do something when this button is pressed
			// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			// actionBar.setTitle("");
			inflateSearchOption(mMenu);
			mMultiAutoCompleteTextView.clearFocus();
			showKeyboard();
			mMultiAutoCompleteTextView.requestFocus();
			item.setOnActionExpandListener(new OnActionExpandListener() {

				@Override
				public boolean onMenuItemActionExpand(MenuItem item) {
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							// myMultiAutoCompleteTextView.clearFocus();
							// showKeyboard();
							// myMultiAutoCompleteTextView.requestFocus();
						}

					}, 400);
					return true;
				}

				@Override
				public boolean onMenuItemActionCollapse(MenuItem item) {
					// return back to previous page
					mActionBar
							.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
					hideKeyboard();
					onTodayEventMenuClick();
					return true;
				}
			});
			return true;

		case R.id.item_display_controls:

			CalendarDayViewFragment dayfragment = (CalendarDayViewFragment) getSupportFragmentManager()
					.findFragmentByTag(CalendarConstants.CALENDAR_DAY_VIEW);

			CalendarWeekViewFragment weekfragment = (CalendarWeekViewFragment) getSupportFragmentManager()
					.findFragmentByTag(CalendarConstants.CALENDAR_WEEK_VIEW);

			if (item.getTitle().equals(getString(R.string.calHideControl))) {
				item.setTitle(getString(R.string.calShowControl));
				if (findViewById(R.id.displayDetail) != null) {
					findViewById(R.id.displayDetail).setVisibility(View.GONE);
				}
				mFlag_hide_control = true;
			} else {
				item.setTitle(getString(R.string.calHideControl));
				if (findViewById(R.id.displayDetail) != null) {
					if ((dayfragment != null && dayfragment.isVisible())
							|| (weekfragment != null && weekfragment
									.isVisible())) {
						findViewById(R.id.displayDetail).setVisibility(
								View.VISIBLE);
					} else {
						findViewById(R.id.displayDetail).setVisibility(
								View.GONE);
					}

				}
				mFlag_hide_control = false;
			}
			return true;

//		case R.id.itemCalendars_to_Display:
//			// do something when this button is pressed
//			return true;
		case R.id.itemSettings:
			// do something when this button is pressed
			Intent settingIntent = new Intent(
					CalendarMainActivity.this,
					com.cognizant.trumobi.calendar.settings.CalendarSettings.class);

			startActivity(settingIntent);
			return true;
		default:
			return true;
		}
	}

	private void hideKeyboard() {
		// InputMethodManager mInputManager = null;
		try {
			if (android.os.Build.VERSION.SDK_INT < 11) {
				mInputManager.hideSoftInputFromWindow(this.getWindow()
						.getCurrentFocus().getWindowToken(), 0);
			} else {
				mInputManager.hideSoftInputFromWindow(this.getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showKeyboard() {
		// InputMethodManager mInputManager = null;
		try {
			if (android.os.Build.VERSION.SDK_INT < 11) {
				mInputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,
						InputMethodManager.HIDE_IMPLICIT_ONLY);
			} else {
				mInputManager.toggleSoftInput(0,
						InputMethodManager.SHOW_IMPLICIT);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

		}
	};

	/**
	 * Show Today Event on Today menu icon click
	 */
	private void onTodayEventMenuClick() {

		Email.setTodayEventFlag(true);
		Email.getCurrentDDMMYY();
		mPresentDay = Email.todayDate + " "
				+ Email.getNameOFMonth(Email.todayMonth) + " "
				+ Email.todayYear;
		mToday = Email.todayDate + " "
				+ Email.getNameOFMonth(Email.todayMonth);
		mMonth = Email.getNameOFMonth(Email.todayMonth);
		Email.setPresentDate(mPresentDay);
		mLayout_today_text.setText(String.valueOf(Email.todayDate));
		
		// RefreshActivity();

		if (Email.getmCurrentFragment().equals(CalendarFragmentType.DAYVIEW))
			callDayView();
		if (Email.getmCurrentFragment().equals(CalendarFragmentType.WEEKVIEW))
			callWeekAgenda();
		if (Email.getmCurrentFragment().equals(CalendarFragmentType.MONTHVIEW))
			callMonthAgenda();
		if (Email.getmCurrentFragment().equals(CalendarFragmentType.AGENDAVIEW))
			callAgendaView();
		
		
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

		Email.setTodayEventFlag(false);
		if (mIsmenuclick) {
			mIsmenuclick = false;
			return true;
		}
		if (mItemPreviousPosition == itemPosition) {
			return true;
		}
		mItemPreviousPosition = itemPosition;
		switch (itemPosition) {
		case 0:
			callDayView();
			callCalendarView();
			break;
		case 1:
			callWeekAgenda();
			callCalendarView();
			break;
		case 2:
			callMonthAgenda();
			break;
		case 3:
			callAgendaView();
			break;
		}
		return true;
	}

	@Override
	public void onRefreshActionBar(int position) {

		if ((position != -3) && (position == 1)) {
			showNextDay();
		}

		if ((position != 3) && (position == -1)) {
			//Email.todayDate = Email.todayDate - 1;
			showPreviousDay();
		}

	}

	@Override
	public void UpdateActionBar(int day, int month, int Year) {
		Email.todayDate = day;
		//Email.currentdatevalue = day;
		Email.todayMonth = month + 1;
		Email.todayYear = Year;
		mPresentDay = Email.todayDate + " "
				+ Email.getNameOFMonth(Email.todayMonth) + " "
				+ Email.todayYear;
		Email.setPresentDate(mPresentDay);
		if ((mSearchFlag)
				&& (CalendarCommonFunction
						.isSmallScreen(getApplicationContext()))) {
			mSearchFlag = false;
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			mActionBar.setCustomView(mTodayView);
			mNavigationListAdapter = new CalendarNavigationListAdapter(this,
					getResources().getStringArray(R.array.nav_list),
					Email.getNameOFMonth(month + 1) + " " + Year, "",
					Email.getNameOFMonth(Email.todayMonth) + " "
							+ Email.todayDate,
					Email.getNameOFMonth(Email.todayMonth),
					Email.getWeekDays(), getSupportFragmentManager(), "WEEK");
			mActionBar.setListNavigationCallbacks(mNavigationListAdapter,
					CalendarMainActivity.this);
			mActionBar.setSelectedNavigationItem(1);

		}

		else {
			mNavigationListAdapter = new CalendarNavigationListAdapter(this,
					getResources().getStringArray(R.array.nav_list),
					Email.getNameOFMonth(month + 1) + " " + Year, "",
					Email.getNameOFMonth(Email.todayMonth) + " "
							+ Email.todayDate,
					Email.getNameOFMonth(Email.todayMonth),
					Email.getWeekDays(), getSupportFragmentManager(), "WEEK");
			mActionBar.setListNavigationCallbacks(mNavigationListAdapter,
					CalendarMainActivity.this);
			mActionBar.setSelectedNavigationItem(1);

		}
		mIsmenuclick = true;
	}

	protected void onSaveInstanceState(Bundle outState) {

		try {
			if (outState != null) {
				outState.putString(CalendarConstants.FRAGMENT_TYPE, Email
						.getmCurrentFragment().toString());
				outState.putBoolean(CalendarConstants.FLAG_HIDE,
						mFlag_hide_control);
				outState.putString(CalendarConstants.CURRENT_VIEW, mCurrentView);
				outState.putString("CurrentDay", Email.getPresentDay());
				outState.putInt(CalendarConstants.CALENDAR_NAVIGATION_POSITION, mItemPreviousPosition);
				super.onSaveInstanceState(outState);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void callWeekAgenda() {
		
		if(checkUserTimeEnabled())
		{
			
			txt_timeZone.setVisibility(View.VISIBLE);
			
		}
		else
		{
			txt_timeZone.setVisibility(View.GONE);
		}
		if (CalendarCommonFunction.isTablet(getApplicationContext())
				&& (!mFlag_hide_control)) {
			findViewById(R.id.displayDetail)
					.setVisibility(LinearLayout.VISIBLE);
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		CalendarWeekViewFragment WeekViewFragment = new CalendarWeekViewFragment();
		ft.replace(R.id.dayViewFragment, WeekViewFragment,
				CalendarConstants.CALENDAR_WEEK_VIEW);
		ft.addToBackStack(CalendarConstants.CALENDAR_WEEK_VIEW);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();

		mNavigationListAdapter = new CalendarNavigationListAdapter(this,
				getResources().getStringArray(R.array.nav_list),
				Email.getNameOFMonth(Email.todayMonth) + " " + Email.todayYear,
				"", Email.getNameOFMonth(Email.todayMonth) + " "
						+ Email.todayDate,
				Email.getNameOFMonth(Email.todayMonth), Email.getWeekDays(),
				getSupportFragmentManager(), "WEEK");
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		mActionBar.setListNavigationCallbacks(mNavigationListAdapter,
				CalendarMainActivity.this);
		mActionBar.setSelectedNavigationItem(1);
		Email.setmCurrentFragment(CalendarFragmentType.WEEKVIEW);
		mIsmenuclick = true;
		mCurrentView = "WEEK";

	}

	public void callAgendaView() {
		// getSupportFragmentManager().popBackStack();
		if(checkUserTimeEnabled())
		{
			txt_timeZone.setVisibility(View.VISIBLE);
			
		}
		else
		{
			txt_timeZone.setVisibility(View.GONE);
		}
		if (CalendarCommonFunction.isTablet(getApplicationContext())) {
			findViewById(R.id.displayDetail).setVisibility(LinearLayout.GONE);
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		CalendarAgendaFragment agendaFragment = new CalendarAgendaFragment();
		ft.replace(R.id.dayViewFragment, agendaFragment,
				CalendarConstants.CALENDAR_AGENDA_VIEW);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.addToBackStack(CalendarConstants.CALENDAR_AGENDA_VIEW);
		ft.commit();

		if ((mSearchFlag)
				&& CalendarCommonFunction
						.isSmallScreen(getApplicationContext())) {
			inflateSearchOption(mMenu);
		} else {
			mNavigationListAdapter = new CalendarNavigationListAdapter(this,
					getResources().getStringArray(R.array.nav_list),
					Email.getNameOFMonth(Email.todayMonth) + " "
							+ Email.todayYear, "",
					Email.getNameOFMonth(Email.todayMonth) + " "
							+ Email.todayDate,
					Email.getNameOFMonth(Email.todayMonth),
					Email.getWeekDays(), getSupportFragmentManager(), "AGENDA");

			mActionBar.setListNavigationCallbacks(mNavigationListAdapter,
					CalendarMainActivity.this);
			mActionBar.setSelectedNavigationItem(3);
		}
		Email.setmCurrentFragment(CalendarFragmentType.AGENDAVIEW);
		mIsmenuclick = true;
		mCurrentView = "AGENDA";
		
		if(Email.isSearchFlag())
		{
			mMenu.getItem(0).setVisible(true);
			mMenu.getItem(1).setVisible(false);
			mMenu.getItem(2).setVisible(false);
			mMenu.getItem(3).setVisible(false);
			mMenu.getItem(4).setVisible(false);
			
		}
		else
		{
			mMenu.getItem(0).setVisible(false);
			mMenu.getItem(1).setVisible(true);
			mMenu.getItem(2).setVisible(true);
			mMenu.getItem(3).setVisible(true);
			mMenu.getItem(4).setVisible(true);
			
		}
	}

	private void callDayView() {
		
		if(checkUserTimeEnabled())
		{
			
				txt_timeZone.setVisibility(View.VISIBLE);
			
		}
		else
		{
			txt_timeZone.setVisibility(View.GONE);
		}
		if (CalendarCommonFunction.isTablet(getApplicationContext())
				&& (!mFlag_hide_control)) {
			findViewById(R.id.displayDetail)
					.setVisibility(LinearLayout.VISIBLE);
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		CalendarDayViewFragment dayViewFragment = new CalendarDayViewFragment();
		ft.replace(R.id.dayViewFragment, dayViewFragment,
				CalendarConstants.CALENDAR_DAY_VIEW);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.addToBackStack(CalendarConstants.CALENDAR_DAY_VIEW);
		ft.commit();

		mNavigationListAdapter = new CalendarNavigationListAdapter(this,
				getResources().getStringArray(R.array.nav_list), mPresentDay,
				Email.getWeekDayOfDate(Email.todayDate + "/"
						+ Email.todayMonth + "/" + Email.todayYear),
				Email.getNameOFMonth(Email.todayMonth) + " "
						+ Email.todayDate,
				Email.getNameOFMonth(Email.todayMonth), Email.getWeekDays(),
				getSupportFragmentManager(), "DAY");

		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setListNavigationCallbacks(mNavigationListAdapter,
				CalendarMainActivity.this);
		mActionBar.setSelectedNavigationItem(0);
		Email.setmCurrentFragment(CalendarFragmentType.DAYVIEW);
		mIsmenuclick = true;
		mCurrentView = "DAY";
	}

	@Override
	protected void onPause() {
		super.onPause();

		try {
//			unregisterReceiver(receiveCompleteSync);
			Email.unregisterCalendarReceiver(receiveCompleteSync);
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		try {
			
			super.onResume();
			/**** To get the timezone if device timezone is changed for Agenda view ****/
			Date date;
			CalendarUtility.setTimeZone();
			notificationManager.cancel(102);
			setTimeZoneOnTop();
			if(misOrentationChanged){
				// onTodayEventMenuClick();

				misOrentationChanged=false;
//				 onTodayEventMenuClick();
				 if(!oldPresentDay.equalsIgnoreCase("")){
					 String[] arr_PresentDay = oldPresentDay.split(" ");
					 
						
					date = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(arr_PresentDay[1]);
					Calendar cal = Calendar.getInstance();
				    cal.setTime(date);
				    int month = cal.get(Calendar.MONTH);
				    if(arr_PresentDay[0].length()==1)
				    {
				    	arr_PresentDay[0] = "0"+arr_PresentDay[0];
				    }
								
						
					// CognizantEmail.setPresentDate(oldPresentDay);
					 
					/* 	mPresentDay = CognizantEmail.currentdatevalue + " "
								+ CognizantEmail.getNameOFMonth(CognizantEmail.todayMonth)
								+ " " + CognizantEmail.todayYear;
						mToday = CognizantEmail.currentdatevalue + " "
								+ CognizantEmail.getNameOFMonth(CognizantEmail.todayMonth);
						mMonth = CognizantEmail.getNameOFMonth(CognizantEmail.todayMonth);*/
					
						mLayout_today_text.setText(String
								.valueOf(Email.todayDate));
						
					    mToday = arr_PresentDay[0]+" "+arr_PresentDay[1];
					    mMonth = arr_PresentDay[1];
					    Email.todayDate = Integer.parseInt(arr_PresentDay[0]);
					    Email.todayMonth = month+1;
					    Email.todayYear = Integer.parseInt(arr_PresentDay[2]);
						
					    Email.todayDate = Email.todayDate;
					    mPresentDay = Email.getNameOFMonth(Email.todayMonth)
								+ " " + Email.todayDate + ", "
								+ Email.todayYear;
					    
					    String currentDay  = arr_PresentDay[0] + " "
								+ Email.getNameOFMonth(month+1)
								+ " " +arr_PresentDay[2];
					    Email.setPresentDate(currentDay);
						
//						CognizantEmail.setTodayEventFlag(true);
					
						if (Email.getmCurrentFragment().equals(
								CalendarFragmentType.DAYVIEW))
							callDayView();
						if (Email.getmCurrentFragment().equals(
								CalendarFragmentType.WEEKVIEW))
							callWeekAgenda();
						if (Email.getmCurrentFragment().equals(
								CalendarFragmentType.MONTHVIEW))
							callMonthAgenda();
						if (Email.getmCurrentFragment().equals(
								CalendarFragmentType.AGENDAVIEW))
							callAgendaView();

				 }
			
			}
			refreshActivity();
			if (mMenu != null) {
				if (mMenu.findItem(R.id.item_display_controls).getTitle()
						.equals(getString(R.string.calHideControl))
						&& (mFlag_hide_control)) {
					mMenu.findItem(R.id.item_display_controls).setTitle(
							getString(R.string.calShowControl));
				} else {
					mMenu.findItem(R.id.item_display_controls).setTitle(
							getString(R.string.calHideControl));
				}
			}

			IntentFilter filter = new IntentFilter(
					CalendarConstants.INTENT_ACTION_SYNC_COMPLETED);
			registerReceiver(receiveCompleteSync, filter);

		} catch (Exception ex) {

		}
	}

	private void refreshActivity() {
		try {

			if ((mSearchFlag)
					&& (CalendarCommonFunction
							.isSmallScreen(getApplicationContext()))) {
				mSearchFlag = false;
				mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
				mActionBar.setCustomView(mTodayView);
				mNavigationListAdapter = new CalendarNavigationListAdapter(
						this, getResources().getStringArray(R.array.nav_list),
						Email.getNameOFMonth(Email.todayMonth) + " "
								+ Email.todayYear, "",
						Email.getNameOFMonth(Email.todayMonth) + " "
								+ Email.todayDate,
						Email.getNameOFMonth(Email.todayMonth),
						Email.getWeekDays(), getSupportFragmentManager(),
						"AGENDA");

				mActionBar.setListNavigationCallbacks(mNavigationListAdapter,
						CalendarMainActivity.this);
				mActionBar.setSelectedNavigationItem(3);

			}

			CalendarDayViewFragment dayfragment = (CalendarDayViewFragment) getSupportFragmentManager()
					.findFragmentByTag(CalendarConstants.CALENDAR_DAY_VIEW);
			if (dayfragment != null)
				if (dayfragment.isVisible()) {

					if ((findViewById(R.id.displayDetail) != null)
							&& (CalendarCommonFunction.isTablet(Email
									.getAppContext()))) {
						findViewById(R.id.displayDetail).setVisibility(
								LinearLayout.VISIBLE);
					}
					callDayView();
					callCalendarView();
				}
			CalendarWeekViewFragment weekfragment = (CalendarWeekViewFragment) getSupportFragmentManager()
					.findFragmentByTag(CalendarConstants.CALENDAR_WEEK_VIEW);
			if (weekfragment != null)
				if (weekfragment.isVisible()) {
					if ((findViewById(R.id.displayDetail) != null)
							&& (CalendarCommonFunction.isTablet(Email
									.getAppContext()))) {
						findViewById(R.id.displayDetail).setVisibility(
								LinearLayout.VISIBLE);
					}
					callWeekAgenda();
					callCalendarView();
				}
			CalendarMonthViewFragment monthfragment = (CalendarMonthViewFragment) getSupportFragmentManager()
					.findFragmentByTag(CalendarConstants.CALENDAR_MONTH_VIEW);
			if (monthfragment != null)
				if (monthfragment.isVisible()) {
					if ((findViewById(R.id.displayDetail) != null)
							&& (CalendarCommonFunction.isTablet(Email
									.getAppContext()))) {
						findViewById(R.id.displayDetail).setVisibility(
								LinearLayout.GONE);
					}
					callMonthAgenda();
				}
			CalendarAgendaFragment agendafragment = (CalendarAgendaFragment) getSupportFragmentManager()
					.findFragmentByTag(CalendarConstants.CALENDAR_AGENDA_VIEW);
			if (agendafragment != null)
				if (agendafragment.isVisible()) {

					if ((findViewById(R.id.displayDetail) != null)
							&& (CalendarCommonFunction.isTablet(Email
									.getAppContext()))) {
						findViewById(R.id.displayDetail).setVisibility(
								LinearLayout.GONE);
					}
					callAgendaView();
				}

			// CalendarEditEvent editEventfragment = (CalendarEditEvent)
			// getSupportFragmentManager()
			// .findFragmentByTag("EditEventViewFragment");
			// if (editEventfragment != null)
			// if (editEventfragment.isVisible()) {
			// String calendarType = CalendarEditEvent
			// .getCurrentEventType();
			// ContentValues contentValues = null;
			// if (calendarType.equals("CorporateCalendar")) {
			// contentValues = CalendarDatabaseHelper
			// .getEventContentValues(CalendarDatabaseHelper
			// .getEventDetails(Integer
			// .parseInt(CalendarEditEvent
			// .getCurrentEventID())));
			// contentValues.put("calendarType", "CorporateCalendar");
			// } else if (calendarType.equals("Personal_Calendar_V4")) {
			//
			// if (Email.isDevicePreICS()) {
			// contentValues = CalendarDBHelperClassPreICS
			// .getPersonalEventContentValues(CalendarDBHelperClassPreICS.getEventDetails(Integer
			// .parseInt(CalendarEditEvent
			// .getCurrentEventID())));
			// contentValues.put("calendarType", "Personal_Calendar_V4");
			//
			// } else {
			// contentValues = CalendarDBHelperClassPostICS
			// .getPersonalEventContentValues(CalendarDBHelperClassPostICS.getEventDetails(Integer
			// .parseInt(CalendarEditEvent
			// .getCurrentEventID())));
			// contentValues.put("calendarType", "Personal_Calendar_V4");
			//
			// }
			// } else {
			//
			// }
			//
			// onEditEvent(contentValues, CalendarFragmentType.DAYVIEW);
			// // Log.e("Onresume", "onresume visible weekfragment");
			// }
		} catch (Exception ex) {
			CalendarLog.e(CalendarConstants.Tag,
					"RefreshActivity" + ex.toString());
		}
	}

	@Override
	protected void onDestroy() {
		Email.unregisterCalendarReceiver(receiveCompleteSync);
		super.onDestroy();
	}

	@Override
	protected void onStop() {

		super.onStop();
	}

	private void showNextDay() {

		Email.setPageScrollLeft(false);
		Calendar calNextDay = CalendarUtility.getCurrentDay();
		calNextDay.add(Calendar.DAY_OF_YEAR, 1);
//		CognizantEmail.currentdatevalue = CognizantEmail.currentdatevalue + 1;
//		CalendarLog.d(CalendarConstants.Tag, "CognizantEmail.todayMonth "+CognizantEmail.todayMonth);
//		CalendarLog.d(CalendarConstants.Tag, "CognizantEmail.getPresentDay "+CognizantEmail.getPresentDay());
//		if (CognizantEmail.todayMonth == 12) {
//			CognizantEmail.todayMonth = 1;
//			CognizantEmail.todayDate = 1;
//			CognizantEmail.todayYear = CognizantEmail.todayYear + 1;
//			CognizantEmail.currentdatevalue = CognizantEmail.todayDate;
//		}
//		// To check current is last day of month
//		int totalNumberOfDaysInMonth = CognizantEmail.getTotalNumberOfDay(
//				CognizantEmail.todayMonth - 1, CognizantEmail.todayDate,
//				CognizantEmail.todayYear);
//		if (CognizantEmail.currentdatevalue > totalNumberOfDaysInMonth) {
//			CognizantEmail.todayMonth = CognizantEmail.todayMonth + 1;
//			CognizantEmail.todayDate = 1;
//			CognizantEmail.currentdatevalue = CognizantEmail.todayDate;
//		}
		Email.todayDate = calNextDay.get(Calendar.DATE);
		Email.todayMonth = calNextDay.get(Calendar.MONTH)+1;
		Email.todayYear = calNextDay.get(Calendar.YEAR);
//		CognizantEmail.currentdatevalue = CognizantEmail.todayDate;

		mPresentDay = Email.getNameOFMonth(Email.todayMonth)
				+ " " + Email.todayDate + ", "
				+ Email.todayYear;
		mToday = Email.getNameOFMonth(Email.todayMonth) + " "
				+ Email.todayDate;
		mMonth = Email.getNameOFMonth(Email.todayMonth);
		Email.setPresentDate(Email.todayDate + " "
				+ Email.getNameOFMonth(Email.todayMonth)
				+ " " + Email.todayYear);

		mNavigationListAdapter = new CalendarNavigationListAdapter(this, getResources()
				.getStringArray(R.array.nav_list), mPresentDay,
				Email.getWeekDayOfDate(Email.todayDate
						+ "/" + Email.todayMonth + "/"
						+ Email.todayYear), mToday, mMonth,
				CalendarUtility.getWeekDays(), getSupportFragmentManager(), "DAY");

		mActionBar.setListNavigationCallbacks(mNavigationListAdapter,
				CalendarMainActivity.this);
		mActionBar.setSelectedNavigationItem(0);
		mIsmenuclick = true;
	
		/*	Email.setPageScrollLeft(false);
		Email.currentdatevalue = Email.currentdatevalue + 1;
		if (Email.todayMonth == 12) {
			Email.todayMonth = 1;
			Email.todayDate = 1;
			Email.todayYear = Email.todayYear + 1;
			Email.currentdatevalue = Email.todayDate;
		}
		// To check current is last day of month
		int totalNumberOfDaysInMonth = Email.getTotalNumberOfDay(
				Email.todayMonth - 1, Email.todayDate, Email.todayYear);
		if (Email.currentdatevalue > totalNumberOfDaysInMonth) {
			Email.todayMonth = Email.todayMonth + 1;
			Email.todayDate = 1;
			Email.currentdatevalue = Email.todayDate;
		}

		mPresentDay = Email.getNameOFMonth(Email.todayMonth) + " "
				+ Email.currentdatevalue + ", " + Email.todayYear;
		mToday = Email.getNameOFMonth(Email.todayMonth) + " "
				+ Email.currentdatevalue;
		mMonth = Email.getNameOFMonth(Email.todayMonth);
		Email.setPresentDate(Email.currentdatevalue + " "
				+ Email.getNameOFMonth(Email.todayMonth) + " "
				+ Email.todayYear);

		mNavigationListAdapter = new CalendarNavigationListAdapter(this,
				getResources().getStringArray(R.array.nav_list), mPresentDay,
				Email.getWeekDayOfDate(Email.currentdatevalue + "/"
						+ Email.todayMonth + "/" + Email.todayYear), mToday,
				mMonth, Email.getWeekDays(), getSupportFragmentManager(), "DAY");

		mActionBar.setListNavigationCallbacks(mNavigationListAdapter,
				CalendarMainActivity.this);
		mActionBar.setSelectedNavigationItem(0);
		mIsmenuclick = true;
	*/
		
	}

	private void showPreviousDay() {
		
		Email.setPageScrollLeft(true);

//		if (CognizantEmail.currentdatevalue == 0) {
//			CognizantEmail.todayMonth = CognizantEmail.todayMonth - 1;
//			CognizantEmail.todayDate = CognizantEmail.getTotalNumberOfDay(
//					CognizantEmail.todayMonth - 1, CognizantEmail.todayDate,
//					CognizantEmail.todayYear);
//			CognizantEmail.currentdatevalue = CognizantEmail.todayDate;
//		}
//		if (CognizantEmail.todayMonth == 0) {
//			CognizantEmail.todayMonth = 12;
//			CognizantEmail.todayDate = CognizantEmail.getTotalNumberOfDay(
//					CognizantEmail.todayMonth - 1, CognizantEmail.todayDate,
//					CognizantEmail.todayYear);
//			CognizantEmail.todayYear = CognizantEmail.todayYear - 1;
//		}

		Calendar calNextDay = CalendarUtility.getCurrentDay();
		calNextDay.add(Calendar.DAY_OF_YEAR, -1);
		Email.todayDate = calNextDay.get(Calendar.DATE);
		Email.todayMonth = calNextDay.get(Calendar.MONTH)+1;
		Email.todayYear = calNextDay.get(Calendar.YEAR);
//		CognizantEmail.currentdatevalue = CognizantEmail.todayDate;
		
		mPresentDay = Email.getNameOFMonth(Email.todayMonth)
				+ " " + Email.todayDate + ", "
				+ Email.todayYear;
		mToday = Email.getNameOFMonth(Email.todayMonth) + " "
				+ Email.todayDate;
		mMonth = Email.getNameOFMonth(Email.todayMonth);
		Email.setPresentDate(Email.todayDate + " "
				+ Email.getNameOFMonth(Email.todayMonth)
				+ " " + Email.todayYear);

		mNavigationListAdapter = new CalendarNavigationListAdapter(this, getResources()
				.getStringArray(R.array.nav_list), mPresentDay,
				Email.getWeekDayOfDate(Email.todayDate
						+ "/" + Email.todayMonth + "/"
						+ Email.todayYear), mToday, mMonth,
				CalendarUtility.getWeekDays(), getSupportFragmentManager(), "DAY");

		mActionBar.setListNavigationCallbacks(mNavigationListAdapter,
				CalendarMainActivity.this);
		mActionBar.setSelectedNavigationItem(0);
		mIsmenuclick = true;

	/*
		Email.setPageScrollLeft(true);

		if (Email.todayDate == 0) {
			Email.todayMonth = Email.todayMonth - 1;
			Email.todayDate = Email.getTotalNumberOfDay(Email.todayMonth - 1,
					Email.todayDate, Email.todayYear);
			Email.currentdatevalue = Email.todayDate;
		}
		if (Email.todayMonth == 0) {
			Email.todayMonth = 12;
			Email.todayDate = Email.getTotalNumberOfDay(Email.todayMonth - 1,
					Email.todayDate, Email.todayYear);
			Email.todayYear = Email.todayYear - 1;
		}

		mPresentDay = Email.getNameOFMonth(Email.todayMonth) + " "
				+ Email.currentdatevalue + ", " + Email.todayYear;
		mToday = Email.getNameOFMonth(Email.todayMonth) + " "
				+ Email.currentdatevalue;
		mMonth = Email.getNameOFMonth(Email.todayMonth);
		Email.setPresentDate(Email.currentdatevalue + " "
				+ Email.getNameOFMonth(Email.todayMonth) + " "
				+ Email.todayYear);

		mNavigationListAdapter = new CalendarNavigationListAdapter(this,
				getResources().getStringArray(R.array.nav_list), mPresentDay,
				Email.getWeekDayOfDate(Email.currentdatevalue + "/"
						+ Email.todayMonth + "/" + Email.todayYear), mToday,
				mMonth, Email.getWeekDays(), getSupportFragmentManager(), "DAY");

		mActionBar.setListNavigationCallbacks(mNavigationListAdapter,
				CalendarMainActivity.this);
		mActionBar.setSelectedNavigationItem(0);
		mIsmenuclick = true;

	*/}

	public void updateMonth(String month) {

		mNavigationListAdapter = new CalendarNavigationListAdapter(this,
				getResources().getStringArray(R.array.nav_list), month, "",
				Email.getNameOFMonth(Email.todayMonth) + " "
						+ Email.todayDate,
				Email.getNameOFMonth(Email.todayMonth), Email.getWeekDays(),
				getSupportFragmentManager(), "MONTH");
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		mActionBar.setListNavigationCallbacks(mNavigationListAdapter,
				CalendarMainActivity.this);
		mActionBar.setSelectedNavigationItem(2);
		mIsmenuclick = true;
		mCurrentView = "MONTH";

	}

	private void callMonthAgenda() {
		// getSupportFragmentManager().popBackStack();
		
		txt_timeZone.setVisibility(View.GONE);
		if (CalendarCommonFunction.isTablet(getApplicationContext())) {
			findViewById(R.id.displayDetail).setVisibility(LinearLayout.GONE);
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		CalendarMonthFragment monthViewFragment = new CalendarMonthFragment();
		ft.replace(R.id.dayViewFragment, monthViewFragment,
				CalendarConstants.CALENDAR_MONTH_VIEW);
		ft.addToBackStack("CalendarMonthViewFragment");
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();

		mNavigationListAdapter = new CalendarNavigationListAdapter(this,
				getResources().getStringArray(R.array.nav_list),
				Email.getNameOFMonth(Email.todayMonth) + " " + Email.todayYear,
				"", Email.getNameOFMonth(Email.todayMonth) + " "
						+ Email.todayDate,
				Email.getNameOFMonth(Email.todayMonth), Email.getWeekDays(),
				getSupportFragmentManager(), "MONTH");
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setListNavigationCallbacks(mNavigationListAdapter,
				CalendarMainActivity.this);
		mActionBar.setSelectedNavigationItem(2);
		Email.setmCurrentFragment(CalendarFragmentType.MONTHVIEW);
		mIsmenuclick = true;
		mCurrentView = "MONTH";

	}

	/**
	 * OnAgendaView Listener Used to Launch CalendarEditEvent View when event is
	 * clicked in Agenda View
	 */
	public void onEditEvent(ContentValues eventName, CalendarFragmentType type) {
		// if((findViewById(R.id.displayDetail) != null) &&
		// (CalendarCommonFunction.isTablet(Email.getAppContext()))){
		// findViewById(R.id.displayDetail).setVisibility(LinearLayout.GONE);
		// }
		// FragmentTransaction ft =
		// getSupportFragmentManager().beginTransaction();
		// CalendarEditEvent editEventFragment = new
		// CalendarEditEvent(eventName,
		// type);
		// ft.replace(R.id.dayViewFragment, editEventFragment,
		// "EditEventViewFragment");
		// ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		// ft.addToBackStack(null);
		// ft.commit();
		// ismenuclick = true;
	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();
		onBack();

	}

	/*
	 * public void CallMonth(int day, int month, int year) { Email.todayDate =
	 * day; Email.currentdatevalue = day; Email.todayMonth = month;
	 * Email.todayYear = year; mPresentDay = Email.currentdatevalue + " " +
	 * Email.getNameOFMonth(Email.todayMonth) + " " + Email.todayYear;
	 * Email.setPresentDate(mPresentDay); mItemPreviousPosition = 0;
	 * callDayView(); }
	 */

	public void HideActionBar(boolean isShow) {
		if (isShow)
			mActionBar.show();
		else
			mActionBar.hide();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);
		// RefreshActivity();
	}

	@Override
	public void onDayViewScrolled(Calendar cal) {
		CalendarView calView = new CalendarView();
		calView.setSelectionGridView(CalendarMainActivity.this, cal);
	}

	public void callCalendarView() {
		if ((findViewById(R.id.displayDetail) != null)
				&& (CalendarCommonFunction.isTablet(Email.getAppContext()))
				&& (!mFlag_hide_control)) {

			findViewById(R.id.displayDetail)
					.setVisibility(LinearLayout.VISIBLE);
			// getSupportFragmentManager().popBackStack();
			mCalendarView = (CalendarView) getSupportFragmentManager()
					.findFragmentById(R.id.displayDetail);
			// DetailFragment detailFragment =
			if (mCalendarView == null) {

				FragmentTransaction ft1 = getSupportFragmentManager()
						.beginTransaction();
				mCalendarView = new CalendarView();
				ft1.replace(R.id.displayDetail, mCalendarView,
						"CalendarView_Fragment");
				ft1.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft1.commit();
			}
		}
	}

	@Override
	public void updateViewOnCalendarSelection() {
		mPresentDay = Email.getPresentDay();

		if (Email.getmCurrentFragment().equals(CalendarFragmentType.DAYVIEW))
			callDayView();
		if (Email.getmCurrentFragment().equals(CalendarFragmentType.WEEKVIEW))
			callWeekAgenda();
		if (Email.getmCurrentFragment().equals(CalendarFragmentType.MONTHVIEW))
			callMonthAgenda();
		if (Email.getmCurrentFragment().equals(CalendarFragmentType.AGENDAVIEW))
			callAgendaView();
	}

	public void onBack() {

		if ((mSearchFlag)
				&& (CalendarCommonFunction
						.isSmallScreen(getApplicationContext()))) {
			mSearchFlag = false;
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			mActionBar.setCustomView(mTodayView);
			mNavigationListAdapter = new CalendarNavigationListAdapter(this,
					getResources().getStringArray(R.array.nav_list),
					Email.getNameOFMonth(Email.todayMonth) + " "
							+ Email.todayYear, "",
					Email.getNameOFMonth(Email.todayMonth) + " "
							+ Email.todayDate,
					Email.getNameOFMonth(Email.todayMonth),
					Email.getWeekDays(), getSupportFragmentManager(), "AGENDA");

			mActionBar.setListNavigationCallbacks(mNavigationListAdapter,
					CalendarMainActivity.this);
			mActionBar.setSelectedNavigationItem(3);

		}
		FragmentManager fm = getSupportFragmentManager();
		CalendarDayViewFragment dayfragment = (CalendarDayViewFragment) getSupportFragmentManager()
				.findFragmentByTag(CalendarConstants.CALENDAR_DAY_VIEW);
		if (dayfragment != null)
			if (dayfragment.isVisible()) {

				if ((findViewById(R.id.displayDetail) != null)
						&& (CalendarCommonFunction.isTablet(Email
								.getAppContext()))) {
					findViewById(R.id.displayDetail).setVisibility(
							LinearLayout.VISIBLE);
				}
				mActionBar.setSelectedNavigationItem(0);
			}
		CalendarWeekViewFragment weekfragment = (CalendarWeekViewFragment) getSupportFragmentManager()
				.findFragmentByTag(CalendarConstants.CALENDAR_WEEK_VIEW);
		if (weekfragment != null)
			if (weekfragment.isVisible()) {
				if ((findViewById(R.id.displayDetail) != null)
						&& (CalendarCommonFunction.isTablet(Email
								.getAppContext()))) {
					findViewById(R.id.displayDetail).setVisibility(
							LinearLayout.VISIBLE);
				}
				mActionBar.setSelectedNavigationItem(1);
			}
		if (!mActionBar.isShowing())
			mActionBar.show();

		if (fm.getBackStackEntryCount() == 0)
			finish();
	}

	@Override
	public void callMonth(int day, int month, int year) {
		Email.todayDate = day;
		//Email.currentdatevalue = day;
		Email.todayMonth = month;
		Email.todayYear = year;
		mPresentDay = Email.todayDate + " "
				+ Email.getNameOFMonth(Email.todayMonth) + " "
				+ Email.todayYear;
		Email.setPresentDate(mPresentDay);
		mItemPreviousPosition = 0;
		callDayView();

	}

	public void SetStartTime(String startTime) {
		this.mStartTime = startTime;
	}

	public String GetStartTime() {
		return mStartTime;
	}

	public static class PrograssDialogFragment extends DialogFragment {

		public static PrograssDialogFragment newInstance() {
			PrograssDialogFragment frag = new PrograssDialogFragment();
			return frag;
		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final ProgressDialog dialog = new ProgressDialog(getActivity()); //

			dialog.setMessage(" Loading.. Please Wait");
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			// etc...
			return dialog;
		}
	}
	
	
	private boolean checkUserTimeEnabled()
	{
		boolean  checkTimeZoneEnable = mPreferences.getBoolean(getString(R.string.key_parent_calender), false);
		return checkTimeZoneEnable;
	}
	private void setTimeZoneOnTop()
	{
		if(checkUserTimeEnabled())
		{
			
				txt_timeZone.setVisibility(View.VISIBLE);
			
		}
		else
		{
			txt_timeZone.setVisibility(View.GONE);
		}
		final Handler handler = new Handler();
		handler.postDelayed( new Runnable() {

		    @Override
		    public void run() {
		    	checkTime();
		    	String timeZone = mPreferences.getString(getString(R.string.key_home_time_zone_preference), CalendarCommonFunction
						.getDeviceCurrentTimezoneOffset());
				int startIndex = timeZone.indexOf("(");
				int endIndex = timeZone.indexOf(")");
				String timeZoneValue = timeZone.substring(startIndex+1,endIndex);
				TimeZone tz = TimeZone.getTimeZone(timeZoneValue);
				Calendar c = Calendar.getInstance(tz);
				if(c.get(Calendar.MINUTE)<10)
				{
					minValue = "0"+c.get(Calendar.MINUTE);
				}
				else
				{
					minValue = String.valueOf(c.get(Calendar.MINUTE));
				}
				if(is24Hrs)
				{
					finalTimeZone = c.get(Calendar.HOUR_OF_DAY)+":"+minValue+" "+"("+timeZoneValue+")";
				}
				else
				{
					
					finalTimeZone = c.get(Calendar.HOUR)+":"+minValue+" "+am_pm+" "+"("+timeZoneValue+")";
					
				}
				txt_timeZone.setText(finalTimeZone);
				handler.postDelayed( this, 60 * 1000 );
		    }
		}, 0);
		
		
		
	}

	private void checkTime() {
		String userSettings = DateFormat.getTimeFormat(
				Email.getAppContext()).format(
				Email.getNewCalendar().getTime());
		is24Hrs = true;
		if (userSettings.indexOf("PM") != -1) {
			is24Hrs = false;
			am_pm = "pm";
		} else if (userSettings.indexOf("AM") != -1) {
			is24Hrs = false;
			am_pm = "am";
		}

	}
}