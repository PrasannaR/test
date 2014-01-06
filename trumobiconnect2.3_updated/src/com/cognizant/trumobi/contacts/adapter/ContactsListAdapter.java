package com.cognizant.trumobi.contacts.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
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
import com.cognizant.trumobi.contacts.activity.ContactsDialogActivity;
import com.cognizant.trumobi.contacts.utils.ContactsDialog;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;

public class ContactsListAdapter extends BaseAdapter {


	private int selectedPos = -1;
	String lastChar = "";
	Cursor dataCursor;
	Context mCtx;
	LayoutInflater mInflater;
	ArrayList<ContactsModel> cModel;
	String viewOrder;
	String sortOrder;


	public ContactsListAdapter(Context context, ArrayList<ContactsModel> objects) {
		cModel = objects;
		mCtx = context;
		if(mCtx != null){
			mInflater = LayoutInflater.from(mCtx);
			viewOrder = new SharedPreferences(mCtx.getApplicationContext()).getString("prefViewType","First_name_first");
			sortOrder = new SharedPreferences(mCtx.getApplicationContext()).getString("prefSortOrder",ContactsConsts.CONTACT_FIRST_NAME);
		}
		
		
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
	public View getView(final int position, View convertView, ViewGroup parent) {
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

					if (thisname != null && !thisname.isEmpty()) 
					{
						if (!prevalpha.equalsIgnoreCase(thisalpha)) {
							holder.headerTxt.setVisibility(View.VISIBLE);
							holder.headerTxt.setText(thisalpha);
							holder.headerTxt.setFocusable(false);
							holder.headerTxt.setClickable(false);
						}
					}
					else
					{
						
						if(cModel.get(position).getcontacts_last_name() != null && !cModel.get(position).getcontacts_last_name().isEmpty()){
							if (!prevalpha.equalsIgnoreCase(cModel.get(position).getcontacts_last_name().subSequence(0, 1).toString())) {
								
								holder.headerTxt.setVisibility(View.VISIBLE);						
								holder.headerTxt.setText(cModel.get(position).getcontacts_last_name().subSequence(0, 1));
								holder.headerTxt.setFocusable(false);
								holder.headerTxt.setClickable(false);

								}
						}
						
					}
				}
				else{
					holder.headerTxt.setVisibility(View.GONE);
					prevname = cModel.get(prePosition).getcontacts_last_name();

					if (prevname != null && !prevname.isEmpty()) {
						prevalpha = (String) prevname.subSequence(0, 1);
					if (thisname != null && !thisname.isEmpty()) {
						if (!prevalpha.equalsIgnoreCase(thisalpha)) {
						holder.headerTxt.setVisibility(View.VISIBLE);
						holder.headerTxt.setText(thisalpha);
						holder.headerTxt.setFocusable(false);
						holder.headerTxt.setClickable(false);
						}
					} 
					else {
						//holder.headerTxt.setVisibility(View.GONE);

						if(cModel.get(position).getcontacts_last_name() != null && !cModel.get(position).getcontacts_last_name().isEmpty()){
							if (!prevalpha.equalsIgnoreCase(cModel.get(position).getcontacts_last_name().subSequence(0, 1).toString())) {
								
								
								holder.headerTxt.setVisibility(View.VISIBLE);					

								holder.headerTxt.setText(cModel.get(position).getcontacts_last_name().subSequence(0, 1));
								holder.headerTxt.setFocusable(false);
								holder.headerTxt.setClickable(false);
								}
						}
						
					}
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
					//holder.headerTxt.setVisibility(View.GONE);

					holder.headerTxt.setVisibility(View.VISIBLE);	
					if(cModel.get(position).getcontacts_last_name() != null && !cModel.get(position).getcontacts_last_name().isEmpty()){
						holder.headerTxt.setText(cModel.get(position).getcontacts_last_name().subSequence(0, 1));
						holder.headerTxt.setFocusable(false);
						holder.headerTxt.setClickable(false);
					}
					

				}
			}

		} else if (sortOrder.equals("last_name")) {
			
			


			holder.headerTxt.setVisibility(View.GONE);
			//String thisname = (String) dataCursor.getString(dataCursor.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME));
			String thisname = cModel.get(position).getcontacts_last_name();

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
				prevname = cModel.get(prePosition).getcontacts_last_name();

				if (prevname != null && !prevname.isEmpty()) {
					prevalpha = (String) prevname.subSequence(0, 1);

					if (thisname != null && !thisname.isEmpty()) 
					{
						if (!prevalpha.equalsIgnoreCase(thisalpha)) {
							holder.headerTxt.setVisibility(View.VISIBLE);
							holder.headerTxt.setText(thisalpha);
							holder.headerTxt.setFocusable(false);
							holder.headerTxt.setClickable(false);
						}
					}
					else
					{
						if(cModel.get(position).getcontacts_first_name() != null && !cModel.get(position).getcontacts_first_name().isEmpty()){
							if (!prevalpha.equalsIgnoreCase(cModel.get(position).getcontacts_first_name().subSequence(0, 1).toString())) {
								holder.headerTxt.setVisibility(View.VISIBLE);						
								holder.headerTxt.setText(cModel.get(position).getcontacts_first_name().subSequence(0, 1));
								holder.headerTxt.setFocusable(false);
								holder.headerTxt.setClickable(false);
							}
						}
						
						

					}
				}
				else{
					holder.headerTxt.setVisibility(View.GONE);
					prevname = cModel.get(prePosition).getcontacts_first_name();

					if (prevname != null && !prevname.isEmpty()) {
						prevalpha = (String) prevname.subSequence(0, 1);
					if (thisname != null && !thisname.isEmpty()) {
						if (!prevalpha.equalsIgnoreCase(thisalpha)) {
						holder.headerTxt.setVisibility(View.VISIBLE);
						holder.headerTxt.setText(thisalpha);
						holder.headerTxt.setFocusable(false);
						holder.headerTxt.setClickable(false);
						}
					} 
					else {
						//holder.headerTxt.setVisibility(View.GONE);
						
						if(cModel.get(position).getcontacts_first_name() != null && !cModel.get(position).getcontacts_first_name().isEmpty()){
							if (!prevalpha.equalsIgnoreCase(cModel.get(position).getcontacts_first_name().subSequence(0, 1).toString())) {

								holder.headerTxt.setVisibility(View.VISIBLE);					

								holder.headerTxt.setText(cModel.get(position).getcontacts_first_name().subSequence(0, 1));
								holder.headerTxt.setFocusable(false);
								holder.headerTxt.setClickable(false);
								}
						}
						

					}
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
					//holder.headerTxt.setVisibility(View.GONE);

					holder.headerTxt.setVisibility(View.VISIBLE);					
					if(cModel.get(position).getcontacts_first_name() != null && !cModel.get(position).getcontacts_first_name().isEmpty()){
						holder.headerTxt.setText(cModel.get(position).getcontacts_first_name().subSequence(0, 1));
						holder.headerTxt.setFocusable(false);
						holder.headerTxt.setClickable(false);
					}
					

				}
			}

		
			
		}

