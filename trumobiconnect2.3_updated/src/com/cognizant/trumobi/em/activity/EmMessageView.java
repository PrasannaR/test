//	Added Functionality to Attachment Icon BugFix #1733

package com.cognizant.trumobi.em.activity;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Browser;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.QuickContact;
import android.provider.ContactsContract.StatusUpdates;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.TruboxFileEncryption;
import com.TruBoxSDK.TruboxFileEncryption.STORAGEMODE;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.calendar.view.CalendarMainActivity;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.commonabstractclass.TruMobiTimerClass;
import com.cognizant.trumobi.contacts.activity.ContactsAddContact;
import com.cognizant.trumobi.container.AsynctaskCallback.AttachmentOpenHelper;
import com.cognizant.trumobi.container.AsynctaskCallback.SecAppFileListener;
import com.cognizant.trumobi.container.Pojo.ChildMailbox;
import com.cognizant.trumobi.container.Utils.OutlookPreference;
import com.cognizant.trumobi.container.Utils.UtilList;
import com.cognizant.trumobi.container.activity.AttachmentListActivity;
import com.cognizant.trumobi.container.activity.MimeTypeTextShow;
import com.cognizant.trumobi.em.EmEmController;
import com.cognizant.trumobi.em.EmUtility;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.mail.EmAddress;
import com.cognizant.trumobi.em.mail.EmMeetingInfo;
import com.cognizant.trumobi.em.mail.EmMessagingException;
import com.cognizant.trumobi.em.mail.EmPackedString;
import com.cognizant.trumobi.em.mail.internet.EmEmailHtmlUtil;
import com.cognizant.trumobi.em.mail.internet.EmMimeUtility;
import com.cognizant.trumobi.em.provider.EmAttachmentProvider;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Attachment;
import com.cognizant.trumobi.em.provider.EmEmailContent.Body;
import com.cognizant.trumobi.em.provider.EmEmailContent.BodyColumns;
import com.cognizant.trumobi.em.provider.EmEmailContent.Message;
import com.cognizant.trumobi.em.service.EmEmailServiceConstants;
import com.cognizant.trumobi.log.CalendarLog;
import com.cognizant.trumobi.tfm.TFDocLoader;

import cx.hell.android.pdfview.OpenFileActivity;


