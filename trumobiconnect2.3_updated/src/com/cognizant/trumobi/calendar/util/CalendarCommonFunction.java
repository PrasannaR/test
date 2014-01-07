package com.cognizant.trumobi.calendar.util;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.SharedPreferences.Editor;
import com.cognizant.trumobi.calendar.provider.CalendarDBHelperClassPreICS;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.CalendarLog;

public class CalendarCommonFunction {

	public static String sFromDate;
	public static String sToDate;
	static int sCurrentDay, sCurrentMonth, sCurrentYear;
	static int sNewEventDay, sNewEventMonth, sNewEventYear;

	
	
	public static void saveDismissedEventInPreference(ArrayList<String> myArrayList)
	{
		SharedPreferences mSecurePrefs = new SharedPreferences(
					Email.getAppContext());
		Editor edit = mSecurePrefs.edit();
		for(int i=0;i<myArrayList.size();i++)
		{
			edit.putString("event"+i,myArrayList.get(i));
		}
		edit.commit();
	}
	
	public static ArrayList<String> fetchSavedEventFromPreference()
	{
		SharedPreferences mSecurePrefs = new SharedPreferences(Email.getAppContext());
	
		ArrayList<String> myAList=new ArrayList<String>();
		int size = mSecurePrefs.getInt("eventlistsize", 0);
		 for(int j=0;j<size;j++)
		 {
		    myAList.add(mSecurePrefs.getString("event"+j, ""));
		 }
		 
		 return myAList;
	}
	
	
	public static String setTextpreviousMonthEvent() {
		DateFormatSymbols dateFormatSymbol = new DateFormatSymbols();
		String monthName = dateFormatSymbol.getShortMonths()[sCurrentMonth - 1];
		String previousDate = String.valueOf(01 + " " + monthName + " "
				+ sCurrentYear);
		if ((sCurrentMonth == 1)) {
			sCurrentMonth = 12;
			sCurrentYear = sCurrentYear - 1;
		} else if ((sCurrentMonth == 2)) {
			sCurrentMonth = 13;
			sCurrentYear = sCurrentYear - 1;
		}
		sCurrentMonth -= 2;
		sFromDate = previousDate;
		return previousDate;

	}

	public static String setTextNewEvent() {
		if (sNewEventMonth == 12) {
			sNewEventMonth = 0;
			sNewEventYear = sNewEventYear + 1;
		} else if (sNewEventMonth == 13) {
			sNewEventMonth = 1;
			sNewEventYear = sNewEventYear + 1;
		}

		DateFormatSymbols dateFormatSymbol = new DateFormatSymbols();
		String monthName = dateFormatSymbol.getShortMonths()[sNewEventMonth];
		String previousDate = String.valueOf(01 + " " + monthName + " "
				+ sNewEventYear);

		sNewEventMonth += 2;
		sToDate = previousDate;
		return previousDate;

	}

	public static void getCurrentDDMMYY() {
		// getting local time, date, day of week and other details in local
		// timezone
		Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
		sCurrentDay = localCalendar.get(Calendar.DATE);
		sCurrentMonth = localCalendar.get(Calendar.MONTH) + 1;
		sCurrentYear = localCalendar.get(Calendar.YEAR);
		sNewEventDay = localCalendar.get(Calendar.DATE);
		sNewEventMonth = localCalendar.get(Calendar.MONTH) + 1;

		sNewEventYear = localCalendar.get(Calendar.YEAR);

		StringBuilder currentDayOFYear = new StringBuilder();
		currentDayOFYear.append(sCurrentDay);
		currentDayOFYear.append(getNameOFMonth(sCurrentMonth));
		currentDayOFYear.append(sCurrentYear);

	}

	public static String getNameOFMonth(int month) {
		DateFormatSymbols dateFormatSymbol = new DateFormatSymbols();
		String monthName = dateFormatSymbol.getMonths()[month - 1];
		return monthName;

	}

	public static String getSubNameOFMonth(int month) {
		DateFormatSymbols dateFormatSymbol = new DateFormatSymbols();
		String monthName = dateFormatSymbol.getShortMonths()[month];
		return monthName;

	}

	/**
	 * Use to load Today Page with animation
	 */
	public static Animation inFromLeftAnimation() {
		Animation inFromLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromLeft.setDuration(200);
		inFromLeft.setInterpolator(new AccelerateInterpolator());
		return inFromLeft;
	}

