/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cognizant.trumobi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Parcelable;
import android.provider.CallLog;
import android.provider.LiveFolders;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.SparseArray;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.PersonaActionButton.SwipeListener;
import com.cognizant.trumobi.PersonaDockBar.DockBarListener;
import com.cognizant.trumobi.catalogue.PersonaAppCatalogueFilter;
import com.cognizant.trumobi.catalogue.PersonaAppCatalogueFilters;
import com.cognizant.trumobi.catalogue.PersonaAppGroupAdapter;
import com.cognizant.trumobi.catalogue.PersonaAppInfoMList;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.DialerLog;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.messenger.db.SmsIndividualTable;
import com.cognizant.trumobi.messenger.sms.SmsListdisplay;
import com.cognizant.trumobi.persona.PersonaSecurityProfileUpdate;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.content.PersonaLauncherIntent;
import com.cognizant.trumobi.persona.content.PersonaLauncherMetadata;
import com.cognizant.trumobi.persona.quickaction.PersonaQuickActionWindow;
import com.thinkfree.dexdex.DexDex;

/**
 * Default launcher application.
 */
/**
 * KEYCODE		AUTHOR			PURPOSE
 * BADGE		Brindha			To implement badge in Dialer and messenger
 * WIDGET		Brindha			To update already added widget after application replace
 * @author rudhra resolved widget/shortvut dialog issue
 * 			
 *
 */

