package com.cognizant.trumobi.container.Adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.container.AsynctaskCallback.SecAppFileListener;
import com.cognizant.trumobi.container.Pojo.ChildMailbox;
import com.cognizant.trumobi.container.Utils.UtilList;

/**
 * MailListAdapter, get the data from DB and populate to the mail items list.
 * 
 * @author Sri Sandhiya D. <srisandhiya.dhanabal@cognizant.com>
 * 
 * 
 */
public class AttachmentListAdapterMail extends BaseAdapter {
	private ArrayList<String> mHeaderFullContent = new ArrayList<String>();
	public ArrayList<Boolean> mBookmarkedStatus = new ArrayList<Boolean>();
	public ArrayList<Integer> selectedIds = new ArrayList<Integer>();
	public ArrayList<Integer> attSelectedIds = new ArrayList<Integer>();
	List<ChildMailbox> attachmentItemList = null;
	private LayoutInflater inflater = null;
	private Context context;
	private View vi;
	private ViewHolder viewHolder;
	public int count;
	
	public static int clickId = -1;
	
	private SecAppFileListener listObj = null;
	
	private enum Extension {
		txt,rtf,doc,docx, xls,xlsx,ppt,pptx,pps,h,pdf,png,jpeg,jpg,gif,bmp,tiff,
		  vsd,java,htm,xml,csv,html,py,log,m,vcf	
	}

	Extension exten;
	
	private enum FileRef{
		a,b,c,d
	}
	FileRef fr;
	
	

	public AttachmentListAdapterMail(Context context,
			List<ChildMailbox> attachmentItemList) {

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		
		listObj = (SecAppFileListener) context;
		
		this.attachmentItemList = attachmentItemList;
		
		if(attachmentItemList != null)
		for (int i = 0; i < attachmentItemList.size(); i++) {
			mBookmarkedStatus.add(attachmentItemList.get(i)
					.isATTACHMENT_BOOKMARKED());
			
			//27-8-2013
			if (UtilList.deleteItemIDMail.containsKey(Integer
					.toString(i))) {
			selectedIds.add(1);
			}else{
				selectedIds.add(0);
			}
			//27-8-2013
			
			attSelectedIds.add(0);
			switch (UtilList.dataTypeMail) {
			case UtilList.RECENT_ADDED:
				mHeaderFullContent.add(attachmentItemList.get(i).getDATE()
						.toString());
				break;
			case UtilList.BOOKMARK:
				mHeaderFullContent.add(attachmentItemList.get(i).getDATE()
						.toString());
				break;
			case UtilList.SENTBY:
				mHeaderFullContent.add(attachmentItemList.get(i).getDISPLAY_NAME()
						.toString());
				break;

			}
		}
}

	

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		//Log.i("Adapter Count ",": "+mMailDocumentName.size());
		//return mMailDocumentName.size();
		if(attachmentItemList != null)
		return attachmentItemList.size();
		else
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		vi = convertView;
		final int pos = position;
		if (vi == null) {
			vi = inflater.inflate(R.layout.con_attachment_list_elements, null);

			viewHolder = new ViewHolder();
			viewHolder.mLaydummy = 	(LinearLayout)vi.findViewById(R.id.dummyheader);
			// Get all the list elements mail id
			viewHolder.mLytListElements = (LinearLayout) vi
					.findViewById(R.id.lyt_listelements_parent);
			viewHolder.mLytListSectionHeader = (LinearLayout) vi
					.findViewById(R.id.lyt_listsection_header);
			
			viewHolder.mLytListSectionFooter = (LinearLayout) vi
					.findViewById(R.id.container_footer);
			
			
			viewHolder.mTxtmailGroupHeadrerName = (TextView) vi
					.findViewById(R.id.txt_listgroup_header);
			
			viewHolder.mTxtmailGroupHeadrerCount = (TextView) vi
					.findViewById(R.id.container_no_items);
		
			viewHolder.mTxtmailDocumentName = (TextView) vi
					.findViewById(R.id.txt_document_name);
			viewHolder.mTxtmailAddress = (TextView) vi
					.findViewById(R.id.txt_email_address);
			viewHolder.mTxtTime = (TextView) vi
					.findViewById(R.id.txt_email_time);
			viewHolder.mImgBookmarked = (CheckBox) vi
					.findViewById(R.id.img_bookmarked);
			viewHolder.mImgDocumentType = (ImageView) vi
					.findViewById(R.id.img_doctype);
			
			viewHolder.mTxtsize = (TextView) vi
					.findViewById(R.id.txt_container_attsize);//NEW CHANGES 22
			viewHolder.mImgDelete = (CheckBox) vi
					.findViewById(R.id.img_delete);

			viewHolder.mTxtFooter = (TextView) vi
					.findViewById(R.id.container_list_footer);
			viewHolder.mFooterBorder = (View) vi
					.findViewById(R.id.container_footer_view);

			vi.setTag(viewHolder);
		}

