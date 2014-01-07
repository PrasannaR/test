

package com.cognizant.trumobi.exchange.provider;

import com.cognizant.trumobi.exchange.EmEasSyncService;
import com.cognizant.trumobi.exchange.provider.EmGalResult.GalData;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;

/**
 * ExchangeProvider provides real-time data from the Exchange server; at the moment, it is used
 * solely to provide GAL (Global Address Lookup) service to email address adapters
 */
public class EmExchangeProvider extends ContentProvider {
    public static final String EXCHANGE_AUTHORITY = "com.cognizant.trumobi.exchange.provider";
    public static final Uri GAL_URI = Uri.parse("content://" + EXCHANGE_AUTHORITY + "/gal/");

    private static final int GAL_BASE = 0;
    private static final int GAL_FILTER = GAL_BASE;

    public static final String[] GAL_PROJECTION = new String[] {"_id", "displayName", "data"};
    public static final int GAL_COLUMN_ID = 0;
    public static final int GAL_COLUMN_DISPLAYNAME = 1;
    public static final int GAL_COLUMN_DATA = 2;

    public static final String EXTRAS_TOTAL_RESULTS = "com.cognizant.trumobi.exchange.provider.TOTAL_RESULTS";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // Exchange URI matching table
        UriMatcher matcher = sURIMatcher;
        // The URI for GAL lookup contains three user-supplied parameters in the path:
        // 1) the account id of the Exchange account
        // 2) the constraint (filter) text
        matcher.addURI(EXCHANGE_AUTHORITY, "gal/*/*", GAL_FILTER);
    }

    private static void addGalDataRow(MatrixCursor mc, long id, String name, String address) {
        mc.newRow().add(id).add(name).add(address);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        int match = sURIMatcher.match(uri);
        switch (match) {
            case GAL_FILTER:
                long accountId = -1;
                // Pull out our parameters
                MatrixCursorExtras c = new MatrixCursorExtras(GAL_PROJECTION);
                String accountIdString = uri.getPathSegments().get(1);
                String filter = uri.getPathSegments().get(2);
                // Make sure we get a valid id; otherwise throw an exception
                try {
                    accountId = Long.parseLong(accountIdString);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Illegal value in URI");
                }
                // Get results from the Exchange account
                EmGalResult galResult = EmEasSyncService.searchGal(getContext(), accountId, filter);
                if (galResult != null) {
                    for (GalData data : galResult.galData) {
                        addGalDataRow(c, data._id, data.displayName, data.emailAddress);
                    }
                    // Use cursor side channel to report metadata
                    final Bundle bundle = new Bundle();
                    bundle.putInt(EXTRAS_TOTAL_RESULTS, galResult.total);
                    c.setExtras(bundle);
                }
                return c;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return -1;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/gal-entry";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return -1;
    }

    /**
     * A simple extension to MatrixCursor that supports extras
     */
    private static class MatrixCursorExtras extends MatrixCursor {

        private Bundle mExtras;

        public MatrixCursorExtras(String[] columnNames) {
            super(columnNames);
            mExtras = null;
        }

        public void setExtras(Bundle extras) {
            mExtras = extras;
        }

        @Override
        public Bundle getExtras() {
            return mExtras;
        }
    }
}
