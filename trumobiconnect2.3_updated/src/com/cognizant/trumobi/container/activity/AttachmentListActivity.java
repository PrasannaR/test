package com.cognizant.trumobi.container.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseFragmentActivity;
import com.cognizant.trumobi.commonabstractclass.TruMobiTimerClass;
import com.cognizant.trumobi.container.Adapter.AttachmentListAdapterCal;
import com.cognizant.trumobi.container.Adapter.AttachmentListAdapterMail;
import com.cognizant.trumobi.container.AsynctaskCallback.AttachmentOpenHelper;
import com.cognizant.trumobi.container.AsynctaskCallback.SecAppFileListener;
import com.cognizant.trumobi.container.Pojo.ChildMailbox;
import com.cognizant.trumobi.container.Popup.ActionItem;
import com.cognizant.trumobi.container.Popup.QuickAction;
import com.cognizant.trumobi.container.Utils.OutlookPreference;
import com.cognizant.trumobi.container.Utils.UpdateDB;
import com.cognizant.trumobi.container.Utils.UtilList;
import com.cognizant.trumobi.container.fragments.ViewerFragment;
import com.cognizant.trumobi.container.fragments.ViewerFragmentDummy;
import com.cognizant.trumobi.container.fragments.ViewerFragmentPdf;
import com.cognizant.trumobi.em.EmEmController;
import com.cognizant.trumobi.em.mail.EmMessagingException;
import com.cognizant.trumobi.em.provider.EmAttachmentProvider;
import com.cognizant.trumobi.em.provider.EmEmailContent.Attachment;
import com.cognizant.trumobi.tfm.TFDocLoader;

import cx.hell.android.pdfview.OpenFileActivity;

