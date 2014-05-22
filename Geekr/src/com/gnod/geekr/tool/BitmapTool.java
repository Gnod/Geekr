package com.gnod.geekr.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class BitmapTool {

	public static byte[] bitmapToBytes(Bitmap bitmap) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 100, output);
		return output.toByteArray();
	}
	
	public static Bitmap bytesToBitmap(byte[] b) {
		if( b == null) {
			return null;
		}
		
		final int length = b.length;
		if (length > 0) {
			return BitmapFactory.decodeByteArray(b, 0, length);
		}
		
		return null;
	}
	
	public static Bitmap zoomBitmap(Bitmap bitmap, int dstWidth, int dstHeight) {
		return Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
	}
	
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if( drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable)drawable).getBitmap();
		}
		
		final int width = drawable.getIntrinsicWidth();
		final int height = drawable.getIntrinsicHeight();
		
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ?
								Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap(width, height, config);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		
		return bitmap;
	}
	
	public static Drawable bitmapToDrawable(Resources res, Bitmap bitmap) {
		return new BitmapDrawable(res, bitmap);
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		
		final int width = options.outWidth;
		final int height = options.outHeight;
		int inSampleSize = 1;
		
		if ( width > reqWidth || height > reqHeight) {
			
			final int halfWidth = width / 2;
			final int halfHeight = height / 2;
			
			while ((halfWidth / inSampleSize) > reqWidth && (halfHeight / inSampleSize) > reqHeight) {
				inSampleSize *= 2;
			}
		}
		
		return inSampleSize;
	}
	
	/**
	 * calculate sample base on request size
	 * 
	 * @param type	0 for reqSize is the value of bitmap width, 1 for height
	 *
	 * */
	public static int calculateInSampleSize(int type, BitmapFactory.Options options, int reqSize) {
		
		final int size = type == 0 ? options.outWidth : options.outHeight;
		int inSampleSize = 1;
		
		if (size > reqSize) {
			final int halfSize = size / 2;
			
			while ((halfSize / inSampleSize) > reqSize) {
				inSampleSize *= 2;
			}
		}
		
		return inSampleSize;
	}
	
	public static Bitmap decodeScaleBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
		
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public static Bitmap decoBitmapScaleBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}
}
