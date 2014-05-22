package com.gnod.geekr.tool.manager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.MessageDigestSpi;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.gnod.geekr.BuildConfig;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Build.VERSION_CODES;
import android.os.StatFs;
import android.support.v4.util.LruCache;
import android.util.Log;

public class ImageCache {

	private static final String TAG = ImageCache.class.getSimpleName();
	
	// Default memory cache size in KB
	private static final int DEFAULT_MEM_CACHE_SIZE = 5 * 1024; // 5MB
	
	// Default disk cache size in bytes
	private static final int DEFAULT_DISK_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
	
	private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
	private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
	private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

	public static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.PNG;
	public static final int DEFAULT_COMPRESS_QUALITY = 70;
	public static final int DISK_CACHE_INDEX = 0;

	private static ImageCache sImageCache;
	
	private Set<SoftReference<Bitmap>> mReusableBitmaps;

	private ImageCacheParams mCacheParams;
	private LruCache<String, BitmapDrawable> mMemCache;
	private final Object mDiskCacheLock = new Object();
	private DiskLruCache mDiskLruCache;
	private boolean mDiskCacheStarting = true;
	
	
	public static ImageCache getInstance(ImageCache.ImageCacheParams params) {
		
		if(sImageCache == null ) {
			sImageCache = new ImageCache(params);
		}
		return sImageCache;
	}
	
	private ImageCache(ImageCacheParams params) {
		init(params);
	}
	
	private void init(ImageCacheParams params) {
		mCacheParams = params;
		
		if (mCacheParams.memCacheEnabled) {
			if (Utils.hasHoneycomb()) {
				mReusableBitmaps = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
			}
			
			mMemCache = new LruCache<String, BitmapDrawable>(mCacheParams.memCacheSize) {

				@Override
				protected void entryRemoved(boolean evicted, String key,
						BitmapDrawable oldValue, BitmapDrawable newValue) {
					if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {
						((RecyclingBitmapDrawable)oldValue).setIsCached(false);
					} else {
						
						if (Utils.hasHoneycomb()) {
							mReusableBitmaps.add(new SoftReference<Bitmap>(oldValue.getBitmap()));
						}
					}
					
				}

				/**
                 * Measure item size in kilobytes rather than units which is more practical
                 * for a bitmap cache
                 */
				@Override
				protected int sizeOf(String key, BitmapDrawable value) {
					final int size = getBitmapSize(value) / 1024; // KB
					return size == 0 ? 1 : size;
				}
				
			};
		}
		
		if (params.initDiskCacheOnCreate) {
			initDiskCache();
		}
	}

