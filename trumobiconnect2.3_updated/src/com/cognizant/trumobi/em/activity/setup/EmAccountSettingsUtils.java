

package com.cognizant.trumobi.em.activity.setup;

import java.io.Serializable;
import java.net.URI;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.em.EmAccountBackupRestore;
import com.cognizant.trumobi.em.EmVendorPolicyLoader;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.AccountColumns;

public class EmAccountSettingsUtils {

    /**
     * Commits the UI-related settings of an account to the provider.  This is static so that it
     * can be used by the various account activities.  If the account has never been saved, this
     * method saves it; otherwise, it just saves the settings.
     * @param context the context of the caller
     * @param account the account whose settings will be committed
     */
    public static void commitSettings(Context context, EmEmailContent.Account account) {
        if (!account.isSaved()) {
            account.save(context);
        } else {
            ContentValues cv = new ContentValues();
            cv.put(AccountColumns.IS_DEFAULT, account.mIsDefault);
            cv.put(AccountColumns.DISPLAY_NAME, account.getDisplayName());
            cv.put(AccountColumns.SENDER_NAME, account.getSenderName());
            cv.put(AccountColumns.SIGNATURE, account.getSignature());
            cv.put(AccountColumns.SYNC_INTERVAL, account.mSyncInterval);
            cv.put(AccountColumns.RINGTONE_URI, account.mRingtoneUri);
            cv.put(AccountColumns.FLAGS, account.mFlags);
            cv.put(AccountColumns.SYNC_LOOKBACK, account.mSyncLookback);
            account.update(context, cv);
        }
        // Update the backup (side copy) of the accounts
        EmAccountBackupRestore.backupAccounts(context);
    }

    /**
     * Search the list of known Email providers looking for one that matches the user's email
     * domain.  We check for vendor supplied values first, then we look in providers_product.xml,
     * and finally by the entries in platform providers.xml.  This provides a nominal override
     * capability.
     *
     * A match is defined as any provider entry for which the "domain" attribute matches.
     *
     * @param domain The domain portion of the user's email address
     * @return suitable Provider definition, or null if no match found
     */
    public static Provider findProviderForDomain(Context context, String domain) {
        Provider p = EmVendorPolicyLoader.getInstance(context).findProviderForDomain(domain);
        if (p == null) {
            p = findProviderForDomain(context, domain, R.xml.em_providers_product);
        }
        if (p == null) {
            p = findProviderForDomain(context, domain, R.xml.em_providers);
        }
        return p;
    }

    /**
     * Search a single resource containing known Email provider definitions.
     *
     * @param domain The domain portion of the user's email address
     * @param resourceId Id of the provider resource to scan
     * @return suitable Provider definition, or null if no match found
     */
    private static Provider findProviderForDomain(Context context, String domain, int resourceId) {
        try {
            XmlResourceParser xml = context.getResources().getXml(resourceId);
            int xmlEventType;
            Provider provider = null;
            while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT) {
                if (xmlEventType == XmlResourceParser.START_TAG
                        && "provider".equals(xml.getName())
                        && domain.equalsIgnoreCase(getXmlAttribute(context, xml, "domain"))) {
                    provider = new Provider();
                    provider.id = getXmlAttribute(context, xml, "id");
                    provider.label = getXmlAttribute(context, xml, "label");
                    provider.domain = getXmlAttribute(context, xml, "domain");
                    provider.note = getXmlAttribute(context, xml, "note");
                }
                else if (xmlEventType == XmlResourceParser.START_TAG
                        && "incoming".equals(xml.getName())
                        && provider != null) {
                    provider.incomingUriTemplate = new URI(getXmlAttribute(context, xml, "uri"));
                    provider.incomingUsernameTemplate = getXmlAttribute(context, xml, "username");
                }
                else if (xmlEventType == XmlResourceParser.START_TAG
                        && "outgoing".equals(xml.getName())
                        && provider != null) {
                    provider.outgoingUriTemplate = new URI(getXmlAttribute(context, xml, "uri"));
                    provider.outgoingUsernameTemplate = getXmlAttribute(context, xml, "username");
                }
                else if (xmlEventType == XmlResourceParser.END_TAG
                        && "provider".equals(xml.getName())
                        && provider != null) {
                    return provider;
                }
            }
        }
        catch (Exception e) {
            Log.e(Email.LOG_TAG, "Error while trying to load provider settings.", e);
        }
        return null;
    }

    /**
     * Attempts to get the given attribute as a String resource first, and if it fails
     * returns the attribute as a simple String value.
     * @param xml
     * @param name
     * @return the requested resource
     */
    private static String getXmlAttribute(Context context, XmlResourceParser xml, String name) {
        int resId = xml.getAttributeResourceValue(null, name, 0);
        if (resId == 0) {
            return xml.getAttributeValue(null, name);
        }
        else {
            return context.getString(resId);
        }
    }

    public static class Provider implements Serializable {
        private static final long serialVersionUID = 8511656164616538989L;

        public String id;
        public String label;
        public String domain;
        public URI incomingUriTemplate;
        public String incomingUsernameTemplate;
        public URI outgoingUriTemplate;
        public String outgoingUsernameTemplate;
        public String note;
    }
}
