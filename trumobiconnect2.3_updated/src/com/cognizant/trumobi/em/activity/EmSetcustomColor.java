package com.cognizant.trumobi.em.activity;

import java.lang.reflect.Constructor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cognizant.trumobi.R;

public class EmSetcustomColor implements LayoutInflater.Factory {

	static final Class<?>[] constructorSignature = new Class[] { Context.class,
			AttributeSet.class };

	@Override
	public View onCreateView(String name, final Context context,
			AttributeSet attrs) {
		// TODO Auto-generated method stub
		boolean optionsMenu = (name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuItemView")
								|| name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuView"));
		final boolean contextMenu = name.equalsIgnoreCase("com.android.internal.view.menu.ListMenuItemView");
		if (optionsMenu || contextMenu) {
			
			try {
				@SuppressWarnings("unchecked")
				Class<? extends ViewGroup> clazz = (Class<? extends ViewGroup>) context.getClassLoader()
						.loadClass(name).asSubclass(View.class);
				Constructor<? extends ViewGroup> constructor = clazz
						.getConstructor(constructorSignature);
				final View view = constructor.newInstance(new Object[] {
						context, attrs });

				new Handler().post(new Runnable() {

					@SuppressLint("ResourceAsColor")
					public void run() {
						try {
							// view.setBackgroundColor(R.color.menu_option_color);
							// view.setBackgroundResource(R.drawable.list_read_holo);
							view.setBackgroundDrawable(context
									.getResources()
									.getDrawable(contextMenu? (R.drawable.em_contextmenuselector) : (R.drawable.em_optionsmenuselector)));
						} catch (Exception e) {
							Log.i("Liiiittttttttt", "Caught Exception!", e);
						}

					}
				});
				return view;
			} catch (Exception e) {
				Log.i("Liiiiiiiiiiiiii", "Caught Exception!", e);
			}
		}
		return null;
	}

}
