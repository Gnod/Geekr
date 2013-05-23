package com.gnod.geekr.tool.manager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.gnod.geekr.tool.FileUtils;
import com.gnod.geekr.tool.ImageHelper;
import com.gnod.geekr.tool.StringUtils;

public class DrawableManager {
	
	public static final int TYPE_AVATAR_IMAGE = 0;
	public static final int TYPE_NORMAL_IMAGE = 1;
	
	private static HashMap<String, SoftReference<Bitmap>> bitmapCache;
	private static ExecutorService pool;
	
	public DrawableManager() {
		bitmapCache = new HashMap<String, SoftReference<Bitmap>>();
		pool = Executors.newFixedThreadPool(7);
	}
  
 
	public void loadAvatar(String url, ImageView imageView, boolean cache) { 
    	loadBitmap(url, imageView, null, 0, 0, cache, TYPE_AVATAR_IMAGE);
    }
	
    public void loadBitmap(String url, ImageView imageView, boolean cache) { 
    	loadBitmap(url, imageView, null, 0, 0, cache, TYPE_NORMAL_IMAGE);
    }
	
    public void loadBitmap(String url, ImageView imageView, Bitmap defaultBmp, boolean cache) {  
    	loadBitmap(url, imageView, defaultBmp, 0, 0, cache, TYPE_NORMAL_IMAGE);
    }
    
    public void loadBitmap(String url, ImageView imageView, Bitmap defaultBmp, int width, int height, boolean cache, int type) {  
        if(StringUtils.isNullOrEmpty(url))
        	return;
    	Bitmap bitmap = getBitmapFromCache(url);  
        imageView.setTag(url);
        if (bitmap != null) {  
            imageView.setImageBitmap(bitmap);
            return;
        } 
        
    	String filename = FileUtils.convertUrl(url);
		Bitmap bmp = null;
		if(type == TYPE_AVATAR_IMAGE) {
			bmp = ImageHelper.getBitmap(imageView.getContext(), filename);
		} else if(type == TYPE_NORMAL_IMAGE) {
			bmp = ImageHelper.getCacheImage(imageView.getContext(), filename);
		}
		
		if(bmp != null){
    		imageView.setImageBitmap(bmp);
    	}else{
    		if(defaultBmp != null)
    			imageView.setImageBitmap(defaultBmp);
    		fetchImage(url, filename, imageView, width, height, cache, type);
    	}
    }  
  
    public Bitmap getBitmapFromCache(String url) {  
        if (bitmapCache.containsKey(url)) { 
            return bitmapCache.get(url).get();  
        }
        return null;  
    }
    
    public void fetchImage(final String url, final String name, final ImageView imageView, final int width, final int height, final boolean cache, final int type) {  
        final Handler handler = new Handler() {  
            public void handleMessage(Message msg) {
            	String tagUrl = (String) imageView.getTag();
            	
            	//why: 由于在listview中对于item会回收利用，所以这里做一步判断
            	//确保所抓取的图片显示的目标View在获取图片后未被回收（即对应
            	//的View仍然是处于可见的状态）
            	if(tagUrl != null && tagUrl.equals(url)) {
            		if (msg.obj != null) {
            			Bitmap bitmap = (Bitmap)msg.obj;
                        imageView.setImageBitmap(bitmap);  
                        bitmapCache.put(url, new SoftReference<Bitmap>(bitmap));
                        if(cache) {
    	                    try {
    	                    	if(type == TYPE_AVATAR_IMAGE) {
    	                    		ImageHelper.saveImage(imageView.getContext(), name, (Bitmap) msg.obj, 100);
        	                	} else if(type == TYPE_NORMAL_IMAGE) {
    	                			ImageHelper.saveCacheImage(imageView.getContext(), name, (Bitmap) msg.obj, 100);
    	                		}
    						} catch (IOException e) {
    							Log.e("fetch", e.getMessage());
    						}
                        }
                    }
            	} 
            }  
        };  
  
        pool.execute(new Runnable() {   
            public void run() {  
                Message message = Message.obtain();  
                message.obj = downloadBitmap(url, width, height);  
                handler.sendMessage(message);  
            }  
        });  
    }
    
    
    public void fetchImage(final String url, final ImageView imageView, final int width, final int height, final FetchImageCompleteListener fetchListener) {  
    	if(StringUtils.isNullOrEmpty(url)){
    		return;
    	}
    	imageView.setTag(url);
    	Bitmap bitmap = getBitmapFromCache(url);  
        if (bitmap != null) {
            bitmapCache.put(url, new SoftReference<Bitmap>(bitmap));
        	if(fetchListener != null) 
				fetchListener.fetchComplete(bitmap);
        	return;
        }
    	final Handler handler = new Handler() {  
            public void handleMessage(Message msg) {
            	String tagUrl = (String) imageView.getTag();
            	if(tagUrl != null && tagUrl.equals(url)) {
            		if (msg.obj != null) {
            			Bitmap bitmap = (Bitmap)msg.obj;
            			if(fetchListener != null) 
            				fetchListener.fetchComplete(bitmap);
                    }
            	} 
            }  
        };  
  
        pool.execute(new Runnable() {   
            public void run() {  
                Message message = Message.obtain();  
                message.obj = downloadBitmap(url, width, height);  
                handler.sendMessage(message);  
            }  
        });  
    }
    
    private Bitmap downloadBitmap(String url, int width, int height) {   
        Bitmap bitmap = null;
        try {
        	DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);
            
            InputStream inputStream =  response.getEntity().getContent();
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if(bitmap != null ) {
            	int sourceHeight = bitmap.getHeight();
            	int sourceWidth = bitmap.getWidth();
            	
            	if(width > 0 && height > 0) {
	            	if(sourceHeight < sourceWidth) {
	            		int newHeight = width * sourceHeight / sourceWidth;
	            		bitmap = Bitmap.createScaledBitmap(bitmap, width, newHeight, true);            		
	            	} else if(sourceHeight > sourceWidth){
	            		int newWidth = height * sourceWidth / sourceHeight;
	            		bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, height, true);
	            	}
            	}
				
            }
		} catch (Exception e) {
			Log.e("error", e.getMessage());
			return null;
		}
		
        return bitmap;  
    }
    
    public interface FetchImageCompleteListener{
    	public void fetchComplete(Bitmap d);
    }
}
