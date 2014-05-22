package com.gnod.geekr.app.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.gnod.geekr.holder.StatusViewHolder;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.tool.LaunchHelper;
import com.gnod.geekr.widget.GeekrPanel;
import com.gnod.geekr.widget.StatusItemLayout;

public class ProfileTimelineAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<StatusModel> mList;
	private boolean isSelf = false;
	private GeekrPanel.OnItemClcikListener mPanelItemClicked;
	
	public ProfileTimelineAdapter(Context context, ArrayList<StatusModel> statusList) {
		this.mContext = context;
		this.mList = statusList;
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = new StatusItemLayout(mContext);
			if(mClickListener != null)
				convertView.setOnClickListener(mClickListener);
			if(mOnLongClickListener != null)
				convertView.setOnLongClickListener(mOnLongClickListener);
			if(mPanelItemClicked != null)
				((StatusViewHolder) convertView.getTag()).togglePanel.setClickedListener(
						mPanelItemClicked);
		}
		
		StatusModel item = mList.get(position);
		if (item == null)
			return null;
		StatusViewHolder statusView = (StatusViewHolder) convertView.getTag();
		statusView.togglePanel.resetLayout();
		statusView.layoutAvatar.setVisibility(View.GONE);
		statusView.textName.setVisibility(View.GONE);
		
		
		if(isSelf){
			statusView.togglePanel.setBtnDelVisible(true);
		}
		statusView.togglePanel.setItemIndex(position);
		((StatusItemLayout) convertView).attachViewData(item, position);
		return convertView;
	}
	
	public void setSelfFlag(boolean flag) {
		isSelf = flag;
	}
	
	public void setOnPanelItemClickListener(GeekrPanel.OnItemClcikListener l) {
		this.mPanelItemClicked = l;
	}
	
	private OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			StatusViewHolder statusView = (StatusViewHolder) view.getTag();
			if(statusView.togglePanel.isOpen()){
				statusView.togglePanel.toggle();
				return;
			}
			int index = statusView.tag;
			StatusModel item = mList.get(index);
			LaunchHelper.startDetailActivity(view.getContext(), item);
		}
	};
	
	private OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			StatusViewHolder statusView = (StatusViewHolder) v.getTag();
			statusView.togglePanel.toggle();
			return true;
		}
	};
}
