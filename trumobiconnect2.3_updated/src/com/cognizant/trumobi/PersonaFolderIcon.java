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


import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cognizant.trumobi.R;

/**
 * An icon that can appear on in the workspace representing an {@link PersonaUserFolder}.
 */
public class PersonaFolderIcon extends PersonaBubbleTextView implements PersonaDropTarget {
    private PersonaUserFolderInfo mInfo;
    private PersonaLauncher mLauncher;
    private Drawable mCloseIcon;
    private Drawable mOpenIcon;

    public PersonaFolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PersonaFolderIcon(Context context) {
        super(context);
    }

    static PersonaFolderIcon fromXml(int resId, PersonaLauncher personaLauncher, ViewGroup group,
            PersonaUserFolderInfo folderInfo) {

        PersonaFolderIcon icon = (PersonaFolderIcon) LayoutInflater.from(personaLauncher).inflate(resId, group, false);
        //TODO:ADW Load icon from theme/iconpack
        Drawable dclosed;
        Drawable dopen;
        final Resources resources = personaLauncher.getResources();
        String themePackage=PersonaAlmostNexusSettingsHelper.getThemePackageName(personaLauncher, PersonaLauncher.THEME_DEFAULT);
        if(themePackage.equals(PersonaLauncher.THEME_DEFAULT)){
        	dclosed = resources.getDrawable(R.drawable.pr_ic_launcher_folder);
        	dopen = resources.getDrawable(R.drawable.pr_ic_launcher_folder_open);
        }else{
        	Drawable tmpIcon1 = loadFolderFromTheme(personaLauncher, personaLauncher.getPackageManager(), themePackage,"ic_launcher_folder");
        	if(tmpIcon1==null){
        		dclosed = resources.getDrawable(R.drawable.pr_ic_launcher_folder);
        	}else{
        		dclosed = tmpIcon1;
        	}
        	Drawable tmpIcon2 = loadFolderFromTheme(personaLauncher, personaLauncher.getPackageManager(), themePackage,"ic_launcher_folder_open");
        	if(tmpIcon2==null){
        		dopen = resources.getDrawable(R.drawable.pr_ic_launcher_folder_open);
        	}else{
        		dopen = tmpIcon2;
        	}
        }
        icon.mCloseIcon=PersonaUtilities.createIconThumbnail(dclosed, personaLauncher);
        icon.mOpenIcon=dopen;
        /*final Resources resources = launcher.getResources();
        Drawable d = resources.getDrawable(R.drawable.ic_launcher_folder);
        d = PersonaUtilities.createIconThumbnail(d, launcher);
        icon.mCloseIcon = d;
        icon.mOpenIcon = resources.getDrawable(R.drawable.ic_launcher_folder_open);*/
        icon.setCompoundDrawablesWithIntrinsicBounds(null, dclosed, null, null);
        if(!PersonaAlmostNexusSettingsHelper.getUIHideLabels(personaLauncher))icon.setText(folderInfo.title);
        icon.setTag(folderInfo);
        icon.setOnClickListener(personaLauncher);
        icon.mInfo = folderInfo;
        icon.mLauncher = personaLauncher;
        
        return icon;
    }

    public boolean acceptDrop(PersonaDragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
        final PersonaItemInfo item = (PersonaItemInfo) dragInfo;
        final int itemType = item.itemType;
        return (itemType == PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == PersonaLauncherSettings.Favorites.ITEM_TYPE_SHORTCUT)
                && item.container != mInfo.id;
    }

    public Rect estimateDropLocation(PersonaDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo, Rect recycle) {
        return null;
    }

    public void onDrop(PersonaDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
        final PersonaApplicationInfo item = (PersonaApplicationInfo) dragInfo;
        // TODO: update open folder that is looking at this data
        mInfo.add(item);
        PersonaLauncherModel.addOrMoveItemInDatabase(mLauncher, item, mInfo.id, 0, 0, 0);
    }

    public void onDragEnter(PersonaDragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
        setCompoundDrawablesWithIntrinsicBounds(null, mOpenIcon, null, null);
    }

    public void onDragOver(PersonaDragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
    }

    public void onDragExit(PersonaDragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
        setCompoundDrawablesWithIntrinsicBounds(null, mCloseIcon, null, null);
    }
    /**
     * ADW: Load the floder icon drawables from the theme
     * @param context
     * @param manager
     * @param themePackage
     * @param resourceName
     * @return
     */
    static Drawable loadFolderFromTheme(Context context,
			PackageManager manager, String themePackage, String resourceName) {
		Drawable icon=null;
    	Resources themeResources=null;
    	try {
			themeResources=manager.getResourcesForApplication(themePackage);
		} catch (NameNotFoundException e) {
			//e.printStackTrace();
		}
		if(themeResources!=null){
			int resource_id=themeResources.getIdentifier (resourceName, "drawable", themePackage);
			if(resource_id!=0){
				icon=themeResources.getDrawable(resource_id);
			}
		}
		return icon;
	}
    
}
