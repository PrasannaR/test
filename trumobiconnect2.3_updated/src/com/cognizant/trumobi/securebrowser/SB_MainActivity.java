package com.cognizant.trumobi.securebrowser;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cordova.CordovaChromeClient;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
import org.apache.cordova.DroidGap;
import org.apache.cordova.IceCreamCordovaWebViewClient;
import org.apache.cordova.api.CordovaInterface;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.container.activity.AttachmentListActivity;
import com.cognizant.trumobi.externaladapter.ExternalAdapterRegistrationClass;
import com.cognizant.trumobi.persona.PersonaMainActivity;
//import android.util.Log;
//import android.util.Log;

public class SB_MainActivity extends DroidGap implements CordovaInterface {

	LinearLayout sub = null;
	boolean isOpen = false;
	SB_MainActivity ObjActivity;
	private ImageView refresh = null;
	private ImageView stop = null;
	PopupWindow popupWindow = null;
	private EditText addressbar = null;
	RelativeLayout browserhead = null;
	RelativeLayout headerlayout = null;
	RelativeLayout findlayout = null;
	EditText findtext = null;
	String sdrUrl = null;
	String strOtherAppURL = null;
	String tileString = null;
	TextView findinpages;
	TextView bookmarks;
	TextView history;
	TextView settingsPage;
	TextView proxysetting;
	boolean proxyStatus = false;
	String url1;
	boolean click_home = false;
	ImageView leftarrow;
	ImageView rightarrow;
	ImageView leftArrowDisable;
	ImageView rightArrowDisable;
	TextView homePage;
	ImageView downloadPage;
	ImageView bookmarkfav;
	ImageView bookmarkfav_enabled;	
	ImageView stopLoad;
	CordovaWebView tempview;	
	boolean version4 = false;
	String urlValue;
	String stopURL = "";
	boolean homeurlflag = true;
	static String encriptKey = TruBoxDatabase.getString();
	String dbName = TruBoxDatabase.getHashValue("TruBrowser", this);
	boolean match_flag = false;
	Uri MASTER_URI = Uri.parse(SB_BrowserDBAdapter.CONTENT_URI + "/"
			+ SB_BrowserDBAdapter.MASTER_TABLE);
	static Uri ALLOW_BLOCK_URI = Uri.parse(SB_BrowserDBAdapter.CONTENT_URI + "/"
			+ SB_BrowserDBAdapter.URLLIST_TABLE);
	static byte[] ipStatic = null;

	//Set of flags updated from a DB
		boolean allowcopyPaste=false;
		String blockCookiesFlag="Y";
		public static String blockallowFlag = null;
		public static boolean allowdownload = true;

	String baseURL = "file:///android_asset/www/Main.html?home-page" + "&" + encriptKey + "&" + dbName;
	static String homeURL = "";
	String tempURL = "file:///android_asset/www/Main.html?temp-page" + "&" + encriptKey + "&" + dbName;

	public static ArrayList<String> blacklist = new ArrayList<String>();
	String[] TLDs1 = new String[] { "ac", "ad", "ae", "aero", "af", "ag", "ai",
			"al", "am", "an", "ao", "aq", "ar", "arpa", "as", "asia", "at",
			"au", "aw", "ax", "az", "ba", "bb", "bd", "be", "bf", "bg", "bh",
			"bi", "biz", "bj", "bm", "bn", "bo", "br", "bs", "bt", "bv", "bw",
			"by", "bz", "ca", "cat", "cc", "cd", "cf", "cg", "ch", "ci", "ck",
			"cl", "cm", "cn", "co", "com", "coop", "cr", "cu", "cv", "cx",
			"cy", "cz", "de", "dj", "dk", "dm", "do", "dz", "ec", "edu", "ee",
			"eg", "er", "es", "et", "eu", "fi", "fj", "fk", "fm", "fo", "fr",
			"ga", "gb", "gd", "ge", "gf", "gg", "gh", "gi", "gl", "gm", "gn",
			"gov", "gp", "gq", "gr", "gs", "gt", "gu", "gw", "gy", "hk", "hm",
			"hn", "hr", "ht", "hu", "id", "ie", "il", "im", "in", "info",
			"int", "io", "iq", "ir", "is", "it", "je", "jm", "jo", "jobs",
			"jp", "ke", "kg", "kh", "ki", "km", "kn", "kp", "kr", "kw", "ky",
			"kz", "la", "lb", "lc", "li", "lk", "lr", "ls", "lt", "lu", "lv",
			"ly", "ma", "mc", "md", "me", "mg", "mh", "mil", "mk", "ml", "mm",
			"mn", "mo", "mobi", "mp", "mq", "mr", "ms", "mt", "mu", "museum",
			"mv", "mw", "mx", "my", "mz", "na", "name", "nc", "ne", "net",
			"nf", "ng", "ni", "nl", "no", "np", "nr", "nu", "nz", "om", "org",
			"pa", "pe", "pf", "pg", "ph", "pk", "pl", "pm", "pn", "pr", "pro",
			"ps", "pt", "pw", "py", "qa", "re", "ro", "rs", "ru", "rw", "sa",
			"sb", "sc", "sd", "se", "sg", "sh", "si", "sj", "sk", "sl", "sm",
			"sn", "so", "sr", "st", "su", "sv", "sy", "sz", "tc", "td", "tel",
			"tf", "tg", "th", "tj", "tk", "tl", "tm", "tn", "to", "tp", "tr",
			"travel", "tt", "tv", "tw", "tz", "ua", "ug", "uk", "us", "uy",
			"uz", "va", "vc", "ve", "vg", "vi", "vn", "vu", "wf", "ws",
			"xn--0zwm56d", "xn--11b5bs3a9aj6g", "xn--3e0b707e", "xn--45brj9c",
			"xn--80akhbyknj4f", "xn--90a3ac", "xn--9t4b11yi5a",
			"xn--clchc0ea0b2g2a9gcd", "xn--deba0ad", "xn--fiqs8s",
			"xn--fiqz9s", "xn--fpcrj9c3d", "xn--fzc2c9e2c", "xn--g6w251d",
			"xn--gecrj9c", "xn--h2brj9c", "xn--hgbk6aj7f53bba",
			"xn--hlcj6aya9esc7a", "xn--j6w193g", "xn--jxalpdlp",
			"xn--kgbechtv", "xn--kprw13d", "xn--kpry57d", "xn--lgbbat1ad8j",
			"xn--mgbaam7a8h", "xn--mgbayh7gpa", "xn--mgbbh1a71e",
			"xn--mgbc0a9azcg", "xn--mgberp4a5d4ar", "xn--o3cw4h",
			"xn--ogbpf8fl", "xn--p1ai", "xn--pgbs0dh", "xn--s9brj9c",
			"xn--wgbh1c", "xn--wgbl6a", "xn--xkc2al3hye2a",
			"xn--xkc2dl3a5ee0h", "xn--yfro4i67o", "xn--ygbi2ammx",
			"xn--zckzah", "xxx", "ye", "yt", "za", "zm", "zw" };
	ArrayList<String> TLDs = new ArrayList<String>();
	boolean blacklistUrl = false;
	int addbookmark;
	int exIndex = 0;
	int back = 0;
	private Context mContext;
	String adressUrl;
	String addressbarText;
	
     
    View header;
	private MyWebChromeClient mWebChromeClient;
	private CordovaInterface cordova;
	private boolean fullscreenStatus=false;
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// hiding the default title bar
		super.setBooleanProperty("showTitle", false);
		super.onCreate(savedInstanceState);
		this.mContext = getApplicationContext();
		 header = View
				.inflate(getContext(), R.layout.sb_header, null);
		View header2 = View.inflate(getContext(), R.layout.sb_subheader, null);
		root.addView(header);

