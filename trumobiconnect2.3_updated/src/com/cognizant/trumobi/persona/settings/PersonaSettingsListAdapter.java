package com.cognizant.trumobi.persona.settings;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cognizant.trumobi.R;


public class PersonaSettingsListAdapter extends BaseAdapter{
	private static LayoutInflater inflater = null;
	List<String> mList;
	Context mContext;
	ViewHolder mviewHolder;
	int count;
	int position;
	int deviceType;
	public PersonaSettingsListAdapter(Activity activity,List<String> list){
		inflater=(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	mList=	list;
	mContext=activity;
	deviceType=mContext.getResources().getInteger(R.integer.deviceType);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		// TODO Auto-generated method stub
		
		
			view=inflater.inflate(R.layout.pr_settings_menu_row, null);
			mviewHolder 	 = new ViewHolder();
			mviewHolder.mListItem=  (TextView) view.findViewById(R.id.prlistItem);
			mviewHolder.mListItem.setText(mList.get(position));
			mviewHolder.mListItem.setPadding(20, 20, 20, 20);
			
			
			
			if(deviceType==1){
			if(PersonaSettingsListFragment.selectedItem==position){
				mviewHolder.mListItem. setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.pr_list_selected_bg));
			}
			else{
				mviewHolder.mListItem. setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.pr_list_bg));
			}
			}
		//mviewHolder.mListItem. setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.pr_list_selected_bg));
		//mviewHolder.mListItem.setPadding(20, 20, 20, 20);
			//mList.get(position);
			//mviewHolder = (ViewHolder) view.getTag();	
		
		mviewHolder = (ViewHolder) view.getTag();	
		return view;
	}

	static class ViewHolder {
		TextView mListItem;
	}
	
	public PersonaSettingsListAdapter() {
		// TODO Auto-generated constructor stub
	}
	
}
