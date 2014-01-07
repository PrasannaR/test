package com.cognizant.trumobi.calendar.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;

import android.app.AlertDialog;
import android.content.Context;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.CalendarLog;

public class CalendarUtility {
	public static final Charset UTF_8 = Charset.forName("UTF-8");
	public static final Charset ASCII = Charset.forName("US-ASCII");
	public static ArrayList<String> sNameOfEvent = new ArrayList<String>();

	/**
	 * @param encoded
	 *            base64 data
	 * @return decoded data Decodes base64 data
	 */
	public static byte[] base64Decode(byte[] encoded) {
		if (encoded == null) {
			return null;
		}
		return new Base64().decode(encoded);
	}

	/**
	 * @param s
	 *            String to encode
	 * @return base64 encoded string Encoder the string to base64
	 */
	public static String base64Encode(String s) {
		if (s == null) {
			return s;
		}
		byte[] encoded = new Base64().encode(s.getBytes());
		return new String(encoded);
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

	/**
	 * Ensures that the given string starts and ends with the double quote
	 * character. The string is not modified in any way except to add the double
	 * quote character to start and end if it's not already there.
	 * 
	 * 
	 * things.
	 * 
	 * sample -> "sample" "sample" -> "sample" ""sample"" -> "sample"
	 * "sample"" -> "sample" sa"mp"le -> "sa"mp"le" "sa"mp"le" -> "sa"mp"le"
	 * (empty string) -> "" " -> ""
	 */
	public static String quoteString(String s) {
		if (s == null) {
			return null;
		}
		if (!s.matches("^\".*\"$")) {
			return "\"" + s + "\"";
		} else {
			return s;
		}
	}

	private static String decode(Charset charset, byte[] b) {
		if (b == null) {
			return null;
		}
		final CharBuffer cb = charset.decode(ByteBuffer.wrap(b));
		return new String(cb.array(), 0, cb.length());
	}

	/** Converts a String to UTF-8 */
	public static byte[] toUtf8(String s) {
		return encode(UTF_8, s);
	}

	/** Builds a String from UTF-8 bytes */
	public static String fromUtf8(byte[] b) {
		return decode(UTF_8, b);
	}

	/** Converts a String to ASCII bytes */
	public static byte[] toAscii(String s) {
		return encode(ASCII, s);
	}

	/** Builds a String from ASCII bytes */
	public static String fromAscii(byte[] b) {
		return decode(ASCII, b);
	}

	public static boolean isFirstUtf8Byte(byte b) {
		// If the top 2 bits is '10', it's not a first byte.
		return (b & 0xc0) != 0x80;
	}

	public static Calendar getCurrentDay() {
		Calendar cal = Calendar.getInstance();
		String string = Email.getPresentDay();
		Date date = null;
		try {
			date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
					.parse(string);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		cal.setTime(date);
		return cal;
	}
	/**
	 * @param s
	 *            The alert message Displays an alert dialog with the messaged
	 *            provided
	 */
	public static void showAlert(Context context, String mesg) {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);
		alt_bld.setMessage(mesg).setPositiveButton(android.R.string.ok, null);
		AlertDialog alert = alt_bld.create();
		alert.show();
	}

	/**
	 * Indicates what OS (API level) the device is running
	 * 
	 * @return true if the device is running a pre-2.2 (FroYo) OS, false if OS
	 *         is 2.2 or later Encoder the string to base64
	 */
	public static Boolean isPreFroYo() {
		return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO;
	}

	// /**
	// * Indicates what OS (API level) the device is running
	// *
	// * @return true if the device is running a pre-3.0 (Honeycomb) OS, false
	// if
	// * OS is 3.0 or later Encoder the string to base64
	// */
	// public static Boolean isPreHoneycomb() {
	// return android.os.Build.VERSION.SDK_INT <
	// android.os.Build.VERSION_CODES.HONEYCOMB;
	// }
	//
	// /**
	// * Indicates what OS (API level) the device is running
	// *
	// * @return true if the device is running a pre-4.1 (Jelly Bean) OS, false
	// if
	// * OS is 4.1 or later Encoder the string to base64
	// */
	// public static Boolean isPreJellyBean() {
	// return android.os.Build.VERSION.SDK_INT <
	// android.os.Build.VERSION_CODES.JELLY_BEAN;
	// }

	public static String byteToHex(int b) {
		return byteToHex(new StringBuilder(), b).toString();
	}

	public static StringBuilder byteToHex(StringBuilder sb, int b) {
		b &= 0xFF;
		sb.append("0123456789ABCDEF".charAt(b >> 4));
		sb.append("0123456789ABCDEF".charAt(b & 0xF));
		return sb;
	}

	/**
	 * Generate a time in milliseconds from a date string that represents a
	 * date/time in GMT
	 * 
	 * @param date
	 *            string in format 20090211T180303Z (rfc2445, iCalendar).
	 * @return the time in milliseconds (since Jan 1, 1970)
	 */
	public static long parseDateTimeToMillis(String date) {
		GregorianCalendar cal = parseDateTimeToCalendar(date);
		return cal.getTimeInMillis();
	}

	/**
	 * Generate a GregorianCalendar from a date string that represents a
	 * date/time in GMT
	 * 
	 * @param date
	 *            string in format 20090211T180303Z (rfc2445, iCalendar).
	 * @return the GregorianCalendar
	 */
	public static GregorianCalendar parseDateTimeToCalendar(String date) {
		GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(date
				.substring(0, 4)), Integer.parseInt(date.substring(4, 6)) - 1,
				Integer.parseInt(date.substring(6, 8)), Integer.parseInt(date
						.substring(9, 11)), Integer.parseInt(date.substring(11,
						13)), Integer.parseInt(date.substring(13, 15)));
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));

		return cal;
	}

	/****** For Month View *******/
	private static Calendar current = Calendar.getInstance();
	private static String[] months = { "January", "Febuary", "March", "April",
			"May", "June", "July", "August", "September", "October",
			"November", "December" };

	public static int getNextMonth() {
		current.add(Calendar.MONTH, 1);
		return current.get(Calendar.MONTH);
	}

	public static int getPreviousMonth() {
		current.add(Calendar.MONTH, -1);
		return current.get(Calendar.MONTH);
	}

	public static int getYear() {
		return current.get(Calendar.YEAR);
	}

	public static String getDateText() {
		return months[current.get(Calendar.MONTH)] + " "
				+ current.get(Calendar.YEAR);
	}

	// public static String getUCString(int stringID) {
	// return App.getInstance().getString(stringID).toUpperCase();
	// }

	public static ArrayList<String> readCalendarEvent(Context context) {
		// Cursor cursor = context.getContentResolver()
		// .query(Uri.parse("content://com.android.calendar/events"),
		// new String[] { "calendar_id", "title", "description",
		// "dtstart", "dtend", "eventLocation" }, null,
		// null, null);
		// cursor.moveToFirst();
		// // fetching calendars name
		// String CNames[] = new String[cursor.getCount()];
		//
		// // fetching calendars id
		// nameOfEvent.clear();
		// startDates.clear();
		// endDates.clear();
		// descriptions.clear();
		// for (int i = 0; i < CNames.length; i++) {
		//
		// nameOfEvent.add(cursor.getString(1));
		// startDates.add(getDate(Long.parseLong(cursor.getString(3))));
		// //endDates.add(getDate(Long.parseLong(cursor.getString(4))));
		// descriptions.add(cursor.getString(2));
		// CNames[i] = cursor.getString(1);
		// cursor.moveToNext();
		//
		// }
		return sNameOfEvent;
	}

	public static String getDate(long milliSeconds) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);
		return formatter.format(calendar.getTime());
	}

	// /**********Log***************/
	//
	// public enum LogType
	// {
	// DEBUG,
	// ERROR,
	// WARNING
	// }
	// public static final String Tag="Calendar_Log";
	// public static boolean isLogEnabled=true;
	//
	// public static void logCalendar(String message,LogType logType)
	// {
	// if(isLogEnabled)
	// switch(logType)
	// {
	// case DEBUG:Log.d(Tag,message);
	// break;
	// case ERROR:Log.e(Tag,message);
	// break;
	// case WARNING:Log.w(Tag,message);
	// break;
	// default:
	// break;
	//
	// }
	// }

	public static String getAgendaHeaderTitle(String input) {
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		Date dt1;
		String finalDay = "";
		try {
			dt1 = format1.parse(input);
			DateFormat format2 = new SimpleDateFormat("MMMM dd", Locale.US);
			finalDay = format2.format(dt1);
		} catch (ParseException e) {

			e.printStackTrace();
		}

		return finalDay;
	}

	public static String getAgendaHeaderTitleDay(String input) {
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		Date dt1;
		String finalDay = "";
		try {
			dt1 = format1.parse(input);
			DateFormat format2 = new SimpleDateFormat("EEEE", Locale.US);
			finalDay = format2.format(dt1);
		} catch (ParseException e) {

			e.printStackTrace();
		}

		return finalDay;
	}
	
	public static String getIntervalString(String selectedIntervalString) {
		String[] intervals = Email.getAppContext().getResources()
				.getStringArray(R.array.cal_sync_interval_string);
		try
		{
		int selectedInterval=Integer.parseInt(selectedIntervalString);
		switch (selectedInterval) {
		case 2:
			return intervals[2];
		case 3:
			return intervals[3];
		case 4:
			return intervals[4];
		case 5:
			return intervals[0];
		default:
			return intervals[1];
		}
		}
		catch(Exception e)
		{
			CalendarLog.e(CalendarConstants.Tag,e.toString());
		}
		return intervals[1];

	}
	
	public static String getWeekStartsOn(Context mContext){
		SharedPreferences mSecurePrefs = new SharedPreferences(Email.getAppContext());
		// PreferenceManager.getDefaultSharedPreferences(mContext);//
		String weekStartsOn = mSecurePrefs.getString(
				mContext.getString(R.string.key_list_preference), "");
		return weekStartsOn;
	}
	
	
	public static boolean getShowWeekNumber(Context mContext){
		SharedPreferences mSecurePrefs = new SharedPreferences(Email.getAppContext());
		// PreferenceManager.getDefaultSharedPreferences(mContext);//
		boolean showWeekNumber = mSecurePrefs.getBoolean(mContext
				.getString(R.string.key_show_week_checkbox_preference),
				false);
		return showWeekNumber;
	}
	public static String getWeekDayOfDate(Calendar input) throws ParseException {

		Date dt1 = new Date();
		dt1.setTime(input.getTimeInMillis());
		String finalDay = "";
		// dt1 = format1.parse(input);
		DateFormat format2 = new SimpleDateFormat("EEE", Locale.US);
		finalDay = format2.format(dt1);
		finalDay = finalDay.toUpperCase(Locale.US);
		return finalDay;
	}

	public static String getWeekDayOfDate(Calendar input, int digits)
			throws ParseException {

		Date dt1 = new Date();
		dt1.setTime(input.getTimeInMillis());
		String finalDay = "";
		DateFormat format2 = new SimpleDateFormat("EEE", Locale.US);
		finalDay = format2.format(dt1);
		finalDay = finalDay.toUpperCase(Locale.US);
		finalDay = finalDay.substring(0, digits);
		return finalDay;
	}
	
	
	public static String getWeekDays() {
		Calendar cal = Calendar.getInstance();
		String string = Email.getPresentDay();
		// EmailLog.e("getPresentDay", ""+string);
		Date date = null;
		try {
			date = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
					.parse(string);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		cal.setTime(date);
		SimpleDateFormat format = new SimpleDateFormat("MMM dd", Locale.US);
		Calendar firstCal = (Calendar) cal.clone();

		firstCal.add(Calendar.DAY_OF_WEEK, firstCal.getFirstDayOfWeek()
				- firstCal.get(Calendar.DAY_OF_WEEK));
		SharedPreferences prefs = Email.getmSecurePrefs();
		String WeekStartsOn = prefs.getString(
				Email.getAppContext().getString(R.string.key_list_preference), "");
		if (WeekStartsOn.equalsIgnoreCase("2")) {
			firstCal.setFirstDayOfWeek(Calendar.SATURDAY);
		} else if (WeekStartsOn.equalsIgnoreCase("3")) {
			firstCal.setFirstDayOfWeek(Calendar.SUNDAY);
		} else if (WeekStartsOn.equalsIgnoreCase("4")) {
			firstCal.setFirstDayOfWeek(Calendar.MONDAY);
		}
		int firstDayOfWeek = firstCal.getFirstDayOfWeek();
		int dayOfWeek = firstCal.get(Calendar.DAY_OF_WEEK);
		if (firstDayOfWeek == 7) {
			firstCal.add(Calendar.DAY_OF_WEEK, -dayOfWeek);
		} else
			firstCal.add(Calendar.DAY_OF_WEEK, firstCal.getFirstDayOfWeek()
					- firstCal.get(Calendar.DAY_OF_WEEK));
		String[] days = new String[2];
		days[0] = format.format(firstCal.getTime());
		Calendar lastCal = (Calendar) firstCal.clone();
		lastCal.add(Calendar.DAY_OF_YEAR, 6);
		days[1] = format.format(lastCal.getTime());
		String[] weekDays = Arrays.toString(days).split(",");
		// String weekVal2=weekDays[1].substring(weekDays[1].length()-1);
		String weekVal2 = weekDays[1].replace("]", "");
		String weekVal = weekDays[0].substring(1) + " - " + weekVal2;
		return weekVal;
	}
	
	public static void setTimeZone(){
		String finalStrZone = CalendarCommonFunction
				.getDeviceCurrentTimezoneOffset();
		int endIndex = finalStrZone.indexOf(")");
		
		if (!Email.getmSecurePrefs().getBoolean(
				Email.getAppContext().getString(R.string.key_parent_calender), false)) {
			Email.setNewZone(finalStrZone.substring(1, endIndex));
			Email.getCurrentDDMMYY();
		} else {
			Email.setNewZone(Email.getmSecurePrefs().getString(
					CalendarConstants.PREF_NEW_CALENDAR_ZONE,
					finalStrZone.substring(1, endIndex)));
			Email.getCurrentDDMMYY();
		}
	}
}
