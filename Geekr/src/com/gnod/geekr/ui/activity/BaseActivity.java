package com.gnod.geekr.ui.activity;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.actionbarsherlock.app.SherlockActivity;
import com.gnod.geekr.app.AppManager;

public abstract class BaseActivity extends SherlockActivity {

	private boolean isEnableGesture = true;
	private GestureDetector detector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		//add activity to atctivity's stack
		AppManager.getInstance().addActivity(this);
		detector = new GestureDetector(this, gestureListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//remove activity from activity's stack
		AppManager.getInstance().finishActivity(this);
	}
	
	public void enableFling(boolean checked){
		isEnableGesture = checked;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(isEnableGesture && detector.onTouchEvent(ev)){
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}
	
	public boolean flingRight() {
		finish();
		return true;
	}
	
	public boolean flingLeft() {
		return false;
	}

	private OnGestureListener gestureListener = new OnGestureListener() {
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}
		@Override
		public void onShowPress(MotionEvent e) {
		}
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			return false;
		}
		@Override
		public void onLongPress(MotionEvent e) {
		}
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if(e2.getX() - e1.getX() > 150 && Math.abs(e2.getY() - e1.getY()) < 100){
				return flingRight();
			} else if(e1.getX() - e2.getX()> 150&& Math.abs(e2.getY() - e1.getY()) < 100) {
				return flingLeft();
			}
			return false;
		}
		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}
	};

	
}
