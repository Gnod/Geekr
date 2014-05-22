package com.gnod.geekr.app.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.gnod.geekr.holder.StatusDataHolder;
import com.gnod.geekr.holder.StatusViewHolder;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.tool.LaunchHelper;
import com.gnod.geekr.tool.converter.GeekrViewConverter;
import com.gnod.geekr.tool.manager.SettingManager;
import com.gnod.geekr.tool.manager.StatusManager;
import com.gnod.geekr.widget.GeekrPanel;
import com.gnod.geekr.widget.GeekrPanel.OnItemClcikListener;
import com.gnod.geekr.widget.StatusItemLayout;

public class TimelineAdapter extends BaseAdapter {

	private Context mContext;
	private StatusDataHolder mStatusHolder;
	private OnItemClcikListener mOnPanelItemClicked;
	
	public TimelineAdapter(Context context,	StatusDataHolder statusHolder) {
		this.mContext = context;
		this.mStatusHolder = statusHolder;
	}
	
	public TimelineAdapter(Context context, ArrayList<StatusModel> list) {
		this(context, new StatusDataHolder(list));
	}

	public TimelineAdapter(Context context,
			StatusDataHolder mStatusHolder2,
			OnItemClcikListener onPanelItemClicked) {
		this(context, mStatusHolder2);
		this.mOnPanelItemClicked = onPanelItemClicked;
	}

	@Override
	public int getCount() {
		return mStatusHolder.list.size();
	}

	@Override
	public Object getItem(int position) {
		return mStatusHolder.list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		StatusViewHolder statusView = null;
		if (convertView == null) {
			convertView = new StatusItemLayout(mContext);
			if(mClickListener != null)
				convertView.setOnClickListener(mClickListener);
			if(mOnLongClickListener != null)
				convertView.setOnLongClickListener(mOnLongClickListener);
			if(mOnPanelItemClicked != null)
				((StatusViewHolder) convertView.getTag()).togglePanel.setClickedListener(
						mOnPanelItemClicked);
		}
		statusView = (StatusViewHolder) convertView.getTag();
		
		statusView.togglePanel.resetLayout();
		StatusModel item = mStatusHolder.list.get(position);
		if (item == null)
			return null;
		statusView.togglePanel.setItemIndex(position);
		GeekrViewConverter.attachViewDatas(statusView, item, position, 
				SettingManager.getPicModel());
		return convertView;
	}
	
	public void setItemClickListener(OnClickListener clickListener) {
		this.mClickListener = clickListener;
	}

	public void setOnItemLongClickListener(OnLongClickListener l){
		this.mOnLongClickListener = l;
	}
	
	public void setOnPanelItemClickListener(GeekrPanel.OnItemClcikListener l) {
		this.mOnPanelItemClicked = l;
	}
	
	private OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			StatusViewHolder statusView = (StatusViewHolder) v.getTag();
			int index = statusView.tag;
			if(statusView.togglePanel.isOpen()){
				statusView.togglePanel.toggle();
			} else {
				StatusModel item = mStatusHolder.list.get(index);
				LaunchHelper.startDetailActivity(v.getContext(), 
						item, index, StatusManager.getCacheTag());
			}
		}
	};
	
	private OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			StatusViewHolder statusView = (StatusViewHolder) v.getTag();
			statusView.togglePanel.toggle();
			return true;
		}
	};
	
}
