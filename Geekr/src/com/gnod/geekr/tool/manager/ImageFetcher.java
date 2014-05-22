package com.gnod.geekr.tool.manager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.gnod.geekr.BuildConfig;

public class ImageFetcher extends ImageResizer {

	private static final String TAG = ImageFetcher.class.getSimpleName();
	private static final long HTTP_CACHE_SIZE = 10 * 1024 * 1024; //10MB
	private static final int IO_BUFFER_SIZE = 8 * 1024;
	private static final String HTTP_CACHE_DIR = "http";
	private static final int DISK_CACHE_INDEX = 0;
	
	private File mHttpCacheDir;
	private final Object mHttpDiskCacheLock = new Object();
	private DiskLruCache mHttpDiskCache;
	private boolean mHttpDiskCacheStarting = true;

	public ImageFetcher(Context context, ImageCache imageCache, int imageSize) {
		this(context, imageCache, imageSize, imageSize);
	}
	
	public ImageFetcher(Context context, ImageCache imageCache, int imageWidth,
			int imageHeight) {
		super(context, imageCache, imageWidth, imageHeight);
		init(context);
	}

	private void init(Context context) {
		checkConnection(context);
		mHttpCacheDir = ImageCache.getDiskCacheDir(context, HTTP_CACHE_DIR);
	}

	@Override
	protected void initDiskCacheInternal() {
		super.initDiskCacheInternal();
		initHttpDiskCache();
	}

	private void initHttpDiskCache() {
		if (!mHttpCacheDir.exists()) {
			mHttpCacheDir.mkdirs();
		}
		
		synchronized (mHttpDiskCacheLock) {
			if (ImageCache.getUsableSpace(mHttpCacheDir) > HTTP_CACHE_SIZE) {
				try {
					mHttpDiskCache = DiskLruCache.open(mHttpCacheDir, 1, 1, HTTP_CACHE_SIZE);
				} catch (IOException e) {
					mHttpDiskCache = null;
				}
			}
			mHttpDiskCacheStarting = false;
			mHttpDiskCacheLock.notifyAll();
		}
	}
	
	
	
	@Override
	protected Bitmap processBitmap(Object data) {
		return processBitmap(String.valueOf(data));
	}

	private Bitmap processBitmap(String data) {
		if (BuildConfig.DEBUG) {
            Log.d(TAG, "processBitmap - " + data);
        }
		
		final String key = ImageCache.hashKeyForDisk(data);
		FileDescriptor fd = null;
		FileInputStream fileInputStream = null;
		DiskLruCache.Snapshot snapshot;
		
		synchronized (mHttpDiskCacheLock) {
			while (mHttpDiskCacheStarting) {
				try {
					mHttpDiskCacheLock.wait();
				} catch (InterruptedException e) {
				}
			}
			
			if (mHttpDiskCache != null) {
				try {
					snapshot = mHttpDiskCache.get(key);
					if (snapshot == null) {
						if (BuildConfig.DEBUG) {
                            Log.d(TAG, "processBitmap, not found in http cache, downloading...");
                        }
						DiskLruCache.Editor editor = mHttpDiskCache.edit(key);
						if (editor != null) {
							if (downloadUrlToStream(data, editor.newOutputStream(DISK_CACHE_INDEX))) {
								editor.commit();
							} else {
								editor.abort();
							}
						}
						snapshot = mHttpDiskCache.get(key);
					} else {
						if (BuildConfig.DEBUG) {
                            Log.d(TAG, "processBitmap, found in http disk cache");
                        }
					}
					
					if (snapshot != null) {
						fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
						fd = fileInputStream.getFD();
					}
					
				} catch (IOException e) {
                    Log.e(TAG, "processBitmap - " + e);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "processBitmap - " + e);
                } finally {
                    if (fd == null && fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {}
                    }
                }
			}
		}
		
		Bitmap bitmap = null;
		if (fd != null) {
			bitmap = decodeSampledBitmapFromDescriptor(fd, mImageWidth, mImageHeight, getImageCache());
		}
		
		if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {}
        }
		
		return bitmap;
	}
	private boolean downloadUrlToStream(String data,
			OutputStream newOutputStream) {
		disableConnectionReuseIfNecessary();
		HttpURLConnection urlConnection = null;
		BufferedInputStream input = null;
		BufferedOutputStream output = null;
		
		try {
			final URL url = new URL(data);
//			DefaultHttpClient httpClient = new DefaultHttpClient();
//	        HttpGet request = new HttpGet(url);
//	        HttpResponse response = httpClient.execute(request);
//	        InputStream inputStream =  response.getEntity().getContent();
//	        bitmap = BitmapFactory.decodeStream(inputStream);
//	        inputStream.close();
			urlConnection = (HttpURLConnection) url.openConnection();
			input = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
			output = new BufferedOutputStream(newOutputStream, IO_BUFFER_SIZE);
			
			int b;
			while ((b = input.read()) != -1) {
				output.write(b);
			}
			return true;
		} catch (final IOException e) {
			Log.e(TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (final IOException e) {}
        }
		
		return false;
	}
	
	/**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
	public static void disableConnectionReuseIfNecessary() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	@Override
	protected void clearCacheInternal() {
        super.clearCacheInternal();
        synchronized (mHttpDiskCacheLock) {
            if (mHttpDiskCache != null && !mHttpDiskCache.isClosed()) {
                try {
                    mHttpDiskCache.delete();
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "HTTP cache cleared");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "clearCacheInternal - " + e);
                }
                mHttpDiskCache = null;
                mHttpDiskCacheStarting = true;
                initHttpDiskCache();
            }
        }
    }

    @Override
    protected void flushCacheInternal() {
        super.flushCacheInternal();
        synchronized (mHttpDiskCacheLock) {
            if (mHttpDiskCache != null) {
                try {
                    mHttpDiskCache.flush();
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "HTTP cache flushed");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "flush - " + e);
                }
            }
        }
    }

    @Override
    protected void closeCacheInternal() {
        super.closeCacheInternal();
        synchronized (mHttpDiskCacheLock) {
            if (mHttpDiskCache != null) {
                try {
                    if (!mHttpDiskCache.isClosed()) {
                        mHttpDiskCache.close();
                        mHttpDiskCache = null;
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "HTTP cache closed");
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "closeCacheInternal - " + e);
                }
            }
        }
    }

	private void checkConnection(Context context) {
		final ConnectivityManager mgr = 
				(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo info = mgr.getActiveNetworkInfo();
		if (info == null || !info.isConnectedOrConnecting()) {
			Log.e(TAG, "checkConnection - no connection found");
		}
	}

}
