package com.cognizant.trumobi.container.Utils;

public class SettingItem {

	private String reporterName;
	private String date;


	public String getReporterName() {
		return reporterName;
	}

	public void setReporterName(String reporterName) {
		this.reporterName = reporterName;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "Title=" + 
				reporterName + " , Subtitle=" + date + "]";
	}
}
