

package com.cognizant.trumobi.em;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.em.mail.EmAddress;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;

public class EmEmailAddressAdapter extends ResourceCursorAdapter {
    public static final int ID_INDEX = 0;
    public static final int NAME_INDEX = 1;
    public static final int DATA_INDEX = 2;

    protected static final String SORT_ORDER =
            Contacts.TIMES_CONTACTED + " DESC, " + Contacts.DISPLAY_NAME;

    protected final ContentResolver mContentResolver;

    protected static final String[] PROJECTION = {
        Data._ID,               // 0
        Contacts.DISPLAY_NAME,  // 1
        Email.DATA              // 2
    };

    public EmEmailAddressAdapter(Context context) {
        super(context, R.layout.em_recipient_dropdown_item, null);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public final String convertToString(Cursor cursor) {
        String name = cursor.getString(NAME_INDEX);
        String address = cursor.getString(DATA_INDEX);

        return new EmAddress(address, name).toString();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView text1 = (TextView)view.findViewById(R.id.text1);
        TextView text2 = (TextView)view.findViewById(R.id.text2);
        text1.setText(cursor.getString(NAME_INDEX));
        text2.setText(cursor.getString(DATA_INDEX));
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        String filter = constraint == null ? "" : constraint.toString();
        Uri uri = Uri.withAppendedPath(Email.CONTENT_FILTER_URI, Uri.encode(filter));
        Cursor c = mContentResolver.query(uri, PROJECTION, null, null, SORT_ORDER);
        // To prevent expensive execution in the UI thread
        // Cursors get lazily executed, so if you don't call anything on the cursor before
        // returning it from the background thread you'll have a complied program for the cursor,
        // but it won't have been executed to generate the data yet. Often the execution is more
        // expensive than the compilation...
        if (c != null) {
            c.getCount();
        }
        return c;
    }

    /**
     * Set the account when known.  Not used for generic contacts lookup;  Use when
     * linking lookup to specific account.
     */
    public void setAccount(Account account) { }
}
