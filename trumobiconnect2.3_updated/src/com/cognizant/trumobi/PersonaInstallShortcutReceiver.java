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


import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import com.cognizant.trumobi.R;

public class PersonaInstallShortcutReceiver extends BroadcastReceiver {
    private static final String ACTION_INSTALL_SHORTCUT =
            "com.android.launcher.action.INSTALL_SHORTCUT";

    private final int[] mCoordinates = new int[2];

    public void onReceive(Context context, Intent data) {
        if (!ACTION_INSTALL_SHORTCUT.equals(data.getAction())) {
            return;
        }

        int screen = PersonaLauncher.getScreen();

        if (!installShortcut(context, data, screen)) {
            // The target screen is full, let's try the other screens
        	final int count=PersonaLauncher.getScreenCount(context);
            for (int i = 0; i < count; i++) {
                if (i != screen && installShortcut(context, data, i)) break;
            }
        }
    }

    private boolean installShortcut(Context context, Intent data, int screen) {
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (findEmptyCell(context, mCoordinates, screen)) {
            PersonaCellLayout.CellInfo cell = new PersonaCellLayout.CellInfo();
            cell.cellX = mCoordinates[0];
            cell.cellY = mCoordinates[1];
            cell.screen = screen;

            Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);

            if (intent.getAction() == null) {
                intent.setAction(Intent.ACTION_VIEW);
            }

            // By default, we allow for duplicate entries (located in
            // different places)
            boolean duplicate = data.getBooleanExtra(PersonaLauncher.EXTRA_SHORTCUT_DUPLICATE, true);
            if (duplicate || !PersonaLauncherModel.shortcutExists(context, name, intent)) {
                PersonaLauncher.addShortcut(context, data, cell, true);
                Toast.makeText(context, context.getString(R.string.shortcut_installed, name),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getString(R.string.shortcut_duplicate, name),
                        Toast.LENGTH_SHORT).show();
            }

            return true;
        } else {
            Toast.makeText(context, context.getString(R.string.out_of_space),
                    Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private static boolean findEmptyCell(Context context, int[] xy, int screen) {
        final int xCount = PersonaLauncher.NUMBER_CELLS_X;
        final int yCount = PersonaLauncher.NUMBER_CELLS_Y;

        boolean[][] occupied = new boolean[xCount][yCount];

        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(PersonaLauncherSettings.Favorites.CONTENT_URI,
            new String[] { PersonaLauncherSettings.Favorites.CELLX, PersonaLauncherSettings.Favorites.CELLY,
                    PersonaLauncherSettings.Favorites.SPANX, PersonaLauncherSettings.Favorites.SPANY },
            PersonaLauncherSettings.Favorites.SCREEN + "=?",
            new String[] { String.valueOf(screen) }, null);

        final int cellXIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.CELLX);
        final int cellYIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.CELLY);
        final int spanXIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.SPANX);
        final int spanYIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.SPANY);

        try {
            while (c.moveToNext()) {
                int cellX = c.getInt(cellXIndex);
                int cellY = c.getInt(cellYIndex);
                int spanX = c.getInt(spanXIndex);
                int spanY = c.getInt(spanYIndex);

                for (int x = cellX; x < cellX + spanX && x < xCount; x++) {
                    for (int y = cellY; y < cellY + spanY && y < yCount; y++) {
                        occupied[x][y] = true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        } finally {
            c.close();
        }

        return PersonaCellLayout.findVacantCell(xy, 1, 1, xCount, yCount, occupied);
    }
}
