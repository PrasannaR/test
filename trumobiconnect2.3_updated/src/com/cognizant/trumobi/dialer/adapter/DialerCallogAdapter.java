package com.cognizant.trumobi.dialer.adapter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.dialer.dbController.DialerCallLogList;
import com.cognizant.trumobi.dialer.utils.DialerUtilities;
import com.cognizant.trumobi.em.Email;

public class DialerCallogAdapter extends BaseAdapter {

	private ArrayList<DialerCallLogList> mArrayCal;
	private LayoutInflater inflater = null;
	private ViewHolder viewHolder;
	private CallLogImageLoader imageLoader;
	protected int selectedPos;

	public DialerCallogAdapter(Context mContext,
			ArrayList<DialerCallLogList> mArrayCal) {

		this.mArrayCal = mArrayCal;
		inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new CallLogImageLoader(mContext);
		selectedPos = -1;

	}

	public void setSelectedPosition(int pos) {
		selectedPos = pos;
		// inform the view of this change
		notifyDataSetChanged();
	}

	public int getSelectedPosition() {
		return selectedPos;
	}

	@Override
	public int getCount() {

		return mArrayCal.size();
	}

	@Override
	public Object getItem(int arg0) {

		return arg0;
	}

	@Override
	public long getItemId(int arg0) {

		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final int pos = position;
		if (null == convertView) {
			convertView = inflater.inflate(R.layout.dial_calrowitem, null);
			viewHolder = new ViewHolder();
			viewHolder.mNumber = (TextView) convertView
					.findViewById(R.id.number);
			viewHolder.mName = (TextView) convertView.findViewById(R.id.name);
			viewHolder.mDate = (TextView) convertView.findViewById(R.id.date);
			viewHolder.mCallButton = (ImageView) convertView
					.findViewById(R.id.call);
			viewHolder.mCallOutgoing = (ImageView) convertView
					.findViewById(R.id.calloutgng);
			viewHolder.mCallIncomming = (ImageView) convertView
					.findViewById(R.id.callrecieved);
			viewHolder.mCallMissed = (ImageView) convertView
					.findViewById(R.id.callmissed);
			viewHolder.mContactPhoto = (ImageView) convertView
					.findViewById(R.id.profile);
			viewHolder.mCallCount = (TextView) convertView
					.findViewById(R.id.callTimes);
			convertView.setTag(viewHolder);
		}
		viewHolder = (ViewHolder) convertView.getTag();
		viewHolder.position = position;
		viewHolder.mCallCount.setVisibility(View.GONE);
		viewHolder.mCallIncomming.setVisibility(View.GONE);
		viewHolder.mCallOutgoing.setVisibility(View.GONE);
		viewHolder.mCallMissed.setVisibility(View.GONE);
		String callTimes = mArrayCal.get(position).getCALL_NO_TIMES_STRING();
		int callCount = Integer.parseInt(callTimes);
		if (callCount >= 3) {
			viewHolder.mCallCount.setVisibility(View.VISIBLE);
			viewHolder.mCallCount.setText("(" + callTimes + ")");
		}
		ArrayList<String> callType = mArrayCal.get(position)
				.getCALL_TYPE_STRING();
		ArrayList<String> callDuration = mArrayCal.get(position)
				.getCALL_DURATION();
		int val;
		Date duration;

		if (callDuration.size() >= 1) {
			duration = new Date(Long.parseLong(callDuration.get(0)));
			viewHolder.mDate.setText(DialerUtilities.diffInTime(duration,
					DialerUtilities.frmString(DialerUtilities.frmSysCal())));
		}

		for (int i = 1; i <= callCount; i++) {

			switch (i) {

			case 1:

				viewHolder.mCallOutgoing.setVisibility(View.VISIBLE);

				val = Integer.parseInt(callType.get(0));

				switch (val) {
				case 2:
					viewHolder.mCallOutgoing
							.setImageResource(R.drawable.ic_call_outgoing_holo_dark);
					break;
				case 3:
					viewHolder.mCallOutgoing
							.setImageResource(R.drawable.ic_call_missed_holo_dark);
					break;
				case 1:
					viewHolder.mCallOutgoing
							.setImageResource(R.drawable.ic_call_incoming_holo_dark);
					break;
				case 4:
					viewHolder.mCallOutgoing
							.setImageResource(R.drawable.ic_call_incoming_holo_dark);
					// to set for incoming call rejection from user end
					break;
				default:
					break;
				}

				break;

			case 2:

				viewHolder.mCallMissed.setVisibility(View.VISIBLE);

				val = Integer.parseInt(callType.get(1));

				switch (val) {
				case 2:
					viewHolder.mCallMissed
							.setImageResource(R.drawable.ic_call_outgoing_holo_dark);
					break;
				case 3:
					viewHolder.mCallMissed
							.setImageResource(R.drawable.ic_call_missed_holo_dark);
					break;
				case 1:
					viewHolder.mCallMissed
							.setImageResource(R.drawable.ic_call_incoming_holo_dark);
					break;
				case 4:
					viewHolder.mCallMissed
							.setImageResource(R.drawable.ic_call_incoming_holo_dark);
					// to set for incoming call rejection from user end
					break;
				default:
					break;
				}

				break;

			case 3:

				viewHolder.mCallIncomming.setVisibility(View.VISIBLE);

				val = Integer.parseInt(callType.get(2));

				switch (val) {
				case 2:
					viewHolder.mCallIncomming
							.setImageResource(R.drawable.ic_call_outgoing_holo_dark);
					break;
				case 3:
					viewHolder.mCallIncomming
							.setImageResource(R.drawable.ic_call_missed_holo_dark);
					break;
				case 1:
					viewHolder.mCallIncomming
							.setImageResource(R.drawable.ic_call_incoming_holo_dark);
					break;
				case 4:
					viewHolder.mCallIncomming
							.setImageResource(R.drawable.ic_call_incoming_holo_dark);
					// to set for incoming call rejection from user end
					break;
				default:
					break;
				}

				break;

			default:

				break;

			}
		}

		viewHolder.mName.setText(mArrayCal.get(position).getASSOICIATE_NAME());
		viewHolder.mNumber.setText(mArrayCal.get(position).getNUMBER());

		viewHolder.mCallButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (DialerUtilities.checkSim()) {

					SharedPreferences.Editor prefEditor = new SharedPreferences(
							Email.getAppContext()).edit();
					prefEditor.putBoolean("isCallLogUpdated", true);
					prefEditor.commit();

					DialerUtilities.phoneCallIntent(mArrayCal.get(pos)
							.getNUMBER(), Email.getAppContext());
				} else {
					Toast.makeText(Email.getAppContext(),
							"Insert SIM to make call", Toast.LENGTH_LONG)
							.show();
				}
			}
		});

		if (mArrayCal.get(position).isNativeContact()) {
			if (mArrayCal.get(position).isNativeProfilePic()) {
				imageLoader.DisplayImage(mArrayCal.get(position).getNUMBER(),
						viewHolder.mContactPhoto);
			} else {
				viewHolder.mContactPhoto
						.setImageResource(R.drawable.contacts_ic_contact_picture_holo_light);
			}
		} else {
			byte[] contactPhoto = mArrayCal.get(position).getImg_src();

			if (contactPhoto != null) {
				viewHolder.mContactPhoto.setImageBitmap(DialerUtilities
						.getImage(contactPhoto));
			} else {
				viewHolder.mContactPhoto
						.setImageResource(R.drawable.contacts_ic_contact_picture_holo_light);
			}
		}

		return convertView;
	}

	static class ViewHolder {
		TextView mNumber;
		TextView mName;
		TextView mDate;
		TextView mCallCount;
		ImageView mCallButton;
		ImageView mCallOutgoing, mCallIncomming, mCallMissed;
		ImageView mContactPhoto;
		int position;

	}

	private Bitmap loadPhotoByPhoneNumber(String phoneNumber) {

		Bitmap bitmap = null;
		String[] projection = new String[] { PhoneLookup._ID };
		Uri contactUri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber));
		Cursor cursor = Email.getAppContext().getContentResolver()
				.query(contactUri, projection, null, null, null);

		if (cursor != null && cursor.moveToFirst()) {
			long contactId = cursor.getLong(cursor
					.getColumnIndex(PhoneLookup._ID));

			InputStream inputStream = Contacts
					.openContactPhotoInputStream(Email.getAppContext()
							.getContentResolver(), ContentUris.withAppendedId(
							Contacts.CONTENT_URI, contactId));
			if (inputStream != null) {
				bitmap = BitmapFactory.decodeStream(inputStream);
			}
		}

		if (cursor != null)
			cursor.close();
		return bitmap;

	}

	public class CallLogImageLoader {

		private BitmapCacheManager memoryCache = new BitmapCacheManager();
		private Map<ImageView, String> imageViews = Collections
				.synchronizedMap(new WeakHashMap<ImageView, String>());
		private ExecutorService executorService;
		private Handler handler = new Handler();
		private final int stub_id = R.drawable.contacts_ic_contact_picture_holo_light;

		public CallLogImageLoader(Context context) {
			executorService = Executors.newFixedThreadPool(5);
		}

		public void DisplayImage(String phoneNumber, ImageView imageView) {
			imageViews.put(imageView, phoneNumber);
			Bitmap bitmap = memoryCache.getBitmapFromMemCache(phoneNumber);
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
			} else {
				queuePhoto(phoneNumber, imageView);
				imageView.setImageResource(stub_id);
			}
		}

		private void queuePhoto(String phoneNumber, ImageView imageView) {
			PhotoToLoad p = new PhotoToLoad(phoneNumber, imageView);
			executorService.submit(new PhotosLoader(p));
		}

		private class PhotoToLoad {
			public String phoneNumber;
			public ImageView imageView;

			public PhotoToLoad(String u, ImageView i) {
				phoneNumber = u;
				imageView = i;
			}
		}

		class PhotosLoader implements Runnable {
			PhotoToLoad photoToLoad;

			PhotosLoader(PhotoToLoad photoToLoad) {
				this.photoToLoad = photoToLoad;
			}

			@Override
			public void run() {
				try {
					if (imageViewReused(photoToLoad))
						return;
					Bitmap bmp = loadPhotoByPhoneNumber(photoToLoad.phoneNumber);
					memoryCache.addBitmapToMemoryCache(photoToLoad.phoneNumber,
							bmp);
					if (imageViewReused(photoToLoad))
						return;
					BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
					handler.post(bd);
				} catch (Throwable th) {
					th.printStackTrace();
				}
			}
		}

		boolean imageViewReused(PhotoToLoad photoToLoad) {
			String tag = imageViews.get(photoToLoad.imageView);
			if (tag == null || !tag.equals(photoToLoad.phoneNumber))
				return true;
			return false;
		}

		class BitmapDisplayer implements Runnable {
			Bitmap bitmap;
			PhotoToLoad photoToLoad;

			public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
				bitmap = b;
				photoToLoad = p;
			}

			public void run() {
				if (imageViewReused(photoToLoad))
					return;
				if (bitmap != null)
					photoToLoad.imageView.setImageBitmap(bitmap);
				else
					photoToLoad.imageView.setImageResource(stub_id);
			}
		}

		public void clearCache() {
			memoryCache.clear();
		}

		public class MemoryCache {

			private static final int INITIAL_CAPACITY = 10;
			private static final float LOAD_FACTOR = 1.1f;

			// Last argument true for LRU ordering
			private Map<String, Bitmap> mCache = Collections
					.synchronizedMap(new LinkedHashMap<String, Bitmap>(
							INITIAL_CAPACITY, LOAD_FACTOR, true));

			// current allocated size
			private long size = 0;
			// max memory in bytes
			private long limit = 1000000;
			private final int KB = 1024;

			public MemoryCache() {

				final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / KB);
				// Use 1/8th of the available memory for this memory cache.
				int cacheSize = maxMemory / 8;
				setLimit(cacheSize);
			}

			public void setLimit(long new_limit) {
				limit = new_limit;
			}

			public Bitmap get(String id) {
				try {
					if (!mCache.containsKey(id))
						return null;
					// NullPointerException sometimes happen here
					return mCache.get(id);
				} catch (NullPointerException ex) {
					ex.printStackTrace();
					return null;
				}
			}

			public void put(String id, Bitmap bitmap) {
				try {
					if (mCache.containsKey(id))
						size -= getSizeInBytes(mCache.get(id));
					mCache.put(id, bitmap);
					size += getSizeInBytes(bitmap);
					checkSize();
				} catch (Throwable th) {
					th.printStackTrace();
				}
			}

			private void checkSize() {

				if (size > limit) {
					Iterator<Entry<String, Bitmap>> iter = mCache.entrySet()
							.iterator();
					// least recently accessed item will be the first one
					// iterated
					while (iter.hasNext()) {
						Entry<String, Bitmap> entry = iter.next();
						size -= getSizeInBytes(entry.getValue());
						iter.remove();
						if (size <= limit)
							break;
					}

				}
			}

			public void clear() {
				try {
					// NullPointerException sometimes happen here
					// http://code.google.com/p/osmdroid/issues/detail?id=78
					mCache.clear();
					size = 0;
				} catch (NullPointerException ex) {
					ex.printStackTrace();
				}
			}

			long getSizeInBytes(Bitmap bitmap) {
				if (bitmap == null)
					return 0;
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		}

		public class BitmapCacheManager {
			private LruCache<Object, Bitmap> cache = null;
			private final int KB = 1024;

			public BitmapCacheManager() {

				// Get max available VM memory, exceeding this amount will throw
				// an
				// OutOfMemory exception. Stored in kilobytes as LruCache takes
				// an
				// int in its constructor.

				final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / KB);

				// Use 1/8th of the available memory for this memory cache.
				int cacheSize = maxMemory / 8;

				cache = new LruCache<Object, Bitmap>(cacheSize) {
					@Override
					protected int sizeOf(Object albumId, Bitmap bitmap) {
						return (bitmap.getRowBytes() * bitmap.getHeight() / KB);
					}

					protected void entryRemoved(boolean evicted, Object key,
							Bitmap oldValue, Bitmap newValue) {
						oldValue.recycle();
					}
				};
			}

			public void addBitmapToMemoryCache(Object key, Bitmap bitmap) {
				if (bitmap != null && key != null && cache.get(key) == null)
					cache.put(key, bitmap);
			}

			public Bitmap getBitmapFromMemCache(Object key) {
				return cache.get(key);
			}

			public void clear() {
				try {
					cache.evictAll();
				} catch (NullPointerException ex) {
					ex.printStackTrace();
				}
			}

		}

	}

}
