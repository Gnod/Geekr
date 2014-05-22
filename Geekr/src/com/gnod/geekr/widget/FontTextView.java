package com.gnod.geekr.widget;

import java.io.File;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class FontTextView extends TextView {

	public FontTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FontTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FontTextView(Context context) {
		super(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Typeface type = Typeface.createFromAsset(getContext().getAssets(), 
				"fonts" + File.separator + "HelveticaNeueLTPro-ThEx.otf");
		setTypeface(type);
	}
}