		if (viewOrder.equals("First_name_first")) {
			/*holder.secNameTxt.setText(cModel.get(position).getcontacts_last_name());
			holder.firstNameTxt.setText(cModel.get(position).getcontacts_first_name());
*/
			
			
			
			if (cModel.get(position).getcontacts_last_name() != null
					&& !cModel.get(position).getcontacts_last_name().isEmpty())
			{
			holder.secNameTxt.setText(cModel.get(position).getcontacts_last_name());
			}
			else
			{
				holder.secNameTxt.setText("");
			}
			if (cModel.get(position).getcontacts_first_name() != null
					&& !cModel.get(position).getcontacts_first_name().isEmpty())
			{
			holder.firstNameTxt.setText(cModel.get(position).getcontacts_first_name());
			}
			else
			{
				holder.firstNameTxt.setText("");
			}
			
		} else if (viewOrder.equals("Last_name_first")) {
			
			
			
			if(cModel.get(position).getcontacts_first_name() != null
					&& !cModel.get(position).getcontacts_first_name().isEmpty())
			{
				
				if (cModel.get(position).getcontacts_last_name() != null
						&& !cModel.get(position).getcontacts_last_name().isEmpty())
				{
					holder.firstNameTxt.setText(cModel.get(position)
							.getcontacts_last_name() + ",");

					holder.secNameTxt.setText(cModel.get(position)
							.getcontacts_first_name());
				}
				else
				{
					holder.firstNameTxt.setText("");
					holder.secNameTxt.setText(cModel.get(position).getcontacts_first_name());
				}
				
			}
			else
			{
				if (cModel.get(position).getcontacts_last_name() != null
						&& !cModel.get(position).getcontacts_last_name().isEmpty())
				{
				holder.firstNameTxt.setText(cModel.get(position).getcontacts_last_name());
				holder.secNameTxt.setText("");
				}
			}
			
					

		/*	if (cModel.get(position).getcontacts_first_name().equals("") && cModel.get(position).getcontacts_first_name()
					.isEmpty()) {
				holder.firstNameTxt.setText(cModel.get(position)
						.getcontacts_last_name());
				holder.secNameTxt.setText("");

			} else if (cModel.get(position).getcontacts_last_name().equals("") && cModel.get(position).getcontacts_last_name()
					.isEmpty()) {
				holder.firstNameTxt.setText("");
				holder.secNameTxt.setText(cModel.get(position)
						.getcontacts_first_name());
			} else {
				holder.firstNameTxt.setText(cModel.get(position)
						.getcontacts_last_name() + ",");

				holder.secNameTxt.setText(cModel.get(position)
						.getcontacts_first_name());

			}*/
		}
		//holder.phoneNameTxt.setText(cModel.get(position).getcontacts_mobile_telephone_number());

