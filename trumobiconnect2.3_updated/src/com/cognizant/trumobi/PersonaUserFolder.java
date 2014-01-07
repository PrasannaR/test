package com.cognizant.trumobi;


import java.util.ArrayList;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cognizant.trumobi.R;

/**
 * PersonaFolder which contains applications or shortcuts chosen by the user.
 *
 */
public class PersonaUserFolder extends PersonaFolder implements PersonaDropTarget {
    public PersonaUserFolder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    /**
     * Creates a new PersonaUserFolder, inflated from R.layout.user_folder.
     *
     * @param context The application's context.
     *
     * @return A new PersonaUserFolder.
     */
    static PersonaUserFolder fromXml(Context context) {
        return (PersonaUserFolder) LayoutInflater.from(context).inflate(R.layout.pr_user_folder, null);
    }

    public boolean acceptDrop(PersonaDragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
        final PersonaItemInfo item = (PersonaItemInfo) dragInfo;
        final int itemType = item.itemType;
        return (itemType == PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == PersonaLauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) && item.container != mInfo.id;
    }
    
    public Rect estimateDropLocation(PersonaDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo, Rect recycle) {
        return null;
    }

    @SuppressWarnings("unchecked")
    public void onDrop(PersonaDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
        final PersonaApplicationInfo item = (PersonaApplicationInfo) dragInfo;
        //noinspection unchecked
        ((ArrayAdapter<PersonaApplicationInfo>) mContent.getAdapter()).add((PersonaApplicationInfo) dragInfo);
        PersonaLauncherModel.addOrMoveItemInDatabase(mLauncher, item, mInfo.id, 0, 0, 0);
    }

    public void onDragEnter(PersonaDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
    }

    public void onDragOver(PersonaDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
    }

    public void onDragExit(PersonaDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onDropCompleted(View target, boolean success) {
        if (success) {
            //noinspection unchecked
            ArrayAdapter<PersonaApplicationInfo> adapter =
                    (ArrayAdapter<PersonaApplicationInfo>) mContent.getAdapter();
            adapter.remove(mDragItem);
        }
    }

    void bind(PersonaFolderInfo info) {
        super.bind(info);
        //setContentAdapter(new PersonaApplicationsAdapter(mLauncher, ((PersonaUserFolderInfo) info).contents));
        setContentAdapter(new FolderAdapter(mLauncher, ((PersonaUserFolderInfo) info).contents));
    }

    // When the folder opens, we need to refresh the GridView's selection by
    // forcing a layout
    @Override
    void onOpen() {
        super.onOpen();
        requestFocus();
    }
    private class FolderAdapter extends ArrayAdapter<PersonaApplicationInfo> {
    	private LayoutInflater mInflater;
    	private Drawable mBackground;
    	private int mTextColor = 0;
    	private boolean useThemeTextColor = false;
        private Typeface themeFont=null;
    	
		public FolderAdapter(Context context, ArrayList<PersonaApplicationInfo> icons) {
			super(context, 0,icons);
			mInflater=LayoutInflater.from(context);
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
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final PersonaApplicationInfo info = getItem(position);

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.pr_application_boxed, parent,
						false);
			}

			if (!info.filtered) {
				info.icon = PersonaUtilities.createIconThumbnail(info.icon, getContext());
				info.filtered = true;
			}

			final TextView textView = (TextView) convertView;
			textView.setCompoundDrawablesWithIntrinsicBounds(null, info.icon, null,
					null);
			textView.setText(info.title);
			if (useThemeTextColor) {
				textView.setTextColor(mTextColor);
			}
			//ADW: Custom font
			if(themeFont!=null) textView.setTypeface(themeFont);
			// so i'd better not use it, sorry themers
			if (mBackground != null)
				convertView.setBackgroundDrawable(mBackground);
			return convertView;
		}
    	
    }
}
