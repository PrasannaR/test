package com.cognizant.trumobi.persona.net;

import java.util.LinkedList;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class PersonaAllAppsListDetails implements Parcelable{
	
	private boolean checked = false;
	private boolean appInstalled;
	@SerializedName("appstore_item_category")
	public Map<String,String> appstore_item_category;
	@SerializedName("appstore_item_data_type") //   NativeApp == 0,WebApp == 1
	public int appstore_item_data_type;
	@SerializedName("appstore_item_description")
	public String appstore_item_description;
	@SerializedName("appstore_item_download_count")
	public int appstore_item_download_count;
	@SerializedName("appstore_item_identifier") // Uniquely Identify App/Resource : ItemId and ItemDetailId
	public Map<String,Integer> appstore_item_identifier;
	@SerializedName("appstore_item_name")
	public String appstore_item_name;
	@SerializedName("appstore_item_rating")
	public float appstore_item_rating;
	@SerializedName("appstore_item_status") //  CanInstallOrDownload == 0,CanRequest == 1
	public int appstore_item_status;
	@SerializedName("appstore_item_version")
	public String appstore_item_version;
	@SerializedName("icon_image_url")
	public String icon_image_url;
	@SerializedName("is_appstore_item_featured")
	public boolean is_appstore_item_featured;
	
	@SerializedName("is_like")
	public boolean is_like;
	
	@SerializedName("last_downloaded_date")
	public String last_downloaded_date;
	@SerializedName("last_modified_date")
	public String last_modified_date;
	@SerializedName("no_of_likes")
	public int no_of_likes;
	
	@SerializedName("release_date")
	public String release_date;
	
//	@SerializedName("reviews")
//	public LinkedList<String> reviews;
	//@SerializedName("reviews")
	//public LinkedList<ReviewResourceListDetails> reviews;
	@SerializedName("app_manifest_source_path") // Used for Iphone/Ipad
	public String app_manifest_source_path;
	@SerializedName("binary_source_path") // Apk Path
	public String binary_source_path;
	
	@SerializedName("is_already_installed")
	public boolean is_already_installed;
	
	@SerializedName("release_notes")
	public String release_notes;
	@SerializedName("screen_shots")
	public LinkedList<String> screen_shots;
	@SerializedName("no_of_comments")
	public int no_of_comments;
	
	@SerializedName("bundle_identifier") 
	public String bundle_identifier;
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	 public boolean isChecked()
     {
         return checked;
     }

     public void setChecked(boolean checked)
     {
         this.checked = checked;
     }
     
     
     public void setInstalled(boolean appInstalled){
    	 this.appInstalled=appInstalled;
     }
     
     public boolean getAppInstalled(){
    	 return  this.appInstalled;
     }
	
	/*public static final Parcelable.Creator<PersonaAllAppsListDetails> CREATOR = new Parcelable.Creator<PersonaAllAppsListDetails>() {
        public PersonaAllAppsListDetails createFromParcel(Parcel in) {
            return new PersonaAllAppsListDetails();
        }

        public PersonaAllAppsListDetails[] newArray(int size) {
            return new PersonaAllAppsListDetails[size];
        }
	
	
	};*/
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
