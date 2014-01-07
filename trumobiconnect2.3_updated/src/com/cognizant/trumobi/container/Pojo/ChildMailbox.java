package com.cognizant.trumobi.container.Pojo;


public class ChildMailbox {
	
	//Message List
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
	
	private String ID="";
	private String DISPLAY_NAME="";
	private String SERVER_ID="";
	private String CLIENT_ID="";
	private String MAILBOX_KEY="";
	private String ATTACHMENT_TABLE_ID="";
	private String ACCOUNT_KEY="";
	
	private String HEADER_COUNT="";//UIChanges
	
	public String getHEADER_COUNT() {
		return HEADER_COUNT;
	}

	public void setHEADER_COUNT(String hEADER_COUNT) {
		HEADER_COUNT = hEADER_COUNT;
	}

	//SecureBrowser
		private String MIME_TYPE="";
		
		public String getMIME_TYPE() {
			return MIME_TYPE;
		}

		public void setMIME_TYPE(String mIME_TYPE) {
			MIME_TYPE = mIME_TYPE;
		}
	
	public String getATTACHMENT_TABLE_ID() {
		return ATTACHMENT_TABLE_ID;
	}

	public void setATTACHMENT_TABLE_ID(String aTTACHMENT_TABLE_ID) {
		ATTACHMENT_TABLE_ID = aTTACHMENT_TABLE_ID;
	}

	public String getACCOUNT_KEY() {
		return ACCOUNT_KEY;
	}

	public void setACCOUNT_KEY(String aCCOUNT_KEY) {
		ACCOUNT_KEY = aCCOUNT_KEY;
	}

	/**
	 * @return the iD
	 */
	public String getID() {
		return ID;
	}

	/**
	 * @param iD the iD to set
	 */
	public void setID(String iD) {
		ID = iD;
	}

	/**
	 * @return the dISPLAY_NAME
	 */
	public String getDISPLAY_NAME() {
		return DISPLAY_NAME;
	}

	/**
	 * @param dISPLAY_NAME the dISPLAY_NAME to set
	 */
	public void setDISPLAY_NAME(String dISPLAY_NAME) {
		DISPLAY_NAME = dISPLAY_NAME;
	}

	/**
	 * @return the sERVER_ID
	 */
	public String getSERVER_ID() {
		return SERVER_ID;
	}

	/**
	 * @param sERVER_ID the sERVER_ID to set
	 */
	public void setSERVER_ID(String sERVER_ID) {
		SERVER_ID = sERVER_ID;
	}

	/**
	 * @return the cLIENT_ID
	 */
	public String getCLIENT_ID() {
		return CLIENT_ID;
	}

	/**
	 * @param cLIENT_ID the cLIENT_ID to set
	 */
	public void setCLIENT_ID(String cLIENT_ID) {
		CLIENT_ID = cLIENT_ID;
	}

	/**
	 * @return the mAILBOX_KEY
	 */
	public String getMAILBOX_KEY() {
		return MAILBOX_KEY;
	}

	/**
	 * @param mAILBOX_KEY the mAILBOX_KEY to set
	 */
	public void setMAILBOX_KEY(String mAILBOX_KEY) {
		MAILBOX_KEY = mAILBOX_KEY;
	}

