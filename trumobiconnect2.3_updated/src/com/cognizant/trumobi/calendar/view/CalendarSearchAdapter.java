package com.cognizant.trumobi.calendar.view;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.cognizant.trumobi.R;

public class CalendarSearchAdapter extends ArrayAdapter<String> {

	private ArrayList<String> mOriginalEventList;
	private ArrayList<String> mFirstItems;
	private Filter mFilter;
	private LayoutInflater mInflater;
	private ArrayList<String> mListItem = new ArrayList<String>();

	public CalendarSearchAdapter(Context context, int resource,
			int textViewResourceId, ArrayList<String> objects) {
		super(context, resource, textViewResourceId, objects);
		mListItem = objects;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mOriginalEventList = new ArrayList<String>(objects);
		this.mFirstItems = new ArrayList<String>(objects);

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListItem.size();
	}

	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub
		if (mFilter == null)
			mFilter = new PkmnNameFilter();

		return mFilter;
	}

	@Override
	public String getItem(int position) {
		if(mFirstItems!=null)
		{
			return mFirstItems.get(position);
		}
		else
		{
			return " ";
		}
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = mInflater.inflate(
					R.layout.cal_custom_auto_complete_textview, null);
		}

		TextView textView = (TextView) convertView
				.findViewById(R.id.autoCompleteTextItem);
		textView.setText(mFirstItems.get(position));
		return convertView;
	}
	private class PkmnNameFilter extends Filter {
		
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			String prefix = constraint.toString().toLowerCase();

			if (prefix == null || prefix.length() == 0) {
				ArrayList<String> list = new ArrayList<String>(mOriginalEventList);
				results.values = list;
				results.count = list.size();
			} else {
				final ArrayList<String> list = new ArrayList<String>(mOriginalEventList);
				final ArrayList<String> nlist = new ArrayList<String>();
				int count = list.size();

				for (int i = 0; i < count; i++) {
					final String pkmn = list.get(i);
					final String value = pkmn.toLowerCase();

					if (value.startsWith(prefix)) {
						nlist.add(pkmn);
					}
				}
				results.values = nlist;
				results.count = nlist.size();
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			mFirstItems = (ArrayList<String>) results.values;
			if (mFirstItems != null) {
				clear();
				int count = mFirstItems.size();
				for (int i = 0; i < count; i++) {

					String pkmn = mFirstItems.get(i);
					add(pkmn);
				}
			}

		}

	}
}
