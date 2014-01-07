package com.cognizant.trumobi.contacts.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;

public class ContactsFavoriteListAdapter extends BaseAdapter {

	ArrayList<ContactsModel> cModel;
	Context mCtx;
	LayoutInflater inflater;
	String viewOrder;
	String sortOrder ;

	public ContactsFavoriteListAdapter(Context context,
			ArrayList<ContactsModel> objects) {
		cModel = objects;
		mCtx = context;
		if(mCtx != null){
			inflater = LayoutInflater.from(mCtx);
				viewOrder = new SharedPreferences(mCtx).getString("prefViewType",
				"First_name_first");
				sortOrder = new SharedPreferences(mCtx).getString("prefSortOrder",
				ContactsConsts.CONTACT_FIRST_NAME);
		}
	
	}

	@Override
	public int getCount() {
		return cModel.size();
	}

	@Override
	public ContactsModel getItem(int position) {
		// TODO Auto-generated method stub
		return cModel.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.contacts_favorite_grid_item, parent, false);
			holder = new ViewHolder();
			holder.fav_name_txt = (TextView) convertView
					.findViewById(R.id.fav_gray_name_overlay);
			holder.fav_image_thumb = (ImageView) convertView
					.findViewById(R.id.fav_image_thumb);
			convertView.setTag(holder);
		}

		holder = (ViewHolder) convertView.getTag();

		holder.fav_image_thumb
				.setImageResource(R.drawable.contacts_ic_contact_picture_180_holo_light);

		if (viewOrder.equals("First_name_first")) {

			if (cModel.get(position).getcontacts_first_name() != null
					&& !cModel.get(position).getcontacts_first_name().isEmpty()) {

				holder.fav_name_txt.setText(cModel.get(position)
						.getcontacts_first_name()
						+ " "
						+ cModel.get(position).getcontacts_last_name());

			} else {
				holder.fav_name_txt.setText(cModel.get(position)
						.getcontacts_last_name());
			}

		} else if (viewOrder.equals("Last_name_first")) {

			if (cModel.get(position).getcontacts_last_name() != null
					&& !cModel.get(position).getcontacts_last_name().isEmpty()) {

				if (cModel.get(position).getcontacts_first_name() != null
						&& !cModel.get(position).getcontacts_first_name()
								.isEmpty()) {

					holder.fav_name_txt.setText(cModel.get(position)
							.getcontacts_last_name()
							+ ", "
							+ cModel.get(position).getcontacts_first_name());
				} else {
					holder.fav_name_txt.setText(cModel.get(position)
							.getcontacts_last_name());
				}
			} else {

				holder.fav_name_txt.setText(cModel.get(position)
						.getcontacts_first_name());

			}

		}

		byte[] bmpByteArray = cModel.get(position).getContacts_image();
		if (bmpByteArray != null) {
			Bitmap bmp = ContactsUtilities.getImage(bmpByteArray);
			if (bmp != null) {
				holder.fav_image_thumb.setImageBitmap(bmp);
			} else {
				holder.fav_image_thumb
						.setImageResource(R.drawable.contacts_ic_contact_picture_180_holo_light);
			}
		}

		return convertView;

	}

	static class ViewHolder {
		TextView fav_name_txt;
		ImageView fav_image_thumb;
	}
}