public class AttachmentListActivity extends TruMobiBaseFragmentActivity
		implements SecAppFileListener, ListView.OnItemClickListener,OnClickListener {

	/*
	 * private static String mailHead = "Recently received"; private static
	 * String calHead = "Recently received";
	 */
	public static boolean isTFNeeded = true;//290388
	private ListView mailListview = null;
	private ListView calListView = null;
	private TextView mTxtDropDownItemsName = null;
	private TextView mTxtDropDownItemsType = null;// 7-10-2013
	private TruMobBaseEditText mEdTxtSubHeaderMail = null;
	private TruMobBaseEditText mEdTxtSubHeaderCal = null;
	private LinearLayout mLytListFooterMail = null;
	private Button mTxtDeleteMail = null;
	private Button mTxtCancelMail = null;
	private LinearLayout mLytListFooterCal = null;
	private Button mTxtDeleteCal = null;
	private Button mTxtCancelCal = null;
	private AttachmentListAdapterMail mailAdapter = null;
	private AttachmentListAdapterCal calAdapter = null;
	private Context mContext = null;
	private QuickAction mQuickAction;
	private Dialog dialog;
	private CharSequence mSearch = "";

	ViewerFragmentPdf viewerDisplay;

	private Dialog timerDialog;
	int _gLayid_mail = 0;

	int _gLayid_cal = 0;

	List<ChildMailbox> mailArrayList = null;
	List<ChildMailbox> calArrayList = null;
	List<ChildMailbox> searchArrayMail = new ArrayList<ChildMailbox>();
	List<ChildMailbox> searchArrayCal = new ArrayList<ChildMailbox>();

	private ViewPager myPager;
	private boolean isDeleteModeMail = false;
	private boolean isDeleteModeCal = false;

	// NEW CHANGES
	private EmEmController mController;
	private ControllerResults mControllerCallback;

	long ID;
	long Account;
	long AccId;
	long mailKey;
	long AttSize;
	private MessageViewHandler mHandler;
	// NEW CHANGES ENDS

	LinearLayout delCan = null;
	LinearLayout header = null;
	LinearLayout editHeaderMail = null;
	LinearLayout editHeaderCal = null;

	View quickHeader = null;

	ImageView deleteIcon = null;
	TextView deleteCount = null;

	CheckBox bookmarkIcon = null;// MultiBook
	CheckBox titleBookmarkIcon = null;// TitleBook
	ViewerFragmentDummy viewerDisplayDummy;

	ImageView deleteIconTitle = null;//TitleDel
	// NEW CHANGES 23ENDS
	

	private static boolean isActVisible = false;//2-12-2013
	private static long attId = 0;//2-12-2013



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i("onCreate ", ": -------------> onCreate");
		mContext = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		setContentView(R.layout.con_pager_panel);
		UtilList.dataTypeMail = detDataFromprefView();
		UtilList.dataTypeCal = detDataFromprefViewCal();
		registerReceiver(RestrictedReceiver, new IntentFilter("Restricted_App"));

		mControllerCallback = new ControllerResults();
		mController = EmEmController.getInstance(getApplication());
		// NEW CHANGES ENDS
		header = (LinearLayout) findViewById(R.id.lyt_mailitems_header);
		delCan = (LinearLayout) findViewById(R.id.con_eventtopheader);
		deleteIcon = (ImageView) findViewById(R.id.con_del_icon);
		deleteCount = (TextView) findViewById(R.id.con_del_count);
		bookmarkIcon = (CheckBox) findViewById(R.id.img_title_bookmarked_include);// MultiBook
		titleBookmarkIcon = (CheckBox) findViewById(R.id.img_title_bookmarked);//TitleBook

		deleteIconTitle = (ImageView) findViewById(R.id.con_del_title_icon);//TitleDel
		deleteCount.setTypeface(UtilList
				.getTextTypeFaceNormal(this));
		
		mTxtDropDownItemsType = (TextView) findViewById(R.id.txt_pager_name);//7-10-2013
		mTxtDropDownItemsType.setTypeface(UtilList.getTextTypeFaceNormal(this));//7-10-2013


		mTxtDropDownItemsType.setText(getResources().getString(R.string.container_sendEmail));
		
		uiRenderingEngine();
	}

	public void uiRenderingEngine() {

		if (isTablet()) {
			FrameLayout rtFrag = (FrameLayout) findViewById(R.id.alert_right_group_mail_new);
			_gLayid_mail = rtFrag.getId();
			_gLayid_cal = rtFrag.getId();

			Bundle args = new Bundle();
			args.putString("displayMsg",
					getResources().getString(R.string.container_dum_msg_normal));
			viewerDisplayDummy = ViewerFragmentDummy.newInstance(args);

			android.app.FragmentManager fragmentManager = ((Activity) mContext)
					.getFragmentManager();
			FragmentTransaction transaction = fragmentManager
					.beginTransaction();
			transaction.replace(_gLayid_mail, viewerDisplayDummy, "test");

			transaction.commitAllowingStateLoss();

		}

		mTxtDropDownItemsName = (TextView) findViewById(R.id.txt_dropdownitems_name);
		mContext = this;
		// Initialize the quick action menu items

		ActionItem itm1 = new ActionItem(1, "Recently received", 0);
		ActionItem itm2 = new ActionItem(2, "Bookmarked", 1);
		// ActionItem itm3 = new ActionItem(3, "LABEL", 1);
		ActionItem itm4 = new ActionItem(4, "Sent by", 1);
		// ActionItem itm5 = new ActionItem(5, "DOCUMENT TYPE", 1);
		mQuickAction = new QuickAction(this, QuickAction.VERTICAL);
		mQuickAction.addActionItem(itm1);
		mQuickAction.addActionItem(itm2);
		// mQuickAction.addActionItem(itm3);
		mQuickAction.addActionItem(itm4);
		// mQuickAction.addActionItem(itm5);
		mQuickAction.setOnActionItemClickListener(startQuickActionItemEvents);

		// Set font for text
		mTxtDropDownItemsName.setTypeface(UtilList.getTextTypeFaceNormal(this));
		// mTxtDropDownItemsName.setText(getDataStrFromprefView());

		ListPagerAdapter adapter = new ListPagerAdapter(this);
		myPager = (ViewPager) findViewById(R.id.pager_panel);
		myPager.setAdapter(adapter);

		Intent frmBrwse = getIntent();
		boolean isFrmBrowser = frmBrwse.getBooleanExtra("FromBrowser", false);
		if (isFrmBrowser) {
			myPager.setCurrentItem(1);
			mTxtDropDownItemsType.setText(getResources().getString(
					R.string.container_secureBrowser));
			try {
				mQuickAction.removeView();
			} catch (Exception e) {

			}
		}

		myPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int pagenum) {

				Log.i("Pager", "onPageSelected " + pagenum);
				String count;
				switch (pagenum) {
				case 0:

					/*
					 * mTxtDropDownItemsName
					 * .setText(detDataFromprefViewId(UtilList.dataTypeMail));
					 */

					try {

						ActionItem itm4 = new ActionItem(4, "Sent by", 1);
						// ActionItem itm5 = new ActionItem(5, "DOCUMENT TYPE",
						// 1);

						mQuickAction.addActionItem(itm4);

					} catch (Exception e) {

					}

					if (isDeleteModeMail) {

						count = String.valueOf(UtilList.deleteItemIDMail.size())
								+ " selected";
						deleteCount.setText(count);

						delCan.setVisibility(View.VISIBLE);
						header.setVisibility(View.GONE);
						editHeaderMail.setVisibility(View.GONE);

					} else {

						refreshFromDelte();
						delCan.setVisibility(View.GONE);
						header.setVisibility(View.VISIBLE);
						editHeaderMail.setVisibility(View.VISIBLE);
						try {

							String searchText = mEdTxtSubHeaderMail.getText()
									.toString();
							if (searchText != null
									&& !(searchText.trim().equalsIgnoreCase(""))) {
								if(!(searchArrayMail == null)){
								if( searchArrayMail.size() <= 0 ){

									toast(mContext,"No data matches your search result");

								}
								}else{



									toast(mContext,"No data matches your search result");
								}
							} else {
								if(!(mailArrayList == null)){
								if( mailArrayList.size() <= 0 ){
									toast(mContext,"No Email attachments in last 3 days");
								}
								}else{
									toast(mContext,"No Email attachments in last 3 days");
								}
							}
							searchText = null;
							
						}catch(Exception e){
							Log.e("Exception ", "Email "+e.toString());
							try {
								toast(mContext,"No Email attachments in last 3 days");
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						
					}
					
					break;

				case 1:

					/*
					 * mTxtDropDownItemsName
					 * .setText(detDataFromprefViewId(UtilList.dataTypeCal));
					 */

					try {
						mQuickAction.removeView();
					} catch (Exception e) {

					}

					if (isDeleteModeCal) {

						count = String.valueOf(UtilList.deleteItemIDCal.size())
								+ " selected";
						deleteCount.setText(count);

						delCan.setVisibility(View.VISIBLE);
						header.setVisibility(View.GONE);
						editHeaderCal.setVisibility(View.GONE);

					} else {

						refreshFromDelte();
						delCan.setVisibility(View.GONE);
						header.setVisibility(View.VISIBLE);
						editHeaderCal.setVisibility(View.VISIBLE);

						try{
							
							String searchText = mEdTxtSubHeaderCal.getText().toString();



							if (searchText != null && !(searchText.trim().equalsIgnoreCase(""))) {
								if(!(searchArrayCal == null)){
								if( (searchArrayCal != null) && searchArrayCal.size() <= 0 ){
									
									toast(mContext,"No data matches your search result");

									
								}
								}else{
									toast(mContext,"No data matches your search result");
								}
							} else {
								if(!(searchArrayCal == null)){
								if( (calArrayList != null) && calArrayList.size() <= 0 ){

									toast(mContext,"No browser attachments  in last 3 days");
								}
								}else{
									toast(mContext,"No browser attachments  in last 3 days");

								}
							}
							searchText = null;
							
						}catch(Exception e){
							
							Log.e("Exception ", "Browser "+e.toString());
							try {
								toast(mContext,"No browser attachments  in last 3 days");
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
						}
						
					}
					
					break;
				default:
					break;
				}

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

	}

	private void runOnuiThreadMail() {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				/*mTxtDropDownItemsName.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));*/
				mTxtDropDownItemsName.setText(detViewId(UtilList.dataTypeMail));

				mTxtDropDownItemsType.setText(getResources().getString(
						R.string.container_sendEmail));

			}
		});
	}

	private void runOnuiThreadCal() {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				/*mTxtDropDownItemsName.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));*/
				mTxtDropDownItemsName.setText(detViewId(UtilList.dataTypeCal));

				mTxtDropDownItemsType.setText(getResources().getString(
						R.string.container_secureBrowser));

			}
		});
	}

	private void toast(Context ctx, String Msg) throws Exception {

		Toast.makeText(ctx, Msg, Toast.LENGTH_SHORT).show();

	}

	// ListPageAdapter for swipe functionality
	public class ListPagerAdapter extends PagerAdapter implements
			OnLoadCompleteListener<AttachmentListActivity> {
		Context ctx;

		public ListPagerAdapter(Object ctx) {
			this.ctx = (Context) ctx;

		}

		@Override
		public void finishUpdate(ViewGroup container) {
			super.finishUpdate(container);
			//Log.i("Header", "finishUpdate " + myPager.getCurrentItem());

			switch (myPager.getCurrentItem()) {
			case 0:
				// mTxtDropDownItemsName.setVisibility(View.VISIBLE);
				/*
				 * mTxtDropDownItemsName
				 * .setText(detViewId(UtilList.dataTypeMail));
				 */
				runOnuiThreadMail();
				// mTxtDropDownItemsCal.setVisibility(View.GONE);
				break;

			case 1:
				// mTxtDropDownItemsCal.setVisibility(View.VISIBLE);
				/*
				 * mTxtDropDownItemsName
				 * .setText(detViewId(UtilList.dataTypeCal));
				 */
				runOnuiThreadCal();
				// mTxtDropDownItemsName.setVisibility(View.GONE);
				break;

			default:
				break;
			}

		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {

			Log.i("Pager ", ": -------------> ListPagerAdapter");
			LayoutInflater inflater = (LayoutInflater) container.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = null;
			if (position == 0) {
				view = inflater.inflate(R.layout.con_attachment_list, null);
				mailListview = (ListView) view.findViewById(R.id.listviewMail);
				((ViewPager) container).addView(view, 0);

				editHeaderMail = (LinearLayout) findViewById(R.id.lyt_mailitems_subheader);
				/*
				 * mEdTxtSubHeaderMail = (EditText) view
				 * .findViewById(R.id.edtxt_subheader_name_att);
				 */
				mEdTxtSubHeaderMail = (TruMobBaseEditText) view
						.findViewById(R.id.edtxt_subheader_name_att);
				mLytListFooterMail = (LinearLayout) view
						.findViewById(R.id.lyt_mailitems_footer_mail);
				mTxtDeleteMail = (Button) view
						.findViewById(R.id.btn_delete_mail);
				mTxtCancelMail = (Button) view
						.findViewById(R.id.btn_cancel_mail);
				mTxtDeleteMail.setTypeface(UtilList
						.getTextTypeFaceNormal(mContext));
				mTxtCancelMail.setTypeface(UtilList
						.getTextTypeFaceNormal(mContext));

				mEdTxtSubHeaderMail.setOnClickListener((OnClickListener) ctx);
				mailgetListData(UtilList.dataTypeMail);

				/*
				 * if (getResources().getString(R.string.container_app_mode)
				 * .equalsIgnoreCase("seveninch") || getResources()
				 * .getString(R.string.container_app_mode)
				 * .equalsIgnoreCase("teninch")) { FrameLayout rtFrag =
				 * (FrameLayout) view
				 * .findViewById(R.id.alert_right_group_mail); _gLayid_mail =
				 * rtFrag.getId(); Configuration config =
				 * getResources().getConfiguration(); if (config.orientation ==
				 * Configuration.ORIENTATION_LANDSCAPE) {
				 * onDisplayAlertListFragment(_gLayid_mail); } }
				 */

				// Set listener for edit text mail attach
				mEdTxtSubHeaderMail.addTextChangedListener(new TextWatcher() {

					public void afterTextChanged(Editable s) {

						try {

							isDeleteModeMail = false;
							// mLytListFooterMail.setVisibility(View.GONE);
							// UtilList.deleteItemIDMail.clear();

						} catch (Exception e) {

						}

						if (mailArrayList != null) {
							if (searchArrayMail != null
									&& searchArrayMail.size() > 0) {

								displaySearchList(searchArrayMail);
								mailAdapter = new AttachmentListAdapterMail(
										mContext, searchArrayMail);
								mailListview.invalidate();
								mailAdapter.notifyDataSetChanged();
								mailListview.setAdapter(mailAdapter);
							}

							else if (s.length() > 0) {
								Toast.makeText(mContext,
										"No data matches your search result",
										Toast.LENGTH_SHORT).show();
							}
						}
					}

					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					public void onTextChanged(CharSequence searchText,
							int start, int before, int count) {

						if (mailArrayList != null) {
							mSearch = searchText;
							getSearchList(searchText, mailArrayList);
						} else {
							Toast.makeText(getBaseContext(),
									"No data to search", Toast.LENGTH_SHORT)
									.show();
						}
					}
				});

				// Listener to trigger the detailed mail view on click of each
				// mail
				/*
				 * mailListview .setOnItemClickListener(new
				 * ListView.OnItemClickListener() {
				 * 
				 * @Override public void onItemClick(AdapterView<?> arg0, View
				 * arg1, int position, long arg3) {
				 * 
				 * if (isDeleteModeMail) {
				 * 
				 * try {
				 * 
				 * String searchText = mEdTxtSubHeaderMail
				 * .getText().toString();
				 * 
				 * if (searchText != null && !(searchText.trim()
				 * .equalsIgnoreCase(""))) {
				 * 
				 * onUiUpdateListner(position, searchArrayMail);
				 * 
				 * } else {
				 * 
				 * onUiUpdateListner(position, mailArrayList);
				 * 
				 * } } catch (Exception e) {
				 * 
				 * onUiUpdateListner(position, mailArrayList);
				 * 
				 * }
				 * 
				 * } else { // mailAdapter.toggleSelectedAttachmnets(position);
				 * // mailListview.invalidate(); //
				 * mailAdapter.notifyDataSetChanged(); try {
				 * 
				 * String searchText = mEdTxtSubHeaderMail
				 * .getText().toString();
				 * 
				 * if (searchText != null && !(searchText.trim()
				 * .equalsIgnoreCase(""))) {
				 * 
				 * getAttachmentDeatil(position, searchArrayMail);
				 * 
				 * } else {
				 * 
				 * getAttachmentDeatil(position, mailArrayList);
				 * 
				 * } } catch (Exception e) {
				 * 
				 * getAttachmentDeatil(position, mailArrayList);
				 * 
				 * } } } });
				 */
				mailListview.setOnItemClickListener((OnItemClickListener) ctx);
				/*
				 * mailListview .setOnItemLongClickListener(new
				 * OnItemLongClickListener() {
				 * 
				 * @Override public boolean onItemLongClick(AdapterView<?> arg0,
				 * View view, int position, long arg3) { if (!isDeleteModeMail)
				 * { isDeleteModeMail = true;
				 * 
				 * mLytListFooterMail .setVisibility(View.VISIBLE);
				 * 
				 * } return true; } });
				 */

			} else {
				view = inflater.inflate(R.layout.con_calender_list, null);
				calListView = (ListView) view.findViewById(R.id.listviewCal);
				((ViewPager) container).addView(view, 0);

				/*
				 * int datatype = detDataFromprefView();
				 * calgetListData(datatype);
				 */
				// Commented Nw

				editHeaderCal = (LinearLayout) findViewById(R.id.lyt_mailitems_subheader);

				mLytListFooterCal = (LinearLayout) view
						.findViewById(R.id.lyt_mailitems_footer_cal);
				mTxtDeleteCal = (Button) view.findViewById(R.id.btn_delete_cal);
				mTxtCancelCal = (Button) view.findViewById(R.id.btn_cancel_cal);
				mTxtDeleteCal.setTypeface(UtilList
						.getTextTypeFaceNormal(mContext));
				mTxtCancelCal.setTypeface(UtilList
						.getTextTypeFaceNormal(mContext));
				mEdTxtSubHeaderCal = (TruMobBaseEditText) view
						.findViewById(R.id.edtxt_subheader_name_cal);

				mEdTxtSubHeaderCal.setOnClickListener((OnClickListener) ctx);
				calgetListData(UtilList.dataTypeCal);

				/*
				 * if (getResources().getString(R.string.container_app_mode)
				 * .equalsIgnoreCase("seveninch") || getResources()
				 * .getString(R.string.container_app_mode)
				 * .equalsIgnoreCase("teninch")) { FrameLayout rtFrag =
				 * (FrameLayout) view .findViewById(R.id.alert_right_group_cal);
				 * _gLayid_cal = rtFrag.getId(); Configuration config =
				 * getResources().getConfiguration(); if (config.orientation ==
				 * Configuration.ORIENTATION_LANDSCAPE) {
				 * onDisplayAlertListFragment(_gLayid_cal); } }
				 */

				// Set listener for edit text calender Attach
				mEdTxtSubHeaderCal.addTextChangedListener(new TextWatcher() {

					public void afterTextChanged(Editable s) {

						try {

							isDeleteModeCal = false;
							// mLytListFooterMail.setVisibility(View.GONE);
							// UtilList.deleteItemIDMail.clear();

						} catch (Exception e) {

						}

						if (calArrayList != null) {
							if (searchArrayCal != null
									&& searchArrayCal.size() > 0) {

								displaySearchList(calArrayList);
								calAdapter = new AttachmentListAdapterCal(
										mContext, searchArrayCal);
								calListView.invalidate();
								calAdapter.notifyDataSetChanged();
								calListView.setAdapter(calAdapter);

							}

							else if (s.length() > 0) {
								Toast.makeText(mContext,
										"No data matches your search result",
										Toast.LENGTH_SHORT).show();
							}
						}
					}

					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					public void onTextChanged(CharSequence searchText,
							int start, int before, int count) {

						if (calArrayList != null) {
							mSearch = searchText;
							getSearchList(searchText, calArrayList);
						} else {
							Toast.makeText(getBaseContext(),
									"No data to search", Toast.LENGTH_SHORT)
									.show();
						}
						// getSearchList(searchText, calArrayList);
					}

				});

				// Listener to trigger the detailed mail view on click of each
				// mail
				calListView.setOnItemClickListener((OnItemClickListener) ctx);
				/*
				 * calListView .setOnItemClickListener(new
				 * ListView.OnItemClickListener() {
				 * 
				 * @Override public void onItemClick(AdapterView<?> arg0, View
				 * arg1, int position, long arg3) { if (isDeleteModeCal) {
				 * //deleteSelectedMode(position, calArrayList); try {
				 * 
				 * String searchText = mEdTxtSubHeaderCal .getText().toString();
				 * 
				 * if (searchText != null && !(searchText.trim()
				 * .equalsIgnoreCase(""))) {
				 * 
				 * onUiUpdateListner(position, searchArrayCal);
				 * 
				 * } else {
				 * 
				 * onUiUpdateListner(position, calArrayList);
				 * 
				 * } } catch (Exception e) {
				 * 
				 * onUiUpdateListner(position, calArrayList);
				 * 
				 * } } else { //getAttachmentDeatil(position, calArrayList); try
				 * {
				 * 
				 * String searchText = mEdTxtSubHeaderCal .getText().toString();
				 * 
				 * if (searchText != null && !(searchText.trim()
				 * .equalsIgnoreCase(""))) {
				 * 
				 * getAttachmentDeatilSB(position, searchArrayCal);
				 * 
				 * } else {
				 * 
				 * getAttachmentDeatilSB(position, calArrayList);
				 * 
				 * } } catch (Exception e) {
				 * 
				 * getAttachmentDeatilSB(position, calArrayList);
				 * 
				 * } } } }); calListView .setOnItemLongClickListener(new
				 * OnItemLongClickListener() {
				 * 
				 * @Override public boolean onItemLongClick(AdapterView<?> arg0,
				 * View view, int position, long arg3) { if (!isDeleteModeCal) {
				 * isDeleteModeCal = true; } return true; } });
				 */

			}
			return view;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == ((View) arg1);
		}

		@Override
		public void destroyItem(View collection, int position, Object o) {
			View view = (View) o;
			((ViewPager) collection).removeView(view);
			// view.remove(position);
			view = null;
		}

		@Override
		public void onLoadComplete(Loader<AttachmentListActivity> arg0,
				AttachmentListActivity arg1) {

		}

	}

	/*
	 * @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	 * 
	 * @SuppressLint("NewApi") private void onDisplayAlertListFragment(int
	 * _Layid) { // Log.w(TAG, "onDisplayAlertListFragment"); String searchText
	 * = ""; try { if (mSearch != "") searchText = mSearch.toString(); } catch
	 * (Exception e) { searchText = ""; }
	 * 
	 * Log.e("NEW", "-===----=== " + (searchText.trim().equalsIgnoreCase("")));
	 * 
	 * switch (myPager.getCurrentItem()) { case 0:
	 * 
	 * if ((searchText.trim().equalsIgnoreCase(""))) { if (mailArrayList != null
	 * && mailArrayList.size() != 0) getAttachmentDeatil(0, mailArrayList); }
	 * else {
	 * 
	 * if (searchArrayMail != null && searchArrayMail.size() != 0)
	 * getAttachmentDeatil(0, searchArrayMail);
	 * 
	 * }
	 * 
	 * break;
	 * 
	 * case 1:
	 * 
	 * if ((searchText.trim().equalsIgnoreCase(""))) { if (calArrayList != null
	 * && calArrayList.size() != 0) getAttachmentDeatilSB(0, calArrayList); }
	 * else {
	 * 
	 * if (searchArrayCal != null && searchArrayCal.size() != 0)
	 * getAttachmentDeatilSB(0, searchArrayCal);
	 * 
	 * }
	 * 
	 * break; default: break; }
	 * 
	 * }
	 */

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);
		// Log.d("NEW",
		// "onConfigurationChanged list acti "+mQuickAction.isShowing());

		if (mQuickAction.isShowing()) {

			mQuickAction.dismiss();
			/*
			 * quickHeader = (View) findViewById(R.id.txt_pager);//7-10-2013
			 * mQuickAction.show(quickHeader);
			 */
		}

		// Configuration config = getResources().getConfiguration();//Commented

		if (timerDialog != null)
			timerDialog.dismiss();

		/*
		 * if (getResources().getString(R.string.container_app_mode)
		 * .equalsIgnoreCase("seveninch") || getResources()
		 * .getString(R.string.container_app_mode) .equalsIgnoreCase("teninch"))
		 * {
		 * 
		 * uiRenderingEngine(); }
		 */// Commented

		switch (myPager.getCurrentItem()) {
		case 0:

			if (mSearch != "") {
				Log.d("NEW", "---======--- " + mSearch + " aaajjj");
				mEdTxtSubHeaderMail.setText(mSearch);
			}

			break;

		case 1:

			if (mSearch != "") {
				Log.d("NEW", "---======--- " + mSearch + " aaajjj");
				mEdTxtSubHeaderCal.setText(mSearch);
			}
			break;
		default:
			break;
		}

	}

	// select attachment for delete
	private void deleteSelectedMode(int position,
			List<ChildMailbox> deleteAttachmnetList) {
		if (myPager.getCurrentItem() == 0) {
			if (UtilList.deleteItemIDMail.containsKey(Integer
					.toString(position))) {

				if (UtilList.bookmarkItemIDMail.contains(Integer
						.toString(position)))
					mailAdapter.mBookmarkedStatus.set(position, false);// MultiBook
																		// 12-10

				mailAdapter.toggleSelected(position, 0);
				mailAdapter.notifyDataSetChanged();
				UtilList.deleteItemIDMail.remove(Integer.toString(position));
			} else {

				if (bookmarkIcon.isChecked()) {
					bookmarkIcon.toggle();
				}
				mailAdapter.toggleSelected(position, 1);
				mailAdapter.notifyDataSetChanged();
				UtilList.deleteItemIDMail.put(Integer.toString(position),
						deleteAttachmnetList.get(position).getAttachmentId()
								.toString().trim());
			}

			if (!(UtilList.deleteItemIDMail.size() > 0)) {

				bookmarkIcon.setChecked(false);
				isDeleteModeMail = false;
				refreshFromDelte();
				delCan.setVisibility(View.GONE);
				header.setVisibility(View.VISIBLE);
				editHeaderMail.setVisibility(View.VISIBLE);

			}

		} else {

			/*
			 * Log.i("SecureBrowser",
			 * "else in deleteSelectedMode  "+(UtilList.deleteItemIDCal
			 * .containsKey(Integer.toString(position))));
			 */

			if (UtilList.deleteItemIDCal
					.containsKey(Integer.toString(position))) {

				if (UtilList.bookmarkItemIDCal.contains(Integer
						.toString(position)))
					calAdapter.mBookmarkedStatus.set(position, false);// MultiBook
																		// 12-10

				calAdapter.toggleSelected(position, 0);
				calAdapter.notifyDataSetChanged();
				UtilList.deleteItemIDCal.remove(Integer.toString(position));
			} else {
				if (bookmarkIcon.isChecked()) {
					bookmarkIcon.toggle();
				}
				calAdapter.toggleSelected(position, 1);
				calAdapter.notifyDataSetChanged();
				UtilList.deleteItemIDCal.put(Integer.toString(position),
						deleteAttachmnetList.get(position).getAttachmentId()
								.toString().trim());
			}

			if (!(UtilList.deleteItemIDCal.size() > 0)) {

				bookmarkIcon.setChecked(false);
				isDeleteModeCal = false;
				refreshFromDelte();
				delCan.setVisibility(View.GONE);
				header.setVisibility(View.VISIBLE);
				editHeaderCal.setVisibility(View.VISIBLE);

			}
		}

	}

	public int detDataFromprefView() {
		int _id = 1;

		/*
		 * public static final int RECENT_ADDED = 1; public static final int
		 * BOOKMARK = 2; public static final int LABEL = 3; public static final
		 * int SENTBY = 4; public static final int DOCTYPE = 5; public static
		 * final int SEARCH = 6;
		 */
		String str = OutlookPreference.getInstance(this).getValue(
				getResources().getString(R.string.container_default_view),
				"Recently received");
		Log.i("", "det :------>     " + str);

		if (str.equalsIgnoreCase("Recently received")) {
			_id = 1;
		} else if (str.equalsIgnoreCase("Bookmarked")) {
			_id = 2;
		} else if (str.equalsIgnoreCase("Sent by")) {
			_id = 4;
		}
		return _id;
	}

	public int detDataFromprefViewCal() {
		int _id = 1;

		/*
		 * public static final int RECENT_ADDED = 1; public static final int
		 * BOOKMARK = 2; public static final int LABEL = 3; public static final
		 * int SENTBY = 4; public static final int DOCTYPE = 5; public static
		 * final int SEARCH = 6;
		 */
		String str = OutlookPreference.getInstance(this).getValue(
				getResources().getString(R.string.container_default_view),
				"Recently received");
		Log.i("", "det :------>     " + str);

		if (str.equalsIgnoreCase("Recently received")) {
			_id = 1;
		} else if (str.equalsIgnoreCase("Bookmarked")) {
			_id = 2;
		} else if (str.equalsIgnoreCase("Sent by")) {
			_id = 4;
		}

		switch (_id) {
		case UtilList.SENTBY:
			return 1;

		default:
			return _id;

		}

	}

	public String detViewId(int _id) {
		String strRet = "Recently received";

		if (_id == 1) {
			strRet = "Recently received";
		} else if (_id == 2) {
			strRet = "Bookmarked";
		} else if ((_id == 4)) {
			strRet = "Sent by";
		}
		return strRet;
	}

	// Quick Action items event listener
	private QuickAction.OnActionItemClickListener startQuickActionItemEvents = new QuickAction.OnActionItemClickListener() {

		@Override
		public void onItemClick(QuickAction source, int pos, int actionId) {

			/*
			 * mSearch = ""; mEdTxtSubHeaderMail.setText(mSearch);
			 */
			isDeleteModeMail = false;
			/*
			 * mLytListFooterMail.setVisibility(View.GONE); try {
			 * UtilList.deleteItemIDMail.clear(); } catch (Exception e) {
			 * 
			 * } // refreshDeleteToNormal(mailArrayList);
			 */

			switch (myPager.getCurrentItem()) {
			case 0:
				if (!(UtilList.dataTypeMail == actionId)) {
					mailAdapter.clickId = -1;
					mailAdapter.notifyDataSetChanged();
				}

				break;

			default:
				if (!(UtilList.dataTypeCal == actionId)) {
					calAdapter.clickId = -1;
					calAdapter.notifyDataSetChanged();
				}
				break;
			}

			try {

				if (viewerDisplayDummy == null && isTablet()) {

					Bundle args = new Bundle();
					args.putString(
							"displayMsg",
							getResources().getString(
									R.string.container_dum_msg_normal));
					viewerDisplayDummy = ViewerFragmentDummy.newInstance(args);

					android.app.FragmentManager fragmentManager = ((Activity) mContext)
							.getFragmentManager();
					FragmentTransaction transaction = fragmentManager
							.beginTransaction();
					transaction.replace(_gLayid_mail, viewerDisplayDummy,
							"test");

					transaction.commitAllowingStateLoss();
				}

				titleBookmarkIcon.setChecked(false);

			} catch (Exception e) {

			}

			switch (actionId) {
			case UtilList.RECENT_ADDED: {

				if (myPager.getCurrentItem() == 0) {
					UtilList.dataTypeMail = 1;
					mailgetListData(UtilList.dataTypeMail);
				} else {
					UtilList.dataTypeCal = 1;
					calgetListData(UtilList.dataTypeCal);
				}
			}
				break;
			case UtilList.BOOKMARK: {

				if (myPager.getCurrentItem() == 0) {
					UtilList.dataTypeMail = 2;
					mailgetListData(UtilList.dataTypeMail);
				} else {
					UtilList.dataTypeCal = 2;
					calgetListData(UtilList.dataTypeCal);
				}
				/*if (myPager.getCurrentItem() == 0) {

					Log.i("SUDAR",
							"======> if email "
									+ UtilList.bookmarkedAttachmentItemIDMail
											.size()
									+ "   "
									+ UtilList.bookmarkedAttachmentItemIDValuesMail
											.size()
									+ "   "
									+ UtilList.bookmarkedAttachmentItemIDMail
											.keySet().size()
									+ "   "
									+ UtilList.bookmarkedAttachmentItemIDValuesMail
											.keySet().size());
					if (UtilList.bookmarkedAttachmentItemIDMail.size() > 0) {

						UtilList.dataTypeMail = 2;
						mailgetListData(UtilList.dataTypeMail);

					} else {

						Log.i("SUDAR", "======> else in email");
						Toast.makeText(mContext,
								"No items are bookmarked to display",
								Toast.LENGTH_LONG).show();
					}
				} else {

					if (UtilList.bookmarkedAttachmentItemIDCal.size() > 0) {

						UtilList.dataTypeCal = 2;
						calgetListData(UtilList.dataTypeCal);

					} else {

						Log.i("SUDAR", "======> else in email");
						Toast.makeText(mContext,
								"No items are bookmarked to display",
								Toast.LENGTH_LONG).show();
					}

					if (UtilList.bookmarkedAttachmentItemIDValuesCal.keySet()
							.size() > 0) {
						UtilList.dataTypeCal = 2;
						calgetListData(UtilList.dataTypeCal);

					} else {

						Log.i("SUDAR", "======> else in cal");
						Toast.makeText(mContext,
								"No items are bookmarked to display",


								Toast.LENGTH_LONG).show();
					}
				}*/
			}
				break;

			case UtilList.SENTBY: {

				if (myPager.getCurrentItem() == 0) {
					UtilList.dataTypeMail = 4;
					mailgetListData(UtilList.dataTypeMail);
				} else {
					UtilList.dataTypeCal = 4;
					calgetListData(UtilList.dataTypeCal);
				}
			}
				break;
			}
			/*
			 * if (getResources().getString(R.string.container_app_mode)
			 * 
			 * .equalsIgnoreCase("seveninch") ||
			 * getResources().getString(R.string.container_app_mode)
			 * .equalsIgnoreCase("teninch")) { Configuration config =
			 * getResources().getConfiguration(); if (config.orientation ==
			 * Configuration.ORIENTATION_LANDSCAPE) {
			 * 
			 * switch (myPager.getCurrentItem()) { case 0:
			 * onDisplayAlertListFragment(_gLayid_mail); break;
			 * 
			 * case 1: onDisplayAlertListFragment(_gLayid_cal); break; default:
			 * break; }
			 * 
			 * } }
			 */// Commented
		}
	};

	private void mailgetListData(int datatype) {
		Log.e("NEW", "mailgetListData ");
		mailArrayList = null;
		switch (datatype) {
		case UtilList.RECENT_ADDED:
			// mTxtDropDownItemsName.setText("Recently received");
			// mailHead = "Recently received";
			UpdateDB.bookmarkAttachments(AttachmentListActivity.this,
					UtilList.bookmarkedAttachmentItemIDValuesMail);
			mailArrayList = UpdateDB.getListDetails(this);
			break;
		case UtilList.BOOKMARK:
			// mTxtDropDownItemsName.setText("Bookmarked");
			// mailHead = "Bookmarked";
			UpdateDB.bookmarkAttachments(AttachmentListActivity.this,
					UtilList.bookmarkedAttachmentItemIDValuesMail);
			mailArrayList = UpdateDB.getBookmarkedListDetails(mContext);
			break;
		case UtilList.SENTBY:

			/*
			 * runOnUiThread(new Runnable() {
			 * 
			 * @Override public void run() {
			 * mTxtDropDownItemsName.setText("Sent by"); } });
			 */
			// mTxtDropDownItemsName.setText("Sent by");

			// mailHead = "Sent by";
			UpdateDB.bookmarkAttachments(AttachmentListActivity.this,
					UtilList.bookmarkedAttachmentItemIDValuesMail);
			mailArrayList = UpdateDB.getListSentDetails(this);
			break;
		}

		if (mailArrayList != null) {
			for (int i = 0; i < mailArrayList.size(); i++) {
				performDateTimeSplit(i, mailArrayList);
			}

			UtilList.makeHeaderList(mailArrayList, datatype);

			// 27-8-2013
			String searchText = "";
			try {

				searchText = mEdTxtSubHeaderMail.getText().toString();
				Log.i("TRY", "in mail new list " + searchText + "   uhsh");
			} catch (Exception e) {
				Log.i("Exception", "in mail new list");
				searchText = "";
			}
			if (!(searchText.trim().equalsIgnoreCase(""))) {
				getSearchList(searchText, mailArrayList);
				displaySearchList(searchArrayMail);
				mailAdapter = new AttachmentListAdapterMail(mContext,
						searchArrayMail);
			} else
				mailAdapter = new AttachmentListAdapterMail(mContext,
						mailArrayList);
			// 27-8-2013

			mailListview.setAdapter(mailAdapter);
		} else {

			try{
			
			if(myPager.getCurrentItem() == 0){
			
			if (datatype == UtilList.BOOKMARK)
				Toast.makeText(mContext,
						"No email attachments are bookmarked in last 3 days",
						Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(mContext, "No email attachments in last 3 days",
						Toast.LENGTH_SHORT).show();
				
			}
			}catch(Exception e){
				
			}
			ArrayList<ChildMailbox> dummysize = new ArrayList<ChildMailbox>();
			mailAdapter = new AttachmentListAdapterMail(mContext, dummysize);
			mailListview.setAdapter(mailAdapter);
			mailAdapter.notifyDataSetChanged();
		}
		/*
		 * if (dialog != null) { dialog.dismiss(); }
		 */
	}

	private void calgetListData(int datatype) {
		calArrayList = null;

		/*
		 * List<ChildMailbox> data = new ArrayList<ChildMailbox>(); for(int
		 * i=0;i<3;i++){
		 * 
		 * ChildMailbox datum = new ChildMailbox();
		 * datum.setAttachmentName("Attachment"+i+".txt");
		 * datum.setMIME_TYPE("txt"); datum.setSize("20000");
		 * datum.setContent(Environment.getExternalStorageDirectory()+"/a.txt");
		 * datum.setDateTimeReceived("1379310360");
		 * datum.setEmailAddress("http://www.google.com"); data.add(datum);
		 * 
		 * }
		 * 
		 * UpdateDB.storeInboxMessages(this, data);
		 */
		switch (datatype) {
		case UtilList.SENTBY:
		case UtilList.RECENT_ADDED:
			// mTxtDropDownItemsName.setText("Recently received");
			// calHead = "Recently received";
			UpdateDB.bookmarkAttachmentsSecureBrowser(
					AttachmentListActivity.this,
					UtilList.bookmarkedAttachmentItemIDValuesCal);
			calArrayList = UpdateDB.getListBrowserDetails(this, 1);
			break;
		case UtilList.BOOKMARK:
			// mTxtDropDownItemsName.setText("Bookmarked");
			// calHead = "Bookmarked";
			UpdateDB.bookmarkAttachmentsSecureBrowser(
					AttachmentListActivity.this,
					UtilList.bookmarkedAttachmentItemIDValuesCal);
			calArrayList = UpdateDB.getListBrowserDetails(this, 2);
			break;
		/*
		 * case UtilList.SENTBY:
		 * 
		 * runOnUiThread(new Runnable() {
		 * 
		 * @Override public void run() {
		 * mTxtDropDownItemsName.setText("Sent by"); } });
		 * 
		 * 
		 * mTxtDropDownItemsName.setText("Sent by"); calHead = "Sent by";
		 * UpdateDB
		 * .bookmarkAttachmentsSecureBrowser(AttachmentListActivity.this,
		 * UtilList.bookmarkedAttachmentItemIDValuesCal); calArrayList =
		 * UpdateDB.getListBrowserDetails(this,3); break;
		 */
		}

		if (calArrayList != null) {
			for (int i = 0; i < calArrayList.size(); i++) {
				performDateTimeSplit(i, calArrayList);
			}

			/*
			 * OutlookPreference.getInstance(mContext).setValue(
			 * mContext.getResources().getString(R.string.synctime),
			 * calArrayList.get(0).getDATE_TIME_RECEIVED());
			 */

			UtilList.makeHeaderList(calArrayList, datatype);

			// 27-8-2013
			String searchText = "";
			try {

				searchText = mEdTxtSubHeaderCal.getText().toString();
				Log.i("TRY", "in mail new list " + searchText + "   uhsh");
			} catch (Exception e) {
				Log.i("Exception", "in mail new list");
				searchText = "";
			}
			if (!(searchText.trim().equalsIgnoreCase(""))) {
				getSearchList(searchText, calArrayList);
				displaySearchList(searchArrayCal);
				calAdapter = new AttachmentListAdapterCal(mContext,
						searchArrayCal);
			} else
				calAdapter = new AttachmentListAdapterCal(mContext,
						calArrayList);
			// 27-8-2013

			// calAdapter = new AttachmentListAdapterCal(mContext,
			// calArrayList);
			calListView.setAdapter(calAdapter);
		} else {

			if(myPager.getCurrentItem() == 1){
				
			if(datatype == UtilList.BOOKMARK)
				Toast.makeText(mContext, "No browser attachments are bookmarked in last 3 days", Toast.LENGTH_SHORT)
					.show();
			else
				Toast.makeText(mContext, "No browser attachments in last 3 days", Toast.LENGTH_SHORT)
				.show();

			}
			
			ArrayList<ChildMailbox> dummysize = new ArrayList<ChildMailbox>();
			calArrayList = dummysize;
			calAdapter = new AttachmentListAdapterCal(mContext, calArrayList);
			calListView.setAdapter(calAdapter);
			calAdapter.notifyDataSetChanged();
		}
		/*
		 * if (dialog != null) { dialog.dismiss(); }
		 */
	}

	// perform date and time split operation

	public String performDateTimeSplit(int index,
			List<ChildMailbox> splitAttachmnetList) {
		String dateTime = splitAttachmnetList.get(index).getDateTimeReceived()
				.toString();
		String[] Date = dateTime.split("@");
		String mixedTime = Date[1];
		String[] Time = mixedTime.split("Z");

		/*
		 * Log.i("NEWWWW", "DATE " + Date[0].toUpperCase() + "   " + Time[0] +
		 * "   " + dateTime);
		 */

		splitAttachmnetList.get(index).setDATE(Date[0].toUpperCase());
		splitAttachmnetList.get(index).setTIME(Time[0]);
		return Date[0].toString().trim();
	}

	// The click events to be performed for various action controls
	public void startEventAction(View viewID) {

		switch (viewID.getId()) {

		// Header items click events
		case R.id.txt_pager:
		case R.id.txt_pager_name:
		case R.id.txt_dropdownitems_name:
		case R.id.lytDropDown:
			mQuickAction.show(viewID);
			break;

		// Footer items click event(for delete functionality)
		case R.id.btn_cancel_mail:
			mLytListFooterMail.setVisibility(View.GONE);
			refreshFromDelte();
			break;

		case R.id.btn_delete_mail:

			if (UtilList.deleteItemIDMail.keySet().size() > 0) {
				showDialog("Deleting Please Wait...");
				deleteSelectedItems();
			} else {
				Toast.makeText(this,
						"Please select atleast one item to delete",
						Toast.LENGTH_LONG).show();
			}
			mLytListFooterMail.setVisibility(View.GONE);
			refreshDeleteToNormal(mailArrayList);
			break;

		case R.id.btn_cancel_cal:
			mLytListFooterCal.setVisibility(View.GONE);
			refreshDeleteToNormal(calArrayList);

			break;

		case R.id.btn_delete_cal:

			if (UtilList.deleteItemIDCal.keySet().size() > 0) {
				showDialog("Deleting Please Wait...");
				deleteSelectedItems();
			} else {
				Toast.makeText(this,
						"Please select atleast one item to delete",
						Toast.LENGTH_LONG).show();
			}
			mLytListFooterCal.setVisibility(View.GONE);
			refreshDeleteToNormal(calArrayList);
			break;

		case R.id.img_settings:
			startSettingsAct();
			break;

		case R.id.img_refresh: {

			try {
				if(myPager.getCurrentItem() == 0){
			
			Toast.makeText(this,
					"Syncing mailbox...",
					Toast.LENGTH_SHORT).show();
					}
			} catch (Exception e) {

			}
			onRefresh();
			break;

		}

		case R.id.img_connect: {

			finish();
			break;
		}

		case R.id.con_del_icon:
		case R.id.actionbar_discard:
			Log.i("NEW", "----------> con_del_icon ");
			showDialog("Deleting Please Wait...");
			deleteSelectedItems();
			delCan.setVisibility(View.GONE);
			header.setVisibility(View.VISIBLE);
			editHeaderMail.setVisibility(View.VISIBLE);
			editHeaderCal.setVisibility(View.VISIBLE);
			isDeleteModeMail = false;
			refreshFromDelte();
			// mailgetListData(UtilList.dataTypeMail);
			break;

		case R.id.actionbar_close:

			Log.i("Size",
					"" + UtilList.bookmarkedAttachmentItemIDValuesMail.size());
			Log.i("Size",
					"Cal "
							+ UtilList.bookmarkedAttachmentItemIDValuesCal
									.size());

			UpdateDB.bookmarkAttachments(AttachmentListActivity.this,
					UtilList.bookmarkedAttachmentItemIDValuesMail);

			UpdateDB.bookmarkAttachmentsSecureBrowser(
					AttachmentListActivity.this,
					UtilList.bookmarkedAttachmentItemIDValuesCal);
			cancelHeader();

		case R.id.img_title_bookmarked_include:

			Log.i("Multi", "r " + bookmarkIcon.isChecked());
			multiBookmark(bookmarkIcon.isChecked());
			break;

		case R.id.img_title_bookmarked:

			int id;
			switch (myPager.getCurrentItem()) {
			case 0:

				id = mailAdapter.clickId;
				if (id != -1) {
					mailAdapter.mBookmarkedStatus.set(id,
							titleBookmarkIcon.isChecked());
					mailAdapter.notifyDataSetChanged();
				}

				break;

			case 1:
				id = calAdapter.clickId;
				if (id != -1) {
					calAdapter.mBookmarkedStatus.set(id,
							titleBookmarkIcon.isChecked());
					calAdapter.notifyDataSetChanged();
				}
				break;
			}
			break;
			
		case R.id.con_del_title_icon:
			
			ArrayList<String> singleDelete = new ArrayList<String>();
			int _idMail = mailAdapter.clickId;
			Log.i("Singledelete", "co "+_idMail);
			try{
			if( _idMail != -1 ){
				singleDelete.add(mailAdapter.getAttId(_idMail));
				Log.i("Singledelete", "con_del_title_icon "+singleDelete.get(0));
				UpdateDB.deleteAttachments(this, singleDelete);
				
				mailAdapter.clickId = -1;
				mailgetListData(UtilList.dataTypeMail);
				
				mailListview.setSelection(_idMail);
				
			}

			int _idCal = calAdapter.clickId;
			
			if(_idCal != -1){
				
				singleDelete.add(calAdapter.getAttId(_idCal));
				Log.i("Singledelete", "con_del_title_icon "+singleDelete.get(0));
				UpdateDB.deleteAttachmentsSecureBrowser(this, singleDelete);
				
				calAdapter.clickId = -1;
				calgetListData(UtilList.dataTypeCal);
				
				calListView.setSelection(_idCal);
				
			}
			}catch (Exception e) {
			
			}
			try{
				
				Bundle args = new Bundle();
				args.putString(
						"displayMsg",
						getResources().getString(
								R.string.container_dum_msg_normal));
				viewerDisplayDummy = ViewerFragmentDummy.newInstance(args);

				android.app.FragmentManager fragmentManager = ((Activity) mContext)
						.getFragmentManager();
				FragmentTransaction transaction = fragmentManager
						.beginTransaction();
				transaction.replace(_gLayid_mail, viewerDisplayDummy, "test");

				transaction.commitAllowingStateLoss();
				
			}catch (Exception e) {
			}
			break;
			

		}
	}
	
	private void cancelHeader() {

		delCan.setVisibility(View.GONE);
		header.setVisibility(View.VISIBLE);
		editHeaderMail.setVisibility(View.VISIBLE);
		editHeaderCal.setVisibility(View.VISIBLE);
		isDeleteModeMail = false;
		isDeleteModeCal = false;
		refreshFromDelte();
		UtilList.bookmarkItemIDMail.clear();
		UtilList.bookmarkItemIDCal.clear();

	}

	private void multiBookmark(boolean checkedBookmark) {

		// ArrayList<String> deleteAttachmentIdsMail = new ArrayList<String>();

		for (Map.Entry<String, String> attachmentID : UtilList.deleteItemIDMail
				.entrySet()) {

			String key = attachmentID.getKey();
			Log.i("DeleteFn ", "  " + key);

			if (!(UtilList.bookmarkItemIDMail.contains(key))) {
				Log.i("If ", " not present " + key);
				UtilList.bookmarkItemIDMail.add(key);
			}
			mailAdapter.mBookmarkedStatus.set(Integer.parseInt(key),
					checkedBookmark);
		}
		mailAdapter.notifyDataSetChanged();
		for (Map.Entry<String, String> attachmentID : UtilList.deleteItemIDCal
				.entrySet()) {

			String key = attachmentID.getKey();
			Log.i("DeleteFn ", "  " + key);
			if (!(UtilList.bookmarkItemIDCal.contains(key))) {
				Log.i("If ", " not present " + key);
				UtilList.bookmarkItemIDCal.add(key);
			}
			calAdapter.mBookmarkedStatus.set(Integer.parseInt(key),
					checkedBookmark);

		}
		calAdapter.notifyDataSetChanged();
		/*
		 * UpdateDB.bookmarkAttachments(AttachmentListActivity.this,
		 * UtilList.bookmarkedAttachmentItemIDValuesMail);
		 */
	}

	private void startSettingsAct() {
		Intent intent = new Intent(this, SecAppSettingsActivity.class);
		startActivity(intent);
	}

	private void refreshDeleteToNormal(List<ChildMailbox> attachmentList) {
		try {
			if (myPager.getCurrentItem() == 0) {
				isDeleteModeMail = false;
				if (attachmentList != null) {
					for (int i = 0; i < attachmentList.size(); i++) {
						mailAdapter.toggleSelected(i, 0);
					}
				}
				mailListview.invalidate();
				mailAdapter.notifyDataSetChanged();
			} else {
				isDeleteModeCal = false;
				if (attachmentList != null) {
					for (int i = 0; i < attachmentList.size(); i++) {
						calAdapter.toggleSelected(i, 0);
					}
				}
				calListView.invalidate();
				calAdapter.notifyDataSetChanged();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteSelectedItems() {
		if (isDeleteModeMail) {
			ArrayList<String> deleteAttachmentIdsMail = new ArrayList<String>();
			for (Object attachmentID : UtilList.deleteItemIDMail.keySet()) {
				deleteAttachmentIdsMail.add(UtilList.deleteItemIDMail
						.get(attachmentID));
			}

			UpdateDB.deleteAttachments(this, deleteAttachmentIdsMail);
			UtilList.deleteItemIDMail.clear();
			mailgetListData(UtilList.dataTypeMail);
		}

		if (isDeleteModeCal) {

			ArrayList<String> deleteAttachmentIdsCal = new ArrayList<String>();
			for (Object attachmentID : UtilList.deleteItemIDCal.keySet()) {
				deleteAttachmentIdsCal.add(UtilList.deleteItemIDCal
						.get(attachmentID));
			}
			UpdateDB.deleteAttachmentsSecureBrowser(this,
					deleteAttachmentIdsCal);
			UtilList.deleteItemIDMail.clear();
			calgetListData(UtilList.dataTypeCal);
		}

		if (dialog != null)
			dialog.dismiss();

	}

	private void showDialog(String dialogMessage) {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.con_check_server, null);
		alert.setView(view);
		alert.setCancelable(false);
		dialog = alert.create();
		TextView txtDialogMessage = (TextView) view
				.findViewById(R.id.checkserver_content_text);
		txtDialogMessage.setText(dialogMessage);
		// txtDialogMessage.setTextColor(Color.WHITE);
		dialog.show();
	}

	// View the attachments in a deatiled aspect
	private void getAttachmentDeatil(int position,
			List<ChildMailbox> attachmnetListDetail) {

		final String documentName = attachmnetListDetail.get(position)
				.getAttachmentName().toString();

		String extension = "";

		int middle = documentName.lastIndexOf(".");
		extension = documentName.substring(middle + 1, documentName.length());

		Log.d("NEW", "----> getAttachmentDeatil " + documentName + "   "
				+ extension);

		if (extension.equalsIgnoreCase("pdf")
				|| extension.equalsIgnoreCase("txt")) {
			//System.out.println("Inside Click getAttachmentDeatil");

			UtilList.fileName = attachmnetListDetail.get(position)
					.getAttachmentName();
			Log.i("NEW", "----->New File " + UtilList.fileName);
			String parseMessageId = attachmnetListDetail.get(position)
					.getAttachmentId().toString();

			String content = attachmnetListDetail.get(position).getContent();

			if (content == null)
				content = "";

			Log.d("NEW", "----> getAttachmentDeatil " + parseMessageId + "   "
					+ content + "   "
					+ attachmnetListDetail.get(position).getContent());

			AttSize = Long.parseLong(attachmnetListDetail.get(position)
					.getSize());
			ID = Long.parseLong(attachmnetListDetail.get(position).getID());

			Account = Long.parseLong(attachmnetListDetail.get(position)
					.getACCOUNT_KEY());

			AccId = Long.parseLong(attachmnetListDetail.get(position)
					.getATTACHMENT_TABLE_ID());

			mailKey = Long.parseLong(attachmnetListDetail.get(position)
					.getMAILBOX_KEY());

			UtilList.fileName = attachmnetListDetail.get(position)
					.getAttachmentName();

			Log.d("NEW", "----> getAttachmentDeatil need to download " + AccId
					+ "   " + ID + "  " + mailKey + "   " + Account);

			if (content.trim().equalsIgnoreCase("")) {

				onDownloadAttachment(AccId, ID, mailKey, Account);

			} else {

				intermediatefn(mContext, Account, AccId, AttSize, extension,
						UtilList.fileName);
				/*
				 * openAttachment( UtilList.writeTemp(Uri.parse(content),
				 * extension, this), mContext, _gLayid);
				 */
			}

		} else {

			if (!(isTablet()))
				Toast.makeText(mContext,
						"Only pdf and text files can be viewed",
						Toast.LENGTH_SHORT).show();
			else {
				Bundle args = new Bundle();
				args.putString(
						"displayMsg",
						getResources().getString(
								R.string.container_dum_msg_specific));
				viewerDisplayDummy = ViewerFragmentDummy.newInstance(args);

				android.app.FragmentManager fragmentManager = ((Activity) mContext)
						.getFragmentManager();
				FragmentTransaction transaction = fragmentManager
						.beginTransaction();
				transaction.replace(_gLayid_mail, viewerDisplayDummy, "test");

				transaction.commitAllowingStateLoss();
			}

			if (dialog != null)
				dialog.dismiss();

		}
	}

	private void getAttachmentDeatilSB(int position,
			List<ChildMailbox> attachmnetListDetail) {

		final String documentName = attachmnetListDetail.get(position)
				.getAttachmentName().toString();

		String extension = "";

		int middle = documentName.lastIndexOf(".");
		extension = documentName.substring(middle + 1, documentName.length());

		Log.d("NEW", "----> getAttachmentDeatil " + documentName + "   "
				+ extension);

		if (extension.equalsIgnoreCase("pdf")
				|| extension.equalsIgnoreCase("txt")) {
			//System.out.println("Inside Click");
			showDialog("Opening attachment...");

			UtilList.fileName = attachmnetListDetail.get(position)
					.getAttachmentName();
			Log.i("NEW", "----->New File " + UtilList.fileName);
			String parseMessageId = attachmnetListDetail.get(position)
					.getAttachmentId().toString();

			String content = attachmnetListDetail.get(position).getContent();

			if (content == null)
				content = "";

			AttSize = Long.parseLong(attachmnetListDetail.get(position)
					.getSize());

			Log.d("NEW", "----> getAttachmentDeatil " + parseMessageId + "   "
					+ content + "   " + AttSize);

			/*
			 * UtilList.fileName = attachmnetListDetail.get(position)
			 * .getAttachmentName().toString();
			 */

			if (content.trim().equalsIgnoreCase("")) {

				if (dialog != null)
					dialog.dismiss();

			} else {

				/*
				 * decryptFileSB(mContext,content, AttSize, extension);
				 */
				decryptFileSB(mContext, content, AttSize, extension,
						documentName, parseMessageId);
			}

		} else {

			if (!(isTablet()))
				Toast.makeText(mContext,
						"Only pdf and text files can be viewed",
						Toast.LENGTH_SHORT).show();
			else {
				Bundle args = new Bundle();
				args.putString(
						"displayMsg",
						getResources().getString(
								R.string.container_dum_msg_specific));
				viewerDisplayDummy = ViewerFragmentDummy.newInstance(args);

				android.app.FragmentManager fragmentManager = ((Activity) mContext)
						.getFragmentManager();
				FragmentTransaction transaction = fragmentManager
						.beginTransaction();
				transaction.replace(_gLayid_cal, viewerDisplayDummy, "test");

				transaction.commitAllowingStateLoss();
			}
				
				if(dialog != null)
					dialog.dismiss();
		}
	}

	private void displaySearchList(List<ChildMailbox> splitAttachmnetList) {
		List<ChildMailbox> searchList = new ArrayList<ChildMailbox>();
		if (myPager.getCurrentItem() == 0) {
			searchList = searchArrayMail;
		} else {
			searchList = searchArrayCal;
		}

		if (searchList != null) {

			for (int i = 0; i < searchList.size(); i++) {
				performDateTimeSplit(i, splitAttachmnetList);

			}

			if (searchList.size() > 0) {
				if (myPager.getCurrentItem() == 0) {
					UtilList.makeHeaderList(searchList, UtilList.dataTypeMail);
				} else {
					UtilList.makeHeaderList(searchList, UtilList.dataTypeCal);
				}
			}
		}

	}

	private void getSearchList(CharSequence searchText,
			List<ChildMailbox> attachmentSearchList) {
		switch (searchText.length()) {
		case 0:
			if (myPager.getCurrentItem() == 0) {
				searchArrayMail = null;
				mailListview.invalidate();
				mailAdapter.notifyDataSetChanged();
				mailgetListData(UtilList.dataTypeMail);
			} else {
				searchArrayCal = null;
				calListView.invalidate();
				calAdapter.notifyDataSetChanged();
				calgetListData(UtilList.dataTypeCal);
			}

			break;
		default: {
			if (myPager.getCurrentItem() == 0) {
				searchArrayMail = null;
				searchArrayMail = new ArrayList<ChildMailbox>();
			} else {
				searchArrayCal = null;
				searchArrayCal = new ArrayList<ChildMailbox>();
			}

			for (int i = 0; i < attachmentSearchList.size(); i++) {
				if (attachmentSearchList.get(i).getEmailAddress().toString()
						.toUpperCase().trim()
						.contains((searchText.toString().toUpperCase()))
						|| attachmentSearchList
								.get(i)
								.getAttachmentName()
								.toUpperCase()
								.trim()
								.contains((searchText.toString().toUpperCase()))) {
					if (myPager.getCurrentItem() == 0) {
						searchArrayMail.add(attachmentSearchList.get(i));
					} else {
						searchArrayCal.add(attachmentSearchList.get(i));
					}

				}
			}
			break;
		}

		}
	}

	BroadcastReceiver RestrictedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context1, Intent intent) {

			try {
				Log.d("BroadCast REceived for resource download", "ghfg ");

				if (intent.getStringExtra("Broadcast").equalsIgnoreCase(
						"Broadcast")) {

					mailgetListData(UtilList.dataTypeMail);
					calgetListData(UtilList.dataTypeCal);

				} else if (intent.getStringExtra("Broadcast").equalsIgnoreCase(
						"DefaultView")) {

					UtilList.dataTypeMail = detDataFromprefView();
					UtilList.dataTypeCal = detDataFromprefViewCal();

					Log.i("Header", " " + UtilList.dataTypeMail + "   "
							+ UtilList.dataTypeCal);

					switch (UtilList.dataTypeCal) {
					case UtilList.SENTBY:
						UtilList.dataTypeCal = UtilList.RECENT_ADDED;
						break;

					default:
						break;
					}

					mailgetListData(UtilList.dataTypeMail);
					calgetListData(UtilList.dataTypeCal);
					/*
					 * switch (myPager.getCurrentItem()) { case 0:
					 * mailgetListData(UtilList.dataTypeMail); break;
					 * 
					 * case 1: calgetListData(UtilList.dataTypeCal); break;
					 * 
					 * default: break; }
					 */

				} else {
					AttachmentListActivity.this.finish();
				}
			} catch (Exception e) {

			}

		}

	};

	@Override
	protected void onDestroy() {

		super.onDestroy();
		mController.removeResultCallback(mControllerCallback);
		try {
			if (RestrictedReceiver != null)
				unregisterReceiver(RestrictedReceiver);
		} catch (Exception e) {

		}
		calAdapter.clickId = -1;
	}

	@Override
	protected void onPause() {
		super.onPause();

		isActVisible = false;//2-12-2103
		Log.i("2058", "onPause :------> " + UtilList.dataTypeMail + "   "
				+ UtilList.dataTypeCal);

		UpdateDB.bookmarkAttachments(AttachmentListActivity.this,
				UtilList.bookmarkedAttachmentItemIDValuesMail);
		UpdateDB.bookmarkAttachmentsSecureBrowser(AttachmentListActivity.this,
				UtilList.bookmarkedAttachmentItemIDValuesCal);
		if (isTablet()) {

			mailAdapter.clickId = -1;
			calAdapter.clickId = -1;
			mailAdapter.notifyDataSetChanged();
			calAdapter.notifyDataSetChanged();

			if (viewerDisplayDummy == null) {

				Bundle args = new Bundle();
				args.putString(
						"displayMsg",
						getResources().getString(
								R.string.container_dum_msg_normal));
				viewerDisplayDummy = ViewerFragmentDummy.newInstance(args);

				android.app.FragmentManager fragmentManager = ((Activity) mContext)
						.getFragmentManager();
				FragmentTransaction transaction = fragmentManager
						.beginTransaction();
				transaction.replace(_gLayid_mail, viewerDisplayDummy, "test");

				transaction.commitAllowingStateLoss();
			}

		}

		/*
		 * UtilList.dataTypeMail = detDataFromprefView(); UtilList.dataTypeCal =
		 * detDataFromprefView(); mailgetListData(UtilList.dataTypeMail);
		 * calgetListData(UtilList.dataTypeCal);
		 */
	}

	@Override
	protected void onResume() {
		super.onResume();
		// uiRenderingEngine();
		/* int datatype = detDataFromprefView(); */

		isActVisible = true;
		File dir = new File(this.getFilesDir()+File.separator+"temp"+File.separator);
		
		if(!dir.exists())
			dir.mkdirs();
		
		UtilList.recursiveDelete(dir);

		UtilList.dataTypeMail = detDataFromprefView();
		UtilList.dataTypeCal = detDataFromprefViewCal();
		;
		Log.i("2058", " resume  " + UtilList.dataTypeMail + "   "
				+ UtilList.dataTypeCal);
		mHandler = new MessageViewHandler();
		mController.addResultCallback(mControllerCallback);
		// Log.i("onResume ", "onResume :------> " + datatype);

		if (dialog != null)
			dialog.dismiss();

	}

	/*
	 * public void openAttachment(String msg, Context mContext) {
	 * 
	 * String ext = ""; try {
	 * 
	 * int mid = msg.lastIndexOf("."); ext = msg.substring(mid + 1,
	 * msg.length());
	 * 
	 * } catch (Exception e) {
	 * 
	 * }
	 * 
	 * String path = msg; Log.i("PTATH ", "----> path " + path);
	 * 
	 * if(dialog != null) dialog.dismiss();
	 * 
	 * 
	 * if (!(path.equals(""))) { // Give to PDF
	 * 
	 * if (ext.equalsIgnoreCase("pdf")) {
	 * 
	 * Intent pdfIntent = new Intent();
	 * 
	 * pdfIntent.setDataAndType(Uri.fromFile(new File(path)),
	 * "application/pdf"); pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	 * pdfIntent.setClass(mContext, OpenFileActivity.class);
	 * pdfIntent.setAction("android.intent.action.VIEW");
	 * mContext.startActivity(pdfIntent);
	 * 
	 * 
	 * } else {
	 * 
	 * 
	 * Intent pdfIntent = new Intent();
	 * pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	 * pdfIntent.setClass(mContext, MimeTypeTextShow.class);
	 * OutlookPreference.getInstance(mContext).setValue( "FilePath", path);
	 * mContext.startActivity(pdfIntent); } } else { } }
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	// @SuppressLint("NewApi")
	public void openAttachment(String msg, Context mContext, int _Layid) {

		String ext = "";
		try {

			int mid = msg.lastIndexOf(".");
			ext = msg.substring(mid + 1, msg.length());

		} catch (Exception e) {

		}

		String path = msg;
		Log.i("PTATH ", "----> path " + path);

		if (dialog != null)
			dialog.dismiss();

		if (!(path.equals(""))) {
			// Give to PDF

			if (ext.equalsIgnoreCase("pdf")) {

				if (isTablet()) {

					/*
					 * Configuration config = mContext.getResources()
					 * .getConfiguration(); if (config.orientation ==
					 * Configuration.ORIENTATION_LANDSCAPE) {
					 */// Commented

					// showDialogTimer("Loading ...", 2500);

					viewerDisplayDummy = null;

					Bundle args = new Bundle();
					Intent pdfIntent = new Intent();
					pdfIntent.setDataAndType(Uri.fromFile(new File(path)),
							"application/pdf");
					if (viewerDisplay != null) {
						viewerDisplay.fragclearCacheMemory();
					}
					viewerDisplay = ViewerFragmentPdf.newInstance(args);
					viewerDisplay.setIntent(pdfIntent);
					android.app.FragmentManager fragmentManager = ((Activity) mContext)
							.getFragmentManager();
					FragmentTransaction transaction = fragmentManager
							.beginTransaction();
					transaction.replace(_Layid, viewerDisplay, "test");

					transaction.commitAllowingStateLoss();
					/*
					 * } else {
					 * 
					 * Intent pdfIntent = new Intent();
					 * 
					 * pdfIntent.setDataAndType(Uri.fromFile(new File(path)),
					 * "application/pdf");
					 * pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					 * pdfIntent.setClass(mContext, OpenFileActivity.class);
					 * pdfIntent.setAction("android.intent.action.VIEW");
					 * mContext.startActivity(pdfIntent);
					 * 
					 * }
					 */// Commented

				} else {

					File opFile = new File(path);
					if (!(opFile.length() == 0)) {
					Intent pdfIntent = new Intent();

					pdfIntent.setDataAndType(Uri.fromFile(new File(path)),
							"application/pdf");
					pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					pdfIntent.setClass(mContext, OpenFileActivity.class);
					pdfIntent.setAction("android.intent.action.VIEW");
					mContext.startActivity(pdfIntent);
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

			} 
			else if(ext.equalsIgnoreCase("vcf")){
				UtilList.openEzcard(path, this);
			}else {

				if (isTablet()) {

					/*
					 * Configuration config = mContext.getResources()
					 * .getConfiguration(); if (config.orientation ==
					 * Configuration.ORIENTATION_LANDSCAPE) {
					 */// Commented

					viewerDisplayDummy = null;

					Bundle args = new Bundle();
					OutlookPreference.getInstance(mContext).setValue(
							"FilePath", path);
					args.putString("FilePath", path);
					Fragment viewerDisplay = ViewerFragment.newInstance(args);
					android.app.FragmentManager fragmentManager = ((Activity) mContext)
							.getFragmentManager();
					FragmentTransaction transaction = fragmentManager
							.beginTransaction();
					transaction.replace(_Layid, viewerDisplay, "test");
					transaction.commitAllowingStateLoss();
					/*
					 * } else {
					 * 
					 * Intent pdfIntent = new Intent();
					 * pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					 * pdfIntent.setClass(mContext, MimeTypeTextShow.class);
					 * OutlookPreference.getInstance(mContext).setValue(
					 * "FilePath", path); mContext.startActivity(pdfIntent); }
					 */// Commented

				} else {

					File opFile = new File(path);
					if (!(opFile.length() == 0)) {
					Intent pdfIntent = new Intent();
					pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					pdfIntent.setClass(mContext, MimeTypeTextShow.class);
					OutlookPreference.getInstance(mContext).setValue(
							"FilePath", path);
					mContext.startActivity(pdfIntent);
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
			}
		} else {
			// Show toast
		}
	}

	public boolean isNetworkOnline() {
		boolean status = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if (netInfo != null
					&& netInfo.getState() == NetworkInfo.State.CONNECTED) {
				status = true;
			} else {
				netInfo = cm.getNetworkInfo(1);
				if (netInfo != null
						&& netInfo.getState() == NetworkInfo.State.CONNECTED)
					status = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return status;

	}

	/*
	 * private void showDialogTimer(String dialogMessage, int timer) {
	 * 
	 * AlertDialog.Builder alert = new AlertDialog.Builder(this); LayoutInflater
	 * inflater = (LayoutInflater) this
	 * .getSystemService(Context.LAYOUT_INFLATER_SERVICE); View view =
	 * inflater.inflate(R.layout.con_check_server, null); alert.setView(view);
	 * alert.setCancelable(false); timerDialog = alert.create(); TextView
	 * txtDialogMessage = (TextView) view
	 * .findViewById(R.id.checkserver_content_text);
	 * txtDialogMessage.setText(dialogMessage); timerDialog.show(); Handler
	 * hadler = new Handler(); hadler.postDelayed(new Runnable() {
	 * 
	 * @Override public void run() {
	 * 
	 * if (timerDialog != null) timerDialog.dismiss(); } }, 3000);
	 * 
	 * }
	 */

	/**
	 * Controller results listener. This completely replaces MessagingListener
	 */
	private class ControllerResults implements EmEmController.Result {

		public void loadMessageForViewCallback(EmMessagingException result,
				long messageId, int progress) {

		}

		public void loadAttachmentCallback(EmMessagingException result,
				long messageId, long attachmentId, int progress) {

			Log.i("NEW", "Progress   " + progress + "   " + (messageId == ID));
			if (messageId == ID) {
				if (result == null) {
					switch (progress) {
					case 0:

						break;
					case 100:
						mHandler.finishLoadAttachment(attachmentId);
						break;
					default:
						break;
					}
				} else {

					mHandler.networkError();
				}
			}
		}

		public void updateMailboxCallback(EmMessagingException result,
				long accountId, long mailboxId, int progress, int numNewMessages) {

			switch (progress) {
			case 100:

				runOnUiThread(new Runnable() {
					public void run() {

						if (!isDeleteModeMail) {
							try {
								switch (myPager.getCurrentItem()) {
								case 1:
									Log.i("2058", "updateMailboxCallback 1 "
											+ UtilList.dataTypeCal);
									calgetListData(UtilList.dataTypeCal);
									break;

								case 0:
									Log.i("2058", "updateMailboxCallback 0 "
											+ UtilList.dataTypeMail);
									mailgetListData(UtilList.dataTypeMail);
									break;
								default:
									break;
								}
							} catch (Exception e) {

							}

						}
						if (dialog != null)
							dialog.dismiss();
					}
				});

				break;

			default:
				break;
			}

		}

		public void updateMailboxListCallback(EmMessagingException result,
				long accountId, int progress) {
		}

		public void serviceCheckMailCallback(EmMessagingException result,
				long accountId, long mailboxId, int progress, long tag) {
		}

		public void sendMailCallback(EmMessagingException result,
				long accountId, long messageId, int progress) {
		}
	}

	private void onDownloadAttachment(Long attTabId, Long msgTbId, Long mKey,
			Long acKey) {

		mController.loadAttachment(attTabId, msgTbId, mKey, acKey,
				mControllerCallback);
	}

	private class MessageViewHandler extends Handler {

		private static final int MSG_NETWORK_ERROR = 6;
		private static final int MSG_FETCHING_ATTACHMENT = 10;
		private static final int MSG_VIEW_ATTACHMENT_ERROR = 12;
		private static final int MSG_FINISH_LOAD_ATTACHMENT = 19;

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {

			case MSG_NETWORK_ERROR:
				Toast.makeText(AttachmentListActivity.this,
						R.string.status_network_error, Toast.LENGTH_LONG)
						.show();
				break;
			case MSG_FETCHING_ATTACHMENT:
				Toast.makeText(
						AttachmentListActivity.this,
						getString(R.string.message_view_fetching_attachment_toast),
						Toast.LENGTH_SHORT).show();
				break;
			case MSG_VIEW_ATTACHMENT_ERROR:
				Toast.makeText(
						AttachmentListActivity.this,
						getString(R.string.message_load_display_attachment_toast), // NaGa
						Toast.LENGTH_SHORT).show();
				break;

			case MSG_FINISH_LOAD_ATTACHMENT:
				long attachmentId = (Long) msg.obj;
				doFinishLoadAttachment(attachmentId);
				break;
			default:
				super.handleMessage(msg);
			}
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

		public void finishLoadAttachment(long attachmentId) {
			android.os.Message msg = android.os.Message.obtain(this,
					MSG_FINISH_LOAD_ATTACHMENT);
			msg.obj = Long.valueOf(attachmentId);
			sendMessage(msg);
		}
	}

	private void doFinishLoadAttachment(long attachmentId) {

		Attachment attachment = Attachment.restoreAttachmentWithId(
				AttachmentListActivity.this, attachmentId);

		final String documentName = attachment.mFileName;

		UtilList.fileName = documentName;

		String extension = "";

		int middle = documentName.lastIndexOf(".");
		extension = documentName.substring(middle + 1, documentName.length());

		Uri attachmentUri = EmAttachmentProvider.getAttachmentUri(Account,
				AccId);
		Uri contentUri = EmAttachmentProvider.resolveAttachmentIdToContentUri(
				getContentResolver(), attachmentUri);

		try {
			Log.i("NEW", "------> acc " + Account + "   " + AccId + "   "
					+ attachmentUri.toString() + "   " + contentUri.toString());
			if (!(contentUri == null)  && isActVisible && (attachmentId == attId)
					&& (myPager.getCurrentItem() == 0)) {//2-12-2013

				/*
				 * openAttachment( UtilList.writeTemp(contentUri, extension,
				 * mContext), mContext,_gLayid_mail);
				 */
				intermediatefn(mContext, Account, AccId, AttSize, extension,
						documentName);

			}

		} catch (Exception ioe) {
			Toast.makeText(
					AttachmentListActivity.this,
					getString(R.string.message_view_status_attachment_not_saved),
					Toast.LENGTH_LONG).show();
			openAttachment("", mContext, _gLayid_mail);
		}

	}

	private void onRefresh() {

		Long[] keyArray = UpdateDB.getMailAccKey(this);
		Log.i("onRefresh", "------> " + keyArray[1] + "    " + keyArray[0]);
		mController
				.updateMailbox(keyArray[1], keyArray[0], mControllerCallback);

	}

	private void intermediatefn(Context mContext, long mAccountId, long mId,
			long mSize, String ext, String fileName) {

		// CONTAINER CHNAGES
		// 14-8-2013
		String path = "";
		File saveIn = EmAttachmentProvider.getAttachmentDirectory(mContext,
				mAccountId);
		File newFile1 = new File(saveIn, Long.toString(mId) + ".truboxenc");
		File newFile = new File(saveIn, Long.toString(mId));
		/*
		 * TruboxFileEncryption truboxFileEncryption = new TruboxFileEncryption(
		 * mContext, newFile.getAbsolutePath(), STORAGEMODE.EXTERNAL);
		 */

		Integer i = (int) mSize;
		Log.i("NEW", "===============> " + i + "   " + newFile1.length()
				+ "   " + mId + "   " + newFile.getAbsolutePath());
		/*
		 * // 14-8-2013 if ((ext.equalsIgnoreCase("pdf"))) {
		 * 
		 * try { path = UtilList.createTempFile(mContext.getFilesDir() +
		 * File.separator + "temp.pdf", newFile.length(), i,
		 * truboxFileEncryption); } catch (Exception e) { e.printStackTrace(); }
		 * } else {
		 * 
		 * try { path = UtilList.createTempFile(mContext.getFilesDir() +
		 * File.separator + "temp.txt", newFile.length(), i,
		 * truboxFileEncryption); } catch (Exception e) { e.printStackTrace(); }
		 * 
		 * } openAttachment(path, mContext, _gLayid_mail);
		 */
		AttachmentOpenHelper objMail;
		// 14-8-2013

		// showDialog("Opening attachment...");
		if ((ext.equalsIgnoreCase("java")) || (ext.equalsIgnoreCase("h")) || (ext.equalsIgnoreCase("log"))
		|| (ext.equalsIgnoreCase("py")) || (ext.equalsIgnoreCase("m"))) {

			path = mContext.getFilesDir() + File.separator +"temp"+ File.separator + UtilList.generateMessageId(mId)  + ""
					/*+ fileName*/+".txt";

			objMail = new AttachmentOpenHelper(this, newFile, path, i, 1,"txt");

		} else {

/*			path = mContext.getFilesDir() + File.separator + mId + ""
					+ fileName;

			objMail = new AttachmentOpenHelper(this, newFile, path, i, 1,ext);*/
			path = mContext.getFilesDir() + File.separator + "temp"+ File.separator + UtilList.generateMessageId(mId) + ""
					/*+ fileName*/+"."+ext;

			objMail = new AttachmentOpenHelper(this, newFile, path, i, 1,ext);


		}

		objMail.execute(new String[] {});
		return;

	}

	@Override
	public void onRemoteCallback(boolean result, String path, int openType,
			Context mContext,String ext) {

		Log.i("NEW", "onRemoteCallback  " + result + "   " + path + "  "
				+ openType);

		/*if (dialog != null)
			dialog.dismiss();*/

		if (result) {
			switch (openType) {
			case 1:
				if(!isTFNeeded)//290388
					openAttachment(path, mContext, _gLayid_mail);
					else{
						openAttachment(path, mContext,ext);
					}
				break;

			case 2:
				if(!isTFNeeded)//290388
					openAttachment(path, mContext, _gLayid_cal);
					else{
						openAttachment(path, mContext,ext);
					}
					break;

			default:
				break;

			}

		}else{
			
			if (dialog != null)
				dialog.dismiss();
			
		}

	}

	private void decryptFileSB(Context mContext, String filePath, long mSize,
			String ext, String fileName, String pos) {

		// CONTAINER CHNAGES
		// 14-8-2013
		String path = "";

		File newFile = new File(filePath);
		/*
		 * TruboxFileEncryption truboxFileEncryption = new TruboxFileEncryption(
		 * mContext, newFile.getAbsolutePath(), STORAGEMODE.EXTERNAL);
		 */

		Integer i = (int) mSize;

		if (i >= (2 * 1024 * 1024))
			i = 2 * 1024 * 1024;

		Log.i("NEW", "===============> " + i + "   " + newFile.length());
		AttachmentOpenHelper objCal;
		// 14-8-2013
		/*if ((ext.equalsIgnoreCase("pdf"))) {

			path = mContext.getFilesDir() + File.separator + pos + fileName;

			objCal = new AttachmentOpenHelper(this, newFile, path, i, 2,ext);

		} else {

			path = mContext.getFilesDir() + File.separator + pos + fileName;

			objCal = new AttachmentOpenHelper(this, newFile, path, i, 2,ext);
		}
		objCal.execute(new String[] {});*/
		if ((ext.equalsIgnoreCase("java")) || (ext.equalsIgnoreCase("h")) || (ext.equalsIgnoreCase("log"))
				|| (ext.equalsIgnoreCase("py")) || (ext.equalsIgnoreCase("m"))) {

			path = mContext.getFilesDir() + File.separator +"temp"+ File.separator + UtilList.generateMessageId(Long.parseLong(pos)) + ""
					/*+ fileName*/+".txt";//Delte Temp files

			objCal = new AttachmentOpenHelper(this, newFile, path, i, 1,"txt");

		} else {

			path = mContext.getFilesDir() + File.separator +"temp"+ File.separator + UtilList.generateMessageId(Long.parseLong(pos)) /*+ fileName*/+"."+ext;

			objCal = new AttachmentOpenHelper(this, newFile, path, i, 2,ext);
		}
		objCal.execute(new String[] {});

		return;

	}

	@Override
	public void onUiUpdateListner(int position,
			List<ChildMailbox> deleteAttachmnetList) {

		Log.i("onUiUpdateListner", "-----> " + delCan.getVisibility());

		if (isTablet()) {

			if (viewerDisplayDummy == null) {

				Bundle args = new Bundle();
				args.putString(
						"displayMsg",
						getResources().getString(
								R.string.container_dum_msg_normal));
				viewerDisplayDummy = ViewerFragmentDummy.newInstance(args);

				android.app.FragmentManager fragmentManager = ((Activity) mContext)
						.getFragmentManager();
				FragmentTransaction transaction = fragmentManager
						.beginTransaction();
				transaction.replace(_gLayid_mail, viewerDisplayDummy, "test");

				transaction.commitAllowingStateLoss();
			}

		}

		mailAdapter.clickId = -1;
		calAdapter.clickId = -1;

		deleteSelectedMode(position, deleteAttachmnetList);

		String count;
		boolean deleteVisible = false;

		switch (myPager.getCurrentItem()) {

		case 0:

			/*
			 * Log.i("Multi", "onUiUpdateListner 0 "+bookmarkIcon.isChecked());
			 * if(bookmarkIcon.isChecked()){
			 * 
			 * bookmarkIcon.toggle();
			 * //bookmarkIcon.setBackgroundDrawable(getResources
			 * ().getDrawable(R.drawable.con_star_btn_off));
			 * 
			 * }
			 */// MultiBook

			isDeleteModeMail = true;
			count = String.valueOf(UtilList.deleteItemIDMail.size())
					+ " selected";
			deleteCount.setText(count);

			if (UtilList.deleteItemIDMail.size() > 0) {

				deleteVisible = true;

			} else {

				deleteVisible = false;
				isDeleteModeMail = false;

			}
			visibleCheck(deleteVisible, editHeaderMail);
			break;

		case 1:

			/*
			 * Log.i("Multi", "onUiUpdateListner 1 "+bookmarkIcon.isChecked());
			 * if(bookmarkIcon.isChecked()){
			 * 
			 * bookmarkIcon.toggle();
			 * //bookmarkIcon.setBackgroundDrawable(getResources
			 * ().getDrawable(R.drawable.con_star_btn_off));
			 * 
			 * }
			 */// MultiBook

			isDeleteModeCal = true;
			count = String.valueOf(UtilList.deleteItemIDCal.size())
					+ " selected";
			deleteCount.setText(count);

			if (UtilList.deleteItemIDCal.size() > 0) {

				deleteVisible = true;

			} else {

				deleteVisible = false;
				isDeleteModeCal = false;

			}
			visibleCheck(deleteVisible, editHeaderCal);

			break;

		}

	}

	private void visibleCheck(boolean deleteVisible, LinearLayout editHeader) {

		if (deleteVisible) {
			switch (delCan.getVisibility()) {

			case View.VISIBLE:

				break;

			case View.GONE:
			case View.INVISIBLE:

				delCan.setVisibility(View.VISIBLE);
				header.setVisibility(View.GONE);
				editHeader.setVisibility(View.GONE);
				break;

			default:
				break;

			}

		} else {

			bookmarkIcon.setChecked(false);// MultiBook
			refreshFromDelte();
			delCan.setVisibility(View.GONE);
			header.setVisibility(View.VISIBLE);
			editHeader.setVisibility(View.VISIBLE);

		}

	}

	@Override
	public void onBackPressed() {

		if (isDeleteModeMail || isDeleteModeCal) {
			bookmarkIcon.setChecked(false);// MultiBook
			delCan.setVisibility(View.GONE);
			header.setVisibility(View.VISIBLE);
			editHeaderMail.setVisibility(View.VISIBLE);
			editHeaderCal.setVisibility(View.VISIBLE);
			isDeleteModeMail = false;
			isDeleteModeCal = false;
			refreshFromDelte();
			UtilList.bookmarkItemIDMail.clear();
			UtilList.bookmarkItemIDCal.clear();
			
		} else {

			super.onBackPressed();
		}
	}

	private void refreshFromDelte() {

		String searchText;
		switch (myPager.getCurrentItem()) {
		case 0:

			searchText = mEdTxtSubHeaderMail.getText().toString();

			if (searchText != null && !(searchText.trim().equalsIgnoreCase(""))) {
				refreshDeleteToNormal(searchArrayMail);
			} else {
				refreshDeleteToNormal(mailArrayList);
			}
			try {
				UtilList.deleteItemIDMail.clear();
			} catch (Exception e) {

			}

			break;

		case 1:

			searchText = mEdTxtSubHeaderCal.getText().toString();

			if (searchText != null && !(searchText.trim().equalsIgnoreCase(""))) {
				refreshDeleteToNormal(searchArrayCal);
			} else {
				refreshDeleteToNormal(calArrayList);
			}
			try {
				UtilList.deleteItemIDCal.clear();
			} catch (Exception e) {

			}

			break;
		default:
			break;
		}

	}

	/*
	 * public void onRemoteCallback(boolean result,String path,int
	 * openType,Context mContext) {
	 * 
	 * Log.i("NEW","onRemoteCallback  "+result+"   "+path+"  "+openType);
	 * 
	 * if(dialog != null) dialog.dismiss();
	 * 
	 * if(result) { switch (openType) { case 1: openAttachment(path, mContext,
	 * _gLayid_mail); break;
	 * 
	 * case 2: openAttachment(path, mContext, _gLayid_cal); break;
	 * 
	 * default: break; }
	 * 
	 * }
	 * 
	 * }
	 */

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		Log.i("", "onItemClick " + arg0.getId() + "   " + R.id.listviewMail
				+ "   " + R.id.listviewCal + "   " + arg1.getId());

		switch (arg0.getId()) {
		case R.id.listviewMail:

			onItemClickMail(arg2);
			break;

		case R.id.listviewCal:

			onItemClickCal(arg2);
			break;

		default:
			break;
		}

	}

	public void onItemClickMail(int position) {

		if (isDeleteModeMail) {
			mailAdapter.clickId = -1;
			/*
			 * Log.i("Multi", "onItemClickMail "+bookmarkIcon.isChecked());
			 * if(bookmarkIcon.isChecked()){
			 * 
			 * bookmarkIcon.toggle();
			 * 
			 * }
			 */// MultiBook

			try {

				String searchText = mEdTxtSubHeaderMail.getText().toString();

				if (searchText != null
						&& !(searchText.trim().equalsIgnoreCase(""))) {

					onUiUpdateListner(position, searchArrayMail);

				} else {

					onUiUpdateListner(position, mailArrayList);

				}
			} catch (Exception e) {

				onUiUpdateListner(position, mailArrayList);

			}

		} else {

			if (isTablet()) {
				mailAdapter.clickId = position;
				calAdapter.clickId = -1;
				mailAdapter.notifyDataSetChanged();

				titleBookmarkIcon.setChecked(mailAdapter.mBookmarkedStatus
						.get(position));

			}
			showDialog("Loading attachment...");

			try {

				String searchText = mEdTxtSubHeaderMail.getText().toString();

				if (searchText != null
						&& !(searchText.trim().equalsIgnoreCase(""))) {

					if(!isTFNeeded)//290388
						getAttachmentDeatil(position, searchArrayMail);
						else{
							getAttachmentDeatil(position, searchArrayMail,0);
						}

				} else {

					if(!isTFNeeded)//290388
						getAttachmentDeatil(position, mailArrayList);
						else{
							getAttachmentDeatil(position, mailArrayList,0);
						}

				}
			} catch (Exception e) {

				if(!isTFNeeded)//290388
					getAttachmentDeatil(position, mailArrayList);
					else{
						getAttachmentDeatil(position, mailArrayList,0);
					}

			}
		}
	}

	private boolean isTablet() {

		if (getResources().getString(R.string.container_app_mode)
				.equalsIgnoreCase("seveninch")
				|| getResources().getString(R.string.container_app_mode)
						.equalsIgnoreCase("teninch")) {

			return true;
		}

		return false;

	}

	public void onItemClickCal(int position) {

		if (isDeleteModeCal) {
			calAdapter.clickId = -1;
			/*
			 * Log.i("Multi", "onItemClickCal "+bookmarkIcon.isChecked());
			 * if(bookmarkIcon.isChecked()){
			 * 
			 * bookmarkIcon.toggle(); //
			 * bookmarkIcon.setBackgroundDrawable(getResources
			 * ().getDrawable(R.drawable.con_star_btn_off));
			 * 
			 * }
			 */// MultiBook

			try {

				String searchText = mEdTxtSubHeaderCal.getText().toString();

				if (searchText != null
						&& !(searchText.trim().equalsIgnoreCase(""))) {

					onUiUpdateListner(position, searchArrayCal);

				} else {

					onUiUpdateListner(position, calArrayList);

				}
			} catch (Exception e) {

				onUiUpdateListner(position, calArrayList);

			}
		} else {

			if (isTablet()) {
				calAdapter.clickId = position;
				mailAdapter.clickId = -1;
				calAdapter.notifyDataSetChanged();
				titleBookmarkIcon.setChecked(calAdapter.mBookmarkedStatus
						.get(position));
			}

			try {

				String searchText = mEdTxtSubHeaderCal.getText().toString();

				if (searchText != null
						&& !(searchText.trim().equalsIgnoreCase(""))) {

					if(!isTFNeeded)//290388
						getAttachmentDeatilSB(position, searchArrayCal);
						else{
							getAttachmentDeatilSB(position, searchArrayCal,0);
						}

				} else {

					if(!isTFNeeded)//290388
						getAttachmentDeatilSB(position, calArrayList);
						else{
							getAttachmentDeatilSB(position, calArrayList,0);
						}

				}
			} catch (Exception e) {

				if(!isTFNeeded)//290388
					getAttachmentDeatilSB(position, calArrayList);
					else{
						getAttachmentDeatilSB(position, calArrayList,0);
					}

			}
		}
	}

	@Override
	public void onTitleBookmarkedUpdate(int pos, boolean isChecked) {

		if (isTablet())
			titleBookmarkIcon.setChecked(isChecked);

	}

	private void visibleItemDelBook(int isVisbleNeeded){
		
		if(isTablet()){
			
			deleteIconTitle.setVisibility(isVisbleNeeded);
			titleBookmarkIcon.setVisibility(isVisbleNeeded);
			
		}
		
		
	}

	@Override
	public void onClick(View v) {
		Log.i("OnClick ","=== View called ");
		switch (v.getId()) {
		case R.id.edtxt_subheader_name_att:
		case R.id.edtxt_subheader_name_cal:
			
			if (isTablet()) {

				mailAdapter.clickId = -1;
				calAdapter.clickId = -1;
				mailAdapter.notifyDataSetChanged();
				calAdapter.notifyDataSetChanged();
				
				if (viewerDisplayDummy == null) {

					Bundle args = new Bundle();
					args.putString(
							"displayMsg",
							getResources().getString(
									R.string.container_dum_msg_normal));
					viewerDisplayDummy = ViewerFragmentDummy.newInstance(args);

					android.app.FragmentManager fragmentManager = ((Activity) mContext)
							.getFragmentManager();
					FragmentTransaction transaction = fragmentManager
							.beginTransaction();
					transaction.replace(_gLayid_mail, viewerDisplayDummy, "test");

					transaction.commitAllowingStateLoss();
				}
			
		}
		
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
				|| ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("bmp")){
			
			shwMimeActivity(filePath,ext);/*
			File opFile = new File(filePath);
			if (!(opFile.length() == 0)) {
				Intent pdfIntent = new Intent();
				pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				pdfIntent.setClass(mContext, MimeTypeTextShow.class);
				OutlookPreference.getInstance(mContext).setValue(
						"FilePath", filePath);
				mContext.startActivity(pdfIntent);
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
			}*/
			
		}else if(ext.equalsIgnoreCase("html") || ext.equalsIgnoreCase("htm")){
			shwMimeActivity(filePath,ext);
		}
		else{
		TFDocLoader loader = TFDocLoader.createInstance(this);
		loader.setDocProperties(filePath);

		
		result = loader.launchViewer();
		TruMobiTimerClass.userInteractedStopTimer();
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
		
		if (dialog != null)
			dialog.dismiss();
		
	}

void shwMimeActivity(String filePath,String ext){
	

	
	File opFile = new File(filePath);
	if (!(opFile.length() == 0)) {
		Intent pdfIntent = new Intent();
		pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		pdfIntent.setClass(mContext, MimeTypeTextShow.class);
		OutlookPreference.getInstance(mContext).setValue(
				"FilePath", filePath);
		OutlookPreference.getInstance(mContext).setValue(
				"Extension", ext);
		mContext.startActivity(pdfIntent);
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

	
	// View the attachments in a deatiled aspect
	private void getAttachmentDeatil(int position,
			List<ChildMailbox> attachmnetListDetail,int dummy) {//290388

		final String documentName = attachmnetListDetail.get(position)
				.getAttachmentName().toString();

		String extension = "";

		int middle = documentName.lastIndexOf(".");
		extension = documentName.substring(middle + 1, documentName.length());

		Log.d("NEW", "----> getAttachmentDeatil " + documentName + "   "
				+ extension);

		
			// System.out.println("Inside Click getAttachmentDeatil");

			UtilList.fileName = attachmnetListDetail.get(position)
					.getAttachmentName();
			Log.i("NEW", "----->New File " + UtilList.fileName);
			String parseMessageId = attachmnetListDetail.get(position)
					.getAttachmentId().toString();

			String content = attachmnetListDetail.get(position).getContent();

			if (content == null)
				content = "";

			Log.d("Singledelete", "----> getAttachmentDeatil " + parseMessageId
					+ "   " + content);

			AttSize = Long.parseLong(attachmnetListDetail.get(position)
					.getSize());
			ID = Long.parseLong(attachmnetListDetail.get(position).getID());

			Account = Long.parseLong(attachmnetListDetail.get(position)
					.getACCOUNT_KEY());

			AccId = Long.parseLong(attachmnetListDetail.get(position)
					.getATTACHMENT_TABLE_ID());

			mailKey = Long.parseLong(attachmnetListDetail.get(position)
					.getMAILBOX_KEY());

			UtilList.fileName = attachmnetListDetail.get(position)
					.getAttachmentName();

			Log.d("NEW", "----> getAttachmentDeatil need to download " + AccId
					+ "   " + ID + "  " + mailKey + "   " + Account);

			attId = AccId;//2-12-2013
			if (content.trim().equalsIgnoreCase("")) {

				onDownloadAttachment(AccId, ID, mailKey, Account);

			} else {

				intermediatefn(mContext, Account, AccId, AttSize, extension,
						UtilList.fileName);
			}

	}
	
	private void getAttachmentDeatilSB(int position,
			List<ChildMailbox> attachmnetListDetail,int dummy) {

		final String documentName = attachmnetListDetail.get(position)
				.getAttachmentName().toString();

		String extension = "";

		int middle = documentName.lastIndexOf(".");
		extension = documentName.substring(middle + 1, documentName.length());

		Log.d("NEW", "----> getSB " + documentName + "   " + extension);

			showDialog("Opening attachment...");

			UtilList.fileName = attachmnetListDetail.get(position)
					.getAttachmentName();
			Log.i("NEW", "----->New File getSB " + UtilList.fileName);
			String parseMessageId = attachmnetListDetail.get(position)
					.getAttachmentId().toString();

			String content = attachmnetListDetail.get(position).getContent();

			if (content == null)
				content = "";

			AttSize = Long.parseLong(attachmnetListDetail.get(position)
					.getSize());

			Log.d("NEW", "----> getSB " + parseMessageId + "   " + content
					+ "   " + AttSize);

			if (content.trim().equalsIgnoreCase("")) {

				if (dialog != null)
					dialog.dismiss();

			} else {

				decryptFileSB(mContext, content, AttSize, extension,
						documentName, parseMessageId);
			}

	}

}