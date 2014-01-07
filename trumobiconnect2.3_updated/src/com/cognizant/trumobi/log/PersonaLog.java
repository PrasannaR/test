package com.cognizant.trumobi.log;

public class PersonaLog {

	private static boolean DEBUG_LOG = true;// DO NOT CHECK IN WITH THIS SET TO TRUE

	public static void v(String tag, String msg) {
		if (DEBUG_LOG) {
			Log.v(tag, msg);
		}

	}

	public static void v(String tag, String msg, Throwable tr) {
		if (DEBUG_LOG) {
			Log.v(tag, msg, tr);
		}
	}

	public static void d(String tag, String msg) {
		if (DEBUG_LOG) {
			Log.d(tag, msg);
		}
	}

	public static void d(String tag, String msg, Throwable tr) {
		if (DEBUG_LOG) {
			Log.d(tag, msg, tr);
		}
	}

	public static void i(String tag, String msg) {
		if (DEBUG_LOG) {
			Log.i(tag, msg);
		}
	}

	public static void i(String tag, String msg, Throwable tr) {
		if (DEBUG_LOG) {
			Log.i(tag, msg, tr);
		}
	}

	public static void w(String tag, String msg) {
		if (DEBUG_LOG) {
			Log.w(tag, msg);
		}
	}

	public static void w(String tag, String msg, Throwable tr) {
		if (DEBUG_LOG) {
			Log.w(tag, msg, tr);
		}
	}

	public static void w(String tag, Throwable tr) {
		if (DEBUG_LOG) {
			Log.w(tag, tr);
		}
	}

	public static void e(String tag, String msg) {
		if (DEBUG_LOG) {
			Log.e(tag, msg);
		}

	}

	public static void e(String tag, String msg, Throwable tr) {
		if (DEBUG_LOG) {
			Log.e(tag, msg, tr);
		}
	}
}
