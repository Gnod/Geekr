package com.gnod.geekr.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.OnClickListener;

import com.actionbarsherlock.internal.widget.IcsSpinner;

public class GeekrSpinner extends IcsSpinner{

	private OnClickListener mOnClicked;

	public GeekrSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean performClick() {
		if(mOnClicked != null)
			mOnClicked.onClick(this);
		return super.performClick();
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		this.mOnClicked = l;
	}

	
}
