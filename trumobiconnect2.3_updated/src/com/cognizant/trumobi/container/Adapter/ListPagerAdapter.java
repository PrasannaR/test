package com.cognizant.trumobi.container.Adapter;
/*package com.cognizant.seccontainerapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cognizant.seccontainerapp.activity.R;

public class ListPagerAdapter extends PagerAdapter {
	Context context;

	public ListPagerAdapter(Context con) {
		// TODO Auto-generated constructor stub
		this.context = con;

	}

	public int getCount() {
		return 2;
	}

	public Object instantiateItem(View collection, int position) {
		LayoutInflater inflater = (LayoutInflater) collection.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int resId = 0;
		switch (position) {
		case 0:
			resId = R.layout.lay1;
			

			break;
		case 1:
			resId = R.layout.lay2;
			break;
		case 2:
			resId = R.layout.lay3;
			break;
		case 3:
			resId = R.layout.lay4;
			break;
		case 4:
			resId = R.layout.lay5;
			break;
		}

		View view = inflater.inflate(resId, null);

		((ViewPager) collection).addView(view, 0);
		TextView t1 = (TextView) findViewById(R.id.textView1);
		t1.setTextColor(Color.GREEN);
		t1.setOnTouchListener((OnTouchListener) context);
		LinearLayout l1 = (LinearLayout) findViewById(R.id.pinkLayout);
		LinearLayout l2 = (LinearLayout) findViewById(R.id.yellowLayout);
		l1.setOnDragListener((OnDragListener) context);
		l2.setOnDragListener((OnDragListener) context);
		return view;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		// ((ViewPager) arg0).removeView((View) arg2);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == ((View) arg1);
	}

	@Override
	public Parcelable saveState() {

		return null;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {

		// TODO Auto-generated method stub
		super.restoreState(state, loader);
	}

}*/