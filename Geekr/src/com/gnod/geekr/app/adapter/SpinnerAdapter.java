package com.gnod.geekr.app.adapter;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gnod.geekr.R;

public class SpinnerAdapter extends BaseAdapter {
	private ArrayList mList;
	private Context mContext;
	
	public SpinnerAdapter(Context context, ArrayList<String> list) {
		mContext = context;
		mList = list;
	}
	
	public SpinnerAdapter(Context context, String[] array) {
		this(context, new ArrayList<String>(Arrays.asList(array)));
	}
	
	public SpinnerAdapter(Context context) {
		this(context, new ArrayList<String>());
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SpinnerItem item;
		if(convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.spinner_item, null);
			item = new SpinnerItem();
			item.textTitle = (TextView) convertView;
			convertView.setTag(item);
		} else {
			item = (SpinnerItem) convertView.getTag();
		}
		setItemText(item.textTitle, position);
		return convertView;
	}
	
	@Override
	public View getDropDownView(int position, View convertView,
			ViewGroup parent) {
		SpinnerItem item;
		
		if(convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.spinner_item_dropdown, null);
			item = new SpinnerItem();
			item.textTitle = (TextView) convertView;
			convertView.setTag(item);
		} else {
			item = (SpinnerItem) convertView.getTag();
		}
		setDropDownItemText(item.textTitle, position);
		return convertView;
	}
	
	public void setItemText(TextView view, int position) {
		view.setText(getList().get(position).toString());
	}
	
	public void setDropDownItemText(TextView view, int position) {
		view.setText(getList().get(position).toString());
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public Object getItem(int position) {
		return getList().get(position);
	}
	@Override
	public int getCount() {
		return getList().size();
	}
	
	public void setList(ArrayList list) {
		mList = list;
	}
	
	public ArrayList getList() {
		return mList;
	}
	
	private class SpinnerItem {
		public TextView textTitle;
	}
}
