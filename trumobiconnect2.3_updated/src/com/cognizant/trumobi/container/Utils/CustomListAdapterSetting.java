package com.cognizant.trumobi.container.Utils;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cognizant.trumobi.R;

public class CustomListAdapterSetting extends BaseAdapter {

	private ArrayList<SettingItem> listData;

	private LayoutInflater layoutInflater;

	Context ctx;

	// public ViewHolder holder;

	public CustomListAdapterSetting(Context context,
			ArrayList<SettingItem> listData) {
		this.listData = listData;
		layoutInflater = LayoutInflater.from(context);
		ctx = context;
	}

	@Override
	public int getCount() {
		return listData.size();
	}

	@Override
	public Object getItem(int position) {
		return listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater
					.inflate(R.layout.con_setting_row, null);
			holder = new ViewHolder();
			// holder.headlineView = (TextView)
			// convertView.findViewById(R.id.title);
			holder.reporterNameView = (TextView) convertView
					.findViewById(R.id.reporter);
			holder.reportedDateView = (TextView) convertView
					.findViewById(R.id.date);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// holder.headlineView.setText(listData.get(position).getHeadline());

		holder.reporterNameView
				.setTypeface(UtilList.getTextTypeFaceNormal(ctx));
		holder.reportedDateView
				.setTypeface(UtilList.getTextTypeFaceNormal(ctx));

		holder.reporterNameView.setText(""
				+ listData.get(position).getReporterName());
		holder.reportedDateView.setText(listData.get(position).getDate());

		return convertView;
	}

	static class ViewHolder {
		// TextView headlineView;
		TextView reporterNameView;
		TextView reportedDateView;
	}

}
