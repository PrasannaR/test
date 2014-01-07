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

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;

class PersonaLiveFolderInfo extends PersonaFolderInfo {

    /**
     * The base intent, if it exists.
     */
    Intent baseIntent;

    /**
     * The live folder's content uri.
     */
    Uri uri;

    /**
     * The live folder's display type.
     */
    int displayMode;

    /**
     * The live folder icon.
     */
    Drawable icon;

    /**
     * When set to true, indicates that the icon has been resized.
     */
    boolean filtered;

    /**
     * Reference to the live folder icon as an application's resource.
     */
    Intent.ShortcutIconResource iconResource;

    PersonaLiveFolderInfo() {
        itemType = PersonaLauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER;
    }

    @Override
    void onAddToDatabase(ContentValues values) {
        super.onAddToDatabase(values);
        values.put(PersonaLauncherSettings.Favorites.TITLE, title.toString());
        values.put(PersonaLauncherSettings.Favorites.URI, uri.toString());
        if (baseIntent != null) {
            values.put(PersonaLauncherSettings.Favorites.INTENT, baseIntent.toUri(0));
        }
        values.put(PersonaLauncherSettings.Favorites.ICON_TYPE, PersonaLauncherSettings.Favorites.ICON_TYPE_RESOURCE);
        values.put(PersonaLauncherSettings.Favorites.DISPLAY_MODE, displayMode);
        if (iconResource != null) {
            values.put(PersonaLauncherSettings.Favorites.ICON_PACKAGE, iconResource.packageName);
            values.put(PersonaLauncherSettings.Favorites.ICON_RESOURCE, iconResource.resourceName);
        }
    }
}
