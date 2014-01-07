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

import android.view.View;
import com.cognizant.trumobi.R;
/**
 * Interface for initiating a drag within a view or across multiple views.
 *
 */
public interface PersonaDragController {
    
    /**
     * Interface to receive notifications when a drag starts or stops
     */
    interface DragListener {
        
        /**
         * A drag has begun
         * 
         * @param v The view that is being dragged
         * @param source An object representing where the drag originated
         * @param info The data associated with the object that is being dragged
         * @param dragAction The drag action: either {@link PersonaDragController#DRAG_ACTION_MOVE}
         *        or {@link PersonaDragController#DRAG_ACTION_COPY}
         */
        void onDragStart(View v, PersonaDragSource source, Object info, int dragAction);
        
        /**
         * The drag has eneded
         */
        void onDragEnd();
    }
    
    /**
     * Indicates the drag is a move.
     */
    public static int DRAG_ACTION_MOVE = 0;

    /**
     * Indicates the drag is a copy.
     */
    public static int DRAG_ACTION_COPY = 1;

    /**
     * Starts a drag
     * 
     * @param v The view that is being dragged
     * @param source An object representing where the drag originated
     * @param info The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *        {@link #DRAG_ACTION_COPY}
     */
    void startDrag(View v, PersonaDragSource source, Object info, int dragAction);
}