	public static Animation inFromRightAnimation() {
		Animation inFromRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(200);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}

	public static Animation inFromBottomToTopAnimation() {
		Animation surfaceGrowingAnimation = new TranslateAnimation(0, 0, 100,
				Animation.ZORDER_TOP);
		surfaceGrowingAnimation.setDuration(500);
		return surfaceGrowingAnimation;
	}

	public static Animation inFromTopToBottomAnimation() {
		Animation surfaceGrowingAnimation = new TranslateAnimation(0, 0,
				Animation.ZORDER_TOP, 100);
		surfaceGrowingAnimation.setDuration(500);
		return surfaceGrowingAnimation;
	}

	public static String getDeviceCurrentTimezoneOffset() {

		TimeZone timeZone = TimeZone.getDefault();
		Calendar calendar = GregorianCalendar.getInstance(timeZone);
		int offsetInMillis = timeZone.getOffset(calendar.getTimeInMillis());

		String offset = String.format("%02d:%02d",
				Math.abs(offsetInMillis / 3600000),
				Math.abs((offsetInMillis / 60000) % 60));
		offset = (offsetInMillis >= 0 ? "+" : "-") + offset;

		String homeZone = "(" + "GMT" + offset + ")" + " " + timeZone.getID();
		return homeZone;
	}

	@SuppressWarnings("deprecation")
	public static void checkBuildVersion() {
		String providerUri;
		Uri CALENDAR_URI;

		if (Integer.parseInt(Build.VERSION.SDK) >= 8
				&& Integer.parseInt(Build.VERSION.SDK) <= 13) {

			providerUri = CalendarConstants.LOWER_VERSION_PROVIDER_URI;
			CALENDAR_URI = Uri.parse(providerUri);
			// Email.setDevicePreICS(true);

		} else if (Integer.parseInt(Build.VERSION.SDK) >= 14) {

			CALENDAR_URI = CalendarContract.Events.CONTENT_URI;
			// Email.setDevicePreICS(false);
		} else {
			providerUri = CalendarConstants.DEFAULT_PROVIDER;
			CALENDAR_URI = Uri.parse(providerUri);
			// Email.setDevicePreICS(true);
		}
		Email.setCALENDAR_URI(CALENDAR_URI);
	}

	public static String getLastDays(int daysBefore) {
		// substract days from current date using Calendar.add method
		Calendar now = Email.getCurrentDay();
		now.add(Calendar.DATE, -daysBefore);
		String todayMonth = null;
		if (String.valueOf(now.get(Calendar.MONTH) + 1).length() == 1) {
			todayMonth = "0" + String.valueOf(now.get(Calendar.MONTH) + 1);
		}

		String currentDate = (now.get(Calendar.MONTH) + 1) + "-"
				+ now.get(Calendar.DATE) + "-" + now.get(Calendar.YEAR);

		return currentDate;
		// add days to current date using Calendar.add method
		// now.add(Calendar.DATE,1);
		//

	}

	public static String[] getSplitDate(String selectedDate) {
		String[] splitDate = selectedDate.split("-");
		;

		return splitDate;
	}

	public static String getMonth(int day) {
		switch (day) {
		case 0:
			return "Jan";
		case 1:
			return "Feb";
		case 2:
			return "Mar";
		case 3:
			return "Apr";
		case 4:
			return "May";
		case 5:
			return "Jun";
		case 6:
			return "Jul";
		case 7:
			return "Aug";
		case 8:
			return "Sep";
		case 9:
			return "Oct";
		case 10:
			return "Nov";
		case 11:
			return "Dec";
		default:
			return "Jan";
		}

	}

