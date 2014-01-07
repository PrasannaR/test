package com.cognizant.trumobi.dialer.dbController;

import java.io.Serializable;
import java.util.ArrayList;

public class DialerCallLogList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String ASSOICIATE_NAME = "";
	private String NUMBER_TYPE = "";
	private String DATE = "";
	private ArrayList<String> CALL_DURATION;
	private int CALL_TYPE_INT;
	private String NUMBER = "";
	private ArrayList<String> CALL_TYPE_STRING;
	private String CALL_NO_TIMES_STRING = "";
	private String FOREIGN_KEY_STRING = "";
	private int CALL_STATE_INT;
	private byte[] img_src = null;
	private boolean isNativeContact = false;
	
	private boolean isNativeProfilePic = false;

	public boolean isNativeProfilePic() {
		return isNativeProfilePic;
	}

	public void setNativeProfilePic(boolean isNativeProfilePic) {
		this.isNativeProfilePic = isNativeProfilePic;
	}
	public boolean isNativeContact() {
		return isNativeContact;
	}

	public void setNativeContact(boolean isNativeContact) {
		this.isNativeContact = isNativeContact;
	}

	/**
	 * @return the aSSOICIATE_NAME
	 */
	public String getASSOICIATE_NAME() {
		return ASSOICIATE_NAME;
	}

	/**
	 * @param aSSOICIATE_NAME
	 *            the aSSOICIATE_NAME to set
	 */
	public void setASSOICIATE_NAME(String aSSOICIATE_NAME) {
		ASSOICIATE_NAME = aSSOICIATE_NAME;
	}

	/**
	 * @return the nUMBER_TYPE
	 */
	public String getNUMBER_TYPE() {
		return NUMBER_TYPE;
	}

	/**
	 * @param nUMBER_TYPE
	 *            the nUMBER_TYPE to set
	 */
	public void setNUMBER_TYPE(String nUMBER_TYPE) {
		NUMBER_TYPE = nUMBER_TYPE;
	}

	/**
	 * @return the dATE
	 */
	public String getDATE() {
		return DATE;
	}

	/**
	 * @param dATE
	 *            the dATE to set
	 */
	public void setDATE(String dATE) {
		DATE = dATE;
	}

	/**
	 * @return the cALL_DURATION
	 */
	public ArrayList<String> getCALL_DURATION() {
		return CALL_DURATION;
	}

	/**
	 * @param cALL_DURATION
	 *            the cALL_DURATION to set
	 */
	public void setCALL_DURATION(ArrayList<String> cALL_DURATION) {
		CALL_DURATION = cALL_DURATION;
	}

	/**
	 * @return the cALL_TYPE_INT
	 */
	public int getCALL_TYPE_INT() {
		return CALL_TYPE_INT;
	}

	/**
	 * @param cALL_TYPE_INT
	 *            the cALL_TYPE_INT to set
	 */
	public void setCALL_TYPE_INT(int cALL_TYPE_INT) {
		CALL_TYPE_INT = cALL_TYPE_INT;
	}

	/**
	 * @return the nUMBER
	 */
	public String getNUMBER() {
		return NUMBER;
	}

	/**
	 * @param nUMBER
	 *            the nUMBER to set
	 */
	public void setNUMBER(String nUMBER) {
		NUMBER = nUMBER;
	}

	/**
	 * @return the cALL_TYPE_STRING
	 */
	public ArrayList<String> getCALL_TYPE_STRING() {
		return CALL_TYPE_STRING;
	}

	/**
	 * @param cALL_TYPE_STRING
	 *            the cALL_TYPE_STRING to set
	 */
	public void setCALL_TYPE_STRING(ArrayList<String> cALL_TYPE_STRING) {
		CALL_TYPE_STRING = cALL_TYPE_STRING;
	}

	/**
	 * @return the cALL_NO_TIMES_STRING
	 */
	public String getCALL_NO_TIMES_STRING() {
		return CALL_NO_TIMES_STRING;
	}

	/**
	 * @param cALL_NO_TIMES_STRING
	 *            the cALL_NO_TIMES_STRING to set
	 */
	public void setCALL_NO_TIMES_STRING(String cALL_NO_TIMES_STRING) {
		CALL_NO_TIMES_STRING = cALL_NO_TIMES_STRING;
	}

	/**
	 * @return the fOREIGN_KEY_STRING
	 */
	public String getFOREIGN_KEY_STRING() {
		return FOREIGN_KEY_STRING;
	}

	/**
	 * @param fOREIGN_KEY_STRING
	 *            the fOREIGN_KEY_STRING to set
	 */
	public void setFOREIGN_KEY_STRING(String fOREIGN_KEY_STRING) {
		FOREIGN_KEY_STRING = fOREIGN_KEY_STRING;
	}

	/**
	 * @return the cALL_STATE_INT
	 */
	public int getCALL_STATE_INT() {
		return CALL_STATE_INT;
	}

	/**
	 * @param cALL_STATE_INT
	 *            the cALL_STATE_INT to set
	 */
	public void setCALL_STATE_INT(int cALL_STATE_INT) {
		CALL_STATE_INT = cALL_STATE_INT;
	}

	/**
	 * @return the img_src
	 */
	public byte[] getImg_src() {
		return img_src;
	}

	/**
	 * @param img_src
	 *            the img_src to set
	 */
	public void setImg_src(byte[] img_src) {
		this.img_src = img_src;
	}

}
