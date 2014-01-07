package com.cognizant.trumobi.log;

class Log {

	static boolean DEBUG_LOG = false; // DO NOT CHECK IN WITH THIS SET TO TRUE

	Log() {
	}

	static void v(String tag, String msg) {
		if (DEBUG_LOG) {
			android.util.Log.v(tag, msg);
		}

	}

	static void v(String tag, String msg, Throwable tr) {
		if (DEBUG_LOG) {
			android.util.Log.v(tag, msg, tr);
		}
	}

	static void d(String tag, String msg) {
		if (DEBUG_LOG) {
			android.util.Log.d(tag, msg);
		}
	}

	static void d(String tag, String msg, Throwable tr) {
		if (DEBUG_LOG) {
			android.util.Log.d(tag, msg, tr);
		}
	}

	static void i(String tag, String msg) {
		if (DEBUG_LOG) {
			android.util.Log.i(tag, msg);
		}
	}

	static void i(String tag, String msg, Throwable tr) {
		if (DEBUG_LOG) {
			android.util.Log.i(tag, msg, tr);
		}
	}

	static void w(String tag, String msg) {
		if (DEBUG_LOG) {
			android.util.Log.w(tag, msg);
		}
	}

	static void w(String tag, String msg, Throwable tr) {
		if (DEBUG_LOG) {
			android.util.Log.w(tag, msg, tr);
		}
	}

	static void w(String tag, Throwable tr) {
		if (DEBUG_LOG) {
			android.util.Log.w(tag, tr);
		}
	}

	static void e(String tag, String msg) {
		if (DEBUG_LOG) {
			android.util.Log.e(tag, msg);
		}

	}

	static void e(String tag, String msg, Throwable tr) {
		if (DEBUG_LOG) {
			android.util.Log.e(tag, msg, tr);
		}

	}

}