	/**
	 * @return the aTTACHMENT_UI_STATE
	 *//*
	public String getATTACHMENT_UI_STATE() {
		return ATTACHMENT_UI_STATE;
	}

	*//**
	 * @param aTTACHMENT_UI_STATE the aTTACHMENT_UI_STATE to set
	 *//*
	public void setATTACHMENT_UI_STATE(String aTTACHMENT_UI_STATE) {
		ATTACHMENT_UI_STATE = aTTACHMENT_UI_STATE;
	}

	*//**
	 * @return the aTTACHMENT_UI_DESTINATION
	 *//*
	public String getATTACHMENT_UI_DESTINATION() {
		return ATTACHMENT_UI_DESTINATION;
	}

	*//**
	 * @param aTTACHMENT_UI_DESTINATION the aTTACHMENT_UI_DESTINATION to set
	 *//*
	public void setATTACHMENT_UI_DESTINATION(String aTTACHMENT_UI_DESTINATION) {
		ATTACHMENT_UI_DESTINATION = aTTACHMENT_UI_DESTINATION;
	}

	*//**
	 * @return the aTTACHMENT_UI_DESTINATION_SIZE
	 *//*
	public String getATTACHMENT_UI_DESTINATION_SIZE() {
		return ATTACHMENT_UI_DESTINATION_SIZE;
	}

	*//**
	 * @param aTTACHMENT_UI_DESTINATION_SIZE the aTTACHMENT_UI_DESTINATION_SIZE to set
	 *//*
	public void setATTACHMENT_UI_DESTINATION_SIZE(
			String aTTACHMENT_UI_DESTINATION_SIZE) {
		ATTACHMENT_UI_DESTINATION_SIZE = aTTACHMENT_UI_DESTINATION_SIZE;
	}
*/
		//Extra Newly Added
		private Boolean ATTACHMENT_BOOKMARKED = false;
		private String CURRENT_LOACTION = "";
		
		public Boolean isATTACHMENT_BOOKMARKED() {
			return ATTACHMENT_BOOKMARKED;
		}

	public Boolean getATTACHMENT_BOOKMARKED() {
			return ATTACHMENT_BOOKMARKED;
		}
		public void setATTACHMENT_BOOKMARKED(Boolean aTTACHMENT_BOOKMARKED) {
			ATTACHMENT_BOOKMARKED = aTTACHMENT_BOOKMARKED;
		}
		public String getCURRENT_LOACTION() {
			return CURRENT_LOACTION;
		}
		public void setCURRENT_LOACTION(String cURRENT_LOACTION) {
			CURRENT_LOACTION = cURRENT_LOACTION;
		}

	private String HEADER_CONTENT= "";
	
	public String getHEADER_CONTENT() {
		return HEADER_CONTENT;
	}
	public void setHEADER_CONTENT(String hEADER_CONTENT) {
		HEADER_CONTENT = hEADER_CONTENT;
	}
	
	private String DATE = " ";
	private String TIME = " ";
	
	public String getDATE() {
		return DATE;
	}
	public void setDATE(String dATE) {
		DATE = dATE;
	}
	public String getTIME() {
		return TIME;
	}
	public void setTIME(String tIME) {
		TIME = tIME;
	}
	
	

	public String getBOOKMARK() {
		return BOOKMARK;
	}

	public void setBOOKMARK(String bOOKMARK) {
		BOOKMARK = bOOKMARK;
	}

	public String getAttachmentId(){
		return this.ATTACHMENT_ID;
		
	}
	
	public String getSize(){
		return this.SIZE;
		
	}
	
	public String getAttachmentName(){
		return this.ATTACHMENT_NAME;
		
	}
	
	public String getContent(){
		return this.CONTENT;
		
	}
	
	public String getREAD(){
		return this.READ;
		
	}
	
	public String getSubject(){
		return this.SUBJECT;
		
	}
	
	public String getDateTimeReceived(){
		return this.DATE_TIME_RECEIVED;
		
	}
	
	public String getEmailAddress(){
		return this.EMAIL_ADDRESS;
		
	}
	
	
	public String getImportance(){
		
		return this.IMPORTANCE;
		
	}
	
	public void setREAD(String value){
		
		this.READ = value;
	}
	
	
	public void setSubject(String value){
		
		SUBJECT = value;
	}
	
	public void setDateTimeReceived(String value){
		
		//Log.e("DateTime ", "datetimereceived "+value);
		
		this.DATE_TIME_RECEIVED = value;
	}
	
	public void setEmailAddress(String value){
		
		this.EMAIL_ADDRESS = value;
	}
	
	public void setImportance(String value){
		
		this.IMPORTANCE = value;
	}
	
	public void setAttachmentId(String value){
		 
		this.ATTACHMENT_ID = value;
		
	}
	
	public void setSize(String value){
		this.SIZE = value ;
		
	}
	
	public void setAttachmentName(String value){
		 this.ATTACHMENT_NAME = value;
		
	}
	
	public void setContent(String value){
		 this.CONTENT= value;
		
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