		tempview = (CordovaWebView) header.findViewById(R.id.webView1);

		super.init();
		ObjActivity = this;
		popupWindow = new PopupWindow(header2, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		parseProfilesJson();
		appView.addJavascriptInterface(this, "SB_MainActivity");
		appView.setFilterTouchesWhenObscured(true) ;
		// loading the default html file at first
		// haveNetworkConnection()

		MarginLayoutParams marginsParams = new MarginLayoutParams(
				tempview.getLayoutParams());
		marginsParams.setMargins(0, -2, 0, 0);
		RelativeLayout.LayoutParams lp = new LayoutParams(marginsParams);
		tempview.setLayoutParams(lp);
		tempview.setBackgroundColor(0);

		WebSettings tempsett = tempview.getSettings();

		tempsett.setSupportZoom(true);
		tempsett.setBuiltInZoomControls(true);
		tempsett.setUseWideViewPort(true);
		tempsett.setLoadWithOverviewMode(true);
		tempview.addJavascriptInterface(this, "SB_MainActivity");
		tempsett.setGeolocationEnabled(false);
		tempsett.setSavePassword(false);
		tempsett.setSaveFormData(false);

		super.setIntegerProperty("splashscreen", R.drawable.sb_splash);
		if (!(Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR1 || Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR2)) {
			tempview.setWebChromeClient(new WebChromeClient());
		}
		tempview.loadUrl(tempURL);

		if (isConnectingToInternet()) {
			if (homeURL != "") {
				if (!(homeURL.startsWith("http://") || homeURL
						.startsWith("https://"))) {
					homeURL = "http://" + homeURL;
				}
				super.loadUrl(homeURL, 2000);
			} else {
				super.loadUrl(baseURL, 2000);
				SB_Log.sbD("Base URL", baseURL);
			}

		} else {
			super.loadUrl("file:///android_asset/www/error.html?nointernet",2000);
		}

		// controlling of the zoom effects
		WebSettings settings = this.appView.getSettings();
		settings.setAllowFileAccess(true);
		settings.setDatabaseEnabled(true);
		settings.setDatabasePath("/data/data/"
				+ appView.getContext().getPackageName() + "/databases/");
		settings.setDomStorageEnabled(true);
		settings.setGeolocationEnabled(false);

		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		// settings.setDisplayZoomControls(false);
		settings.setSavePassword(false);
		settings.setSaveFormData(false);

		refresh = (ImageView) findViewById(R.id.refresh);
		stop = (ImageView) findViewById(R.id.stop);
		headerlayout = (RelativeLayout) findViewById(R.id.headertop);
		findlayout = (RelativeLayout) findViewById(R.id.findinpagelayout);
		browserhead = (RelativeLayout) findViewById(R.id.browserheader);
		findtext = (EditText) findViewById(R.id.findinpagetext);
		settingsPage = (TextView) header2.findViewById(R.id.settings);
		proxysetting = (TextView) header2.findViewById(R.id.proxysetting);
		rightarrow = (ImageView) header2.findViewById(R.id.rightarrow);
		leftarrow = (ImageView) header2.findViewById(R.id.leftarrow);
		leftArrowDisable = (ImageView) header2
				.findViewById(R.id.leftarrowdisable);
		rightArrowDisable = (ImageView) header2
				.findViewById(R.id.rightarrowdisable);
		homePage = (TextView) header2.findViewById(R.id.home);
		downloadPage = (ImageView) header2.findViewById(R.id.download);
		bookmarkfav = (ImageView) header2.findViewById(R.id.favorite);
		bookmarkfav_enabled = (ImageView) header2.findViewById(R.id.favorite_selected);
		stopLoad = (ImageView) findViewById(R.id.stopload);
		//ontouch of webview hiding the popup window and keyboard
		addressbar = (EditText) findViewById(R.id.titlebar);

		this.appView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				Closepopup();
				return false;
			}
		});
		appView.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					SB_Log.sbD("back1", "back1" + back);
					if (back != 1) {
						deviceback();
						back++;
					} else if (back == 1) {
						back = 0;
					}
					return true;
				}
				return onKeyDown(keyCode, event);
			}

		});

		Bundle objExtras = getIntent().getExtras();
		strOtherAppURL = objExtras.getString("toBrowser");
		if (strOtherAppURL != null) {
			//exIndex = 1;
			try {
				if (!(strOtherAppURL.startsWith("http://") || strOtherAppURL
						.startsWith("https://"))) {
					strOtherAppURL = "http://" + strOtherAppURL;
				}
				ObjActivity
						.loadUrl("file:///android_asset/www/Main.html?redirect-page&"
								+ encriptKey + "&" + dbName + "&" + strOtherAppURL);
				//TODO defer the call to searchquery until the js file loads
			} catch (Exception objException) {
				SB_Log.sbD(":onMessage", objException.toString());
			}
			addressbar.setText(strOtherAppURL);
			// ObjActivity.loadUrl("javascript:searchQuery('" + strOtherAppURL +
			// "',"+newTabStatus+")");
		}
		CordovaWebViewClient webViewClient1 = null;

		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			webViewClient1 = new CordovaWebViewClient(this) {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					WebStorage wSt = WebStorage.getInstance();
			        wSt.deleteAllData();
					clearLocalStorage();

					
					// TODO Auto-generated method stub
					super.onPageStarted(view, url, favicon);
					tempview.loadUrl(tempURL);
				}
	
				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					SB_Log.sbD("url", "url" + url);
					if (!(url == null || url == "" || (url
							.equalsIgnoreCase("about:blank")))) {
						if (!(url.startsWith("file:///"))) {
							SB_Log.sbD("url", "url" + url);
							tempview.loadUrl("javascript:AddToHistory('" + url
									+ "','" + url + "','" + encriptKey + "','"
									+ dbName + "')");
						}
	
						if (homeurlflag) {
							SB_Log.sbD("homeURL", "homeurlflag");
	
							tempview.loadUrl("javascript:definitDatabase('"
									+ homeURL + "','" + "true" + "','" + encriptKey
									+ "','" + dbName + "')");
							homeurlflag = false;
						}
	
						if (strOtherAppURL != null) {
							tempview.loadUrl("javascript:AddToHistory('" + url
									+ "','" + url + "','" + encriptKey + "','"
									+ dbName + "')");
							strOtherAppURL = null;
						}
						SB_Log.sbI("TEST", "onPageFinished: " + url);
					}
				}
				
				@Override
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
					SB_Log.sbI("SSLERROR", "error code" + errorCode);
				}
	
				@Override
				public void onReceivedSslError(WebView view,
						final SslErrorHandler handler, SslError error) {
					SB_Log.sbI("SSLERROR", "error code" + error.toString());
					AlertDialog.Builder builder = new AlertDialog.Builder(
							view.getContext());
	
					builder.setTitle("Invalid Certificate")
							.setMessage(
									"Proceeding to this site puts your confidential information at risk. Proceed anyway?");
	
					// Add the buttons
					builder.setPositiveButton("Proceed",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									handler.proceed();
								}
							});
					builder.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									// User cancelled the dialog
	
								}
							});
	
					AlertDialog dialog = builder.create();
					dialog.show();
	
				}
	
				@Override
				public void onReceivedHttpAuthRequest(WebView view,
						final HttpAuthHandler handler, final String host,
						final String realm) {
					String username = null;
					String password = null;
					SB_Log.sbI("HOST", "ON RECEIVED HTTP AUTH HOST1: " + host);
					boolean reuseHttpAuthUsernamePassword = handler
							.useHttpAuthUsernamePassword();
	
					SB_Log.sbI("AUTH", "" + reuseHttpAuthUsernamePassword);
	
					if (reuseHttpAuthUsernamePassword && view != null) {
						String[] credentials = view.getHttpAuthUsernamePassword(
								host, realm);
						if (credentials != null && credentials.length == 2) {
							username = credentials[0];
							password = credentials[1];
						}
					}
	
					if (username != null && password != null) {
						handler.proceed(username, password);
					} else {
						// if (view) {
	
						SB_HttpAuthenticationDialog dialog = new SB_HttpAuthenticationDialog(
								view.getContext(), host, realm);
						SB_Log.sbI("HOST", "ON RECEIVED HTTP AUTH HOST2: " + host);
						dialog.setOkListener(new SB_HttpAuthenticationDialog.OkListener() {
							public void onOk(String host, String realm,
									String username, String password) {
								SB_Log.sbI("MAIN1 ", "DATA " + username);
								handler.proceed(username, password);
							}
						});
	
						dialog.setCancelListener(new SB_HttpAuthenticationDialog.CancelListener() {
							public void onCancel() {
								handler.cancel();
							}
						});
	
						dialog.show();
						/*
						 * } else { handler.cancel(); }
						 */
					}
				}
			};
		}
		else{
			webViewClient1 = new IceCreamCordovaWebViewClient(this) {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					// TODO Auto-generated method stub
					super.onPageStarted(view, url, favicon);
					tempview.loadUrl(tempURL);
				}
	
				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					SB_Log.sbD("url", "url" + url);
					if (!(url == null || url == "" || (url
							.equalsIgnoreCase("about:blank")))) {
						if (!(url.startsWith("file:///"))) {
							SB_Log.sbD("url", "url" + url);
							tempview.loadUrl("javascript:AddToHistory('" + url
									+ "','" + url + "','" + encriptKey + "','"
									+ dbName + "')");
						}
	
						if (homeurlflag) {
							SB_Log.sbD("homeURL", "homeurlflag");
	
							tempview.loadUrl("javascript:definitDatabase('"
									+ homeURL + "','" + "true" + "','" + encriptKey
									+ "','" + dbName + "')");
							homeurlflag = false;
						}
	
						if (strOtherAppURL != null) {
							tempview.loadUrl("javascript:AddToHistory('" + url
									+ "','" + url + "','" + encriptKey + "','"
									+ dbName + "')");
							strOtherAppURL = null;
						}
						SB_Log.sbI("TEST", "onPageFinished: " + url);
					}
				}
	
				@Override
				public void onReceivedSslError(WebView view,
						final SslErrorHandler handler, SslError error) {
					SB_Log.sbI("SSLERROR", "error code" + error.toString());
					AlertDialog.Builder builder = new AlertDialog.Builder(
							view.getContext());
	
					builder.setTitle("Invalid Certificate")
							.setMessage(
									"Proceeding to this site puts your confidential information at risk. Proceed anyway?");
	
					// Add the buttons
					builder.setPositiveButton("Proceed",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									handler.proceed();
								}
							});
					builder.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									// User cancelled the dialog
	
								}
							});
	
					AlertDialog dialog = builder.create();
					dialog.show();
	
				}
	
				@Override
				public void onReceivedHttpAuthRequest(WebView view,
						final HttpAuthHandler handler, final String host,
						final String realm) {
					String username = null;
					String password = null;
					SB_Log.sbI("HOST", "ON RECEIVED HTTP AUTH HOST1: " + host);
					boolean reuseHttpAuthUsernamePassword = handler
							.useHttpAuthUsernamePassword();
	
					SB_Log.sbI("AUTH", "" + reuseHttpAuthUsernamePassword);
	
					if (reuseHttpAuthUsernamePassword && view != null) {
						String[] credentials = view.getHttpAuthUsernamePassword(
								host, realm);
						if (credentials != null && credentials.length == 2) {
							username = credentials[0];
							password = credentials[1];
						}
					}
	
					if (username != null && password != null) {
						handler.proceed(username, password);
					} else {
						// if (view) {
	
						SB_HttpAuthenticationDialog dialog = new SB_HttpAuthenticationDialog(
								view.getContext(), host, realm);
						SB_Log.sbI("HOST", "ON RECEIVED HTTP AUTH HOST2: " + host);
						dialog.setOkListener(new SB_HttpAuthenticationDialog.OkListener() {
							public void onOk(String host, String realm,
									String username, String password) {
								SB_Log.sbI("MAIN1 ", "DATA " + username);
								handler.proceed(username, password);
							}
						});
	
						dialog.setCancelListener(new SB_HttpAuthenticationDialog.CancelListener() {
							public void onCancel() {
								handler.cancel();
							}
						});
	
						dialog.show();
						/*
						 * } else { handler.cancel(); }
						 */
					}
				}
			};
		}
		
		//if (!(Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR1 || Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR2)) {
			webViewClient1.setWebView(this.appView);
			this.appView.setWebViewClient(webViewClient1);
		//}
		//if (!(Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR1 || Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR2)) {
			mWebChromeClient = new MyWebChromeClient(this.cordova, appView);
			this.appView.setWebChromeClient(mWebChromeClient);
		//}

		// file download
		this.appView.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				
				SB_Log.sbD("Content Disposition","contentDisposition:"+contentDisposition+"****mimetype:" + mimetype + "content length:" + contentLength + "userAgent" + userAgent );
				new SB_DownloadFile(mContext).execute(url);
			}
		});

		// tabcount.setText(String.valueOf(tabadded));
		addressbar.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		findtext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {				
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {					
					SB_Log.sbD("search", "search");
					// hide virtual keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(addressbar.getWindowToken(),
							InputMethodManager.RESULT_UNCHANGED_SHOWN);
					match_flag = true;
					searchText();
					return true;
				}
				return false;
			}
		});
		findtext.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				searchText();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		addressbar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					stop.setVisibility(View.VISIBLE);
					refresh.setVisibility(View.INVISIBLE);
					Closepopup();
				} else {
					if (addressbar.getText().toString().equals("")) {
						addressbar.setText(adressUrl);
					}
					refresh.setVisibility(View.VISIBLE);
					stop.setVisibility(View.INVISIBLE);

				}
			}
		});

		// URL functionality
		addressbar.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						boolean connect;
						addressbarText= addressbar.getText().toString();
						
						Boolean isProxyEnabled = false;
						if(PersonaMainActivity.isRovaPoliciesOn){
						ExternalAdapterRegistrationClass extnAdapClass =  ExternalAdapterRegistrationClass.getInstance(context);
						isProxyEnabled = extnAdapClass.getExternalPIMSettingsInfo().bisProxyBasedRoutingEnabled;
						SB_Log.sbD("proxy settings from extnAdapClass","" + extnAdapClass.getExternalPIMSettingsInfo().bisProxyBasedRoutingEnabled);
						}
						if(isProxyEnabled==null){
							isProxyEnabled=false;
						}
						SB_Log.sbD("proxy settings","" + isProxyEnabled);
						
						if (isProxyEnabled) {
							connect = haveNetworkConnection();
						} else {
							connect = isConnectingToInternet();
						}
						if (connect) {
							tileString = addressbar.getText().toString();
							if (tileString.trim().length() > 0) {
								if (!(tileString.startsWith("http://") || tileString
										.startsWith("https://"))) {
									tileString = "http://" + tileString;
								}
								try {
									ObjActivity
											.loadUrl("file:///android_asset/www/Main.html?dbcall&"
													+ encriptKey
													+ "&"
													+ dbName
													+ "");
									Thread.sleep(2000);
								} catch (Exception e) {
									e.printStackTrace();
								}
								//if (Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR1
										//|| Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR2) {
									SB_Log.sbD("version"
											+ Build.VERSION.SDK_INT, "version");
									version4 = true;
								//}
								ObjActivity.loadUrl("javascript:searchQuery('"
										+ tileString + "'," + version4 + ",'"
										+ encriptKey + "','" + dbName + "')");
								version4 = false;

							} else {
								SB_Log.sbD("previous page", "");
							}
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									addressbar.getWindowToken(), 0);
						} else {
							ObjActivity
									.loadUrl("file:///android_asset/www/error.html?nointernet");
						}
						return true;
					default:
						break;
					}
				}
				return false;
			}
		});
		// proxy
		proxysetting.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				proxyStatus = true;
				Closepopup();
			}
		});
		settingsPage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Closepopup();
				ObjActivity
						.loadUrl("file:///android_asset/www/Main.html?settings-page&"
								+ encriptKey + "&" + dbName + "");
			}
		});
		homePage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Closepopup();
				click_home = true;
				ObjActivity.loadUrl("" + baseURL + "");
			}
		});
		downloadPage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Closepopup();
				Intent launchActivity = new Intent(mContext,
						AttachmentListActivity.class);
				launchActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				launchActivity.putExtra("FromBrowser", true);
				startActivity(launchActivity);
			}
		});
		stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				adressUrl = addressbar.getText().toString();
				addressbar.setText("");
			}
		});
		stopLoad.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub				
				ObjActivity.appView.stopLoading();				
				stopLoad.setVisibility(View.INVISIBLE);
				refresh.setVisibility(View.VISIBLE);
			}
		});

		final ImageView more = (ImageView) findViewById(R.id.more);
		more.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				WebBackForwardList wbf = appView.copyBackForwardList();
				int curIndex = wbf.getCurrentIndex();
				curIndex = curIndex + exIndex;
				addbookmark = SB_ClearBrowsingData.addbookmarkflag;
				if (appView.getUrl().equalsIgnoreCase(
						"file:///android_asset/www/Main.html?add-bookmarks-page&"
								+ encriptKey + "&" + dbName + "")) {
					findinpages.setVisibility(View.GONE);
				} else {
					findinpages.setVisibility(View.VISIBLE);
				}
				if (isOpen)
					popupWindow.dismiss();
				else
					popupWindow.showAsDropDown(more, 10, 10);
				isOpen = !isOpen;
				if (curIndex == 1) {
					leftarrow.setVisibility(View.INVISIBLE);
					leftArrowDisable.setVisibility(View.VISIBLE);
				} else {
					leftArrowDisable.setVisibility(View.INVISIBLE);
					leftarrow.setVisibility(View.VISIBLE);
				}
				if (appView.canGoForward()) {
					rightArrowDisable.setVisibility(View.INVISIBLE);
					rightarrow.setVisibility(View.VISIBLE);
				} else {
					rightarrow.setVisibility(View.INVISIBLE);
					rightArrowDisable.setVisibility(View.VISIBLE);
				}
				if (addbookmark == 1
						&& !(appView.getUrl().startsWith("file://"))) {
					bookmarkfav.setVisibility(View.INVISIBLE);
					bookmarkfav_enabled.setVisibility(View.VISIBLE);
				} else {
					bookmarkfav_enabled.setVisibility(View.INVISIBLE);
					bookmarkfav.setVisibility(View.VISIBLE);
				}
			}
		});
		ImageView connectApp = (ImageView) findViewById(R.id.connectApp);
		connectApp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ObjActivity, PersonaLauncher.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				// ObjActivity.finish();

			}
		});

		// Title Refresh
		refresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Closepopup();
				boolean connect1;
				addressbarText= addressbar.getText().toString();
				if (proxyStatus) {
					connect1 = haveNetworkConnection();
				} else {
					connect1 = isConnectingToInternet();
				}
				if (connect1) {

					SB_Log.sbD("str", "str"
							+ addressbar.getText().toString().equals(""));
					if ((!((appView.getUrl()).startsWith("file:///")) || !(addressbar
							.getText().toString().equals("")))) {
						// tileString = addressbar.getText().toString();
						String reloadurl = addressbar.getText().toString();
						SB_Log.sbD("url" + addressbar.getText(), "url"
								+ addressbar.getText());
						if (!(reloadurl.startsWith("http://") || reloadurl
								.startsWith("https://"))) {
							reloadurl = "http://" + reloadurl;
						}
						if ((appView.getUrl())
								.startsWith("file:///android_asset/www/error.html")) {
							ObjActivity.loadUrl("javascript:searchQuery('"
									+ reloadurl + "'," + version4 + ")");
						} else {
							ObjActivity.loadUrl(reloadurl);
						}
					} else {
						ObjActivity.loadUrl(appView.getUrl());
					}

				} else {
					ObjActivity
							.loadUrl("file:///android_asset/www/error.html?nointernet");
				}
				if (!((appView.getUrl()).startsWith("file:///"))) {
					addressbar.setText(appView.getUrl());
				}
			}
		});
		// Menu Left Arrow
		/*
		leftArrowDisable.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ObjActivity.finish();
			}
		});
		*/
		leftarrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Closepopup();
				if (blacklistUrl || click_home) {
					appView.backHistory();
					blacklistUrl = false;
					click_home = false;
				}
				appView.backHistory();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String chkurl = appView.getUrl();
				if (!(chkurl.startsWith("file:///"))) {
					addressbar.setText(chkurl);
				}
				if (chkurl
						.equalsIgnoreCase("file:///android_asset/www/Main.html?dbcall&"
								+ encriptKey + "&" + dbName + "")) {
					appView.backHistory();
				}
			}
		});
		// Menu Right Arrow.
		rightarrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Closepopup();
				appView.goForward();
				try {
					Thread.sleep(3000);
					String furl = appView.getUrl();
					if (!(furl.startsWith("file:///"))) {
						addressbar.setText(furl);
					}
					if (furl.equalsIgnoreCase("file:///android_asset/www/Main.html?dbcall&"
							+ encriptKey + "&" + dbName + "")) {
						appView.goForward();
					}

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		// Menu BookMark
		bookmarkfav.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Closepopup();
				String url1 = appView.getUrl();
				String pagetitle = appView.getTitle();
				if (url1 == "" || url1 == null) {
					url1 = "";
					pagetitle = "";
				}
				try {
					ObjActivity
							.loadUrl("file:///android_asset/www/Main.html?add-bookmarks-page&"
									+ encriptKey + "&" + dbName + "");
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				addressbar.setText("");
				ObjActivity.loadUrl("javascript:bookMark('" + url1 + "')");
				ObjActivity.loadUrl("javascript:bookMarkTitle('" + pagetitle
						+ "')");
			}
		});
		bookmarkfav_enabled.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Closepopup();
				String url1 = appView.getUrl();
				String pagetitle = appView.getTitle();
				try {
					ObjActivity
							.loadUrl("file:///android_asset/www/Main.html?add-bookmarks-page&"
									+ encriptKey + "&" + dbName + "");
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				addressbar.setText("");
				ObjActivity.loadUrl("javascript:bookMark('" + url1 + "')");
				ObjActivity.loadUrl("javascript:bookMarkTitle('" + pagetitle
						+ "')");
			}
		});
		// Menu BookMark
		bookmarks = (TextView) header2.findViewById(R.id.bookmarks);
		bookmarks.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Closepopup();
				addressbar.setText("");
				ObjActivity
						.loadUrl("file:///android_asset/www/Main.html?view-bookmarks-page&"
								+ encriptKey + "&" + dbName + "");
				// ObjActivity.loadUrl("javascript:checkpage('viewbookmark','true')");
			}
		});
		bookmarks.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_ENTER) {
					bookmarks.setBackgroundColor(0x808080);
					return true;
				} else {
					bookmarks.setBackgroundColor(0);
				}
				return false;
			}
		});

		// Menu History
		history = (TextView) header2.findViewById(R.id.history);
		history.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Closepopup();
				addressbar.setText("");
				ObjActivity
						.loadUrl("file:///android_asset/www/Main.html?history-page&"
								+ encriptKey + "&" + dbName + "");
			}
		});
		history.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_ENTER) {
					history.setBackgroundColor(0x808080);
					return true;
				} else {
					history.setBackgroundColor(0x0);
				}
				return false;
			}
		});

		// To cancel the search
		ImageView findcancel = (ImageView) findViewById(R.id.findcancel);
		findcancel.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				headerlayout.setVisibility(View.VISIBLE);
				findlayout.setVisibility(View.GONE);
				headerlayout.invalidate();
				RelativeLayout address = (RelativeLayout) findViewById(R.id.addressbarlayout);
				address.invalidate();
				UpdateURL();
				appView.findAll("adadjajsdhjsdfhjsdfhsjfhjsafadadjajsdhjsdfhjsdfhsjfhjsafadadjajsdhjsdfhjsdfhsjfhjsafadadjajsdhjsdfhjsdfhsjfhjsaf");
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(findtext.getWindowToken(),
						InputMethodManager.RESULT_UNCHANGED_SHOWN);
				//
				// //appView.requestFocus();
				// //v.requestFocus();
				// //
				// appView.setDescendantFocusability(appView.FOCUS_BEFORE_DESCENDANTS);
			}
		});
		// Menu find in pages
		findinpages = (TextView) header2.findViewById(R.id.findinpages);
		findinpages.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Closepopup();
				String URLAddress = appView.getUrl();
				if (URLAddress.startsWith("file:///"))
					URLAddress = "";
				headerlayout.setVisibility(View.GONE);
				findlayout.setVisibility(View.VISIBLE);
				// searchText();
				findtext.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(findtext, InputMethodManager.SHOW_IMPLICIT);
			}
		});
	}

	@Override
	protected void onPause() {
		Log.d("pause", "................");
		super.onPause();
	}

	@Override  
	protected void onStop() {
		Log.d("stop", "................");
		this.appView.stopLoading();
		super.onStop();  
	}


	@Override
	protected void onResume() {
		Log.d("Resuming", "................"+fullscreenStatus);
		try {
			if(fullscreenStatus){
				header.setVisibility(View.VISIBLE);
			appView.hideCustomView();
			fullscreenStatus=false;
			}
			super.onResume();
		} catch (NullPointerException e) {

		}

	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("keydown", "................");
		
		if (keyCode == KeyEvent.KEYCODE_BACK && fullscreenStatus) {
			header.setVisibility(View.VISIBLE);
			fullscreenStatus=false;
		}
		return super.onKeyDown(keyCode, event);
	}
	public void parseProfilesJson() {

		try {
			InputStream is = getResources().getAssets().open(
					"www/blacklist.json");
			int size = is.available();
			byte buffer[] = new byte[size];
			is.read(buffer);
			is.close();
			String text = new String(buffer);
			JSONObject jsonObject;
			jsonObject = new JSONObject(text);
			for (int i = 1; i <= jsonObject.length(); i++) {
				// Log.d("black", "black"+jsonObject.getString(""+i+""));
				blacklist.add(jsonObject.getString("" + i + ""));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deviceback() {
		if (blacklistUrl || click_home) {
			appView.backHistory();
			blacklistUrl = false;
			click_home = false;
		}
		WebBackForwardList wbf = appView.copyBackForwardList();
		int curIndex = wbf.getCurrentIndex();
		curIndex = curIndex + exIndex;
		if (curIndex == 1) {
			ObjActivity.finish();
		}
		appView.backHistory();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String chkurl = appView.getUrl();
		if (!(chkurl.startsWith("file:///"))) {
			addressbar.setText(chkurl);
		}
		if (chkurl
				.equalsIgnoreCase("file:///android_asset/www/Main.html?dbcall&"
						+ encriptKey + "&" + dbName + "")) {
			appView.backHistory();
		}
	}

	public String getDomainName(String url) {

		for (int k = 0; k < TLDs1.length; k++) {
			TLDs.add(TLDs1[k]);
		}
		int n = url.indexOf("/", 8);
		if (n != -1) {
			url = url.substring(0, n);
		}
		String part = null;
		String[] parts = url.split("\\.");
		// Log.d("domain name","domain nameparts"+url.split("\\."));
		if (parts.length != 0) {
			int ln = parts.length;
			int i = ln;
			int j = parts.length - 1;
			int minLength = parts[j].length();
			while (true) {
				part = parts[--i];
				if (part != null) {
					if (TLDs.indexOf(part) < 0 || part.length() < minLength
							|| i < ln - 2 || i == 0) {
						String[] data = part.split("/");
						if (data.length > 0) {
							part = data[data.length - 1];
						}
						break;
					}
				}
			}
		}
		return part;
	}

	// checking the search data and version device
	private void searchText() {
		try {

			if (findtext.getText().toString().equals("")) {
				appView.findAll("adadjajsdhjsdfhjsdfhsjfhjsafadadjajsdhjsdfhjsdfhsjfhjsafadadjajsdhjsdfhjsdfhsjfhjsafadadjajsdhjsdfhjsdfhsjfhjsaf");
			} else {
				int textCount = appView.findAll(findtext.getText().toString());
				// Log.d("test", textCount+"");
				if (match_flag) {
					if (textCount == 0) {
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								getContext());

						// set title
						alertDialogBuilder.setTitle("Alert");

						// set dialog message
						alertDialogBuilder
								.setMessage("NO matches found")
								.setCancelable(false)
								.setPositiveButton("OK",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										});

						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();

						// show it
						alertDialog.show();
					}
				}
				match_flag = false;
			}

			Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
			m.setAccessible(true);
			m.invoke(appView, true);

		} catch (Exception ignored) {

			Log.d("test", "exception");

		}
	}

	// closing the popup
	private void Closepopup() {		
		popupWindow.dismiss();
		isOpen = false;
	}

	// update the url in serachbox
	public void UpdateURL() {
		String URL = appView.getUrl();
		String protocal = null;
		if (URL != null) {
			if (URL.startsWith("http://")) {
				protocal = "http://";
			} else if (URL.startsWith("https://")) {
				protocal = "https://";
			}
			String getDomainName = getDomainName(URL);
			getDomainName = protocal + "www." + getDomainName + ".com";

			if (blacklist.contains(getDomainName)) {
				blacklistUrl = true;
				tileString = URL;
				ObjActivity
						.loadUrl("file:///android_asset/www/error.html?blockedsite");
			} else {
				if (!URL.startsWith("file:///")) {
					addressbar.setText(URL);
					url1 = URL;
				} else if (URL.startsWith("file:///android_asset/www/error.html")) {
					blacklistUrl = true;
					addressbar.setText(addressbarText);
				}else if (URL.startsWith("file:///android_asset/www/Main.html?dbcall&"+ encriptKey+ "&"+ dbName+ "")){
					SB_Log.sbD("urank1", "urank1"+addressbarText);
					addressbar.setText(addressbarText);
				}else {
					addressbar.setText("");
				}
			}
		}
	}

	// Check the network connectivity
	public boolean isConnectingToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}

		}
		return false;
	}

	private boolean haveNetworkConnection() {
		final String DEBUG_TAG = "INTERNET";
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;
		if(PersonaMainActivity.isRovaPoliciesOn){
		ExternalAdapterRegistrationClass extnAdapClass =  ExternalAdapterRegistrationClass.getInstance(context);
		SB_Log.sbD("proxy settings",extnAdapClass.getExternalProxySettingsInfo().get("ProxyServerName"));
		SB_Log.sbD("proxy settings",extnAdapClass.getExternalProxySettingsInfo().get("ProxyServerPortNumber"));
		SB_Log.sbD("proxy settings", "is proxy based routing enabled=====" + extnAdapClass.getExternalPIMSettingsInfo().bisProxyBasedRoutingEnabled);
		
		Boolean isProxyEnabled = extnAdapClass.getExternalPIMSettingsInfo().bisProxyBasedRoutingEnabled;
		String ProxyServerName = extnAdapClass.getExternalProxySettingsInfo().get("ProxyServerName");
		String ProxyServerPortNumber =  extnAdapClass.getExternalProxySettingsInfo().get("ProxyServerPortNumber");
		if(isProxyEnabled==null){
			isProxyEnabled=false;
			ProxyServerName="";
			ProxyServerPortNumber="";
		}
		
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
				if (ni.isConnected()) {
					haveConnectedWifi = true;
					if(isProxyEnabled){
					new SB_MyProxySettings().setProxy(getApplicationContext(),
							ProxyServerName, Integer.parseInt(String.valueOf(ProxyServerPortNumber)));
					}
					SB_Log.sbD(DEBUG_TAG, "Wifi connected: "
							+ haveConnectedWifi);
				}
			}
			if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
				if (ni.isConnected()) {
					haveConnectedMobile = true;
					SB_Log.sbD(DEBUG_TAG, "Mobile connected: "
							+ haveConnectedMobile);
				}
			}
		}
	}

		return haveConnectedWifi || haveConnectedMobile;
	}

	public static Object getField(Object obj, String name)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		Field f = obj.getClass().getField(name);
		Object out = f.get(obj);
		return out;
	}

	public static Object getDeclaredField(Object obj, String name)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		Field f = obj.getClass().getDeclaredField(name);
		f.setAccessible(true);
		Object out = f.get(obj);
		return out;
	}

	public static void setEnumField(Object obj, String value, String name)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		Field f = obj.getClass().getField(name);
		f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
	}

	public static void setProxySettings(String assign,
			WifiConfiguration wifiConf) throws SecurityException,
			IllegalArgumentException, NoSuchFieldException,
			IllegalAccessException {
		setEnumField(wifiConf, assign, "proxySettings");
	}

	WifiConfiguration GetCurrentWifiConfiguration(WifiManager manager) {
		if (!manager.isWifiEnabled())
			return null;

		List<WifiConfiguration> configurationList = manager
				.getConfiguredNetworks();
		WifiConfiguration configuration = null;
		int cur = manager.getConnectionInfo().getNetworkId();
		for (int i = 0; i < configurationList.size(); ++i) {
			WifiConfiguration wifiConfiguration = configurationList.get(i);
			if (wifiConfiguration.networkId == cur)
				configuration = wifiConfiguration;
		}

		return configuration;
	}

	void setWifiProxySettings() {
		// get the current wifi configuration
		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiConfiguration config = GetCurrentWifiConfiguration(manager);
		if (null == config)
			return;

		try {
			// get the link properties from the wifi configuration
			Object linkProperties = getField(config, "linkProperties");
			if (null == linkProperties)
				return;

			// get the setHttpProxy method for LinkProperties
			Class proxyPropertiesClass = Class
					.forName("android.net.ProxyProperties");
			Class[] setHttpProxyParams = new Class[1];
			setHttpProxyParams[0] = proxyPropertiesClass;
			Class lpClass = Class.forName("android.net.LinkProperties");
			Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy",
					setHttpProxyParams);
			setHttpProxy.setAccessible(true);

			// get ProxyProperties constructor
			Class[] proxyPropertiesCtorParamTypes = new Class[3];
			proxyPropertiesCtorParamTypes[0] = String.class;
			proxyPropertiesCtorParamTypes[1] = int.class;
			proxyPropertiesCtorParamTypes[2] = String.class;

			Constructor proxyPropertiesCtor = proxyPropertiesClass
					.getConstructor(proxyPropertiesCtorParamTypes);

			// create the parameters for the constructor
			Object[] proxyPropertiesCtorParams = new Object[3];
			proxyPropertiesCtorParams[0] = "proxy.cognizant.com";
			proxyPropertiesCtorParams[1] = 6050;
			proxyPropertiesCtorParams[2] = null;

			// create a new object using the params
			Object proxySettings = proxyPropertiesCtor
					.newInstance(proxyPropertiesCtorParams);

			// pass the new object to setHttpProxy
			Object[] params = new Object[1];
			params[0] = proxySettings;
			setHttpProxy.invoke(linkProperties, params);

			setProxySettings("STATIC", config);

			// save the settings
			manager.updateNetwork(config);
			manager.disconnect();
			manager.reconnect();
		} catch (Exception e) {
		}
	}

	void unsetWifiProxySettings() {
		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiConfiguration config = GetCurrentWifiConfiguration(manager);
		if (null == config)
			return;

		try {
			// get the link properties from the wifi configuration
			Object linkProperties = getField(config, "linkProperties");
			if (null == linkProperties)
				return;

			// get the setHttpProxy method for LinkProperties
			Class proxyPropertiesClass = Class
					.forName("android.net.ProxyProperties");
			Class[] setHttpProxyParams = new Class[1];
			setHttpProxyParams[0] = proxyPropertiesClass;
			Class lpClass = Class.forName("android.net.LinkProperties");
			Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy",
					setHttpProxyParams);
			setHttpProxy.setAccessible(true);

			// pass null as the proxy
			Object[] params = new Object[1];
			params[0] = null;
			setHttpProxy.invoke(linkProperties, params);

			setProxySettings("NONE", config);

			// save the config
			manager.updateNetwork(config);
			manager.disconnect();
			manager.reconnect();
		} catch (Exception e) {
		}
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.sb_main, menu);
	// return true;
	// }

	

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			popupWindow.dismiss();
			isOpen = false;
		} else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			popupWindow.dismiss();
			isOpen = false;

		}

	}

	private class MyWebChromeClient extends CordovaChromeClient {
		public MyWebChromeClient(CordovaInterface ctx, CordovaWebView app) {
			super(ctx, app);
		}

		FrameLayout customComponenet;
		
		
		public void openFileChooser(ValueCallback<Uri> uploadMsg) {
	        this.openFileChooser(uploadMsg, "*/*");
	    }

		
	    public void openFileChooser( ValueCallback<Uri> uploadMsg, String acceptType ) {
	        this.openFileChooser(uploadMsg, acceptType, null);
	    }
	    
		
	    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
	    {
	        mUploadMessage = uploadMsg;
	        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
	        i.addCategory(Intent.CATEGORY_OPENABLE);
	        i.setType("*/*");
	            getActivity().startActivityForResult(Intent.createChooser(i, "File Browser"),
	                FILECHOOSER_RESULTCODE);
	    }
	    
	    public ValueCallback<Uri> getValueCallback() {
	        return this.mUploadMessage;
	    }

		public void onShowCustomView(View view, CustomViewCallback callback) {
			try {
//				header.setVisibility(View.GONE);
//				fullscreenStatus = true;
				super.onShowCustomView(view, callback);

				if (view instanceof FrameLayout) {
					customComponenet.addView(view,
							new FrameLayout.LayoutParams(
									ViewGroup.LayoutParams.FILL_PARENT,
									ViewGroup.LayoutParams.FILL_PARENT,
									Gravity.CENTER));
					customComponenet.setVisibility(View.VISIBLE);
				}
			} catch (NullPointerException e) {

			}
		}

		@Override
		public void onHideCustomView() {
			Log.d("onHideCustomView", "onHideCustomView");
//			header.setVisibility(View.VISIBLE);
//			fullscreenStatus=false;
			try {
				// TODO Auto-generated method stub
				super.onHideCustomView();
			} catch (NullPointerException e) {

			}
		}

	

//		@Override
//		public View getVideoLoadingProgressView() {
//			Log.d("video loading", "video loading");
//			try{
//				Log.d("video loading", "try");
//			return super.getVideoLoadingProgressView();
//			}catch(NullPointerException e) {
//				Log.d("video loading", "catch");
//				LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
//                View mVideoProgressView = inflater.inflate(R.layout.video_loading_progress, null);
//                return mVideoProgressView;
//			}
//		}

		final ProgressBar progessBar1 = (ProgressBar) findViewById(R.id.prograssbar);

		@Override
		public void onProgressChanged(WebView view, int progress) {

			progessBar1.setProgress(progress * 1000);
			if (progress < 100
					&& progessBar1.getVisibility() == ProgressBar.GONE) {
				progessBar1.setVisibility(ProgressBar.VISIBLE);
				refresh.setVisibility(View.INVISIBLE);
				stopLoad.setVisibility(View.VISIBLE);	
			}
			if (progress > 15 && progress < 40) {

			}
			progessBar1.setProgress(progress);
			if (progress == 100) {
				progessBar1.setVisibility(ProgressBar.GONE);
				stopLoad.setVisibility(View.INVISIBLE);
				stop.setVisibility(View.INVISIBLE);
				refresh.setVisibility(View.VISIBLE);
			}
			UpdateURL();
		}

		// @Override
		// public void onProgressChanged(WebView view, int newProgress) {
		// ((Activity) mContext).getWindow().setFeatureInt(
		// Window.FEATURE_PROGRESS, newProgress * 100);
		// }

	}
	
	public void clearLocalStorage(){
        File cache = this.getCacheDir();
  File appDir = new File(cache.getParent());
 
  if (appDir.exists()) {
      String[] children = appDir.list();
      for (String s : children) {
         if (s.equals("app_database")) {
               appDir = new File(appDir, s);
               if (appDir.exists()) {
                      String[] children1 = appDir.list();
                      for(String l: children1){
                            if(l.equals("localstorage")){
                                   appDir=new File(appDir,l);
                                   deleteDir(appDir);
                           
                            }
                      }
               }
         }
      }
  }
        Log.d("message", appDir.toString());
       
 }

 public  boolean deleteDir(File dir)
 {
     if (dir != null && dir.isDirectory()) {
         String[] children = dir.list();
         for (int i = 0; i < children.length; i++) {
             boolean success = deleteDir(new File(dir, children[i]));
             if (!success) {
                 return false;
             }
         }
     }
     return dir.delete();
 }
 
