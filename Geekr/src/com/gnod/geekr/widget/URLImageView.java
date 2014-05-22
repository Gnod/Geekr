package com.gnod.geekr.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.buuuk.android.gallery.ImageViewFlipper;
import com.gnod.geekr.app.AppConfig;

public class URLImageView extends ImageView{

	public URLImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public URLImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public URLImageView(Context context) {
		super(context);
	}

	public void setURL(final String url){
		setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), ImageViewFlipper.class);
				intent.putExtra("src", url);
				getContext().startActivity(intent);
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setAlpha(200);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			setAlpha(255);
			break;
		}
		return super.onTouchEvent(event);
	}
}
