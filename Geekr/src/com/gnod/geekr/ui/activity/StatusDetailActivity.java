package com.gnod.geekr.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gnod.geekr.R;
import com.gnod.geekr.app.adapter.StatusDetailAdapter;
import com.gnod.geekr.holder.StatusViewHolder;
import com.gnod.geekr.model.AccountModel;
import com.gnod.geekr.model.CommentModel;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.tool.LaunchHelper;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.converter.GeekrViewConverter;
import com.gnod.geekr.tool.converter.GeekrViewConverter.IMAGE_MODEL;
import com.gnod.geekr.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.gnod.geekr.tool.fetcher.CommentFetcher;
import com.gnod.geekr.tool.fetcher.NoticeFetcher;
import com.gnod.geekr.tool.manager.AccountManager;
import com.gnod.geekr.tool.manager.StatusManager;
import com.gnod.geekr.widget.AvatarView;
import com.gnod.geekr.widget.ColorToast;
import com.gnod.geekr.widget.ColorToast.ToastColor;
import com.gnod.geekr.widget.ListViewFooter;
import com.gnod.geekr.widget.URLImageView;

public class StatusDetailActivity extends BaseActivity {

	private static final int FETCH_COUNT = 20;
	private ListView mListView;
	private ArrayList<CommentModel> mList = new ArrayList<CommentModel>();
	private StatusModel itemModel;
	private StatusDetailAdapter mAdapter;
	private CommentFetcher mFetcher;
	
