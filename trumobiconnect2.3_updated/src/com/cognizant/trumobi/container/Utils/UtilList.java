package com.cognizant.trumobi.container.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Log;

import com.TruBoxSDK.TruboxFileEncryption;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.contacts.activity.ContactsAddContact;
import com.cognizant.trumobi.container.Pojo.ChildMailbox;
import com.cognizant.trumobi.em.activity.EmMessageView;

import ezvcard.Ezvcard;
import ezvcard.VCard;

public class UtilList {
	public static String LOGCAT_PARSER = "ContactParser";
	public static String LOGCAT_FOLDER = "FolderParser";
	public static String LOGCAT_ATTACH = "AttachParser";
	private static Typeface mTypeFaceBold = null;
	private static Typeface mTypeFaceNormal = null;
	public static String COL_Email_id, COL_Domain, COL_Server, COL_Password;

	public static int certificateResponseCode;
	public static String colId = "";

	public static String att_file_name = null;
	public static String att_file_dateTime = null;
	public static String att_file_index = null;
	public static String att_forwarded_subject = null;
	public static Boolean att_forwarded_boolean = null;
	
	public static int selectedItemPosition;
	public static boolean isClicked = false;

	public static final int Login_Sucess_Callback = 1;
	public static final int Getting_Item_Id_Callback = 2;
	public static final int Getting_Attachment_Id_Callback = 3;
	public static final int Getting_Attachment_Content_Callback = 10;

	public static final int Login_Cmd = 1;
	public static final int Getting_Item_Id = 2;
	public static final int Getting_Attachment_Id = 3;
	public static final int Getting_Attachment_Content = 10;

	public static HashMap<String, String> bookmarkedAttachmentItemIDMail = new HashMap<String, String>();
	public static HashMap<String, Boolean> bookmarkedAttachmentItemIDValuesMail = new HashMap<String, Boolean>();

	public static HashMap<String, String> bookmarkedAttachmentItemIDCal = new HashMap<String, String>();
	public static HashMap<String, Boolean> bookmarkedAttachmentItemIDValuesCal = new HashMap<String, Boolean>();

	public static HashMap<String, String> deleteItemIDMail = new HashMap<String, String>();
	public static HashMap<String, String> deleteItemIDCal = new HashMap<String, String>();
	public static int dataTypeMail = 1;
	public static int dataTypeCal = 1;

	public static ArrayList<String> bookmarkItemIDMail = new ArrayList<String>();//MultiBook
	public static ArrayList<String> bookmarkItemIDCal = new ArrayList<String>();//MultiBook
	
	
	public static final int RECENT_ADDED = 1;
	public static final int BOOKMARK = 2;
	public static final int LABEL = 3;
	public static final int SENTBY = 4;
	public static final int DOCTYPE = 5;
	public static final int SEARCH = 6;

	public static final Charset UTF_8 = Charset.forName("UTF-8");
	
	public static String fileName = "";

	// Return bold font style
	public static Typeface getTextTypeFaceBold(Context context) {
		if (mTypeFaceBold == null) {
			mTypeFaceBold = Typeface.createFromAsset(context.getAssets(),
					"Roboto-Bold.ttf");
		}

		return mTypeFaceBold;

	}

	// Return normal font style
	public static Typeface getTextTypeFaceNormal(Context context) {
		if (mTypeFaceNormal == null) {
			mTypeFaceNormal = Typeface.createFromAsset(context.getAssets(),
					"Roboto-Regular.ttf");
		}

		return mTypeFaceNormal;

	}

	//@SuppressLint("SimpleDateFormat")
	public static String manipulateTimeGMT(int arg) {

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, arg);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss a");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateAndTimeGMT = dateFormat.format(c.getTime());

		String[] splitedDateTime = dateAndTimeGMT.split(" ");
		String currentTimeGMT = splitedDateTime[0] + "T" + splitedDateTime[1]
				+ "Z";

