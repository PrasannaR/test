package com.cognizant.trumobi.messenger.adapter;

import java.util.ArrayList;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.messenger.sms.SmsContactBean;
import com.google.common.base.Strings;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class SmsContactPickerAdapter extends ArrayAdapter<SmsContactBean>
		implements Filterable {

	private ArrayList<SmsContactBean> contactList, cloneContactList;
	private LayoutInflater layoutInflater;

	@SuppressWarnings("unchecked")
	public SmsContactPickerAdapter(Context context, int textViewResourceId,
			ArrayList<SmsContactBean> contactList) {
		super(context, textViewResourceId);
		this.contactList = contactList;
		this.cloneContactList = (ArrayList<SmsContactBean>) this.contactList
				.clone();
		layoutInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public int getCount() {

		return contactList.size();
	}

	@Override
	public SmsContactBean getItem(int position) {

		return contactList.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Holder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(
					R.layout.sms_contact_list_item, null);
			holder = new Holder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.phone = (TextView) convertView.findViewById(R.id.phone);
			holder.type = (TextView) convertView.findViewById(R.id.type);
			holder.photo = (ImageView) convertView
					.findViewById(R.id.contact_image);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		SmsContactBean contact = getItem(position);
		holder.name.setText(contact.name);
		holder.phone.setText(contact.phoneNo);
		holder.type.setText(contact.type);
		holder.photo.setImageBitmap(BitmapFactory.decodeByteArray(
				contact.getByteImage(), 0, contact.getByteImage().length));
		return convertView;
	}

	@Override
	public Filter getFilter() {
		Filter contactFilter = new Filter() {

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				if (results.values != null) {
					contactList = (ArrayList<SmsContactBean>) results.values;
					notifyDataSetChanged();
				}

			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {

				String sortValue = constraint == null ? "" : constraint
						.toString().toLowerCase();
				FilterResults filterResults = new FilterResults();
				if (!TextUtils.isEmpty(sortValue.trim())) {
					ArrayList<SmsContactBean> sortedContactList = new ArrayList<SmsContactBean>();
					for (SmsContactBean contact : cloneContactList) {

						if (!Strings.isNullOrEmpty(contact.getPhoneNo())) {
							if (contact.getName().toLowerCase()
									.startsWith(sortValue)
									|| contact.getPhoneNo().toLowerCase()
											.startsWith(sortValue))
								sortedContactList.add(contact);
						}
					}

					filterResults.values = sortedContactList;
					filterResults.count = sortedContactList.size();

				}
				return filterResults;
			}

			@Override
			public CharSequence convertResultToString(Object resultValue) {
				// need to save this to saved contact
				return ((SmsContactBean) resultValue).name;
			}
		};

		return contactFilter;
	}

	@SuppressWarnings("unchecked")
	public void setContactList(ArrayList<SmsContactBean> contactList) {
		// this isn't the efficient method
		// need to improvise on this
		this.contactList = contactList;
		this.cloneContactList = (ArrayList<SmsContactBean>) this.contactList
				.clone();
		notifyDataSetChanged();
	}

	public static class Holder {
		public TextView phone, name, type;
		public ImageView photo;
	}

}
