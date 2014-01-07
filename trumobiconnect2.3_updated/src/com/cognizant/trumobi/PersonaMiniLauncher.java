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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.Toast;

import com.cognizant.trumobi.PersonaFlingGesture.FlingListener;
import com.cognizant.trumobi.R;

public class PersonaMiniLauncher extends ViewGroup implements View.OnLongClickListener, PersonaDropTarget,
                                     PersonaDragController.DragListener,PersonaDragSource,
                                     FlingListener {
    private static final int HORIZONTAL=1;
    private static final int VERTICAL=0;
	private PersonaLauncher mLauncher;
    //private View mDeleteView;
    private int mOrientation=HORIZONTAL;
    private int mNumCells=4;
    private int mCellWidth=20;
    private int mCellHeight=20;
    private TransitionDrawable mBackground;
    /**
     * ADW: Scrolling vars
     */
    private Scroller mScroller;
    private float mLastMotionX;
    private float mLastMotionY;
    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;
    private static final int TOUCH_STATE_DOWN = 2;
    private int mTouchState = TOUCH_STATE_REST;
    private int mTouchSlop;
    private final int mScrollingSpeed=600;
    private boolean mScrollAllowed=false;
    private PersonaDragController mDragger;
    private final PersonaFlingGesture mFlingGesture = new PersonaFlingGesture();
    private int mCurrentIndex = 0;

    public PersonaMiniLauncher(Context context) {
        super(context);
    }

    public PersonaMiniLauncher(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PersonaMiniLauncher(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

		setHapticFeedbackEnabled(true);
		TypedArray a=context.obtainStyledAttributes(attrs,R.styleable.MiniLauncher,defStyle,0);
		mOrientation=a.getInt(R.styleable.MiniLauncher_orientation, mOrientation);
		mNumCells=a.getInt(R.styleable.MiniLauncher_cells, mNumCells);
		mCellWidth=a.getDimensionPixelSize(R.styleable.MiniLauncher_cellWidth, mCellWidth);
		mCellHeight=a.getDimensionPixelSize(R.styleable.MiniLauncher_cellHeight, mCellHeight);
        mScroller = new Scroller(getContext());
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mFlingGesture.setListener(this);
    }

    public boolean acceptDrop(PersonaDragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
		return true;
    }

    public Rect estimateDropLocation(PersonaDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo, Rect recycle) {
        return null;
    }

    private int FindItemDropPos(int x, int y) {
    	final boolean horizontal = (mOrientation==HORIZONTAL);
    	final int count=getChildCount();
    	int margin = horizontal ? ((getMeasuredWidth())/2)-(((count*mCellWidth)/2)) :
    	                          ((getMeasuredHeight())/2)-(((count*mCellHeight)/2));
    	if (margin < 0)
    		margin = 0;
    	// check for drop on an item
    	int dropPos = horizontal ? (x + getScrollX()) : (y +  getScrollY());

	    for (int i = 0; i < count; i++) {
	        View child = getChildAt(i);
	        PersonaItemInfo item=(PersonaItemInfo) child.getTag();
	        if (child.getVisibility() != GONE) {

	        	int bound1 = margin + (item.cellX * (horizontal ? mCellWidth : mCellHeight));
                int bound2 = bound1 + (horizontal ? mCellWidth : mCellHeight);

	            if (dropPos < bound1 && item.cellX == 0) // before the first item
	            	return 0;
	            if (dropPos >= bound2 && item.cellX == (count - 1))
	            	return count;

	            if (dropPos >= bound1 && dropPos < bound2) {
	            	int pos = item.cellX;
	            	for (int j = 0; j < item.cellX; j++) {
	            		View chld = getChildAt(j);
	            		if (chld.getVisibility() == GONE)
	            			pos++;
	            	}

	            	int middle = bound1 + (((mOrientation==HORIZONTAL) ? mCellWidth : mCellHeight) / 2);
	            	if (dropPos >= middle)
	            		return pos + 1;
	            	return pos;
	            }

	        }
	    }
	    //something went wrong...
	    return getChildCount();
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     * @param insert When true, the child is inserted at the beginning of the children list.
     */

    public void onDrop(PersonaDragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
    	final PersonaLauncherModel model = PersonaLauncher.getModel();
		PersonaItemInfo info = (PersonaItemInfo) dragInfo;
		boolean accept=true;
        switch (info.itemType) {
        case PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
        case PersonaLauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
        case PersonaLauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
        case PersonaLauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
        	//we do accept those
        	break;
        case PersonaLauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
        	Toast t=Toast.makeText(getContext(), R.string.toast_widgets_not_supported, Toast.LENGTH_SHORT);
        	t.show();
        	accept=false;
        	return;
        default:
        	Toast t2=Toast.makeText(getContext(), R.string.toast_unknown_item, Toast.LENGTH_SHORT);
        	t2.show();
        	accept=false;
        	return;
        }
        info.cellX=FindItemDropPos(x, y);
        //add it to launcher database
        if (info instanceof PersonaLauncherAppWidgetInfo) {
            model.removeDesktopAppWidget((PersonaLauncherAppWidgetInfo) info);
            final PersonaLauncherAppWidgetInfo personaLauncherAppWidgetInfo = (PersonaLauncherAppWidgetInfo) info;
            final PersonaLauncherAppWidgetHost appWidgetHost = mLauncher.getAppWidgetHost();
            if (appWidgetHost != null) {
                appWidgetHost.deleteAppWidgetId(personaLauncherAppWidgetInfo.appWidgetId);
            }
        }
        if(accept){
	        model.addDesktopItem(info);
	        PersonaLauncherModel.addOrMoveItemInDatabase(mLauncher, info,
	                PersonaLauncherSettings.Favorites.CONTAINER_DOCKBAR, -1, info.cellX, -1);
	        addItemInDockBar(info);
	        // Reorder the other items:
	        for(int i = 0; i < getChildCount(); i++) {
	        	View child = getChildAt(i);
	        	PersonaItemInfo childInfo = (PersonaItemInfo)child.getTag();
	        	if (childInfo != info && childInfo.cellX >= info.cellX) {
	        		childInfo.cellX += 1;
	                PersonaLauncherModel.moveItemInDatabase(mLauncher, childInfo,
	                        PersonaLauncherSettings.Favorites.CONTAINER_DOCKBAR, -1, childInfo.cellX, -1);
	        	}
	        }

        }else{
        	PersonaLauncherModel.deleteItemFromDatabase(mLauncher, info);
        }
    }

    public void onDragEnter(PersonaDragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
    	mBackground.startTransition(200);
    }

    public void onDragOver(PersonaDragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
    }

    public void onDragExit(PersonaDragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
    	mBackground.resetTransition();
    }

    public void onDragStart(View v, PersonaDragSource source, Object info, int dragAction) {

    }

    public void onDragEnd() {

    }
    public void addItemInDockBar(PersonaItemInfo info){
    	View view=null;
        switch (info.itemType) {
        case PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
        case PersonaLauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
            if (info.container == NO_ID) {
                // Came from all apps -- make a copy
                info = new PersonaApplicationInfo((PersonaApplicationInfo) info);
            }
            view = mLauncher.createSmallShortcut(R.layout.pr_small_application, this,
                    (PersonaApplicationInfo) info);
            break;
        case PersonaLauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
            view = mLauncher.createSmallLiveFolder(R.layout.pr_small_application, this,
                    (PersonaLiveFolderInfo) info);
            break;
        case PersonaLauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
            view = mLauncher.createSmallFolder(R.layout.pr_small_application, this,
                    (PersonaUserFolderInfo) info);
            break;
        case PersonaLauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
        	//Toast t=Toast.makeText(getContext(), "Widgets not supported... sorry :-)", Toast.LENGTH_SHORT);
        	//t.show();
        	return;
        default:
        	//Toast t2=Toast.makeText(getContext(), "Unknown item. We can't add unknown item types :-)", Toast.LENGTH_SHORT);
        	//t2.show();
        	return;
            //throw new IllegalStateException("Unknown item type: " + info.itemType);
        }
        view.setLongClickable(true);
        view.setOnLongClickListener(this);
        view.setBackgroundDrawable(PersonaIconHighlights.getDrawable(mLauncher,PersonaIconHighlights.TYPE_DOCKBAR));
        addView(view);
        invalidate();
    }

	public boolean onLongClick(View v) {
        if (!v.isInTouchMode() || mLauncher.isDesktopBlocked()) {
            return false;
        }
		//ADW Delete the item and reposition the remaining ones
		PersonaItemInfo item=(PersonaItemInfo) v.getTag();
		//Now we need to update database (and position) for remainint items
		final int count=getChildCount();
		for(int i=0;i<count;i++){
			final View cell=getChildAt(i);
			final PersonaItemInfo info = (PersonaItemInfo) cell.getTag();
            if(info.cellX>item.cellX){
            	info.cellX-=1;
            	cell.setTag(info);
                PersonaLauncherModel.moveItemInDatabase(mLauncher, info,
                        PersonaLauncherSettings.Favorites.CONTAINER_DOCKBAR, -1, info.cellX, -1);
            }
        }
		requestLayout();
		PersonaLauncher.getModel().removeDesktopItem(item);
        mDragger.startDrag(v, this, item, PersonaDragController.DRAG_ACTION_COPY);
        detachViewFromParent(v);
        removeView(v);
        return true;
	}


    void setLauncher(PersonaLauncher personaLauncher) {
        mLauncher = personaLauncher;
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
        	child.measure(mCellWidth, mCellHeight);
        }

		if(mOrientation==HORIZONTAL){
			super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mCellHeight, MeasureSpec.AT_MOST));
		}else{
			super.onMeasure(MeasureSpec.makeMeasureSpec(mCellWidth, MeasureSpec.AT_MOST),heightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int count = getChildCount();


		int marginLeft=((getMeasuredWidth())/2)-(((count*mCellWidth)/2));
		int marginTop=((getMeasuredHeight())/2)-(((count*mCellHeight)/2));
		if(getChildCount()>mNumCells){
			if(mOrientation==HORIZONTAL){
				marginLeft=0;
				mCellWidth=(getMeasuredWidth()/mNumCells);
			}else{
				marginTop=0;
				mCellHeight=(getMeasuredHeight()/mNumCells);
			}
			mScrollAllowed=true;
			snapScroll();
		}else{
			mScrollAllowed=false;
			scrollTo(0, 0);
		}
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            PersonaItemInfo item=(PersonaItemInfo) child.getTag();
            if (child.getVisibility() != GONE) {
                int childLeft=(mOrientation==HORIZONTAL)?marginLeft+(item.cellX*mCellWidth):0;
                int childTop = (mOrientation==VERTICAL)?marginTop+(item.cellX*mCellHeight):0;
                int childRight = childLeft+mCellWidth;
                int childBottom = childTop+mCellHeight;
                child.layout(childLeft, childTop, childRight, childBottom);
            }
        }

	}
    /**
     * ADW: Lets add scrolling capabilities :P
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	if(!mScrollAllowed){
    		return super.onInterceptTouchEvent(ev);
    	}
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }
        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);

                final int touchSlop = mTouchSlop;
                boolean xMoved = xDiff > touchSlop;
                boolean yMoved = yDiff > touchSlop;

                if (xMoved || yMoved) {
                    if (xMoved && mOrientation==HORIZONTAL) {
                        mTouchState = TOUCH_STATE_SCROLLING;
                    }else if(yMoved && mOrientation==VERTICAL){
                    	mTouchState = TOUCH_STATE_SCROLLING;
                    }
                }
                break;

            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
            	mTouchState=mScroller.isFinished() ? TOUCH_STATE_REST:TOUCH_STATE_SCROLLING;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
        }

        return mTouchState != TOUCH_STATE_REST;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	if(!mScrollAllowed){
    		return super.onTouchEvent(ev);
    	}
        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();
        mFlingGesture.ForwardTouchEvent(ev);

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            mTouchState = TOUCH_STATE_DOWN;
            // Remember where the motion event started
            mLastMotionX = x;
            mLastMotionY = y;
            break;
        case MotionEvent.ACTION_MOVE:
        	if (mTouchState == TOUCH_STATE_SCROLLING) {
            	// Scroll to follow the motion event
                final int delta = (mOrientation==HORIZONTAL)?(int) (mLastMotionX - x):(int) (mLastMotionY - y);
                if(Math.abs(delta)>mTouchSlop || mTouchState == TOUCH_STATE_SCROLLING){
                	mTouchState = TOUCH_STATE_SCROLLING;
	                mLastMotionX = x;
	                mLastMotionY = y;
	                if (delta < 0) {
                        if(mOrientation==HORIZONTAL)
                        	scrollBy(delta, 0);
                        else
                        	scrollBy(0,delta);
	                } else if (delta > 0) {
                    	if(mOrientation==HORIZONTAL)
                    		scrollBy(delta, 0);
                    	else
                    		scrollBy(0,delta);
	                }
                }
            }
            break;
        case MotionEvent.ACTION_UP:
            mTouchState = TOUCH_STATE_REST;
            invalidate();
            break;
        case MotionEvent.ACTION_CANCEL:
            mTouchState = TOUCH_STATE_REST;
        }
        return true;
    }

	@Override
	public void OnFling(int Direction) {
		int leftDir = (mOrientation == HORIZONTAL)?PersonaFlingGesture.FLING_LEFT:PersonaFlingGesture.FLING_DOWN;
		int rightDir = (mOrientation == HORIZONTAL)?PersonaFlingGesture.FLING_RIGHT:PersonaFlingGesture.FLING_UP;

        if (Direction == leftDir && mCurrentIndex > 0) {
            // Fling hard enough to move left
        	snapToItem(mCurrentIndex - getItemsPerPage());
        } else if (Direction == rightDir && mCurrentIndex < getChildCount() - 1) {
            // Fling hard enough to move right
        	snapToItem(mCurrentIndex + getItemsPerPage());
        }
        else
        	snapScroll();
	}

    private void snapToItem(int index) {
    	if (index < 0)
    		index = 0;
    	if (index > getChildCount() - getItemsPerPage())
    		index = getChildCount() - getItemsPerPage();
    	final int actualScroll=(mOrientation==HORIZONTAL)?getScrollX():getScrollY();
        final int cellSize=(mOrientation==HORIZONTAL)?mCellWidth:mCellHeight;
        final int target = cellSize * index;
        final int delta=target-actualScroll;
        if(mOrientation==HORIZONTAL){
        	mScroller.startScroll(actualScroll, 0, delta, 0, mScrollingSpeed);
        }else{
        	mScroller.startScroll(0,actualScroll, 0, delta, mScrollingSpeed);
        }
        mCurrentIndex = index;
    }

    private int getItemsPerPage() {
    	final int actualLimit=(mOrientation==HORIZONTAL)?getWidth():getHeight();
    	final int cellSize=(mOrientation==HORIZONTAL)?mCellWidth:mCellHeight;
    	return actualLimit / cellSize;
    }

    private void snapScroll(){
        final int actualScroll=(mOrientation==HORIZONTAL)?getScrollX():getScrollY();
        final int cellSize=(mOrientation==HORIZONTAL)?mCellWidth:mCellHeight;
        final int actualLimit=(mOrientation==HORIZONTAL)?getWidth():getHeight();
    	int position = actualScroll-(actualScroll%cellSize);
    	if(position<0)position=0;
    	if(position>((getChildCount())*cellSize)-actualLimit) position=(getChildCount()*cellSize)-actualLimit;
    	mCurrentIndex = position / cellSize;
        final int delta=position-actualScroll;
        if(mOrientation==HORIZONTAL){
        	mScroller.startScroll(actualScroll, 0, delta, 0, mScrollingSpeed);
        }else{
        	mScroller.startScroll(0,actualScroll, 0, delta, mScrollingSpeed);
        }
        invalidate();
    }
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }

	@Override
	public void setBackgroundDrawable(Drawable d) {
		// TODO Auto-generated method stub
		if(mBackground!=null)mBackground.setCallback(null);
		super.setBackgroundDrawable(d);
		mBackground=(TransitionDrawable) d;
		mBackground.setCrossFadeEnabled(true);

	}
	/**
	 * ADW: Reload the proper icons
	 * This is mainly used when the apps from SDcard are available in froyo
	 */
	public void reloadIcons(String packageName){
		final int count=getChildCount();
		for(int i=0;i<count;i++){
			final View cell=getChildAt(i);
			final PersonaItemInfo personaItemInfo = (PersonaItemInfo) cell.getTag();
	        if(personaItemInfo instanceof PersonaApplicationInfo){
	            final PersonaApplicationInfo info=(PersonaApplicationInfo)personaItemInfo;
	            final Intent intent = info.intent;
	            final ComponentName name = intent.getComponent();
	            if ((info.itemType == PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
	                    info.itemType == PersonaLauncherSettings.Favorites.ITEM_TYPE_SHORTCUT)&&
	                    Intent.ACTION_MAIN.equals(intent.getAction()) && name != null &&
	                    packageName.equals(name.getPackageName())) {
	                final Drawable icon = PersonaLauncher.getModel().getApplicationInfoIcon(
	                        mLauncher.getPackageManager(), info, mLauncher);
	                if (icon != null && icon != info.icon) {
	                    info.icon.setCallback(null);
	                    info.icon = PersonaUtilities.createIconThumbnail(icon, mLauncher);
	                    info.filtered = true;
	                    ((ImageView)cell).setImageDrawable(PersonaUtilities.drawReflection(info.icon, mLauncher));
	                }
	            }
	        }
        }
	}

	public void onDropCompleted(View target, boolean success) {
	}
	public void setDragger(PersonaDragController dragger) {
		mDragger=dragger;
	}
	public void updateCounters(String packageName, int counter, int color){
        final int count=getChildCount();
	    for(int i=0;i<count;i++){
            final View view=getChildAt(i);
            final Object tag = view.getTag();
            if (tag instanceof PersonaApplicationInfo) {
	            PersonaApplicationInfo info = (PersonaApplicationInfo) tag;
	            // We need to check for ACTION_MAIN otherwise getComponent() might
	            // return null for some shortcuts (for instance, for shortcuts to
	            // web pages.)
	            final Intent intent = info.intent;
	            final ComponentName name = intent.getComponent();
	            if ((info.itemType==PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION||
	                    info.itemType==PersonaLauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) &&
	                Intent.ACTION_MAIN.equals(intent.getAction()) && name != null &&
	                packageName.equals(name.getPackageName())) {
	                if(view instanceof PersonaCounterImageView)
	                    ((PersonaCounterImageView) view).setCounter(counter, color);
	                //else if
	                view.invalidate();
	                PersonaLauncher.getModel().updateCounterDesktopItem(info, counter, color);
	            }
	        }

	    }
	}
}
