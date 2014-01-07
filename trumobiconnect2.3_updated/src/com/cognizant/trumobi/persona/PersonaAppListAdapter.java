package com.cognizant.trumobi.persona;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.net.PersonaAllAppsListDetails;

public class PersonaAppListAdapter extends BaseAdapter {
	private static LayoutInflater inflater = null;

	LinkedList<PersonaAllAppsListDetails> list = null;
	int count;
	int position;

	public PersonaAppListAdapter(Activity activity,
			LinkedList<PersonaAllAppsListDetails> allAppsLists) {
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.count = allAppsLists.size();
		list = allAppsLists;
	}

	public class AllAppViewHolder {
		ImageView app_icon;

		TextView app_name;
		int id;
	}

	public int getCount() {
		return count;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		this.position = position;
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final AllAppViewHolder mHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.pr_persona_app_item, null);
			mHolder = new AllAppViewHolder();

			mHolder.id = position;
			mHolder.app_icon = (ImageView) convertView
					.findViewById(R.id.homeScreenAppIconView);
			
			// mHolder.app_icon.setBackgroundResource(R.drawable.email_icon);
			mHolder.app_name = (TextView) convertView
					.findViewById(R.id.grid_item_label);
			// mHolder.app_name.setText("Email");

		} else
			mHolder = (AllAppViewHolder) convertView.getTag();

		String appIconImageUrl = PersonaConstants.STAGING
				+ list.get(position).icon_image_url;

	//	PersonaLog.d("APPS ADAPTER", "ImageUrl :" + appIconImageUrl);

		String appItemName = list.get(position).appstore_item_name;
		
		//Bitmap placeholder = aq.getCachedImage(R.drawable.email_icon_grey);

		/*
		 * switch(PersonaApplicationConstants.PACKAGE_NAME.valueOf(list.get(position).
		 * bundle_identifier)){
		 * 
		 * case EMAIL_PACKAGE:
		 */
		
		//trumobiedits as a part of changes with ADW
		
		/*AQUtility.setCacheDir(PersonaApplicationConstants.cacheDir);
		AQuery aq = new AQuery(convertView);
		if (list.get(position).bundle_identifier
				.contains(PersonaApplicationConstants.EMAIL_PACKAGE)) {
			aq.id(R.id.homeScreenAppIconView)
			.progress(R.id.progressImageIcon)
			.image(R.drawable.email_icon);
			aq.id(R.id.grid_item_label).progress(R.id.progressImageIcon)
			.text(appItemName);
			if (!list.get(position).getAppInstalled()) {

				aq.id(R.id.install_layout).visible();

			}

		}
		if (list.get(position).bundle_identifier
				.contains(PersonaApplicationConstants.FILE_VIEWER_PACKAGE)) {
			
			aq.id(R.id.homeScreenAppIconView)
			.progress(R.id.progressImageIcon)
			.image(R.drawable.file_viewer_icon);
	aq.id(R.id.grid_item_label).progress(R.id.progressImageIcon)
			.text(appItemName);
			if (!list.get(position).getAppInstalled()) {
				aq.id(R.id.install_layout).visible();

			}

		}
		if (list.get(position).bundle_identifier
				.contains(PersonaApplicationConstants.CONTACTS_PACKAGE)) {
			
				aq.id(R.id.homeScreenAppIconView)
						.progress(R.id.progressImageIcon)
						.image(R.drawable.contacts_icon);
				aq.id(R.id.grid_item_label).progress(R.id.progressImageIcon)
						.text(appItemName);

				if (!list.get(position).getAppInstalled()) {
				
				aq.id(R.id.install_layout).visible();

			}
		}
		if (list.get(position).bundle_identifier
				.contains(PersonaApplicationConstants.CALENDAR_PACKAGE)) {
		
				aq.id(R.id.homeScreenAppIconView)
						.progress(R.id.progressImageIcon)
						.image(R.drawable.calendar_icon);
				aq.id(R.id.grid_item_label).progress(R.id.progressImageIcon)
						.text(appItemName);

				if (!list.get(position).getAppInstalled()) {
				aq.id(R.id.install_layout).visible();

			}
		}
		if (list.get(position).bundle_identifier
				.contains(PersonaApplicationConstants.SECURE_BROWSER_PACKAGE)) {
			
				aq.id(R.id.homeScreenAppIconView)
						.progress(R.id.progressImageIcon)
						.image(R.drawable.browser_icon);
				aq.id(R.id.grid_item_label).progress(R.id.progressImageIcon)
						.text(appItemName);

				if (!list.get(position).getAppInstalled()) {
				
				aq.id(R.id.install_layout).visible();

			}
		}
		
		if (list.get(position).bundle_identifier
				.contains(PersonaApplicationConstants.CONNECT_PACKAGE)) {
			
				aq.id(R.id.homeScreenAppIconView)
						.progress(R.id.progressImageIcon)
						.image(R.drawable.connect_icon);
				aq.id(R.id.grid_item_label).progress(R.id.progressImageIcon)
						.text(appItemName);

				if (!list.get(position).getAppInstalled()) {
				
				aq.id(R.id.install_layout).visible();

			}
		}
		*/

		return convertView;
	}

	public static Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
