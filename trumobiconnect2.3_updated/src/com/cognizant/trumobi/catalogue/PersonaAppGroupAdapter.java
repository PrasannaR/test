/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cognizant.trumobi.catalogue;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.PersonaWorkspace;

/**
 * Adapter showing the types of items that can be added to a {@link PersonaWorkspace}.
 */
public class PersonaAppGroupAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;

    private final ArrayList<ListItem> mItems = new ArrayList<ListItem>();

    public static final int APP_GROUP_ALL = -1;
    public static final int APP_GROUP_CONFIG = -2;
    public static final int APP_GROUP_ADD = -3;
    /**
     * Specific item in our list.
     */
    public class ListItem {
        public final CharSequence text;
        public final int actionTag;

        public ListItem(Resources res, int textResourceId, int actionTag) {
            text = res.getString(textResourceId);
            this.actionTag = actionTag;
        }
        public ListItem(Resources res, String textResource, int actionTag) {
            text = textResource;
            this.actionTag = actionTag;
        }

    }

	private void addListItem(Resources res, PersonaAppCatalogueFilters.Catalogue catalogue)
	{
		String grpTitle = catalogue.getTitle();
		if (grpTitle != null) {
			mItems.add(new ListItem(res, grpTitle, catalogue.getIndex()));
		}
	}

    public PersonaAppGroupAdapter(PersonaLauncher personaLauncher) {
        super();

        mInflater = (LayoutInflater) personaLauncher.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Create default actions
        Resources res = personaLauncher.getResources();

        mItems.add(new ListItem(res, R.string.AppGroupAdd, APP_GROUP_ADD));
		mItems.add(new ListItem(res, R.string.AppGroupAll, APP_GROUP_ALL));

		for(PersonaAppCatalogueFilters.Catalogue itm : PersonaAppCatalogueFilters.getInstance().getAllGroups()) {
			addListItem(res, itm);
		}

    }

	public View getView(int position, View convertView, ViewGroup parent) {
		ListItem item = (ListItem) getItem(position);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.pr_add_list_item, parent,
					false);
		}

		TextView textView = (TextView) convertView;
		textView.setTag(item);
		textView.setText(item.text);

		return convertView;
	}

    public int getCount() {
        return mItems.size();
    }

    public Object getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

}
