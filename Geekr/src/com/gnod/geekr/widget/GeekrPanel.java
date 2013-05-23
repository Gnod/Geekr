package com.gnod.geekr.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

import com.gnod.geekr.R;

public class GeekrPanel extends RelativeLayout{

	private int iDefaultHeight;
	private PanelHolder mPanelHolder;
	private int iPosition;
	private OnItemClcikListener mOnItemClicked;

	public GeekrPanel(Context context) {
		this(context, null);
	}

	public GeekrPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutParams(new ViewGroup.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		View view = LayoutInflater.from(context).inflate(R.layout.layout_panel, null);
		
		mPanelHolder = new PanelHolder();
		mPanelHolder.btnDel = view.findViewById(R.id.sd_btn_del);
		mPanelHolder.delDivide = view.findViewById(R.id.sd_btn_del_divider);
		mPanelHolder.btnFav = view.findViewById(R.id.sd_btn_fav);
		mPanelHolder.favDivide = view.findViewById(R.id.sd_btn_fav_divider);
		mPanelHolder.btnView = view.findViewById(R.id.sd_btn_view);
		mPanelHolder.viewDivide = view.findViewById(R.id.sd_btn_view_divider);
		mPanelHolder.btnCopy = view.findViewById(R.id.sd_btn_copy);
		mPanelHolder.btnRet = view.findViewById(R.id.sd_btn_ret);
		mPanelHolder.retDivide = view.findViewById(R.id.sd_btn_ret_divider);
		mPanelHolder.btnCmt = view.findViewById(R.id.sd_btn_cmt);
		
		mPanelHolder.btnDel.setOnClickListener(onClckListener);
		mPanelHolder.btnFav.setOnClickListener(onClckListener);
		mPanelHolder.btnCopy.setOnClickListener(onClckListener);
		mPanelHolder.btnView.setOnClickListener(onClckListener);
		mPanelHolder.btnRet.setOnClickListener(onClckListener);
		mPanelHolder.btnCmt.setOnClickListener(onClckListener);
		
		addView(view);
		measureView(this);
		measureView(view);
		iDefaultHeight = view.getMeasuredHeight();
		resetLayout();
	}
	
	public void setBtnDelVisible(boolean flag){
		mPanelHolder.btnDel.setVisibility(flag? View.VISIBLE : View.GONE);
		mPanelHolder.delDivide.setVisibility(flag? View.VISIBLE : View.GONE);
	}
	
	public void setClickedListener(OnItemClcikListener l) {
		this.mOnItemClicked = l;
	}
	
	public void setItemIndex(int position) {
		this.iPosition = position;
	}
	
	public interface OnItemClcikListener {
		public void onClick(int position, View v);
	}
	
	
	private View.OnClickListener onClckListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mOnItemClicked != null)
				mOnItemClicked.onClick(iPosition, v);
		}
	};
	
	public void resetLayout() {
		getLayoutParams().height = 0;
		requestLayout();
	}

	public void toggle() {
		if(getAnimation() == null) {
			ResetAnimimation animation = new ResetAnimimation(this,
					getHeight() == 0? iDefaultHeight : 0, false);
			animation.setDuration(100);
			startAnimation(animation);
			requestLayout();
		}
	}
	
	public boolean isOpen() {
		return getHeight() != 0;
	}
	
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}
	
	public class ResetAnimimation extends Animation {
		private int targetHeight;
		private int originalHeight;
		private int extraHeight;
		private boolean down;
		private View view;

		protected ResetAnimimation(View view, int targetHeight, boolean down) {
			this.view = view;
			this.targetHeight = targetHeight;
			this.down = down;
			originalHeight = view.getHeight();
			extraHeight = this.targetHeight - originalHeight;
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {

			int newHeight;
			newHeight = (int) (targetHeight - extraHeight * (1 - interpolatedTime));
			getLayoutParams().height = newHeight;
			view.requestLayout();
		}

		@Override
		public void initialize(int width, int height, int parentWidth,
				int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
		}

	}
	
	private class PanelHolder {
		public View btnDel;
		public View delDivide;
		public View btnFav;
		public View favDivide;
		public View btnView;
		public View viewDivide;
		public View btnCopy;
		public View copyDivide;
		public View btnRet;
		public View retDivide;
		public View btnCmt;
	}
}
