package com.gnod.geekr.tool.manager;

import java.lang.ref.WeakReference;

import android.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.widget.ImageView;

import com.gnod.geekr.BuildConfig;

public abstract class ImageWorker {

	private static final int MESSAGE_CLEAR = 0;
	private static final int MESSAGE_INIT_DISK_CACHE = 1;
	private static final int MESSAGE_FLUSH = 2;
	private static final int MESSAGE_CLOSE = 3;
	
	private static final int FADE_IN_TIME = 200;
	private static final String TAG = "ImageWorker";

	protected Resources mResources;
	private ImageCache mImageCache;
	private Bitmap mDefaultBitmap;
	private boolean mFadeInBitmap = false;
	private boolean mExitTasksEarly = false;
	private final Object mPauseWorkLock = new Object();
	private boolean mPauseWork = false;
	
	protected ImageWorker(Context context, ImageCache imageCache) {
		mResources = context.getResources();
		addImageCache(imageCache);
	}
	
	public void addImageCache(ImageCache imageCache) {
		mImageCache = imageCache;
		initDiskCache();
	}
	
	public void loadImage(Object data, ImageView imageView) {
		loadImage(data, imageView, mDefaultBitmap);
	}
	
	public void loadImage(Object data, ImageView imageView, int defaultResId) {
		BitmapDrawable value = null;
		if (mImageCache != null) {
			value = mImageCache.getBitmapFromMemCache(String.valueOf(defaultResId));
		}
		if (value == null) {
			value = new BitmapDrawable(mResources, BitmapFactory.decodeResource(mResources, defaultResId));
			mImageCache.addToMemCache(String.valueOf(defaultResId), value);
		}
		loadImage(data, imageView, value.getBitmap());
	}
	
	public void loadImage(Object data, ImageView imageView, Bitmap defaultBitmap) {
		if (data == null) {
			return;
		}
		
		BitmapDrawable value = null;
		
		if (mImageCache != null) {
			value = mImageCache.getBitmapFromMemCache(String.valueOf(data));
		}
		
		if (value != null) {
			if (BuildConfig.DEBUG) {
	            Log.d(TAG, "processBitmap loadImage memory cache hit -- " + data);
	        }
			imageView.setImageDrawable(value);
		} else if (cancelPotentailWork(data, imageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(data, imageView);
			final AsyncDrawable drawable = new AsyncDrawable(mResources, defaultBitmap, task);
			imageView.setImageDrawable(drawable);
			
			// NOTE: This uses a custom version of AsyncTask that has been pulled from the
            // framework and slightly modified. Refer to the docs at the top of the class
            // for more info on what was changed.
			task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR);
		}
	}
	
	public void setDefaultImage(Bitmap bitmap) {
		mDefaultBitmap = bitmap;
	}
	
	public void setDefaultImage(int resId) {
		mDefaultBitmap = BitmapFactory.decodeResource(mResources, resId);
	}
	
	protected ImageCache getImageCache() {
		return mImageCache;
	}
	
	public void setImageFadeIn(boolean fadeIn) {
		mFadeInBitmap  = fadeIn;
	}
	
	public void setExitTasksEarly(boolean exitTasksEarly) {
		mExitTasksEarly  = exitTasksEarly;
		setPauseWork(false);
	}
	
	public void setPauseWork(boolean pauseWork) {
		synchronized (mPauseWorkLock) {
			mPauseWork  = pauseWork;
			if (!mPauseWork) {
				mPauseWorkLock.notifyAll();
			}
		}
	}
	
	public static void cancelWork(ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkingTask(imageView);
		if (bitmapWorkerTask != null) {
			bitmapWorkerTask.cancel(true);
		}
		
	}

	public static boolean cancelPotentailWork(Object data, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkingTask = getBitmapWorkingTask(imageView);
		
		if(bitmapWorkingTask != null ) {
			final Object bitmapData = bitmapWorkingTask.data;
			if( bitmapData == null || !bitmapData.equals(data)) {
				bitmapWorkingTask.cancel(true);
			} else {
				return false;
			}
		}
		return true;
	}
	
	private static BitmapWorkerTask getBitmapWorkingTask(ImageView imageView) {
		if( imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if(drawable instanceof AsyncDrawable) {
				return ((AsyncDrawable)drawable).getBitmapWorkingTask();
			}
		}
		
		return null;
	}
	
	private void initDiskCache() {
		new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
	}
	
	public void clearCache() {
		new CacheAsyncTask().execute(MESSAGE_CLEAR);
	}
	
