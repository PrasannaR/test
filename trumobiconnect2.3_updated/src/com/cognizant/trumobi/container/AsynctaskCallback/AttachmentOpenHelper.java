package com.cognizant.trumobi.container.AsynctaskCallback;

import java.io.File;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.TruBoxSDK.TruboxFileEncryption;
import com.TruBoxSDK.TruboxFileEncryption.STORAGEMODE;
import com.cognizant.trumobi.container.Utils.UtilList;

public class AttachmentOpenHelper extends AsyncTask<String, String, Boolean> {

	private SecAppFileListener callbackPrg;
	static Context cntx;
	String inFilePath = null;
	String outFilePath;
	String result = null;
	TruboxFileEncryption truboxFileEncryption;
	Integer orgFileSize;
	File _newFile;
	int openType;
	String extension = null;

	public AttachmentOpenHelper(Context ctx,File newFile,String _outFilePath,Integer _orgFileSize,int _openType,String ext) {

		cntx = ctx;
		callbackPrg = (SecAppFileListener) cntx;
		truboxFileEncryption = new TruboxFileEncryption(
				ctx, newFile.getAbsolutePath(), STORAGEMODE.EXTERNAL);
		_newFile = newFile;
		outFilePath = _outFilePath;
		orgFileSize =  _orgFileSize;
		openType = _openType;
		extension = ext;//290388

	}

	@Override
	protected Boolean doInBackground(String... params) {
		
		try {
			inFilePath = UtilList.createTempFile(outFilePath, _newFile.length(), orgFileSize,
					truboxFileEncryption);
			
			if(inFilePath != null)
				return true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}


	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		Log.i("NEW","resu "+result);
		
		if (result)
			callbackPrg.onRemoteCallback(result, inFilePath,openType,cntx,extension);
		else
			callbackPrg.onRemoteCallback(result, null,openType,cntx,extension);
		
		return;
	}
}
