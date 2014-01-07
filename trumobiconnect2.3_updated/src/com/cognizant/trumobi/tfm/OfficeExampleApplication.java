package com.cognizant.trumobi.tfm;

import android.content.Context;
import android.widget.Toast;

import com.tf.thinkdroid.common.app.IdleHandler;
import com.tf.thinkdroid.common.app.TFApplication;

public class OfficeExampleApplication extends TFApplication {
    
	@Override
    public IdleHandler createIdleHandler() {
        return new TestIdleHandler();
    }
    
    protected class TestIdleHandler implements IdleHandler {

        @Override
        public int getIdleTimeout() {
            return 10000;   // ms
        }

        @Override
        public void onTimeout(Context context) {
            Toast.makeText(context, "Over time", Toast.LENGTH_SHORT).show();
        }

		@Override
		public boolean isStrict() {
			// TODO Auto-generated method stub
			return true;
		}
        
    }
}
