package com.gnod.geekr.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.ui.activity.ProfileActivity;

public class AvatarView extends ImageView{

	private UserInfoModel mItem;
	public AvatarView(Context context) {
		super(context);
	}

	public AvatarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AvatarView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setOnClickListener(mClickListener);
	}

	public void setItem(final UserInfoModel item) {
		if(item == null)
			return;
		this.mItem = item;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setAlpha(127);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			setAlpha(255);
			break;
		}
		return super.onTouchEvent(event);
	}

	private View.OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mItem == null)
				return;
			Intent intent = new Intent(getContext(), ProfileActivity.class);
			intent.putExtra("UserInfoModel", mItem);
			getContext().startActivity(intent);
		}
	};

}