	public static long[] convertDateToMillisec(String dateString) {

		long[] dateToMilliSec = null;
		String[] dateString_array = dateString.split("-");

		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss", Locale.US);
		Date dateCC;
		try {
			dateCC = formatter.parse(dateString + " 00:00:00");
			Calendar calendarStartDate = Calendar.getInstance();
			calendarStartDate.set(Integer.parseInt(dateString_array[0]),
					Integer.parseInt(dateString_array[1]),
					Integer.parseInt(dateString_array[2]));
			calendarStartDate.setTime(dateCC);

			// EndTime
			SimpleDateFormat formatterr = new SimpleDateFormat(
					"yyyy-MM-dd hh:mm:ss", Locale.US);
			Calendar endOfDay = Calendar.getInstance();
			Date dateCCC = formatterr.parse(dateString + " 23:59:59");
			endOfDay.set(Integer.parseInt(dateString_array[0]),
					Integer.parseInt(dateString_array[1]),
					Integer.parseInt(dateString_array[2]));
			endOfDay.setTime(dateCCC);
			dateToMilliSec = new long[] { calendarStartDate.getTimeInMillis(),
					endOfDay.getTimeInMillis() };
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return dateToMilliSec;
	}

	public static ContentValues getUpdatedEvents(String event_id,
			String calendarType) {
		ContentValues contentValues = null;
		if (calendarType.equals(CalendarConstants.CALENDAR_TYPE_CORPORATE)) {
			contentValues = CalendarDatabaseHelper
					.getEventContentValues(CalendarDatabaseHelper
							.getEventDetails(Integer.parseInt(event_id)));
			contentValues.put(CalendarConstants.CALENDAR_TYPE_KEY,
					CalendarConstants.CALENDAR_TYPE_CORPORATE);
		} else if (calendarType
				.equals(CalendarConstants.CALENDAR_TYPE_PERSONAL)) {

			contentValues = CalendarDBHelperClassPreICS
					.getPersonalEventContentValues(CalendarDBHelperClassPreICS
							.getEventDetails(Integer.parseInt(event_id)));
			contentValues.put(CalendarConstants.CALENDAR_TYPE_KEY,
					CalendarConstants.CALENDAR_TYPE_PERSONAL);

		} else {

		}
		return contentValues;
	}

	public static Calendar convertDBDateToCalendar(String eventDate) {
		String[] eventDays = eventDate.split("-");
		Calendar currentCal = Calendar.getInstance();
		currentCal.setFirstDayOfWeek(Calendar.SUNDAY);
		currentCal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(eventDays[2]));
		currentCal.set(Calendar.MONTH, Integer.valueOf(eventDays[1]) - 1);
		currentCal.set(Calendar.YEAR, Integer.valueOf(eventDays[0]));
		return currentCal;

	}
	
	public static Calendar convertToCalendar(int days,int month,int year) {
		Calendar currentCal = Calendar.getInstance();
		currentCal.setFirstDayOfWeek(Calendar.SUNDAY);
		currentCal.set(Calendar.DAY_OF_MONTH, days);
		currentCal.set(Calendar.MONTH, month);
		currentCal.set(Calendar.YEAR, year);
		return currentCal;

	}

	public static String convertMilliSecondsToDate(long milliseconds) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		Date currentDate = new Date(milliseconds);
		return df.format(currentDate);

	}

	public static boolean isTablet(Context context) {
		boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
		boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
		return (xlarge || large);
	}

	public static boolean isSmallScreen(Context context) {
		boolean isSmall;
		if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == (Configuration.SCREENLAYOUT_SIZE_SMALL)
				|| (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == (Configuration.SCREENLAYOUT_SIZE_NORMAL)) {
			// on a large screen device ...
			isSmall = true;
		} else {
			isSmall = false;
		}
		return isSmall;
	}

	public static void callSecuredBrowser(Context context, String url) {

		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			url = "http://" + url;
		}

		Intent openWebAppIntent = new Intent(
				"com.cognizant.trumobi.securebrowser");
		if (openWebAppIntent != null) {
			openWebAppIntent.putExtra("toBrowser", url);
			//openWebAppIntent.putExtra("name", url);
			// openWebAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			/*openWebAppIntent
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);*/
			try {
				// activity.startActivity(openWebAppIntent);
				context.startActivity(openWebAppIntent);
				// result = true;
			} catch (ActivityNotFoundException ex) {
				// No applications can handle it. Ignore.
			}
		}
	}
	
	public static int compareTo(Calendar cal1,Calendar cal2){
		return cal1.compareTo(cal2);
	}
	
	public static int compareTo(Date cal1,Date cal2){
		return cal1.compareTo(cal2);
	}
	
	public static Date getZeroTimeDate(Date fecha) {
	    Date res = fecha;
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime( fecha );
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);

	    res = calendar.getTime();

	    return res;
	}
	
	public static Date getZeroTimeDate(int days,int month,int year) {
	    Calendar calendar = Calendar.getInstance();
	    calendar = convertToCalendar(days, month, year);
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);

	    Date res = calendar.getTime();

	    return res;
	}
}
