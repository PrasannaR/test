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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.cognizant.trumobi.R;

public class PersonaHandleView extends ImageView {
    private static final int ORIENTATION_HORIZONTAL = 1;

    private PersonaLauncher mLauncher;
    private int mOrientation = ORIENTATION_HORIZONTAL;

    public PersonaHandleView(Context context) {
        super(context);
    }

    public PersonaHandleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PersonaHandleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HandleView, defStyle, 0);
        mOrientation = a.getInt(R.styleable.HandleView_direction, ORIENTATION_HORIZONTAL);
        a.recycle();
    }

    @Override
    public View focusSearch(int direction) {
        View newFocus = super.focusSearch(direction);
        if (newFocus == null && !mLauncher.isAllAppsVisible()) {
            final PersonaWorkspace personaWorkspace = mLauncher.getWorkspace();
            personaWorkspace.dispatchUnhandledMove(null, direction);
            return (mOrientation == ORIENTATION_HORIZONTAL && direction == FOCUS_DOWN) ?
                    this : personaWorkspace;
        }
        return newFocus;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final boolean handled = super.onKeyDown(keyCode, event);

        if (!handled && mLauncher.isAllAppsVisible() && !isDirectionKey(keyCode)) {
            return mLauncher.getApplicationsGrid().onKeyDown(keyCode, event);
        }

        return handled;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        final boolean handled = super.onKeyUp(keyCode, event);

        if (!handled && mLauncher.isAllAppsVisible() && !isDirectionKey(keyCode)) {
            return mLauncher.getApplicationsGrid().onKeyUp(keyCode, event);
        }

        return handled;
    }

    private static boolean isDirectionKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_UP;
    }

    void setLauncher(PersonaLauncher personaLauncher) {
        mLauncher = personaLauncher;
    }
}
