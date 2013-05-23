package com.gnod.geekr.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListAdapter;

import com.gnod.geekr.R;
import com.gnod.geekr.app.adapter.TimelineAdapter;
import com.gnod.geekr.holder.StatusDataHolder;
import com.gnod.geekr.holder.StatusViewHolder;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.tool.GeekrTool;
import com.gnod.geekr.tool.LaunchHelper;
import com.gnod.geekr.tool.WeiboBaseTool;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.fetcher.NoticeFetcher;
import com.gnod.geekr.tool.fetcher.StatusFetcher;
import com.gnod.geekr.widget.GeekrPanel;

public class TopicsActivity extends RefreshActivity<StatusModel> {

	private TimelineAdapter mStatusAdapter;
	private StatusFetcher mStatusFetcher;
	private StatusDataHolder mStatusHolder = new StatusDataHolder();
	private String mTopics;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void initFetcher() {
		mStatusFetcher = new StatusFetcher();
	}
	@Override
	public void initAdapter() {
		mStatusHolder.list = new ArrayList<StatusModel>();
		mStatusAdapter = new TimelineAdapter(this, mStatusHolder, onPanelItemClicked);
		mStatusAdapter.setItemClickListener(mClickListener);
	}

	@Override
	public ListAdapter getAdapter() {
		return mStatusAdapter;
	}

	@Override
	public int getListSize() {
		return mStatusHolder.list.size();
	}
	
	@Override
	public void bindView() {
		Intent intent = getIntent();
		
		mTopics = intent.getStringExtra("Topics");
		setTitle("话题: " + mTopics );
		if(StringUtils.isNullOrEmpty(mTopics)){
			finish();
		}
	}

	@Override
	public void onItemClicked(View view, int position, long id) {
		StatusViewHolder statusView = (StatusViewHolder)view.getTag();
		if(statusView == null)
			return;
		StatusModel item = mStatusHolder.list.get(position);
		Intent intent = new Intent(TopicsActivity.this, StatusDetailActivity.class);
		intent.putExtra("itemModel", item);
		startActivity(intent);
	}

	@Override
	public void fetchDatas(int count, int page) {
		mStatusFetcher.fetchTopics(mTopics, count, page, getFetchListener());
	}

	@Override
	public void onFetchSucceed(int state, ArrayList<StatusModel> resultList) {
		if(resultList.size() != 0){
			if(state == NoticeFetcher.FETCH_SUCCEED_NEWS) {
				mStatusHolder.list.clear();
			} 
			mStatusHolder.list.addAll(resultList);
			mStatusAdapter.notifyDataSetChanged();
		}
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
						item);
			}
		}
	};
	
	private GeekrPanel.OnItemClcikListener onPanelItemClicked = new GeekrPanel.OnItemClcikListener() {
		@Override
		public void onClick(int position, View v) {
			switch (v.getId()) {
			case R.id.sd_btn_fav:
				WeiboBaseTool.getInstance().favoriteStatus(
						mStatusHolder.list.get(position).ID);
				break;
			case R.id.sd_btn_copy:
				GeekrTool.copyTextToClipboard(
						mStatusHolder.list.get(position).content);
				break;
			case R.id.sd_btn_ret:
				LaunchHelper.startRetweetActivity(v.getContext(), 
						mStatusHolder.list.get(position));
				break;
			case R.id.sd_btn_cmt:
				LaunchHelper.startCommentActivity(v.getContext(), 
						mStatusHolder.list.get(position));
				break;
			default:
				break;
			}
		}
	};
}
