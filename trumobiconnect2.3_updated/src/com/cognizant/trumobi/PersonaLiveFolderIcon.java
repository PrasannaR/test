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
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.cognizant.trumobi.R;
public class PersonaLiveFolderIcon extends PersonaFolderIcon {
    public PersonaLiveFolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PersonaLiveFolderIcon(Context context) {
        super(context);
    }

    static PersonaLiveFolderIcon fromXml(int resId, PersonaLauncher personaLauncher, ViewGroup group,
            PersonaLiveFolderInfo folderInfo) {

        PersonaLiveFolderIcon icon = (PersonaLiveFolderIcon)
                LayoutInflater.from(personaLauncher).inflate(resId, group, false);

        final Resources resources = personaLauncher.getResources();
        Drawable d = folderInfo.icon;
        if (d == null) {
            d = PersonaUtilities.createIconThumbnail(
                    resources.getDrawable(R.drawable.pr_ic_launcher_folder), personaLauncher);
            folderInfo.filtered = true;
        }
        icon.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
        if(!PersonaAlmostNexusSettingsHelper.getUIHideLabels(personaLauncher))icon.setText(folderInfo.title);
        icon.setTag(folderInfo);
        icon.setOnClickListener(personaLauncher);
        
        return icon;
    }

    @Override
    public boolean acceptDrop(PersonaDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
        return false;
    }

    @Override
    public void onDrop(PersonaDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
    }

    @Override
    public void onDragEnter(PersonaDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
    }

    @Override
    public void onDragOver(PersonaDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
    }

    @Override
    public void onDragExit(PersonaDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
    }
}
