package com.cognizant.trumobi.container.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.container.Utils.UtilList;

@SuppressLint("NewApi")
public class ViewerFragmentDummy extends Fragment {

	ViewerListener mViewerListener;
	static String displayMsg = "";
	
	static String filePathCompare = "";

	public static ViewerFragmentDummy newInstance(Bundle args) {
		
		ViewerFragmentDummy viewFrament = new ViewerFragmentDummy();
		viewFrament.setArguments(args);
		displayMsg = args.getString("displayMsg", "");
		return viewFrament;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.con_activity_tab_dum, container,
				false);

		TextView fileHeader = (TextView) view
				.findViewById(R.id.dummy_display);
		
		fileHeader.setTypeface(UtilList.getTextTypeFaceNormal(getActivity()));
		fileHeader.setText(displayMsg);
		//fileHeader.setTextColor(R.color.Black);
		
		return view;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
