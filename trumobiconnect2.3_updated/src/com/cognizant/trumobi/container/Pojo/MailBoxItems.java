package com.cognizant.trumobi.container.Pojo;

import java.util.ArrayList;
import java.util.List;

import com.cognizant.trumobi.container.Utils.UpdateDB;

import android.content.Context;
import android.util.Log;


public class MailBoxItems {
	
	public List<ChildMailbox> objList;
	
	private  String SUBJECT= "";
	private  String DATE_TIME_RECEIVED= "";
	private  String READ= "";
	private  String EMAIL_ADDRESS= "";
	private  String IMPORTANCE= "";
	private  String BOOKMARK= "";		
	private  String ATTACHMENT_ID= "";
	private  String SIZE= "";
	private  String ATTACHMENT_NAME= "";
	private  String CONTENT= "";

	public MailBoxItems(String SUBJECT,String DATE_TIME_RECEIVED,String READ,String IMPORTANCE,String EMAIL_ADDRESS){
		
		this.SUBJECT = SUBJECT;
		this.DATE_TIME_RECEIVED = DATE_TIME_RECEIVED;
		this.READ = READ;
		this.IMPORTANCE = IMPORTANCE;
		this.EMAIL_ADDRESS = EMAIL_ADDRESS;
		objList = new ArrayList<ChildMailbox>();
	}

	public void add(String ATTACHMENT_ID,String SIZE,String ATTACHMENT_NAME,String CONTENT){
		
		ChildMailbox obj = new ChildMailbox();
		obj.setAttachmentId(ATTACHMENT_ID);
		obj.setSize(SIZE);
		obj.setAttachmentName(ATTACHMENT_NAME);
		obj.setContent(CONTENT);
		obj.setSubject(SUBJECT);
		obj.setDateTimeReceived(DATE_TIME_RECEIVED);
		obj.setREAD(READ);
		obj.setImportance(IMPORTANCE);
		obj.setEmailAddress(EMAIL_ADDRESS);
		
		objList.add(obj);
		
		
	}
	
	public void updateDB(Context ctx){
		
		Log.i("Oblist ","---------> "+objList.size());
		for(int i = 0; i < objList.size() ; i++){
			
			Log.i("Oblist ","___________________________START____________________________________ \n");
			Log.i("Oblist ","---------> "+objList.get(i).getAttachmentId());
			Log.i("Oblist ","---------> "+objList.get(i).getAttachmentName());
			Log.i("Oblist ","---------> "+objList.get(i).getDateTimeReceived());
			Log.i("Oblist ","---------> "+objList.get(i).getEmailAddress());
			Log.i("Oblist ","---------> "+objList.get(i).getImportance());
			Log.i("Oblist ","---------> "+objList.get(i).getREAD());
			Log.i("Oblist ","---------> "+objList.get(i).getSize());
			Log.i("Oblist ","---------> "+objList.get(i).getSubject());
			Log.i("Oblist ","_____________________________END__________________________________ \n");
			
		}
		
		//UpdateDB.storeInboxMessages(ctx, 0, objList);
		
	}
	
	public void clearAll(){
		
		this.SUBJECT= "";
		this.DATE_TIME_RECEIVED= "";
		this.READ= "";
		this.EMAIL_ADDRESS= "";
		this.IMPORTANCE="";
		this.BOOKMARK= "";
		this.ATTACHMENT_ID= "";
		this.SIZE= "";
		this.ATTACHMENT_NAME= "";
		this.CONTENT= "";
		
	}

}
