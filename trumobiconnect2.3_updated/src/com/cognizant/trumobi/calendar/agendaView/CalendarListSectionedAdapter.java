package com.cognizant.trumobi.calendar.agendaView;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.modal.Event;
import com.cognizant.trumobi.calendar.provider.CalendarDBHelperClassPreICS;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.calendar.util.CalendarUtility;

public class CalendarListSectionedAdapter extends CalendarSectionedBaseAdapter {
	ArrayList<ArrayList<ContentValues>> mAgendaListValues = new ArrayList<ArrayList<ContentValues>>();
	private ContentValues mContentValues;
	private Context mContext;

	public CalendarListSectionedAdapter(Context context,
			ArrayList<ArrayList<ContentValues>> value) {
		this.mAgendaListValues = value;
		mContext = context;

	}

	@Override
	public Object getItem(int section, int position) {

		return null;
	}

	@Override
	public long getItemId(int section, int position) {

		return 0;
	}

	@Override
	public int getSectionCount() {
		return mAgendaListValues.size();
	}

	@Override
	public int getCountForSection(int section) {
		return mAgendaListValues.get(section).size();
	}

	@Override
	public View getItemView(int section, int position, View convertView,
			ViewGroup parent) {
		RelativeLayout layout = null;
		if (convertView == null) {
			LayoutInflater inflator = (LayoutInflater) parent.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			layout = (RelativeLayout) inflator.inflate(
					R.layout.cal_agenda_list_item, null);
		} else {
			layout = (RelativeLayout) convertView;
		}
		String StartDate = mAgendaListValues.get(section).get(position)
				.getAsString("HeaderDate");
//		CalendarLog.d(CalendarConstants.Tag, "whereClause " + StartDate + " section "+section+" position "+position);
		if (StartDate == null || StartDate.equalsIgnoreCase(null)) {
			return layout;
		}
		Calendar cal = Calendar.getInstance();
		String[] days = StartDate.split("-");
		int currentYear = cal.get(Calendar.YEAR);
		int currentMonth = cal.get(Calendar.MONTH) + 1;
		int currentDay = cal.get(Calendar.DAY_OF_MONTH);
		if ((currentMonth > Integer.valueOf(days[1]))
				&& (currentYear >= Integer.valueOf(days[0]))) {

			layout.setBackgroundColor(Color.parseColor("#DCDCDC"));
		} else if ((currentYear == Integer.valueOf(days[0]))
				&& (currentMonth == Integer.valueOf(days[1]))
				&& (currentDay == Integer.valueOf(days[2]))) {
			layout.setBackgroundColor(mContext.getResources().getColor(R.color.white));
		} else if ((currentMonth >= Integer.valueOf(days[1]))
				&& (currentYear >= Integer.valueOf(days[0]))) {
			if (currentDay > Integer.valueOf(days[2]))
				layout.setBackgroundColor(mContext.getResources().getColor(R.color.layoutbg));
			else
				layout.setBackgroundColor(mContext.getResources().getColor(R.color.white));

		} else {
			layout.setBackgroundColor(mContext.getResources().getColor(R.color.white));
		}

		String calendarType = mAgendaListValues.get(section).get(position)
				.getAsString(CalendarConstants.CALENDAR_TYPE_KEY);
		// String colorBG =
		// arrayValue.get(section).get(position).getAsString("eventBGColor");
		ImageView agendavieweventfor = ((ImageView) layout
				.findViewById(R.id.agenda_view_event_for));

		if (calendarType.equals(CalendarConstants.CALENDAR_TYPE_CORPORATE)) {
			agendavieweventfor.setVisibility(View.VISIBLE);

		} else {
			agendavieweventfor.setVisibility(View.GONE);
		}
		TextView agendaEvent = ((TextView) layout
				.findViewById(R.id.txt_agenda_event));
		agendaEvent.setText((CharSequence) mAgendaListValues.get(section)
				.get(position).get("title"));
		agendaEvent.setTextSize(14);
		TextView agendaTime = ((TextView) layout
				.findViewById(R.id.txt_agenda_time));
		
		String time = "";
		
		long startTimeLong = mAgendaListValues.get(section).get(position).getAsLong(Event.DTSTART);
		long endTimeLong =  mAgendaListValues.get(section).get(position).getAsLong(Event.DTEND);

		String startDate = CalendarDatabaseHelper.getDateFromLong(startTimeLong, CalendarDatabaseHelper.getDefaultTimeZone());
		String endDate = CalendarDatabaseHelper.getDateFromLong(endTimeLong, CalendarDatabaseHelper.getDefaultTimeZone());
//		CalendarLog.d(CalendarConstants.Tag,"aish init values endtime");

		String endtime = CalendarDatabaseHelper
				.getTimeFromLong( mAgendaListValues.get(section).get(position).getAsLong(Event.DTEND),CalendarDatabaseHelper.getDefaultTimeZone());
//		CalendarLog.d(CalendarConstants.Tag,"aish init values startTime");
		String starttime = CalendarDatabaseHelper
				.getTimeFromLong( mAgendaListValues.get(section).get(position).getAsLong(Event.DTSTART),CalendarDatabaseHelper.getDefaultTimeZone());
		if (startDate.equalsIgnoreCase(endDate)) {

			if (endtime.equalsIgnoreCase("00:00")
					&& starttime.equalsIgnoreCase("00:00")) {
				time = CalendarDatabaseHelper.getDateForAgenda(startDate);
				time = time.replace(",", "");

			} else if (endtime.equalsIgnoreCase("00:00")) {
				time = /*CalendarDatabaseHelper.getDateForAgenda( mAgendaListValues.get(section).get(position)
						.getAsString(Event.START_DATE))
						+ " "
						+*/ starttime
						+ " -  midnight";

			} else {
				time = /*CalendarDatabaseHelper.getDateForAgenda(startDate)
						+ " " +*/ starttime + " - " + endtime;
			}
		} else {
			if (endtime.equalsIgnoreCase("00:00")
					&& (endTimeLong - startTimeLong) < 86400000) {
				time = CalendarDatabaseHelper.getDateForAgenda(startDate)
						+ " " + starttime + " -  midnight";

			} else if (endtime.equalsIgnoreCase("00:00")) {
				// String newEndDate =
				// CalendarCommonFunction.convertMilliSecondsToDate(endTimeLong-86400000);
				time = CalendarDatabaseHelper.getDateForAgenda(startDate)
						+ " " + starttime + " - "
						+ CalendarDatabaseHelper.getDateForAgenda(endDate)
						+ " -  midnight";

			} else
				time = CalendarDatabaseHelper.getDateForAgenda(startDate)
						+ " " + starttime + " - "
						+ CalendarDatabaseHelper.getDateForAgenda(endDate)
						+ " " + endtime;
		}
		agendaTime.setText(time
				+ " "
				+ CalendarDatabaseHelper
						.getTimeZoneOffsetFromString(CalendarDatabaseHelper
								.getDefaultTimeZone()));
//		agendaTime.setText(CalendarDatabaseHelper.getTimeFromLong(mAgendaListValues
//				.get(section).get(position).getAsLong(Event.DTSTART),mAgendaListValues
//				.get(section).get(position).getAsString(Event.EVENT_TIMEZONE))
//				+ " - "
//				+ CalendarDatabaseHelper.getTimeFromLong(mAgendaListValues
//						.get(section).get(position).getAsLong(Event.DTEND),mAgendaListValues
//						.get(section).get(position).getAsString(Event.EVENT_TIMEZONE)));
		agendaTime.setTextSize(12);
		String positionObject = section + " " + position;
		TextView agendaLocation = ((TextView) layout
				.findViewById(R.id.txt_agenda_location));
		agendaLocation.setTextSize(12);
		layout.setTag(positionObject);

		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				String position[] = ((String) v.getTag()).split(" ");

				if (mAgendaListValues.get(Integer.parseInt(position[0]))
						.get(Integer.parseInt(position[1])).get(CalendarConstants.CALENDAR_TYPE_KEY)
						.equals(CalendarConstants.CALENDAR_TYPE_CORPORATE)) {
					mContentValues = CalendarDatabaseHelper
							.getEventContentValues(CalendarDatabaseHelper
									.getEventDetails(mAgendaListValues
											.get(Integer.parseInt(position[0]))
											.get(Integer.parseInt(position[1]))
											.getAsInteger("_id")));
					mContentValues.put(CalendarConstants.CALENDAR_TYPE_KEY, CalendarConstants.CALENDAR_TYPE_CORPORATE);

				} else if (mAgendaListValues.get(Integer.parseInt(position[0]))
						.get(Integer.parseInt(position[1])).get(CalendarConstants.CALENDAR_TYPE_KEY)
						.equals(CalendarConstants.CALENDAR_TYPE_PERSONAL)) {
					
						mContentValues = CalendarDBHelperClassPreICS
								.getPersonalEventContentValues(CalendarDBHelperClassPreICS
										.getEventDetails(mAgendaListValues
												.get(Integer
														.parseInt(position[0]))
												.get(Integer
														.parseInt(position[1]))
												.getAsInteger("_id")));
						mContentValues.put(CalendarConstants.CALENDAR_TYPE_KEY,
								CalendarConstants.CALENDAR_TYPE_PERSONAL);

				} else {

				}

				Bundle contentValue = new Bundle();
				contentValue.putParcelable("EventContentValues", mContentValues);
				if(!mContentValues.getAsString(CalendarConstants.CALENDAR_TYPE_KEY).equals(CalendarConstants.CALENDAR_TYPE_PERSONAL))
				{
					Intent eventIntent = new Intent(
						mContext,
						com.cognizant.trumobi.calendar.event.CalendarEditEvent.class);
					eventIntent.putExtras(contentValue);
					mContext.startActivity(eventIntent);
				}
			}
		});
		return layout;
	}

	@Override
	public View getSectionHeaderView(int section, View convertView,
			ViewGroup parent) {
		LinearLayout layout = null;
		if (convertView == null) {
			LayoutInflater inflator = (LayoutInflater) parent.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			layout = (LinearLayout) inflator.inflate(
					R.layout.cal_agenda_list_header_item, null);
		} else {
			layout = (LinearLayout) convertView;
		}

		String StartDate = mAgendaListValues.get(section).get(0)
				.getAsString("HeaderDate");
		if (StartDate == null || StartDate.equalsIgnoreCase(null)) {
			return layout;
		}
		Calendar cal = Calendar.getInstance();
		String[] days = StartDate.split("-");
		int currentYear = cal.get(Calendar.YEAR);
		int currentMonth = cal.get(Calendar.MONTH) + 1;
		int currentDay = cal.get(Calendar.DAY_OF_MONTH);
		
		if ((currentMonth > Integer.valueOf(days[1]))
				&& (currentYear >= Integer.valueOf(days[0]))) {
			layout.setBackgroundColor(mContext.getResources().getColor(R.color.layoutbg));
		} else if ((currentYear == Integer.valueOf(days[0]))
				&& (currentMonth == Integer.valueOf(days[1]))
				&& (currentDay == Integer.valueOf(days[2]))) {
			layout.setBackgroundColor(mContext.getResources().getColor(R.color.white));
		} else if ((currentMonth >= Integer.valueOf(days[1]))
				&& (currentYear >= Integer.valueOf(days[0]))) {
			if (currentDay > Integer.valueOf(days[2]))
				layout.setBackgroundColor(mContext.getResources().getColor(R.color.layoutbg));
			else
				layout.setBackgroundColor(mContext.getResources().getColor(R.color.white));

		} else {
			layout.setBackgroundColor(mContext.getResources().getColor(R.color.white));
		}
		if ((currentYear == Integer.valueOf(days[0]))
				&& (currentMonth == Integer.valueOf(days[1]))
				&& (currentDay == Integer.valueOf(days[2]))) {
			View agendatodayblack = (View) layout
					.findViewById(R.id.agendatodayblack);
			agendatodayblack.setVisibility(View.VISIBLE);

		} else {
			View agendatodayblack = (View) layout
					.findViewById(R.id.agendatodayblack);
			agendatodayblack.setVisibility(View.GONE);
		}
//		long time = mAgendaListValues.get(section).get(0).getAsLong(Event.DTSTART);
	
		TextView agendaday = (TextView) layout.findViewById(R.id.txt_agendaDay);
		agendaday.setText(CalendarUtility.getAgendaHeaderTitleDay(mAgendaListValues
				.get(section).get(0).getAsString("HeaderDate")));
		agendaday.setTextSize(12);

		TextView agendaDate = (TextView) layout
				.findViewById(R.id.txt_agendaDate);
		agendaDate.setText(CalendarUtility.getAgendaHeaderTitle(mAgendaListValues
				.get(section).get(0).getAsString("HeaderDate")));
		agendaDate.setTextSize(18);
		return layout;

	
	}

}
