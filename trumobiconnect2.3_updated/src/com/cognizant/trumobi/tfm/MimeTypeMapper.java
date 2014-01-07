package com.cognizant.trumobi.tfm;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;

import android.webkit.MimeTypeMap;

public class MimeTypeMapper {
	private static HashMap<String, String> mimeTypeMap = new HashMap<String, String>();

	static{
		mimeTypeMap.put("txt", "text/plain");
		mimeTypeMap.put("xml", "text/xml");
		mimeTypeMap.put("rtf", "application/rtf");
		mimeTypeMap.put("doc", "application/vnd.ms-word");
		mimeTypeMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		mimeTypeMap.put("xls", "application/vnd.ms-excel");
		mimeTypeMap.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		mimeTypeMap.put("csv", "text/comma-separated-values");
		mimeTypeMap.put("ppt", "application/vnd.ms-powerpoint");
		mimeTypeMap.put("pps", "application/vnd.ms-powerpoint");
		mimeTypeMap.put("pot", "application/vnd.ms-powerpoint");
		mimeTypeMap.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
		mimeTypeMap.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
		mimeTypeMap.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
		mimeTypeMap.put("hwp", "application/hwp");

		//others
		mimeTypeMap.put("htm", "text/html");
		mimeTypeMap.put("html", "text/html");

		//media
		mimeTypeMap.put("aac", "audio/aac");
		mimeTypeMap.put("amr", "audio/amr");
		mimeTypeMap.put("awb", "audio/amr-wb");
		mimeTypeMap.put("qcp", "audio/qcp");
		mimeTypeMap.put("xmf", "audio/midi");
		mimeTypeMap.put("rtttl", "audio/midi");
		mimeTypeMap.put("imy", "audio/imelody");
		mimeTypeMap.put("ota", "audio/midi");
		mimeTypeMap.put("m4a", "audio/mp4"); // audio/mpeg
		mimeTypeMap.put("m4v", "video/mp4");
		mimeTypeMap.put("3gpp", "video/3gpp");
		mimeTypeMap.put("3g2", "video/3gpp2"); //-> video/3gpp
		mimeTypeMap.put("3gpp2", "video/3gpp2");
		//       mimeTypeMap.put("avi", "video/avi");   //-> video/x-msvideo
		mimeTypeMap.put("divx", "video/divx");
		mimeTypeMap.put("m3u", "audio/x-mpegurl"); //-> audio/mpegurl
		mimeTypeMap.put("oga", "application/ogg");
		mimeTypeMap.put("wpl", "application/vnd.ms-wpl");
		mimeTypeMap.put("flac", "audio/flac");

		mimeTypeMap.put("smf", "audio/sp-midi");  //-> application/vnd.stardivision.math
		mimeTypeMap.put("rtx", "audio/midi"); //-> text/richtext

		mimeTypeMap.put("swf", "application/x-shockwave-flash");

		mimeTypeMap.put("eng", "application/eng");

		/**
		 * video call capture file
		 * for samsung
		 */
		mimeTypeMap.put("vcy", "videocallimages/jpeg-scramble");
		mimeTypeMap.put("vci", "videocallimages/jpeg");

		/**
		 * bell ringing
		 * for lge
		 */
		mimeTypeMap.put("imy", "audio/imelody");
	}

	public static String getMimeTypeFromExtension(String ext){
		if(ext == null) return "";
		ext = ext.toLowerCase(Locale.US);
		String value = mimeTypeMap.get(ext);
		if(value == null || value.equalsIgnoreCase("")){
			return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
		}
		return value;
	}

	public static String getMimeTypeFromUrl(String url){
		String ext = MimeTypeMap.getFileExtensionFromUrl(url);
		return getMimeTypeFromExtension(ext);
	}

	// XXX inefficient
	public static String[] findExtensionsFromMimeType(String mimeType) {
		if(mimeType == null) return null;

		HashMap<String, String> map = mimeTypeMap;
		String[] extensions = new String[map.size()+1];
		String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
		int count = 0;
		if(ext != null) extensions[count++] = ext;

		Set<Entry<String, String>> entrySet = map.entrySet();
		for(Entry<String, String> entry : entrySet) {
			if(mimeType.equals(entry.getValue())) {
				String key = entry.getKey();
				if(!key.equals(ext)) extensions[count++] = key;
			}
		}

		if(count <= 0) return null;
		if(count < extensions.length) {
			String[] shrinked = new String[count];
			System.arraycopy(extensions, 0, shrinked, 0, count);
			extensions = shrinked;
		}

		return extensions;
	}

	private String mFilePath = null;

	

	public void setFilePath(String filePath) {
		this.mFilePath = filePath;
	}

	public String getFilePath() {
		return this.mFilePath;
	}

}