		viewHolder = (ViewHolder) vi.getTag();
		if (attachmentItemList.get(position).getHEADER_CONTENT().toString()
				.equalsIgnoreCase(mHeaderFullContent.get(position).toString().trim())) {
			viewHolder.mLytListSectionHeader.setVisibility(View.VISIBLE);
			viewHolder.mTxtmailGroupHeadrerName.setTypeface(UtilList
					.getTextTypeFaceBold(context));
			viewHolder.mTxtmailGroupHeadrerName.setText(attachmentItemList
					.get(position).getHEADER_CONTENT().toString().toUpperCase());

			viewHolder.mTxtmailGroupHeadrerCount.setTypeface(UtilList
					.getTextTypeFaceBold(context));
			//Log.e("Count","Att List "+position);
			viewHolder.mTxtmailGroupHeadrerCount.setText("("+attachmentItemList
					.get(position).getHEADER_COUNT().toString().toUpperCase()+")");

		} else {

			viewHolder.mLytListSectionHeader.setVisibility(View.GONE);

		}

		// set the font style
		viewHolder.mTxtmailDocumentName.setTypeface(UtilList
				.getTextTypeFaceNormal(context));
	//	viewHolder.mTxtmailDocumentName.setTextColor(colors)
				
		viewHolder.mTxtmailAddress.setTypeface(UtilList
				.getTextTypeFaceNormal(context));
		viewHolder.mTxtTime
				.setTypeface(UtilList.getTextTypeFaceNormal(context));

		viewHolder.mTxtmailDocumentName.setText(UtilList.stringConvertion(attachmentItemList
				.get(position).getAttachmentName().toString()));
		viewHolder.mTxtmailAddress.setText(UtilList.stringConvertion(attachmentItemList.get(position)
				.getDISPLAY_NAME().toString()));

		//Log.i("12"," "+UtilList.dataTypeMail);
		switch (UtilList.dataTypeMail) {
		case UtilList.SENTBY:
			
			String setTextTime = UtilList.sentByTime(attachmentItemList.get(position).getDATE()
					.toString().trim(), attachmentItemList.get(position).getTIME()
					.toString().trim());
			//Log.i("12-9-2013"," "+UtilList.dataTypeMail);
			viewHolder.mTxtTime.setText(setTextTime);
			break;

		default:
			viewHolder.mTxtTime.setText(attachmentItemList.get(position).getTIME()
					.toString().trim());
			break;
		}
		
		
		//NEW CHANGES 22
		viewHolder.mTxtsize.setTypeface(UtilList
				.getTextTypeFaceNormal(context));
		viewHolder.mTxtsize.setText(UtilList.convertToStringRepresentation(Long.parseLong(attachmentItemList.get(position)
				.getSize().toString())));
		//NEW CHANGES 22

		//Set BG for selected attachments items in list for deletion
		if (selectedIds.get(position) == 1) {
			vi.setBackgroundColor(context.getResources().getColor(
					R.color.container_list_item_selected));
			viewHolder.mImgDelete.setChecked(true);
			//Log.i("onUiUpdateListner","======>  if ");
		} else {
			//Log.i("onUiUpdateListner","======>  else ");
			viewHolder.mImgDelete.setChecked(false);
			//vi.setBackgroundResource(0);
			vi.setBackgroundColor(context.getResources().getColor(
					R.color.container_White));
		}
		
		// set doctype image based on the document name extention
		final String documentName = attachmentItemList.get(position)
				.getAttachmentName().toString();
		
		String ext = "";

		int mid = documentName.lastIndexOf(".");
		ext = documentName.substring(mid + 1, documentName.length());

		//Log.i("Ext ", ": " + ext + " ");
		try{
		exten = Extension.valueOf(ext.toLowerCase());
		switch (exten) {
		case rtf:
		case doc:
		case docx:
			viewHolder.mImgDocumentType.setImageResource(R.drawable.con_word_icon);
			break;

		case ppt:
		case pptx:
		case pps:
			viewHolder.mImgDocumentType
					.setImageResource(R.drawable.con_powerpoint_icon);
			break;

		case pdf:
			viewHolder.mImgDocumentType.setImageResource(R.drawable.con_pdf_icon);
			break;

		case xlsx:
		case xls:
			viewHolder.mImgDocumentType.setImageResource(R.drawable.con_excel_icon);
			break;

		case log:
		case m:
		case py:
		case h:
		case java:
		case txt:
			viewHolder.mImgDocumentType
					.setImageResource(R.drawable.con_txt_icon);
			break;

		case jpg:
		case jpeg:
		case gif:
		case bmp:
		case tiff:
		case png:
			viewHolder.mImgDocumentType
					.setImageResource(R.drawable.con_picture_file_icon);
			break;

		case vcf:
		case vsd:
			viewHolder.mImgDocumentType
					.setImageResource(R.drawable.con_business_card_icon);
			break;

		case csv:
			viewHolder.mImgDocumentType
			.setImageResource(R.drawable.con_csv_file_icon);
			break;

		case html:
		case htm:
			viewHolder.mImgDocumentType
			.setImageResource(R.drawable.con_html_file_icon);
			break;
		
		default:
			viewHolder.mImgDocumentType
					.setImageResource(R.drawable.con_document_default_icon);
			break;
		}
		}catch(Exception e){
			
			viewHolder.mImgDocumentType
			.setImageResource(R.drawable.con_document_default_icon);
			
		}

