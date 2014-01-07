package com.cognizant.trumobi.dialer.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.contacts.utils.ContactsDialog;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;
import com.cognizant.trumobi.log.DialerLog;

public class DialerContactAdapter extends BaseAdapter {

	
	private int selectedPos = -1;
	String lastChar = "";
	Cursor dataCursor;
	Context mCtx;
	LayoutInflater mInflater;
	ArrayList<ContactsModel> cModel;


	public DialerContactAdapter(Context context, ArrayList<ContactsModel> objects) {
		cModel = objects;
		mCtx = context;
		mInflater = LayoutInflater.from(mCtx);
	}
	
	public void setSelectedPosition(int pos){
		selectedPos = pos;
		// inform the view of this change
		notifyDataSetChanged();
	}

	public int getSelectedPosition(){
		return selectedPos;
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
		// TODO Auto-generated method stub
		//final int viewPosition = position;
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.contacts_custom_contact_list,parent, false);
			holder = new ViewHolder();
			holder.headerTxt = (TextView) convertView.findViewById(R.id.contact_primary_name_header);
			holder.firstNameTxt = (TextView) convertView.findViewById(R.id.contact_primary_name);
			holder.secNameTxt = (TextView) convertView.findViewById(R.id.contact_sec_name);
			holder.phoneNameTxt = (TextView) convertView.findViewById(R.id.contact_prim_phone_no);
			holder.contactImage = (ImageView) convertView.findViewById(R.id.contact_image);
			holder.corporateContact = (ImageView) convertView.findViewById(R.id.corporate_cntx_icon);
			convertView.setTag(holder);
		}
		
		holder = (ViewHolder) convertView.getTag();
		holder.headerTxt.setVisibility(View.GONE);
		//this.dataCursor.moveToPosition(viewPosition);
		holder.contactImage.setImageResource(R.drawable.contacts_ic_contact_picture_180_holo_light);
		SharedPreferences sortPrefs = PreferenceManager.getDefaultSharedPreferences(mCtx.getApplicationContext());
		String viewOrder = sortPrefs.getString("prefViewType","First_name_first");
		String sortOrder = sortPrefs.getString("prefSortOrder",ContactsConsts.CONTACT_FIRST_NAME);

		if (sortOrder.equals("first_name")) {
						
			holder.headerTxt.setVisibility(View.GONE);
			//String thisname = (String) dataCursor.getString(dataCursor.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME));
			String thisname = cModel.get(position).getcontacts_first_name();
			
			String thisalpha = null;
			String prevname = null;

			String prevalpha = null;
			if (thisname != null && !thisname.isEmpty()) {
				thisalpha = (String) thisname.subSequence(0, 1);

			}
			else
			{
				holder.headerTxt.setVisibility(View.GONE);
			}
			
			int prePosition = position-1;
			if(cModel.size() >0 &&  prePosition >=  0){
				prevname = cModel.get(prePosition).getcontacts_first_name();
				
				if (prevname != null && !prevname.isEmpty()) {
					prevalpha = (String) prevname.subSequence(0, 1);

					if (!prevalpha.equalsIgnoreCase(thisalpha)) {
						holder.headerTxt.setVisibility(View.VISIBLE);
						holder.headerTxt.setText(thisalpha);
						holder.headerTxt.setFocusable(false);
						holder.headerTxt.setClickable(false);
					}
			}
			else{
					holder.headerTxt.setVisibility(View.GONE);
					if (thisname != null && !thisname.isEmpty()) {
						holder.headerTxt.setVisibility(View.VISIBLE);
						holder.headerTxt.setText(thisalpha);
						holder.headerTxt.setFocusable(false);
						holder.headerTxt.setClickable(false);
					} 
					else {
						holder.headerTxt.setVisibility(View.GONE);
					}
				
				}
			}
			else
			{
				holder.headerTxt.setVisibility(View.GONE);
				if (thisname != null && !thisname.isEmpty()) {
					holder.headerTxt.setVisibility(View.VISIBLE);
					holder.headerTxt.setText(thisalpha);
					holder.headerTxt.setFocusable(false);
					holder.headerTxt.setClickable(false);
				} 
				else {
					holder.headerTxt.setVisibility(View.GONE);
				}
			}
			
		} else if (sortOrder.equals("last_name")) {
			
			
			
			

			String thisname = cModel.get(position).getcontacts_last_name();
			String thisalpha = null;
			String prevname = null;
			String prevalpha = null;
			if (thisname != null && !thisname.isEmpty()) {
				thisalpha = (String) thisname.subSequence(0, 1);
			}
			// previous row, for comparison
			
			
			
			
			int prePosition = position-1;
			if(cModel.size() >0 &&  prePosition >=  0){
				prevname = cModel.get(prePosition).getcontacts_last_name();			
				if (prevname != null && !prevname.isEmpty()) {
					prevalpha = (String) prevname.subSequence(0, 1);
					if (!prevalpha.equalsIgnoreCase(thisalpha)) {
						holder.headerTxt.setVisibility(View.VISIBLE);
						holder.headerTxt.setText(thisalpha);
						holder.headerTxt.setFocusable(false);
						holder.headerTxt.setClickable(false);
					}
				} else {
					if (thisname != null && !thisname.isEmpty()) {
						holder.headerTxt.setVisibility(View.VISIBLE);
						holder.headerTxt.setText(thisalpha);
						holder.headerTxt.setFocusable(false);
						holder.headerTxt.setClickable(false);
					} else {						
						//working fine
						holder.headerTxt.setVisibility(View.GONE);					
					}
				}
				//dataCursor.moveToNext();
			}
		
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			/*String thisname =cModel.get(position).getcontacts_last_name();
			String thisalpha = null;
			String prevname = null;

			String prevalpha = null;
			if (thisname != null && !thisname.isEmpty()) {
				thisalpha = (String) thisname.subSequence(0, 1);

			}
			
			int prePosition = position-1;
			if(cModel.size() >0 &&  prePosition >  0){
				prevname = cModel.get(prePosition).getcontacts_last_name();
				
				if (prevname != null && !prevname.isEmpty()) {
					prevalpha = (String) prevname.subSequence(0, 1);

					if (!prevalpha.equalsIgnoreCase(thisalpha)) {
						holder.headerTxt.setVisibility(View.VISIBLE);
						holder.headerTxt.setText(thisalpha);
						holder.headerTxt.setFocusable(false);
						holder.headerTxt.setClickable(false);
					}
				} else {
					if (thisname != null && !thisname.isEmpty()) {
						holder.headerTxt.setVisibility(View.VISIBLE);
						holder.headerTxt.setText(thisalpha);
						holder.headerTxt.setFocusable(false);
						holder.headerTxt.setClickable(false);
					} else {
						
						//working fine
						holder.headerTxt.setVisibility(View.GONE);
					}
				}
			}*/
		}

		if (viewOrder.equals("First_name_first")) {
			holder.secNameTxt.setText(cModel.get(position).getcontacts_last_name());
			holder.firstNameTxt.setText(cModel.get(position).getcontacts_first_name());

		} else if (viewOrder.equals("Last_name_first")) {

			if (cModel.get(position).getcontacts_first_name().equals("")) {
				holder.firstNameTxt.setText(cModel.get(position).getcontacts_last_name());
				holder.secNameTxt.setText("");
				
			} 
			else if(cModel.get(position).getcontacts_last_name().equals(""))
			{
				holder.firstNameTxt.setText("");
				holder.secNameTxt.setText(cModel.get(position).getcontacts_first_name());
			}
			else {
				holder.firstNameTxt.setText(cModel.get(position).getcontacts_last_name()+ ",");
				
				holder.secNameTxt.setText(cModel.get(position).getcontacts_first_name());
				
			}
		}
		holder.phoneNameTxt.setText(cModel.get(position).getcontacts_mobile_telephone_number());

		byte[] bmpByteArray = cModel.get(position).getContacts_image();
		if (bmpByteArray != null) {
			Bitmap bmp = ContactsUtilities.getImage(bmpByteArray);
			if (bmp != null) {
				holder.contactImage.setImageBitmap(bmp);
			} else {
				holder.contactImage
						.setImageResource(R.drawable.contacts_ic_contact_picture_180_holo_light);
			}
		}
		//Log.i("=======================  :::::   ", ""+cModel.get(position).isNativeContact());
		
		if(cModel.get(position).isNativeContact()){
			
			holder.corporateContact.setVisibility(View.GONE);
		}else{
			holder.corporateContact.setVisibility(View.VISIBLE);
			DialerLog.i("=======================  :::::   ", ""+cModel.get(position).getcontacts_first_name());
		}
		
		holder.contactImage.setTag(new Integer(position));
		holder.contactImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				Object obj = cModel.get((Integer) view.getTag());
				ContactsModel contact = (ContactsModel) obj;
				new ContactsDialog(mCtx, (ContactsModel) obj);
				
			}
		});
		convertView.setBackgroundColor(Color.TRANSPARENT);
		 if(selectedPos == position){
			 convertView.setBackgroundColor(Color.LTGRAY);
	      }
		 
		return convertView;
	}

	static class ViewHolder {
		TextView headerTxt;
		TextView firstNameTxt;
		TextView secNameTxt;
		TextView phoneNameTxt;
		ImageView contactImage;
		
		ImageView corporateContact;
	}
}
