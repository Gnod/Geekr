package com.gnod.geekr.tool.manager;

import java.io.FileDescriptor;

import com.gnod.geekr.BuildConfig;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.util.Log;

public class ImageResizer extends ImageWorker {
	private static final String TAG = ImageResizer.class.getSimpleName();
	protected int mImageWidth;
	protected int mImageHeight;

	public ImageResizer(Context context, ImageCache imageCache, int imageSize) {
		this(context, imageCache, imageSize, imageSize);
	}

	public ImageResizer(Context context, ImageCache imageCache, int imageWidth,
			int imageHeight) {
		super(context, imageCache);
		setImageSize(imageWidth, imageHeight);
	}

	private void setImageSize(int imageWidth, int imageHeight) {
		mImageWidth = imageWidth;
		mImageHeight = imageHeight;
	}

	@Override
	protected Bitmap processBitmap(Object data) {
		return processBitmap(Integer.parseInt(String.valueOf(data)));
	}

	private Bitmap processBitmap(int resId) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "processBitmap - " + resId);
		}
		return decodeSampledBitmapFromResource(mResources, resId, mImageWidth,
				mImageHeight, getImageCache());
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight, ImageCache imageCache) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		// END_INCLUDE (read_bitmap_dimensions)

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (Utils.hasHoneycomb()) {
			addInBitmapOptions(options, imageCache);
		}

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fd,
			int reqWidth, int reqHeight, ImageCache cache) {

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fd, null, options);

		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		options.inJustDecodeBounds = false;
		if (Utils.hasHoneycomb()) {
			addInBitmapOptions(options, cache);
		}
		return BitmapFactory.decodeFileDescriptor(fd, null, options);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void addInBitmapOptions(BitmapFactory.Options options,
			ImageCache cache) {

		// inBitmap only works with mutable bitmaps so force the decoder to
		// return mutable bitmaps.
		options.inMutable = true;

		if (cache != null) {
			Bitmap inBitmap = cache.getBitmapFromReusableSet(options);
			if (inBitmap != null) {
				options.inBitmap = inBitmap;
			}
		}
	}

	/**
	 * Calculate an inSampleSize for use in a
	 * {@link android.graphics.BitmapFactory.Options} object when decoding
	 * bitmaps using the decode* methods from
	 * {@link android.graphics.BitmapFactory}. This implementation calculates
	 * the closest inSampleSize that is a power of 2 and will result in the
	 * final decoded bitmap having a width and height equal to or larger than
	 * the requested width and height.
	 * 
	 * @param options
	 *            An options object with out* params already populated (run
	 *            through a decode* method with inJustDecodeBounds==true
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @return The value to be used for inSampleSize
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}

			// This offers some additional logic in case the image has a strange
			// aspect ratio. For example, a panorama may have a much larger
			// width than height. In these cases the total pixels might still
			// end up being too large to fit comfortably in memory, so we should
			// be more aggressive with sample down the image (=larger
			// inSampleSize).

			long totalPixels = width * height / inSampleSize;

			// Anything more than 2x the requested pixels we'll sample down
			// further
			final long totalReqPixelsCap = reqWidth * reqHeight * 2;

			while (totalPixels > totalReqPixelsCap) {
				inSampleSize *= 2;
				totalPixels /= 2;
			}
		}
		return inSampleSize;
	}

}
