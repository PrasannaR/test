package com.cognizant.trumobi.contacts;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockFragmentBaseActivity;
import com.cognizant.trumobi.contacts.activity.ContactsAddContact;
import com.cognizant.trumobi.contacts.activity.ContactsAllContactsFragment;
import com.cognizant.trumobi.contacts.activity.ContactsDetailFragment;
import com.cognizant.trumobi.contacts.activity.ContactsEditContact;
import com.cognizant.trumobi.contacts.activity.ContactsFavoritesFragment;
import com.cognizant.trumobi.contacts.activity.ContactsGroupFragment;
import com.cognizant.trumobi.contacts.activity.ContactsToDisplay;
import com.cognizant.trumobi.contacts.activity.ContactsUserSettingActivity;
import com.cognizant.trumobi.contacts.adapter.ContactPageAdapter;
import com.cognizant.trumobi.contacts.adapter.ContactsViewPager;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;
import com.cognizant.trumobi.contacts.utils.ContactsVcardShare;
import com.cognizant.trumobi.em.EmEmController;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.log.ContactsLog;

@SuppressLint("ValidFragment")
public class ContactsParentActivity extends
		TruMobiBaseSherlockFragmentBaseActivity implements
		OnNavigationListener,
		ContactsAllContactsFragment.ContactsFragmentChangeListner {

	String SelectedRingTone = "";

	Intent Mringtone;
	public Uri CustomRingToneUri;
	RingtoneManager mRingtoneManager;

	// private Contacts myApplication;
	private ContactsViewPager  mPager;
	private Tab tab;
	public static final String TAG = "ParentActivity";
	public static String LOGCAT = "Contacts";
	
	
	private MenuItem menuInvisible;
	private MenuItem menuInvisible2;
	private MenuItem menuInvisible3;
	

	private MenuItem menuShare;

	private MenuItem menuAddContact;
	private MenuItem menuEdit;
	private MenuItem menuSync;
	// private MenuItem menuConnect;
	private MenuItem menuDelete;
	private MenuItem menuSetRingtone;

	private MenuItem menuSearch;
	private MenuItem menucontactstodisplay;
	private MenuItem menusettings;
	private MenuItem menuAddGrpContact;

	private MenuItem menuConnect;

	public static ContactsParentActivity app = null;
	public static final String CERT_DETAILS = "Certificatedetails";
	public static final String CERT_DOMAIN = "cert_domain";
	public static final String CERT_EXCHANGE = "cert_exchange";
	public static final String CERT_CRT_PWD = "cert_pwd";
	public static final String CERT_EMAIL = "cert_email";

	private static final String CURRENT_FRAGMENT_TAG = "fragmentPosition";
	// public static final boolean certificateAuth = true;

	public static final boolean certificateAuth = true;

	// public static final boolean certificateAuth = true;

	public boolean isPersonaExists(String targetPackage) {
		PackageManager pm = getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(targetPackage,
					PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			return false;
		}
		return true;
	}

	String readPrefernce(String key) {
		return new SharedPreferences(Email.getAppContext()).getString(
				key, "empty");
	}

	boolean loadCertificate() {
		/*
		 * String crtDetails = readPrefernce(CERT_DETAILS); if
		 * (!crtDetails.equalsIgnoreCase("empty")) {
		 * 
		 * // load certificate from local db try { byte[] b =
		 * readPrefernce(CERT_DETAILS).getBytes("UTF-8");
		 * 
		 * byte[] str = Base64.decode(b, Base64.NO_WRAP);
		 * 
		 * ExchangeData.setPfxbyteArray(str); } catch (Exception e) { return
		 * false; }
		 * 
		 * ExchangeData.setDomain(readPrefernce(CERT_DOMAIN));
		 * ExchangeData.setPfxPass(readPrefernce(CERT_CRT_PWD));
		 * ExchangeData.setServer(readPrefernce(CERT_EXCHANGE));
		 * ExchangeData.setEmail_ID(readPrefernce(CERT_EMAIL));
		 * 
		 * return true; } else { // we dont have certificate so retrieving it
		 * from persona ... // provide persona package name
		 * 
		 * if (isPersonaExists("com.cognizant.trumobi.persona")) {
		 * 
		 * loadingDialog = new ProgressDialog(this);
		 * loadingDialog.setCancelable(false);
		 * loadingDialog.setCanceledOnTouchOutside(false);
		 * loadingDialog.setMessage("Loading certificate...");
		 * 
		 * loaderTask = new CertificateLoader(); loaderTask.execute();
		 * 
		 * } else { // throw error no persona installed.
		 * 
		 * Message msg = new Message(); Bundle b = new Bundle();
		 * b.putString("msg", "Persona App is not installed"); msg.setData(b);
		 * 
		 * mHandler.sendMessage(new Message()); } } return false;
		 */
		return false;
	}

	Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			Bundle b = msg.getData();
			if (b != null) {
				String message = b.getString("msg");
				// showDialog(message, true);

				AlertDialog alertDialog = new AlertDialog.Builder(
						ContactsParentActivity.this).create();
				alertDialog.setTitle("Certificate Not Generated");
				alertDialog.setMessage(message);
				// alertDialog.setIcon(R.drawable.tick);
				alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						});
				alertDialog.show();
			}
		};
	};
