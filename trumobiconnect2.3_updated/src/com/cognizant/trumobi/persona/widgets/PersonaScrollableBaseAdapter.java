package com.cognizant.trumobi.persona.widgets;

import android.content.Context;
import android.widget.BaseAdapter;

/*
 * Base Adapter class that brings a common interface for PersonaWidgetRemoteViewsListAdapter and
 * PersonaWidgetListAdapter.
 */
public abstract class PersonaScrollableBaseAdapter extends BaseAdapter {

	/*
	 * Tell the adapter to regenerate the data cache
	 */
	public abstract void notifyToRegenerate();

	/*
	 * Tell the adapter to drop the cache
	 */
	public abstract void dropCache(Context context);

}
