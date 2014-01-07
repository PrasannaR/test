

package com.cognizant.trumobi.exchange.adapter;

import com.cognizant.trumobi.exchange.EmEasSyncService;
import com.cognizant.trumobi.exchange.provider.EmGalResult;
import com.cognizant.trumobi.exchange.provider.EmGalResult.GalData;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parse the result of a GAL command.
 */
public class EmGalParser extends EmParser {
    private EmEasSyncService mService;
    EmGalResult mGalResult = new EmGalResult();

    public EmGalParser(InputStream in, EmEasSyncService service) throws IOException {
        super(in);
        mService = service;
    }

    public EmGalResult getGalResult() {
        return mGalResult;
    }

    @Override
    public boolean parse() throws IOException {
        if (nextTag(START_DOCUMENT) != EmTags.SEARCH_SEARCH) {
            throw new IOException();
        }
        while (nextTag(START_DOCUMENT) != END_DOCUMENT) {
            if (tag == EmTags.SEARCH_RESPONSE) {
                parseResponse(mGalResult);
            } else {
                skipTag();
            }
         }
         return mGalResult.total > 0;
     }

     public void parseProperties(EmGalResult galResult) throws IOException {
    	 GalData galData = new GalData();
         String displayName = null;
         String email = null;
         while (nextTag(EmTags.SEARCH_STORE) != END) {
			if (tag == EmTags.GAL_DISPLAY_NAME) {
				displayName = getValue();
				galData.put(GalData.DISPLAY_NAME, displayName);
				galData.displayName = displayName;
			} else if (tag == EmTags.GAL_EMAIL_ADDRESS) {
				String emailAddress = getValue();
				galData.put(GalData.EMAIL_ADDRESS, emailAddress);
				galData.emailAddress = emailAddress;
			} else if (tag == EmTags.GAL_PHONE) {
				galData.put(GalData.WORK_PHONE, getValue());
			} else if (tag == EmTags.GAL_OFFICE) {
				galData.put(GalData.OFFICE, getValue());
			} else if (tag == EmTags.GAL_TITLE) {
				galData.put(GalData.TITLE, getValue());
			} else if (tag == EmTags.GAL_COMPANY) {
				galData.put(GalData.COMPANY, getValue());
			} else if (tag == EmTags.GAL_ALIAS) {
				galData.put(GalData.ALIAS, getValue());
			} else if (tag == EmTags.GAL_FIRST_NAME) {
				galData.put(GalData.FIRST_NAME, getValue());
			} else if (tag == EmTags.GAL_LAST_NAME) {
				galData.put(GalData.LAST_NAME, getValue());
			} else if (tag == EmTags.GAL_HOME_PHONE) {
				galData.put(GalData.HOME_PHONE, getValue());
			} else if (tag == EmTags.GAL_MOBILE_PHONE) {
				galData.put(GalData.MOBILE_PHONE, getValue());
			} else {
				skipTag();
			}
		}
         //if (displayName != null && email != null) {
             galResult.addGalData(galData);
         //}
     }

     public void parseResult(EmGalResult galResult) throws IOException {
         while (nextTag(EmTags.SEARCH_STORE) != END) {
             if (tag == EmTags.SEARCH_PROPERTIES) {
                 parseProperties(galResult);
             } else {
                 skipTag();
             }
         }
     }

     public void parseResponse(EmGalResult galResult) throws IOException {
         while (nextTag(EmTags.SEARCH_RESPONSE) != END) {
             if (tag == EmTags.SEARCH_STORE) {
                 parseStore(galResult);
             } else {
                 skipTag();
             }
         }
     }

     public void parseStore(EmGalResult galResult) throws IOException {
         while (nextTag(EmTags.SEARCH_STORE) != END) {
             if (tag == EmTags.SEARCH_RESULT) {
                 parseResult(galResult);
             } else if (tag == EmTags.SEARCH_RANGE) {
                 // Retrieve value, even if we're not using it for debug logging
                 String range = getValue();
                 if (EmEasSyncService.DEBUG_GAL_SERVICE) {
                     mService.userLog("GAL result range: " + range);
                 }
             } else if (tag == EmTags.SEARCH_TOTAL) {
                 galResult.total = getValueInt();
             } else {
                 skipTag();
             }
         }
     }
}