	public void initDiskCache() {
		
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
				File diskCacheDir = mCacheParams.diskCacheDir;
				if (mCacheParams.diskCacheEnabled && diskCacheDir != null) {
					if (!diskCacheDir.exists()) {
						diskCacheDir.mkdirs();
					}
					if (getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize) {
						try {
							mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, mCacheParams.diskCacheSize);
						} catch (IOException e) {
							mCacheParams.diskCacheDir = null;
							Log.e(TAG, "initDiskCache -- " + e);
						}
					}
				}
			}
			mDiskCacheStarting  = false;
			mDiskCacheLock.notifyAll();
		}
	}
	
	public void addBitmapToCache(String data, BitmapDrawable value) {
		
		if (data == null || value == null) {
			return;
		}
		
		addToMemCache(data, value);
		
		synchronized (mDiskCacheLock) {
			
			if (mDiskLruCache != null) {
				final String key = hashKeyForDisk(data);
				OutputStream out = null;
				
				try {
					DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
					if (snapshot == null) {
						final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
						if (editor != null) {
							out = editor.newOutputStream(DISK_CACHE_INDEX);
							value.getBitmap().compress(mCacheParams.compressFormat, mCacheParams.compressQuality, out);
							editor.commit();
							out.close();
						}
					} else {
						snapshot.getInputStream(DISK_CACHE_INDEX).close();
					}
				} catch (IOException e) {
					Log.e(TAG, "addBitmapToCache -- " + e);
				} catch (Exception e) {
					Log.e(TAG, "addBitmapToCache -- " + e);
				} finally {
					try {
						if (out != null) {
							out.close();
						}
					} catch (IOException e) {
					}
				}
			}
		}
	}
	
	public void addToMemCache(String data, BitmapDrawable value) {
		if (mMemCache != null) {
			if (RecyclingBitmapDrawable.class.isInstance(value)) {
				((RecyclingBitmapDrawable)value).setIsCached(true);
			}
			mMemCache.put(data, value);
		}
	}
	
	public BitmapDrawable getBitmapFromMemCache(String data) {
		BitmapDrawable value = null;
		
		if (mMemCache != null) {
			value = mMemCache.get(data);
		}
		
		if (BuildConfig.DEBUG && value != null) {
			Log.d(TAG, "Memory cache hit");
		}
		
		return value;
	}
	
	public Bitmap getBitmapFromDiskCache(String data) {
		final String key = hashKeyForDisk(data);
		Bitmap bitmap = null;
		
		synchronized (mDiskCacheLock) {
			while (mDiskCacheStarting) {
				try {
					mDiskCacheLock.wait();
				} catch (InterruptedException e) {
				}
			}
			
			if (mDiskLruCache != null) {
				InputStream input = null;
				try {
					final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
					if (snapshot != null) {
						if (BuildConfig.DEBUG) {
							Log.d(TAG, "Disk cache hit");
						}
						input = snapshot.getInputStream(DISK_CACHE_INDEX);
						if (input != null) {
							FileDescriptor fd = ((FileInputStream)input).getFD();
							bitmap = ImageResizer.decodeSampledBitmapFromDescriptor(
									fd, Integer.MAX_VALUE, Integer.MAX_VALUE, this);
						}
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}
	
	protected Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
		Bitmap bitmap = null;
		
		if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
			final Iterator<SoftReference<Bitmap>> it = mReusableBitmaps.iterator();
			Bitmap item;
			
			while (it.hasNext()) {
				item = it.next().get();
				
				if (item != null && item.isMutable()) {
					if (canUseForInBitmap(item, options)) {
						bitmap = item;
						it.remove();
						break;
					} else {
						it.remove();
					}
				}
			}
		}
		
		return bitmap;
	}
	
	/**
     * Clears both the memory and disk cache associated with this ImageCache object. Note that
     * this includes disk access so this should not be executed on the main/UI thread.
     */
	public void clearCache() {
		if (mMemCache != null) {
			mMemCache.evictAll();
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "memory cache cleared");
			}
		}
		
		synchronized (mDiskCacheLock) {
			mDiskCacheStarting = true;
			if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
				try {
					mDiskLruCache.delete();
					if (BuildConfig.DEBUG) {
						Log.d(TAG, "disk cache cleared");
					}
				} catch (IOException e) {
					Log.e(TAG, "clearCache -- " + e);
				}
				mDiskLruCache = null;
				initDiskCache();
			}
		}
	}
	
	/**
     * Flushes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
	public void flush() {
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				try {
					mDiskLruCache.flush();
					if (BuildConfig.DEBUG) {
						Log.d(TAG, "Disk cache flushed");
					}
					
				} catch (IOException e) {
					Log.e(TAG, "flush -- " + e);
				}
			}
		}
	}
	
	/**
     * Closes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
	public void close() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Disk cache closed");
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "close - " + e);
                }
            }
        }
    }

	@TargetApi(VERSION_CODES.KITKAT)
	private static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {
		
		if (!Utils.hasKitKat()) {
			
			return candidate.getWidth() == targetOptions.outWidth
					&& candidate.getHeight() == targetOptions.outHeight
					&& targetOptions.inSampleSize == 1;
		}
		
		// From Android 4.4 (KitKat) onward we can re-use if the byte size of the new bitmap
        // is smaller than the reusable bitmap candidate allocation byte count.
		final int width = targetOptions.outWidth / targetOptions.inSampleSize;
		final int height = targetOptions.outHeight / targetOptions.inSampleSize;
		
		int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
		return byteCount <= candidate.getAllocationByteCount();
	}
	
	private static int getBytesPerPixel(Config config) {
		if (config == Config.ARGB_8888) {
			return 4;
		} else if (config == Config.ARGB_4444) {
			return 2;
		} else if (config == Config.RGB_565) {
			return 2;
		} else if (config == Config.ALPHA_8) {
			return 1;
		}
		
		return 1;
	}
	
	@TargetApi(VERSION_CODES.GINGERBREAD)
	public static long getUsableSpace(File path) {
		if (Utils.hasGingerbread()) {
			return path.getUsableSpace();
		}
		
		final StatFs stats = new StatFs(path.getPath());
		return (long)stats.getBlockSize() * (long)stats.getAvailableBlocks();
	}
	
	/**
	 * Get the size in bytes of a bitmap in a BitmapDrawable. Note that from Android 4.4 (KitKat)
     * onward this returns the allocated memory size of the bitmap which can be larger than the
     * actual bitmap data byte count (in the case it was re-used).
     *
	 */
	@TargetApi(VERSION_CODES.KITKAT)
	public static int getBitmapSize(BitmapDrawable value) {
		Bitmap bitmap = value.getBitmap();
		
		if (Utils.hasKitKat()) {
			return bitmap.getAllocationByteCount();
		}
		
		if (Utils.hasHoneycombMR1()) {
			return bitmap.getByteCount();
		}
		
		return bitmap.getRowBytes() * bitmap.getHeight();
	}
	
	public static String hashKeyForDisk(String key) {
		String cacheKey;
		
		try {
			final MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(key.getBytes());
			cacheKey = bytesToHexString(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		
		return cacheKey;
	}
	
	public static String bytesToHexString(byte[] bytes) {
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < bytes.length; i ++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		
		return sb.toString();
	}
	public static File getDiskCacheDir(Context context, String uniqueName) {
		
		final String cachePath = 
				Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
					!isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
						context.getCacheDir().getPath();
			
		return new File(cachePath + File.separator + uniqueName);
	}
	
	@TargetApi(VERSION_CODES.GINGERBREAD)
	public static boolean isExternalStorageRemovable() {
		if (Utils.hasGingerbread()) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}

	@TargetApi(VERSION_CODES.FROYO)
	public static File getExternalCacheDir(Context context) {
		if (Utils.hasFroyo()) {
			return context.getExternalCacheDir();
		}

		// Before Froyo we need to construct the external cache dir
		final String cacheDir = "Android/data/" + context.getPackageName() + "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
	}



	public static class ImageCacheParams {
		public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
		
		public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
		
		public boolean memCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
		public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
		public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;

		private File diskCacheDir;
		public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
		public int compressQuality = DEFAULT_COMPRESS_QUALITY;
		
		
		public ImageCacheParams(Context context, String diskCacheDirName) {
			diskCacheDir = getDiskCacheDir(context, diskCacheDirName);
		}
		
		/**
		 * Sets the memory cache size based on a percentage of the max available VM memory.
         * Eg. setting percent to 0.2 would set the memory cache to one fifth of the available
         * memory. Throws {@link IllegalArgumentException} if percent is < 0.01 or > .8.
         * memCacheSize is stored in kilobytes instead of bytes as this will eventually be passed
         * to construct a LruCache which takes an int in its constructor.
         * 
		 */
		public void setMemCacheSizePercent(float percent) {
			if (percent < 0.01f || percent > 0.8f) {
				throw new IllegalArgumentException("setMemCacheSizePercent - percent must be between 0.01 and 0.8 (inclusive)");
			}
			
			memCacheSize = Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
		}
	}
}
