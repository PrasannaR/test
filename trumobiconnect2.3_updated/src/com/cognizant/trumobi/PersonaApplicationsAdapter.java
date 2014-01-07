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

package com.cognizant.trumobi;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.catalogue.PersonaAppCatalogueFilter;

/**
 * GridView adapter to show the list of applications and shortcuts
 */
public class PersonaApplicationsAdapter extends ArrayAdapter<PersonaApplicationInfo> {
	private final LayoutInflater mInflater;
	private Drawable mBackground;
	private int mTextColor = 0;
	private boolean useThemeTextColor = false;
    private Typeface themeFont=null;
	// TODO: Check if allItems is used somewhere else!
	public static ArrayList<PersonaApplicationInfo> allItems = new ArrayList<PersonaApplicationInfo>();
	private static HashMap<PersonaApplicationInfo, View> viewCache = new HashMap<PersonaApplicationInfo, View>();
	private CatalogueFilter filter;
    private static final Collator sCollator = Collator.getInstance();
    private PersonaAppCatalogueFilter mCatalogueFilter;
    private boolean mWithDrawingCache = false;

	public PersonaApplicationsAdapter(Context context, ArrayList<PersonaApplicationInfo> apps, PersonaAppCatalogueFilter filter) {
		super(context, 0, apps);

		mCatalogueFilter = filter;

		mInflater = LayoutInflater.from(context);
		// ADW: Load textcolor and bubble color from theme
		String themePackage = PersonaAlmostNexusSettingsHelper.getThemePackageName(
				getContext(), PersonaLauncher.THEME_DEFAULT);
		if (!themePackage.equals(PersonaLauncher.THEME_DEFAULT)) {
			Resources themeResources = null;
			try {
				themeResources = getContext().getPackageManager()
						.getResourcesForApplication(themePackage);
			} catch (NameNotFoundException e) {
				// e.printStackTrace();
			}
			if (themeResources != null) {
				int textColorId = themeResources.getIdentifier(
						"drawer_text_color", "color", themePackage);
				if (textColorId != 0) {
					mTextColor = themeResources.getColor(textColorId);
					useThemeTextColor = true;
				}
				mBackground = PersonaIconHighlights.getDrawable(getContext(),
						PersonaIconHighlights.TYPE_DRAWER);
    			try{
    				themeFont=Typeface.createFromAsset(themeResources.getAssets(), "themefont.ttf");
    			}catch (RuntimeException e) {
					// TODO: handle exception
				}
			}
		}
	}
	
	public void buildViewCache(ViewGroup parent) {
		for(int i = 0; i < getCount(); i++) {
			final PersonaApplicationInfo info = getItem(i);	
			addToViewCache(parent, info);
		}
	}
	
	private void addToViewCache(ViewGroup parent, PersonaApplicationInfo info) {
		if (!info.filtered) {
			info.icon = PersonaUtilities.createIconThumbnail(info.icon, getContext());
			info.filtered = true;
		}
		View convertView = mInflater.inflate(R.layout.pr_application_boxed, parent, false);
		convertView.setDrawingCacheEnabled(mWithDrawingCache);
		viewCache.put(info, convertView);
		final TextView textView = (TextView) convertView;
		textView.setCompoundDrawablesWithIntrinsicBounds(null, info.icon, null, null);
		textView.setText(info.title);
		if (useThemeTextColor) {
			textView.setTextColor(mTextColor);
		}
		//ADW: Custom font
		if(themeFont!=null) textView.setTypeface(themeFont);
		// so i'd better not use it, sorry themers
		if (mBackground != null)
			convertView.setBackgroundDrawable(mBackground);
	}
	