	private int position = -1;
	private MenuItem refreshMenu;
	private ListViewFooter footer;
	private ColorToast toastTop;
	private String mStatusTag;
	private StatusViewHolder statusView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);

		mFetcher = new CommentFetcher();
		
		Intent intent = this.getIntent();
		itemModel =(StatusModel)intent.getSerializableExtra("itemModel");
		position  = intent.getIntExtra("Position", -1);
		//Using for cache query
		mStatusTag = intent.getStringExtra("StatusTag");
		if(itemModel == null || itemModel.userInfo == null)
			finish();
		initView();
		bindListener();
		bindView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_status_detail, menu);
		refreshMenu = menu.findItem(R.id.menu_detail_refresh);
		setRefreshing(true);
		fetchDatas(itemModel.ID);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_detail_cmt:
			LaunchHelper.startCommentActivity(this, itemModel);
			return true;
		case R.id.menu_detail_ret:
			LaunchHelper.startRetweetActivity(this, itemModel);
			return true;
		case R.id.menu_detail_refresh:
			setRefreshing(true);
			footer.startLoading();
			fetchDatas(itemModel.ID);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initView() {
		
		mListView = (ListView)findViewById(R.id.view_detail_list);
		View header = LayoutInflater.from(this).inflate(R.layout.listitem_detail_header, null);
		footer = new ListViewFooter(this);
		
		toastTop = (ColorToast)findViewById(R.id.view_detail_toast_top);
		
		statusView = new StatusViewHolder();
		statusView.layoutAvatar = (RelativeLayout)header.findViewById(R.id.layout_timeline_avatar);
		statusView.imageAvatar = (AvatarView)header.findViewById(R.id.image_avatar_small);
		statusView.verifiedImage = (ImageView)header.findViewById(R.id.image_avatar_verified);
		statusView.textName = (TextView) header.findViewById(R.id.text_detailhead_name);
		
		statusView.textContent = (TextView)header.findViewById(R.id.text_detailhead_content);
		statusView.imageThumb = (URLImageView)header.findViewById(R.id.image_detailhead_thumb);
		statusView.layoutRetweet = (LinearLayout)header.findViewById(R.id.layout_detailhead_retweet);
		statusView.textRetweetContent = (TextView) header.findViewById(R.id.text_detailhead_retweet_content);
		statusView.imageRetweetThumb = (URLImageView) header.findViewById(R.id.image_detailhead_retweet_thumb);
		statusView.textTime = (TextView) header.findViewById(R.id.text_detailhead_time);
		statusView.textSource = (TextView) header.findViewById(R.id.text_detailhead_source);
		statusView.textRetweetCount = (TextView)header.findViewById(R.id.text_detailhead_ret_count);
		statusView.textCommentCount = (TextView) header.findViewById(R.id.text_detailhead_cmt_count);
		
		mListView.addHeaderView(header);
		mListView.addFooterView(footer);
		mAdapter = new StatusDetailAdapter(this, itemModel, mList);
		mListView.setAdapter(mAdapter);
	}
	
	private void bindListener() {
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			private boolean lastViewVisible = false;
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(mList.size() == 0 || footer.isLoading())
					return;
				if(firstVisibleItem + visibleItemCount >= totalItemCount && 
						!lastViewVisible) {
					lastViewVisible = true;
					if(mList.size()  < Integer.parseInt(itemModel.commentCount)) {
						setRefreshing(true);
						footer.startLoading();
						long maxId = Long.parseLong(mList.get(mList.size() - 1).ID);
						mFetcher.fetchComment(itemModel.ID, 0, maxId, 
								FETCH_COUNT, 1, onCommentFetchListener);
					}
				} else if(firstVisibleItem + visibleItemCount < totalItemCount) {
					lastViewVisible = false;
				}
			}
		});
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// comment header 占据了位置 0
				if(position == 0)
					return;
				int index = position - 1;
				// 排除footer
				if(index >= mList.size())
					return;
				CommentModel model = mList.get(index);
				LaunchHelper.startReplyActivity(
						StatusDetailActivity.this, model);
			}
		});
	}
	
	private void setRefreshing(boolean checked) {
		if(refreshMenu != null){
			if(checked)
				refreshMenu.setActionView(R.layout.layout_loading);
			else 
				refreshMenu.setActionView(null);
		}
	}
	
	private void bindView() {
		setTitle("正文");
		
		GeekrViewConverter.attachViewDatas(statusView, itemModel, position, 
				IMAGE_MODEL.BIG);
		if(itemModel.retweetItem != null) {
			statusView.layoutRetweet.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(StatusDetailActivity.this, StatusDetailActivity.class);
					intent.putExtra("itemModel", itemModel.retweetItem);
					startActivity(intent);
				}
			});
		}
	}

	private void fetchDatas(String id) {
		mFetcher.fetchStatus(id, onStatusFetchListener);
		mFetcher.fetchComment(id, 0, 0, FETCH_COUNT, 
				1, onCommentFetchListener);
	}
	
	private FetchCompleteListener onStatusFetchListener = new FetchCompleteListener() {
		@Override
		public void fetchComplete(int state, int code, Object obj) {
			switch (state) {
			case NoticeFetcher.FETCH_AUTH_FAILED:
			case NoticeFetcher.FETCH_NOT_NETWORK:
				toastTop.show((String) obj, ToastColor.RED);
				break;
			case NoticeFetcher.FETCH_SUCCEED_NEWS:
				itemModel = (StatusModel)obj;
				if(position != -1 && !StringUtils.isNullOrEmpty(mStatusTag)) {
					AccountModel account = AccountManager.getActivityAccount();
					StatusManager.setSingleStatus(mStatusTag, 
							account, position, itemModel);
				}
				GeekrViewConverter.attachViewDatas(statusView, itemModel, position, 
						IMAGE_MODEL.BIG);
				break;
			}
		}
	};
	
	private FetchCompleteListener onCommentFetchListener = new FetchCompleteListener() {
		@Override
		public void fetchComplete(int state, int code, Object obj) {
			setRefreshing(false);
			switch (state) {
			case NoticeFetcher.FETCH_NOT_NETWORK:
			case NoticeFetcher.FETCH_AUTH_FAILED:
				toastTop.show((String) obj, ToastColor.RED);
				footer.stopLoading("-FAILED-");
				break;
			case NoticeFetcher.FETCH_EMPTY:
				footer.stopLoading("-NO COMMENTS-");
				break;
			case NoticeFetcher.FETCH_SUCCEED_NEWS:
				ArrayList<CommentModel> result = (ArrayList<CommentModel>)obj;
				if(result.size() != 0){
					mList.clear();
					mList.addAll(result);
					mAdapter.notifyDataSetChanged();
					mListView.setSelection(0);
					if(result.size() < 5) {
						footer.stopLoading("-END-");
					} else {
						footer.stopLoading("-MORE-");
					}
				}
				break;
			case NoticeFetcher.FETCH_SUCCEED_MORE:
				ArrayList<CommentModel> resultList = (ArrayList<CommentModel>)obj;
				if(resultList.size() != 0){
					int lastIndex = mList.size() - 1;
					mList.remove(lastIndex);
					mList.addAll(resultList);
					mAdapter.notifyDataSetChanged();
					if(resultList.size() < 5) {
						footer.stopLoading("-END-");
					} else {
						footer.stopLoading("-MORE-");
					}
				}
				break;
			default:
				footer.stopLoading("");
				break;
			}
		}
	};
	public class StatusDetailHeadView {
		public AvatarView imageAvatar;
		public TextView textName;
		public ImageView verifiedImage;
	}
	
	public class StatusDetailContentView {
		public TextView textContent;
		public URLImageView imageThumb;
		public LinearLayout layoutRetweet;
		public TextView textRetweetContent;
		public URLImageView imageRetweetThumb;
		public TextView textTime;
		public TextView textSource;
		public TextView textRetweetCount;
		public TextView textCommentCount;
	}
}
