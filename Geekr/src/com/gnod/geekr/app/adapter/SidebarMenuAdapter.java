package com.gnod.geekr.app.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.app.adapter.item.SidebarItem;
import com.gnod.geekr.tool.manager.DrawableManager;

public class SidebarMenuAdapter extends BaseAdapter {

	private ArrayList<SidebarItem> mList = new ArrayList<SidebarItem>();
	private LayoutInflater mInflater;
	
	public SidebarMenuAdapter(Context context) {
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void addGroup(SidebarItem group) {
		mList.add(group);
	}
	
	public void removeGroup(SidebarItem group) {
		mList.remove(group);
	}
	
	public void removeGroup(int index) {
		mList.remove(index);
	}
	
	public void removeAllGroup() {
		mList.clear();
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GroupView groupView;
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.sidebar_item, null);
			groupView = new GroupView();
			groupView.icon = (ImageView) convertView.findViewById(R.id.sidebar_item_icon);
			groupView.name = (TextView) convertView.findViewById(R.id.sidebar_item_name);
			convertView.setTag(groupView);
		} else {
			groupView = (GroupView) convertView.getTag();
		}
		bindGroupView(groupView, position);
		return convertView;
	}

	private void bindGroupView(GroupView groupView, int position) {
		SidebarItem item = mList.get(position);
		groupView.name.setText(item.name);
		if(item.iconId != -1)
			groupView.icon.setImageResource(item.iconId);
	}
	

	public class GroupView {
		public ImageView icon;
		public TextView name;
	}
}