		// Called on click of the bookmarked image
		viewHolder.mLaydummy.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//dnt do any thing
			}
		});
		viewHolder.mImgBookmarked
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						
						if(clickId == pos)
							listObj.onTitleBookmarkedUpdate(pos, isChecked);
						Log.i("SUDAR","======> "+isChecked);
						
						if (isChecked) {
							mBookmarkedStatus.set(pos, true);

							// Add the bookmarked attachment ID to the bookmarkedAttachmentItemID list
							if (!(UtilList.bookmarkedAttachmentItemIDMail.containsValue(attachmentItemList.get(pos).getAttachmentId().toString()
									.trim()))){

							UtilList.bookmarkedAttachmentItemIDMail.put(
									Integer.toString(pos), attachmentItemList
											.get(pos).getAttachmentId().toString().trim());
							
							UtilList.bookmarkedAttachmentItemIDValuesMail.put(
									attachmentItemList.get(pos).getAttachmentId().toString()
											.trim(), mBookmarkedStatus.get(pos));
							}
						} else {
							mBookmarkedStatus.set(pos, false);
							// Remove the bookmarked attachment ID from the bookmarkedAttachmentItemID list
							
							Log.i("SUDAR","   "+UtilList.bookmarkedAttachmentItemIDMail.remove(Integer
									.toString(pos)));
							UtilList.bookmarkedAttachmentItemIDValuesMail.put(
									attachmentItemList.get(pos).getAttachmentId()
											.trim(), mBookmarkedStatus.get(pos));
						}
					}
				});
		viewHolder.mImgBookmarked.setChecked(mBookmarkedStatus.get(position));
		
		viewHolder.mImgDelete
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				//Log.i("onUiUpdateListner","======> cilck  listner "+pos+"   "+!(viewHolder.mImgDelete.isChecked()));
				listObj.onUiUpdateListner(pos,attachmentItemList);
				
				
			}
		});
		
		//Log.i("Mail","  "+position +"   "+ (attachmentItemList.size()-1));
		if(position == (attachmentItemList.size()-1)){
		
			viewHolder.mLytListSectionFooter.setVisibility(View.VISIBLE);
			
			//viewHolder.mTxtFooter.setVisibility(View.VISIBLE);
			
			viewHolder.mTxtFooter.setText("File viewer retains attachments from last three days");
			viewHolder.mTxtFooter.setTypeface(UtilList
				.getTextTypeFaceNormal(context));
			viewHolder.mFooterBorder.setVisibility(View.VISIBLE);
			
		}else{
			viewHolder.mLytListSectionFooter.setVisibility(View.GONE);
			/*viewHolder.mTxtFooter.setVisibility(View.GONE);
			viewHolder.mFooterBorder.setVisibility(View.GONE);*/
		}
		
		if(clickId == pos)
		{
			viewHolder.mLytListElements.setBackgroundColor(context.getResources().getColor(
					R.color.container_list_item_selected));
			
		}else{
			viewHolder.mLytListElements.setBackgroundColor(0);
		}
		
		return vi;

	}

	public void toggleSelected(int position, int selected) {
		selectedIds.set(position, selected);
	}
	public void toggleSelectedAttachmnets(int position) {
		for(int i =0;i<attachmentItemList.size();i++)
		{
			attSelectedIds.set(i,0);
		}
		attSelectedIds.set(position,1);
	}

	public String getAttId(int pos){
		try{
		return attachmentItemList.get(pos).getAttachmentId();
		}catch(Exception e){
			return "0";
		}
	}
	
	public void removeAttId(){
		try{
		attachmentItemList.remove(clickId);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	static class ViewHolder {
		LinearLayout mLytListSectionHeader;
		LinearLayout mLytListElements;
		TextView mTxtmailGroupHeadrerName;
		TextView mTxtmailDocumentName;
		TextView mTxtmailAddress;
		TextView mTxtTime;
		CheckBox mImgBookmarked;
		ImageView mImgDocumentType;
		LinearLayout mLaydummy;
		
		TextView mTxtsize;//NEW CHANGES 22
		CheckBox mImgDelete;//NEW CHANGES 22
		
		LinearLayout mLytListSectionFooter;
		TextView mTxtFooter;
		View mFooterBorder;
		TextView mTxtmailGroupHeadrerCount;
	}

}