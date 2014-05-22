package com.gnod.geekr.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;

public class ColorToast extends LinearLayout{

	private final Animation animTopShow = AnimationUtils.loadAnimation(
			AppConfig.getAppContext(), R.anim.move_down_from_top_anim);
	private final Animation animTopHidden = AnimationUtils.loadAnimation(
			AppConfig.getAppContext(), R.anim.move_up_to_top_anim);
	private final Animation animBottomShow = AnimationUtils.loadAnimation(
			AppConfig.getAppContext(), R.anim.move_up_from_bottom_anim);
	private final Animation animBottomHidden = AnimationUtils.loadAnimation(
			AppConfig.getAppContext(), R.anim.move_down_to_bottom_anim);
	private Animation animShow = this.animTopShow;
	private Animation animHidden = this.animTopHidden;
	private TextView hintText;
	private Resources res;

	public ColorToast(Context context) {
		this(context, null);
	}

	public ColorToast(Context context, AttributeSet attrs) {
		super(context, attrs);
		res = context.getResources();

		setLayoutParams(new ViewGroup.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		hintText = new TextView(context);
		hintText.setTextColor(res.getColor(R.color.color_white));
		hintText.setLayoutParams(params);
		hintText.setGravity(Gravity.CENTER);
		hintText.setTextSize(14.0f);
		hintText.setBackgroundColor(res.getColor(R.color.toast_grey));
		hintText.setPadding(0, 4, 0, 4);
		addView(hintText);
		hintText.setVisibility(View.GONE);
		
		TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.ColorToast);
		boolean alignTop = typeArray.getBoolean(0, false);
		typeArray.recycle();
		setAlignTop(alignTop);
	}
	
	private void show(CharSequence text,ToastColor color,
			View.OnClickListener listener, long duration) {
		post(new ToastRunnable(this, text, color, duration, listener));
	}
	
	public final void show(CharSequence text, long duration) {
		show(text, ToastColor.GREY,  null, duration);
	}
	
	public final void show(CharSequence text, ToastColor color, long duration) {
		show(text, color, null, duration);
	}
	
	public final void show(CharSequence text, ToastColor color) {
		show(text, color, null, 2000L);
	}
	
	public final void show(int textId, ToastColor color, long duration) {
		show(getContext().getText(textId), color, null, duration);
	}
	
	protected void appear(final long duration) {
		AnimationSet animSet = new AnimationSet(false);
		animSet.addAnimation(animShow);
		animHidden.setStartOffset(animShow.getDuration() + duration);
		animSet.addAnimation(animHidden);
		animSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationEnd(Animation animation) {
				hintText.setVisibility(View.GONE);
			}
		});
		hintText.startAnimation(animSet);
		hintText.setVisibility(View.VISIBLE);
	}
	
	public void setText(CharSequence text, ToastColor color ) {
		int colorId = R.color.toast_grey;
		
		if(color == ToastColor.BLUE)
			colorId = R.color.toast_blue;
		else if(color == ToastColor.GREY)
			colorId = R.color.toast_grey;
		else if(color == ToastColor.RED)
			colorId = R.color.toast_red;
		hintText.setBackgroundColor(res.getColor(colorId));
		hintText.setText(text);
	}

	
	private class ToastRunnable implements Runnable {
		private ColorToast toast;
		private CharSequence text;
		private ToastColor color;
		private long duration;
		private View.OnClickListener listener;
		
		public ToastRunnable(ColorToast toast, CharSequence text,
				ToastColor color, long duration, OnClickListener listener) {
			this.toast = toast;
			this.text = text;
			this.color = color;
			this.duration = duration;
			this.listener = listener;
		}

		@Override
		public void run() {
			ColorToast toast = this.toast;
			toast.setText(text, color);
			toast.appear(duration);
		}
		
	}

	private void setAlignTop(boolean paramBoolean) {
		if (paramBoolean) {
			this.animShow = this.animTopShow;
			this.animHidden = this.animTopHidden;
		} else {
			this.animShow = this.animBottomShow;
			this.animHidden = this.animBottomHidden;
		}
	}
	 
	public enum ToastColor {
		GREY, BLUE, RED
	}
}
