package com.gnod.geekr.widget;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;

public class NotifiedToast extends Toast {

	public static final int TYPE_BLUE = 0;
	public static final int TYPE_GREY = 1;
	public static final int TYPE_RED = 2;
	private boolean mIsSound;
	private MediaPlayer mPlayer;

	public NotifiedToast(Context context) {
		this(context, false);
	}
	
	public NotifiedToast(Context context, boolean isSound) {
		super(context);
		
		mIsSound = isSound;
		mPlayer = MediaPlayer.create(context, R.raw.newdatatoast);
		mPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.release();
			}
		});
	}

	@Override
	public void show() {
		super.show();
		
		if(mIsSound) {
			mPlayer.start();
		}
	}
	
	public static NotifiedToast makeText(Context cx, CharSequence text, int type, boolean isSound){
		NotifiedToast toast = new NotifiedToast(cx, isSound);
		
		LayoutInflater inflater = (LayoutInflater) cx.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		Resources res = cx.getResources();
		DisplayMetrics dms = res.getDisplayMetrics();
		
		View view = inflater.inflate(R.layout.layout_toast, null);
		view.setMinimumWidth(dms.widthPixels);
		
		TextView textView = (TextView) view.findViewById(R.id.toast_message);
		textView.setText(text);
		
		switch (type) {
		case TYPE_BLUE:
			textView.setBackgroundColor(res.getColor(R.color.toast_blue));
			break;
		case TYPE_GREY:
			textView.setBackgroundColor(res.getColor(R.color.toast_grey));
			break;
		case TYPE_RED:
			textView.setBackgroundColor(res.getColor(R.color.toast_red));
			break;
		default:
			break;
		}
		
		toast.setView(view);
		toast.setDuration(400);
		toast.setGravity(Gravity.TOP, 0, (int) ( cx.getResources().getDimension(
				R.dimen.abs__action_bar_default_height)));
		
		return toast;
	}
}
