package com.cognizant.trumobi.container.AsynctaskCallback;

import java.util.List;

import android.content.Context;

import com.cognizant.trumobi.container.Pojo.ChildMailbox;

public interface SecAppFileListener {
	
	public void onUiUpdateListner(int position,
			List<ChildMailbox> deleteAttachmnetList);
	
	public void onTitleBookmarkedUpdate(int pos,boolean isChecked);
	public void onRemoteCallback(boolean result,String path,int openType,Context ctx,String ext);
	

}