//new code
	public void updateMastertable() {
		// String blockallow1 = null,blockallow2=null;

		try {
			// dbUser.open();
			InputStream is = getResources().getAssets().open(
					"www/masterlist.json");
			int size = is.available();
			byte buffer[] = new byte[size];
			is.read(buffer);
			is.close();
			String text = new String(buffer);
			JSONObject jsonObject;
			jsonObject = new JSONObject(text);

			/*
			 * Cursor c1 =
			 * getApplicationContext().getContentResolver().query(MASTER_URI,
			 * null, where, args, null); if(c1!=null){ if (c1.moveToFirst()) {
			 * blockallow1
			 * =c1.getString(c1.getColumnIndex(SB_BrowserDBAdapter.MASTER_COL2
			 * )); } c1.close(); }
			 */
			for (int i = 1; i <= jsonObject.length() / 2; i++) {
				ContentValues master_initialValues = new ContentValues();
				master_initialValues.put(SB_BrowserDBAdapter.MASTER_COL1,
						jsonObject.getString("" + i + ""));
				master_initialValues.put(SB_BrowserDBAdapter.MASTER_COL2,
						jsonObject.getString("" + i + "f"));
				String where1 = SB_BrowserDBAdapter.MASTER_COL1 + " = ?";
				String[] args1 = new String[] { jsonObject.getString("" + i
						+ "") };
				Cursor cur = getApplicationContext().getContentResolver()
						.query(MASTER_URI, null, where1, args1, null);

				if (cur != null) {
					if (cur.getCount() > 0) {
						SB_Log.sbE("UPDATE MASTER" + i, ">0");
						ContentValues master_updateValues = new ContentValues();
						master_updateValues.put(
								SB_BrowserDBAdapter.MASTER_COL2,
								jsonObject.getString("" + i + "f"));
						String[] args2 = new String[] { jsonObject.getString(""
								+ i + "") };
						String where2 = SB_BrowserDBAdapter.MASTER_COL1
								+ " = ?";
						int d = getApplicationContext().getContentResolver()
								.update(MASTER_URI, master_updateValues,
										where2, args2);

						SB_Log.sbE("UPDATE MASTER" + i, d + "");
					} else {
						SB_Log.sbE("UPDATE MASTER" + i, "<0");
						Uri uri = getApplicationContext().getContentResolver()
								.insert(MASTER_URI, master_initialValues);
						SB_Log.sbE("INSERT MASTER" + i, uri + "");
					}
				}
				cur.close();

			}
			/*
			 * Cursor c =
			 * getApplicationContext().getContentResolver().query(MASTER_URI,
			 * null, where, args, null); if(c!=null){ if (c.getCount() > 0) { if
			 * (c.moveToFirst()) {
			 * blockallow2=c.getString(c.getColumnIndex(SB_BrowserDBAdapter
			 * .MASTER_COL2)); } }c.close();
			 * }if(blockallow1.equals(blockallow2)){ delflag=false;
			 * SB_Log.sbE("URL TABLE DEL FLAG",delflag+""); }else{ delflag=true;
			 * SB_Log.sbE("URL TABLE DEL FLAG",delflag+""); }
			 */
			// dbUser.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateUrlList() {
		try {
			// dbUser.open();

			// if(delflag){
			int del = getApplicationContext().getContentResolver().delete(
					ALLOW_BLOCK_URI, null, null);
			SB_Log.sbD("DELETED", del + "");
			// }
			InputStream is = getResources().getAssets()
					.open("www/urllist.json");
			int size = is.available();
			byte buffer[] = new byte[size];
			is.read(buffer);
			is.close();
			String text = new String(buffer);
			JSONObject jsonObject;
			jsonObject = new JSONObject(text);
			for (int i = 1; i <= jsonObject.length(); i++) {
				ContentValues url_initialValues = new ContentValues();
				String url = jsonObject.getString("" + i + "");
				url_initialValues.put(SB_BrowserDBAdapter.URL_COL1, url);
				if (isIP(url)) {
					url_initialValues.put(SB_BrowserDBAdapter.URL_COL2, "Y");
					String ip[] = url.split("/");
					String net_id = ip[0], mask = ip[1];
					long netID = getNetIP(net_id);
					long mask_val = getMask(Integer.parseInt(mask));
					netID &= mask_val;//added by Naveen to get only the subnet id based on mask value 
					url_initialValues.put(SB_BrowserDBAdapter.URL_COL3, netID);
					url_initialValues.put(SB_BrowserDBAdapter.URL_COL4,
							mask_val);
				} else {
					url_initialValues.put(SB_BrowserDBAdapter.URL_COL2, "N");
				}
				String where = SB_BrowserDBAdapter.URL_COL1 + " = ?";
				String[] whereArgs = new String[] { jsonObject.getString("" + i
						+ "") };
				Cursor cur = getApplicationContext().getContentResolver()
						.query(ALLOW_BLOCK_URI, null, where, whereArgs, null);
				if (cur.getCount() == 0) {
					SB_Log.sbE("INSERT URL AND FLAG" + i, "=0");
					Uri uri = getApplicationContext().getContentResolver()
							.insert(ALLOW_BLOCK_URI, url_initialValues);
					SB_Log.sbD("INSERT URL" + i, uri + "");

				}
				cur.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private long getMask(int mask) {
		long i = (~(0xFFFFFFFFL >> mask) & 0xFFFFFFFFL);
		return i;
	}

	private static long getNetIP(String net_id) {
		long result = 0;
		SB_Log.sbD("blacklist", "string" + net_id);
		String[] atoms = net_id.split("\\.");

		for (int i = 3; i >= 0; i--) {
			result |= (Long.parseLong(atoms[3 - i]) << (i * 8));
		}
		SB_Log.sbD("blacklist", "string" + (result & 0xFFFFFFFF));
		return result & 0xFFFFFFFF;
	}

	private static boolean isIP(String s) {
		String regex = "^((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})(/([0-9]|[0-2][0-9]|3[0-2]))$";
		try {
			Pattern patt = Pattern.compile(regex);
			Matcher matcher = patt.matcher(s);
			return matcher.matches();
		} catch (RuntimeException e) {
			return false;
		}
	}
	
	private static boolean isValidIP(String s){
		String regex = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		try {
			Pattern patt = Pattern.compile(regex);
			Matcher matcher = patt.matcher(s);
			return matcher.matches();
		} catch (RuntimeException e) {
			return false;
		}
	}

	public void updateDatabase() {

		updateMastertable();
		updateUrlList();

	}
	
	public void disableCookies(){
		
	try {
           CookieSyncManager.createInstance(cordova.getActivity());
           CookieManager cm = CookieManager.getInstance();
           if (cm.hasCookies()) {
                  cm.removeSessionCookie();
                  cm.removeAllCookie();
           }
           cm.setAcceptCookie(false);
           SB_Log.sbD("disable", "cookies disabled..");

     }

     catch (Exception ex) {
         SB_Log.sbD("disable", ex.toString());
     }
	}
	public void enableCookies(){
		try {
          CookieSyncManager.createInstance(cordova.getActivity());
          CookieManager cm = CookieManager.getInstance();
          if (!cm.acceptCookie()) {
                 Log.d("enable", "cookies disabled..");
                 Log.d("enable", "enabling cookies..");
                 cm.setAcceptCookie(true);
          } else {
                 Log.d("enable", "cookies already enabled");
          }
    }

    catch (Exception ex) {
          Log.d("enable", ex.toString());
    }
	}

	public void getBWFlag() {
		String where = SB_BrowserDBAdapter.MASTER_COL1 + " = ?";
		
		//get whether url white listed or black listed
		String[] args = { "Block_Allow" };
		Cursor cur = getApplicationContext().getContentResolver().query(
				MASTER_URI, null, where, args, null);
		if (cur != null) {
			if (cur.moveToFirst()) {
				blockallowFlag = cur.getString(cur
						.getColumnIndex(SB_BrowserDBAdapter.MASTER_COL2));
			}
			cur.close();
		}
		
		//get whether file downloads allowed
		args[0] = "Allow_Downloads";
		cur = getApplicationContext().getContentResolver().query(
				MASTER_URI, null, where, args, null);
		if (cur != null) {
			if (cur.moveToFirst()) {
				allowdownload =  (cur.getString(cur
						.getColumnIndex(SB_BrowserDBAdapter.MASTER_COL2)).equals("Y"));
			}
			cur.close();
		}
		
		//get whether file downloads allowed
		args[0] = "Allow_Copy";
		cur = getApplicationContext().getContentResolver().query(
				MASTER_URI, null, where, args, null);
		if (cur != null) {
			if (cur.moveToFirst()) {
				allowcopyPaste =  (cur.getString(cur
						.getColumnIndex(SB_BrowserDBAdapter.MASTER_COL2)).equals("Y"));
			}
			cur.close();
		}
		
		//get whether file downloads allowed
		args[0] = "Block_Cookies";
		cur = getApplicationContext().getContentResolver().query(
				MASTER_URI, null, where, args, null);
		if (cur != null) {
			if (cur.moveToFirst()) {
				blockCookiesFlag =  cur.getString(cur
						.getColumnIndex(SB_BrowserDBAdapter.MASTER_COL2));
			}
			cur.close();
		}

	}
	
	public void alertbox(String title, String message){
		AlertDialog alertDialog = new AlertDialog.Builder(SB_MainActivity.this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setCancelable(true);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK" ,new DialogInterface.OnClickListener() {
			  @Override
			public void onClick(DialogInterface dialog, int which) {
				  dialog.cancel();

			} });
		alertDialog.show();
	}
	
	
}