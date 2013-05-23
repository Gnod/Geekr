package com.buuuk.android.gallery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import com.buuuk.android.ui.touch.TouchActivity;
import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.tool.DateUtils;
import com.gnod.geekr.tool.manager.DrawableManager;
import com.gnod.geekr.tool.manager.DrawableManager.FetchImageCompleteListener;
import com.gnod.geekr.tool.ToastHelper;


@SuppressLint("SdCardPath")
public class ImageViewFlipper extends TouchActivity {
	
	private static final int EXIT = 0;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private static final String DIRECTORY = "/sdcard/";
	private static final String DATA_DIRECTORY = "/sdcard/.ImageViewFlipper/";
	private static final String DATA_FILE = "/sdcard/.ImageViewFlipper/imagelist.dat";
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;
	private ViewFlipper viewFlipper;
	private int currentView = 0;
	List<String> ImageList = new ArrayList();
	private int currentIndex = 0;
	private int maxIndex = 0;
	private ImageView currentImageView = null;
	
	private float mMinZoomScale=1;
	
	private DrawableManager drawableManager = new DrawableManager();
	FileOutputStream output = null;
	OutputStreamWriter writer = null;
	
	private LinearLayout layoutProgress;	
	
	private Boolean isGallery = true;
	private String singleSrc = "";
	private View btnDownload;
	
	protected void onSaveInstanceState(Bundle icicle) {
		super.onSaveInstanceState(icicle);
		icicle.putSerializable("currentGalleryIndex",currentIndex);		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		setContentView(R.layout.activity_picture_detail);
		final ImageView iv = (ImageView) findViewById(R.id.zero);
		
		singleSrc = this.getIntent().getStringExtra("src");
		if(singleSrc == null && ImageList.size() != 0) {
			isGallery = true;
			currentIndex = this.getIntent().getIntExtra("index", 0);
		}
		else { 
			isGallery = false;
		}
		
		if(savedInstanceState != null)
		{
			currentIndex = savedInstanceState.getInt("currentGalleryIndex", currentIndex);
		}
		
		init();
		
		maxIndex = ImageList.size() - 1;
		

		viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);

		slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
		slideLeftOut = AnimationUtils
				.loadAnimation(this, R.anim.slide_left_out);
		slideRightIn = AnimationUtils
				.loadAnimation(this, R.anim.slide_right_in);
		slideRightOut = AnimationUtils.loadAnimation(this,
				R.anim.slide_right_out);

		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_out));
		
		
		String src = "";
		if(isGallery)
			src = ImageList.get(currentIndex);
		else
			src = singleSrc;
		currentImageView = iv;
		AppConfig config = (AppConfig)getApplication();
		LayoutParams params = iv.getLayoutParams();
		
		gestureDetector = new GestureDetector(new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};
		
		btnDownload = findViewById(R.id.btn_pic_download);
		btnDownload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onSaveImage();
			}
		});
		
		drawableManager.fetchImage(src, iv, params.width, params.height, new FetchImageCompleteListener() {
			public void fetchComplete(Bitmap d) {
				layoutProgress.setVisibility(View.GONE);
				iv.setImageBitmap(d);
				resetImage(iv,iv.getDrawable());
				btnDownload.setVisibility(View.VISIBLE);
			}
			
		});
		
	}
	
	private void onSaveImage()
	{		
				
		BitmapDrawable drawable = (BitmapDrawable) currentImageView.getDrawable();
		if(drawable == null)
			return;
		Bitmap bmp = drawable.getBitmap();
		
		AppConfig config = (AppConfig) getApplication();
		String dir = config.getImgPath();
		File myDir = new File(dir + File.separator);
		if(!myDir.exists()) {
			myDir.mkdirs();
		}

		Date date = new Date();
		String sigDate = DateUtils.getDateTag(date);
		
		String fname = sigDate + ".jpg";
		File file = new File(myDir, fname);				
		
		try {
		       FileOutputStream out = new FileOutputStream(file);
		       bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
		} catch (Exception e) {
		       ToastHelper.show("图片保存失败");
		}
	    ToastHelper.show("图片已保存至" + file.toString(), 1, true);
	}
	
	/**
	 * initControl之前必须确保存intent已经解析过了
	 */
	private void init()
	{
		layoutProgress = (LinearLayout) findViewById(R.id.picture_detail_progress_layout);
		layoutProgress.setVisibility(View.VISIBLE);
		
	}
	
	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onDoubleTap(final MotionEvent e){
			
			
	    	ImageView view = (ImageView)findViewById(R.id.zero);
			switch(currentView){
			case 0: view = (ImageView)findViewById(R.id.zero); break;
			case 1: view = (ImageView)findViewById(R.id.view_image_one); break;
			case 2:view = (ImageView)findViewById(R.id.two); break;				
			}
			
			if(view.getDrawable() == null)
				return true;
			resetImage(view,view.getDrawable());
			return true;
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return super.onSingleTapUp(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				if(!isGallery)
					return false;
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					viewFlipper.setInAnimation(slideLeftIn);
					viewFlipper.setOutAnimation(slideLeftOut);

					if (currentIndex == maxIndex) {
						currentIndex = 0;
					} else {
						currentIndex = currentIndex + 1;
					}
					
					final ImageView iv;				
					
					if (currentView == 0) {
						currentView = 1;
						iv = (ImageView) findViewById(R.id.view_image_one);
					} else if (currentView == 1) {
						currentView = 2;
						 iv = (ImageView) findViewById(R.id.two);
					} else {
						currentView = 0;
						iv = (ImageView) findViewById(R.id.zero);
					}					
					iv.setImageDrawable(null);
					currentImageView = iv;
					layoutProgress.setVisibility(View.VISIBLE);
					drawableManager.fetchImage(ImageList.get(currentIndex), iv, 0, 0, new FetchImageCompleteListener() {
						@Override
						public void fetchComplete(Bitmap d) {
							layoutProgress.setVisibility(View.GONE);
							iv.setImageBitmap(d);
							System.gc();
							resetImage(iv,iv.getDrawable());	
							Log.v("ImageViewFlipper", "Current View: " + currentView);
						}
					});		
					viewFlipper.showNext();
					
					// 预加载后一张
					if(currentIndex < maxIndex)
					{
						drawableManager.fetchImage(ImageList.get(currentIndex + 1), iv, 0, 0, null);
					}
					return true;
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					viewFlipper.setInAnimation(slideRightIn);
					viewFlipper.setOutAnimation(slideRightOut);
					
					
					
					if (currentIndex == 0) {
						currentIndex = maxIndex;
					} else {
						currentIndex = currentIndex - 1;
					}
					
					final ImageView iv;

					if (currentView == 0) {
						currentView = 2;
						iv = (ImageView) findViewById(R.id.two);						
					} else if (currentView == 2) {
						currentView = 1;
						iv = (ImageView) findViewById(R.id.view_image_one);						
					} else {
						currentView = 0;
						iv = (ImageView) findViewById(R.id.zero);						
					}
					currentImageView = iv;
					iv.setImageDrawable(null);
					layoutProgress.setVisibility(View.VISIBLE);
					drawableManager.fetchImage(ImageList.get(currentIndex), iv, 0, 0, new FetchImageCompleteListener() {
							
						@Override
						public void fetchComplete(Bitmap d) {
							layoutProgress.setVisibility(View.GONE);
							iv.setImageBitmap(d);
							System.gc();
							
							resetImage(iv,iv.getDrawable());	
							Log.v("ImageViewFlipper", "Current View: " + currentView);
						}
					});
					viewFlipper.showPrevious();
					
					// 预加载前一张
					if(currentIndex > 0)
					{
						drawableManager.fetchImage(ImageList.get(currentIndex - 1), iv, 0, 0, null);
					}
					return true;
				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}
		
	}
	
	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public void resetImage(ImageView iv, Drawable draw) {
		try {
			Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
			int rotation = display.getRotation();
			
			int orientation = 0;
			if( rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
				orientation = 0;
			else
				orientation = 1;
			
	        matrix = new Matrix();
	        matrix.setTranslate(1f, 1f);
	        float scale = 1;
	        
	        int controlHeight = viewFlipper.getHeight();
	        
	        mMinZoomScale = 1;
	        if( orientation==0 ) {
	        	
	        	scale = (float)(getWindowManager().getDefaultDisplay().getWidth() + 2)/(float)draw.getIntrinsicWidth();        	
	        	mMinZoomScale = scale;
	        	matrix.postScale(scale,scale);
	        
	        	iv.setImageMatrix(matrix);
	        }else if( orientation==1){
	        	scale = (float)controlHeight/(float)draw.getIntrinsicHeight();
	        	mMinZoomScale = scale;
	        	matrix.postScale(scale,scale);
	        
	        	iv.setImageMatrix(matrix);
	        }
	        	
	        	
	        float transX = -1;
	        
	        float transY = (float)-1;
	        float y1 = (float)controlHeight/2;
	        float y2 = (float)(draw.getIntrinsicHeight()*scale)/2;
	        
	        if(y1 > y2)  
	        	transY = y1 - y2;
	        
	        matrix.postTranslate(transX,transY);
	        iv.setImageMatrix(matrix);
		} catch (Exception e) {
		}
	
	}
	
	
	@Override
	public float getMinZoomScale(){
		return mMinZoomScale;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent rawEvent) {
		if(gestureDetector.onTouchEvent(rawEvent))
			return true;
			
		
		ImageView view = (ImageView)findViewById(R.id.zero);
		switch(currentView){
		case 0: view = (ImageView)findViewById(R.id.zero); break;
		case 1: view = (ImageView)findViewById(R.id.view_image_one); break;
		case 2:view = (ImageView)findViewById(R.id.two); break;				
		}	
		onTouchEvented(view, rawEvent);
		
		return true;
	}
	
 
	public void quit() {
		
		File settings = new File(DATA_FILE);
		settings.delete();
		finish();
		int pid = android.os.Process.myPid();
		android.os.Process.killProcess(pid);
		System.exit(0);
	}


}