		Log.i("Tag ", "Time " + currentTimeGMT);
		return currentTimeGMT;
	}

	public synchronized static String createTempFile(String in, String op) {
		try {
			Log.i("AAPARSER ", "SUccess " + in + "   " + op);
			int BUFFER_SIZE = 4096;
			byte[] buffer = new byte[BUFFER_SIZE];
			InputStream input = new Base64InputStream(new FileInputStream(in),
					Base64.DEFAULT);
			OutputStream output = new FileOutputStream(op);
			int n = input.read(buffer, 0, BUFFER_SIZE);
			while (n >= 0) {
				output.write(buffer, 0, n);
				n = input.read(buffer, 0, BUFFER_SIZE);
			}
			input.close();
			output.close();
		} catch (Exception e) {
			Log.e("Exception in Parser ", " " + e.toString());
			op = "";
		}

		return op;

	}

	public static String getDeviceUniqueId(Context context) {
		final String deviceId;
		try {
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (tm == null) {
				return null;
			}
			deviceId = tm.getDeviceId();
			if (deviceId == null) {
				return null;
			}
		} catch (Exception e) {
			Log.d("Device ID Exception",
					"Error in TelephonyManager.getDeviceId(): "
							+ e.getMessage());
			return null;
		}
		return getSmallHash(deviceId);
	}

	public static String getSmallHash(final String value) {
		final MessageDigest sha;
		try {
			sha = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException impossible) {
			return null;
		}
		sha.update(encode(UTF_8, value));
		final int hash = getSmallHashFromSha1(sha.digest());
		return Integer.toString(hash);
	}

	private static byte[] encode(Charset charset, String s) {
		if (s == null) {
			return null;
		}
		final ByteBuffer buffer = charset.encode(CharBuffer.wrap(s));
		final byte[] bytes = new byte[buffer.limit()];
		buffer.get(bytes);
		return bytes;
	}

	static int getSmallHashFromSha1(byte[] sha1) {
		final int offset = sha1[19] & 0xf; // SHA1 is 20 bytes.
		return ((sha1[offset] & 0x7f) << 24)
				| ((sha1[offset + 1] & 0xff) << 16)
				| ((sha1[offset + 2] & 0xff) << 8)
				| ((sha1[offset + 3] & 0xff));
	}

	/*public static String getCertificate(Context ctx) {
		byte[] certificateData = null;
		String cert = null;
		Uri certificateUri = Uri
				.parse("content://com.cognizant.trumobi.persona.emailcontentprovider/email");
		ContentProviderClient certificateClient = ctx.getContentResolver()
				.acquireContentProviderClient(certificateUri);
		
		if (certificateClient != null) {
			String ID = "_id";
			COL_Email_id = "email_id";
			COL_Domain = "domain";
			COL_Server = "server";
			COL_Password = "password";
			String COL_File = "file";
			// projection field
			String[] requestList = { ID, COL_Email_id, COL_File, COL_Domain,
					COL_Server, COL_Password };
			try {
				Cursor cursor = certificateClient.query(certificateUri,
						requestList, null, null, null);
				if (cursor != null & cursor.moveToFirst()) {
					do {
						certificateData = cursor.getBlob(cursor
								.getColumnIndex(COL_File));
						COL_Email_id = cursor.getString(cursor
								.getColumnIndex(COL_Email_id));
						COL_Domain = cursor.getString(cursor
								.getColumnIndex(COL_Domain));
						COL_Server = cursor.getString(cursor
								.getColumnIndex(COL_Server));
						COL_Password = cursor.getString(cursor
								.getColumnIndex(COL_Password));
					} while (cursor.moveToNext());
				} else {
					Log.d("Cursor Failed", "Cursor is null");
					return null;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Log.e("URI Response", "URI not found");
			return null;
		}
		Log.i("", "" + COL_Email_id + " " + COL_Domain + "  " + COL_Server
				+ "  " + COL_Password);
		byte[] str = Base64.encode(certificateData, Base64.NO_WRAP);
		cert = new String(str);
		return cert;
	}
*/
	public static JSONObject appendJSONData(Context ctx) {
		String deviceID = UtilList.getDeviceUniqueId(ctx);
		if (deviceID != null) {
			deviceID = "androidc" + deviceID;
		} else {
			deviceID = "android" + deviceID;
		}
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.putOpt("RequestDeviceSL", deviceID);
			jsonObj.putOpt("RequestDeviceType", Constants.DEVICE_TYPE);
			jsonObj.putOpt("RequestEmailId", COL_Email_id);
			jsonObj.putOpt("RequestUserDomain", COL_Domain);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObj;
	}

	public static void makeHeaderList(List<ChildMailbox> attachmentItemList,
			int type) {
		// UtilList.dataType

		boolean setHeader = true;
		
		try{
			
			if(attachmentItemList.size() > 0)
				setHeader = true;
			else
				setHeader = false;
			
		}catch(Exception e){
			setHeader = false;
		}
		
		switch (type) {
		case UtilList.SENTBY: {
			
			if(setHeader){
				
			int countSentBy = 1;
			int posSentBy = 0;
	
			attachmentItemList.get(0).setHEADER_CONTENT(
					attachmentItemList.get(0).getDISPLAY_NAME().toString()
							.trim());
			String temp = attachmentItemList.get(0).getHEADER_CONTENT()
					.toString().trim();
			for (int i = 1; i < attachmentItemList.size(); i++) {
				if (temp.equals(attachmentItemList.get(i).getDISPLAY_NAME()
						.toString().trim())) {
					attachmentItemList.get(i).setHEADER_CONTENT(" ");
					countSentBy+=1;
				} else {
					
					//Log.i("Count","hi "+countSentBy+"  "+posSentBy);
					attachmentItemList.get(posSentBy).setHEADER_COUNT(String.valueOf(countSentBy));
					countSentBy = 1;
					posSentBy = i;
					attachmentItemList.get(i).setHEADER_CONTENT(
							attachmentItemList.get(i).getDISPLAY_NAME()
									.toString().trim());
					temp = attachmentItemList.get(i).getDISPLAY_NAME()
							.toString().trim();
				}
			}
			//Log.i("Count","hi "+countSentBy+"  "+posSentBy);
			attachmentItemList.get(posSentBy).setHEADER_COUNT(String.valueOf(countSentBy));
		}
			break;
		}
		default: {// Recently Added and BookMard Header change
			
			if(setHeader){
				
				int countSentBy = 1;
				int posSentBy = 0;
				
				attachmentItemList.get(0).setHEADER_CONTENT(
					performDateTimeSplit(0, attachmentItemList));
			String temp = attachmentItemList.get(0).getHEADER_CONTENT();
			for (int i = 1; i < attachmentItemList.size(); i++) {
				if (temp.equalsIgnoreCase(performDateTimeSplit(i,
						attachmentItemList))) {
					attachmentItemList.get(i).setHEADER_CONTENT(" ");
					countSentBy+=1;
				} else {
					
					//Log.i("Count","hi "+countSentBy+"  "+posSentBy);
					attachmentItemList.get(posSentBy).setHEADER_COUNT(String.valueOf(countSentBy));
					countSentBy = 1;
					posSentBy = i;
					
					attachmentItemList.get(i).setHEADER_CONTENT(
							performDateTimeSplit(i, attachmentItemList));
					temp = performDateTimeSplit(i, attachmentItemList);
				}
			}
			
			//Log.i("Count","hi "+countSentBy+"  "+posSentBy);
			attachmentItemList.get(posSentBy).setHEADER_COUNT(String.valueOf(countSentBy));
			
				}
			break;
		}
		}
	}

	public static String performDateTimeSplit(int index,
			List<ChildMailbox> splitAttachmnetList) {
		String dateTime = splitAttachmnetList.get(index).getDateTimeReceived()
				.toString();
		String[] Date = dateTime.split("@");
		String mixedTime = Date[1];
		String[] Time = mixedTime.split("Z");
		splitAttachmnetList.get(index).setDATE(Date[0].toUpperCase());
		splitAttachmnetList.get(index).setTIME(Time[0]);
		return Date[0].toString().trim();
	}

	public static String dateFn(String dateString) {

		try {
			return reformatDateString(dateString,
					"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateString;
	}

	public static String dateFnWithType(String dateString, String dateType) {

		try {
			return reformatDateString(dateString, dateType,
					"yyyy-MM-dd'T'HH:mm:ss'Z'");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static final String timeConersion(String abc, int add){
		
		try {
			/*
			 * Date date = new
			 * SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(abc);

			 * System.out.println(date); Calendar calendar =
			 * Calendar.getInstance(); calendar.setTime(date);
			 */


			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			cal.setTime(sdf.parse(abc));// all done
			//System.out.println(cal.getTime());
			cal.add(Calendar.MINUTE, add);
			//System.out.println(cal.getTime());
			//System.out.println(sdf.format(cal.getTime()));
			return sdf.format(cal.getTime());

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";

		
	}

	public static final String reformatDateString(final String dateString,
			final String dateStringFormat, final String outputFormat)
			throws ParseException {
		final SimpleDateFormat dateStringParser = new SimpleDateFormat(
				dateStringFormat);
		final SimpleDateFormat outputFormatter = new SimpleDateFormat(
				outputFormat);
		return outputFormatter.format(dateStringParser.parse(dateString));
	}

	/*public static void populateProvider(Context ctx) {
		Uri customEmailURI = Uri
				.parse("content://com.cognizant.trumobi.persona.emailcontentprovider/email");
		ContentProviderClient cpEmailClient = ctx.getContentResolver()
				.acquireContentProviderClient(customEmailURI);

		if (cpEmailClient != null) {
			String ID = "_id";
			String COL_Email_id = "email_id";
			String COL_Domain = "domain";
			String COL_Server = "server";
			String COL_Password = "password";
			String COL_File = "file";
			String[] list_pro = { ID, COL_Email_id, COL_File, COL_Domain,
					COL_Server, COL_Password };

			try {
				Cursor cursor = cpEmailClient.query(customEmailURI, list_pro,
						null, null, null);
				if (cursor != null & cursor.moveToFirst()) {
					do {
						ExchangeData.setPfxbyteArray(cursor.getBlob(cursor
								.getColumnIndex(COL_File)));
						ExchangeData.setPfxPass(cursor.getString(cursor
								.getColumnIndex(COL_Password)));
						ExchangeData.setDomain(cursor.getString(cursor
								.getColumnIndex(COL_Domain)));
						ExchangeData.setServer(cursor.getString(cursor
								.getColumnIndex(COL_Server)));
						ExchangeData.setEmail_ID(cursor.getString(cursor
								.getColumnIndex(COL_Email_id)));
					} while (cursor.moveToNext());
				}
			} catch (Exception e) {

			}

		}
		
		Log.d("CertificateData", ""+ExchangeData.getPfxPass().toString());
		Log.d("CertificateData", ""+ExchangeData.getDomain());
		Log.d("CertificateData", ""+ExchangeData.getServer());
		Log.d("CertificateData", ""+ExchangeData.getEmail_ID());
		Log.d("CertificateData", ""+ExchangeData.getPfxbyteArray().toString().length());
	}*/
/*<<<<<<< .mine
	
=======
	public static void openAttachment(String msg, Context mContext) {

		try{
		Log.i("Else ", "File Path " + msg);
		String ext = "";

		int mid = msg.lastIndexOf(".");
		ext = msg.substring(mid + 1, msg.length());

		Log.i("Ext ", ": " + ext + " ");

		String path = msg;
						 * UtilList.createTempFile( mContext.getFilesDir() + "/"
						 * + msg, mContext.getFilesDir() + "/" + "temp." + ext);
						 
		Log.i("Else ", "File Path " + path);

		if (!(path.equals(""))) {
			// Give to PDF

			if (ext.equalsIgnoreCase("pdf")) {
				Intent pdfIntent = new Intent();

				pdfIntent.setDataAndType(Uri.fromFile(new File(path)),
						"application/pdf");
				pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				pdfIntent.setClass(mContext, OpenFileActivity.class);
				pdfIntent.setAction("android.intent.action.VIEW");
				mContext.startActivity(pdfIntent);

			} else {
				Intent pdfIntent = new Intent();
				pdfIntent.setClass(mContext, MimeTypeTextShow.class);
				pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				OutlookPreference.getInstance(mContext).setValue("FilePath",
						path);
				mContext.startActivity(pdfIntent);

			}
		} else {
			// Show toast
		}

		}catch(Exception e){
			Log.i("Utilist catch open ","Hi exception");
		}
	}
>>>>>>> .r41124*/
	public static String createTempFileWithSize(String filepath, long fileSize,
			int buffSize, TruboxFileEncryption truboxFileEncryption)
			throws Exception {

		String path = filepath;
		byte[] Write = new byte[buffSize];
		Log.i("PTATH ", "----> path " + path);
		File file1 = new File(path);
		if (!file1.exists()) {
			file1.createNewFile();
		}
		long Size = fileSize;
		FileOutputStream stream = new FileOutputStream(path);

		while (Size >= 0) {

			try {
				Write = truboxFileEncryption.read();
				Size = Size - Write.length;
				Log.i("Si ", "--> " + Size + "   " + Write.length);
				stream.write(Write);

			} catch (Exception e1) {
				Size = -1;
				Log.i("WARNING", "--> " + e1.toString());
			}

		}
		stream.close();
		Log.i("PTATH ",
				"----> createTempFile  " + "    " + file1.getAbsolutePath());
		return file1.getAbsolutePath();

	}
	
	public static String createTempFile(String filepath, long fileSize,
			int buffSize, TruboxFileEncryption truboxFileEncryption)
			throws Exception {
		
		byte[] Write = new byte[buffSize];
		Log.i("PTATH ", "----> path " + filepath);
		File file1 = new File(filepath);
		if (!file1.exists()) {
			file1.createNewFile();
		}else{

//			Log.i("PTATH ",
//					"----> NEWWWW  " + file1.getName()+"    " + file1.length());
			if(file1.length() > 0)
			return file1.getAbsolutePath();
			
		}
		//long Size = fileSize;
		FileOutputStream stream = new FileOutputStream(filepath);

		try{
		while ((Write = truboxFileEncryption.read()) != null) {

			try {

				stream.write(Write);

			} catch (Exception e1) {

				//Size = -1;
				Log.i("WARNING", "--> " + e1.toString());

			}


		}
		}catch(Exception e){
			
			e.printStackTrace();
			stream.flush();
			stream.close();
			return null;
			
		}
		stream.flush();
		stream.close();
		Log.i("PTATH ",
				"----> createTempFile  " + file1.getName()+"    " + file1.length());
		return file1.getAbsolutePath();
		
	}
		
	public static String getAttachmentIndex(String attachmentRef){
		try{
		return attachmentRef.substring(attachmentRef.length() - 1);
		}catch(Exception e){
			return "";
		}
	}
	
	/*public static String frmEpochDate(Date date){
		
		   DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		   String strDate = dateFormat.format(date);
		   // Log.i("NEW","frmEpochDate "+strDate);
		   frmEpochDatenew(date);

		return strDate;
	}*/
	
	public static String frmEpochDate(Date date){
		
		DateFormat dateFormat = new SimpleDateFormat(
				"EEEE '-' MMM dd','yyyy'@'HH:mm'Z'");
		String strDate = dateFormat.format(date);
		//Log.i("NEWWW", "frmEpochDate " + strDate);

		return strDate;
	}

	public static String frmEpochDatenew(Date date) {

		DateFormat dateFormat = new SimpleDateFormat(
				"EEEE '-' MMM dd','yyyy'@'HH:mm'Z'");
		String strDate = dateFormat.format(date);
		//Log.i("NEWWW", "frmEpochDate " + strDate);

		return strDate;
	}
	
public static String writeTemp(Uri in,String ext,Context ctx){
		
		String path = "";
		try 
	      {
	       
			File file = new File(ctx.getFilesDir() + "/temp."+ext);
			
			
			if(!file.exists())
				file.createNewFile();
			
			InputStream inputStream = ctx.getContentResolver().openInputStream(in);
			OutputStream os = new FileOutputStream(file);
			
			path = file.getAbsolutePath();
			
			IOUtils.copy(inputStream, os);
			os.flush();
			os.close();
			inputStream.close();
			
			
			/*int CHUNK_SIZE = 16*1024;
			byte[] bytes = new byte[CHUNK_SIZE];
	        InputStream inputStream = ctx.getContentResolver().openInputStream(in);
	         Log.i("","after out stream ");
	         while (true) {
	        	 Log.i("","inside while ");
	        	 int read = inputStream.read(bytes, 0, CHUNK_SIZE);
	             if (read < 0) {
	                 break;
	             }

	       
	             os.write(bytes, 0, read);

	         
	         }
	         inputStream.close();
	         os.close();*/

	      }catch(Exception e){
	    	  System.err.println("FileCopy: " + e);
	      }
		return path;
	}
	public static String frmDateEpoch(String str) throws Exception {
		long epoch = 0;
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = df.parse(str);
			epoch = date.getTime();
			//Log.i("NEWW", "frmDateEpoch " + epoch);// 1055545912454
		} catch (Exception e) {
			epoch = 0;
		}

		return Long.toString(epoch);
	}

	public static String frmSysCal() {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// get current date time with Date()
		Date date = new Date();
		String strDate = dateFormat.format(date);
		//Log.i("NEWW", "frmSysCal " + strDate);

		return strDate;
	}

	//@SuppressLint("SimpleDateFormat")
	public static String manipulateTime(int arg) {

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, arg);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss");
		// dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateAndTimeGMT = dateFormat.format(c.getTime());

		/*
		 * String[] splitedDateTime = dateAndTimeGMT.split(" "); String
		 * currentTimeGMT = splitedDateTime[0] + "T" + splitedDateTime[1] + "Z";
		 */
		//Log.i("NEWW", "Time " + dateAndTimeGMT);
		return dateAndTimeGMT;
	}

	public static String stringConvertion(String text) {

		try {
			String s = text;
			int l = s.length();
			char c = Character.toUpperCase(s.charAt(0));
			s = c + s.substring(1);
			for (int i = 1; i < l; i++) {
				if (s.charAt(i) == ' ') {
					c = Character.toUpperCase(s.charAt(i + 1));
					s = s.substring(0, i) + c + s.substring(i + 2);
				}
			}
			return s;
		} catch (Exception e) {
			Log.i("", "");
			return text;
		}

	}

	// NEW CHANGES 22
	private static final long KB = 1024;
	private static final long MB = KB * KB;

	public static String convertToStringRepresentation(final long value) {
		final long[] dividers = new long[] { MB, KB, 1 };
		final String[] units = new String[] { "MB", "KB", "B" };
		
		if (value < 1)
			return "0B";
		
		String result = null;
		for (int i = 0; i < dividers.length; i++) {
			final long divider = dividers[i];
			if (value >= divider) {
				result = format(value, divider, units[i]);
				break;
			}
		}
		//Log.i("Sudar","===== "+result);
		return result;
	}

	private static String format(final long value, final long divider,
			final String unit) {
		final double result = divider > 1 ?  Math.round((double) value / (double) divider)
				:  Math.round((double) value);

		NumberFormat nf = new DecimalFormat();
		nf.setMaximumFractionDigits(1);

		return nf.format(result) + " " + unit;
	}
	
	public static String sentByTime(String mDate,String mTime){
		
		String currentTime;
		
		try{
		long curTime = System.currentTimeMillis();
		SimpleDateFormat curdf = new SimpleDateFormat("EEEE '-' MMM dd','yyyy");
		
		String strmDate = curdf.format(curTime);
		
		if(mDate.equals(strmDate)){
			currentTime = mTime;
		}
		else{
			SimpleDateFormat df = new SimpleDateFormat("MMM dd");
			Date date = null;
			try {
				date = curdf.parse(mDate);
			} catch (ParseException e) {
				e.printStackTrace();
				return mTime;
			}
			currentTime = df.format(date);
		}
		}catch(Exception e){
			
			return mTime;
		}

		return currentTime;
	} 

	public static void openEzcard(String path, Context mContext) {

		File file = new File(path);
		VCard vcard = null;
		try {
			vcard = Ezvcard.parse(file).first();

			Intent addContact = new Intent(mContext, ContactsAddContact.class);
			ContactsModel contactsModel = new ContactsModel();

			if (vcard.getTelephoneNumbers() != null
					&& vcard.getTelephoneNumbers().size() > 0) {
				for (int i1 = 0; i1 < vcard.getTelephoneNumbers().size(); i1++) {
					if (vcard.getTelephoneNumbers().get(i1) != null
							&& vcard.getTelephoneNumbers().get(i1).getText()
									.length() > 0) {
						contactsModel.setcontacts_mobile_telephone_number(vcard
								.getTelephoneNumbers().get(i1).getText());
					}
				}

			}

			if (vcard.getFormattedName() != null) {
				contactsModel.setcontacts_first_name(vcard.getFormattedName()
						.getValue());
			}

			if (vcard.getEmails().size() > 0 && vcard.getEmails() != null) {
				for (int i2 = 0; i2 < vcard.getEmails().size(); i2++) {
					if (vcard.getEmails().get(i2) != null
							&& vcard.getEmails().get(i2).getValue().length() > 0) {
						contactsModel.setcontacts_email1_address(vcard
								.getEmails().get(i2).getValue());
					}
				}
			}

			if (vcard.getTitles().size() > 0 && vcard.getTitles() != null) {
				contactsModel.setcontacts_title(vcard.getTitles().get(0)
						.getValue());
			}
			if (vcard.getAddresses().size() > 0 && vcard.getAddresses() != null) {
				contactsModel.setcontacts_home_address_city(vcard
						.getAddresses().get(0).getLocality());
			}
			if (vcard.getAddresses().size() > 0 && vcard.getAddresses() != null) {
				contactsModel.setcontacts_home_address_country(vcard
						.getAddresses().get(0).getCountry());
			}

			if (vcard.getAddresses().size() > 0 && vcard.getAddresses() != null) {
				contactsModel.setcontacts_home_address_state(vcard
						.getAddresses().get(0).getRegion());
			}
			if (vcard.getAddresses().size() > 0 && vcard.getAddresses() != null) {
				contactsModel.setcontacts_home_address_postal_code(vcard
						.getAddresses().get(0).getPostalCode());
			}
			if (vcard.getAddresses().size() > 0 && vcard.getAddresses() != null) {
				contactsModel.setcontacts_home_address_street(vcard
						.getAddresses().get(0).getStreetAddress());
			}
			if (vcard.getOrganization().getValues().size() > 0
					&& vcard.getOrganization() != null) {
				contactsModel.setcontacts_yomi_company_name(vcard
						.getOrganization().getValues().get(0).toString());
			}

			Bundle data = new Bundle();
			data.putSerializable("obj", contactsModel);
			addContact.putExtras(data);
			addContact.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(addContact);
			file.delete();
		} catch (Exception e) {
			file.delete();
			e.printStackTrace();
		}

	}
	
    public static String generateMessageId(long mId) {
        StringBuffer sb = new StringBuffer();
        sb.append(mId);
        final java.util.Random sRandom = new java.util.Random();
        for (int i = 0; i < 24; i++) {
            // We'll use a 5-bit range (0..31)
            int value = sRandom.nextInt() & 31;
            char c = "0123456789abcdefghijklmnopqrstuv".charAt(value);
            sb.append(c);
        }
        Log.i("generateMessageId ", "generateMessageId "+mId+"  "+sb.toString());
        return sb.toString();
    }
	
    public static void recursiveDelete(File directory) {
		
		try{
		if (!directory.exists())
			return;
		if (directory.isDirectory()) {
			for (File f : directory.listFiles()) {
				Log.i("recursiveDelete" , f.getAbsolutePath()+"  "+f.delete());
				/*f.delete()*/;
				
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
		

}