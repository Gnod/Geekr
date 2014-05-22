package com.gnod.geekr.tool.manager;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class RecyclingBitmapDrawable extends BitmapDrawable {

	private boolean mHasBeenDisplayed = false;
	private int mDisplayRefCount = 0;
	private int mCacheRefCount = 0;

	public RecyclingBitmapDrawable(Resources res, Bitmap bitmap) {
		super(res, bitmap);
	}
	
	public void setIsDisplayed(boolean isDisplayed) {
		
		synchronized (this) {
			if(isDisplayed) {
				mDisplayRefCount ++;
				mHasBeenDisplayed = true;
			} else {
				mDisplayRefCount --;
			}
		}
		
		checkState();
	}
	
	public void setIsCached(boolean isCached) {
		synchronized (this) {
			if (isCached) {
				mCacheRefCount  ++;
			} else {
				mCacheRefCount --;
			}
		}
		
		checkState();
	}

	private synchronized void checkState() {
		
		if (mCacheRefCount <= 0 && mDisplayRefCount <= 0 && mHasBeenDisplayed && hasValidBitmap()) {
			
			getBitmap().recycle();
		}
	}

	private synchronized boolean hasValidBitmap() {
		Bitmap bitmap = getBitmap();
		return bitmap != null && !bitmap.isRecycled();
	}
}