		/*byte[] bmpByteArray = cModel.get(position).getContacts_image();
		if (bmpByteArray != null) {
			Bitmap bmp = ContactsUtilities.getImage(bmpByteArray);
			if (bmp != null) {
				holder.contactImage.setImageBitmap(bmp);
			} else {
				holder.contactImage
				.setImageResource(R.drawable.contacts_ic_contact_picture_180_holo_light);
			}
		}*/
		//Log.i("=======================  :::::   ", ""+cModel.get(position).isNativeContact());

	/*	if(cModel.get(position).isNativeContact()){

			holder.corporateContact.setVisibility(View.GONE);
		}else{
			holder.corporateContact.setVisibility(View.VISIBLE);
		}
*/
		
		
		
		
		if (cModel.get(position).isNativeContact()
				|| cModel.get(position).isContactfromSearch()) {

			holder.corporateContact.setVisibility(View.GONE);
		} else {
			holder.corporateContact.setVisibility(View.VISIBLE);
		}
		
		holder.contactImage.setTag(new Integer(position));
		holder.contactImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				Object obj = cModel.get((Integer) view.getTag());
				ContactsModel contact = (ContactsModel) obj;
				Intent myIntent = new Intent(mCtx,ContactsDialogActivity.class);
				Bundle myBundle = new Bundle();
				myBundle.putSerializable("contact", cModel.get(position));
				myIntent.putExtra("bundle", myBundle);
				mCtx.startActivity(myIntent);
				
				//Object obj = cModel.get((Integer) view.getTag());
				//ContactsModel contact = (ContactsModel) obj;
				//new ContactsDialog(mCtx, cModel.get(position));
				

			}
		});
		holder.position = position;
		convertView.setBackgroundColor(Color.TRANSPARENT);
		if(selectedPos == position){
			convertView.setBackgroundColor(Color.LTGRAY);
		}

		if(cModel.get(position).isNativeContact()){
			ContactsImageTask contactsImageTask = new ContactsImageTask(position , holder, mCtx);
			contactsImageTask.execute(String.valueOf(cModel.get(position).getContact_id()));
		}else{
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
		}
		
	        
		return convertView;
	}

	
	private static class ContactsImageTask extends AsyncTask<String, Void, byte[]> {
	    private int mPosition;
	    private ViewHolder mHolder;
	    private Context mContext;

	    public ContactsImageTask(int position, ViewHolder holder, Context context) {
	        mPosition = position;
	        mHolder = holder;
	        mContext = context;
	    }

		@Override
		protected byte[] doInBackground(String... params) {
			// TODO Auto-generated method stub
			//Contact Photo Cursor
			byte[] imagebyte = null;
			int id= Integer.parseInt(params[0]);
			String[] photocols = { ContactsContract.CommonDataKinds.Photo.PHOTO };
			String photofilter = ContactsContract.Data.CONTACT_ID + " = ? "
					+ " AND " + ContactsContract.Data.MIMETYPE + " = ?";
			String[] photoparams = { String.valueOf(id) ,  ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE};
		    Cursor photoCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, photocols, photofilter, photoparams, null);

		    while (photoCur.moveToNext()) {
		    	if(photoCur.getBlob(photoCur.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO)) != null){
		    		imagebyte = photoCur.getBlob(photoCur.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
		    	}
		    }
		    photoCur.close();
			return imagebyte;
		}
		
		@Override
	    protected void onPostExecute(byte[] bmpByteArray) {
			if(bmpByteArray != null){
				Bitmap bmp = ContactsUtilities.getImage(bmpByteArray);
		        if (mHolder.position == mPosition) {
		        	if (bmp != null) {
		        		mHolder.contactImage.setImageBitmap(bmp);
					} else {
						mHolder.contactImage.setImageResource(R.drawable.contacts_ic_contact_picture_180_holo_light);
					}
		        }
			}
			
	    }
	}

	
	static class ViewHolder {
		TextView headerTxt;
		TextView firstNameTxt;
		TextView secNameTxt;
		TextView phoneNameTxt;
		ImageView contactImage;
		ImageView corporateContact;
		int position;
	}

}