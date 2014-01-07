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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.cognizant.trumobi.log.PersonaLog;

import android.content.ContentValues;
import android.graphics.Bitmap;


/**
 * Represents an item in the launcher.
 */
class PersonaItemInfo {

    static final int NO_ID = -1;

    /**
     * The id in the settings database for this item
     */
    long id = NO_ID;

    /**
     * One of {@link PersonaLauncherSettings.Favorites#ITEM_TYPE_APPLICATION},
     * {@link PersonaLauncherSettings.Favorites#ITEM_TYPE_SHORTCUT},
     * {@link PersonaLauncherSettings.Favorites#ITEM_TYPE_USER_FOLDER}, or
     * {@link PersonaLauncherSettings.Favorites#ITEM_TYPE_APPWIDGET}.
     */
    int itemType;

    /**
     * The id of the container that holds this item. For the desktop, this will be
     * {@link PersonaLauncherSettings.Favorites#CONTAINER_DESKTOP}. For the all applications folder it
     * will be {@link #NO_ID} (since it is not stored in the settings DB). For user folders
     * it will be the id of the folder.
     */
    long container = NO_ID;

    /**
     * Iindicates the screen in which the shortcut appears.
     */
    int screen = -1;

    /**
     * Indicates the X position of the associated cell.
     */
    int cellX = -1;

    /**
     * Indicates the Y position of the associated cell.
     */
    int cellY = -1;

    /**
     * Indicates the X cell span.
     */
    int spanX = 1;

    /**
     * Indicates the Y cell span.
     */
    int spanY = 1;

    /**
     * Indicates whether the item is a gesture.
     */
    boolean isGesture = false;

    PersonaItemInfo() {
    }

    PersonaItemInfo(PersonaItemInfo info) {
    	assignFrom(info);
    }

    void assignFrom(PersonaItemInfo info) {
        id = info.id;
        cellX = info.cellX;
        cellY = info.cellY;
        spanX = info.spanX;
        spanY = info.spanY;
        screen = info.screen;
        itemType = info.itemType;
        container = info.container;
    }

    /**
     * Write the fields of this item to the DB
     *
     * @param values
     */
    void onAddToDatabase(ContentValues values) {
        values.put(PersonaLauncherSettings.BaseLauncherColumns.ITEM_TYPE, itemType);
        if (!isGesture) {
            values.put(PersonaLauncherSettings.Favorites.CONTAINER, container);
            values.put(PersonaLauncherSettings.Favorites.SCREEN, screen);
            values.put(PersonaLauncherSettings.Favorites.CELLX, cellX);
            values.put(PersonaLauncherSettings.Favorites.CELLY, cellY);
            values.put(PersonaLauncherSettings.Favorites.SPANX, spanX);
            values.put(PersonaLauncherSettings.Favorites.SPANY, spanY);
        }
    }

    static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            // Try go guesstimate how much space the icon will take when serialized
            // to avoid unnecessary allocations/copies during the write.
            int size = bitmap.getWidth() * bitmap.getHeight() * 4;
            ByteArrayOutputStream out = new ByteArrayOutputStream(size);
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();

                values.put(PersonaLauncherSettings.Favorites.ICON, out.toByteArray());
            } catch (IOException e) {
                PersonaLog.w("Favorite", "Could not write icon");
            }
        }
    }

}
