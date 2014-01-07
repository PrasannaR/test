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

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.LiveFolders;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import com.cognizant.trumobi.R;


public class PersonaLiveFolder extends PersonaFolder {
    private AsyncTask<PersonaLiveFolderInfo,Void,Cursor> mLoadingTask;

    public PersonaLiveFolder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    static PersonaLiveFolder fromXml(Context context, PersonaFolderInfo personaFolderInfo) {
        final int layout = isDisplayModeList(personaFolderInfo) ?
                R.layout.pr_live_folder_list : R.layout.pr_live_folder_grid;
        return (PersonaLiveFolder) LayoutInflater.from(context).inflate(layout, null);
    }

    private static boolean isDisplayModeList(PersonaFolderInfo personaFolderInfo) {
        return ((PersonaLiveFolderInfo) personaFolderInfo).displayMode ==
                LiveFolders.DISPLAY_MODE_LIST;
    }

    @SuppressWarnings("rawtypes")
	@Override
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        PersonaLiveFolderAdapter.ViewHolder holder = (PersonaLiveFolderAdapter.ViewHolder) v.getTag();

        if (holder.useBaseIntent) {
            final Intent baseIntent = ((PersonaLiveFolderInfo) mInfo).baseIntent;
            if (baseIntent != null) {
                final Intent intent = new Intent(baseIntent);
                Uri uri = baseIntent.getData();
                uri = uri.buildUpon().appendPath(Long.toString(holder.id)).build();
                intent.setData(uri);
        		// set bound
        		if (v != null) {
        		    Rect targetRect = new Rect();
        		    v.getGlobalVisibleRect(targetRect);
        		    try{
        		    	intent.setSourceBounds(targetRect);
        		    }catch(NoSuchMethodError e){};
        		}        
                mLauncher.startActivitySafely(intent);
                if (mLauncher.autoCloseFolder) {
                    mLauncher.closeFolder(this);
                }
            }
        } else if (holder.intent != null) {
    		if (v != null) {
    		    Rect targetRect = new Rect();
    		    v.getGlobalVisibleRect(targetRect);
    		    try{
    		    	holder.intent.setSourceBounds(targetRect);
    		    }catch(NoSuchMethodError e){};
    		}        
            mLauncher.startActivitySafely(holder.intent);
            if (mLauncher.autoCloseFolder) {
                mLauncher.closeFolder(this);
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    void bind(PersonaFolderInfo info) {
        super.bind(info);
        if (mLoadingTask != null && mLoadingTask.getStatus() == AsyncTask.Status.RUNNING) {
            mLoadingTask.cancel(true);
        }
        mLoadingTask = new FolderLoadingTask(this).execute((PersonaLiveFolderInfo) info);
    }

    @Override
    void onOpen() {
        super.onOpen();
        requestFocus();
    }

    @Override
    void onClose() {
        super.onClose();
        if (mLoadingTask != null && mLoadingTask.getStatus() == AsyncTask.Status.RUNNING) {
            mLoadingTask.cancel(true);
        }

        // The adapter can be null if onClose() is called before FolderLoadingTask
        // is done querying the provider
        final PersonaLiveFolderAdapter adapter = (PersonaLiveFolderAdapter) mContent.getAdapter();
        if (adapter != null) {
            adapter.cleanup();
        }
    }

    static class FolderLoadingTask extends AsyncTask<PersonaLiveFolderInfo, Void, Cursor> {
        private final WeakReference<PersonaLiveFolder> mFolder;
        private PersonaLiveFolderInfo mInfo;

        FolderLoadingTask(PersonaLiveFolder folder) {
            mFolder = new WeakReference<PersonaLiveFolder>(folder);
        }

        protected Cursor doInBackground(PersonaLiveFolderInfo... params) {
            final PersonaLiveFolder folder = mFolder.get();
            if (folder != null) {
                mInfo = params[0];
                return PersonaLiveFolderAdapter.query(folder.mLauncher, mInfo);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (!isCancelled()) {
                if (cursor != null) {
                    final PersonaLiveFolder folder = mFolder.get();
                    if (folder != null) {
                        final PersonaLauncher personaLauncher = folder.mLauncher;
                        folder.setContentAdapter(new PersonaLiveFolderAdapter(personaLauncher, mInfo, cursor));
                    }
                }
            } else if (cursor != null) {
                cursor.close();
            }
        }
    }
}