/*
	private class RefreshListener implements EmailRefreshManager.Listener {
		private MenuItem mRefreshIcon;

		@Override
		public void onMessagingError(final long accountId, long mailboxId,
				final String message) {
			updateRefreshIcon();
		}

		@Override
		public void onRefreshStatusChanged(long accountId, long mailboxId) {
			updateRefreshIcon();
		}

		void setRefreshIcon(MenuItem icon) {
			mRefreshIcon = icon;
			updateRefreshIcon();
		}

		private void updateRefreshIcon() {
			// if (mRefreshIcon == null) {
			// return;
			// }
			//
			// if (isRefreshInProgress()) {
			// mRefreshIcon
			// .setActionView(R.layout.email_action_bar_indeterminate_progress);
			// } else {
			// mRefreshIcon.setActionView(null);
			// }
		}
	};

	protected final RefreshListener mRefreshListener = new RefreshListener();
	EmailRefreshManager mRefreshManager;*/
	private String mCurrentAccountName = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_activity_main);
		//mRefreshManager = EmailRefreshManager.getInstance(this);
		app = this;
		final ActionBar mActionBar = getSupportActionBar();

		if (mCurrentAccountName == null) {
			mCurrentAccountName = GetAccountDetails();
		}

		if (ContactsUtilities.isTablet(this)) {

			supportInvalidateOptionsMenu();

			mActionBar.setDisplayShowTitleEnabled(false);
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_LIST);
			ArrayAdapter<CharSequence> list = ArrayAdapter
					.createFromResource(this, R.array.contact_list,
							R.layout.contacts_dropdown_text);
			list.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
			getSupportActionBar().setListNavigationCallbacks(list, this);

			if (savedInstanceState != null) {
				getSupportActionBar().setSelectedNavigationItem(
						savedInstanceState.getInt(CURRENT_FRAGMENT_TAG));
			}

		} else {
			mActionBar.setDisplayShowTitleEnabled(false);
			mActionBar.setDisplayShowHomeEnabled(false);
			// Activate Fragment Manager
			FragmentManager fm = getSupportFragmentManager();
			ViewPager.SimpleOnPageChangeListener ViewPagerListener = new ViewPager.SimpleOnPageChangeListener() {
				@Override
				public void onPageSelected(int position) {
					super.onPageSelected(position);
					// Find the ViewPager Position
					mActionBar.setSelectedNavigationItem(position);
					invalidateOptionsMenu();
				}
			};
			mPager = (ContactsViewPager) findViewById(R.id.pager);
			mPager.setOnPageChangeListener(ViewPagerListener);
			ContactPageAdapter viewpageradapter = new ContactPageAdapter(fm);
			mPager.setAdapter(viewpageradapter);
			// Capture tab button clicks
			ActionBar.TabListener tabListener = new ActionBar.TabListener() {
				@Override
				public void onTabSelected(Tab tab, FragmentTransaction ft) {
					mPager.setCurrentItem(tab.getPosition());
				}

				@Override
				public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				}

				@Override
				public void onTabReselected(Tab tab, FragmentTransaction ft) {
				}
			};

			// Create first Tab
			tab = mActionBar.newTab()
					.setIcon(R.drawable.contacts_ic_tab_groups)
					.setTabListener(tabListener);
			mActionBar.addTab(tab);

			// Create second Tab
			tab = mActionBar.newTab().setIcon(R.drawable.contacts_ic_tab_all)
					.setTabListener(tabListener);
			mActionBar.addTab(tab);

			// Create third Tab
			tab = mActionBar.newTab()
					.setIcon(R.drawable.contacts_ic_tab_starred)
					.setTabListener(tabListener);
			mActionBar.addTab(tab);
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

			mPager.setCurrentItem(1);

			if (savedInstanceState != null) {
				if (savedInstanceState.getInt("tab") != -1) {
					getSupportActionBar().setSelectedNavigationItem(
							savedInstanceState.getInt("tab"));
				}
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (ContactsUtilities.isTablet(this)) {
			outState.putInt(CURRENT_FRAGMENT_TAG, getSupportActionBar()
					.getSelectedNavigationIndex());
		} else {
			outState.putInt("tab", getSupportActionBar()
					.getSelectedNavigationIndex());
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (ContactsUtilities.isTablet(this)) {
			if (getSupportActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_LIST) {
				ContactsLog.d(TAG, "CURRENT_FRAGMENT_TAG  :"
						+ CURRENT_FRAGMENT_TAG);
				getSupportActionBar().setSelectedNavigationItem(
						savedInstanceState.getInt(CURRENT_FRAGMENT_TAG));
			}
		} else {

			if (savedInstanceState.getInt("tab") != -1) {
				getSupportActionBar().setSelectedNavigationItem(
						savedInstanceState.getInt("tab"));
			} else {

				Fragment currentFragment = (Fragment) mPager.getAdapter()
						.instantiateItem(mPager, mPager.getCurrentItem());
				Log.d("ParentActivity",
						"***********************************currentFragment.getTag()"
								+ currentFragment.getTag());

				getSupportActionBar().setNavigationMode(
						ActionBar.NAVIGATION_MODE_STANDARD);

			}

		}

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

		try {

			ContactsLog.d(TAG, "onNavigationItemSelected itemPosition  "
					+ itemPosition + "  itemId " + (int) itemId);
			FragmentManager mFragmentManager = getSupportFragmentManager();
			Fragment currentFragment = mFragmentManager
					.findFragmentById(android.R.id.content);

			switch ((int) itemId) {

			case 0:// All contacts

				menuAddContact.setVisible(true);
				menuSearch.setVisible(true);
				menuSync.setVisible(true);
				menusettings.setVisible(true);
				menuConnect.setVisible(true);
				menuAddGrpContact.setVisible(false);
				menuDelete.setVisible(true);
				menuSetRingtone.setVisible(true);
				menucontactstodisplay.setVisible(true);
				menuShare.setVisible(true);

				ContactsAllContactsFragment allContactsFragment = new ContactsAllContactsFragment();
				FragmentTransaction fragmentTransaction = mFragmentManager
						.beginTransaction();
				if (currentFragment == null) {

					fragmentTransaction.add(android.R.id.content,
							allContactsFragment,
							ContactsAllContactsFragment.TAG).commit();

				} else if (currentFragment.toString().contains(
						(ContactsFavoritesFragment.TAG).toString())
						|| (currentFragment.toString()
								.contains((ContactsGroupFragment.TAG)
										.toString()))) {

					fragmentTransaction.replace(android.R.id.content,
							allContactsFragment,
							ContactsAllContactsFragment.TAG);
					fragmentTransaction.commit();
				}

				break;
			case 1: // Fav

				menuConnect.setVisible(true);

				menuAddContact.setVisible(false);
				menuSearch.setVisible(false);
				menuSync.setVisible(false);
				menusettings.setVisible(true);

				menuAddGrpContact.setVisible(false);
				menuEdit.setVisible(false);
				menuDelete.setVisible(false);
				menuSetRingtone.setVisible(false);
				menucontactstodisplay.setVisible(false);
				menuShare.setVisible(false);

				if (currentFragment.toString().contains(
						(ContactsAllContactsFragment.TAG).toString())
						|| (currentFragment.toString()
								.contains((ContactsGroupFragment.TAG)
										.toString()))) {
					FragmentTransaction Transaction = mFragmentManager
							.beginTransaction();
					ContactsFavoritesFragment favoritesFragment = new ContactsFavoritesFragment();
					Transaction.replace(android.R.id.content,
							favoritesFragment, ContactsFavoritesFragment.TAG);
					Transaction.commit();
				}
				break;
			case 2:// grp

				menusettings.setVisible(true);
				menuConnect.setVisible(true);
				menuAddGrpContact.setVisible(true);

				menuAddContact.setVisible(false);
				menuSearch.setVisible(false);
				menuDelete.setVisible(false);
				menuSetRingtone.setVisible(false);
				menuShare.setVisible(false);
				menucontactstodisplay.setVisible(false);
				menuSync.setVisible(false);
				menuEdit.setVisible(false);

				ContactsGroupFragment grpContactsfragment = new ContactsGroupFragment();

				if (currentFragment.toString().contains(
						(ContactsAllContactsFragment.TAG).toString())
						|| (currentFragment.toString()
								.contains((ContactsFavoritesFragment.TAG)
										.toString()))) {
					FragmentTransaction fTransaction = mFragmentManager
							.beginTransaction();
					fTransaction.replace(android.R.id.content,
							grpContactsfragment, ContactsGroupFragment.TAG);

					fTransaction.commit();
				}

				break;
			}
		} catch (Exception e) {

			ContactsLog.d(TAG, "onNavigationItemSelected " + e.toString());
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		ContactsLog.d(TAG, "onCreateOptionsMenu ");
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.contacts_people_options, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menuAddContact = menu.findItem(R.id.menu_add_contact);
		menuSearch = menu.findItem(R.id.menu_search);
		menuSync = menu.findItem(R.id.menu_manual_sync);
		menucontactstodisplay = menu.findItem(R.id.contactstodisplay);
		menusettings = menu.findItem(R.id.gsettings);
		menuConnect = menu.findItem(R.id.menu_show_connect);
		menuAddGrpContact = menu.findItem(R.id.menu_add_group_contact);
		menuEdit = menu.findItem(R.id.menu_edit);
		menuDelete = menu.findItem(R.id.menu_delete);
		menuSetRingtone = menu.findItem(R.id.menu_SetRingtone);
		menuShare = menu.findItem(R.id.menu_share);
		
		
		menuInvisible =  menu.findItem(R.id.menu_invisible);
		menuInvisible2 =  menu.findItem(R.id.menu_invisible2);
		menuInvisible3 =  menu.findItem(R.id.menu_invisible3);
		
		
		ContactsLog.d(TAG, "onPrepareOptionsMenu ");

		menuConnect.setVisible(true);

		if (getSupportActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS) {

			int pageItem = mPager.getCurrentItem();

			ContactsLog.e(TAG, " pageItem " + pageItem);

			if (pageItem == 1) { // all contacts
				menuAddContact.setVisible(true);
				menuSearch.setVisible(true);
				menuSync.setVisible(true);
				menusettings.setVisible(true);
				menucontactstodisplay.setVisible(true);
				menuAddGrpContact.setVisible(false);
				
				
				menuInvisible.setVisible(false);
				menuInvisible2.setVisible(false);
				menuInvisible3.setVisible(false);
				
				menuInvisible.setEnabled(false);
				menuInvisible2.setEnabled(false);
				menuInvisible3.setVisible(false);
				

			} else { // Fa

				menuAddContact.setVisible(false);
				menuSearch.setVisible(false);
				menuSync.setVisible(false);
				menusettings.setVisible(false);
				menucontactstodisplay.setVisible(false);
				menuAddGrpContact.setVisible(false);
				
				menuInvisible.setVisible(true);
				menuInvisible2.setVisible(true);
				menuInvisible3.setVisible(true);
				
				
				menuInvisible.setEnabled(false);
				menuInvisible2.setEnabled(false);
				menuInvisible3.setVisible(false);

				menuAddContact.setVisible(false);
				menuSearch.setVisible(false);
				menuSync.setVisible(false);
				menucontactstodisplay.setVisible(false);

				if (pageItem == 0) { // Group tab
					menusettings.setVisible(true);
					menuAddGrpContact.setVisible(true);
					
					
					if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						menuInvisible.setVisible(false);
						menuInvisible2.setVisible(false);
						menuInvisible3.setVisible(false);
						menuInvisible.setEnabled(false);
						menuInvisible2.setEnabled(false);
						menuInvisible3.setEnabled(false);
					}else{
						menuInvisible.setVisible(true);
						menuInvisible2.setVisible(true);
						menuInvisible3.setVisible(true);
						menuInvisible.setEnabled(false);
						menuInvisible2.setEnabled(false);
						menuInvisible3.setEnabled(false);
					}

				}
			}

		} else if (getSupportActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_LIST) {

			if (getSupportActionBar().getSelectedNavigationIndex() == 0) {// allcontacts
				menuAddContact.setVisible(true);
				menuSearch.setVisible(true);
				menuSync.setVisible(true);
				menusettings.setVisible(true);
				menuAddGrpContact.setVisible(false);
				menuDelete.setVisible(true);
				menuSetRingtone.setVisible(true);
				menucontactstodisplay.setVisible(true);
				menuShare.setVisible(true);
				menuEdit.setVisible(true);

			} else {// fav & groups

				menuAddContact.setVisible(false);
				menuSearch.setVisible(false);
				menuSync.setVisible(false);
				menusettings.setVisible(true);
				menuAddGrpContact.setVisible(false);
				menuEdit.setVisible(false);
				menuDelete.setVisible(false);
				menuSetRingtone.setVisible(false);
				menucontactstodisplay.setVisible(false);
				menuShare.setVisible(false);

				if (getSupportActionBar().getSelectedNavigationIndex() == 2) {// groups
					menusettings.setVisible(true);
					menuAddGrpContact.setVisible(true);
				}

			}

		}

		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	}

	@Override
	public void onOptionsMenuClosed(android.view.Menu menu) {
		// TODO Auto-generated method stub
		super.onOptionsMenuClosed(menu);

		Log.d("ParentActivity", "**********onOptionsMenuClosed***");

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final FragmentManager fragmentManager = getSupportFragmentManager();
		final FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();

		ContactsLog.d("onOptionsItemSelected", "Menu item selected. activity.."
				+ item);
		switch (item.getItemId()) {

		case android.R.id.home:

			Log.d("ParentActivity", "**********home clicked***");
			getSherlock().getActionBar().setDisplayHomeAsUpEnabled(false);
			Intent homeIntent = new Intent(this, ContactsParentActivity.class);
			homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeIntent);
			break;
		case R.id.menu_search:

			break;

		case R.id.menu_manual_sync:
			// myApplication.sync();

			if (isNetworkConnected()) {
				Log.d("Contacts Parent activity onOptionsItemSelected", "menu_manual_sync ");
				Sync();
			} else {
				Toast.makeText(this,
						R.string.ContactsInternetConnectionNotAvail,
						Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.menu_share:
			// ContactsDetailAct.ConvertVcardShare();

			if (ContactsUtilities.SELECTED_ID != null) {

				new ContactsVcardShare(ContactsParentActivity.this,
						ContactsUtilities.SELECTED_ID);

			}

			break;

		case R.id.menu_settings:
			// Toast.makeText(this, "Setting selected",
			// Toast.LENGTH_SHORT).show();
			break;

		case R.id.menu_show_connect:

			InputMethodManager immConnect = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

			if (immConnect.isAcceptingText()) {
				// writeToLog("Software Keyboard was shown");

				immConnect.toggleSoftInput(
						InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

			} else {
				// writeToLog("Software Keyboard was not shown");
			}

			SharedPreferences.Editor prefEditor = new SharedPreferences(
					Email.getAppContext()).edit();
			prefEditor.putBoolean("SearchState", false);
			prefEditor.commit();
			finish();

			break;

		case R.id.contactstodisplay:

			Intent intent = new Intent(ContactsParentActivity.this,
					ContactsToDisplay.class);
			startActivity(intent);
			break;

		case R.id.gsettings:

			// setting for sort
			Intent settingsIntent = new Intent(this,
					ContactsUserSettingActivity.class);
			startActivity(settingsIntent);

			break;

		case R.id.menu_add_contact:
			// Toast.makeText(this, "Accounts", Toast.LENGTH_SHORT).show();

			// if (mCurrentAccountName != null) {
			Intent intent3 = new Intent(ContactsParentActivity.this,
					ContactsAddContact.class);
			startActivity(intent3);
			// } else {
			// Toast.makeText(
			// ContactsParentActivity.this,
			// "Account informations is are not available. please try again",
			// Toast.LENGTH_SHORT).show();
			// }

			break;

		case R.id.menu_edit:
			// Toast.makeText(this, "Accounts", Toast.LENGTH_SHORT).show();
			if (ContactsUtilities.SELECTED_ID != null) {

				Log.e("Edit ContactsUtilities.SELECTED_ID", ""
						+ ContactsUtilities.SELECTED_ID.toString());

				Intent editContactIntent = new Intent(
						ContactsParentActivity.this, ContactsEditContact.class);
				editContactIntent.putExtra("selected_id",
						ContactsUtilities.SELECTED_ID);
				startActivity(editContactIntent);
			}
			break;

		case R.id.menu_delete:
			// Toast.makeText(this, "Accounts", Toast.LENGTH_SHORT).show();
			if (ContactsUtilities.SELECTED_ID != null) {

				new MyDialog().show(getSupportFragmentManager(), "dialog");
			}
			break;

		case R.id.menu_SetRingtone:

			if (ContactsUtilities.SELECTED_ID != null) {

				SelectedRingTone = "";

				Cursor RingtoneCursor = getContentResolver().query(
						ContactsConsts.CONTENT_URI_CONTACTS, null, "_id = ?",
						new String[] { ContactsUtilities.SELECTED_ID }, null);

				if (RingtoneCursor != null) {
					if (RingtoneCursor.moveToFirst()) {

						if (RingtoneCursor
								.getString(RingtoneCursor
										.getColumnIndex(ContactsConsts.CONTACT_RINGTONE_PATH)) != null) {
							SelectedRingTone = RingtoneCursor
									.getString(RingtoneCursor
											.getColumnIndex(ContactsConsts.CONTACT_RINGTONE_PATH));
						}
					}
				}

			}

			Mringtone = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
			// Mringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
			// RingtoneManager.TYPE_NOTIFICATION);
			Mringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
					RingtoneManager.TYPE_ALL);
			Mringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
					"Select RingTone");
			// Mringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
			// (Uri) null);
			if (SelectedRingTone != null) {

				Mringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
						(Uri) Uri.parse(SelectedRingTone));
			} else {
				Mringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
						(Uri) null);
			}

			this.startActivityForResult(Mringtone, 0);
			break;

		default:
			break;
		}
		return false;
	}

	private void updateLocalCacheForDelete() {
		ContentValues deleteValues = new ContentValues();
		Cursor deletedValueCursor = getContentResolver().query(
				ContactsConsts.CONTENT_URI_CONTACTS,
				new String[] { ContactsConsts.CONTACT_SERVER_ID }, "_id = ?",
				new String[] { ContactsUtilities.SELECTED_ID }, null);
		if (deletedValueCursor != null) {

			if (deletedValueCursor.moveToFirst()) {
				String deletedServerID = deletedValueCursor
						.getString(deletedValueCursor
								.getColumnIndex(ContactsConsts.CONTACT_SERVER_ID));
				/*
				 * System.out.println("*****DELETED SERVER_ID*****" +
				 * deletedServerID);
				 */
				if (deletedServerID != null && deletedServerID.length() > 0) {
					deleteValues.put(ContactsConsts.CONTACT_SERVER_ID,
							deletedServerID);
					getContentResolver()
							.insert(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_DELETE,
									deleteValues);

					ContactsModel deletedContact = new ContactsModel();
					deletedContact.setServerId(deletedServerID);
					ArrayList<ContactsModel> deleteList = new ArrayList<ContactsModel>();
					deleteList.add(deletedContact);

					// Contacts contact = (Contacts) getApplication();
					// contact.getActiveSyncManager().setCurrentRequest(
					// Contacts.SYNC_CONTACTS_DELETE);
					// contact.getActiveSyncManager().setMlocalChanges(deleteList);
					//
					// EasSynctask deleteTask = new EasSynctask(this,
					// Contacts.STATUS_CONTACTS_SYNC);
					// deleteTask.execute(contact.getActiveSyncManager());
				}
				Log.d("Contacts parent activity updateLocalCacheForDelete", "update del cache ");
				Sync();

			}
		}
	}

	void Sync() {
		try {
			Cursor c = getContentResolver()
					.query(EmEmailContent.Mailbox.CONTENT_URI, null, EmEmailContent.Account.DISPLAY_NAME + "=?", new String[] { "Contacts" }, null);

			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				String Accname = c.getString(c.getColumnIndex(EmEmailContent.Mailbox.ACCOUNT_KEY));
				String folderId = c.getString(c.getColumnIndex(Mailbox.RECORD_ID));

				//mRefreshManager.refreshMessageList(Long.parseLong(Accname), Long.parseLong(folderId), true);

				
				EmEmController controller = EmEmController.getInstance(getApplication());
				controller.updateMailbox(Long.parseLong(Accname), Long.parseLong(folderId), null);
				
			}
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			Log.e("Contacts parent actvity", "menu_manual_sync " + e.toString());
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (!ContactsUtilities.isTablet(this)) {
			finish();
		} else {
			SharedPreferences.Editor prefEditor = new SharedPreferences(
					Email.getAppContext()).edit();
			prefEditor.putBoolean("SearchState", false);
			prefEditor.commit();
		}
		super.onBackPressed();
	}

	Handler DialogHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			if (msg.arg1 == 1) {

				Bundle b = msg.getData();
				if (b != null) {
					String message = b.getString("msg");
					showDialog(message, true);
				}

			} else {
				showDialog("", false);
			}
		};
	};

	ProgressDialog searchDialog;

	void showDialog(String msg, boolean show) {

		if (searchDialog != null) {
			searchDialog.cancel();
			searchDialog = null;
		}
		if (show) {
			searchDialog = new ProgressDialog(this);
			searchDialog.setCancelable(false);
			searchDialog.setCanceledOnTouchOutside(false);

			searchDialog.setMessage(msg);
			searchDialog.show();
		}
	}

	String GetAccountDetails() {

		Cursor c = null;
		try {

			c = getContentResolver()
					.query(EmEmailContent.Mailbox.CONTENT_URI, null,
							EmEmailContent.Mailbox.DISPLAY_NAME + "=?",
							new String[] { "Contacts" }, null);

			if (c != null && c.getCount() > 0) {
				c.moveToFirst();

				return c.getString(c.getColumnIndex(EmEmailContent.Mailbox.ACCOUNT_KEY));

			}

		} catch (Exception e) {

			ContactsLog.e(TAG, "Unable to get Account info");

		} finally {
			if (c != null) {
				c.close();
			}
		}
		return null;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != RESULT_CANCELED) {
			switch (resultCode) {
			case RESULT_OK:

				CustomRingToneUri = data
						.getParcelableExtra(mRingtoneManager.EXTRA_RINGTONE_PICKED_URI);

				if (CustomRingToneUri != null) {

					getContentResolver().update(
							ContactsConsts.CONTENT_URI_CONTACTS,
							contactRingTone(CustomRingToneUri.toString()),
							ContactsConsts.CONTACT_ID + "=?",
							new String[] { ContactsUtilities.SELECTED_ID });

					RingtoneManager.setActualDefaultRingtoneUri(this,
							RingtoneManager.TYPE_RINGTONE, CustomRingToneUri);
					Log.i("Sample", "CustomRingToneUri " + CustomRingToneUri);
					break;
				}
			}
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private ContentValues contactRingTone(String Ringtonepath) {
		ContentValues updateValues = new ContentValues();
		updateValues.put(ContactsConsts.CONTACT_RINGTONE_PATH, Ringtonepath);
		return updateValues;
	}

	private boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public class MyDialog extends DialogFragment {

		@SuppressLint("ValidFragment")
		public MyDialog() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
					.setMessage("This contact will be deleted.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									updateLocalCacheForDelete();
									getContentResolver()
											.delete(ContactsConsts.CONTENT_URI_CONTACTS,
													ContactsConsts.CONTACT_ID + "=?",
													new String[] { ContactsUtilities.SELECTED_ID });

									// Utilities.SELECTED_ID = null;

									FragmentManager fragmentManager = getActivity()
											.getSupportFragmentManager();
									FragmentTransaction fragmentTransaction = fragmentManager
											.beginTransaction();
									ContactsAllContactsFragment allContactsFragment = new ContactsAllContactsFragment();
									fragmentTransaction.replace(
											android.R.id.content,
											allContactsFragment,
											ContactsAllContactsFragment.TAG);

									fragmentTransaction.commit();
									// myApplication.sync();
									// finish();
									

									SharedPreferences.Editor edit = new SharedPreferences(
											Email.getAppContext()).edit();
									edit.putInt("currentListIndex", 0);
									edit.putString("currentListRowID", null);
									edit.commit();
									
								}
							}).setNegativeButton("Cancel", null).create();
		}

		/**
		 * workaround for issue #17423
		 */
		@Override
		public void onDestroyView() {
			if (getDialog() != null && getRetainInstance())
				getDialog().setOnDismissListener(null);

			super.onDestroyView();
		}
	}

	@Override
	public void onUpdate(ContactsModel model) {
		ContactsDetailFragment detailFragment = (ContactsDetailFragment) getSupportFragmentManager()
				.findFragmentByTag(ContactsDetailFragment.TAG);

		if (detailFragment != null) {
			detailFragment.updateModel(model);
		}
	}
	
	
	

	// Method called when Home is selected.
	public void onUserLeaveHint() { 
	       // do stuff
	       super.onUserLeaveHint();
	       SharedPreferences.Editor prefEditor = new SharedPreferences(
					Email.getAppContext()).edit();
			prefEditor.putBoolean("isPrefUpdated", true);
			prefEditor.commit();
	}

	
	@Override
    public void onSearchInitiated(boolean isOnSearchView) {
           // TODO Auto-generated method stub
           if (mPager != null) {
                  mPager.setSwipeable(!isOnSearchView);
           }
    }


}