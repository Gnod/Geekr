package com.gnod.geekr.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gnod.geekr.R;

public class ListViewFooter extends LinearLayout {

	private TextView footerText;
	private View footerSpinner;

	public ListViewFooter(Context context) {
		this(context, null);
	}

	public ListViewFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setLayoutParams(new AbsListView.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		View view = LayoutInflater.from(context).inflate(R.layout.listitem_footer, null);
		addView(view);
		footerText = (TextView)view.findViewById(R.id.text_hint);
		footerSpinner = view.findViewById(R.id.view_progress);
	}
	
	public void startLoading() {
		footerText.setVisibility(View.INVISIBLE);
		footerSpinner.setVisibility(View.VISIBLE);
	}
	
	public boolean isLoading() {
		return footerSpinner.getVisibility() == View.VISIBLE;
	}
	
	public void stopLoading(CharSequence text) {
		footerText.setText(text);
		footerSpinner.setVisibility(View.GONE);
		footerText.setVisibility(View.VISIBLE);
	}
}