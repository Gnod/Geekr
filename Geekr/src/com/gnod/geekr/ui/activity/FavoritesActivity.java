package com.gnod.geekr.ui.activity;

import java.util.ArrayList;

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
import com.gnod.geekr.tool.ToastHelper;
import com.gnod.geekr.tool.fetcher.BaseFetcher;
import com.gnod.geekr.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.gnod.geekr.tool.fetcher.FavoriteFetcher;
import com.gnod.geekr.tool.fetcher.NoticeFetcher;
import com.gnod.geekr.widget.GeekrPanel;

public class FavoritesActivity extends RefreshActivity<StatusModel> {

	private TimelineAdapter mStatusAdapter;
	private FavoriteFetcher mFetcher;
	private StatusDataHolder mStatusHolder = new StatusDataHolder();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void initFetcher() {
		mFetcher = new FavoriteFetcher();
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
		setTitle("收藏");
	}

	
	protected void setItemClickListener() {
	}
	
	protected void setItemLongClickListener() {
	}
	
	@Override
	public void fetchDatas(int count, int page) {
		mFetcher.fetchFavorities(count, page, getFetchListener());
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
		public void onClick(final int position, View v) {
			final StatusModel item = mStatusHolder.list.get(position);
			
			switch (v.getId()) {
			case R.id.sd_btn_fav:
				mFetcher.destroy(item.ID, new FetchCompleteListener() {
					@Override
					public void fetchComplete(int state, int errorCode, Object obj) {
						if(state == BaseFetcher.FETCH_SUCCEED_NEWS) {
							mStatusHolder.list.remove(position);
							mStatusAdapter.notifyDataSetChanged();
							ToastHelper.show("取消收藏成功", 0);
						} else {
							ToastHelper.show("取消收藏失败", 2);
						}
					}
				});
				break;
			case R.id.sd_btn_copy:
				GeekrTool.copyTextToClipboard(item.content);
				break;
			case R.id.sd_btn_ret:
				LaunchHelper.startRetweetActivity(
						FavoritesActivity.this, item);
				break;
			case R.id.sd_btn_cmt:
				LaunchHelper.startCommentActivity(
						FavoritesActivity.this, item);
				break;
			}
		}
	};
}