	public void setChildDrawingCacheEnabled(boolean aValue) {
		if (mWithDrawingCache != aValue) {
			mWithDrawingCache = aValue;
			for(View v : viewCache.values()) {
				v.setDrawingCacheEnabled(aValue);
				if(aValue)
				    v.buildDrawingCache();
				else
				    v.destroyDrawingCache();
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final PersonaApplicationInfo info = getItem(position);
		if (viewCache.isEmpty())
			buildViewCache(parent);
		if (!viewCache.containsKey(info))
			addToViewCache(parent, info);
		View result = viewCache.get(info);
        //ADW:Counters
        ((PersonaCounterTextView)result).setCounter(info.counter, info.counterColor);
		return result; 
	}


	@Override
	public void add(PersonaApplicationInfo info) {
		//check allItems before added. It is a fix for all of the multi-icon issue, but will 
		//lose performance. Anyway, we do not expected to have many applications.
		synchronized (allItems) {
			/*if (!allItems.contains(info)) {
				changed = true;
				allItems.add(info);
				Collections.sort(allItems,new ApplicationInfoComparator());
			}*/
			int count=allItems.size();
			boolean found=false;
			for(int i=0;i<count;i++){
				PersonaApplicationInfo athis=allItems.get(i);
				if(info.intent.getComponent()!=null){
					if(athis.intent.getComponent().flattenToString().equals(
							info.intent.getComponent().flattenToString())){
						found=true;
						break;
					}
				}
			}
			if(!found){
				allItems.add(info);
				Collections.sort(allItems,new ApplicationInfoComparator());
				updateDataSet();
			}
		} 
	}

	//2 super functions, to make sure related add/clear do not affect allItems.
	//in current Froyo/Eclair, it is not necessary.
	void superAdd(PersonaApplicationInfo info) {
		if(info!=null)
			super.add(info);
	}

	void superClear() {
		super.clear();
	}

	@Override
	public void remove(PersonaApplicationInfo info) {
		synchronized (allItems) {
			//allItems.remove(info);
			int count=allItems.size();
			for(int i=0;i<count;i++){
				PersonaApplicationInfo athis=allItems.get(i);
				if(info.intent.getComponent()!=null){
					if(athis.intent.getComponent().flattenToString().equals(
							info.intent.getComponent().flattenToString())){
						viewCache.remove(athis);
						allItems.remove(i);
						Collections.sort(allItems,new ApplicationInfoComparator());
						updateDataSet();
						break;
					}
				}
			}			
		}
	}

	private boolean appInGroup(String s) {
		return mCatalogueFilter.checkAppInGroup(s);
	}

	private String getComponentName(PersonaApplicationInfo info) {
		if (info == null || info.intent == null)
			return null;
		ComponentName cmpName = info.intent.getComponent();
		if (cmpName == null)
			return null;
		return cmpName.flattenToString();
	}

	private void filterApps(ArrayList<PersonaApplicationInfo> theFiltered,
			ArrayList<PersonaApplicationInfo> theItems) {
		theFiltered.clear();
		//AppGrpUtils.checkAndInitGrp();
		if (theItems != null) {
			int length = theItems.size();

			for (int i = 0; i < length; i++) {
				PersonaApplicationInfo info = theItems.get(i);
				String s = getComponentName(info);

				if (s != null && appInGroup(s)) {
					theFiltered.add(info);
				}
			}
		}
	}
	//filter,sort,update
    public void updateDataSet()
	{
        getFilter().filter(null);
	}

	@Override
	public Filter getFilter() {
		if (filter == null)
			filter = new CatalogueFilter();
		return filter;
	}

	public synchronized void setCatalogueFilter(PersonaAppCatalogueFilter filter) {
		if (filter != mCatalogueFilter) {
			mCatalogueFilter = filter;
			updateDataSet();
			notifyDataSetChanged();
		}
	}

	public PersonaAppCatalogueFilter getCatalogueFilter() {
		return mCatalogueFilter;
	}


	private class CatalogueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults result = new FilterResults();
                ArrayList<PersonaApplicationInfo> filt = new ArrayList<PersonaApplicationInfo>();

                synchronized (allItems) {
                        filterApps(filt, allItems);
                }

                result.values = filt;
                result.count = filt.size();
                return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                        FilterResults results) {
                // NOTE: this function is *always* called from the UI thread.
                ArrayList<PersonaApplicationInfo> localFiltered =(ArrayList<PersonaApplicationInfo>) results.values;

                setNotifyOnChange(false);
                superClear();
                // there could be a serious sync issue.
                // very bad
                for (int i = 0;i < results.count; i++) {
                    superAdd(localFiltered.get(i));
                }

                notifyDataSetChanged();

        }
	}
    static class ApplicationInfoComparator implements Comparator<PersonaApplicationInfo> {
        public final int compare(PersonaApplicationInfo a, PersonaApplicationInfo b) {
            return sCollator.compare(a.title.toString(), b.title.toString());
        }
    }
}