public class EmMessageView extends TruMobiBaseActivity implements OnClickListener,
		OnTouchListener,SecAppFileListener {
	private static final String EXTRA_MESSAGE_ID = "com.cognizant.trumobi.em.MessageView_message_id";
	private static final String EXTRA_MAILBOX_ID = "com.cognizant.trumobi.em.MessageView_mailbox_id";
	/* package */static final String EXTRA_DISABLE_REPLY = "com.cognizant.trumobi.em.MessageView_disable_reply";

	// for saveInstanceState()
	private static final String STATE_MESSAGE_ID = "messageId";

	// Regex that matches start of img Tag. '<(?i)img\s+'.
	private static final Pattern IMG_TAG_START_REGEX = Pattern
			.compile("<(?i)img\\s+");
	// Regex that matches Web URL protocol part as case insensitive.
	private static final Pattern WEB_URL_PROTOCOL = Pattern
			.compile("(?i)http|https://");

	// Support for LoadBodyTask
	private static final String[] BODY_CONTENT_PROJECTION = new String[] {
			Body.RECORD_ID, BodyColumns.MESSAGE_KEY, BodyColumns.HTML_CONTENT,
			BodyColumns.TEXT_CONTENT };

	private static final String[] PRESENCE_STATUS_PROJECTION = new String[] { Contacts.CONTACT_PRESENCE };

	private static final int BODY_CONTENT_COLUMN_RECORD_ID = 0;
	private static final int BODY_CONTENT_COLUMN_MESSAGE_KEY = 1;
	private static final int BODY_CONTENT_COLUMN_HTML_CONTENT = 2;
	private static final int BODY_CONTENT_COLUMN_TEXT_CONTENT = 3;

	private TextView mSubjectView;
	private TextView mFromView;
	private TextView mDateView;
	private TextView mTimeView;
	private TextView mToView;
	private TextView mCcView;
	private View mCcContainerView;
	private WebView mMessageContentView;
	private LinearLayout mAttachments;
	private ImageView mAttachmentIcon;
	private ImageView mFavoriteIcon;
	private View mShowPicturesSection;
	private View mInviteSection;
	private ImageView mSenderPresenceView;
	private ProgressDialog mProgressDialog;
	private View mScrollView;
	private ImageView backButton, titleIcon, connectIcon;
	private TextView titleSubject;

	// calendar meeting invite answers
	private TextView mMeetingYes;
	private TextView mMeetingMaybe;
	private TextView mMeetingNo;
	private int mPreviousMeetingResponse = -1;

	private long mAccountId;
	private long mMessageId;
	private long mMailboxId;
	private Message mMessage;
	private long mWaitForLoadMessageId;

	private LoadMessageTask mLoadMessageTask;
	private LoadBodyTask mLoadBodyTask;
	private LoadAttachmentsTask mLoadAttachmentsTask;
	private PresenceCheckTask mPresenceCheckTask;

	private long mLoadAttachmentId; // the attachment being saved/viewed
	private boolean mLoadAttachmentSave; // if true, saving - if false, viewing
	private String mLoadAttachmentName; // the display name

	private java.text.DateFormat mDateFormat;
	private java.text.DateFormat mTimeFormat;

	private Drawable mFavoriteIconOn;
	private Drawable mFavoriteIconOff;

	private MessageViewHandler mHandler;
	private EmEmController mController;
	private ControllerResults mControllerCallback;

	private View mMoveToNewer;
	private View mMoveToOlder;
	private LoadMessageListTask mLoadMessageListTask;
	private Cursor mMessageListCursor;
	private ContentObserver mCursorObserver;
	private Context myContext;

	// contains the HTML body. Is used by LoadAttachmentTask to display inline
	// images.
	// is null most of the time, is used transiently to pass info to
	// LoadAttachementTask
	private String mHtmlTextRaw;

	// contains the HTML content as set in WebView.
	private String mHtmlTextWebView;

	// this is true when reply & forward are disabled, such as messages in the
	// trash
	private boolean mDisableReplyAndForward;

	private class MessageViewHandler extends Handler {
		private static final int MSG_PROGRESS = 1;
		private static final int MSG_ATTACHMENT_PROGRESS = 2;
		private static final int MSG_LOAD_CONTENT_URI = 3;
		private static final int MSG_SET_ATTACHMENTS_ENABLED = 4;
		private static final int MSG_LOAD_BODY_ERROR = 5;
		private static final int MSG_NETWORK_ERROR = 6;
		private static final int MSG_FETCHING_ATTACHMENT = 10;
		private static final int MSG_VIEW_ATTACHMENT_ERROR = 12;
		private static final int MSG_UPDATE_ATTACHMENT_ICON = 18;
		private static final int MSG_FINISH_LOAD_ATTACHMENT = 19;
		
		private static final int MSG_FINISH_VIEW_ATTACHMENT = 20;//290388

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_PROGRESS:
				setProgressBarIndeterminateVisibility(msg.arg1 != 0);
				break;
			case MSG_ATTACHMENT_PROGRESS:
				boolean progress = (msg.arg1 != 0);
				if (progress) {
					mProgressDialog.setMessage(getString(
							R.string.message_view_fetching_attachment_progress,
							mLoadAttachmentName));
					mProgressDialog.setCancelable(false);	//367712
					mProgressDialog.setCanceledOnTouchOutside(false);
					mProgressDialog.show();
				} else {
					mProgressDialog.dismiss();
				}
				setProgressBarIndeterminateVisibility(progress);
				break;
			case MSG_LOAD_CONTENT_URI:
				String uriString = (String) msg.obj;
				if (mMessageContentView != null) {
					mMessageContentView.loadUrl(uriString);
				}
				break;
			case MSG_SET_ATTACHMENTS_ENABLED:
				for (int i = 0, count = mAttachments.getChildCount(); i < count; i++) {
					AttachmentInfo attachment = (AttachmentInfo) mAttachments
							.getChildAt(i).getTag();
					attachment.viewButton.setEnabled(msg.arg1 == 1);
					attachment.downloadButton.setEnabled(msg.arg1 == 1);
				}
				break;
			case MSG_LOAD_BODY_ERROR:
				Toast.makeText(EmMessageView.this,
						R.string.error_loading_message_body, Toast.LENGTH_LONG)
						.show();
				break;
			case MSG_NETWORK_ERROR:
				Toast.makeText(EmMessageView.this,
						R.string.status_network_error, Toast.LENGTH_LONG)
						.show();
				break;
			case MSG_FETCHING_ATTACHMENT:
				Toast.makeText(
						EmMessageView.this,
						getString(R.string.message_view_fetching_attachment_toast),
						Toast.LENGTH_SHORT).show();
				break;
			case MSG_VIEW_ATTACHMENT_ERROR:
				Toast.makeText(
						EmMessageView.this,
						getString(R.string.message_load_display_attachment_toast), // NaGa
						Toast.LENGTH_SHORT).show();
				break;
			case MSG_UPDATE_ATTACHMENT_ICON:
				((AttachmentInfo) mAttachments.getChildAt(msg.arg1).getTag()).iconView
						.setImageBitmap((Bitmap) msg.obj);
				break;
			case MSG_FINISH_LOAD_ATTACHMENT:
				long attachmentId = (Long) msg.obj;
				doFinishLoadAttachment(attachmentId);
				break;
			case MSG_FINISH_VIEW_ATTACHMENT://290388
				boolean viewProgress = (msg.arg1 != 0);
				if (viewProgress) {
					mProgressDialog.setMessage(getString(
							R.string.message_view_viewing_attachment_progress,
							mLoadAttachmentName));
					mProgressDialog.setCancelable(false);	//367712
					mProgressDialog.setCanceledOnTouchOutside(false);
					mProgressDialog.show();
				} else {
					mProgressDialog.dismiss();
				}
				setProgressBarIndeterminateVisibility(viewProgress);
				break;
			default:
				super.handleMessage(msg);
			}
		}

		public void attachmentProgress(boolean progress) {
			android.os.Message msg = android.os.Message.obtain(this,
					MSG_ATTACHMENT_PROGRESS);
			msg.arg1 = progress ? 1 : 0;
			sendMessage(msg);
		}
		
		public void attachmentViewProgress(boolean progress) {
			android.os.Message msg = android.os.Message.obtain(this,
					MSG_FINISH_VIEW_ATTACHMENT);
			msg.arg1 = progress ? 1 : 0;
			sendMessage(msg);
		}

		public void progress(boolean progress) {
			android.os.Message msg = android.os.Message.obtain(this,
					MSG_PROGRESS);
			msg.arg1 = progress ? 1 : 0;
			sendMessage(msg);
		}

		public void loadContentUri(String uriString) {
			android.os.Message msg = android.os.Message.obtain(this,
					MSG_LOAD_CONTENT_URI);
			msg.obj = uriString;
			sendMessage(msg);
		}

		public void setAttachmentsEnabled(boolean enabled) {
			android.os.Message msg = android.os.Message.obtain(this,
					MSG_SET_ATTACHMENTS_ENABLED);
			msg.arg1 = enabled ? 1 : 0;
			sendMessage(msg);
		}

		public void loadBodyError() {
			sendEmptyMessage(MSG_LOAD_BODY_ERROR);
		}

		public void networkError() {
			sendEmptyMessage(MSG_NETWORK_ERROR);
		}

		public void fetchingAttachment() {
			sendEmptyMessage(MSG_FETCHING_ATTACHMENT);
		}

		public void attachmentViewError() {
			sendEmptyMessage(MSG_VIEW_ATTACHMENT_ERROR);
		}

		public void updateAttachmentIcon(int pos, Bitmap icon) {
			android.os.Message msg = android.os.Message.obtain(this,
					MSG_UPDATE_ATTACHMENT_ICON);
			msg.arg1 = pos;
			msg.obj = icon;
			sendMessage(msg);
		}

		public void finishLoadAttachment(long attachmentId) {
			android.os.Message msg = android.os.Message.obtain(this,
					MSG_FINISH_LOAD_ATTACHMENT);
			msg.obj = Long.valueOf(attachmentId);
			sendMessage(msg);
		}
	}

	/**
	 * Encapsulates known information about a single attachment.
	 */
	private static class AttachmentInfo {
		public String name;
		public String contentType;
		public long size;
		public long attachmentId;
		public Button viewButton;
		public Button downloadButton;
		public ImageView iconView;
	}

	/**
	 * View a specific message found in the Email provider.
	 * 
	 * @param messageId
	 *            the message to view.
	 * @param mailboxId
	 *            identifies the sequence of messages used for newer/older
	 *            navigation.
	 * @param disableReplyAndForward
	 *            set if reply/forward do not make sense for this message (e.g.
	 *            messages in Trash).
	 */
	public static void actionView(Context context, long messageId,
			long mailboxId, boolean disableReplyAndForward) {
		if (messageId < 0) {
			throw new IllegalArgumentException("MessageView invalid messageId "
					+ messageId);
		}
		Intent i = new Intent(context, EmMessageView.class);
		i.putExtra(EXTRA_MESSAGE_ID, messageId);
		i.putExtra(EXTRA_MAILBOX_ID, mailboxId);
		i.putExtra(EXTRA_DISABLE_REPLY, disableReplyAndForward);
		context.startActivity(i);
	}

	public static void actionView(Context context, long messageId,
			long mailboxId) {
		actionView(context, messageId, mailboxId, false);
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.em_message_view);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.em_customtitlebar);

		myContext = this;

		mHandler = new MessageViewHandler();
		mControllerCallback = new ControllerResults();

		mSubjectView = (TextView) findViewById(R.id.subject);
		mFromView = (TextView) findViewById(R.id.from);
		mToView = (TextView) findViewById(R.id.to);
		mCcView = (TextView) findViewById(R.id.cc);
		mCcContainerView = findViewById(R.id.cc_container);
		mDateView = (TextView) findViewById(R.id.date);
		mTimeView = (TextView) findViewById(R.id.time);
		mMessageContentView = (WebView) findViewById(R.id.message_content);
		mAttachments = (LinearLayout) findViewById(R.id.attachments);
		mAttachmentIcon = (ImageView) findViewById(R.id.attachment);
		mFavoriteIcon = (ImageView) findViewById(R.id.favorite);
		mShowPicturesSection = findViewById(R.id.show_pictures_section);
		mInviteSection = findViewById(R.id.invite_section);
		mSenderPresenceView = (ImageView) findViewById(R.id.presence);
		mMoveToNewer = findViewById(R.id.moveToNewer);
		mMoveToOlder = findViewById(R.id.moveToOlder);
		mScrollView = findViewById(R.id.scrollview);

		titleIcon = (ImageView) findViewById(R.id.outlooklogo);
		connectIcon = (ImageView) findViewById(R.id.connectHome);
		backButton = (ImageView) findViewById(R.id.goback); // NaGa
		backButton.setVisibility(View.VISIBLE);
		titleSubject = (TextView) findViewById(R.id.title);
		titleIcon.setOnTouchListener(this);
		connectIcon.setOnTouchListener(this);
		backButton.setOnTouchListener(this);

		mMoveToNewer.setOnClickListener(this);
		mMoveToOlder.setOnClickListener(this);
		mFromView.setOnClickListener(this);
		mSenderPresenceView.setOnClickListener(this);
		mFavoriteIcon.setOnClickListener(this);
		findViewById(R.id.reply).setOnClickListener(this);
		findViewById(R.id.reply_all).setOnClickListener(this);
		findViewById(R.id.delete).setOnClickListener(this);
		findViewById(R.id.show_pictures).setOnClickListener(this);

		mMeetingYes = (TextView) findViewById(R.id.accept);
		mMeetingMaybe = (TextView) findViewById(R.id.maybe);
		mMeetingNo = (TextView) findViewById(R.id.decline);

		mMeetingYes.setOnClickListener(this);
		mMeetingMaybe.setOnClickListener(this);
		mMeetingNo.setOnClickListener(this);
		findViewById(R.id.invite_link).setOnClickListener(this);

		mMessageContentView.setClickable(true);
		mMessageContentView.setLongClickable(false); // Conflicts with
														// ScrollView,
														// unfortunately
		mMessageContentView.setVerticalScrollBarEnabled(false);
		mMessageContentView.getSettings().setBlockNetworkLoads(true);
		mMessageContentView.getSettings().setSupportZoom(false);
		mMessageContentView.setWebViewClient(new CustomWebViewClient());

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		mDateFormat = android.text.format.DateFormat.getDateFormat(this); // short
																			// format
		mTimeFormat = android.text.format.DateFormat.getTimeFormat(this); // 12/24
																			// date
																			// format

		mFavoriteIconOn = getResources().getDrawable(
				R.drawable.em_btn_star_on_normal_email_holo_light); // NaGa
		mFavoriteIconOff = getResources().getDrawable(
				R.drawable.em_btn_star_off_normal_email_holo_light);

		initFromIntent();
		if (icicle != null) {
			mMessageId = icicle.getLong(STATE_MESSAGE_ID, mMessageId);
		}

		mController = EmEmController.getInstance(getApplication());

		// This observer is used to watch for external changes to the message
		// list
		mCursorObserver = new ContentObserver(mHandler) {
			@Override
			public void onChange(boolean selfChange) {
				// get a new message list cursor, but only if we already had one
				// (otherwise it's "too soon" and other pathways will cause it
				// to be loaded)
				if (mLoadMessageListTask == null && mMessageListCursor != null) {
					mLoadMessageListTask = new LoadMessageListTask(mMailboxId);
					mLoadMessageListTask.execute();
				}
			}
		};

		messageChanged();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		EmMessageView.this.finish();
	}
	
	/* package */void initFromIntent() {
		Intent intent = getIntent();
		mMessageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1);
		mMailboxId = intent.getLongExtra(EXTRA_MAILBOX_ID, -1);
		mDisableReplyAndForward = intent.getBooleanExtra(EXTRA_DISABLE_REPLY,
				false);
		if (mDisableReplyAndForward) {
			findViewById(R.id.reply).setEnabled(false);
			findViewById(R.id.reply_all).setEnabled(false);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		if (mMessageId != -1) {
			state.putLong(STATE_MESSAGE_ID, mMessageId);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mWaitForLoadMessageId = -1;
		mController.addResultCallback(mControllerCallback);

		File dir = new File(this.getFilesDir()+File.separator+"temp"+File.separator);
		
		if(!dir.exists())
			dir.mkdirs();
		
		UtilList.recursiveDelete(dir);
		// Exit immediately if the accounts list has changed (e.g. externally
		// deleted)
		if (Email.getNotifyUiAccountsChanged()) {
			EmWelcome.actionStart(this);
			finish();
			return;
		}

		if (mMessage != null) {
			startPresenceCheck();

			// get a new message list cursor, but only if mailbox is set
			// (otherwise it's "too soon" and other pathways will cause it to be
			// loaded)
			if (mLoadMessageListTask == null && mMailboxId != -1) {
				mLoadMessageListTask = new LoadMessageListTask(mMailboxId);
				mLoadMessageListTask.execute();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mController.removeResultCallback(mControllerCallback);
		closeMessageListCursor();
	}

	private void closeMessageListCursor() {
		if (mMessageListCursor != null) {
			mMessageListCursor.unregisterContentObserver(mCursorObserver);
			mMessageListCursor.close();
			mMessageListCursor = null;
		}
	}

	private void cancelAllTasks() {
		EmUtility.cancelTaskInterrupt(mLoadMessageTask);
		mLoadMessageTask = null;
		EmUtility.cancelTaskInterrupt(mLoadBodyTask);
		mLoadBodyTask = null;
		EmUtility.cancelTaskInterrupt(mLoadAttachmentsTask);
		mLoadAttachmentsTask = null;
		EmUtility.cancelTaskInterrupt(mLoadMessageListTask);
		mLoadMessageListTask = null;
		EmUtility.cancelTaskInterrupt(mPresenceCheckTask);
		mPresenceCheckTask = null;
	}

	/**
	 * We override onDestroy to make sure that the WebView gets explicitly
	 * destroyed. Otherwise it can leak native references.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		cancelAllTasks();
		// This is synchronized because the listener accesses
		// mMessageContentView from its thread
		synchronized (this) {
			mMessageContentView.destroy();
			mMessageContentView = null;
		}
		// the cursor was closed in onPause()
	}

	private void onDelete() {
		if (mMessage != null) {
			// the delete triggers mCursorObserver
			// first move to older/newer before the actual delete
			long messageIdToDelete = mMessageId;
			boolean moved = moveToOlder() || moveToNewer();
			mController.deleteMessage(messageIdToDelete, mMessage.mAccountKey);
			Toast.makeText(
					this,
					getResources().getQuantityString(
							R.plurals.message_deleted_toast, 1),
					Toast.LENGTH_SHORT).show();
			if (!moved) {
				// this generates a benign warning "Duplicate finish request"
				// because
				// repositionMessageListCursor() will fail to reposition and do
				// its own finish()
				finish();
			}
		}
	}

	/**
	 * Overrides for various WebView behaviors.
	 */
	private class CustomWebViewClient extends WebViewClient {
		/**
		 * This is intended to mirror the operation of the original (see
		 * android.webkit.CallbackProxy) with one addition of intent flags
		 * "FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET". This improves behavior when
		 * sublaunching other apps via embedded URI's.
		 * 
		 * We also use this hook to catch "mailto:" links and handle them
		 * locally.
		 */
		private boolean isSecureBrowserAvailable() {
			final PackageManager pm = getPackageManager();
			// get a list of installed apps.
			List<ApplicationInfo> packages = pm
					.getInstalledApplications(PackageManager.GET_META_DATA);

			for (ApplicationInfo packageInfo : packages) {
				// Log.v("Packageeee", "Installed package :" +
				// packageInfo.packageName);
				// Log.v("LaunchActivity", "Launch Activity :" +
				// pm.getLaunchIntentForPackage(packageInfo.packageName));
				if (packageInfo.packageName
						.equals("com.cognizant.trumobi.securebrowser"))
				// if(packageInfo.packageName.contains(("secure")))
				{
					Log.e("Packageeeeeeeeeee", " " + packageInfo.packageName);
					return true;
				}
			}
			return false;
			// the getLaunchIntentForPackage returns an intent that you can use
			// with startActivity()
		}

		private void openWebApp(String binary_source_path) {
			if (!binary_source_path.startsWith("http://")
					&& !binary_source_path.startsWith("https://")) {
				binary_source_path = "http://" + binary_source_path;
			}
			// Intent openWebAppIntent =
			// getPackageManager().getLaunchIntentForPackage("com.cognizant.trumobi.securebrowser");

			Intent openWebAppIntent = new Intent("com.cognizant.trumobi.securebrowser");
	        if (openWebAppIntent != null) {
	               openWebAppIntent.putExtra("toBrowser",binary_source_path);
	               //openWebAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	               //openWebAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
	               try {
	            	   startActivity(openWebAppIntent);
	            	   
	               }catch (ActivityNotFoundException ex) {
	                   // No applications can handle it.  Ignore.
	               }
	        } 
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// hijack mailto: uri's and handle locally
			if (url != null && url.toLowerCase().startsWith("mailto:")) {
				return EmMessageCompose.actionCompose(EmMessageView.this, url,
						mAccountId);
			}

			// Handle most uri's via intent launch
			boolean result = false;
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			intent.addCategory(Intent.CATEGORY_BROWSABLE);
			intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			try {
				// startActivity(intent);
				openWebApp(url); // NaGa, Redirect to Secure Browser
				result = true;
			} catch (ActivityNotFoundException ex) {
				// If no application can handle the URL, assume that the
				// caller can handle it.
			}
			return result;
		}
	}

	/**
	 * Handle clicks on sender, which shows {@link QuickContact} or prompts to
	 * add the sender as a contact.
	 */
	private void onClickSender() {
		// Bail early if message or sender not present
		if (mMessage == null)
			return;

		final EmAddress senderEmail = EmAddress.unpackFirst(mMessage.mFrom);
		if (senderEmail == null)
			return;

		// First perform lookup query to find existing contact
		final ContentResolver resolver = getContentResolver();
		final String address = senderEmail.getAddress();
		final Uri dataUri = Uri.withAppendedPath(
				CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(address));
		final Uri lookupUri = ContactsContract.Data.getContactLookupUri(
				resolver, dataUri);

		if (lookupUri != null) {
			// Found matching contact, trigger QuickContact
			QuickContact.showQuickContact(this, mSenderPresenceView, lookupUri,
					QuickContact.MODE_LARGE, null);
		} else {
			// No matching contact, ask user to create one
			/*final Uri mailUri = Uri.fromParts("mailto", address, null);
			final Intent intent = new Intent(
					ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, mailUri);

			// Pass along full E-mail string for possible create dialog
			intent.putExtra(ContactsContract.Intents.EXTRA_CREATE_DESCRIPTION,
					senderEmail.toString());

			// Only provide personal name hint if we have one
			final String senderPersonal = senderEmail.getPersonal();
			if (!TextUtils.isEmpty(senderPersonal)) {
				intent.putExtra(ContactsContract.Intents.Insert.NAME,
						senderPersonal);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

			startActivity(intent);*/
			
			
			final String emailId = senderEmail.getAddress();
			final String senderPersonal = senderEmail.getPersonal();
			
			if (!TextUtils.isEmpty(emailId) || !TextUtils.isEmpty(senderPersonal)) {
			
				Intent addContact = new Intent(this,ContactsAddContact.class);
		        Bundle data = new Bundle();
		        ContactsModel contact = new ContactsModel();
		        contact.setcontacts_email1_address(emailId);
		        contact.setcontacts_first_name(senderPersonal);
		        data.putSerializable("obj", contact);
		        addContact.putExtras(data);
		        //addContact.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		        startActivity(addContact);
			}
		}
	}

	/**
	 * Toggle favorite status and write back to provider
	 */
	private void onClickFavorite() {
		if (mMessage != null) {
			// Update UI
			boolean newFavorite = !mMessage.mFlagFavorite;
			mFavoriteIcon.setImageDrawable(newFavorite ? mFavoriteIconOn
					: mFavoriteIconOff);

			// Update provider
			mMessage.mFlagFavorite = newFavorite;
			mController.setMessageFavorite(mMessageId, newFavorite);
		}
	}

	private void onReply() {
		if (mMessage != null) {
			EmMessageCompose.actionReply(this, mMessage.mId, false);
			finish();
		}
	}

	private void onReplyAll() {
		if (mMessage != null) {
			EmMessageCompose.actionReply(this, mMessage.mId, true);
			finish();
		}
	}

	private void onForward() {
		if (mMessage != null) {
			EmMessageCompose.actionForward(this, mMessage.mId);
			finish();
		}
	}

	private boolean moveToOlder() {
		// Guard with !isLast() because Cursor.moveToNext() returns false even
		// as it moves
		// from last to after-last.
		if (mMessageListCursor != null && !mMessageListCursor.isLast()
				&& mMessageListCursor.moveToNext()) {
			mMessageId = mMessageListCursor.getLong(0);
			messageChanged();
			return true;
		}
		return false;
	}

	private boolean moveToNewer() {
		// Guard with !isFirst() because Cursor.moveToPrev() returns false even
		// as it moves
		// from first to before-first.
		if (mMessageListCursor != null && !mMessageListCursor.isFirst()
				&& mMessageListCursor.moveToPrevious()) {
			mMessageId = mMessageListCursor.getLong(0);
			messageChanged();
			return true;
		}
		return false;
	}

	private void onMarkAsRead(boolean isRead) {
		if (mMessage != null && mMessage.mFlagRead != isRead) {
			mMessage.mFlagRead = isRead;
			mController.setMessageRead(mMessageId, isRead);
		}
	}

	/**
	 * Creates a unique file in the given directory by appending a hyphen and a
	 * number to the given filename.
	 * 
	 * @param directory
	 * @param filename
	 * @return a new File object, or null if one could not be created
	 */
	/* package */static File createUniqueFile(File directory, String filename) {
		File file = new File(directory, filename);
		if (!file.exists()) {
			return file;
		}
		// Get the extension of the file, if any.
		int index = filename.lastIndexOf('.');
		String format;
		if (index != -1) {
			String name = filename.substring(0, index);
			String extension = filename.substring(index);
			format = name + "-%d" + extension;
		} else {
			format = filename + "-%d";
		}
		for (int i = 2; i < Integer.MAX_VALUE; i++) {
			file = new File(directory, String.format(format, i));
			if (!file.exists()) {
				return file;
			}
		}
		return null;
	}

	/**
	 * Send a service message indicating that a meeting invite button has been
	 * clicked.
	 */
	private void onRespond(int response, int toastResId) {
		// do not send twice in a row the same response
		if (mPreviousMeetingResponse != response) {
			mController.sendMeetingResponse(mMessageId, response,
					mControllerCallback);
			mPreviousMeetingResponse = response;
		}
		Toast.makeText(this, toastResId, Toast.LENGTH_SHORT).show();
		if (!moveToOlder()) {
			finish(); // if this is the last message, move up to message-list.
		}
	}

	private void onDownloadAttachment(AttachmentInfo attachment) {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			/*
			 * Abort early if there's no place to save the attachment. We don't
			 * want to spend the time downloading it and then abort.
			 */
			Toast.makeText(
					this,
					getString(R.string.message_view_status_attachment_not_saved),
					Toast.LENGTH_SHORT).show();
			return;
		}

		mLoadAttachmentId = attachment.attachmentId;
		mLoadAttachmentSave = true;
		mLoadAttachmentName = attachment.name;

		Log.i("NEW","-------> "+attachment.attachmentId+"   "+mMessageId+"   "+
				mMessage.mMailboxKey+"   "+mAccountId);
		
		
		//final String documentName = attachment.name;
		
		/*if(documentName.endsWith(".pdf") || documentName.endsWith(".txt")||documentName.endsWith(".vcf"))
		{*/
			mController.loadAttachment(attachment.attachmentId, mMessageId,
				mMessage.mMailboxKey, mAccountId, mControllerCallback);
		/*}else{
			Toast.makeText(
					this,
					getString(R.string.message_load_display_attachment_toast),
					Toast.LENGTH_SHORT).show();
			return;
		}*/
	}

	private void onViewAttachment(AttachmentInfo attachment) {
		mLoadAttachmentId = attachment.attachmentId;
		mLoadAttachmentSave = false;
		mLoadAttachmentName = attachment.name;

		
		Attachment attachInfo = Attachment.restoreAttachmentWithId(this, mLoadAttachmentId);
		final String documentName = attachment.name;

		
		String extension = "";

		int middle = documentName.lastIndexOf(".");
		extension = documentName.substring(middle + 1, documentName.length());
		try {
			/*Intent intent = info.getAttachmentIntent(mContext, mAccountId);
			startActivity(intent);*/
			//CONTAINER CHNAGES
			
			File saveIn = EmAttachmentProvider.getAttachmentDirectory(this,
					mAccountId);
			
			File newFile = new File(saveIn, Long.toString(mLoadAttachmentId));
			TruboxFileEncryption truboxFileEncryption = new TruboxFileEncryption(
					this, newFile.getAbsolutePath(), STORAGEMODE.EXTERNAL);

			Integer i = (int) attachInfo.mSize;
			String path = "";
			//Uri content = Uri.parse(attachInfo.mContentUri);
			AttachmentOpenHelper objMail;//290388
			
			if(!AttachmentListActivity.isTFNeeded){//290388
			if((extension.equalsIgnoreCase("pdf"))){
				
				
				try {
					path = UtilList.createTempFile(this.getFilesDir()
							+ File.separator + "temp.pdf", newFile.length(), i,
							truboxFileEncryption);
				} catch (Exception e) {
					e.printStackTrace();
					path="";
				}
				Intent pdfIntent = new Intent();
				pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				pdfIntent.setDataAndType(Uri.fromFile(new File(path)),
						"application/pdf");
				pdfIntent.setClass(this, OpenFileActivity.class);
				pdfIntent.setAction("android.intent.action.VIEW");
			
				File opFile = new File(path);
					if (!(opFile.length() == 0)) {
				startActivity(pdfIntent);
				} else {
							try {
								Toast.makeText(
										this,
										"Problem in loading the file,As File size is "
												+ opFile.length(),
										Toast.LENGTH_SHORT).show();
								opFile.delete();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
				
			}
			else{
				
				try {
					path = UtilList.createTempFile(getFilesDir()
							+ File.separator + "temp.txt", newFile.length(), i,
							truboxFileEncryption);
				} catch (Exception e) {
					e.printStackTrace();
					path="";
				}
				
				UtilList.fileName = mLoadAttachmentName;
				Intent pdfIntent = new Intent();
				pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				pdfIntent.setClass(this, MimeTypeTextShow.class);
				OutlookPreference.getInstance(this).setValue(
						"FilePath", path);				
				
				File opFile = new File(path);
				if (!(opFile.length() == 0)) {
					startActivity(pdfIntent);
				} else {
						try {
							Toast.makeText(
									this,
									"Problem in loading the file,As File size is "
											+ opFile.length(),
									Toast.LENGTH_SHORT).show();
							opFile.delete();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
			}
        }else{//290388
            
            try {
            	
            	mHandler.attachmentViewProgress(true);
            	path = this.getFilesDir() + File.separator +"temp"+ File.separator + UtilList.generateMessageId(mLoadAttachmentId)  /*+ ""+ documentName*/;

            	if(extension.equalsIgnoreCase("java") || extension.equalsIgnoreCase("h") || extension.equalsIgnoreCase("log")
				|| extension.equalsIgnoreCase("py") || extension.equalsIgnoreCase("m")){
            		path= path+".txt";
            		objMail = new AttachmentOpenHelper(this, newFile, path, i, 1,"txt");
            	}
				else{
					path= path+"."+extension;
					objMail = new AttachmentOpenHelper(this, newFile, path, i, 1,extension);
				}
				objMail.execute(new String[] {});
				
                  // path = UtilList.createTempFile(getFilesDir()+File.separator+mLoadAttachmentId+documentName, newFile.length(), i, truboxFileEncryption);
            } catch (Exception e) {
                   e.printStackTrace();
                   path = null;
                   mHandler.attachmentViewProgress(false);
            }
            
           /* if(path == null){
            	Toast.makeText(this, R.string.message_view_display_attachment_toast,Toast.LENGTH_SHORT).show();
                   return;
        
            }
            
           int result;
			TFDocLoader loader = TFDocLoader.createInstance(this);
			loader.setDocProperties(path);
			result = loader.launchViewer();
			if (result != TFDocLoader.SUCCESS) {
				switch (result) {
				case TFDocLoader.NOT_FOUND:
					Toast.makeText(this, "Sorry, File Not Found",Toast.LENGTH_SHORT).show();
					
					break;
				case TFDocLoader.NOT_SUPPORTED_TYPE:
					Toast.makeText(this, "Not Supported Format",Toast.LENGTH_SHORT).show();
					
					break;
				default:
					;
				}
			}*/
            
      }
			
		
			
			//CONTAINER CHANGES ENDS
			
			
			
		} catch (ActivityNotFoundException e) {
		}
		/*mController.loadAttachment(attachment.attachmentId, mMessageId,
				mMessage.mMailboxKey, mAccountId, mControllerCallback);*/
	}

	private void onShowPictures() {
		if (mMessage != null) {
			if (mMessageContentView != null) {
				mMessageContentView.getSettings().setBlockNetworkLoads(false);
				if (mHtmlTextWebView != null) {
					mMessageContentView.loadDataWithBaseURL("email://",
							mHtmlTextWebView, "text/html", "utf-8", null);
				}
			}
			mShowPicturesSection.setVisibility(View.GONE);
		}
	}

	/*private boolean isPackageAvailable(String mPackage) {
		final PackageManager pm = getPackageManager();
		// get a list of installed apps.
		List<ApplicationInfo> packages = pm
				.getInstalledApplications(PackageManager.GET_META_DATA);

		for (ApplicationInfo packageInfo : packages) {
			// Log.v("Packageeee", "Installed package :" +
			// packageInfo.packageName);
			// Log.v("LaunchActivity", "Launch Activity :" +
			// pm.getLaunchIntentForPackage(packageInfo.packageName));
			if (packageInfo.packageName.equals(mPackage))
				return true;
		}
		return false;
		// the getLaunchIntentForPackage returns an intent that you can use with
		// startActivity()
	}

	private void showAlert() {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setIcon(R.drawable.em_ic_dialog_alert);
		alertDialogBuilder.setTitle(this
				.getString(R.string.message_view_status_attachment_not_opend));
		// set dialog message
		alertDialogBuilder
				.setMessage(
						this.getString(R.string.attachment_failed_container_notfound))
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, close
						// current activity
						dialog.dismiss();
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}*/

	public void onClick(View view) {
		
		Log.i("NEW","-------< view "+view.getId());
		switch (view.getId()) {
		case R.id.from:
		case R.id.presence:
			onClickSender();
			break;
		case R.id.favorite:
			onClickFavorite();
			break;
		case R.id.reply:
			onReply();
			break;
		case R.id.reply_all:
			onReplyAll();
			break;
		case R.id.delete:
			onDelete();
			break;
		case R.id.moveToOlder:
			moveToOlder();
			break;
		case R.id.moveToNewer:
			moveToNewer();
			break;
		/*
		 * case R.id.download: //NaGa case R.id.view:
		 * 
		 * Toast.makeText(MessageView.this,
		 * getString(R.string.attachment_warning_messaage_eas_account),
		 * Toast.LENGTH_LONG).show();
		 * 
		 * break;
		 */
		case R.id.download:
			/*if (isPackageAvailable("com.cognizant.seccontainerapp")) {
				Intent i = new Intent(); // NaGa
				i.setClassName("com.cognizant.seccontainerapp.activity",
						"com.cognizant.seccontainerapp.activity.SecAppHome");
				startActivity(i);
			} else {
				AttachmentInfo att = (AttachmentInfo) view.getTag();
				Date date = new Date(mMessage.mTimeStamp);
				Log.v("DateTimeReceived : ", "" + date);
				Log.v("subject : ", "" + mMessage.mSubject);
				Log.v("FromList : ", "" + mMessage.mFrom);
				Log.v("attachment file Name : ", "" + att.name);

				// 
			}*/
			Log.i("NEW","-------< view onDownloadAttachment");
			onDownloadAttachment((AttachmentInfo) view.getTag());
			break;
		case R.id.view:
			Log.i("NEW","-------< view view");
			Attachment attachment = Attachment.restoreAttachmentWithId(this, ((AttachmentInfo) view.getTag()).attachmentId);

			if(!AttachmentListActivity.isTFNeeded){//290388
			if(attachment.mContentUri == null)
			{
				onDownloadAttachment((AttachmentInfo) view.getTag());
			}
			else
				onViewAttachment((AttachmentInfo) view.getTag());
			break;
			}else{
				
				if (attachment.mContentUri == null) {
					onDownloadAttachment((AttachmentInfo) view.getTag());
				} else
					onViewAttachment((AttachmentInfo) view.getTag());
				
			}
			break;
			/*onViewAttachment((AttachmentInfo) view.getTag());
			break;*/
		case R.id.attachment_icon: // NaGa
			Attachment attachmentT = Attachment.restoreAttachmentWithId(this,
					((AttachmentInfo) view.getTag()).attachmentId);
			if(!AttachmentListActivity.isTFNeeded){//290388
			if (attachmentT.mFileName.endsWith(".pdf")
					|| attachmentT.mFileName.endsWith(".txt")||attachmentT.mFileName.endsWith(".vcf")) {
				if (attachmentT.mContentUri == null) {
					onDownloadAttachment((AttachmentInfo) view.getTag());
					//onViewAttachment((AttachmentInfo) view.getTag());
				} else
					onViewAttachment((AttachmentInfo) view.getTag());
			} else
				Toast.makeText(
						this,
						getString(R.string.message_load_display_attachment_toast),
						Toast.LENGTH_SHORT).show();
		}else{
			
			if (attachmentT.mContentUri == null) {
				onDownloadAttachment((AttachmentInfo) view.getTag());
			} else
				onViewAttachment((AttachmentInfo) view.getTag());
			
		}
			/*if (isPackageAvailable("com.cognizant.seccontainerapp.activity")) {
				AttachmentInfo att = (AttachmentInfo) view.getTag();
				Attachment attatchment = Attachment.restoreAttachmentWithId(
						this, att.attachmentId);

				Date date = new Date(mMessage.mTimeStamp);
				SimpleDateFormat sdf = new SimpleDateFormat(
						"dd MM yyyy 'T'HH:mm:ss'Z'");
				sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
				String customDateformat = sdf.format(date);

				Bundle datatoContainer = new Bundle();

				
				 * Log.v("subject", "subjectsubject "+mMessage.mSubject);
				 * Log.v("mFrom", "mFrommFrom "+mMessage.mFrom); Log.v("to",
				 * "tototo "+mMessage.mTo); Log.v("date_time",
				 * "date_timedate_time "+customDateformat); Log.v("doc_name",
				 * "doc_namedoc_name "+att.name); Log.v("fileId",
				 * "fileIdfileId "+attatchment.mLocation);
				 

				Intent intent = new Intent("com.cognizant.secure.container"); // NaGa
				datatoContainer.putString("subject", mMessage.mSubject);
				datatoContainer.putString("from", mMessage.mFrom);
				datatoContainer.putString("to", mMessage.mTo);
				datatoContainer.putString("date_time", customDateformat);
				datatoContainer.putString("doc_name", att.name);
				datatoContainer.putString("fileId", attatchment.mLocation);

				if (supportedExtenstion(att.name)) {
					intent.putExtras(datatoContainer);
					try {
						// intent.setClassName("com.cognizant.seccontainerapp","com.cognizant.seccontainerapp.CheckAttachmentActivity");
						startActivity(intent);
					} catch (Exception e) {
						showAlert();
					}
				} else
					mHandler.attachmentViewError();
			} else {
				AttachmentInfo att = (AttachmentInfo) view.getTag();
				Date date = new Date(mMessage.mTimeStamp);
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss'Z'");
				sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
				String customDateformat = sdf.format(date);
				Log.v("mMessage.mTimeStamp : ", "" + customDateformat);
				Log.v("DateTimeReceived : ", "" + date);
				Log.v("DateTimeReceivedddddd : ", "" + mMessage.mTimeStamp);
				Log.v("subject : ", "" + mMessage.mSubject);
				Log.v("FromList : ", "" + mMessage.mDisplayName);
				Log.v("attachment file Name : ", "" + att.name);
				showAlert();
				// onViewAttachment((AttachmentInfo) view.getTag());
			}*/
			Log.i("NEW","-------< view attachment_icon");
			break;
		case R.id.show_pictures:
			onShowPictures();
			break;
		case R.id.accept:
			onRespond(EmEmailServiceConstants.MEETING_REQUEST_ACCEPTED,
					R.string.message_view_invite_toast_yes);
			break;
		case R.id.maybe:
			onRespond(EmEmailServiceConstants.MEETING_REQUEST_TENTATIVE,
					R.string.message_view_invite_toast_maybe);
			break;
		case R.id.decline:
			onRespond(EmEmailServiceConstants.MEETING_REQUEST_DECLINED,
					R.string.message_view_invite_toast_no);
			break;
		case R.id.invite_link:
			String startTime = new EmPackedString(mMessage.mMeetingInfo)
					.get(EmMeetingInfo.MEETING_DTSTART);
			if (startTime != null) {
				long epochTimeMillis = EmUtility
						.parseEmailDateTimeToMillis(startTime);
		        Intent intent = new Intent(Email.getAppContext(),CalendarMainActivity.class);
		        intent.putExtra("start_time", epochTimeMillis);		        
		        intent.putExtra("start_date", CalendarDatabaseHelper.getDateFromLong(epochTimeMillis));
		        CalendarLog.d(CalendarConstants.Tag,"get date from long "+CalendarDatabaseHelper.getDateFromLong(epochTimeMillis));
		        startActivity(intent);
			} else {
				Email.log("meetingInfo without DTSTART "
						+ mMessage.mMeetingInfo);
			}
			break;

		}
	}

	/*public boolean supportedExtenstion(String fileName) {
		if (fileName.contains(".")) {
			String ext = fileName.substring(fileName.lastIndexOf('.'),
					fileName.length());
			if (ext.equalsIgnoreCase(".txt") || ext.equalsIgnoreCase(".pdf")||ext.equalsIgnoreCase(".vcf"))
				return true;
		}
		return false;
	}*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = handleMenuItem(item.getItemId());
		if (!handled) {
			handled = super.onOptionsItemSelected(item);
		}
		return handled;
	}

	/**
	 * This is the core functionality of onOptionsItemSelected() but broken out
	 * and exposed for testing purposes (because it's annoying to mock a
	 * MenuItem).
	 * 
	 * @param menuItemId
	 *            id that was clicked
	 * @return true if handled here
	 */
	/* package */boolean handleMenuItem(int menuItemId) {
		switch (menuItemId) {
		case R.id.delete:
			onDelete();
			break;
		case R.id.reply:
			onReply();
			break;
		case R.id.reply_all:
			onReplyAll();
			break;
		case R.id.forward:
			onForward();
			break;
		case R.id.mark_as_unread:
			onMarkAsRead(false);
			finish();
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		LayoutInflater mInflater = getLayoutInflater(); // NaGa
		if (mInflater.getFactory() == null) {
			mInflater.setFactory(new EmSetcustomColor());
		}
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.em_message_view_option, menu);
		if (mDisableReplyAndForward) {
			menu.findItem(R.id.forward).setEnabled(false);
			menu.findItem(R.id.reply).setEnabled(false);
			menu.findItem(R.id.reply_all).setEnabled(false);
		}
		return true;
	}

	/**
	 * Re-init everything needed for changing message.
	 */
	private void messageChanged() {
		if (Email.DEBUG) {
			Email.log("MessageView: messageChanged to id=" + mMessageId);
		}
		cancelAllTasks();
		setTitle("");
		if (mMessageContentView != null) {
			mMessageContentView.scrollTo(0, 0);
			mMessageContentView.loadUrl("file:///android_asset/empty.html");
		}
		mScrollView.scrollTo(0, 0);
		mAttachments.removeAllViews();
		mAttachments.setVisibility(View.GONE);
		mAttachmentIcon.setVisibility(View.GONE);

		// Start an AsyncTask to make a new cursor and load the message
		mLoadMessageTask = new LoadMessageTask(mMessageId, true);
		mLoadMessageTask.execute();
		updateNavigationArrows(mMessageListCursor);
	}

	/**
	 * Reposition the older/newer cursor. Finish() the activity if we are no
	 * longer in the list. Update the UI arrows as appropriate.
	 */
	private void repositionMessageListCursor() {
		if (Email.DEBUG) {
			Email.log("MessageView: reposition to id=" + mMessageId);
		}
		// position the cursor on the current message
		mMessageListCursor.moveToPosition(-1);
		while (mMessageListCursor.moveToNext()
				&& mMessageListCursor.getLong(0) != mMessageId) {
		}
		if (mMessageListCursor.isAfterLast()) {
			// overshoot - get out now, the list is no longer valid
			finish();
		}
		updateNavigationArrows(mMessageListCursor);
	}

	/**
	 * Update the arrows based on the current position of the older/newer
	 * cursor.
	 */
	private void updateNavigationArrows(Cursor cursor) {
		if (cursor != null) {
			boolean hasNewer, hasOlder;
			if (cursor.isAfterLast() || cursor.isBeforeFirst()) {
				// The cursor not being on a message means that the current
				// message was not found.
				// While this should not happen, simply disable prev/next arrows
				// in that case.
				hasNewer = hasOlder = false;
			} else {
				hasNewer = !cursor.isFirst();
				hasOlder = !cursor.isLast();
			}
			mMoveToNewer
					.setVisibility(hasNewer ? View.VISIBLE : View.INVISIBLE);
			mMoveToOlder
					.setVisibility(hasOlder ? View.VISIBLE : View.INVISIBLE);
		}
	}

	private Bitmap getPreviewIcon(AttachmentInfo attachment) {
		try {
			Log.v("Uriiiiiiiiiiii",
					" "
							+ EmAttachmentProvider
									.getAttachmentThumbnailUri(mAccountId,
											attachment.attachmentId, 62, 62));
			return BitmapFactory.decodeStream(getContentResolver()
					.openInputStream(
							EmAttachmentProvider
									.getAttachmentThumbnailUri(mAccountId,
											attachment.attachmentId, 62, 62)));
		} catch (Exception e) {
			Log.d(Email.LOG_TAG, "Attachment preview failed with exception "
					+ e.getMessage());
			return null;
		}
	}

	/*
	 * Formats the given size as a String in bytes, kB, MB or GB with a single
	 * digit of precision. Ex: 12,315,000 = 12.3 MB
	 */
	public static String formatSize(float size) {
		long kb = 1024;
		long mb = (kb * 1024);
		long gb = (mb * 1024);
		if (size < kb) {
			return String.format("%d bytes", (int) size);
		} else if (size < mb) {
			return String.format("%.1f kB", size / kb);
		} else if (size < gb) {
			return String.format("%.1f MB", size / mb);
		} else {
			return String.format("%.1f GB", size / gb);
		}
	}

	private void updateAttachmentThumbnail(long attachmentId) {
		for (int i = 0, count = mAttachments.getChildCount(); i < count; i++) {
			AttachmentInfo attachment = (AttachmentInfo) mAttachments
					.getChildAt(i).getTag();
			
			if (attachment.attachmentId == attachmentId) {
				attachment.viewButton.setText(R.string.message_view_attachment_see_action);
				Bitmap previewIcon = getPreviewIcon(attachment);
				if (previewIcon != null) {
					mHandler.updateAttachmentIcon(i, previewIcon);
				}
				return;
			}
		}
	}

	/**
	 * Copy data from a cursor-refreshed attachment into the UI. Called from UI
	 * thread.
	 * 
	 * @param attachment
	 *            A single attachment loaded from the provider
	 */
	private void addAttachment(Attachment attachment) {

		AttachmentInfo attachmentInfo = new AttachmentInfo();
		attachmentInfo.size = attachment.mSize;
		attachmentInfo.contentType = EmAttachmentProvider.inferMimeType(
				attachment.mFileName, attachment.mMimeType);
		attachmentInfo.name = attachment.mFileName;
		attachmentInfo.attachmentId = attachment.mId;

		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.em_message_view_attachment, null);

		TextView attachmentName = (TextView) view
				.findViewById(R.id.attachment_name);
		TextView attachmentInfoView = (TextView) view
				.findViewById(R.id.attachment_info);
		ImageView attachmentIcon = (ImageView) view
				.findViewById(R.id.attachment_icon);
		Button attachmentView = (Button) view.findViewById(R.id.view);
		Button attachmentDownload = (Button) view.findViewById(R.id.download);

		if ((!EmMimeUtility.mimeTypeMatches(attachmentInfo.contentType,
				Email.ACCEPTABLE_ATTACHMENT_VIEW_TYPES))
				|| (EmMimeUtility.mimeTypeMatches(attachmentInfo.contentType,
						Email.UNACCEPTABLE_ATTACHMENT_VIEW_TYPES))) {
			attachmentView.setVisibility(View.GONE);
		}
		if ((!EmMimeUtility.mimeTypeMatches(attachmentInfo.contentType,
				Email.ACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES))
				|| (EmMimeUtility.mimeTypeMatches(attachmentInfo.contentType,
						Email.UNACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES))) {
			attachmentDownload.setVisibility(View.GONE);
		}

		if (attachmentInfo.size > Email.MAX_ATTACHMENT_DOWNLOAD_SIZE) {
			attachmentView.setVisibility(View.GONE);
			attachmentDownload.setVisibility(View.GONE);
		}

		attachmentInfo.viewButton = attachmentView;
		attachmentInfo.downloadButton = attachmentDownload;
		attachmentInfo.iconView = attachmentIcon;

		view.setTag(attachmentInfo);
		attachmentIcon.setOnClickListener(this);
		attachmentIcon.setTag(attachmentInfo);
		attachmentView.setOnClickListener(this);
		attachmentView.setTag(attachmentInfo);
		attachmentDownload.setOnClickListener(this);
		attachmentDownload.setTag(attachmentInfo);

		attachmentName.setText(attachmentInfo.name);
		attachmentInfoView.setText(formatSize(attachmentInfo.size));

		Bitmap previewIcon = getPreviewIcon(attachmentInfo);
		if (previewIcon != null) {
			attachmentIcon.setImageBitmap(previewIcon);
		}
		
		boolean isAttachmentSaved = (attachment.mContentUri != null);
		attachmentView
				.setText(isAttachmentSaved ? R.string.message_view_attachment_see_action
						: R.string.message_view_attachment_view_action);

		
		//if(attachmentInfo.viewButton.isActivated())	attachmentView.setText(R.string.message_view_attachment_view_action);
		mAttachments.addView(view);
		mAttachments.setVisibility(View.VISIBLE);
	}

	private class PresenceCheckTask extends AsyncTask<String, Void, Integer> {
		@Override
		protected Integer doInBackground(String... emails) {
			Cursor cursor = getContentResolver().query(
					ContactsContract.Data.CONTENT_URI,
					PRESENCE_STATUS_PROJECTION,
					CommonDataKinds.Email.DATA + "=?", emails, null);
			if (cursor != null) {
				try {
					if (cursor.moveToFirst()) {
						int status = cursor.getInt(0);
						int icon = StatusUpdates
								.getPresenceIconResourceId(status);
						return icon;
					}
				} finally {
					cursor.close();
				}
			}
			return 0;
		}

		@Override
		protected void onPostExecute(Integer icon) {
			if (icon == null) {
				return;
			}
			updateSenderPresence(icon);
		}
	}

	/**
	 * Launch a thread (because of cross-process DB lookup) to check presence of
	 * the sender of the message. When that thread completes, update the UI.
	 * 
	 * This must only be called when mMessage is null (it will hide presence
	 * indications) or when mMessage has already seen its headers loaded.
	 * 
	 * Note: This is just a polling operation. A more advanced solution would be
	 * to keep the cursor open and respond to presence status updates (in the
	 * form of content change notifications). However, because presence changes
	 * fairly slowly compared to the duration of viewing a single message, a
	 * simple poll at message load (and onResume) should be sufficient.
	 */
	private void startPresenceCheck() {
		if (mMessage != null) {
			EmAddress sender = EmAddress.unpackFirst(mMessage.mFrom);
			if (sender != null) {
				String email = sender.getAddress();
				if (email != null) {
					mPresenceCheckTask = new PresenceCheckTask();
					mPresenceCheckTask.execute(email);
					return;
				}
			}
		}
		updateSenderPresence(0);
	}

	/**
	 * Update the actual UI. Must be called from main thread (or handler)
	 * 
	 * @param presenceIconId
	 *            the presence of the sender, 0 for "unknown"
	 */
	private void updateSenderPresence(int presenceIconId) {
		if (presenceIconId == 0) {
			// This is a placeholder used for "unknown" presence, including
			// signed off,
			// no presence relationship.
			presenceIconId = R.drawable.em_presence_inactive;
		}
		mSenderPresenceView.setImageResource(presenceIconId);
	}

	/**
	 * This task finds out the messageId for the previous and next message in
	 * the order given by mailboxId as used in MessageList.
	 * 
	 * It generates the same cursor as the one used in MessageList (but with an
	 * id-only projection), scans through it until finds the current messageId,
	 * and takes the previous and next ids.
	 */
	private class LoadMessageListTask extends AsyncTask<Void, Void, Cursor> {
		private long mLocalMailboxId;

		public LoadMessageListTask(long mailboxId) {
			mLocalMailboxId = mailboxId;
		}

		@Override
		protected Cursor doInBackground(Void... params) {
			String selection = EmUtility.buildMailboxIdSelection(
					getContentResolver(), mLocalMailboxId);
			Cursor c = getContentResolver().query(
					EmEmailContent.Message.CONTENT_URI,
					EmEmailContent.ID_PROJECTION, selection, null,
					EmEmailContent.MessageColumns.TIMESTAMP + " DESC");
			if (isCancelled()) {
				c.close();
				c = null;
			}
			return c;
		}

		@Override
		protected void onPostExecute(Cursor cursor) {
			// remove the reference to ourselves so another one can be launched
			EmMessageView.this.mLoadMessageListTask = null;

			if (cursor == null || cursor.isClosed()) {
				return;
			}
			// replace the older cursor if there is one
			closeMessageListCursor();
			mMessageListCursor = cursor;
			mMessageListCursor
					.registerContentObserver(EmMessageView.this.mCursorObserver);
			repositionMessageListCursor();
		}
	}

	/**
	 * Async task for loading a single message outside of the UI thread Note: To
	 * support unit testing, a sentinel messageId of Long.MIN_VALUE prevents
	 * loading the message but leaves the activity open.
	 */
	private class LoadMessageTask extends AsyncTask<Void, Void, Message> {

		private long mId;
		private boolean mOkToFetch;

		/**
		 * Special constructor to cache some local info
		 */
		public LoadMessageTask(long messageId, boolean okToFetch) {
			mId = messageId;
			mOkToFetch = okToFetch;
		}

		@Override
		protected Message doInBackground(Void... params) {
			if (mId == Long.MIN_VALUE) {
				return null;
			}
			return Message.restoreMessageWithId(EmMessageView.this, mId);
		}

		@Override
		protected void onPostExecute(Message message) {
			/*
			 * doInBackground() may return null result (due to
			 * restoreMessageWithId()) and in that situation we want to
			 * Activity.finish().
			 * 
			 * OTOH we don't want to Activity.finish() for isCancelled() because
			 * this would introduce a surprise side-effect to task cancellation:
			 * every task cancelation would also result in finish().
			 * 
			 * Right now LoadMesageTask is cancelled not only from onDestroy(),
			 * and it would be a bug to also finish() the activity in that
			 * situation.
			 */
			if (isCancelled()) {
				return;
			}
			if (message == null) {
				if (mId != Long.MIN_VALUE) {
					finish();
				}
				return;
			}
			reloadUiFromMessage(message, mOkToFetch);
			startPresenceCheck();
		}
	}

	/**
	 * Async task for loading a single message body outside of the UI thread
	 */
	private class LoadBodyTask extends AsyncTask<Void, Void, String[]> {

		private long mId;

		/**
		 * Special constructor to cache some local info
		 */
		public LoadBodyTask(long messageId) {
			mId = messageId;
		}

		@Override
		protected String[] doInBackground(Void... params) {
			try {
				String text = null;
				String html = Body.restoreBodyHtmlWithMessageId(
						EmMessageView.this, mId);
				if (html == null) {
					text = Body.restoreBodyTextWithMessageId(
							EmMessageView.this, mId);
				}
				return new String[] { text, html };
			} catch (RuntimeException re) {
				// This catches SQLiteException as well as other RTE's we've
				// seen from the
				// database calls, such as IllegalStateException
				Log.d(Email.LOG_TAG, "Exception while loading message body: "
						+ re.toString());
				mHandler.loadBodyError();
				return new String[] { null, null };
			}
		}

		@Override
		protected void onPostExecute(String[] results) {
			if (results == null) {
				return;
			}
			reloadUiFromBody(results[0], results[1]); // text, html
			onMarkAsRead(true);
		}
	}

	/**
	 * Async task for loading attachments
	 * 
	 * Note: This really should only be called when the message load is complete
	 * - or, we should leave open a listener so the attachments can fill in as
	 * they are discovered. In either case, this implementation is incomplete,
	 * as it will fail to refresh properly if the message is partially loaded at
	 * this time.
	 */
	private class LoadAttachmentsTask extends
			AsyncTask<Long, Void, Attachment[]> {
		@Override
		protected Attachment[] doInBackground(Long... messageIds) {
			return Attachment.restoreAttachmentsWithMessageId(
					EmMessageView.this, messageIds[0]);
		}

		@Override
		protected void onPostExecute(Attachment[] attachments) {
			if (attachments == null) {
				return;
			}
			boolean htmlChanged = false;
			for (Attachment attachment : attachments) {
				if (mHtmlTextRaw != null && attachment.mContentId != null
						&& attachment.mContentUri != null) {
					// for html body, replace CID for inline images
					// Regexp which matches ' src="cid:contentId"'.
					String contentIdRe = "\\s+(?i)src=\"cid(?-i):\\Q"
							+ attachment.mContentId + "\\E\"";
					String srcContentUri = " src=\"" + attachment.mContentUri
							+ "\"";
					mHtmlTextRaw = mHtmlTextRaw.replaceAll(contentIdRe,
							srcContentUri);
					htmlChanged = true;
				} else {
					addAttachment(attachment);
				}
			}
			mHtmlTextWebView = mHtmlTextRaw;
			mHtmlTextRaw = null;
			if (htmlChanged && mMessageContentView != null) {
				mMessageContentView.loadDataWithBaseURL("email://",
						mHtmlTextWebView, "text/html", "utf-8", null);
			}
		}
	}

	/**
	 * Reload the UI from a provider cursor. This must only be called from the
	 * UI thread.
	 * 
	 * @param message
	 *            A copy of the message loaded from the database
	 * @param okToFetch
	 *            If true, and message is not fully loaded, it's OK to fetch
	 *            from the network. Use false to prevent looping here.
	 * 
	 *            TODO: trigger presence check
	 */
	private void reloadUiFromMessage(Message message, boolean okToFetch) {
		mMessage = message;
		mAccountId = message.mAccountKey;
		if (mMailboxId == -1) {
			mMailboxId = message.mMailboxKey;
		}
		// only start LoadMessageListTask here if it's the first time
		if (mMessageListCursor == null) {
			mLoadMessageListTask = new LoadMessageListTask(mMailboxId);
			mLoadMessageListTask.execute();
		}

		mSubjectView.setText(message.mSubject);

		titleSubject.setText(message.mSubject); // NaGa

		mFromView
				.setText(EmAddress.toFriendly(EmAddress.unpack(message.mFrom)));
		Date date = new Date(message.mTimeStamp);
		mTimeView.setText(mTimeFormat.format(date));
		mDateView.setText(EmUtility.isDateToday(date) ? null : mDateFormat
				.format(date));
		mToView.setText(EmAddress.toFriendly(EmAddress.unpack(message.mTo)));
		String friendlyCc = EmAddress.toFriendly(EmAddress.unpack(message.mCc));
		mCcView.setText(friendlyCc);
		mCcContainerView.setVisibility((friendlyCc != null) ? View.VISIBLE
				: View.GONE);
		mAttachmentIcon
				.setVisibility(message.mAttachments != null ? View.VISIBLE
						: View.GONE);
		mFavoriteIcon.setImageDrawable(message.mFlagFavorite ? mFavoriteIconOn
				: mFavoriteIconOff);
		// Show the message invite section if we're an incoming meeting
		// invitation only
		mInviteSection
				.setVisibility((message.mFlags & Message.FLAG_INCOMING_MEETING_INVITE) != 0 ? View.VISIBLE
						: View.GONE);

		// Handle partially-loaded email, as follows:
		// 1. Check value of message.mFlagLoaded
		// 2. If != LOADED, ask controller to load it
		// 3. Controller callback (after loaded) should trigger LoadBodyTask &
		// LoadAttachmentsTask
		// 4. Else start the loader tasks right away (message already loaded)
		if (okToFetch && message.mFlagLoaded != Message.FLAG_LOADED_COMPLETE) {
			mWaitForLoadMessageId = message.mId;
			mController.loadMessageForView(message.mId, mControllerCallback);
		} else {
			mWaitForLoadMessageId = -1;
			// Ask for body
			mLoadBodyTask = new LoadBodyTask(message.mId);
			mLoadBodyTask.execute();
		}
	}

	/**
	 * Reload the body from the provider cursor. This must only be called from
	 * the UI thread.
	 * 
	 * @param bodyText
	 *            text part
	 * @param bodyHtml
	 *            html part
	 * 
	 *            TODO deal with html vs text and many other issues
	 */
	private void reloadUiFromBody(String bodyText, String bodyHtml) {
		String text = null;
		mHtmlTextRaw = null;
		boolean hasImages = false;

		if (bodyHtml == null) {
			text = bodyText;
			/*
			 * Convert the plain text to HTML
			 */
			StringBuffer sb = new StringBuffer("<html><body>");
			if (text != null) {
				// Escape any inadvertent HTML in the text message
				text = EmEmailHtmlUtil.escapeCharacterToDisplay(text);
				// Find any embedded URL's and linkify
				Matcher m = Patterns.WEB_URL.matcher(text);
				while (m.find()) {
					int start = m.start();
					/*
					 * WEB_URL_PATTERN may match domain part of email address.
					 * To detect this false match, the character just before the
					 * matched string should not be '@'.
					 */
					if (start == 0 || text.charAt(start - 1) != '@') {
						String url = m.group();
						Matcher proto = WEB_URL_PROTOCOL.matcher(url);
						String link;
						if (proto.find()) {
							// This is work around to force URL protocol part be
							// lower case,
							// because WebView could follow only lower case
							// protocol link.
							link = proto.group().toLowerCase()
									+ url.substring(proto.end());
						} else {
							// Patterns.WEB_URL matches URL without protocol
							// part,
							// so added default protocol to link.
							link = "http://" + url;
						}
						String href = String.format("<a href=\"%s\">%s</a>",
								link, url);
						m.appendReplacement(sb, href);
					} else {
						m.appendReplacement(sb, "$0");
					}
				}
				m.appendTail(sb);
			}
			sb.append("</body></html>");
			text = sb.toString();
		} else {
			text = bodyHtml;
			mHtmlTextRaw = bodyHtml;
			hasImages = IMG_TAG_START_REGEX.matcher(text).find();
		}

		mShowPicturesSection
				.setVisibility(hasImages ? View.VISIBLE : View.GONE);
		if (mMessageContentView != null) {
			mMessageContentView.loadDataWithBaseURL("email://", text,
					"text/html", "utf-8", null);
		}

		// Ask for attachments after body
		mLoadAttachmentsTask = new LoadAttachmentsTask();
		mLoadAttachmentsTask.execute(mMessage.mId);
	}

	/**
	 * Controller results listener. This completely replaces MessagingListener
	 */
	private class ControllerResults implements EmEmController.Result {

		public void loadMessageForViewCallback(EmMessagingException result,
				long messageId, int progress) {
			if (messageId != EmMessageView.this.mMessageId
					|| messageId != EmMessageView.this.mWaitForLoadMessageId) {
				// We are not waiting for this message to load, so exit quickly
				return;
			}
			if (result == null) {
				switch (progress) {
				case 0:
					mHandler.progress(true);
					mHandler.loadContentUri("file:///android_asset/loading.html");
					break;
				case 100:
					mWaitForLoadMessageId = -1;
					mHandler.progress(false);
					// reload UI and reload everything else too
					// pass false to LoadMessageTask to prevent looping here
					cancelAllTasks();
					mLoadMessageTask = new LoadMessageTask(mMessageId, false);
					mLoadMessageTask.execute();
					break;
				default:
					// do nothing - we don't have a progress bar at this time
					break;
				}
			} else {
				mWaitForLoadMessageId = -1;
				mHandler.progress(false);
				mHandler.networkError();
				mHandler.loadContentUri("file:///android_asset/empty.html");
			}
		}

		public void loadAttachmentCallback(EmMessagingException result,
				long messageId, long attachmentId, int progress) {
			Log.i("NEW","Progress   "+progress);
			if (messageId == EmMessageView.this.mMessageId) {
				if (result == null) {
					switch (progress) {
					case 0:
						mHandler.setAttachmentsEnabled(false);
						mHandler.attachmentProgress(true);
						mHandler.fetchingAttachment();
						break;
					case 100:
						Log.i("NEW","-------> done ");
						mHandler.setAttachmentsEnabled(true);
						mHandler.attachmentProgress(false);
						updateAttachmentThumbnail(attachmentId);
						mHandler.finishLoadAttachment(attachmentId);
						break;
					default:
						// do nothing - we don't have a progress bar at this
						// time
						break;
					}
				} else {
					mHandler.setAttachmentsEnabled(true);
					mHandler.attachmentProgress(false);
					mHandler.networkError();
				}
			}
		}

		public void updateMailboxCallback(EmMessagingException result,
				long accountId, long mailboxId, int progress, int numNewMessages) {
			if (result != null || progress == 100) {
				Email.updateMailboxRefreshTime(mailboxId);
			}
		}

		public void updateMailboxListCallback(EmMessagingException result,
				long accountId, int progress) {
		}

		public void serviceCheckMailCallback(EmMessagingException result,
				long accountId, long mailboxId, int progress, long Tag) {
		}

		public void sendMailCallback(EmMessagingException result,
				long accountId, long messageId, int progress) {
		}
	}

	// @Override
	// public void loadMessageForViewBodyAvailable(Account account, String
	// folder,
	// String uid, com.cognizant.trumobi.em.mail.Message message) {
	// MessageView.this.mOldMessage = message;
	// try {
	// Part part = MimeUtility.findFirstPartByMimeType(mOldMessage,
	// "text/html");
	// if (part == null) {
	// part = MimeUtility.findFirstPartByMimeType(mOldMessage, "text/plain");
	// }
	// if (part != null) {
	// String text = MimeUtility.getTextFromPart(part);
	// if (part.getMimeType().equalsIgnoreCase("text/html")) {
	// text = EmailHtmlUtil.resolveInlineImage(
	// getContentResolver(), mAccount.mId, text, mOldMessage, 0);
	// } else {
	// // And also escape special character, such as "<>&",
	// // to HTML escape sequence.
	// text = EmailHtmlUtil.escapeCharacterToDisplay(text);

	// /*
	// * Linkify the plain text and convert it to HTML by replacing
	// * \r?\n with <br> and adding a html/body wrapper.
	// */
	// StringBuffer sb = new StringBuffer("<html><body>");
	// if (text != null) {
	// Matcher m = Patterns.WEB_URL.matcher(text);
	// while (m.find()) {
	// int start = m.start();
	// /*
	// * WEB_URL_PATTERN may match domain part of email address. To detect
	// * this false match, the character just before the matched string
	// * should not be '@'.
	// */
	// if (start == 0 || text.charAt(start - 1) != '@') {
	// String url = m.group();
	// Matcher proto = WEB_URL_PROTOCOL.matcher(url);
	// String link;
	// if (proto.find()) {
	// // Work around to force URL protocol part be lower case,
	// // since WebView could follow only lower case protocol link.
	// link = proto.group().toLowerCase()
	// + url.substring(proto.end());
	// } else {
	// // Patterns.WEB_URL matches URL without protocol part,
	// // so added default protocol to link.
	// link = "http://" + url;
	// }
	// String href = String.format("<a href=\"%s\">%s</a>", link, url);
	// m.appendReplacement(sb, href);
	// }
	// else {
	// m.appendReplacement(sb, "$0");
	// }
	// }
	// m.appendTail(sb);
	// }
	// sb.append("</body></html>");
	// text = sb.toString();
	// }

	// /*
	// * TODO consider how to get background images and a million other things
	// * that HTML allows.
	// */
	// // Check if text contains img Tag.
	// if (IMG_TAG_START_REGEX.matcher(text).find()) {
	// mHandler.showShowPictures(true);
	// }

	// loadMessageContentText(text);
	// }
	// else {
	// loadMessageContentUrl("file:///android_asset/empty.html");
	// }
	// // renderAttachments(mOldMessage, 0);
	// }
	// catch (Exception e) {
	// if (Email.LOGD) {
	// Log.v(Email.LOG_TAG, "loadMessageForViewBodyAvailable", e);
	// }
	// }
	// }

	/**
	 * Back in the UI thread, handle the final steps of downloading an
	 * attachment (view or save).
	 * 
	 * @param attachmentId
	 *            the attachment that was just downloaded
	 */
	private void doFinishLoadAttachment(long attachmentId) {
		// If the result does't line up, just skip it - we handle one at a time.
		if (attachmentId != mLoadAttachmentId) {
			return;
		}
		Attachment attachment = Attachment.restoreAttachmentWithId(
				EmMessageView.this, attachmentId);
		Uri attachmentUri = EmAttachmentProvider.getAttachmentUri(mAccountId,
				attachment.mId);
		Uri contentUri = EmAttachmentProvider.resolveAttachmentIdToContentUri(
				getContentResolver(), attachmentUri);

		if (mLoadAttachmentSave) {
			try {
				/*File file = createUniqueFile(
						Environment.getExternalStorageDirectory(),
						attachment.mFileName);
				InputStream in = getContentResolver().openInputStream(
						contentUri);
				OutputStream out = new FileOutputStream(file);
				IOUtils.copy(in, out);
				out.flush();
				out.close();
				in.close();

				Toast.makeText(
						EmMessageView.this,
						String.format(
								getString(R.string.message_view_status_attachment_saved),
								file.getName()), Toast.LENGTH_LONG).show();

				new MediaScannerNotifier(this, file, mHandler);*/
			} catch (Exception ioe) {
				Toast.makeText(
						EmMessageView.this,
						getString(R.string.message_view_status_attachment_not_saved),
						Toast.LENGTH_LONG).show();
			}
		} else {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(contentUri);
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
						| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				mHandler.attachmentViewError();
			}
		}
	}

	/**
	 * This notifier is created after an attachment completes downloaded. It
	 * attaches to the media scanner and waits to handle the completion of the
	 * scan. At that point it tries to start an ACTION_VIEW activity for the
	 * attachment.
	 */
	private static class MediaScannerNotifier implements
			MediaScannerConnectionClient {
		private Context mContext;
		private MediaScannerConnection mConnection;
		private File mFile;
		private MessageViewHandler mHandler;

		public MediaScannerNotifier(Context context, File file,
				MessageViewHandler handler) {
			mContext = context;
			mFile = file;
			mHandler = handler;
			mConnection = new MediaScannerConnection(context, this);
			mConnection.connect();
		}

		public void onMediaScannerConnected() {
			mConnection.scanFile(mFile.getAbsolutePath(), null);
		}

		public void onScanCompleted(String path, Uri uri) {
			try {
				if (uri != null) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(uri);
					mContext.startActivity(intent);
				}
			} catch (ActivityNotFoundException e) {
				mHandler.attachmentViewError();
				// TODO: Add a proper warning message (and lots of upstream
				// cleanup to prevent
				// it from happening) in the next release.
			} finally {
				mConnection.disconnect();
				mContext = null;
				mHandler = null;
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) { // NaGa
		// TODO Auto-generated method stub
		if (v.getId() == R.id.outlooklogo || v.getId() == R.id.goback) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:// Log.e("onTouchonTouch",
											// "ACTION_DOWNACTION_DOWN");
				backButton.getDrawable().setColorFilter(
						R.color.menu_option_color, Mode.SRC_ATOP);
				backButton.invalidate();
				titleIcon.getDrawable().setColorFilter(
						R.color.menu_option_color, Mode.SRC_ATOP);
				titleIcon.invalidate();
				break;
			case MotionEvent.ACTION_UP:// Log.e("onTouchonTouch",
										// "defaultdefaultdefaultdefault");
				backButton.getDrawable().clearColorFilter();
				backButton.invalidate();
				titleIcon.getDrawable().clearColorFilter();
				titleIcon.invalidate();
				finish();
				break;
			}
		} else if (v.getId() == R.id.connectHome) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				connectIcon.getDrawable().setColorFilter(
						R.color.menu_option_color, Mode.SRC_ATOP);
				connectIcon.invalidate();
				break;
			case MotionEvent.ACTION_UP:
				connectIcon.getDrawable().clearColorFilter();
				connectIcon.invalidate();
				finish();
				Intent i = new Intent(this, PersonaLauncher.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	this.startActivity(i);
				break;

			}
		}

		return true;
	}

	@Override
	public void onUiUpdateListner(int position,
			List<ChildMailbox> deleteAttachmnetList) {
		
	}

	@Override
	public void onTitleBookmarkedUpdate(int pos, boolean isChecked) {
		
	}

	@Override
	public void onRemoteCallback(boolean result, String path, int openType,
			Context ctx, String ext) {

		mHandler.attachmentViewProgress(false);
		if (result) {
			switch (openType) {
			case 1:
				
				openAttachment(path, ctx,ext);
				
				break;

			case 2:
					openAttachment(path, ctx,ext);
					break;

			default:
				break;

			}

		}else{
			Toast.makeText(this, R.string.message_view_display_attachment_toast,Toast.LENGTH_SHORT).show();
            return;
		}
		
	}
	
public void openAttachment(String msg, Context mContext,String ext) {//290388
		
		try{
		String filePath = null;
		int result;

		Log.i("openAttachment", "path "+msg);
		filePath = msg;
		
		if(ext.equalsIgnoreCase("vcf")){
			UtilList.openEzcard(filePath, this);
		}
		else if(ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("gif") || ext.equalsIgnoreCase("jpg")
				|| ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("bmp") || ext.equalsIgnoreCase("html") || ext.equalsIgnoreCase("htm")){
			
			shwMimeActivity(filePath,ext);
			
		}
		else{
		TFDocLoader loader = TFDocLoader.createInstance(this);
		loader.setDocProperties(filePath);
		TruMobiTimerClass.userInteractedStopTimer();
		
		result = loader.launchViewer();
		if (result != TFDocLoader.SUCCESS) {
			switch (result) {
			case TFDocLoader.NOT_FOUND:
				toast(this,"File not found");
				break;
			case TFDocLoader.NOT_SUPPORTED_TYPE:
				toast(this,"Not supported type");
				break;
			default:
				;
			}
		}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

void shwMimeActivity(String filePath,String ext){
	

	
	File opFile = new File(filePath);
	if (!(opFile.length() == 0)) {
		Intent pdfIntent = new Intent();
		pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		pdfIntent.setClass(this, MimeTypeTextShow.class);
		OutlookPreference.getInstance(this).setValue(
				"FilePath", filePath);
		OutlookPreference.getInstance(this).setValue(
				"Extension", ext);
		startActivity(pdfIntent);
	} else {
		try {
			toast(this,
					"Problem in loading the file,As File size is "
							+ opFile.length());
			opFile.delete();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	
}
	
	private void toast(Context ctx, String Msg) throws Exception {

		Toast.makeText(ctx, Msg, Toast.LENGTH_SHORT).show();

	}
}
