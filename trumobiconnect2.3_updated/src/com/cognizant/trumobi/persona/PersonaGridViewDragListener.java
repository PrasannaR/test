package com.cognizant.trumobi.persona;
/*package com.cognizant.trumobi.persona;

import android.graphics.Color;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.ImageView;

public class GridViewDragListener implements OnDragListener {

	int imagePosition;
	public GridViewDragListener(int position) {
	        imagePosition = position;
	    }
	
	@Override
	public boolean onDrag(View arg0, DragEvent event) {
        switch (event.getAction()) {
        case DragEvent.ACTION_DRAG_STARTED:
            PersonaLog.i("DRAG Started", "" + imagePosition);
          break;
        case DragEvent.ACTION_DRAG_ENTERED:
            PersonaLog.i("DRAG Entered", "" + imagePosition);
            ImageView imageEnter = (ImageView) Grid.getChildAt(imagePosition);
           imageEnter.setBackgroundColor(Color.argb(155, 100, 200, 255));
          break;
        case DragEvent.ACTION_DRAG_EXITED:        
            PersonaLog.i("DRAG Exited", "" + imagePosition);
           ImageView imageExit = (ImageView) Grid.getChildAt(imagePosition);
           imageExit.setBackgroundColor(Color.argb(0, 0, 0, 0));
          break;
        case DragEvent.ACTION_DROP:
            PersonaLog.i("DRAG Dropped", "" + imagePosition);

          break;
        case DragEvent.ACTION_DRAG_ENDED:
            PersonaLog.i("DRAG Ended", "" + imagePosition);
          default:
          break;
        }
        return true;
    }

	
	
	
}
*/