	public void closeCache() {
		new CacheAsyncTask().execute(MESSAGE_CLOSE);
	}
	
	public void flushCache() {
		new CacheAsyncTask().execute(MESSAGE_FLUSH);
	}
	

	protected void closeCacheInternal() {
		if (mImageCache != null) {
			mImageCache.close();
		}		
	}

	protected void flushCacheInternal() {
		if (mImageCache != null) {
			mImageCache.flush();
		}
	}

	protected void initDiskCacheInternal() {
		if (mImageCache != null) {
			mImageCache.initDiskCache();
		}
	}

	protected void clearCacheInternal() {
		if (mImageCache != null) {
			mImageCache.clearCache();
		}
	}
	
	/**
     * Subclasses should override this to define any processing or work that must happen to produce
     * the final bitmap. This will be executed in a background thread and be long running. For
     * example, you could resize a large bitmap here, or pull down an image from the network.
     *
     * @param data The data to identify which image to process, as provided by
     *            {@link ImageWorker#loadImage(Object, android.widget.ImageView)}
     * @return The processed bitmap
     */
    protected abstract Bitmap processBitmap(Object data);
    
    
	private class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
	
		public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask task) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<ImageWorker.BitmapWorkerTask>(task);
		}
		
		public BitmapWorkerTask getBitmapWorkingTask() {
			return bitmapWorkerTaskReference.get();
		}
	}
	
	protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			int type = (Integer) params[0];
			switch (type) {
			case MESSAGE_CLEAR:
				clearCacheInternal();
				break;
			case MESSAGE_INIT_DISK_CACHE:
				initDiskCacheInternal();
				break;
			case MESSAGE_FLUSH:
				flushCacheInternal();
				break;
			case MESSAGE_CLOSE:
				closeCacheInternal();
				break;
			}
			
			return null;
		}
		
	}
	
	private class BitmapWorkerTask extends AsyncTask<Object, Void, BitmapDrawable> {

		private final WeakReference<ImageView> imageViewReference;
		private Object data;
		
		public BitmapWorkerTask(Object data, ImageView imageView) {
			this.data = data;
			imageViewReference = new WeakReference<ImageView>(imageView);
		}
		
		@Override
		protected BitmapDrawable doInBackground(Object... params) {
			
			final String dataString = String.valueOf(data);
			Bitmap bitmap = null;
			BitmapDrawable drawable = null;
			
			synchronized (mPauseWorkLock) {
				while (mPauseWork && !isCancelled()) {
					try {
						mPauseWorkLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}
			
			// If the image cache is available and this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
			if (mImageCache != null && !isCancelled() && getAttachedImageView() != null 
					&& !mExitTasksEarly) {
				bitmap = processBitmap(data);
			}
			
			// If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
			if (bitmap != null) {
				if (Utils.hasHoneycomb()) {
					drawable = new BitmapDrawable(mResources, bitmap);
				} else {
					drawable = new RecyclingBitmapDrawable(mResources, bitmap);
				}
				
				if (mImageCache != null) {
					mImageCache.addBitmapToCache(dataString, drawable);
				}
			}
			
			return drawable;
		}

		@Override
		protected void onPostExecute(BitmapDrawable bitmapDrawable) {
			if(isCancelled()) {
				bitmapDrawable = null;
			}
			
			final ImageView imageView = getAttachedImageView();
			if(imageViewReference != null && imageView != null) {
				setImageDrawable(imageView, bitmapDrawable);
			}
		}
		
		
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			synchronized (mPauseWorkLock) {
				mPauseWorkLock.notifyAll();
			}
		}

		private ImageView getAttachedImageView() {
			final ImageView imageView = imageViewReference.get();
			final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkingTask(imageView);
			
			if (this == bitmapWorkerTask) {
				return imageView;
			}
			
			return null;
		}
	}

	public void setImageDrawable(ImageView imageView,
			BitmapDrawable bitmapDrawable) {
		
		if (mFadeInBitmap && mDefaultBitmap != null) {
			final TransitionDrawable td = new TransitionDrawable(new Drawable[] {
				new ColorDrawable(android.R.color.transparent),
				bitmapDrawable
			});
			imageView.setBackgroundDrawable(new BitmapDrawable(mResources, mDefaultBitmap));
			imageView.setImageDrawable(td);
			td.startTransition(FADE_IN_TIME );
		} else {
			imageView.setImageDrawable(bitmapDrawable);
		}
	}

}