public final class PersonaLauncher extends TruMobiBaseActivity implements
		View.OnClickListener, OnLongClickListener,
		OnSharedPreferenceChangeListener, SwipeListener {
	static final String LOG_TAG = "PersonaLauncher";
	static final boolean LOGD = false;

	private static final boolean PROFILE_STARTUP = false;
	private static final boolean PROFILE_ROTATE = false;
	private static final boolean DEBUG_USER_INTERFACE = false;

	private static final int MENU_GROUP_ADD = 1;
	private static final int MENU_GROUP_CATALOGUE = 2;
	private static final int MENU_GROUP_NORMAL = 3;

	private static final int MENU_ADD = Menu.FIRST + 1;
	private static final int MENU_WALLPAPER_SETTINGS = MENU_ADD + 1;
	private static final int MENU_SEARCH = MENU_WALLPAPER_SETTINGS + 1;
	private static final int MENU_NOTIFICATIONS = MENU_SEARCH + 1;
	private static final int MENU_SETTINGS = MENU_NOTIFICATIONS + 1;
	private static final int MENU_ALMOSTNEXUS = MENU_SETTINGS + 1;
	private static final int MENU_APP_GRP_CONFIG = MENU_SETTINGS + 2;
	private static final int MENU_APP_GRP_RENAME = MENU_SETTINGS + 3;
	private static final int MENU_APP_SWITCH_GRP = MENU_SETTINGS + 4;
	private static final int MENU_APP_DELETE_GRP = MENU_SETTINGS + 5;
	private static final int MENU_LOCK_DESKTOP = MENU_SETTINGS + 6;

	private static final int REQUEST_CREATE_SHORTCUT = 1;
	private static final int REQUEST_CREATE_LIVE_FOLDER = 4;
	private static final int REQUEST_CREATE_APPWIDGET = 5;
	private static final int REQUEST_PICK_APPLICATION = 6;
	private static final int REQUEST_PICK_SHORTCUT = 7;
	private static final int REQUEST_PICK_LIVE_FOLDER = 8;
	private static final int REQUEST_PICK_APPWIDGET = 9;
	private static final int REQUEST_PICK_ANYCUT = 10;
	private static final int REQUEST_SHOW_APP_LIST = 11;
	private static final int REQUEST_EDIT_SHIRTCUT = 12;

	static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

	static final String EXTRA_CUSTOM_WIDGET = "custom_widget";
	// static final String SEARCH_WIDGET = "search_widget";

	static final int WALLPAPER_SCREENS_SPAN = 2;
	static final int SCREEN_COUNT = 5;
	static final int DEFAULT_SCREN = 2;
	static final int NUMBER_CELLS_X = 4;
	static final int NUMBER_CELLS_Y = 4;

	private static final int DIALOG_CREATE_SHORTCUT = 1;
	static final int DIALOG_RENAME_FOLDER = 2;
	static final int DIALOG_CHOOSE_GROUP = 3;
	static final int DIALOG_NEW_GROUP = 4;
	static final int DIALOG_DELETE_GROUP_CONFIRM = 5;

	private static final String PREFERENCES = "launcher.preferences";

	// Type: int
	private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
	// Type: boolean
	private static final String RUNTIME_STATE_ALL_APPS_FOLDER = "launcher.all_apps_folder";
	// Type: long
	private static final String RUNTIME_STATE_USER_FOLDERS = "launcher.user_folder";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cellX";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cellY";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_spanX";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_spanY";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_COUNT_X = "launcher.add_countX";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_COUNT_Y = "launcher.add_countY";
	// Type: int[]
	private static final String RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS = "launcher.add_occupied_cells";
	// Type: boolean
	private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
	// Type: long
	private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";
	// Type: boolean
	private static final String RUNTIME_STATE_DOCKBAR = "launcher.dockbar";

	private static final PersonaLauncherModel sModel = new PersonaLauncherModel();

	private static final Object sLock = new Object();
	private static int sScreen = DEFAULT_SCREN;

	private final BroadcastReceiver mApplicationsReceiver = new ApplicationsIntentReceiver();
	private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsIntentReceiver();
	private final ContentObserver mObserver = new FavoritesChangeObserver();
	private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();

	private LayoutInflater mInflater;

	private PersonaDragLayer mDragLayer;
	private PersonaWorkspace mWorkspace;

	private AppWidgetManager mAppWidgetManager;
	private PersonaLauncherAppWidgetHost mAppWidgetHost;

	static final int APPWIDGET_HOST_ID = 1024;

	//Badge
		private final int UPDATE_COUNTERS_FOR_DIALER = 1;
		private final int UPDATE_COUNTERS_FOR_MESSENGER = 2;
		private ContentObserver mDialerBadgeObserver,mMessengerBadgeObserver ;
		Context mContext;
	
	private PersonaCellLayout.CellInfo mAddItemCellInfo;
	private PersonaCellLayout.CellInfo mMenuAddInfo;
	private final int[] mCellCoordinates = new int[2];
	private PersonaFolderInfo mFolderInfo;

	// Create Media Player object

	MediaPlayer _shootMP;// = MediaPlayer.create(this,
							// Uri.parse("file:///system/media/audio/ui/VideoRecord.ogg"));

	/**
	 * ADW: now i use an PersonaActionButton instead of a fixed app-drawer
	 * button
	 */
	private PersonaActionButton mHandleView;
	/**
	 * mAllAppsGrid will be "PersonaAllAppsGridView" or
	 * "PersonaAllAppsSlidingView" depending on user settings, so I cast it
	 * later.
	 */
	private PersonaDrawer mAllAppsGrid;

	private boolean mDesktopLocked = true;
	private Bundle mSavedState;

	private SpannableStringBuilder mDefaultKeySsb = null;

	private boolean mDestroyed;

	private boolean mIsNewIntent;

	private boolean mRestoring;
	private boolean mWaitingForResult;
	private boolean mLocaleChanged;

	private Bundle mSavedInstanceState;

	private DesktopBinder mBinder;
	/**
	 * ADW: New views/elements for dots, dockbar, lab/rab, etc
	 */
	private ImageView mPreviousView;
	private ImageView mNextView;
	private PersonaMiniLauncher mMiniLauncher;
	private PersonaDockBar mDockBar;
	private PersonaActionButton mLAB;
	private PersonaActionButton mRAB;
	private PersonaActionButton mLAB2;
	private PersonaActionButton mRAB2;
	private View mDrawerToolbar;
	private PersonaDeleteZone mDeleteZone;
	/**
	 * ADW: variables to store actual status of elements
	 */
	private boolean allAppsOpen = false;
	private final boolean allAppsAnimating = false;
	private boolean showingPreviews = false;
	private boolean mShouldHideStatusbaronFocus = false;
	/**
	 * ADW: A lot of properties to store the custom settings
	 */
	private boolean allowDrawerAnimations = true;
	private int audiosettings;
	public static int wsettings;
	private boolean newPreviews = true;
	private boolean hideStatusBar = false;
	private boolean showDots = true;
	private boolean showDockBar = true;
	private boolean autoCloseDockbar;
	protected boolean autoCloseFolder;
	private boolean hideABBg = false;
	private float uiScaleAB = 0.5f;
	private boolean uiABTint = false;
	private int uiABTintColor = 0xffffffff;
	private boolean uiHideLabels = false;
	private boolean wallpaperHack = true;
	private PersonaDesktopIndicator mDesktopIndicator;
	private int savedOrientation;
	private boolean useDrawerCatalogNavigation = true;
	protected int mTransitionStyle = 1;
	private int appDrawerPadding = -1;

	public boolean isDesktopBlocked() {
		return mBlockDesktop;
	}

	private boolean mBlockDesktop = true;
	/**
	 * ADW: Home/Swype down binding constants
	 */
	protected static final int BIND_NONE = 0;
	protected static final int BIND_DEFAULT = 1;
	protected static final int BIND_HOME_PREVIEWS = 2;
	protected static final int BIND_PREVIEWS = 3;
	protected static final int BIND_APPS = 4;
	protected static final int BIND_STATUSBAR = 5;
	protected static final int BIND_NOTIFICATIONS = 6;
	protected static final int BIND_HOME_NOTIFICATIONS = 7;
	protected static final int BIND_DOCKBAR = 8;
	protected static final int BIND_APP_LAUNCHER = 9;
	private static final String isFirstRunning = "isFirstRunning";
	private SharedPreferences prefs;

	private int mHomeBinding = BIND_PREVIEWS;

	/**
	 * wjax: Swipe Down binding enum
	 */
	private int mSwipedownAction = BIND_NOTIFICATIONS;
	/**
	 * wjax: Swipe UP binding enum
	 */
	private int mSwipeupAction = BIND_NOTIFICATIONS;
	/**
	 * ADW:Wallpaper intent receiver
	 */
	// private static WallpaperIntentReceiver sWallpaperReceiver;
	private boolean mShouldRestart = false;
	private boolean mMessWithPersistence = false;
	// ADW Theme constants
	public static final int THEME_ITEM_BACKGROUND = 0;
	public static final int THEME_ITEM_FOREGROUND = 1;
	public static final String THEME_DEFAULT = "ADW.Default theme";
	private Typeface themeFont = null;
	private boolean mIsEditMode = false;
	private View mScreensEditor = null;
	private boolean mIsWidgetEditMode = false;
	private PersonaLauncherAppWidgetInfo mlauncherAppWidgetInfo = null;
	// /TODO:ADW. Current code fully ready for upto 9
	// but need to add more drawables for the desktop dots...
	// or completely redo the desktop dots implementation
	private final static int MAX_SCREENS = 7;
	// ADW: NAVIGATION VALUES FOR THE NEXT/PREV CATALOG ACTIONS
	private final static int ACTION_CATALOG_PREV = 1;
	private final static int ACTION_CATALOG_NEXT = 2;
	// ADW: Custom counter receiver
	private PersonaCounterReceiver mCounterReceiver;
	/**
	 * ADW: Different main dock styles/configurations
	 */
	protected static final int DOCK_STYLE_NONE = 0;
	protected static final int DOCK_STYLE_3 = 1;
	protected static final int DOCK_STYLE_5 = 2;
	protected static final int DOCK_STYLE_1 = 3;
	private int mDockStyle = DOCK_STYLE_3;
	// DRAWER STYLES
	private final int[] mDrawerStyles = { R.layout.pr_old_drawer,
			R.layout.pr_new_drawer };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		PersonaLog.d("personalauncher", "---oncreate of personalauncher-----");
		mMessWithPersistence = PersonaAlmostNexusSettingsHelper
				.getSystemPersistent(this);
		if (mMessWithPersistence) {
			changeOrientation(
					PersonaAlmostNexusSettingsHelper
							.getDesktopOrientation(this),
					true);
			// setPersistent(true);
		} else {
			// setPersistent(false);
			changeOrientation(
					PersonaAlmostNexusSettingsHelper
							.getDesktopOrientation(this),
					false);
		}
		mBlockDesktop = PersonaAlmostNexusSettingsHelper
				.getDesktopBlocked(this);
		super.onCreate(savedInstanceState);
		mInflater = getLayoutInflater();

		PersonaAppCatalogueFilters.getInstance().init(this);
		PersonaLauncherActions.getInstance().init(this);

		mAppWidgetManager = AppWidgetManager.getInstance(this);

		mAppWidgetHost = new PersonaLauncherAppWidgetHost(this,
				APPWIDGET_HOST_ID);
		mAppWidgetHost.startListening();

		if (PROFILE_STARTUP) {
			android.os.Debug.startMethodTracing("/sdcard/launcher");
		}
		updateAlmostNexusVars();
		checkForLocaleChange();
		// setWallpaperDimension();
		setContentView(R.layout.pr_launcher);
		setupViews();

		registerIntentReceivers();
		registerContentObservers();

		mSavedState = savedInstanceState;
		restoreState(mSavedState);

		if (PROFILE_STARTUP) {
			android.os.Debug.stopMethodTracing();
		}

		if (!mRestoring) {
			startLoaders();
		}

		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags |= WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
		getWindow().setAttributes(attrs);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		hideStatusBar = false;

		// 290778 removed rotation animation

		/*
		 * final RotateAnimation rotate = (RotateAnimation)
		 * AnimationUtils.loadAnimation(this, R.anim.pr_rotate);
		 * mDragLayer.setAnimation(rotate);
		 * 
		 * rotate.setAnimationListener(new AnimationListener() {
		 * 
		 * @Override public void onAnimationStart(Animation animation) {
		 * 
		 * }
		 * 
		 * @Override public void onAnimationRepeat(Animation animation) {
		 * 
		 * }
		 * 
		 * @Override public void onAnimationEnd(Animation animation) {
		 * 
		 * // go non-full screen WindowManager.LayoutParams attrs =
		 * getWindow().getAttributes(); attrs.flags &=
		 * (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
		 * getWindow().setAttributes(attrs);
		 * getWindow().clearFlags(WindowManager
		 * .LayoutParams.FLAG_LAYOUT_NO_LIMITS); hideStatusBar=false; } });
		 */

		// For handling default keys
		/*
		 * mDefaultKeySsb = new SpannableStringBuilder();
		 * Selection.setSelection(mDefaultKeySsb, 0);
		 */

		// ADW: register a sharedpref listener
		getSharedPreferences("launcher.preferences.almostnexus",
				Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(
				this);
		showPwdExpiryScreen();
		// showAllApps(true, null);
		onloadlib();
		// WIDGET
		sendBroadcastForWidgetUpdate();
	}

	private void sendBroadcastForWidgetUpdate() {
		Intent in = new Intent();
		in.setAction("com.cognizant.trumobi.calendar.sync_completed");
		sendBroadcast(in);
	}

	public void onloadlib() {

		Thread subdexLoader = new Thread("Subdex loader") {
			@Override
			public void run() {
				DexDex.addAllJARsInAssets(PersonaLauncher.this);
			}
		};
		subdexLoader.start();
	}

	private void showPwdExpiryScreen() {
		prefs = getSharedPreferences(PersonaConstants.PERSONAPREFERENCESFILE,
				MODE_PRIVATE);
		boolean showExpiry = prefs.getBoolean("showExpiry", false);
		int daysLeft = prefs.getInt("daysLeft", 3);
		String pwdtype = prefs.getString("authtype", "PIN");
		PersonaLog.d(LOG_TAG, "#################### No of days left : "
				+ daysLeft);
		PersonaLog.d(LOG_TAG, "#################### Password Type is : "
				+ pwdtype);
		// showExpiry = true;
		if (showExpiry) {
			// Changing the dialog to use PersonaSecurityUpdateDialogFragment.
			Intent intent = new Intent(context,
					PersonaSecurityProfileUpdate.class);
			intent.putExtra("securityevent", PersonaConstants.SHOW_PIN_EXPIRY);
			intent.putExtra("securityaction", PersonaConstants.SHOW_PIN_EXPIRY);
			context.startActivity(intent);
			// PwdExpiryDialog pwdExpiryDialog = new PwdExpiryDialog();
			// pwdExpiryDialog.createDialog(daysLeft, pwdtype);
		}

	}

	private void checkForLocaleChange() {
		final LocaleConfiguration localeConfiguration = new LocaleConfiguration();
		readConfiguration(this, localeConfiguration);

		final Configuration configuration = getResources().getConfiguration();

		final String previousLocale = localeConfiguration.locale;
		final String locale = configuration.locale.toString();

		final int previousMcc = localeConfiguration.mcc;
		final int mcc = configuration.mcc;

		final int previousMnc = localeConfiguration.mnc;
		final int mnc = configuration.mnc;

		mLocaleChanged = !locale.equals(previousLocale) || mcc != previousMcc
				|| mnc != previousMnc;

		if (mLocaleChanged) {
			localeConfiguration.locale = locale;
			localeConfiguration.mcc = mcc;
			localeConfiguration.mnc = mnc;

			writeConfiguration(this, localeConfiguration);
		}
	}

	private static class LocaleConfiguration {
		public String locale;
		public int mcc = -1;
		public int mnc = -1;
	}

	private static void readConfiguration(Context context,
			LocaleConfiguration configuration) {
		DataInputStream in = null;
		try {
			in = new DataInputStream(context.openFileInput(PREFERENCES));
			configuration.locale = in.readUTF();
			configuration.mcc = in.readInt();
			configuration.mnc = in.readInt();
		} catch (FileNotFoundException e) {
			// Ignore
		} catch (IOException e) {
			// Ignore
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}

	private static void writeConfiguration(Context context,
			LocaleConfiguration configuration) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(context.openFileOutput(PREFERENCES,
					MODE_PRIVATE));
			out.writeUTF(configuration.locale);
			out.writeInt(configuration.mcc);
			out.writeInt(configuration.mnc);
			out.flush();
		} catch (FileNotFoundException e) {
			// Ignore
		} catch (IOException e) {
			// noinspection ResultOfMethodCallIgnored
			context.getFileStreamPath(PREFERENCES).delete();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}

	static int getScreen() {
		synchronized (sLock) {
			return sScreen;
		}
	}

	static void setScreen(int screen) {
		synchronized (sLock) {
			sScreen = screen;
		}
	}

	private void startLoaders() {
		boolean loadApplications = sModel.loadApplications(true, this,
				mLocaleChanged);
		sModel.loadUserItems(!mLocaleChanged, this, mLocaleChanged,
				loadApplications);

		mRestoring = false;
	}

	// 290778 commented out- Unused code
	/*
	 * private void setWallpaperDimension(){ WallpaperManager wpm =
	 * (WallpaperManager)getSystemService(WALLPAPER_SERVICE);
	 * 
	 * Display display = getWindowManager().getDefaultDisplay(); boolean
	 * isPortrait = display.getWidth() < display.getHeight(); int width =
	 * isPortrait ? display.getWidth() : display.getHeight(); int height =
	 * isPortrait ? display.getHeight() : display.getWidth();
	 * 
	 * width *= display.DEFAULT_DISPLAY; height *= display.DEFAULT_DISPLAY;
	 * 
	 * 
	 * 
	 * DisplayMetrics display1 = new DisplayMetrics();
	 * getWindowManager().getDefaultDisplay().getMetrics(display1); boolean
	 * isPortrait1 = display1.widthPixels < display1.heightPixels;
	 * 
	 * PersonaLog.v("-----------isPortrait---------------",String.valueOf(
	 * isPortrait1));
	 * 
	 * int width = isPortrait1 ? display1.widthPixels : display1.heightPixels;
	 * int height = isPortrait1 ? display1.heightPixels : display1.widthPixels;
	 * 
	 * 
	 * 
	 * 
	 * 
	 * wpm.suggestDesiredDimensions(width * WALLPAPER_SCREENS_SPAN, height);
	 * 
	 * prefs = PreferenceManager.getDefaultSharedPreferences(this); if
	 * (prefs.getBoolean(isFirstRunning, true)) { try {
	 * setWallpaper(BitmapFactory
	 * .decodeResource(getResources(),R.drawable.pr_bg1));
	 * prefs.edit().putBoolean(isFirstRunning,false) .commit(); } catch
	 * (IOException e) { // TODO: handle exception } }
	 * 
	 * }
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mWaitingForResult = false;
		// The pattern used here is that a user PICKs a specific application,
		// which, depending on the target, might need to CREATE the actual
		// target.

		// For example, the user would PICK_SHORTCUT for "Music playlist", and
		// we
		// launch over to the Music app to actually CREATE_SHORTCUT.

		if (resultCode == RESULT_OK && mAddItemCellInfo != null) {
			switch (requestCode) {
			case REQUEST_PICK_APPLICATION:
				completeAddApplication(this, data, mAddItemCellInfo,
						!mDesktopLocked);
				break;
			case REQUEST_PICK_SHORTCUT:
				processShortcut(data, REQUEST_PICK_APPLICATION,
						REQUEST_CREATE_SHORTCUT);
				break;
			case REQUEST_CREATE_SHORTCUT:
				completeAddShortcut(data, mAddItemCellInfo, !mDesktopLocked);
				break;
			case REQUEST_PICK_LIVE_FOLDER:
				addLiveFolder(data);
				break;
			case REQUEST_CREATE_LIVE_FOLDER:
				completeAddLiveFolder(data, mAddItemCellInfo, !mDesktopLocked);
				break;
			case REQUEST_PICK_APPWIDGET:
				addAppWidget(data);
				break;
			case REQUEST_CREATE_APPWIDGET:
				completeAddAppWidget(data, mAddItemCellInfo, !mDesktopLocked);
				break;
			case REQUEST_PICK_ANYCUT:
				completeAddShortcut(data, mAddItemCellInfo, !mDesktopLocked);
				break;
			case REQUEST_EDIT_SHIRTCUT:
				completeEditShirtcut(data);
				break;
			}
		} else if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_SHOW_APP_LIST: {
				mAllAppsGrid.updateAppGrp();
				showAllApps(true, null);
			}
				break;
			case REQUEST_EDIT_SHIRTCUT:
				completeEditShirtcut(data);
				break;
			}
		} else if ((requestCode == REQUEST_PICK_APPWIDGET || requestCode == REQUEST_CREATE_APPWIDGET)
				&& resultCode == RESULT_CANCELED && data != null) {
			// Clean up the appWidgetId if we canceled
			int appWidgetId = data.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			if (appWidgetId != -1) {
				mAppWidgetHost.deleteAppWidgetId(appWidgetId);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (shouldRestart())
			return;
		//Badge
				updateBadgeCounts();	
		// ADW: Use custom settings to set the rotation
		/*
		 * this.setRequestedOrientation(
		 * PersonaAlmostNexusSettingsHelper.getDesktopRotation(this)?
		 * ActivityInfo.SCREEN_ORIENTATION_USER
		 * :ActivityInfo.SCREEN_ORIENTATION_NOSENSOR );
		 */
		// ADW: Use custom settings to change number of columns (and rows for
		// SlidingGrid) depending on phone rotation
		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			mAllAppsGrid.setNumColumns(PersonaAlmostNexusSettingsHelper
					.getColumnsPortrait(PersonaLauncher.this));
			mAllAppsGrid.setNumRows(PersonaAlmostNexusSettingsHelper
					.getRowsPortrait(PersonaLauncher.this));
			mAllAppsGrid
					.setPageHorizontalMargin(PersonaAlmostNexusSettingsHelper
							.getPageHorizontalMargin(PersonaLauncher.this));
		} else {
			mAllAppsGrid.setNumColumns(PersonaAlmostNexusSettingsHelper
					.getColumnsLandscape(PersonaLauncher.this));
			mAllAppsGrid.setNumRows(PersonaAlmostNexusSettingsHelper
					.getRowsLandscape(PersonaLauncher.this));
		}
		// mWorkspace.setWallpaper(false);
		if (mRestoring) {
			startLoaders();
		}

		// If this was a new intent (i.e., the mIsNewIntent flag got set to true
		// by
		// onNewIntent), then close the search dialog if needed, because it
		// probably
		// came from the user pressing 'home' (rather than, for example,
		// pressing 'back').
		if (mIsNewIntent) {
			// Post to a handler so that this happens after the search dialog
			// tries to open
			// itself again.
			mWorkspace.post(new Runnable() {
				public void run() {
					// ADW: changed from using ISearchManager to use
					// SearchManager (thanks to PersonaLauncher+ source code)
					SearchManager searchManagerService = (SearchManager) PersonaLauncher.this
							.getSystemService(Context.SEARCH_SERVICE);
					try {
						searchManagerService.stopSearch();
					} catch (Exception e) {
						PersonaLog.e(LOG_TAG, "error stopping search", e);
					}
				}
			});
		}

		mIsNewIntent = false;
	}

	//Badge
	private void updateBadgeCounts() {
		int mMissedCalls = getMissedCallCount();//new com.TruBoxSDK.SharedPreferences(Email.getAppContext()).getInt("MissedCallCounts", 0);
		updateCountersForPackage("com.cognizant.trumobi", mMissedCalls, 5, 1);
		System.out.println("badge for missed call   "+mMissedCalls);
		//int mNewMessages = new com.TruBoxSDK.SharedPreferences(Email.getAppContext()).getInt("NewMessages", 0);
		TruBoxDatabase.setPimSettings(false);
		SmsListdisplay mSms = new SmsListdisplay();
		int mNewMessages = mSms.unReadSMSCount(Email.getAppContext());
		updateCountersForPackage("com.cognizant.trumobi", mNewMessages, 5, 2);
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		// ADW: removed cause it was closing app-drawer every time Home button
		// is triggered
		// ADW: it should be done only on certain circumstances
		// closeDrawer(false);
		savedOrientation = getResources().getConfiguration().orientation;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Flag any binder to stop early before switching
		if (mBinder != null) {
			mBinder.mTerminate = true;
		}
		// if(mMessWithPersistence)setPersistent(false);
		if (PROFILE_ROTATE) {
			android.os.Debug.startMethodTracing("/sdcard/launcher-rotate");
		}
		return null;// previous value was null.
	}

	private boolean acceptFilter() {
		final InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		return !inputManager.isFullscreenMode();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		PersonaLog.e("LOG_TAG", "" + "onBackPressed");
		super.onBackPressed();
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// PersonaLog.e(LOG_TAG, "keyCode" + keyCode);
		// System.out.println("==========OnKeyDown ==========="+keyCode);
		// System.out.println("===========KeyEvent"+event.getAction());
		boolean handled = super.onKeyDown(keyCode, event);

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			return true;
		}

		if (112 == keyCode) /* 112 represents F2 key; */
		{
			/* Invoke Application PersonaDrawer screen in case F2 is selected */
			fireHomeBinding(BIND_APPS, 0);
		} else if (!handled && acceptFilter()
				&& keyCode != KeyEvent.KEYCODE_ENTER) {
			/*
			 * boolean gotKey =
			 * TextKeyListener.getInstance().onKeyDown(mWorkspace,
			 * mDefaultKeySsb, keyCode, event);
			 * 
			 * if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() >
			 * 0) { // something usable has been typed - start a search // the
			 * typed text will be retrieved and cleared by // showSearchDialog()
			 * // If there are multiple keystrokes before the search dialog
			 * takes focus, // onSearchRequested() will be called for every
			 * keystroke, // but it is idempotent, so it's fine. return
			 * onSearchRequested(); }
			 */

			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackPressed();
		}

		return handled;
	}

	@SuppressWarnings("unused")
	private String getTypedText() {
		return mDefaultKeySsb.toString();
	}

	@SuppressWarnings("unused")
	private void clearTypedText() {
		mDefaultKeySsb.clear();
		mDefaultKeySsb.clearSpans();
		Selection.setSelection(mDefaultKeySsb, 0);
	}

	/**
	 * Restores the previous state, if it exists.
	 * 
	 * @param savedState
	 *            The previous state.
	 */
	private void restoreState(Bundle savedState) {
		if (savedState == null) {
			return;
		}

		final int currentScreen = savedState.getInt(
				RUNTIME_STATE_CURRENT_SCREEN, -1);
		if (currentScreen > -1) {
			mWorkspace.setCurrentScreen(currentScreen);
		}

		final int addScreen = savedState.getInt(
				RUNTIME_STATE_PENDING_ADD_SCREEN, -1);
		if (addScreen > -1) {
			mAddItemCellInfo = new PersonaCellLayout.CellInfo();
			final PersonaCellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
			addItemCellInfo.valid = true;
			addItemCellInfo.screen = addScreen;
			addItemCellInfo.cellX = savedState
					.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
			addItemCellInfo.cellY = savedState
					.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
			addItemCellInfo.spanX = savedState
					.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
			addItemCellInfo.spanY = savedState
					.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
			addItemCellInfo.findVacantCellsFromOccupied(savedState
					.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS),
					savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_X),
					savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y));
			mRestoring = true;
		}

		boolean renameFolder = savedState.getBoolean(
				RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
		if (renameFolder) {
			long id = savedState
					.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
			mFolderInfo = sModel.getFolderById(this, id);
			mRestoring = true;
		}
	}

	/**
	 * Finds all the views we need and configure them properly.
	 */
	private void setupViews() {
		mDragLayer = (PersonaDragLayer) findViewById(R.id.drag_layer);
		final PersonaDragLayer personaDragLayer = mDragLayer;

		mWorkspace = (PersonaWorkspace) personaDragLayer
				.findViewById(R.id.workspace);
		final PersonaWorkspace personaWorkspace = mWorkspace;
		// ADW: The app drawer is now a ViewStub and we load the resource
		// depending on custom settings
		ViewStub tmp = (ViewStub) personaDragLayer
				.findViewById(R.id.stub_drawer);
		int drawerStyle = PersonaAlmostNexusSettingsHelper.getDrawerStyle(this);
		tmp.setLayoutResource(mDrawerStyles[drawerStyle]);
		mAllAppsGrid = (PersonaDrawer) tmp.inflate();
		mDeleteZone = (PersonaDeleteZone) personaDragLayer
				.findViewById(R.id.delete_zone);

		mHandleView = (PersonaActionButton) personaDragLayer
				.findViewById(R.id.btn_mab);
		mHandleView.setFocusable(true);
		mHandleView.setLauncher(this);
		mHandleView.setOnClickListener(this);
		personaDragLayer.addDragListener(mHandleView);
		/*
		 * mHandleView.setOnTriggerListener(new OnTriggerListener() { public
		 * void onTrigger(View v, int whichHandle) { mDockBar.open(); } public
		 * void onGrabbedStateChange(View v, boolean grabbedState) { } public
		 * void onClick(View v) { if (allAppsOpen) { closeAllApps(true); } else
		 * { showAllApps(true, null); } } });
		 */
		mAllAppsGrid.setTextFilterEnabled(false);
		mAllAppsGrid.setDragger(personaDragLayer);
		mAllAppsGrid.setLauncher(this);

		personaWorkspace.setOnLongClickListener(this);
		personaWorkspace.setDragger(personaDragLayer);
		personaWorkspace.setLauncher(this);

		mDeleteZone.setLauncher(this);
		mDeleteZone.setDragController(personaDragLayer);

		personaDragLayer.setIgnoredDropTarget((View) mAllAppsGrid);
		personaDragLayer.setDragScoller(personaWorkspace);
		personaDragLayer.addDragListener(mDeleteZone);
		// ADW: Dockbar inner icon viewgroup (PersonaMiniLauncher.java)
		mMiniLauncher = (PersonaMiniLauncher) personaDragLayer
				.findViewById(R.id.mini_content);
		mMiniLauncher.setLauncher(this);
		mMiniLauncher.setOnLongClickListener(this);
		mMiniLauncher.setDragger(personaDragLayer);
		personaDragLayer.addDragListener(mMiniLauncher);

		// ADW: Action Buttons (LAB/RAB)
		mLAB = (PersonaActionButton) personaDragLayer
				.findViewById(R.id.btn_lab);
		mLAB.setLauncher(this);
		mLAB.setSpecialIcon(R.drawable.pr_arrow_left);
		mLAB.setSpecialAction(ACTION_CATALOG_PREV);
		personaDragLayer.addDragListener(mLAB);
		mRAB = (PersonaActionButton) personaDragLayer
				.findViewById(R.id.btn_rab);
		mRAB.setLauncher(this);
		mRAB.setSpecialIcon(R.drawable.pr_arrow_right);
		mRAB.setSpecialAction(ACTION_CATALOG_NEXT);
		personaDragLayer.addDragListener(mRAB);
		mLAB.setOnClickListener(this);
		mRAB.setOnClickListener(this);
		// ADW: secondary aActionButtons
		mLAB2 = (PersonaActionButton) personaDragLayer
				.findViewById(R.id.btn_lab2);
		mLAB2.setLauncher(this);
		personaDragLayer.addDragListener(mLAB2);
		mRAB2 = (PersonaActionButton) personaDragLayer
				.findViewById(R.id.btn_rab2);
		mRAB2.setLauncher(this);
		personaDragLayer.addDragListener(mRAB2);
		mLAB2.setOnClickListener(this);
		mRAB2.setOnClickListener(this);
		// ADW: Dots ImageViews
		mPreviousView = (ImageView) findViewById(R.id.btn_scroll_left);
		mNextView = (ImageView) findViewById(R.id.btn_scroll_right);
		mPreviousView.setOnLongClickListener(this);
		mNextView.setOnLongClickListener(this);

		// ADW: ActionButtons swipe gestures
		mHandleView.setSwipeListener(this);
		mLAB.setSwipeListener(this);
		mLAB2.setSwipeListener(this);
		mRAB.setSwipeListener(this);
		mRAB2.setSwipeListener(this);

		// mHandleView.setDragger(dragLayer);
		// mLAB.setDragger(dragLayer);
		// mRAB.setDragger(dragLayer);
		mRAB2.setDragger(personaDragLayer);
		mLAB2.setDragger(personaDragLayer);

		// ADW linearlayout with apptray, lab and rab
		mDrawerToolbar = findViewById(R.id.drawer_toolbar);
		mHandleView.setNextFocusUpId(R.id.drag_layer);
		mHandleView.setNextFocusLeftId(R.id.drag_layer);
		mLAB.setNextFocusUpId(R.id.drag_layer);
		mLAB.setNextFocusLeftId(R.id.drag_layer);
		mRAB.setNextFocusUpId(R.id.drag_layer);
		mRAB.setNextFocusLeftId(R.id.drag_layer);
		mLAB2.setNextFocusUpId(R.id.drag_layer);
		mLAB2.setNextFocusLeftId(R.id.drag_layer);
		mRAB2.setNextFocusUpId(R.id.drag_layer);
		mRAB2.setNextFocusLeftId(R.id.drag_layer);
		// ADW add a listener to the dockbar to show/hide the app-drawer-button
		// and the dots
		mDockBar = (PersonaDockBar) findViewById(R.id.dockbar);
		mDockBar.setDockBarListener(new DockBarListener() {
			public void onOpen() {
				mDrawerToolbar.setVisibility(View.GONE);
				if (mNextView.getVisibility() == View.VISIBLE) {
					mNextView.setVisibility(View.INVISIBLE);
					mPreviousView.setVisibility(View.INVISIBLE);
				}
			}

			public void onClose() {
				if (mDockStyle != DOCK_STYLE_NONE)
					mDrawerToolbar.setVisibility(View.VISIBLE);
				if (showDots && !isAllAppsVisible()) {
					mNextView.setVisibility(View.VISIBLE);
					mPreviousView.setVisibility(View.VISIBLE);
				}

			}
		});
		if (PersonaAlmostNexusSettingsHelper.getDesktopIndicator(this)) {
			mDesktopIndicator = (PersonaDesktopIndicator) (findViewById(R.id.desktop_indicator));
		}
		// ADW: Add focusability to screen items
		mLAB.setFocusable(true);
		mRAB.setFocusable(true);
		mLAB2.setFocusable(true);
		mRAB2.setFocusable(true);
		mPreviousView.setFocusable(true);
		mNextView.setFocusable(true);

		// ADW: Load the specified theme
		String themePackage = PersonaAlmostNexusSettingsHelper
				.getThemePackageName(this, THEME_DEFAULT);
		PackageManager pm = getPackageManager();
		Resources themeResources = null;
		if (!themePackage.equals(THEME_DEFAULT)) {
			try {
				themeResources = pm.getResourcesForApplication(themePackage);
			} catch (NameNotFoundException e) {
				// ADW The saved theme was uninstalled so we save the default
				// one
				PersonaAlmostNexusSettingsHelper.setThemePackageName(this,
						PersonaLauncher.THEME_DEFAULT);
			}
		}
		if (themeResources != null) {
			// Action Buttons
			loadThemeResource(themeResources, themePackage, "lab_bg", mLAB,
					THEME_ITEM_BACKGROUND);
			loadThemeResource(themeResources, themePackage, "rab_bg", mRAB,
					THEME_ITEM_BACKGROUND);
			loadThemeResource(themeResources, themePackage, "lab2_bg", mLAB2,
					THEME_ITEM_BACKGROUND);
			loadThemeResource(themeResources, themePackage, "rab2_bg", mRAB2,
					THEME_ITEM_BACKGROUND);
			loadThemeResource(themeResources, themePackage, "mab_bg",
					mHandleView, THEME_ITEM_BACKGROUND);
			// App drawer button
			// loadThemeResource(themeResources,themePackage,"handle_icon",mHandleView,THEME_ITEM_FOREGROUND);
			// View appsBg=findViewById(R.id.appsBg);
			// loadThemeResource(themeResources,themePackage,"handle",appsBg,THEME_ITEM_BACKGROUND);
			// Deletezone
			loadThemeResource(themeResources, themePackage, "ic_delete",
					mDeleteZone, THEME_ITEM_FOREGROUND);
			loadThemeResource(themeResources, themePackage,
					"delete_zone_selector", mDeleteZone, THEME_ITEM_BACKGROUND);
			loadThemeResource(themeResources, themePackage, "home_arrows_left",
					mPreviousView, THEME_ITEM_FOREGROUND);
			loadThemeResource(themeResources, themePackage,
					"home_arrows_right", mNextView, THEME_ITEM_FOREGROUND);
			// Dockbar
			loadThemeResource(themeResources, themePackage, "dockbar_bg",
					mMiniLauncher, THEME_ITEM_BACKGROUND);
			try {
				themeFont = Typeface.createFromAsset(
						themeResources.getAssets(), "themefont.ttf");
			} catch (RuntimeException e) {
				// TODO: handle exception
			}
		}
		Drawable previous = mPreviousView.getDrawable();
		Drawable next = mNextView.getDrawable();
		mWorkspace.setIndicators(previous, next);

		// ADW: EOF Themes
		updateAlmostNexusUI();
	}

	/**
	 * Creates a view representing a shortcut.
	 * 
	 * @param info
	 *            The data structure describing the shortcut.
	 * 
	 * @return A View inflated from R.layout.application.
	 */
	View createShortcut(PersonaApplicationInfo info) {
		return createShortcut(
				R.layout.pr_application,
				(ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()),
				info);
	}

	/**
	 * Creates a view representing a shortcut inflated from the specified
	 * resource.
	 * 
	 * @param layoutResId
	 *            The id of the XML layout used to create the shortcut.
	 * @param parent
	 *            The group the shortcut belongs to.
	 * @param info
	 *            The data structure describing the shortcut.
	 * 
	 * @return A View inflated from layoutResId.
	 */
	View createShortcut(int layoutResId, ViewGroup parent,
			PersonaApplicationInfo info) {
		PersonaCounterTextView favorite = (PersonaCounterTextView) mInflater
				.inflate(layoutResId, parent, false);

		if (!info.filtered) {
			info.icon = PersonaUtilities.createIconThumbnail(info.icon, this);
			info.filtered = true;
		}

		favorite.setCompoundDrawablesWithIntrinsicBounds(null, info.icon, null,
				null);
		if (!uiHideLabels)
			favorite.setText(info.title);
		favorite.setTag(info);
		favorite.setOnClickListener(this);
		// ADW: Custom font
		if (themeFont != null)
			favorite.setTypeface(themeFont);
		// ADW: Counters stuff
		favorite.setCounter(info.counter, info.counterColor);
		return favorite;
	}

	/**
	 * Add an application shortcut to the workspace.
	 * 
	 * @param data
	 *            The intent describing the application.
	 * @param cellInfo
	 *            The position on screen where to create the shortcut.
	 */
	void completeAddApplication(Context context, Intent data,
			PersonaCellLayout.CellInfo cellInfo, boolean insertAtFirst) {
		cellInfo.screen = mWorkspace.getCurrentScreen();
		if (!findSingleSlot(cellInfo))
			return;

		final PersonaApplicationInfo info = infoFromApplicationIntent(context,
				data);
		if (info != null) {
			mWorkspace.addApplicationShortcut(info, cellInfo, insertAtFirst);
		}
	}

	private static PersonaApplicationInfo infoFromApplicationIntent(
			Context context, Intent data) {
		ComponentName component = data.getComponent();
		PackageManager packageManager = context.getPackageManager();
		ActivityInfo activityInfo = null;
		try {
			activityInfo = packageManager.getActivityInfo(component, 0 /*
																		 * no
																		 * flags
																		 */);
		} catch (NameNotFoundException e) {
			PersonaLog.e(LOG_TAG,
					"Couldn't find ActivityInfo for selected application", e);
		}

		if (activityInfo != null) {
			PersonaApplicationInfo itemInfo = new PersonaApplicationInfo();

			itemInfo.title = activityInfo.loadLabel(packageManager);
			if (itemInfo.title == null) {
				itemInfo.title = activityInfo.name;
			}

			itemInfo.setActivity(component, Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			// itemInfo.icon = activityInfo.loadIcon(packageManager);
			itemInfo.container = PersonaItemInfo.NO_ID;

			itemInfo.icon = PersonaLauncherModel.getIcon(packageManager,
					context, activityInfo);

			return itemInfo;
		}

		return null;
	}

	/**
	 * Add a shortcut to the workspace.
	 * 
	 * @param data
	 *            The intent describing the shortcut.
	 * @param cellInfo
	 *            The position on screen where to create the shortcut.
	 * @param insertAtFirst
	 */
	private void completeAddShortcut(Intent data,
			PersonaCellLayout.CellInfo cellInfo, boolean insertAtFirst) {
		cellInfo.screen = mWorkspace.getCurrentScreen();
		if (!findSingleSlot(cellInfo))
			return;

		final PersonaApplicationInfo info = addShortcut(this, data, cellInfo,
				false);

		if (!mRestoring) {
			sModel.addDesktopItem(info);

			final View view = createShortcut(info);
			mWorkspace.addInCurrentScreen(view, cellInfo.cellX, cellInfo.cellY,
					1, 1, insertAtFirst);
		} else if (sModel.isDesktopLoaded()) {
			sModel.addDesktopItem(info);
		}
	}

	/**
	 * Add a widget to the workspace.
	 * 
	 * @param data
	 *            The intent describing the appWidgetId.
	 * @param cellInfo
	 *            The position on screen where to create the widget.
	 */
	private void completeAddAppWidget(Intent data,
			PersonaCellLayout.CellInfo cellInfo, final boolean insertAtFirst) {

		Bundle extras = data.getExtras();
		final int appWidgetId = extras.getInt(
				AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

		if (LOGD)
			PersonaLog
					.d(LOG_TAG, "dumping extras content=" + extras.toString());

		final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
				.getAppWidgetInfo(appWidgetId);

		// Calculate the grid spans needed to fit this widget
		PersonaCellLayout layout = (PersonaCellLayout) mWorkspace
				.getChildAt(cellInfo.screen);
		final int[] spans = layout.rectToCell(appWidgetInfo.minWidth,
				appWidgetInfo.minHeight);
		final PersonaCellLayout.CellInfo cInfo = cellInfo;
		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		final View dlg_layout = View.inflate(PersonaLauncher.this,
				R.layout.pr_widget_span_setup, null);
		final PersonaNumberPicker ncols = (PersonaNumberPicker) dlg_layout
				.findViewById(R.id.widget_columns_span);
		ncols.setRange(1, mWorkspace.currentDesktopColumns());
		ncols.setCurrent(spans[0]);
		final PersonaNumberPicker nrows = (PersonaNumberPicker) dlg_layout
				.findViewById(R.id.widget_rows_span);
		nrows.setRange(1, mWorkspace.currentDesktopRows());
		nrows.setCurrent(spans[1]);
		// 290661 changes
		spans[0] = 3;
		spans[1] = 2;
		realAddWidget(appWidgetInfo, cInfo, spans, appWidgetId, insertAtFirst);

		/*
		 * builder = new AlertDialog.Builder(PersonaLauncher.this);
		 * builder.setView(dlg_layout); alertDialog = builder.create();
		 * alertDialog
		 * .setTitle(getResources().getString(R.string.widget_config_dialog_title
		 * )); alertDialog.setMessage(getResources().getString(R.string.
		 * widget_config_dialog_summary));
		 * alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
		 * getResources().getString(android.R.string.ok), new
		 * DialogInterface.OnClickListener() { public void
		 * onClick(DialogInterface dialog, int which) {
		 * spans[0]=ncols.getCurrent(); spans[1]=nrows.getCurrent();
		 * realAddWidget(appWidgetInfo,cInfo,spans,appWidgetId,insertAtFirst); }
		 * }); alertDialog.show();
		 */
	}

	public PersonaLauncherAppWidgetHost getAppWidgetHost() {
		return mAppWidgetHost;
	}

	static PersonaApplicationInfo addShortcut(Context context, Intent data,
			PersonaCellLayout.CellInfo cellInfo, boolean notify) {

		final PersonaApplicationInfo info = infoFromShortcutIntent(context,
				data);
		PersonaLauncherModel.addItemToDatabase(context, info,
				PersonaLauncherSettings.Favorites.CONTAINER_DESKTOP,
				cellInfo.screen, cellInfo.cellX, cellInfo.cellY, notify);

		return info;
	}

	private static PersonaApplicationInfo infoFromShortcutIntent(
			Context context, Intent data) {
		Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
		Bitmap bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

		Drawable icon = null;
		boolean filtered = false;
		boolean customIcon = false;
		ShortcutIconResource iconResource = null;

		if (bitmap != null) {
			icon = new PersonaFastBitmapDrawable(
					PersonaUtilities.createBitmapThumbnail(bitmap, context));
			filtered = true;
			customIcon = true;
		} else {
			Parcelable extra = data
					.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
			if (extra != null && extra instanceof ShortcutIconResource) {
				try {
					iconResource = (ShortcutIconResource) extra;
					final PackageManager packageManager = context
							.getPackageManager();
					Resources resources = packageManager
							.getResourcesForApplication(iconResource.packageName);
					final int id = resources.getIdentifier(
							iconResource.resourceName, null, null);
					icon = resources.getDrawable(id);
				} catch (Exception e) {
					PersonaLog.w(LOG_TAG, "Could not load shortcut icon: "
							+ extra);
				}
			}
		}

		if (icon == null) {
			icon = context.getPackageManager().getDefaultActivityIcon();
		}

		final PersonaApplicationInfo info = new PersonaApplicationInfo();
		info.icon = icon;
		info.filtered = filtered;
		info.title = name;
		info.intent = intent;
		info.customIcon = customIcon;
		info.iconResource = iconResource;

		return info;
	}

	void closeSystemDialogs() {
		getWindow().closeAllPanels();

		try {
			dismissDialog(DIALOG_CREATE_SHORTCUT);
			// Unlock the workspace if the dialog was showing
			mWorkspace.unlock();
		} catch (Exception e) {
			// An exception is thrown if the dialog is not visible, which is
			// fine
		}

		try {
			dismissDialog(DIALOG_RENAME_FOLDER);
			// Unlock the workspace if the dialog was showing
			mWorkspace.unlock();
		} catch (Exception e) {
			// An exception is thrown if the dialog is not visible, which is
			// fine
		}
		try {
			dismissDialog(DIALOG_CHOOSE_GROUP);
			// Unlock the workspace if the dialog was showing
		} catch (Exception e) {
			// An exception is thrown if the dialog is not visible, which is
			// fine
		}
		try {
			dismissDialog(DIALOG_NEW_GROUP);
			// Unlock the workspace if the dialog was showing
		} catch (Exception e) {
			// An exception is thrown if the dialog is not visible, which is
			// fine
		}
		try {
			dismissDialog(DIALOG_DELETE_GROUP_CONFIRM);
			// Unlock the workspace if the dialog was showing
		} catch (Exception e) {
			// An exception is thrown if the dialog is not visible, which is
			// fine
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// Close the menu
		if (Intent.ACTION_MAIN.equals(intent.getAction())) {
			closeSystemDialogs();

			// Set this flag so that onResume knows to close the search dialog
			// if it's open,
			// because this was a new intent (thus a press of 'home' or some
			// such) rather than
			// for example onResume being called when the user pressed the
			// 'back' button.
			mIsNewIntent = true;

			if ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) {
				if (!isAllAppsVisible() || mHomeBinding == BIND_APPS)
					fireHomeBinding(mHomeBinding, 1);
				if (mHomeBinding != BIND_APPS) {
					closeDrawer(true);

				}
				final View v = getWindow().peekDecorView();
				if (v != null && v.getWindowToken() != null) {
					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
			} else {
				closeDrawer(false);
			}
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// NOTE: Do NOT do this. Ever. This is a terrible and horrifying hack.
		//
		// Home loads the content of the workspace on a background thread. This
		// means that
		// a previously focused view will be, after orientation change, added to
		// the view
		// hierarchy at an undeterminate time in the future. If we were to
		// invoke
		// super.onRestoreInstanceState() here, the focus restoration would fail
		// because the
		// view to focus does not exist yet.
		//
		// However, not invoking super.onRestoreInstanceState() is equally bad.
		// In such a case,
		// panels would not be restored properly. For instance, if the menu is
		// open then the
		// user changes the orientation, the menu would not be opened in the new
		// orientation.
		//
		// To solve both issues Home messes up with the internal state of the
		// bundle to remove
		// the properties it does not want to see restored at this moment. After
		// invoking
		// super.onRestoreInstanceState(), it removes the panels state.
		//
		// Later, when the workspace is done loading, Home calls
		// super.onRestoreInstanceState()
		// again to restore focus and other view properties. It will not,
		// however, restore
		// the panels since at this point the panels' state has been removed
		// from the bundle.
		//
		// This is a bad example, do not do this.
		//
		// If you are curious on how this code was put together, take a look at
		// the following
		// in Android's source code:
		// - Activity.onRestoreInstanceState()
		// - PhoneWindow.restoreHierarchyState()
		// - PhoneWindow.DecorView.onAttachedToWindow()
		//
		// The source code of these various methods shows what states should be
		// kept to
		// achieve what we want here.

		Bundle windowState = savedInstanceState
				.getBundle("android:viewHierarchyState");
		SparseArray<Parcelable> savedStates = null;
		int focusedViewId = View.NO_ID;

		if (windowState != null) {
			savedStates = windowState.getSparseParcelableArray("android:views");
			windowState.remove("android:views");
			focusedViewId = windowState.getInt("android:focusedViewId",
					View.NO_ID);
			windowState.remove("android:focusedViewId");
		}

		super.onRestoreInstanceState(savedInstanceState);

		if (windowState != null) {
			windowState.putSparseParcelableArray("android:views", savedStates);
			windowState.putInt("android:focusedViewId", focusedViewId);
			windowState.remove("android:Panels");
		}

		mSavedInstanceState = savedInstanceState;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// ADW: If we leave the menu open, on restoration it will try to auto
		// find
		// the ocupied cells. But this could happed before the workspace is
		// fully loaded,
		// so it can cause a NPE cause of the way we load the desktop
		// columns/rows count.
		// I prefer to just close it than diggin the code to make it load
		// later...
		// Accepting patches :-)
		closeOptionsMenu();
		super.onSaveInstanceState(outState);

		outState.putInt(RUNTIME_STATE_CURRENT_SCREEN,
				mWorkspace.getCurrentScreen());
		if (mWorkspace != null) {
			final ArrayList<PersonaFolder> personaFolders = mWorkspace
					.getOpenFolders();
			if (personaFolders.size() > 0) {
				final int count = personaFolders.size();
				long[] ids = new long[count];
				for (int i = 0; i < count; i++) {
					final PersonaFolderInfo info = personaFolders.get(i)
							.getInfo();
					ids[i] = info.id;
				}
				outState.putLongArray(RUNTIME_STATE_USER_FOLDERS, ids);
			}
		}
		final boolean isConfigurationChange = getChangingConfigurations() != 0;

		// When the drawer is opened and we are saving the state because of a
		// configuration change
		if (allAppsOpen && isConfigurationChange) {
			outState.putBoolean(RUNTIME_STATE_ALL_APPS_FOLDER, true);
		}
		if (mDockBar != null && mDockBar.isOpen()) {
			outState.putBoolean(RUNTIME_STATE_DOCKBAR, true);
		}
		if (mAddItemCellInfo != null && mAddItemCellInfo.valid
				&& mWaitingForResult) {
			final PersonaCellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
			final PersonaCellLayout layout = (PersonaCellLayout) mWorkspace
					.getChildAt(addItemCellInfo.screen);

			outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN,
					addItemCellInfo.screen);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X,
					addItemCellInfo.cellX);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y,
					addItemCellInfo.cellY);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X,
					addItemCellInfo.spanX);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y,
					addItemCellInfo.spanY);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_X,
					layout.getCountX());
			outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y,
					layout.getCountY());
			outState.putBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS,
					layout.getOccupiedCells());
		}

		if (mFolderInfo != null && mWaitingForResult) {
			outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
			outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID,
					mFolderInfo.id);
		}
	}

	@Override
	public void onDestroy() {
		mDestroyed = true;
		// setPersistent(false);
		// ADW: unregister the sharedpref listener
		getSharedPreferences("launcher.preferences.almostnexus",
				Context.MODE_PRIVATE)
				.unregisterOnSharedPreferenceChangeListener(this);

		super.onDestroy();

		try {
			mAppWidgetHost.stopListening();
		} catch (NullPointerException ex) {
			PersonaLog
					.w(LOG_TAG,
							"problem while stopping AppWidgetHost during PersonaLauncher destruction",
							ex);
		}

		TextKeyListener.getInstance().release();

		mAllAppsGrid.clearTextFilter();
		mAllAppsGrid.setAdapter(null);

		sModel.unbind();
		sModel.abortLoaders();
		mWorkspace.unbindWidgetScrollableViews();
		getContentResolver().unregisterContentObserver(mObserver);
		getContentResolver().unregisterContentObserver(mWidgetObserver);
		//Badge
				getContentResolver().unregisterContentObserver(mDialerBadgeObserver);
		unregisterReceiver(mApplicationsReceiver);
		unregisterReceiver(mCloseSystemDialogsReceiver);
		if (mCounterReceiver != null)
			unregisterReceiver(mCounterReceiver);
		mWorkspace.unregisterProvider();
	}

	/*
	 * @Override public void startActivityForResult(Intent intent, int
	 * requestCode) { if(intent==null)return; //ADW: closing drawer, removed
	 * from onpause if (requestCode !=REQUEST_SHOW_APP_LIST && //do not close
	 * drawer if it is for switching catalogue.
	 * !PersonaCustomShirtcutActivity.ACTION_LAUNCHERACTION
	 * .equals(intent.getAction())) closeDrawer(false); if (requestCode >= 0)
	 * mWaitingForResult = true; try{ super.startActivityForResult(intent,
	 * requestCode); }catch (Exception e){
	 * Toast.makeText(this,R.string.activity_not_found,Toast.LENGTH_SHORT); } }
	 */

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		// TODO Auto-generated method stub
		PersonaLog.d("PersonaLauncher", "==== startActivityForResult ");
		if (requestCode >= 0)
			mWaitingForResult = true;
		PersonaLog.d("PersonaLauncher", "intent " + intent + "requestCode"
				+ requestCode);
		super.startActivityForResult(intent, requestCode);
	}

	/*
	 * @Override public void startSearch(String initialQuery, boolean
	 * selectInitialQuery, Bundle appSearchData, boolean globalSearch) {
	 * 
	 * closeDrawer(false);
	 * 
	 * // Slide the search widget to the top, if it's on the current screen, //
	 * otherwise show the search dialog immediately. PersonaSearch searchWidget
	 * = mWorkspace.findSearchWidgetOnCurrentScreen(); if (searchWidget == null)
	 * { showSearchDialog(initialQuery, selectInitialQuery, appSearchData,
	 * globalSearch); } else { searchWidget.startSearch(initialQuery,
	 * selectInitialQuery, appSearchData, globalSearch); // show the currently
	 * typed text in the search widget while sliding
	 * searchWidget.setQuery(getTypedText()); } }
	 */

	/**
	 * Show the search dialog immediately, without changing the search widget.
	 * 
	 * @see Activity#startSearch(String, boolean, android.os.Bundle, boolean)
	 */
	/*
	 * void showSearchDialog(String initialQuery, boolean selectInitialQuery,
	 * Bundle appSearchData, boolean globalSearch) {
	 * 
	 * if (initialQuery == null) { // Use any text typed in the launcher as the
	 * initial query initialQuery = getTypedText(); clearTypedText(); } if
	 * (appSearchData == null) { appSearchData = new Bundle();
	 * appSearchData.putString("source", "launcher-search"); }
	 * 
	 * final SearchManager searchManager = (SearchManager)
	 * getSystemService(Context.SEARCH_SERVICE);
	 * 
	 * final PersonaSearch searchWidget =
	 * mWorkspace.findSearchWidgetOnCurrentScreen(); if (searchWidget != null) {
	 * // This gets called when the user leaves the search dialog to go back to
	 * // the PersonaLauncher. searchManager.setOnCancelListener(new
	 * SearchManager.OnCancelListener() { public void onCancel() {
	 * searchManager.setOnCancelListener(null); stopSearch(); } }); }
	 * 
	 * searchManager.startSearch(initialQuery, selectInitialQuery,
	 * getComponentName(), appSearchData, globalSearch); }
	 */

	/**
	 * Cancel search dialog if it is open.
	 */
	/*
	 * void stopSearch() { // Close search dialog SearchManager searchManager =
	 * (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	 * searchManager.stopSearch(); // Restore search widget to its normal
	 * position PersonaSearch searchWidget =
	 * mWorkspace.findSearchWidgetOnCurrentScreen(); if (searchWidget != null) {
	 * searchWidget.stopSearch(false); } }
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mDesktopLocked && mSavedInstanceState == null)
			return false;

		super.onCreateOptionsMenu(menu);
		menu.add(MENU_GROUP_ADD, MENU_ADD, 0, R.string.menu_add)
				.setIcon(android.R.drawable.ic_menu_add)
				.setAlphabeticShortcut('A');
		menu.add(MENU_GROUP_NORMAL, MENU_SEARCH, 0, R.string.menu_search)
				.setIcon(android.R.drawable.ic_search_category_default)
				.setAlphabeticShortcut(SearchManager.MENU_KEY);
		menu.add(MENU_GROUP_NORMAL, MENU_NOTIFICATIONS, 0, R.string.menu_edit)
				.setIcon(android.R.drawable.ic_menu_edit)
				.setAlphabeticShortcut('E');

		final Intent settings = new Intent(
				android.provider.Settings.ACTION_SETTINGS);
		settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		menu.add(MENU_GROUP_NORMAL, MENU_SETTINGS, 0, R.string.menu_settings)
				.setIcon(android.R.drawable.ic_menu_preferences)
				.setAlphabeticShortcut('P').setIntent(settings);
		// ADW: add custom settings
		menu.add(MENU_GROUP_NORMAL, MENU_ALMOSTNEXUS, 0,
				R.string.menu_adw_settings)
				.setIcon(android.R.drawable.ic_menu_preferences)
				.setAlphabeticShortcut('X');

		menu.add(MENU_GROUP_CATALOGUE, MENU_APP_GRP_CONFIG, 0,
				R.string.AppGroupConfig).setIcon(
				android.R.drawable.ic_menu_agenda);
		// menu.add(MENU_GROUP_CATALOGUE, MENU_APP_GRP_RENAME, 0,
		// R.string.AppGroupRename)
		// .setIcon(R.drawable.ic_menu_notifications);
		menu.add(MENU_GROUP_CATALOGUE, MENU_APP_SWITCH_GRP, 0,
				R.string.AppGroupChoose).setIcon(
				android.R.drawable.ic_menu_manage);
		menu.add(MENU_GROUP_CATALOGUE, MENU_APP_DELETE_GRP, 0,
				R.string.AppGroupDel)
				.setIcon(android.R.drawable.ic_menu_delete);
		menu.add(MENU_GROUP_NORMAL, MENU_LOCK_DESKTOP, 0, R.string.menu_lock)
				.setIcon(android.R.drawable.ic_menu_preferences)
				.setAlphabeticShortcut('X');
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (mIsEditMode || mIsWidgetEditMode)
			return false;
		// We can't trust the view state here since views we may not be done
		// binding.
		// Get the vacancy state from the model instead.
		mMenuAddInfo = mWorkspace.findAllVacantCellsFromModel();
		menu.setGroupVisible(MENU_GROUP_ADD, mMenuAddInfo != null
				&& mMenuAddInfo.valid && (!allAppsOpen));
		menu.setGroupVisible(MENU_GROUP_NORMAL, !allAppsOpen);
		menu.setGroupVisible(MENU_GROUP_CATALOGUE, allAppsOpen);
		if (mBlockDesktop) {
			menu.findItem(MENU_LOCK_DESKTOP).setTitle(R.string.menu_unlock);
		} else {
			menu.findItem(MENU_LOCK_DESKTOP).setTitle(R.string.menu_lock);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD:
			addItems();
			return true;

		case MENU_SEARCH:
			onSearchRequested();
			return true;
		case MENU_NOTIFICATIONS:
			// showNotifications();
			// ADW: temp usage for desktop eiting
			if (allAppsOpen)
				closeAllApps(false);
			startDesktopEdit();
			return true;
		case MENU_ALMOSTNEXUS:
			showCustomConfig();
			return true;
		case MENU_APP_GRP_CONFIG:
			showAppList();
			return true;
		case MENU_APP_GRP_RENAME:
			showNewGrpDialog();
			return true;
		case MENU_APP_SWITCH_GRP:
			showSwitchGrp();
			return true;
		case MENU_APP_DELETE_GRP:
			showDeleteGrpDialog();
		case MENU_LOCK_DESKTOP:
			mBlockDesktop = !mBlockDesktop;
			PersonaAlmostNexusSettingsHelper.setDesktopBlocked(this,
					mBlockDesktop);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void showAppList() {

		final PersonaAppCatalogueFilter flt = sModel.getApplicationsAdapter()
				.getCatalogueFilter();
		if (!flt.isUserGroup()) {
			Toast.makeText(this, getString(R.string.AppGroupConfigError),
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent i = new Intent(this, PersonaAppInfoMList.class);
		i.putExtra(PersonaAppInfoMList.EXTRA_CATALOGUE_INDEX,
				flt.getCurrentFilterIndex());
		startActivityForResult(i, REQUEST_SHOW_APP_LIST);
	}

	void showDeleteGrpDialog() {
		if (!sModel.getApplicationsAdapter().getCatalogueFilter().isUserGroup()) {
			Toast.makeText(this, getString(R.string.AppGroupConfigError),
					Toast.LENGTH_SHORT).show();
			return;
		}
		showDialog(DIALOG_DELETE_GROUP_CONFIRM);
	}

	void showNewGrpDialog() {
		mWaitingForResult = true;
		showDialog(DIALOG_NEW_GROUP);
	}

	/**
	 * Indicates that we want global search for this activity by setting the
	 * globalSearch argument for {@link #startSearch} to true.
	 */

	@Override
	public boolean onSearchRequested() {
		startSearch(null, false, null, true);
		return true;
	}

	private void addItems() {
		showAddDialog(mMenuAddInfo);
	}

	private void removeShortcutsForPackage(String packageName) {
		if (packageName != null && packageName.length() > 0) {
			mWorkspace.removeShortcutsForPackage(packageName);
		}
	}

	private void updateShortcutsForPackage(String packageName) {
		if (packageName != null && packageName.length() > 0) {
			mWorkspace.updateShortcutsForPackage(packageName);
			// ADW: Update ActionButtons icons
			mLAB.reloadIcon(packageName);
			mLAB2.reloadIcon(packageName);
			mRAB.reloadIcon(packageName);
			mRAB2.reloadIcon(packageName);
			mHandleView.reloadIcon(packageName);
			mMiniLauncher.reloadIcons(packageName);
		}
	}

	@SuppressWarnings("unused")
	void addAppWidget(final Intent data) {
		// TODO: catch bad widget exception when sent
		int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				-1);

		String customWidget = data.getStringExtra(EXTRA_CUSTOM_WIDGET);
		/*
		 * if (SEARCH_WIDGET.equals(customWidget)) { // We don't need this any
		 * more, since this isn't a real app widget.
		 * mAppWidgetHost.deleteAppWidgetId(appWidgetId); // add the search
		 * widget addSearch(); } else {
		 */
		AppWidgetProviderInfo appWidget = mAppWidgetManager
				.getAppWidgetInfo(appWidgetId);

		try {
			Bundle metadata = getPackageManager().getReceiverInfo(
					appWidget.provider, PackageManager.GET_META_DATA).metaData;
			if (metadata != null) {
				if (metadata
						.containsKey(PersonaLauncherMetadata.Requirements.APIVersion)) {
					int requiredApiVersion = metadata
							.getInt(PersonaLauncherMetadata.Requirements.APIVersion);
					if (requiredApiVersion > PersonaLauncherMetadata.CurrentAPIVersion) {
						onActivityResult(REQUEST_CREATE_APPWIDGET,
								Activity.RESULT_CANCELED, data);
						// Show a nice toast here to tell the user why the
						// widget is rejected.
						new AlertDialog.Builder(this)
								.setTitle(R.string.adw_version)
								.setCancelable(true)
								.setIcon(R.drawable.pr_app_icon)
								.setPositiveButton(
										getString(android.R.string.ok), null)
								.setMessage(
										getString(R.string.scrollable_api_required))
								.create().show();
						return;
					}
				}
			}
		} catch (PackageManager.NameNotFoundException expt) {
			// No Metadata available... then it is all OK...
		}
		configureOrAddAppWidget(data);
	}

	private void configureOrAddAppWidget(Intent data) {
		int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				-1);
		AppWidgetProviderInfo appWidget = mAppWidgetManager
				.getAppWidgetInfo(appWidgetId);
		if (appWidget.configure != null) {
			// Launch over to configure widget, if needed
			Intent intent = new Intent(
					AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			intent.setComponent(appWidget.configure);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

			startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
		} else {
			// Otherwise just add it
			onActivityResult(REQUEST_CREATE_APPWIDGET, Activity.RESULT_OK, data);
		}
	}

	@SuppressWarnings("unused")
	void addSearch() {
		final PersonaWidget info = PersonaWidget.makeSearch();
		final PersonaCellLayout.CellInfo cellInfo = mAddItemCellInfo;

		final int[] xy = mCellCoordinates;
		final int spanX = info.spanX;
		final int spanY = info.spanY;

		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		final View dlg_layout = View.inflate(PersonaLauncher.this,
				R.layout.pr_widget_span_setup, null);
		final PersonaNumberPicker ncols = (PersonaNumberPicker) dlg_layout
				.findViewById(R.id.widget_columns_span);
		ncols.setRange(1, mWorkspace.currentDesktopColumns());
		ncols.setCurrent(spanX);
		final PersonaNumberPicker nrows = (PersonaNumberPicker) dlg_layout
				.findViewById(R.id.widget_rows_span);
		nrows.setRange(1, mWorkspace.currentDesktopRows());
		nrows.setCurrent(spanY);
		builder = new AlertDialog.Builder(PersonaLauncher.this);
		builder.setView(dlg_layout);
		alertDialog = builder.create();
		alertDialog.setTitle(getResources().getString(
				R.string.widget_config_dialog_title));
		alertDialog.setMessage(getResources().getString(
				R.string.widget_config_dialog_summary));
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources()
				.getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						int spanX = ncols.getCurrent();
						int spanY = nrows.getCurrent();
						// realAddSearch(info,cellInfo,xy,spanX,spanY);
					}
				});
		alertDialog.show();
	}

	void processShortcut(Intent intent, int requestCodeApplication,
			int requestCodeShortcut) {
		// Handle case where user selected "Applications"
		String applicationName = getResources().getString(
				R.string.group_applications);
		String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

		if (applicationName != null && applicationName.equals(shortcutName)) {
			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

			// 290778 modified for shortcut
			/*
			 * Intent mainIntent = new Intent("Email");
			 * mainIntent.addCategory("android.intent.category.PIM");
			 */

			Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
			pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
			pickIntent.putExtra(Intent.EXTRA_TITLE,
					getResources().getString(R.string.group_applications));
			startActivityForResult(pickIntent, requestCodeApplication);
		} else {
			startActivityForResult(intent, requestCodeShortcut);
		}
	}

	void addLiveFolder(Intent intent) {
		// Handle case where user selected "PersonaFolder"
		String folderName = getResources().getString(R.string.group_folder);
		String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

		if (folderName != null && folderName.equals(shortcutName)) {
			addFolder(!mDesktopLocked);
		} else {
			startActivityForResult(intent, REQUEST_CREATE_LIVE_FOLDER);
		}
	}

	void addFolder(boolean insertAtFirst) {
		PersonaUserFolderInfo folderInfo = new PersonaUserFolderInfo();
		folderInfo.title = getText(R.string.folder_name);

		PersonaCellLayout.CellInfo cellInfo = mAddItemCellInfo;
		cellInfo.screen = mWorkspace.getCurrentScreen();
		if (!findSingleSlot(cellInfo))
			return;

		// Update the model
		PersonaLauncherModel.addItemToDatabase(this, folderInfo,
				PersonaLauncherSettings.Favorites.CONTAINER_DESKTOP,
				mWorkspace.getCurrentScreen(), cellInfo.cellX, cellInfo.cellY,
				false);
		sModel.addDesktopItem(folderInfo);
		sModel.addFolder(folderInfo);

		// Create the view
		PersonaFolderIcon newFolder = PersonaFolderIcon.fromXml(
				R.layout.pr_folder_icon, this, (ViewGroup) mWorkspace
						.getChildAt(mWorkspace.getCurrentScreen()), folderInfo);
		if (themeFont != null)
			((TextView) newFolder).setTypeface(themeFont);
		mWorkspace.addInCurrentScreen(newFolder, cellInfo.cellX,
				cellInfo.cellY, 1, 1, insertAtFirst);
	}

	private void completeAddLiveFolder(Intent data,
			PersonaCellLayout.CellInfo cellInfo, boolean insertAtFirst) {
		cellInfo.screen = mWorkspace.getCurrentScreen();
		if (!findSingleSlot(cellInfo))
			return;

		final PersonaLiveFolderInfo info = addLiveFolder(this, data, cellInfo,
				false);

		if (!mRestoring) {
			sModel.addDesktopItem(info);

			final View view = PersonaLiveFolderIcon.fromXml(
					R.layout.pr_live_folder_icon, this, (ViewGroup) mWorkspace
							.getChildAt(mWorkspace.getCurrentScreen()), info);
			if (themeFont != null)
				((TextView) view).setTypeface(themeFont);
			mWorkspace.addInCurrentScreen(view, cellInfo.cellX, cellInfo.cellY,
					1, 1, insertAtFirst);
		} else if (sModel.isDesktopLoaded()) {
			sModel.addDesktopItem(info);
		}
	}

	static PersonaLiveFolderInfo addLiveFolder(Context context, Intent data,
			PersonaCellLayout.CellInfo cellInfo, boolean notify) {

		Intent baseIntent = data
				.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT);
		String name = data.getStringExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME);

		Drawable icon = null;
		boolean filtered = false;
		Intent.ShortcutIconResource iconResource = null;

		Parcelable extra = data
				.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON);
		if (extra != null && extra instanceof Intent.ShortcutIconResource) {
			try {
				iconResource = (Intent.ShortcutIconResource) extra;
				final PackageManager packageManager = context
						.getPackageManager();
				Resources resources = packageManager
						.getResourcesForApplication(iconResource.packageName);
				final int id = resources.getIdentifier(
						iconResource.resourceName, null, null);
				icon = resources.getDrawable(id);
			} catch (Exception e) {
				PersonaLog.w(LOG_TAG, "Could not load live folder icon: "
						+ extra);
			}
		}

		if (icon == null) {
			icon = context.getResources().getDrawable(
					R.drawable.pr_ic_launcher_folder);
		}

		final PersonaLiveFolderInfo info = new PersonaLiveFolderInfo();
		info.icon = icon;
		info.filtered = filtered;
		info.title = name;
		info.iconResource = iconResource;
		info.uri = data.getData();
		info.baseIntent = baseIntent;
		info.displayMode = data.getIntExtra(
				LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
				LiveFolders.DISPLAY_MODE_GRID);

		PersonaLauncherModel.addItemToDatabase(context, info,
				PersonaLauncherSettings.Favorites.CONTAINER_DESKTOP,
				cellInfo.screen, cellInfo.cellX, cellInfo.cellY, notify);
		sModel.addFolder(info);

		return info;
	}

	private boolean findSingleSlot(PersonaCellLayout.CellInfo cellInfo) {
		final int[] xy = new int[2];
		if (findSlot(cellInfo, xy, 1, 1)) {
			cellInfo.cellX = xy[0];
			cellInfo.cellY = xy[1];
			return true;
		}
		return false;
	}

	private boolean findSlot(PersonaCellLayout.CellInfo cellInfo, int[] xy,
			int spanX, int spanY) {
		if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
			boolean[] occupied = mSavedState != null ? mSavedState
					.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS)
					: null;
			cellInfo = mWorkspace.findAllVacantCells(occupied);
			if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
				Toast.makeText(this, getString(R.string.out_of_space),
						Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		return true;
	}

	private void showNotifications() {
		if (hideStatusBar) {
			fullScreen(false);
			mShouldHideStatusbaronFocus = true;
		}
		try {
			Object service = getSystemService("statusbar");
			if (service != null) {
				Method expand = service.getClass().getMethod("expand");
				expand.invoke(service);
			}
		} catch (Exception e) {
		}
	}

	private void registerIntentReceivers() {
		boolean useNotifReceiver = PersonaAlmostNexusSettingsHelper
				.getNotifReceiver(this);
		if (useNotifReceiver && mCounterReceiver == null) {
			mCounterReceiver = new PersonaCounterReceiver(this);
			mCounterReceiver
					.setCounterListener(new PersonaCounterReceiver.OnCounterChangedListener() {
						public void onTrigger(String pname, int counter,
								int color) {
							updateCountersForPackage(pname, counter, color, 0);
						}
					});
			registerReceiver(mCounterReceiver, mCounterReceiver.getFilter());
		}

		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		registerReceiver(mApplicationsReceiver, filter);
		filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		registerReceiver(mCloseSystemDialogsReceiver, filter);
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
		registerReceiver(mApplicationsReceiver, filter);

	}

	/**
	 * Registers various content observers. The current implementation registers
	 * only a favorites observer to keep track of the favorites applications.
	 */
	private void registerContentObservers() {
		
		ContentResolver resolver = getContentResolver();
		resolver.registerContentObserver(
				PersonaLauncherSettings.Favorites.CONTENT_URI, true, mObserver);

		// TrumobiEdits

		/*
		 * resolver.registerContentObserver(PersonaLauncherProvider.
		 * CONTENT_APPWIDGET_RESET_URI, true, mWidgetObserver);
		 */

		resolver.registerContentObserver(
				PersonaLauncherDbEncryptHelper.CONTENT_APPWIDGET_RESET_URI,
				true, mWidgetObserver);
		//Badge
				Uri uri = CallLog.Calls.CONTENT_URI;
				Uri messengerUri = SmsIndividualTable.CONTENT_URI_INDIVIDUAL;
				mDialerBadgeObserver = new DialerBadgeObserver(null);
				mMessengerBadgeObserver = new MessengerBadgeObserver(null);
				resolver.registerContentObserver(uri, true, mDialerBadgeObserver);
				resolver.registerContentObserver(messengerUri, true, mMessengerBadgeObserver);
	}
	
	private class DialerBadgeObserver extends ContentObserver {

		public DialerBadgeObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			
			if(isTopActivityIsPersonaLauncher()) {
			
				//for dialer - when call comes dialer activity'll come foreground. Onresume badge values are updated. 
				//This case not needed for dialer unless we clear the missed call notifications from notification bar
				runOnUiThread(new Runnable() {
				    @Override
				    public void run() {
				    	updateBadgeCounts();
				    }
				});
				
			}
		}
		
	}
	
	private class MessengerBadgeObserver extends ContentObserver {

		public MessengerBadgeObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			if(isTopActivityIsPersonaLauncher()) {
				//this case comes only when message is received and persona launcher screen is in foreground
				runOnUiThread(new Runnable() {
				    @Override
				    public void run() {
				    	updateBadgeCounts();
				    }
				});
				
			}
		}
		
	}
	
	private boolean isTopActivityIsPersonaLauncher() {
		boolean isTopActivityIsLauncher = false;
		ActivityManager mActivityManager = (ActivityManager)mContext. getSystemService(Context.ACTIVITY_SERVICE);
			String className = mActivityManager.getRunningTasks(1).get(0).topActivity
					.getClassName();
			System.out.println("top activity   name  "+className);
			if (className.equals("com.cognizant.trumobi.PersonaLauncher")) {
				isTopActivityIsLauncher = true;
			}
							
		return isTopActivityIsLauncher;
	}
	
	private int getMissedCallCount() {
		String[] projection = new String[] { CallLog.Calls.NUMBER,
				CallLog.Calls.TYPE, CallLog.Calls.CACHED_NAME};
		int missedCount = 0;
		String where = CallLog.Calls.TYPE+"="+CallLog.Calls.MISSED_TYPE+" AND "+CallLog.Calls.NEW+"= 1 ";
		//String where = CallLog.Calls.TYPE+"="+CallLog.Calls.MISSED_TYPE+" AND "+CallLog.Calls.IS_READ+"= 0 ";
		Cursor c=Email.getAppContext().getContentResolver().
				query(CallLog.Calls.CONTENT_URI, projection, where, null, null);
		if (c != null && c.getCount() > 0 && c.moveToFirst()) {
			try {
				missedCount=c.getCount();
				DialerLog.d("MissedCall Count", "Call Count is "+missedCount);
				//new com.TruBoxSDK.SharedPreferences(CognizantEmail.getAppContext()).edit().putInt("MissedCallCounts", c.getCount()).commit();
			} catch (Exception e) {
				DialerLog.d("MissedCall Count", "Call Count exception"+e.getMessage());
			}
			
		} else {
			DialerLog.d("MissedCall Count", "Call Count is zero");
			missedCount=0;
			//new com.TruBoxSDK.SharedPreferences(CognizantEmail.getAppContext()).edit().putInt("MissedCallCounts", 0).commit();
		}
		
		return missedCount;
		
	}
	

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:

				if (!allAppsOpen) {
					// android.os.Process.killProcess(android.os.Process.myPid());
					mWorkspace.dispatchKeyEvent(event);

					finish();
				}
				mHandleView.updateIcon();
				return true;
			case KeyEvent.KEYCODE_HOME:
				return true;

			}
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				if (!event.isCanceled()) {
					mWorkspace.dispatchKeyEvent(event);
					if (allAppsOpen) {
						closeDrawer();
					} else {
						closeFolder();
					}
					if (isPreviewing()) {
						dismissPreviews();
					}
					if (mIsEditMode) {
						stopDesktopEdit();
					}
					if (mIsWidgetEditMode) {
						stopWidgetEdit();
					}
				}
				return true;
			case KeyEvent.KEYCODE_HOME:
				return true;
			}
		}

		return super.dispatchKeyEvent(event);
	}

	private void closeDrawer() {
		closeDrawer(true);
	}

	private void closeDrawer(boolean animated) {
		if (allAppsOpen) {
			if (animated) {
				closeAllApps(true);
			} else {
				closeAllApps(false);
			}
			if (mAllAppsGrid.hasFocus()) {
				mWorkspace.getChildAt(mWorkspace.getCurrentScreen())
						.requestFocus();
			}
		}
	}

	private void closeFolder() {
		PersonaFolder personaFolder = mWorkspace.getOpenFolder();
		if (personaFolder != null) {
			closeFolder(personaFolder);
		}
	}

	void closeFolder(PersonaFolder personaFolder) {
		personaFolder.getInfo().opened = false;
		ViewGroup parent = (ViewGroup) personaFolder.getParent();
		if (parent != null) {
			parent.removeView(personaFolder);
		}
		personaFolder.onClose();
	}

	/**
	 * When the notification that favorites have changed is received, requests a
	 * favorites list refresh.
	 */
	private void onFavoritesChanged() {
		mDesktopLocked = true;
		sModel.loadUserItems(false, this, false, false);
	}

	/**
	 * Re-listen when widgets are reset.
	 */
	private void onAppWidgetReset() {
		mAppWidgetHost.startListening();
	}

	void onDesktopItemsLoaded(ArrayList<PersonaItemInfo> shortcuts,
			ArrayList<PersonaLauncherAppWidgetInfo> appWidgets) {
		if (mDestroyed) {
			if (PersonaLauncherModel.DEBUG_LOADERS) {
				PersonaLog.d(PersonaLauncherModel.LOG_TAG,
						"  ------> destroyed, ignoring desktop items");
			}
			return;
		}
		bindDesktopItems(shortcuts, appWidgets);
	}

	/**
	 * Refreshes the shortcuts shown on the workspace.
	 */
	private void bindDesktopItems(ArrayList<PersonaItemInfo> shortcuts,
			ArrayList<PersonaLauncherAppWidgetInfo> appWidgets) {

		final PersonaApplicationsAdapter drawerAdapter = sModel
				.getApplicationsAdapter();
		if (shortcuts == null || appWidgets == null || drawerAdapter == null) {
			if (PersonaLauncherModel.DEBUG_LOADERS)
				PersonaLog.d(PersonaLauncherModel.LOG_TAG,
						"  ------> a source is null");
			return;
		}

		final PersonaWorkspace personaWorkspace = mWorkspace;
		int count = personaWorkspace.getChildCount();
		for (int i = 0; i < count; i++) {
			((ViewGroup) personaWorkspace.getChildAt(i))
					.removeAllViewsInLayout();
		}
		final PersonaMiniLauncher personaMiniLauncher = (PersonaMiniLauncher) mDragLayer
				.findViewById(R.id.mini_content);
		personaMiniLauncher.removeAllViewsInLayout();
		if (DEBUG_USER_INTERFACE) {
			android.widget.Button finishButton = new android.widget.Button(this);
			finishButton.setText("Finish");
			personaWorkspace.addInScreen(finishButton, 1, 0, 0, 1, 1);

			finishButton
					.setOnClickListener(new android.widget.Button.OnClickListener() {
						public void onClick(View v) {
							finish();
						}
					});
		}

		// Flag any old binder to terminate early
		if (mBinder != null) {
			mBinder.mTerminate = true;
		}

		mBinder = new DesktopBinder(this, shortcuts, appWidgets, drawerAdapter);
		mBinder.startBindingItems();
	}

	private void bindItems(PersonaLauncher.DesktopBinder binder,
			ArrayList<PersonaItemInfo> shortcuts, int start, int count) {

		final PersonaWorkspace personaWorkspace = mWorkspace;
		final boolean desktopLocked = mDesktopLocked;
		final PersonaMiniLauncher personaMiniLauncher = (PersonaMiniLauncher) mDragLayer
				.findViewById(R.id.mini_content);
		final int end = Math.min(start + DesktopBinder.ITEMS_COUNT, count);
		int i = start;

		for (; i < end; i++) {
			final PersonaItemInfo item = shortcuts.get(i);
			switch ((int) item.container) {
			case PersonaLauncherSettings.Favorites.CONTAINER_LAB:
				mLAB.UpdateLaunchInfo(item);
				break;
			case PersonaLauncherSettings.Favorites.CONTAINER_RAB:
				mRAB.UpdateLaunchInfo(item);
				break;
			case PersonaLauncherSettings.Favorites.CONTAINER_LAB2:
				mLAB2.UpdateLaunchInfo(item);
				break;
			case PersonaLauncherSettings.Favorites.CONTAINER_RAB2:
				mRAB2.UpdateLaunchInfo(item);
				break;
			case PersonaLauncherSettings.Favorites.CONTAINER_MAB:
				mHandleView.UpdateLaunchInfo(item);
				break;
			case PersonaLauncherSettings.Favorites.CONTAINER_DOCKBAR:
				personaMiniLauncher.addItemInDockBar(item);
				break;
			default:
				switch (item.itemType) {
				case PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
				case PersonaLauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
					final View shortcut = createShortcut((PersonaApplicationInfo) item);
					personaWorkspace.addInScreen(shortcut, item.screen,
							item.cellX, item.cellY, 1, 1, !desktopLocked);
					break;
				case PersonaLauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
					final PersonaFolderIcon newFolder = PersonaFolderIcon
							.fromXml(R.layout.pr_folder_icon, this,
									(ViewGroup) personaWorkspace
											.getChildAt(personaWorkspace
													.getCurrentScreen()),
									(PersonaUserFolderInfo) item);
					if (themeFont != null)
						((TextView) newFolder).setTypeface(themeFont);
					personaWorkspace.addInScreen(newFolder, item.screen,
							item.cellX, item.cellY, 1, 1, !desktopLocked);
					break;
				case PersonaLauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
					final PersonaFolderIcon newLiveFolder = PersonaLiveFolderIcon
							.fromXml(R.layout.pr_live_folder_icon, this,
									(ViewGroup) personaWorkspace
											.getChildAt(personaWorkspace
													.getCurrentScreen()),
									(PersonaLiveFolderInfo) item);
					if (themeFont != null)
						((TextView) newLiveFolder).setTypeface(themeFont);
					personaWorkspace.addInScreen(newLiveFolder, item.screen,
							item.cellX, item.cellY, 1, 1, !desktopLocked);
					break;
				/*
				 * case
				 * PersonaLauncherSettings.Favorites.ITEM_TYPE_WIDGET_SEARCH:
				 * final int screen = workspace.getCurrentScreen(); final View
				 * view = mInflater.inflate(R.layout.widget_search, (ViewGroup)
				 * workspace.getChildAt(screen), false);
				 * 
				 * PersonaSearch search = (PersonaSearch)
				 * view.findViewById(R.id.widget_search);
				 * search.setLauncher(this);
				 * 
				 * final PersonaWidget widget = (PersonaWidget) item;
				 * view.setTag(widget);
				 * 
				 * workspace.addWidget(view, widget, !desktopLocked); break;
				 */
				}
			}
		}

		personaWorkspace.requestLayout();

		if (end >= count) {
			finishBindDesktopItems();
			binder.startBindingDrawer();
		} else {
			binder.obtainMessage(DesktopBinder.MESSAGE_BIND_ITEMS, i, count)
					.sendToTarget();
		}
		
		//Badge
				// Counters are not updated after app kill or first time launch. View is assigned ApplicationInfo (setTag is done)only after bindItems called
				//Whenever updateCountersForPackage is called from onCreate, getTag is null since tag is not assigned. So we are not doing that
				
					updateBadgeCounts();
	}

	private void finishBindDesktopItems() {
		if (mSavedState != null) {
			if (!mWorkspace.hasFocus()) {
				mWorkspace.getChildAt(mWorkspace.getCurrentScreen())
						.requestFocus();
			}

			final long[] userFolders = mSavedState
					.getLongArray(RUNTIME_STATE_USER_FOLDERS);
			if (userFolders != null) {
				for (long folderId : userFolders) {
					final PersonaFolderInfo info = sModel
							.findFolderById(folderId);
					if (info != null) {
						openFolder(info);
					}
				}
				final PersonaFolder openFolder = mWorkspace.getOpenFolder();
				if (openFolder != null) {
					openFolder.requestFocus();
				}
			}

			final boolean allApps = mSavedState.getBoolean(
					RUNTIME_STATE_ALL_APPS_FOLDER, false);
			if (allApps) {
				showAllApps(false, null);
			}
			final boolean dockOpen = mSavedState.getBoolean(
					RUNTIME_STATE_DOCKBAR, false);
			if (dockOpen) {
				mDockBar.open();
			}
			mSavedState = null;
		}

		if (mSavedInstanceState != null) {
			// ADW: sometimes on rotating the phone, some widgets fail to
			// restore its states.... so... damn.
			try {
				super.onRestoreInstanceState(mSavedInstanceState);
			} catch (Exception e) {
			}
			mSavedInstanceState = null;
		}

		if (allAppsOpen && !mAllAppsGrid.hasFocus()) {
			mAllAppsGrid.requestFocus();
		}

		mDesktopLocked = false;
		// ADW: Show the changelog screen if needed
		/*
		 * if(PersonaAlmostNexusSettingsHelper.shouldShowChangelog(this)){ try {
		 * AlertDialog builder =
		 * PersonaAlmostNexusSettingsHelper.ChangelogDialogBuilder.create(this);
		 * builder.show(); } catch (Exception e) { e.printStackTrace(); } }
		 */

	}

	private void bindDrawer(PersonaLauncher.DesktopBinder binder,
			PersonaApplicationsAdapter drawerAdapter) {
		int currCatalog = PersonaAlmostNexusSettingsHelper
				.getCurrentAppCatalog(this);
		PersonaAppCatalogueFilters.getInstance().getDrawerFilter()
				.setCurrentGroupIndex(currCatalog);
		drawerAdapter.buildViewCache((ViewGroup) mAllAppsGrid);
		mAllAppsGrid.setAdapter(drawerAdapter);
		mAllAppsGrid.updateAppGrp();
		binder.startBindingAppWidgetsWhenIdle();
	}

	private void bindAppWidgets(PersonaLauncher.DesktopBinder binder,
			LinkedList<PersonaLauncherAppWidgetInfo> appWidgets) {

		final PersonaWorkspace personaWorkspace = mWorkspace;
		final boolean desktopLocked = mDesktopLocked;

		if (!appWidgets.isEmpty()) {
			final PersonaLauncherAppWidgetInfo item = appWidgets.removeFirst();

			final int appWidgetId = item.appWidgetId;
			final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
					.getAppWidgetInfo(appWidgetId);
			item.hostView = mAppWidgetHost.createView(this, appWidgetId,
					appWidgetInfo);

			if (LOGD) {
				PersonaLog.d(LOG_TAG, String.format(
						"about to setAppWidget for id=%d, info=%s",
						appWidgetId, appWidgetInfo));
			}

			item.hostView.setAppWidget(appWidgetId, appWidgetInfo);
			item.hostView.setTag(item);

			personaWorkspace.addInScreen(item.hostView, item.screen,
					item.cellX, item.cellY, item.spanX, item.spanY,
					!desktopLocked);

			personaWorkspace.requestLayout();
			// finish load a widget, send it an intent
			if (appWidgetInfo != null)
				appwidgetReadyBroadcast(appWidgetId, appWidgetInfo.provider,
						new int[] { item.spanX, item.spanY });
		}

		if (appWidgets.isEmpty()) {
			if (PROFILE_ROTATE) {
				android.os.Debug.stopMethodTracing();
			}
		} else {
			binder.obtainMessage(DesktopBinder.MESSAGE_BIND_APPWIDGETS)
					.sendToTarget();
		}
	}

	/**
	 * Launches the intent referred by the clicked shortcut.
	 * 
	 * @param v
	 *            The view representing the clicked shortcut.
	 */
	public void onClick(View v) {
		Object tag = v.getTag();
		// ADW: Check if the tag is a special action (the app drawer category
		// navigation)
		if (tag instanceof Integer) {
			navigateCatalogs(Integer.parseInt(tag.toString()));
			return;
		}
		// TODO:ADW Check whether to display a toast if clicked mLAB or mRAB
		// withount binding
		if (tag == null && v instanceof PersonaActionButton) {
			Toast t = Toast.makeText(this, R.string.toast_no_application_def,
					Toast.LENGTH_SHORT);
			t.show();
			return;
		}
		if (tag instanceof PersonaApplicationInfo) {
			// Open shortcut
			final PersonaApplicationInfo info = (PersonaApplicationInfo) tag;
			final Intent intent = info.intent;
			int[] pos = new int[2];
			v.getLocationOnScreen(pos);
			try {
				intent.setSourceBounds(new Rect(pos[0], pos[1], pos[0]
						+ v.getWidth(), pos[1] + v.getHeight()));
			} catch (NoSuchMethodError e) {
			}
			;
			startActivitySafely(intent);
			// Close dockbar if setting says so
			if (info.container == PersonaLauncherSettings.Favorites.CONTAINER_DOCKBAR
					&& isDockBarOpen() && autoCloseDockbar) {
				mDockBar.close();
			}
		} else if (tag instanceof PersonaFolderInfo) {
			handleFolderClick((PersonaFolderInfo) tag);
		}
	}

	void startActivitySafely(Intent intent) {
		PersonaLog.d("PersonaLauncher", "startActivitySafely intent" + intent);
		try {

			/*
			 * if(audiosettings==1) { //MediaPlayer _shootMP=
			 * MediaPlayer.create(this,
			 * Uri.parse("file:///system/media/audio/ui/one.wav")); MediaPlayer
			 * _shootMP= MediaPlayer.create(this,
			 * Uri.parse("android.resource://com.pkgname/" + R.raw.one));
			 * AudioManager meng = (AudioManager)
			 * this.getSystemService(Context.AUDIO_SERVICE);
			 * 
			 * @SuppressWarnings("unused") int volume =
			 * meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION); if
			 * (_shootMP != null) _shootMP.start(); } else if(audiosettings==2)
			 * { MediaPlayer _shootMP= MediaPlayer.create(this,
			 * Uri.parse("android.resource://com.pkgname/" + R.raw.two));
			 * AudioManager meng = (AudioManager)
			 * this.getSystemService(Context.AUDIO_SERVICE);
			 * 
			 * @SuppressWarnings("unused") int volume =
			 * meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION); if
			 * (_shootMP != null) _shootMP.start(); } else if(audiosettings==3)
			 * { //MediaPlayer _shootMP= MediaPlayer.create(this,
			 * Uri.parse("file:///system/media/audio/ui/three.wav"));
			 * MediaPlayer _shootMP= MediaPlayer.create(this,
			 * Uri.parse("android.resource://com.pkgname/" + R.raw.three));
			 * AudioManager meng = (AudioManager)
			 * this.getSystemService(Context.AUDIO_SERVICE);
			 * 
			 * @SuppressWarnings("unused") int volume =
			 * meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION); if
			 * (_shootMP != null) _shootMP.start(); } else if(audiosettings==4)
			 * { //MediaPlayer _shootMP= MediaPlayer.create(this,
			 * Uri.parse("file:///system/media/audio/ui/four.wav")); MediaPlayer
			 * _shootMP= MediaPlayer.create(this,
			 * Uri.parse("android.resource://com.pkgname/" + R.raw.four));
			 * AudioManager meng = (AudioManager)
			 * this.getSystemService(Context.AUDIO_SERVICE);
			 * 
			 * @SuppressWarnings("unused") int volume =
			 * meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION); if
			 * (_shootMP != null) _shootMP.start(); } else if(audiosettings==5)
			 * { //MediaPlayer _shootMP= MediaPlayer.create(this,
			 * Uri.parse("file:///system/media/audio/ui/five.wav")); MediaPlayer
			 * _shootMP= MediaPlayer.create(this,
			 * Uri.parse("android.resource://com.pkgname/" + R.raw.five));
			 * AudioManager meng = (AudioManager)
			 * this.getSystemService(Context.AUDIO_SERVICE);
			 * 
			 * @SuppressWarnings("unused") int volume =
			 * meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION); if
			 * (_shootMP != null) _shootMP.start(); } else {
			 * 
			 * }
			 */

			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
		} catch (SecurityException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
			PersonaLog
					.e(LOG_TAG,
							"PersonaLauncher does not have the permission to launch "
									+ intent
									+ ". Make sure to create a MAIN intent-filter for the corresponding activity "
									+ "or use the exported attribute for this activity.",
							e);
		}
	}

	private void handleFolderClick(PersonaFolderInfo personaFolderInfo) {
		if (!personaFolderInfo.opened) {
			// Close any open folder
			closeFolder();
			// Open the requested folder
			openFolder(personaFolderInfo);
		} else {
			// Find the open folder...
			PersonaFolder openFolder = mWorkspace
					.getFolderForTag(personaFolderInfo);
			int folderScreen;
			if (openFolder != null) {
				folderScreen = mWorkspace.getScreenForView(openFolder);
				// .. and close it
				closeFolder(openFolder);
				if (folderScreen != mWorkspace.getCurrentScreen()) {
					// Close any folder open on the current screen
					closeFolder();
					// Pull the folder onto this screen
					openFolder(personaFolderInfo);
				}
			}
		}
	}

	/**
	 * Opens the user fodler described by the specified tag. The opening of the
	 * folder is animated relative to the specified View. If the View is null,
	 * no animation is played.
	 * 
	 * @param personaFolderInfo
	 *            The PersonaFolderInfo describing the folder to open.
	 */
	private void openFolder(PersonaFolderInfo personaFolderInfo) {
		PersonaFolder openFolder;

		if (personaFolderInfo instanceof PersonaUserFolderInfo) {
			openFolder = PersonaUserFolder.fromXml(this);
		} else if (personaFolderInfo instanceof PersonaLiveFolderInfo) {
			openFolder = PersonaLiveFolder.fromXml(this, personaFolderInfo);
		} else {
			return;
		}

		openFolder.setDragger(mDragLayer);
		openFolder.setLauncher(this);

		openFolder.bind(personaFolderInfo);
		personaFolderInfo.opened = true;

		if (personaFolderInfo.container == PersonaLauncherSettings.Favorites.CONTAINER_DOCKBAR
				|| personaFolderInfo.container == PersonaLauncherSettings.Favorites.CONTAINER_LAB
				|| personaFolderInfo.container == PersonaLauncherSettings.Favorites.CONTAINER_RAB
				|| personaFolderInfo.container == PersonaLauncherSettings.Favorites.CONTAINER_LAB2
				|| personaFolderInfo.container == PersonaLauncherSettings.Favorites.CONTAINER_RAB2) {
			mWorkspace.addInScreen(openFolder, mWorkspace.getCurrentScreen(),
					0, 0, mWorkspace.currentDesktopColumns(),
					mWorkspace.currentDesktopRows());
		} else {
			mWorkspace.addInScreen(openFolder, personaFolderInfo.screen, 0, 0,
					mWorkspace.currentDesktopColumns(),
					mWorkspace.currentDesktopRows());
		}
		openFolder.onOpen();
		// ADW: closing drawer, removed from onpause
		closeDrawer(false);
	}

	/**
	 * Returns true if the workspace is being loaded. When the workspace is
	 * loading, no user interaction should be allowed to avoid any conflict.
	 * 
	 * @return True if the workspace is locked, false otherwise.
	 */
	boolean isWorkspaceLocked() {
		return mDesktopLocked;
	}

	public boolean onLongClick(View v) {
		if (mDesktopLocked) {
			return false;
		}
		// ADW: Show previews on longpressing the dots
		switch (v.getId()) {
		case R.id.btn_scroll_left:
			mWorkspace.performHapticFeedback(
					HapticFeedbackConstants.LONG_PRESS,
					HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
			showPreviousPreview(v);
			return true;
		case R.id.btn_scroll_right:
			mWorkspace.performHapticFeedback(
					HapticFeedbackConstants.LONG_PRESS,
					HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
			showNextPreview(v);
			return true;
		}

		if (!(v instanceof PersonaCellLayout)) {
			v = (View) v.getParent();
		}

		PersonaCellLayout.CellInfo cellInfo = (PersonaCellLayout.CellInfo) v
				.getTag();

		// This happens when long clicking an item with the dpad/trackball
		if (cellInfo == null) {
			return true;
		}

		if (mWorkspace.allowLongPress() && !mBlockDesktop) {
			if (cellInfo.cell == null) {
				if (cellInfo.valid) {
					// User long pressed on empty space
					mWorkspace.setAllowLongPress(false);
					showAddDialog(cellInfo);
				}
			} else {
				if (!(cellInfo.cell instanceof PersonaFolder)) {
					// User long pressed on an item
					mWorkspace.startDrag(cellInfo);
				}
			}
		}
		return true;
	}

	static PersonaLauncherModel getModel() {
		return sModel;
	}

	void closeAllApplications() {
		mHandleView.updateIcon();
		closeAllApps(false);
	}

	View getDrawerHandle() {
		return mHandleView;
	}

	/*
	 * boolean isDrawerDown() { return !mDrawer.isMoving() &&
	 * !mDrawer.isOpened(); }
	 * 
	 * boolean isDrawerUp() { return mDrawer.isOpened() && !mDrawer.isMoving();
	 * }
	 * 
	 * boolean isDrawerMoving() { return mDrawer.isMoving(); }
	 */

	PersonaWorkspace getWorkspace() {
		return mWorkspace;
	}

	// ADW: we return a View, so classes using this should cast
	// to PersonaAllAppsGridView or PersonaAllAppsSlidingView if they need to
	// access proper members
	View getApplicationsGrid() {
		return (View) mAllAppsGrid;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_CREATE_SHORTCUT:
			return new CreateShortcut().createDialog();
		case DIALOG_RENAME_FOLDER:
			return new RenameFolder().createDialog();
		case DIALOG_CHOOSE_GROUP:
			return new CreateGrpDialog().createDialog();
		case DIALOG_NEW_GROUP:
			return new NewGrpTitle().createDialog();
		case DIALOG_DELETE_GROUP_CONFIRM:
			return new AlertDialog.Builder(this)
					.setTitle(R.string.AppGroupDelLong)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									delCurrentGrp();
									/* User clicked OK so do some stuff */
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* User clicked OK so do some stuff */
								}
							}).create();
		}

		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_CREATE_SHORTCUT:
			break;
		case DIALOG_RENAME_FOLDER:
			if (mFolderInfo != null) {
				EditText input = (EditText) dialog
						.findViewById(R.id.folder_name);
				final CharSequence text = mFolderInfo.title;
				input.setText(text);
				input.setSelection(0, text.length());
			}
			break;
		}
	}

	public void delCurrentGrp() {
		int index = sModel.getApplicationsAdapter().getCatalogueFilter()
				.getCurrentFilterIndex();
		PersonaAppCatalogueFilters.getInstance().dropGroup(index);
		checkActionButtonsSpecialMode();
		showSwitchGrp();
	}

	public void showSwitchGrp() {
		removeDialog(DIALOG_CHOOSE_GROUP);
		showDialog(DIALOG_CHOOSE_GROUP);
	}

	void showRenameDialog(PersonaFolderInfo info) {
		mFolderInfo = info;
		mWaitingForResult = true;
		showDialog(DIALOG_RENAME_FOLDER);
	}

	private void showAddDialog(PersonaCellLayout.CellInfo cellInfo) {
		mAddItemCellInfo = cellInfo;
		mWaitingForResult = true;
		showDialog(DIALOG_CREATE_SHORTCUT);
	}

	private void pickShortcut(int requestCode, int title) {
		Bundle bundle = new Bundle();

		/*
		 * ArrayList<String> shortcutNames = new ArrayList<String>();
		 * shortcutNames.add(getString(R.string.group_applications));
		 * bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);
		 * 
		 * ArrayList<ShortcutIconResource> shortcutIcons = new
		 * ArrayList<ShortcutIconResource>();
		 * shortcutIcons.add(ShortcutIconResource
		 * .fromContext(PersonaLauncher.this,
		 * R.drawable.pr_ic_launcher_application));
		 * bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
		 * shortcutIcons);
		 * 
		 * Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
		 */
		Intent pickIntent = new Intent();

		String ShortCutName = new String(getString(R.string.group_applications));
		bundle.putString(Intent.EXTRA_SHORTCUT_NAME, ShortCutName);

		pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(
				Intent.ACTION_CREATE_SHORTCUT));
		pickIntent.putExtra(Intent.EXTRA_TITLE, getText(title));
		pickIntent.putExtras(bundle);
		processShortcut(pickIntent, REQUEST_PICK_APPLICATION,
				REQUEST_CREATE_SHORTCUT);

		// startActivityForResult(pickIntent, requestCode);
	}

	private class RenameFolder {
		private EditText mInput;

		Dialog createDialog() {
			mWaitingForResult = true;
			final View layout = View.inflate(PersonaLauncher.this,
					R.layout.pr_rename_folder, null);
			mInput = (EditText) layout.findViewById(R.id.folder_name);

			AlertDialog.Builder builder = new AlertDialog.Builder(
					PersonaLauncher.this);
			builder.setIcon(0);
			builder.setTitle(getString(R.string.rename_folder_title));
			builder.setCancelable(true);
			builder.setOnCancelListener(new Dialog.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					cleanup();
				}
			});
			builder.setNegativeButton(getString(R.string.cancel_action),
					new Dialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							cleanup();
						}
					});
			builder.setPositiveButton(getString(R.string.rename_action),
					new Dialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							changeFolderName();
						}
					});
			builder.setView(layout);

			final AlertDialog dialog = builder.create();
			dialog.setOnShowListener(new DialogInterface.OnShowListener() {
				public void onShow(DialogInterface dialog) {
					mWorkspace.lock();
				}
			});

			return dialog;
		}

		private void changeFolderName() {
			final String name = mInput.getText().toString();
			if (!TextUtils.isEmpty(name)) {
				// Make sure we have the right folder info
				mFolderInfo = sModel.findFolderById(mFolderInfo.id);
				mFolderInfo.title = name;
				PersonaLauncherModel.updateItemInDatabase(PersonaLauncher.this,
						mFolderInfo);

				if (mDesktopLocked) {
					sModel.loadUserItems(false, PersonaLauncher.this, false,
							false);
				} else {
					final PersonaFolderIcon personaFolderIcon = (PersonaFolderIcon) mWorkspace
							.getViewForTag(mFolderInfo);
					if (personaFolderIcon != null) {
						personaFolderIcon.setText(name);
						getWorkspace().requestLayout();
					} else {
						mDesktopLocked = true;
						sModel.loadUserItems(false, PersonaLauncher.this,
								false, false);
					}
				}
			}
			cleanup();
		}

		private void cleanup() {
			mWorkspace.unlock();
			try {
				dismissDialog(DIALOG_RENAME_FOLDER);
			} catch (Exception e) {
				// Restarted while dialog or whatever causes
				// IllegalStateException???
			}
			mWaitingForResult = false;
			mFolderInfo = null;
		}
	}

	protected class CreateGrpDialog implements DialogInterface.OnClickListener,
			DialogInterface.OnCancelListener,
			DialogInterface.OnDismissListener, DialogInterface.OnShowListener {

		private PersonaAppGroupAdapter mAdapter;

		Dialog createDialog() {
			mWaitingForResult = true;

			mAdapter = new PersonaAppGroupAdapter(PersonaLauncher.this);

			final AlertDialog.Builder builder = new AlertDialog.Builder(
					PersonaLauncher.this);
			builder.setTitle(getString(R.string.AppGroupChoose));
			builder.setAdapter(mAdapter, this);

			builder.setInverseBackgroundForced(true);

			AlertDialog dialog = builder.create();
			dialog.setOnCancelListener(this);
			dialog.setOnDismissListener(this);
			dialog.setOnShowListener(this);
			return dialog;
		}

		public void onCancel(DialogInterface dialog) {
			mWaitingForResult = false;
			cleanup();
		}

		public void onDismiss(DialogInterface dialog) {
			mWorkspace.unlock();
		}

		private void cleanup() {
			mWorkspace.unlock();
			try {
				dismissDialog(DIALOG_CHOOSE_GROUP);
			} catch (Exception e) {
				// Restarted while dialog or whatever causes
				// IllegalStateException???
			}
		}

		public void onClick(DialogInterface dialog, int which) {
			cleanup();
			PersonaAppGroupAdapter.ListItem itm = (PersonaAppGroupAdapter.ListItem) mAdapter
					.getItem(which);
			int action = itm.actionTag;

			// 1st is add,
			// 2nd is All, mapping to -1, check AppGrpUtils For detail
			// int dbGrp = AppGrpUtils.getGrpNumber(which-2);
			if (action == PersonaAppGroupAdapter.APP_GROUP_ADD) {
				showNewGrpDialog();
			} else {
				sModel.getApplicationsAdapter().getCatalogueFilter()
						.setCurrentGroupIndex(action);
				PersonaAlmostNexusSettingsHelper.setCurrentAppCatalog(
						PersonaLauncher.this, action);
				mAllAppsGrid.updateAppGrp();
				checkActionButtonsSpecialMode();
			}
			// mDrawer.open();
		}

		public void onShow(DialogInterface dialog) {
			mWorkspace.lock();
		}
	}

	private class NewGrpTitle {
		private EditText mInput;

		Dialog createDialog() {
			mWaitingForResult = true;
			final View layout = View.inflate(PersonaLauncher.this,
					R.layout.pr_rename_grp, null);
			mInput = (EditText) layout.findViewById(R.id.group_name);

			AlertDialog.Builder builder = new AlertDialog.Builder(
					PersonaLauncher.this);
			builder.setIcon(0);
			builder.setTitle(getString(R.string.rename_group_title));
			builder.setCancelable(true);
			builder.setOnCancelListener(new Dialog.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					cleanup();
				}
			});
			builder.setNegativeButton(getString(R.string.cancel_action),
					new Dialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							cleanup();
						}
					});
			builder.setPositiveButton(getString(R.string.rename_action),
					new Dialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							newGrpName();
						}
					});
			builder.setView(layout);

			final AlertDialog dialog = builder.create();

			dialog.setOnShowListener(new DialogInterface.OnShowListener() {
				public void onShow(DialogInterface dialog) {
					mWorkspace.lock();
					mInput.requestFocus();
					InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.showSoftInput(mInput, 0);
				}
			});

			return dialog;
		}

		private void newGrpName() {
			final String name = mInput.getText().toString();
			mInput.setText("");
			if (!TextUtils.isEmpty(name)) {
				// Make sure we have the right folder info
				int which = PersonaAppCatalogueFilters.getInstance()
						.createNewGroup(name);
				PersonaAlmostNexusSettingsHelper.setCurrentAppCatalog(
						PersonaLauncher.this, which);
				sModel.getApplicationsAdapter().getCatalogueFilter()
						.setCurrentGroupIndex(which);
				checkActionButtonsSpecialMode();
				PersonaLauncherModel.mApplicationsAdapter.updateDataSet();
			}
			cleanup();
		}

		private void cleanup() {
			mWorkspace.unlock();
			try {
				dismissDialog(DIALOG_NEW_GROUP);
			} catch (Exception e) {
				// Restarted while dialog or whatever causes
				// IllegalStateException???
			}
			mWaitingForResult = false;
			mFolderInfo = null;
		}
	}

	/**
	 * Displays the shortcut creation dialog and launches, if necessary, the
	 * appropriate activity.
	 */
	private class CreateShortcut implements DialogInterface.OnClickListener,
			DialogInterface.OnCancelListener,
			DialogInterface.OnDismissListener, DialogInterface.OnShowListener {

		private PersonaAddAdapter mAdapter;

		Dialog createDialog() {
			mWaitingForResult = true;

			mAdapter = new PersonaAddAdapter(PersonaLauncher.this);

			final AlertDialog.Builder builder = new AlertDialog.Builder(
					PersonaLauncher.this);
			builder.setTitle(getString(R.string.menu_item_add_item));
			builder.setAdapter(mAdapter, this);

			builder.setInverseBackgroundForced(true);

			AlertDialog dialog = builder.create();
			dialog.setOnCancelListener(this);
			dialog.setOnDismissListener(this);
			dialog.setOnShowListener(this);
			dialog.setCanceledOnTouchOutside(true);
			return dialog;
		}

		public void onCancel(DialogInterface dialog) {

			mWaitingForResult = false;
			cleanup();
		}

		public void onDismiss(DialogInterface dialog) {
			mWorkspace.unlock();
		}

		private void cleanup() {
			mWorkspace.unlock();
			try {
				dismissDialog(DIALOG_CREATE_SHORTCUT);
			} catch (Exception e) {
				// Restarted while dialog or whatever causes
				// IllegalStateException???
			}
		}

		/**
		 * Handle the action clicked in the "Add to home" dialog.
		 */
		public void onClick(DialogInterface dialog, int which) {
			Resources res = getResources();
			cleanup();

			switch (which) {
			case PersonaAddAdapter.ITEM_SHORTCUT: {
				// Insert extra item to handle picking application
				pickShortcut(REQUEST_PICK_SHORTCUT,
						R.string.title_select_shortcut);
				break;
			}

			case PersonaAddAdapter.ITEM_APPWIDGET: {
				int appWidgetId = PersonaLauncher.this.mAppWidgetHost
						.allocateAppWidgetId();

				Intent pickIntent = new Intent(
						AppWidgetManager.ACTION_APPWIDGET_PICK);
				pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetId);
				// add the search widget
				ArrayList<AppWidgetProviderInfo> customInfo = new ArrayList<AppWidgetProviderInfo>();
				AppWidgetProviderInfo info = new AppWidgetProviderInfo();
				info.provider = new ComponentName(getPackageName(), "XXX.YYY");
				info.label = getString(R.string.group_search);
				info.icon = R.drawable.pr_ic_search_widget;
				customInfo.add(info);
				pickIntent.putParcelableArrayListExtra(
						AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
				// Commented due to Availabilty of two search icon in Widgets
				// option
				/*
				 * ArrayList<Bundle> customExtras = new ArrayList<Bundle>();
				 * Bundle b = new Bundle(); b.putString(EXTRA_CUSTOM_WIDGET,
				 * SEARCH_WIDGET); customExtras.add(b);
				 * pickIntent.putParcelableArrayListExtra(
				 * AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
				 */
				// start the pick activity
				startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
				break;
			}

			// case PersonaAddAdapter.ITEM_ANYCUT: {
			// Intent anycutIntent=new Intent();
			// anycutIntent.setClass(PersonaLauncher.this,
			// PersonaCustomShirtcutActivity.class);
			// startActivityForResult(anycutIntent, REQUEST_PICK_ANYCUT);
			// break;
			// }

			/*
			 * case PersonaAddAdapter.ITEM_LIVE_FOLDER: { // Insert extra item
			 * to handle inserting folder Bundle bundle = new Bundle();
			 * 
			 * ArrayList<String> shortcutNames = new ArrayList<String>();
			 * shortcutNames.add(res.getString(R.string.group_folder));
			 * bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME,
			 * shortcutNames);
			 * 
			 * ArrayList<ShortcutIconResource> shortcutIcons = new
			 * ArrayList<ShortcutIconResource>();
			 * shortcutIcons.add(ShortcutIconResource
			 * .fromContext(PersonaLauncher.this,
			 * R.drawable.ic_launcher_folder));
			 * bundle.putParcelableArrayList(Intent
			 * .EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);
			 * 
			 * Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
			 * pickIntent.putExtra(Intent.EXTRA_INTENT, new
			 * Intent(LiveFolders.ACTION_CREATE_LIVE_FOLDER));
			 * pickIntent.putExtra(Intent.EXTRA_TITLE,
			 * getText(R.string.title_select_live_folder));
			 * pickIntent.putExtras(bundle);
			 * 
			 * startActivityForResult(pickIntent, REQUEST_PICK_LIVE_FOLDER);
			 * break; }
			 * 
			 * 
			 * case PersonaAddAdapter.ITEM_LAUNCHER_ACTION: {
			 * AlertDialog.Builder builder = new
			 * AlertDialog.Builder(PersonaLauncher.this);
			 * builder.setTitle(getString(R.string.launcher_actions)); final
			 * ListAdapter adapter =
			 * PersonaLauncherActions.getInstance().getSelectActionAdapter();
			 * builder.setAdapter(adapter, new Dialog.OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int which)
			 * { PersonaLauncherActions.Action action =
			 * (PersonaLauncherActions.Action)adapter.getItem(which); Intent
			 * result = new Intent();
			 * result.putExtra(Intent.EXTRA_SHORTCUT_NAME, action.getName());
			 * result.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
			 * PersonaLauncherActions.getInstance().getIntentForAction(action));
			 * ShortcutIconResource iconResource = new ShortcutIconResource();
			 * iconResource.packageName = PersonaLauncher.this.getPackageName();
			 * iconResource.resourceName =
			 * getResources().getResourceName(action.getIconResourceId());
			 * result.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
			 * iconResource); onActivityResult(REQUEST_CREATE_SHORTCUT,
			 * RESULT_OK, result); } }); builder.create().show(); break; }
			 */
			}
		}

		/**
		 * Dialog to show when long pressed on home screen/ now its only widget
		 */
		public void onShow(DialogInterface dialog) {
			mWorkspace.lock();
		}

	}

	/**
	 * Receives notifications when applications are added/removed.
	 */
	private class ApplicationsIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
					|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
					|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {

				final String packageName = intent.getData()
						.getSchemeSpecificPart();
				final boolean replacing = intent.getBooleanExtra(
						Intent.EXTRA_REPLACING, false);

				if (PersonaLauncherModel.DEBUG_LOADERS) {
					PersonaLog.d(PersonaLauncherModel.LOG_TAG,
							"application intent received: " + action
									+ ", replacing=" + replacing);
					PersonaLog.d(PersonaLauncherModel.LOG_TAG, "  --> "
							+ intent.getData());
				}

				if (!Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
					if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
						if (!replacing) {
							removeShortcutsForPackage(packageName);
							if (PersonaLauncherModel.DEBUG_LOADERS) {
								PersonaLog.d(PersonaLauncherModel.LOG_TAG,
										"  --> remove package");
							}
							sModel.removePackage(PersonaLauncher.this,
									packageName);
						}
						// else, we are replacing the package, so a
						// PACKAGE_ADDED will be sent
						// later, we will update the package at this time
					} else {
						if (!replacing) {
							if (PersonaLauncherModel.DEBUG_LOADERS) {
								PersonaLog.d(PersonaLauncherModel.LOG_TAG,
										"  --> add package");
							}
							sModel.addPackage(PersonaLauncher.this, packageName);
						} else {
							if (PersonaLauncherModel.DEBUG_LOADERS) {
								PersonaLog.d(PersonaLauncherModel.LOG_TAG,
										"  --> update package " + packageName);
							}
							sModel.updatePackage(PersonaLauncher.this,
									packageName);
							// 290778 modified for third party apps shortcut
							// updateShortcutsForPackage(packageName);
						}
					}
					removeDialog(DIALOG_CREATE_SHORTCUT);
				} else {
					if (PersonaLauncherModel.DEBUG_LOADERS) {
						PersonaLog.d(PersonaLauncherModel.LOG_TAG,
								"  --> sync package " + packageName);
					}
					sModel.syncPackage(PersonaLauncher.this, packageName);
				}
			} else {
				if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE
						.equals(action)) {
					String packages[] = intent
							.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
					if (packages == null || packages.length == 0) {
						return;
					} else {
						for (int i = 0; i < packages.length; i++) {
							sModel.addPackage(PersonaLauncher.this, packages[i]);
							// 290778 modified for third party apps shortcut
							// updateShortcutsForPackage(packages[i]);
						}
					}
				} else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE
						.equals(action)) {
					String packages[] = intent
							.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
					if (packages == null || packages.length == 0) {
						return;
					} else {
						for (int i = 0; i < packages.length; i++) {
							sModel.removePackage(PersonaLauncher.this,
									packages[i]);
							// ADW: We tell desktop to update packages
							// (probably will load the standard android icon)
							// to show the user the app is no more available.
							// We may add the froyo code to just load a
							// grayscale version of the icon, but...
							// 290778 modified for third party apps shortcut
							// updateShortcutsForPackage(packages[i]);
						}
					}
				}
			}
		}
	}

	/**
	 * Receives notifications when applications are added/removed.
	 */
	private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			closeSystemDialogs();
		}
	}

	/**
	 * Receives notifications whenever the user favorites have changed.
	 */
	private class FavoritesChangeObserver extends ContentObserver {
		public FavoritesChangeObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			onFavoritesChanged();
		}
	}

	/**
	 * Receives notifications whenever the appwidgets are reset.
	 */
	private class AppWidgetResetObserver extends ContentObserver {
		public AppWidgetResetObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			onAppWidgetReset();
		}
	}

	private static class DesktopBinder extends Handler implements
			MessageQueue.IdleHandler {
		static final int MESSAGE_BIND_ITEMS = 0x1;
		static final int MESSAGE_BIND_APPWIDGETS = 0x2;
		static final int MESSAGE_BIND_DRAWER = 0x3;

		// Number of items to bind in every pass
		static final int ITEMS_COUNT = 6;

		private final ArrayList<PersonaItemInfo> mShortcuts;
		private final LinkedList<PersonaLauncherAppWidgetInfo> mAppWidgets;
		private final PersonaApplicationsAdapter mDrawerAdapter;
		private final WeakReference<PersonaLauncher> mLauncher;

		public boolean mTerminate = false;

		DesktopBinder(PersonaLauncher personaLauncher,
				ArrayList<PersonaItemInfo> shortcuts,
				ArrayList<PersonaLauncherAppWidgetInfo> appWidgets,
				PersonaApplicationsAdapter drawerAdapter) {

			mLauncher = new WeakReference<PersonaLauncher>(personaLauncher);
			mShortcuts = shortcuts;
			mDrawerAdapter = drawerAdapter;

			// Sort widgets so active workspace is bound first
			final int currentScreen = personaLauncher.mWorkspace
					.getCurrentScreen();
			final int size = appWidgets.size();
			mAppWidgets = new LinkedList<PersonaLauncherAppWidgetInfo>();

			for (int i = 0; i < size; i++) {
				PersonaLauncherAppWidgetInfo appWidgetInfo = appWidgets.get(i);
				if (appWidgetInfo.screen == currentScreen) {
					mAppWidgets.addFirst(appWidgetInfo);
				} else {
					mAppWidgets.addLast(appWidgetInfo);
				}
			}

			if (PersonaLauncherModel.DEBUG_LOADERS) {
				PersonaLog.d(PersonaLauncher.LOG_TAG, "------> binding "
						+ shortcuts.size() + " items");
				PersonaLog.d(PersonaLauncher.LOG_TAG, "------> binding "
						+ appWidgets.size() + " widgets");
			}
		}

		public void startBindingItems() {
			if (PersonaLauncherModel.DEBUG_LOADERS)
				PersonaLog.d(PersonaLauncher.LOG_TAG,
						"------> start binding items");
			obtainMessage(MESSAGE_BIND_ITEMS, 0, mShortcuts.size())
					.sendToTarget();
		}

		public void startBindingDrawer() {
			obtainMessage(MESSAGE_BIND_DRAWER).sendToTarget();
		}

		public void startBindingAppWidgetsWhenIdle() {
			// Ask for notification when message queue becomes idle
			final MessageQueue messageQueue = Looper.myQueue();
			messageQueue.addIdleHandler(this);
		}

		public boolean queueIdle() {
			// Queue is idle, so start binding items
			startBindingAppWidgets();
			return false;
		}

		public void startBindingAppWidgets() {
			obtainMessage(MESSAGE_BIND_APPWIDGETS).sendToTarget();
		}

		@Override
		public void handleMessage(Message msg) {
			PersonaLauncher personaLauncher = mLauncher.get();
			if (personaLauncher == null || mTerminate) {
				return;
			}

			switch (msg.what) {
			case MESSAGE_BIND_ITEMS: {
				personaLauncher.bindItems(this, mShortcuts, msg.arg1, msg.arg2);
				break;
			}
			case MESSAGE_BIND_DRAWER: {
				personaLauncher.bindDrawer(this, mDrawerAdapter);
				break;
			}
			case MESSAGE_BIND_APPWIDGETS: {
				personaLauncher.bindAppWidgets(this, mAppWidgets);
				break;
			}
			}
		}
	}

	/****************************************************************
	 * ADW: Start custom functions/modifications
	 ***************************************************************/

	/**
	 * ADW: Show the custom settings activity
	 */
	private void showCustomConfig() {
		Intent launchPreferencesIntent = new Intent().setClass(this,
				PersonaMyLauncherSettings.class);
		startActivity(launchPreferencesIntent);
	}

	private void updateAlmostNexusVars() {
		try {
			// allowDrawerAnimations=PersonaAlmostNexusSettingsHelper.getDrawerAnimated(PersonaLauncher.this);
			audiosettings = PersonaAlmostNexusSettingsHelper
					.audio_setting(this);
			wsettings = PersonaAlmostNexusSettingsHelper
					.workspace_settings(this);
			newPreviews = PersonaAlmostNexusSettingsHelper.getNewPreviews(this);
			mHomeBinding = PersonaAlmostNexusSettingsHelper
					.getHomeBinding(this);
			mSwipedownAction = PersonaAlmostNexusSettingsHelper
					.getSwipeDownActions(this);
			mSwipeupAction = PersonaAlmostNexusSettingsHelper
					.getSwipeUpActions(this);
			hideStatusBar = PersonaAlmostNexusSettingsHelper
					.getHideStatusbar(this);
			showDots = PersonaAlmostNexusSettingsHelper.getUIDots(this);
			mDockStyle = PersonaAlmostNexusSettingsHelper
					.getmainDockStyle(this);
			showDockBar = PersonaAlmostNexusSettingsHelper.getUIDockbar(this);
			autoCloseDockbar = PersonaAlmostNexusSettingsHelper
					.getUICloseDockbar(this);
			autoCloseFolder = PersonaAlmostNexusSettingsHelper
					.getUICloseFolder(this);
			hideABBg = PersonaAlmostNexusSettingsHelper.getUIABBg(this);
			uiHideLabels = PersonaAlmostNexusSettingsHelper
					.getUIHideLabels(this);
			if (mWorkspace != null) {
				mWorkspace.setSpeed(PersonaAlmostNexusSettingsHelper
						.getDesktopSpeed(this));
				mWorkspace.setBounceAmount(PersonaAlmostNexusSettingsHelper
						.getDesktopBounce(this));
				mWorkspace.setDefaultScreen(PersonaAlmostNexusSettingsHelper
						.getDefaultScreen(this));
				// mWorkspace.setWallpaperScroll(PersonaAlmostNexusSettingsHelper.getWallpaperScrolling(this));
			}
			int animationSpeed = PersonaAlmostNexusSettingsHelper
					.getZoomSpeed(this);
			if (mAllAppsGrid != null) {
				mAllAppsGrid.setAnimationSpeed(animationSpeed);
			}
			// wallpaperHack=PersonaAlmostNexusSettingsHelper.getWallpaperHack(this);
			// useDrawerCatalogNavigation=PersonaAlmostNexusSettingsHelper.getDrawerCatalogsNavigation(this);
			// mTransitionStyle=PersonaAlmostNexusSettingsHelper.getDesktopTransitionStyle(this);

		} catch (Exception e) {
			// TODO: handle exception
			PersonaLog.i("Error in Updating Shared preference", e.getMessage());
		}

	}

	/**
	 * ADW: Refresh UI status variables and elements after changing settings.
	 */
	private void updateAlmostNexusUI() {
		if (mIsEditMode || mIsWidgetEditMode)
			return;
		updateAlmostNexusVars();
		float scale = PersonaAlmostNexusSettingsHelper.getuiScaleAB(this);
		boolean tint = PersonaAlmostNexusSettingsHelper.getUIABTint(this);
		int tintcolor = PersonaAlmostNexusSettingsHelper.getUIABTintColor(this);
		if (scale != uiScaleAB || tint != uiABTint
				|| tintcolor != uiABTintColor) {
			uiScaleAB = scale;
			uiABTint = tint;
			uiABTintColor = tintcolor;
			mRAB.updateIcon();
			mLAB.updateIcon();
			mRAB2.updateIcon();
			mLAB2.updateIcon();
			mHandleView.updateIcon();

			// 290778 modified Set all apps button without scaling
			// mHandleView.setBackgroundDrawable(getResources().getDrawable(R.drawable.pr_all_apps_button));
		}
		if (!showDockBar) {
			if (mDockBar.isOpen())
				mDockBar.close();
		}
		fullScreen(hideStatusBar);
		if (!mDockBar.isOpen() && !showingPreviews) {
			if (!isAllAppsVisible()) {
				mNextView.setVisibility(showDots ? View.VISIBLE : View.GONE);
				mPreviousView
						.setVisibility(showDots ? View.VISIBLE : View.GONE);
			}
		}
		switch (mDockStyle) {
		case DOCK_STYLE_1:

			mRAB.setVisibility(View.VISIBLE);
			mLAB.setVisibility(View.VISIBLE);
			mRAB2.setVisibility(View.GONE);
			mLAB2.setVisibility(View.GONE);
			if (!mDockBar.isOpen() && !showingPreviews)
				mDrawerToolbar.setVisibility(View.VISIBLE);
			break;
		case DOCK_STYLE_3:
			/*
			 * mRAB.setVisibility(View.VISIBLE);
			 * mLAB.setVisibility(View.VISIBLE);
			 * 
			 * //trumobiEdits mRAB.setVisibility(View.GONE);
			 * mLAB.setVisibility(View.GONE);
			 */
			// trumobiedits finish here

			mRAB.setVisibility(View.VISIBLE);
			mLAB.setVisibility(View.VISIBLE);
			mRAB2.setVisibility(View.GONE);
			mLAB2.setVisibility(View.GONE);
			if (!mDockBar.isOpen() && !showingPreviews)
				mDrawerToolbar.setVisibility(View.VISIBLE);
			break;
		case DOCK_STYLE_5:

			// trumobiEdits 290778
			/*
			 * mRAB.setVisibility(View.VISIBLE);
			 * mLAB.setVisibility(View.VISIBLE);
			 * mRAB2.setVisibility(View.VISIBLE);
			 * mLAB2.setVisibility(View.VISIBLE);
			 */

			/*
			 * mRAB.setVisibility(View.GONE); mLAB.setVisibility(View.GONE);
			 */

			mRAB.setVisibility(View.VISIBLE);
			mLAB.setVisibility(View.VISIBLE);
			mRAB2.setVisibility(View.GONE);
			mLAB2.setVisibility(View.GONE);

			if (!mDockBar.isOpen() && !showingPreviews)
				mDrawerToolbar.setVisibility(View.VISIBLE);
			break;
		case DOCK_STYLE_NONE:
			mDrawerToolbar.setVisibility(View.GONE);
		default:
			break;
		}
		mHandleView.hideBg(hideABBg);
		mRAB.hideBg(hideABBg);
		mLAB.hideBg(hideABBg);
		mRAB2.hideBg(hideABBg);
		mLAB2.hideBg(hideABBg);
		if (mWorkspace != null) {
			mWorkspace.setWallpaperHack(wallpaperHack);
		}
		if (mDesktopIndicator != null) {
			mDesktopIndicator.setType(PersonaAlmostNexusSettingsHelper
					.getDesktopIndicatorType(this));
			mDesktopIndicator.setAutoHide(PersonaAlmostNexusSettingsHelper
					.getDesktopIndicatorAutohide(this));
			if (mWorkspace != null) {
				mDesktopIndicator.setItems(mWorkspace.getChildCount());
			}
			if (isAllAppsVisible()) {
				if (mDesktopIndicator != null)
					mDesktopIndicator.hide();
			}
		}

	}

	/**
	 * ADW: Create a copy of an application icon/shortcut with a reflection
	 * 
	 * @param layoutResId
	 * @param parent
	 * @param info
	 * @return
	 */
	View createSmallShortcut(int layoutResId, ViewGroup parent,
			PersonaApplicationInfo info) {
		PersonaCounterImageView favorite = (PersonaCounterImageView) mInflater
				.inflate(layoutResId, parent, false);

		if (!info.filtered) {
			info.icon = PersonaUtilities.createIconThumbnail(info.icon, this);
			info.filtered = true;
		}
		favorite.setImageDrawable(PersonaUtilities.drawReflection(info.icon,
				this));
		favorite.setTag(info);
		favorite.setOnClickListener(this);
		// ADW: Counters stuff
		favorite.setCounter(info.counter, info.counterColor);
		return favorite;
	}

	/**
	 * ADW: Create a copy of an folder icon with a reflection
	 * 
	 * @param layoutResId
	 * @param parent
	 * @param info
	 * @return
	 */
	View createSmallFolder(int layoutResId, ViewGroup parent,
			PersonaUserFolderInfo info) {
		ImageView favorite = (ImageView) mInflater.inflate(layoutResId, parent,
				false);

		final Resources resources = getResources();
		// Drawable d = resources.getDrawable(R.drawable.ic_launcher_folder);
		Drawable d = null;
		if (PersonaAlmostNexusSettingsHelper.getThemeIcons(this)) {
			String packageName = PersonaAlmostNexusSettingsHelper
					.getThemePackageName(this, THEME_DEFAULT);
			if (packageName.equals(THEME_DEFAULT)) {
				d = resources.getDrawable(R.drawable.pr_ic_launcher_folder);
			} else {
				d = PersonaFolderIcon.loadFolderFromTheme(this,
						getPackageManager(), packageName, "ic_launcher_folder");
				if (d == null) {
					d = resources.getDrawable(R.drawable.pr_ic_launcher_folder);
				}
			}
		} else {
			d = resources.getDrawable(R.drawable.pr_ic_launcher_folder);
		}
		d = PersonaUtilities.drawReflection(d, this);
		favorite.setImageDrawable(d);
		favorite.setTag(info);
		favorite.setOnClickListener(this);
		return favorite;
	}

	/**
	 * ADW: Create a copy of an PersonaLiveFolder icon with a reflection
	 * 
	 * @param layoutResId
	 * @param parent
	 * @param info
	 * @return
	 */
	View createSmallLiveFolder(int layoutResId, ViewGroup parent,
			PersonaLiveFolderInfo info) {
		ImageView favorite = (ImageView) mInflater.inflate(layoutResId, parent,
				false);

		final Resources resources = getResources();
		Drawable d = info.icon;
		if (d == null) {
			if (PersonaAlmostNexusSettingsHelper.getThemeIcons(this)) {
				// Drawable d =
				// resources.getDrawable(R.drawable.ic_launcher_folder);
				String packageName = PersonaAlmostNexusSettingsHelper
						.getThemePackageName(this, THEME_DEFAULT);
				if (packageName.equals(THEME_DEFAULT)) {
					d = resources.getDrawable(R.drawable.pr_ic_launcher_folder);
				} else {
					d = PersonaFolderIcon.loadFolderFromTheme(this,
							getPackageManager(), packageName,
							"ic_launcher_folder");
					if (d == null) {
						d = resources
								.getDrawable(R.drawable.pr_ic_launcher_folder);
					}
				}
			} else {
				d = resources.getDrawable(R.drawable.pr_ic_launcher_folder);
			}
			info.filtered = true;
		}
		d = PersonaUtilities.drawReflection(d, this);
		favorite.setImageDrawable(d);
		favorite.setTag(info);
		favorite.setOnClickListener(this);
		return favorite;
	}

	/**
	 * ADW:Create a smaller copy of an icon for use inside Action Buttons
	 * 
	 * @param info
	 * @return
	 */
	Drawable createSmallActionButtonIcon(PersonaItemInfo info) {
		Drawable d = null;
		final Resources resources = getResources();
		if (info != null) {
			if (info instanceof PersonaApplicationInfo) {
				if (!((PersonaApplicationInfo) info).filtered) {
					((PersonaApplicationInfo) info).icon = PersonaUtilities
							.createIconThumbnail(
									((PersonaApplicationInfo) info).icon, this);
					((PersonaApplicationInfo) info).filtered = true;
				}
				d = ((PersonaApplicationInfo) info).icon;
			} else if (info instanceof PersonaLiveFolderInfo) {
				d = ((PersonaLiveFolderInfo) info).icon;
				if (d == null) {
					if (PersonaAlmostNexusSettingsHelper.getThemeIcons(this)) {
						// d =
						// PersonaUtilities.createIconThumbnail(resources.getDrawable(R.drawable.ic_launcher_folder),
						// this);
						String packageName = PersonaAlmostNexusSettingsHelper
								.getThemePackageName(this, THEME_DEFAULT);
						if (!packageName.equals(THEME_DEFAULT)) {
							d = PersonaFolderIcon.loadFolderFromTheme(this,
									getPackageManager(), packageName,
									"ic_launcher_folder");
						} else {
							d = PersonaUtilities
									.createIconThumbnail(
											resources
													.getDrawable(R.drawable.pr_ic_launcher_folder),
											this);
						}
					} else {
						d = PersonaUtilities.createIconThumbnail(resources
								.getDrawable(R.drawable.pr_ic_launcher_folder),
								this);
					}
					((PersonaLiveFolderInfo) info).filtered = true;
				}
			} else if (info instanceof PersonaUserFolderInfo) {
				if (PersonaAlmostNexusSettingsHelper.getThemeIcons(this)) {
					// d = resources.getDrawable(R.drawable.ic_launcher_folder);
					String packageName = PersonaAlmostNexusSettingsHelper
							.getThemePackageName(this, THEME_DEFAULT);
					if (!packageName.equals(THEME_DEFAULT)) {
						d = PersonaFolderIcon.loadFolderFromTheme(this,
								getPackageManager(), packageName,
								"ic_launcher_folder");
					} else {
						d = resources
								.getDrawable(R.drawable.pr_ic_launcher_folder);
					}
				} else {
					d = resources.getDrawable(R.drawable.pr_ic_launcher_folder);
				}
			}
		}
		if (d == null) {
			// 290778 modified
			d = PersonaUtilities.createIconThumbnail(
					resources.getDrawable(R.drawable.pr_empty), this);
		}
		d = PersonaUtilities.scaledDrawable(d, this, uiABTint, uiScaleAB,
				uiABTintColor);

		return d;
	}

	Drawable createSmallActionButtonDrawable(Drawable d) {
		d = PersonaUtilities.scaledDrawable(d, this, uiABTint, uiScaleAB,
				uiABTintColor);
		return d;
	}

	// ADW: Previews Functions
	public void previousScreen(View v) {
		mWorkspace.scrollLeft();
	}

	public void nextScreen(View v) {
		mWorkspace.scrollRight();
	}

	protected boolean isPreviewing() {
		return showingPreviews;
	}

	private void fullScreen(boolean enable) {
		if (enable) {
			// go full screen
			WindowManager.LayoutParams attrs = getWindow().getAttributes();
			attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			getWindow().setAttributes(attrs);
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
			hideStatusBar = true;
		} else {
			// go non-full screen
			WindowManager.LayoutParams attrs = getWindow().getAttributes();
			attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().setAttributes(attrs);
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
			hideStatusBar = false;
		}
	}

	private void hideDesktop(boolean enable) {
		if (enable) {
			if (mDesktopIndicator != null)
				mDesktopIndicator.hide();
			mNextView.setVisibility(View.INVISIBLE);
			mPreviousView.setVisibility(View.INVISIBLE);
			mDrawerToolbar.setVisibility(View.GONE);
			if (mDockBar.isOpen()) {
				mDockBar.setVisibility(View.INVISIBLE);
			}
		} else {
			if (mDesktopIndicator != null)
				mDesktopIndicator.show();
			if (mDockBar.isOpen()) {
				mDockBar.setVisibility(View.VISIBLE);
			} else {
				if (mDockStyle != DOCK_STYLE_NONE)
					mDrawerToolbar.setVisibility(View.VISIBLE);
				// 290778 commented
				/*
				 * if(showDots){ mNextView.setVisibility(View.VISIBLE);
				 * mPreviousView.setVisibility(View.VISIBLE); }
				 */
			}
		}
	}

	public void dismissPreviews() {
		if (showingPreviews) {
			if (newPreviews) {
				hideDesktop(false);
				showingPreviews = false;
				// mDesktopLocked=false;
				mWorkspace.openSense(false);
			} else {
				dismissPreview(mNextView);
				dismissPreview(mPreviousView);
				dismissPreview(mHandleView);
				for (int i = 0; i < mWorkspace.getChildCount(); i++) {
					View cell = mWorkspace.getChildAt(i);
					cell.setDrawingCacheEnabled(false);
				}
			}
		}
	}

	private void dismissPreview(final View v) {
		final PopupWindow window = (PopupWindow) v.getTag(R.id.TAG_PREVIEW);
		if (window != null) {
			hideDesktop(false);
			window.setOnDismissListener(new PopupWindow.OnDismissListener() {
				@SuppressWarnings("unchecked")
				public void onDismiss() {
					ViewGroup group = (ViewGroup) v.getTag(R.id.workspace);
					int count = group.getChildCount();
					for (int i = 0; i < count; i++) {
						((ImageView) group.getChildAt(i))
								.setImageDrawable(null);
					}
					ArrayList<Bitmap> bitmaps = (ArrayList<Bitmap>) v
							.getTag(R.id.icon);
					for (Bitmap bitmap : bitmaps)
						bitmap.recycle();

					v.setTag(R.id.workspace, null);
					v.setTag(R.id.icon, null);
					window.setOnDismissListener(null);
				}
			});
			window.dismiss();
			showingPreviews = false;
			mWorkspace.unlock();
			mWorkspace.invalidate();
			mDesktopLocked = false;
		}
		v.setTag(R.id.TAG_PREVIEW, null);
	}

	private void showPreviousPreview(View anchor) {
		if (mWorkspace == null)
			return;
		int current = mWorkspace.getCurrentScreen();
		if (newPreviews) {
			if (current <= 0)
				return;
			showPreviews(anchor, 0, mWorkspace.getCurrentScreen());
		} else {
			showPreviews(anchor, 0, mWorkspace.getChildCount());
		}
	}

	private void showNextPreview(View anchor) {
		if (mWorkspace == null)
			return;
		int current = mWorkspace.getCurrentScreen();
		if (newPreviews) {
			if (current >= mWorkspace.getChildCount() - 1)
				return;
			showPreviews(anchor, mWorkspace.getCurrentScreen() + 1,
					mWorkspace.getChildCount());
		} else {
			showPreviews(anchor, 0, mWorkspace.getChildCount());
		}
	}

	public void showPreviews(final View anchor, int start, int end) {
		if (mWorkspace != null && mWorkspace.getChildCount() > 0) {
			if (newPreviews) {
				showingPreviews = true;
				hideDesktop(true);
				mWorkspace.lock();
				mWorkspace.openSense(true);
			} else {
				// check first if it's already open
				final PopupWindow window = (PopupWindow) anchor
						.getTag(R.id.TAG_PREVIEW);
				if (window != null)
					return;
				Resources resources = getResources();

				PersonaWorkspace personaWorkspace = mWorkspace;
				PersonaCellLayout cell = ((PersonaCellLayout) personaWorkspace
						.getChildAt(start));
				float max;
				ViewGroup preview;
				max = personaWorkspace.getChildCount();
				preview = new LinearLayout(this);

				Rect r = new Rect();
				// ADW: seems sometimes this throws an out of memory error....
				// so...
				try {
					resources.getDrawable(R.drawable.pr_preview_background)
							.getPadding(r);
				} catch (OutOfMemoryError e) {
				}
				int extraW = (int) ((r.left + r.right) * max);
				int extraH = r.top + r.bottom;

				int aW = cell.getWidth() - extraW;
				float w = aW / max;

				int width = cell.getWidth();
				int height = cell.getHeight();
				// width -= (x + cell.getRightPadding());
				// height -= (y + cell.getBottomPadding());
				if (width != 0 && height != 0) {
					showingPreviews = true;
					float scale = w / width;

					int count = end - start;

					final float sWidth = width * scale;
					float sHeight = height * scale;

					PreviewTouchHandler handler = new PreviewTouchHandler(
							anchor);
					ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>(count);

					for (int i = start; i < end; i++) {
						ImageView image = new ImageView(this);
						cell = (PersonaCellLayout) personaWorkspace
								.getChildAt(i);
						Bitmap bitmap = Bitmap.createBitmap((int) sWidth,
								(int) sHeight, Bitmap.Config.ARGB_8888);
						cell.setDrawingCacheEnabled(false);
						Canvas c = new Canvas(bitmap);
						c.scale(scale, scale);
						c.translate(-cell.getLeftPadding(),
								-cell.getTopPadding());
						cell.dispatchDraw(c);

						image.setBackgroundDrawable(resources
								.getDrawable(R.drawable.pr_preview_background));
						image.setImageBitmap(bitmap);
						image.setTag(i);
						image.setOnClickListener(handler);
						image.setOnFocusChangeListener(handler);
						image.setFocusable(true);
						if (i == mWorkspace.getCurrentScreen())
							image.requestFocus();

						preview.addView(image,
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT);

						bitmaps.add(bitmap);
					}

					PopupWindow p = new PopupWindow(this);
					p.setContentView(preview);
					p.setWidth((int) (sWidth * count + extraW));
					p.setHeight((int) (sHeight + extraH));
					p.setAnimationStyle(R.style.AnimationPreview);
					p.setOutsideTouchable(true);
					p.setFocusable(true);
					p.setBackgroundDrawable(new ColorDrawable(0));
					p.showAsDropDown(anchor, 0, 0);
					p.setOnDismissListener(new PopupWindow.OnDismissListener() {
						public void onDismiss() {
							dismissPreview(anchor);
						}
					});
					anchor.setTag(R.id.TAG_PREVIEW, p);
					anchor.setTag(R.id.workspace, preview);
					anchor.setTag(R.id.icon, bitmaps);
				}
			}
		}
	}

	class PreviewTouchHandler implements View.OnClickListener, Runnable,
			View.OnFocusChangeListener {
		private final View mAnchor;

		public PreviewTouchHandler(View anchor) {
			mAnchor = anchor;
		}

		public void onClick(View v) {
			mWorkspace.snapToScreen((Integer) v.getTag());
			v.post(this);
		}

		public void run() {
			dismissPreview(mAnchor);
		}

		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				mWorkspace.snapToScreen((Integer) v.getTag());
			}
		}
	}

	/**
	 * ADW: Override this to hide statusbar when necessary
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (mShouldHideStatusbaronFocus && hasFocus) {
			fullScreen(true);
			mShouldHideStatusbaronFocus = false;
		}
	}

	/************************************************
	 * ADW: Functions to handle Apps Grid
	 */
	public void showAllApps(boolean animated, PersonaAppCatalogueFilter filter) {

		Drawable d = getResources()
				.getDrawable(R.drawable.pr_goto_personal_app);
		mHandleView.setImageDrawable(createSmallActionButtonDrawable(d));

		try {
			if (!allAppsOpen && mAllAppsGrid != null) {
				if (getWindow().getDecorView().getWidth() > getWindow()
						.getDecorView().getHeight()) {
					int dockSize = (mDockStyle != DOCK_STYLE_NONE) ? mDrawerToolbar
							.getMeasuredWidth() : 0;
					if (dockSize != appDrawerPadding) {
						appDrawerPadding = dockSize;
						mAllAppsGrid.setPadding(0, 0, appDrawerPadding, 0);
					}
					mHandleView.setNextFocusUpId(R.id.drag_layer);
					mHandleView.setNextFocusLeftId(R.id.all_apps_view);
					mLAB.setNextFocusUpId(R.id.drag_layer);
					mLAB.setNextFocusLeftId(R.id.all_apps_view);
					mRAB.setNextFocusUpId(R.id.drag_layer);
					mRAB.setNextFocusLeftId(R.id.all_apps_view);
					mLAB2.setNextFocusUpId(R.id.drag_layer);
					mLAB2.setNextFocusLeftId(R.id.all_apps_view);
					mRAB2.setNextFocusUpId(R.id.drag_layer);
					mRAB2.setNextFocusLeftId(R.id.all_apps_view);
				} else {
					int dockSize = (mDockStyle != DOCK_STYLE_NONE) ? mDrawerToolbar
							.getMeasuredHeight() : 0;
					if (dockSize != appDrawerPadding) {
						appDrawerPadding = dockSize;
						mAllAppsGrid.setPadding(0, 0, 0, appDrawerPadding);
					}
					mHandleView.setNextFocusUpId(R.id.all_apps_view);
					mHandleView.setNextFocusLeftId(R.id.drag_layer);
					mLAB.setNextFocusUpId(R.id.all_apps_view);
					mLAB.setNextFocusLeftId(R.id.drag_layer);
					mRAB.setNextFocusUpId(R.id.all_apps_view);
					mRAB.setNextFocusLeftId(R.id.drag_layer);
					mLAB2.setNextFocusUpId(R.id.all_apps_view);
					mLAB2.setNextFocusLeftId(R.id.drag_layer);
					mRAB2.setNextFocusUpId(R.id.all_apps_view);
					mRAB2.setNextFocusLeftId(R.id.drag_layer);
				}
				mWorkspace.hideWallpaper(true);
				allAppsOpen = true;
				mWorkspace.enableChildrenCache(mWorkspace.getCurrentScreen(),
						mWorkspace.getCurrentScreen());
				mWorkspace.lock();

				// Toast.makeText(this, "PersonaAppCatalogueFilters: "+filter,
				// Toast.LENGTH_LONG).show();
				if (filter != null)
					sModel.getApplicationsAdapter().setCatalogueFilter(filter);
				else
					sModel.getApplicationsAdapter().setCatalogueFilter(
							PersonaAppCatalogueFilters.getInstance()
									.getDrawerFilter());
				// mDesktopLocked=true;
				mWorkspace.invalidate();
				checkActionButtonsSpecialMode();
				mAllAppsGrid.open(animated && allowDrawerAnimations);
				mPreviousView.setVisibility(View.GONE);
				mNextView.setVisibility(View.GONE);
				mRAB.setVisibility(View.GONE);
				mLAB.setVisibility(View.GONE);
				if (mDesktopIndicator != null)
					mDesktopIndicator.hide();
			} else if (filter != null)
				sModel.getApplicationsAdapter().setCatalogueFilter(filter);
			else
				sModel.getApplicationsAdapter().setCatalogueFilter(
						PersonaAppCatalogueFilters.getInstance()
								.getDrawerFilter());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkActionButtonsSpecialMode() {
		boolean showSpecialMode = useDrawerCatalogNavigation
				&& allAppsOpen
				&& PersonaAppCatalogueFilters.getInstance()
						.getUserCatalogueCount() > 0;
		mLAB.setSpecialMode(showSpecialMode);
		mRAB.setSpecialMode(showSpecialMode);
	}

	private void closeAllApps(boolean animated) {
		if (allAppsOpen && mAllAppsGrid != null) {
			mHandleView.setNextFocusUpId(R.id.drag_layer);
			mHandleView.setNextFocusLeftId(R.id.drag_layer);
			mLAB.setNextFocusUpId(R.id.drag_layer);
			mLAB.setNextFocusLeftId(R.id.drag_layer);
			mRAB.setNextFocusUpId(R.id.drag_layer);
			mRAB.setNextFocusLeftId(R.id.drag_layer);
			mLAB2.setNextFocusUpId(R.id.drag_layer);
			mLAB2.setNextFocusLeftId(R.id.drag_layer);
			mRAB2.setNextFocusUpId(R.id.drag_layer);
			mRAB2.setNextFocusLeftId(R.id.drag_layer);
			mWorkspace.hideWallpaper(false);
			allAppsOpen = false;
			mWorkspace.unlock();
			// mDesktopLocked=false;
			mWorkspace.invalidate();
			mLAB.setSpecialMode(false);
			mRAB.setSpecialMode(false);

			if (!isDockBarOpen() && showDots) {
				mPreviousView.setVisibility(View.VISIBLE);
				mNextView.setVisibility(View.VISIBLE);
				mRAB.setVisibility(View.VISIBLE);
				mLAB.setVisibility(View.VISIBLE);
			} else {
				mPreviousView.setVisibility(View.GONE);
				mNextView.setVisibility(View.GONE);
				mRAB.setVisibility(View.GONE);
				mLAB.setVisibility(View.GONE);
			}
			if (mDesktopIndicator != null)
				mDesktopIndicator.show();

			mAllAppsGrid.close(animated && allowDrawerAnimations);
			mAllAppsGrid.clearTextFilter();
		}
	}

	boolean isAllAppsVisible() {
		return allAppsOpen;
		/*
		 * if(mAllAppsGrid!=null) return
		 * mAllAppsGrid.getVisibility()==View.VISIBLE; else return false;
		 */
	}

	boolean isAllAppsOpaque() {
		return mAllAppsGrid.isOpaque() && !allAppsAnimating;
	}

	protected boolean isDockBarOpen() {
		return mDockBar.isOpen();
	}

	public void setWindowBackground(boolean lwp) {
		wallpaperHack = lwp;
		if (!lwp) {
			getWindow().setBackgroundDrawable(null);
			getWindow().setFormat(PixelFormat.OPAQUE);
			// getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		} else {
			getWindow().setBackgroundDrawable(new ColorDrawable(0));
			getWindow().setFormat(PixelFormat.TRANSPARENT);
			// getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		}
	}

	private boolean shouldRestart() {
		try {
			if (mShouldRestart) {
				android.os.Process.killProcess(android.os.Process.myPid());
				finish();
				startActivity(getIntent());
				return true;
			} else {
				/*
				 * if(mMessWithPersistence){ int
				 * currentOrientation=getResources(
				 * ).getConfiguration().orientation;
				 * if(currentOrientation!=savedOrientation){
				 * mShouldRestart=true; finish(); startActivity(getIntent()); }
				 * }
				 */
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		// ADW: Try to add the restart flag here instead on preferences activity
		if (PersonaAlmostNexusSettingsHelper.needsRestart(key)) {
			// 290778 Commented for Higher SDK version
			// setPersistent(false);
			mShouldRestart = false;
		} else {
			// TODO: ADW Move here all the updates instead on
			// updateAlmostNexusUI()
			if (key.equals("homeOrientation")) {
				if (!mMessWithPersistence) {
					changeOrientation(
							PersonaAlmostNexusSettingsHelper
									.getDesktopOrientation(this),
							false);
				} else {
					// ADW: If a user changes between different orientation
					// modes
					// we temporarily disable persistence to change the app
					// orientation
					// it will be re-enabled on the next onCreate
					// setPersistent(false);
					changeOrientation(
							PersonaAlmostNexusSettingsHelper
									.getDesktopOrientation(this),
							true);
				}
			} else if (key.equals("systemPersistent")) {
				mMessWithPersistence = PersonaAlmostNexusSettingsHelper
						.getSystemPersistent(this);
				if (mMessWithPersistence) {
					changeOrientation(
							PersonaAlmostNexusSettingsHelper
									.getDesktopOrientation(this),
							true);
					// ADW: If previously in portrait, set persistent
					// else, it will call the setPersistent on the next onCreate
					// caused by the orientation change
					if (savedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
						// setPersistent(true);
					}

				} else {
					// setPersistent(false);
					changeOrientation(
							PersonaAlmostNexusSettingsHelper
									.getDesktopOrientation(this),
							false);
				}
			} else if (key.equals("notif_receiver")) {
				boolean useNotifReceiver = PersonaAlmostNexusSettingsHelper
						.getNotifReceiver(this);
				if (!useNotifReceiver) {
					if (mCounterReceiver != null)
						unregisterReceiver(mCounterReceiver);
					mCounterReceiver = null;
				} else {
					if (mCounterReceiver == null) {
						mCounterReceiver = new PersonaCounterReceiver(this);
						mCounterReceiver
								.setCounterListener(new PersonaCounterReceiver.OnCounterChangedListener() {
									public void onTrigger(String pname,
											int counter, int color) {
										updateCountersForPackage(pname,
												counter, color , 0);
									}
								});
					}
					registerReceiver(mCounterReceiver,
							mCounterReceiver.getFilter());
				}
			} else if (key.equals("main_dock_style")) {
				int dockstyle = PersonaAlmostNexusSettingsHelper
						.getmainDockStyle(this);
				if (dockstyle == DOCK_STYLE_NONE) {
					// mShouldRestart=true;
				} else if (mDockStyle == DOCK_STYLE_NONE) {
					// mShouldRestart=true;
				}
			} else if (key.equals("deletezone_style")) {
				int dz = PersonaAlmostNexusSettingsHelper
						.getDeletezoneStyle(this);
				if (mDeleteZone != null)
					mDeleteZone.setPosition(dz);
			}
			updateAlmostNexusUI();
		}
	}

	private void appwidgetReadyBroadcast(int appWidgetId, ComponentName cname,
			int[] widgetSpan) {
		Intent motosize = new Intent(
				"com.motorola.blur.home.ACTION_SET_WIDGET_SIZE");

		motosize.setComponent(cname);
		motosize.putExtra("appWidgetId", appWidgetId);
		motosize.putExtra("spanX", widgetSpan[0]);
		motosize.putExtra("spanY", widgetSpan[1]);
		motosize.putExtra("com.motorola.blur.home.EXTRA_NEW_WIDGET", true);
		sendBroadcast(motosize);

		Intent ready = new Intent(PersonaLauncherIntent.Action.ACTION_READY)
				.putExtra(PersonaLauncherIntent.Extra.EXTRA_APPWIDGET_ID,
						appWidgetId)
				.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
				.putExtra(PersonaLauncherIntent.Extra.EXTRA_API_VERSION,
						PersonaLauncherMetadata.CurrentAPIVersion)
				.setComponent(cname);
		sendBroadcast(ready);
	}

	/**
	 * ADW: Home binding actions
	 */
	public void fireHomeBinding(int bindingValue, int type) {
		// ADW: switch home button binding user selection
		if (mIsEditMode || mIsWidgetEditMode)
			return;
		switch (bindingValue) {
		case BIND_DEFAULT:
			dismissPreviews();
			if (!mWorkspace.isDefaultScreenShowing()) {
				mWorkspace.moveToDefaultScreen();
			}
			break;
		case BIND_HOME_PREVIEWS:
			if (!mWorkspace.isDefaultScreenShowing()) {
				dismissPreviews();
				mWorkspace.moveToDefaultScreen();
			} else {
				if (!showingPreviews) {
					showPreviews(mHandleView, 0, mWorkspace.mHomeScreens);
				} else {
					dismissPreviews();
				}
			}
			break;
		case BIND_PREVIEWS:
			if (!showingPreviews) {
				showPreviews(mHandleView, 0, mWorkspace.mHomeScreens);
			} else {
				dismissPreviews();
			}
			break;
		case BIND_APPS:
			dismissPreviews();
			if (isAllAppsVisible()) {
				mRAB.setVisibility(View.VISIBLE);
				mLAB.setVisibility(View.VISIBLE);
				mHandleView.updateIcon();
				closeDrawer();
			} else {
				showAllApps(true, null);

			}
			break;
		case BIND_STATUSBAR:
			WindowManager.LayoutParams attrs = getWindow().getAttributes();
			/*
			 * if((attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) ==
			 * WindowManager.LayoutParams.FLAG_FULLSCREEN){ //go non-full screen
			 * fullScreen(false); }else{ //go full screen fullScreen(true); }
			 */
			// 290778 commented for Non full screen mode
			fullScreen(false);
			break;
		case BIND_NOTIFICATIONS:
			dismissPreviews();
			showNotifications();
			break;
		case BIND_HOME_NOTIFICATIONS:
			if (!mWorkspace.isDefaultScreenShowing()) {
				dismissPreviews();
				mWorkspace.moveToDefaultScreen();
			} else {
				dismissPreviews();
				showNotifications();
			}
			break;
		case BIND_DOCKBAR:
			dismissPreviews();
			if (showDockBar) {
				if (mDockBar.isOpen()) {
					mDockBar.close();
				} else {
					mDockBar.open();
				}
			}
			break;
		case BIND_APP_LAUNCHER:
			// Launch or bring to front selected app
			// Get PackageName and ClassName of selected App
			String package_name = "";
			String name = "";
			switch (type) {
			case 1:
				package_name = PersonaAlmostNexusSettingsHelper
						.getHomeBindingAppToLaunchPackageName(this);
				name = PersonaAlmostNexusSettingsHelper
						.getHomeBindingAppToLaunchName(this);
				break;
			case 2:
				package_name = PersonaAlmostNexusSettingsHelper
						.getSwipeUpAppToLaunchPackageName(this);
				name = PersonaAlmostNexusSettingsHelper
						.getSwipeUpAppToLaunchName(this);
				break;
			case 3:
				package_name = PersonaAlmostNexusSettingsHelper
						.getSwipeDownAppToLaunchPackageName(this);
				name = PersonaAlmostNexusSettingsHelper
						.getSwipeDownAppToLaunchName(this);
				break;
			default:
				break;
			}
			// Create Intent to Launch App
			if (package_name != "" && name != "") {
				Intent i = new Intent();
				i.setAction(Intent.ACTION_MAIN);
				i.addCategory(Intent.CATEGORY_LAUNCHER);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				i.setComponent(new ComponentName(package_name, name));
				try {
					startActivity(i);
				} catch (Exception e) {
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * wjax: Swipe down binding action
	 */
	public void fireSwipeDownAction() {
		// wjax: switch SwipeDownAction button binding user selection
		fireHomeBinding(mSwipedownAction, 3);
	}

	/**
	 * wjax: Swipe up binding action
	 */
	public void fireSwipeUpAction() {
		// wjax: switch SwipeUpAction button binding user selection
		fireHomeBinding(mSwipeupAction, 2);
	}

	private void realAddWidget(AppWidgetProviderInfo appWidgetInfo,
			PersonaCellLayout.CellInfo cellInfo, int[] spans, int appWidgetId,
			boolean insertAtFirst) {
		// Try finding open space on PersonaLauncher screen
		final int[] xy = mCellCoordinates;
		if (!findSlot(cellInfo, xy, spans[0], spans[1])) {
			if (appWidgetId != -1)
				mAppWidgetHost.deleteAppWidgetId(appWidgetId);
			return;
		}

		// Build PersonaLauncher-specific widget info and save to database
		PersonaLauncherAppWidgetInfo launcherInfo = new PersonaLauncherAppWidgetInfo(
				appWidgetId);
		launcherInfo.spanX = spans[0];
		launcherInfo.spanY = spans[1];

		PersonaLauncherModel.addItemToDatabase(this, launcherInfo,
				PersonaLauncherSettings.Favorites.CONTAINER_DESKTOP,
				mWorkspace.getCurrentScreen(), xy[0], xy[1], false);

		if (!mRestoring) {
			sModel.addDesktopAppWidget(launcherInfo);

			// Perform actual inflation because we're live
			launcherInfo.hostView = mAppWidgetHost.createView(this,
					appWidgetId, appWidgetInfo);

			launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
			launcherInfo.hostView.setTag(launcherInfo);

			mWorkspace.addInCurrentScreen(launcherInfo.hostView, xy[0], xy[1],
					launcherInfo.spanX, launcherInfo.spanY, insertAtFirst);
		} else if (sModel.isDesktopLoaded()) {
			sModel.addDesktopAppWidget(launcherInfo);
		}
		// finish load a widget, send it an intent
		if (appWidgetInfo != null)
			appwidgetReadyBroadcast(appWidgetId, appWidgetInfo.provider, spans);
		PersonaLog.d("PersonaLauncher", "------realAddWidget method---------");
	}

	/*
	 * private void realAddSearch(PersonaWidget info,final
	 * PersonaCellLayout.CellInfo cellInfo,final int[] xy,int spanX,int spanY){
	 * if (!findSlot(cellInfo, xy, spanX, spanY)) return; info.spanX=spanX;
	 * info.spanY=spanY; sModel.addDesktopItem(info);
	 * PersonaLauncherModel.addItemToDatabase(this, info,
	 * PersonaLauncherSettings.Favorites.CONTAINER_DESKTOP,
	 * mWorkspace.getCurrentScreen(), xy[0], xy[1], false);
	 * 
	 * final View view = mInflater.inflate(info.layoutResource, null);
	 * view.setTag(info); PersonaSearch search = (PersonaSearch)
	 * view.findViewById(R.id.widget_search); search.setLauncher(this);
	 * 
	 * mWorkspace.addInCurrentScreen(view, xy[0], xy[1], spanX, spanY);
	 * 
	 * }
	 */
	public static int getScreenCount(Context context) {
		return PersonaAlmostNexusSettingsHelper.getDesktopScreens(context);
	}

	public PersonaDesktopIndicator getDesktopIndicator() {
		return mDesktopIndicator;
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		// if(mMessWithPersistence)setPersistent(false);
		super.onStart();
		// int currentOrientation=getResources().getConfiguration().orientation;
		// if(currentOrientation!=savedOrientation){
		// mShouldRestart=true;
		// }
	}

	@Override
	protected void onStop() {
		// if(!mShouldRestart){
		// savedOrientation=getResources().getConfiguration().orientation;
		// if(mMessWithPersistence)setPersistent(true);
		// }
		// TODO Auto-generated method stub
		super.onStop();
	}

	/**
	 * ADW: Load the specified theme resource
	 * 
	 * @param themeResources
	 *            Resources from the theme package
	 * @param themePackage
	 *            the theme's package name
	 * @param item_name
	 *            the theme item name to load
	 * @param item
	 *            the View Item to apply the theme into
	 * @param themeType
	 *            Specify if the themed element will be a background or a
	 *            foreground item
	 */
	public static void loadThemeResource(Resources themeResources,
			String themePackage, String item_name, View item, int themeType) {
		Drawable d = null;
		if (themeResources != null) {
			int resource_id = themeResources.getIdentifier(item_name,
					"drawable", themePackage);
			if (resource_id != 0) {
				try {
					d = themeResources.getDrawable(resource_id);
				} catch (Resources.NotFoundException e) {
					return;
				}
				if (themeType == THEME_ITEM_FOREGROUND
						&& item instanceof ImageView) {
					// ADW remove the old drawable
					Drawable tmp = ((ImageView) item).getDrawable();
					if (tmp != null) {
						tmp.setCallback(null);
						tmp = null;
					}
					((ImageView) item).setImageDrawable(d);
				} else {
					// ADW remove the old drawable
					Drawable tmp = item.getBackground();
					if (tmp != null) {
						tmp.setCallback(null);
						tmp = null;
					}
					item.setBackgroundDrawable(d);
				}
			}
		}
	}

	public Typeface getThemeFont() {
		return themeFont;
	}

	private void changeOrientation(int type, boolean persistence) {
		if (!persistence) {
			switch (type) {
			case PersonaAlmostNexusSettingsHelper.ORIENTATION_SENSOR:
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
				break;
			case PersonaAlmostNexusSettingsHelper.ORIENTATION_PORTRAIT:
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
				break;
			case PersonaAlmostNexusSettingsHelper.ORIENTATION_LANDSCAPE:
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
			default:
				break;
			}
		} else {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	void editShirtcut(PersonaApplicationInfo info) {
		Intent edit = new Intent(Intent.ACTION_EDIT);
		edit.setClass(this, PersonaCustomShirtcutActivity.class);
		edit.putExtra(PersonaCustomShirtcutActivity.EXTRA_APPLICATIONINFO,
				info.id);
		startActivityForResult(edit, REQUEST_EDIT_SHIRTCUT);
	}

	private void completeEditShirtcut(Intent data) {
		if (!data.hasExtra(PersonaCustomShirtcutActivity.EXTRA_APPLICATIONINFO))
			return;
		long appInfoId = data.getLongExtra(
				PersonaCustomShirtcutActivity.EXTRA_APPLICATIONINFO, 0);
		PersonaApplicationInfo info = PersonaLauncherModel
				.loadApplicationInfoById(this, appInfoId);
		if (info != null) {
			Bitmap bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

			Drawable icon = null;
			boolean customIcon = false;
			ShortcutIconResource iconResource = null;

			if (bitmap != null) {
				icon = new PersonaFastBitmapDrawable(
						PersonaUtilities.createBitmapThumbnail(bitmap, this));
				customIcon = true;
			} else {
				Parcelable extra = data
						.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
				if (extra != null && extra instanceof ShortcutIconResource) {
					try {
						iconResource = (ShortcutIconResource) extra;
						final PackageManager packageManager = getPackageManager();
						Resources resources = packageManager
								.getResourcesForApplication(iconResource.packageName);
						final int id = resources.getIdentifier(
								iconResource.resourceName, null, null);
						icon = resources.getDrawable(id);
					} catch (Exception e) {
						PersonaLog.w(LOG_TAG, "Could not load shortcut icon: "
								+ extra);
					}
				}
			}

			if (icon != null) {
				info.icon = icon;
				info.customIcon = customIcon;
				info.iconResource = iconResource;
			}
			info.itemType = PersonaLauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
			info.title = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
			info.intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
			PersonaLauncherModel.updateItemInDatabase(this, info);

			if (info.container == PersonaLauncherSettings.Favorites.CONTAINER_MAB)
				mHandleView.UpdateLaunchInfo(info);
			else if (info.container == PersonaLauncherSettings.Favorites.CONTAINER_LAB)
				mLAB.UpdateLaunchInfo(info);
			else if (info.container == PersonaLauncherSettings.Favorites.CONTAINER_LAB2)
				mLAB2.UpdateLaunchInfo(info);
			else if (info.container == PersonaLauncherSettings.Favorites.CONTAINER_RAB)
				mRAB.UpdateLaunchInfo(info);
			else if (info.container == PersonaLauncherSettings.Favorites.CONTAINER_RAB2)
				mRAB2.UpdateLaunchInfo(info);

			mWorkspace.updateShortcutFromApplicationInfo(info);
		}
	}

	/**
	 * ADW: Put the launcher in desktop edit mode We could be able to add,
	 * remove and reorder screens
	 */
	private void startDesktopEdit() {
		if (!mIsEditMode) {
			mIsEditMode = true;
			final PersonaWorkspace personaWorkspace = mWorkspace;
			if (personaWorkspace == null)
				return;
			personaWorkspace.enableChildrenCache(0,
					personaWorkspace.getChildCount());
			hideDesktop(true);
			personaWorkspace.lock();
			// Load a gallery view
			final PersonaScreensAdapter screens = new PersonaScreensAdapter(
					this, personaWorkspace.getChildAt(0).getWidth(),
					personaWorkspace.getChildAt(0).getHeight());
			for (int i = 0; i < personaWorkspace.getChildCount(); i++) {
				screens.addScreen((PersonaCellLayout) personaWorkspace
						.getChildAt(i));
			}
			mScreensEditor = mInflater
					.inflate(R.layout.pr_screens_editor, null);
			final Gallery gal = (Gallery) mScreensEditor
					.findViewById(R.id.gallery_screens);
			gal.setCallbackDuringFling(false);
			gal.setClickable(false);
			gal.setAdapter(screens);
			// Setup delete button event
			View deleteButton = mScreensEditor.findViewById(R.id.delete_screen);
			deleteButton
					.setOnClickListener(new android.view.View.OnClickListener() {
						public void onClick(View v) {
							final int screenToDelete = gal
									.getSelectedItemPosition();
							if (personaWorkspace.getChildCount() > 1) {
								AlertDialog alertDialog = new AlertDialog.Builder(
										PersonaLauncher.this).create();
								alertDialog.setTitle(getResources().getString(
										R.string.title_dialog_xml));
								alertDialog
										.setMessage(getResources()
												.getString(
														R.string.message_delete_desktop_screen));
								alertDialog.setButton(
										DialogInterface.BUTTON_POSITIVE,
										getResources().getString(
												android.R.string.ok),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												personaWorkspace
														.removeScreen(screenToDelete);
												screens.removeScreen(screenToDelete);
											}
										});
								alertDialog.setButton(
										DialogInterface.BUTTON_NEGATIVE,
										getResources().getString(
												android.R.string.cancel),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
											}
										});
								alertDialog.show();
							} else {
								Toast t = Toast
										.makeText(
												PersonaLauncher.this,
												R.string.message_cannot_delete_desktop_screen,
												Toast.LENGTH_LONG);
								t.show();
							}

						}
					});
			// Setup add buttons events
			View addLeftButton = mScreensEditor.findViewById(R.id.add_left);
			addLeftButton
					.setOnClickListener(new android.view.View.OnClickListener() {
						public void onClick(View v) {
							if (screens.getCount() < MAX_SCREENS) {
								final int screenToAddLeft = gal
										.getSelectedItemPosition();
								PersonaCellLayout newScreen = personaWorkspace
										.addScreen(screenToAddLeft);
								screens.addScreen(newScreen, screenToAddLeft);
							} else {
								Toast t = Toast
										.makeText(
												PersonaLauncher.this,
												R.string.message_cannot_add_desktop_screen,
												Toast.LENGTH_LONG);
								t.show();
							}
						}
					});
			View addRightButton = mScreensEditor.findViewById(R.id.add_right);
			addRightButton
					.setOnClickListener(new android.view.View.OnClickListener() {
						public void onClick(View v) {
							if (screens.getCount() < MAX_SCREENS) {
								final int screenToAddRight = gal
										.getSelectedItemPosition();
								PersonaCellLayout newScreen = personaWorkspace
										.addScreen(screenToAddRight + 1);
								screens.addScreen(newScreen,
										screenToAddRight + 1);
							} else {
								Toast t = Toast
										.makeText(
												PersonaLauncher.this,
												R.string.message_cannot_add_desktop_screen,
												Toast.LENGTH_LONG);
								t.show();
							}
						}
					});

			final View swapLeftButton = mScreensEditor
					.findViewById(R.id.swap_left);
			swapLeftButton
					.setOnClickListener(new android.view.View.OnClickListener() {
						public void onClick(View v) {
							int currentScreen = gal.getSelectedItemPosition();
							if (currentScreen > 0) {
								personaWorkspace.swapScreens(currentScreen - 1,
										currentScreen);
								screens.swapScreens(currentScreen - 1,
										currentScreen);
							} else {
								Toast t = Toast
										.makeText(
												PersonaLauncher.this,
												R.string.message_cannot_swap_desktop_screen,
												Toast.LENGTH_LONG);
								t.show();
							}
						}
					});
			final View swapRightButton = mScreensEditor
					.findViewById(R.id.swap_right);
			swapRightButton
					.setOnClickListener(new android.view.View.OnClickListener() {
						public void onClick(View v) {
							int currentScreen = gal.getSelectedItemPosition();
							if (currentScreen < gal.getCount() - 1) {
								personaWorkspace.swapScreens(currentScreen,
										currentScreen + 1);
								screens.swapScreens(currentScreen,
										currentScreen + 1);
							} else {
								Toast t = Toast
										.makeText(
												PersonaLauncher.this,
												R.string.message_cannot_swap_desktop_screen,
												Toast.LENGTH_LONG);
								t.show();
							}
						}
					});
			final View setDefaultButton = mScreensEditor
					.findViewById(R.id.set_default);
			setDefaultButton
					.setOnClickListener(new android.view.View.OnClickListener() {
						public void onClick(View v) {
							int currentScreen = gal.getSelectedItemPosition();
							if (currentScreen < mWorkspace.getChildCount()) {
								mWorkspace.setDefaultScreen(currentScreen);
								PersonaAlmostNexusSettingsHelper
										.setDefaultScreen(PersonaLauncher.this,
												currentScreen);
								Toast t = Toast.makeText(PersonaLauncher.this,
										R.string.pref_title_default_screen,
										Toast.LENGTH_LONG);
								t.show();
							}
						}
					});
			gal.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					if (position <= 0) {
						swapLeftButton.setVisibility(View.GONE);
					} else {
						swapLeftButton.setVisibility(View.VISIBLE);
					}
					if (position < parent.getCount() - 1) {
						swapRightButton.setVisibility(View.VISIBLE);
					} else {
						swapRightButton.setVisibility(View.GONE);
					}
				}

				public void onNothingSelected(AdapterView<?> arg0) {
				}

			});
			mDragLayer.addView(mScreensEditor);
		}
	}

	private void stopDesktopEdit() {
		mIsEditMode = false;
		hideDesktop(false);
		for (int i = 0; i < mWorkspace.getChildCount(); i++) {
			mWorkspace.getChildAt(i).setDrawingCacheEnabled(false);
		}
		mWorkspace.clearChildrenCache();
		mWorkspace.unlock();
		if (mScreensEditor != null) {
			mDragLayer.removeView(mScreensEditor);
			mScreensEditor = null;
		}
	}

	protected boolean isEditMode() {
		return mIsEditMode;
	}

	protected void editWidget(final View widget) {
		if (mWorkspace != null) {
			mIsWidgetEditMode = true;
			final PersonaCellLayout screen = (PersonaCellLayout) mWorkspace
					.getChildAt(mWorkspace.getCurrentScreen());
			if (screen != null) {
				mlauncherAppWidgetInfo = (PersonaLauncherAppWidgetInfo) widget
						.getTag();

				final Intent motosize = new Intent(
						"com.motorola.blur.home.ACTION_SET_WIDGET_SIZE");
				final int appWidgetId = ((AppWidgetHostView) widget)
						.getAppWidgetId();
				final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
						.getAppWidgetInfo(appWidgetId);
				if (appWidgetInfo != null) {
					motosize.setComponent(appWidgetInfo.provider);
				}
				motosize.putExtra("appWidgetId", appWidgetId);
				motosize.putExtra("com.motorola.blur.home.EXTRA_NEW_WIDGET",
						true);
				final int minw = (mWorkspace.getWidth()
						- screen.getLeftPadding() - screen.getRightPadding())
						/ screen.getCountX();
				final int minh = (mWorkspace.getHeight()
						- screen.getBottomPadding() - screen.getTopPadding())
						/ screen.getCountY();
				mScreensEditor = new PersonaResizeViewHandler(this);
				// Create a default HightlightView if we found no face in the
				// picture.
				int width = (mlauncherAppWidgetInfo.spanX * minw);
				int height = (mlauncherAppWidgetInfo.spanY * minh);

				final Rect screenRect = new Rect(0, 0, mWorkspace.getWidth()
						- screen.getRightPadding(), mWorkspace.getHeight()
						- screen.getBottomPadding());
				final int x = mlauncherAppWidgetInfo.cellX * minw;
				final int y = mlauncherAppWidgetInfo.cellY * minh;
				final int[] spans = new int[] { 1, 1 };
				final int[] position = new int[] { 1, 1 };
				final PersonaCellLayout.LayoutParams lp = (PersonaCellLayout.LayoutParams) widget
						.getLayoutParams();
				RectF widgetRect = new RectF(x, y, x + width, y + height);
				((PersonaResizeViewHandler) mScreensEditor).setup(null,
						screenRect, widgetRect, false, false, minw - 10,
						minh - 10);
				mDragLayer.addView(mScreensEditor);
				((PersonaResizeViewHandler) mScreensEditor)
						.setOnValidateSizingRect(new PersonaResizeViewHandler.OnSizeChangedListener() {

							@Override
							public void onTrigger(RectF r) {
								if (r != null) {
									final float left = Math
											.round(r.left / minw) * minw;
									final float top = Math.round(r.top / minh)
											* minh;
									final float right = left
											+ (Math.max(
													Math.round(r.width()
															/ (minw)), 1) * minw);
									final float bottom = top
											+ (Math.max(
													Math.round(r.height()
															/ (minh)), 1) * minh);

									r.set(left, top, right, bottom);
								}
							}
						});
				final Rect checkRect = new Rect();
				((PersonaResizeViewHandler) mScreensEditor)
						.setOnSizeChangedListener(new PersonaResizeViewHandler.OnSizeChangedListener() {
							@Override
							public void onTrigger(RectF r) {
								int[] tmpspans = {
										Math.max(
												Math.round(r.width() / (minw)),
												1),
										Math.max(
												Math.round(r.height() / (minh)),
												1) };
								int[] tmpposition = {
										Math.round(r.left / minw),
										Math.round(r.top / minh) };
								checkRect.set(tmpposition[0], tmpposition[1],
										tmpposition[0] + tmpspans[0],
										tmpposition[1] + tmpspans[1]);
								boolean ocupada = getModel().ocuppiedArea(
										screen.getScreen(), appWidgetId,
										checkRect);
								if (!ocupada) {
									((PersonaResizeViewHandler) mScreensEditor)
											.setColliding(false);
								} else {
									((PersonaResizeViewHandler) mScreensEditor)
											.setColliding(true);
								}
								if (tmpposition[0] != position[0]
										|| tmpposition[1] != position[1]
										|| tmpspans[0] != spans[0]
										|| tmpspans[1] != spans[1]) {
									if (!ocupada) {
										position[0] = tmpposition[0];
										position[1] = tmpposition[1];
										spans[0] = tmpspans[0];
										spans[1] = tmpspans[1];
										lp.cellX = position[0];
										lp.cellY = position[1];
										lp.cellHSpan = spans[0];
										lp.cellVSpan = spans[1];
										widget.setLayoutParams(lp);
										mlauncherAppWidgetInfo.cellX = lp.cellX;
										mlauncherAppWidgetInfo.cellY = lp.cellY;
										mlauncherAppWidgetInfo.spanX = lp.cellHSpan;
										mlauncherAppWidgetInfo.spanY = lp.cellVSpan;
										widget.setTag(mlauncherAppWidgetInfo);
										// send the broadcast
										motosize.putExtra("spanX", spans[0]);
										motosize.putExtra("spanY", spans[1]);
										PersonaLauncher.this
												.sendBroadcast(motosize);
										PersonaLog.d("RESIZEHANDLER",
												"sent resize broadcast");
									}
								}
							}
						});
			}
		}
	}

	private void stopWidgetEdit() {
		mIsWidgetEditMode = false;
		if (mlauncherAppWidgetInfo != null) {
			PersonaLauncherModel.resizeItemInDatabase(this,
					mlauncherAppWidgetInfo,
					PersonaLauncherSettings.Favorites.CONTAINER_DESKTOP,
					mlauncherAppWidgetInfo.screen,
					mlauncherAppWidgetInfo.cellX, mlauncherAppWidgetInfo.cellY,
					mlauncherAppWidgetInfo.spanX, mlauncherAppWidgetInfo.spanY);
			mlauncherAppWidgetInfo = null;
		}
		// Remove the resizehandler view
		if (mScreensEditor != null) {
			mDragLayer.removeView(mScreensEditor);
			mScreensEditor = null;
		}
	}

	private void navigateCatalogs(int direction) {
		final PersonaApplicationsAdapter drawerAdapter = sModel
				.getApplicationsAdapter();
		if (drawerAdapter == null)
			return;

		List<Integer> filterIndexes = PersonaAppCatalogueFilters.getInstance()
				.getGroupsAndSpecialGroupIndexes();
		final PersonaAppCatalogueFilter filter = drawerAdapter
				.getCatalogueFilter();
		int currentFIndex = filter.getCurrentFilterIndex();
		// Translate to index of the list
		currentFIndex = filterIndexes.contains(currentFIndex) ? filterIndexes
				.indexOf(currentFIndex) : filterIndexes
				.indexOf(PersonaAppGroupAdapter.APP_GROUP_ALL);
		switch (direction) {
		case ACTION_CATALOG_PREV:
			currentFIndex--;
			break;
		case ACTION_CATALOG_NEXT:
			currentFIndex++;
			break;
		default:
			break;
		}

		if (currentFIndex < 0)
			currentFIndex = filterIndexes.size() - 1;
		else if (currentFIndex >= filterIndexes.size())
			currentFIndex = 0;
		// Translate to "filter index"
		currentFIndex = filterIndexes.get(currentFIndex);
		filter.setCurrentGroupIndex(currentFIndex);

		if (filter == PersonaAppCatalogueFilters.getInstance()
				.getDrawerFilter())
			PersonaAlmostNexusSettingsHelper.setCurrentAppCatalog(
					PersonaLauncher.this, currentFIndex);
		mAllAppsGrid.updateAppGrp();
		// Uncomment this to show a toast with the name of the new group...
		/*
		 * String name = currentFIndex == PersonaAppGroupAdapter.APP_GROUP_ALL ?
		 * getString(R.string.AppGroupAll) :
		 * PersonaAppCatalogueFilters.getInstance
		 * ().getGroupTitle(currentFIndex); if (name != null) { Toast
		 * t=Toast.makeText(this, name, Toast.LENGTH_SHORT); t.show(); }
		 */
	}

	private void updateCounters(View view, String packageName, int counter,
			int color,int updateCounterFor) {
		Object tag = view.getTag();
		PersonaLog.d("personalauncher","------- view is ---- "+ view.toString());
		if(tag != null)
		PersonaLog.d("personalauncher","------- tag is ---- "+ tag.toString());
				
		if (tag instanceof PersonaApplicationInfo) {
			PersonaApplicationInfo info = (PersonaApplicationInfo) tag;
			// We need to check for ACTION_MAIN otherwise getComponent() might
			// return null for some shortcuts (for instance, for shortcuts to
			// web pages.)
			final Intent intent = info.intent;
			final ComponentName name = intent.getComponent();
			//Badge
			switch(updateCounterFor) {
				case UPDATE_COUNTERS_FOR_DIALER:
				if(name.flattenToString().contains("DialerParentActivity")) {
					//BADGE - below lines from if loop below added since that if loop fails
					if (view instanceof PersonaCounterImageView)
						((PersonaCounterImageView) view).setCounter(counter, color);
					// else if
					view.invalidate();
					sModel.updateCounterDesktopItem(info, counter, color);
				
					//BADGE ends
					}
				break;
				case UPDATE_COUNTERS_FOR_MESSENGER:
					if(name.flattenToString().contains("SmsListdisplay")) {
						//BADGE - below lines from if loop below added since that if loop fails
						if (view instanceof PersonaCounterImageView)
							((PersonaCounterImageView) view).setCounter(counter, color);
						// else if
						view.invalidate();
						sModel.updateCounterDesktopItem(info, counter, color);
					
						//BADGE ends
						}
					break;
			}
			if ((info.itemType == PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION || info.itemType == PersonaLauncherSettings.Favorites.ITEM_TYPE_SHORTCUT)
					&& Intent.ACTION_MAIN.equals(intent.getAction())
					&& name != null
					&& packageName.equals(name.getPackageName())) {
				if (view instanceof PersonaCounterImageView)
					((PersonaCounterImageView) view).setCounter(counter, color);
				// else if
				view.invalidate();
				sModel.updateCounterDesktopItem(info, counter, color);
			}
		}
		else {
			PersonaLog.d("personalauncher","------ tag is not an instance of personaApplicationInfo-----");
			if(tag != null)
			PersonaLog.d("personalauncher","------- tag not an instance of personaapplicationinfo ---- "+ tag.toString());
		}
	}

	private void updateCountersForPackage(String packageName, int counter,
			int color,int updateCountFor) {
		//BADGE 
				packageName = "com.cognizant.trumobi";
				color = R.color.PR_TEXTBOX_FOCUSED_COLOR;
		if (packageName != null && packageName.length() > 0) {
			mWorkspace.updateCountersForPackage(packageName, counter, color);
			// ADW: Update ActionButtons icons
			//BADGE - To hide badge from launcher icon in middle
			//updateCounters(mHandleView, packageName, counter, color);
			//Badge - fourth parameter added to distuinguish update for dialer and messenger
		//	updateCounters(mHandleView, packageName, counter, color);
			PersonaLog.d("personalauncher","mLAB is " + mLAB.toString());
			updateCounters(mLAB, packageName, counter, color, updateCountFor);
			updateCounters(mRAB, packageName, counter, color, updateCountFor);
			updateCounters(mLAB2, packageName, counter, color, updateCountFor);
			updateCounters(mRAB2, packageName, counter, color, updateCountFor);
			mMiniLauncher.updateCounters(packageName, counter, color);
			
			//BADGE - To hide badge in all apps screen
			//sModel.updateCounterForPackage(this, packageName, counter, color);
		}
	}

	@Override
	public void startActivity(Intent intent) {
		// TODO Auto-generated method stub
		PersonaLog.d("PersonaLauncher", " -- startActivity ----");
		final ComponentName name = intent.getComponent();
		if (name != null)
			updateCountersForPackage(name.getPackageName(), 0, 0,0);
		try {
			super.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
			Toast.makeText(this,
					getString(R.string.activity_not_found) + "123",
					Toast.LENGTH_SHORT).show();
		} catch (SecurityException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
			PersonaLog
					.e(LOG_TAG,
							"PersonaLauncher does not have the permission to launch "
									+ intent
									+ ". Make sure to create a MAIN intent-filter for the corresponding activity "
									+ "or use the exported attribute for this activity.",
							e);
		}
	}

	public void showActions(final PersonaItemInfo info, final View view) {
		int[] xy = new int[2];
		// fills the array with the computed coordinates
		view.getLocationInWindow(xy);
		// rectangle holding the clicked view area
		Rect rect = new Rect(xy[0], xy[1], xy[0] + view.getWidth(), xy[1]
				+ view.getHeight());

		// a new PersonaQuickActionWindow object
		final PersonaQuickActionWindow qa = new PersonaQuickActionWindow(this,
				view, rect);
		view.setTag(R.id.TAG_PREVIEW, qa);

		// adds an item to the badge and defines the quick action to be
		// triggered
		// when the item is clicked on
		qa.addItem(getResources()
				.getDrawable(android.R.drawable.ic_menu_delete),
				R.string.menu_delete, new OnClickListener() {
					public void onClick(View v) {
						final PersonaLauncherModel model = PersonaLauncher
								.getModel();
						if (info.container == PersonaLauncherSettings.Favorites.CONTAINER_DESKTOP) {
							if (info instanceof PersonaLauncherAppWidgetInfo) {
								model.removeDesktopAppWidget((PersonaLauncherAppWidgetInfo) info);
							} else {
								model.removeDesktopItem(info);
							}
						} else {
							// in a folder?
							PersonaFolderInfo source = sModel.getFolderById(
									PersonaLauncher.this, info.container);
							if (source instanceof PersonaUserFolderInfo) {
								final PersonaUserFolderInfo personaUserFolderInfo = (PersonaUserFolderInfo) source;
								model.removeUserFolderItem(
										personaUserFolderInfo, info);
							}
						}
						if (info instanceof PersonaUserFolderInfo) {
							final PersonaUserFolderInfo personaUserFolderInfo = (PersonaUserFolderInfo) info;
							PersonaLauncherModel
									.deleteUserFolderContentsFromDatabase(
											PersonaLauncher.this,
											personaUserFolderInfo);
							model.removeUserFolder(personaUserFolderInfo);
						} else if (info instanceof PersonaLauncherAppWidgetInfo) {
							final PersonaLauncherAppWidgetInfo personaLauncherAppWidgetInfo = (PersonaLauncherAppWidgetInfo) info;
							final PersonaLauncherAppWidgetHost appWidgetHost = PersonaLauncher.this
									.getAppWidgetHost();
							PersonaLauncher.this
									.getWorkspace()
									.unbindWidgetScrollableId(
											personaLauncherAppWidgetInfo.appWidgetId);
							if (appWidgetHost != null) {
								appWidgetHost
										.deleteAppWidgetId(personaLauncherAppWidgetInfo.appWidgetId);
							}
						}
						PersonaLauncherModel.deleteItemFromDatabase(
								PersonaLauncher.this, info);
						if (view instanceof PersonaActionButton)
							((PersonaActionButton) view).UpdateLaunchInfo(null);
						else
							((ViewGroup) view.getParent()).removeView(view);

						qa.dismiss();
					}
				});

		if (info instanceof PersonaApplicationInfo) {
			qa.addItem(
					getResources().getDrawable(android.R.drawable.ic_menu_edit),
					R.string.menu_edit, new OnClickListener() {
						public void onClick(View v) {
							editShirtcut((PersonaApplicationInfo) info);
							qa.dismiss();
						}
					});
		} else if (info instanceof PersonaLauncherAppWidgetInfo) {
			qa.addItem(
					getResources().getDrawable(android.R.drawable.ic_menu_edit),
					R.string.menu_edit, new OnClickListener() {
						public void onClick(View v) {
							editWidget(view);
							qa.dismiss();
						}
					});
		}
		if (info instanceof PersonaApplicationInfo
				|| info instanceof PersonaLauncherAppWidgetInfo) {
			qa.addItem(
					getResources().getDrawable(
							android.R.drawable.ic_menu_manage),
					R.string.menu_uninstall, new OnClickListener() {
						public void onClick(View v) {
							String UninstallPkg = null;
							if (info instanceof PersonaApplicationInfo) {
								try {
									final PersonaApplicationInfo appInfo = (PersonaApplicationInfo) info;
									if (appInfo.iconResource != null)
										UninstallPkg = appInfo.iconResource.packageName;
									else {
										PackageManager mgr = PersonaLauncher.this
												.getPackageManager();
										ResolveInfo res = mgr.resolveActivity(
												appInfo.intent, 0);
										UninstallPkg = res.activityInfo.packageName;
									}
									// Dont uninstall ADW ;-)
									if (this.getClass().getPackage().getName()
											.equals(UninstallPkg))
										UninstallPkg = null;
								} catch (Exception e) {
									PersonaLog.w(LOG_TAG,
											"Could not load shortcut icon: "
													+ info);
									UninstallPkg = null;
								}
							} else if (info instanceof PersonaLauncherAppWidgetInfo) {
								PersonaLauncherAppWidgetInfo appwidget = (PersonaLauncherAppWidgetInfo) info;
								final AppWidgetProviderInfo aw = AppWidgetManager
										.getInstance(PersonaLauncher.this)
										.getAppWidgetInfo(appwidget.appWidgetId);
								if (aw != null)
									UninstallPkg = aw.provider.getPackageName();
							}
							if (UninstallPkg != null) {
								Intent uninstallIntent = new Intent(
										Intent.ACTION_DELETE, Uri
												.parse("package:"
														+ UninstallPkg));
								PersonaLauncher.this
										.startActivity(uninstallIntent);
							}
							qa.dismiss();
						}
					});
		}
		// shows the quick action window on the screen
		qa.show();
	}

	@Override
	public void onSwipe() {
		// TODO: specify different action for each PersonaActionButton?
		if (showDockBar)
			mDockBar.open();
	}

	public void setDockPadding(int pad) {
		mDrawerToolbar.setPadding(0, 0, 0, pad);
		mDockBar.setPadding(0, 0, 0, pad);
	}
}
