package com.gnod.geekr.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.app.adapter.SpinnerAdapter;
import com.gnod.geekr.model.CommentModel;
import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.tool.DateUtils;
import com.gnod.geekr.tool.LaunchHelper;
import com.gnod.geekr.tool.WeiboBaseTool;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.gnod.geekr.tool.fetcher.CommentFetcher;
import com.gnod.geekr.tool.fetcher.NoticeFetcher;
import com.gnod.geekr.tool.manager.DrawableManager;
import com.gnod.geekr.tool.manager.StatusManager;
import com.gnod.geekr.tool.manager.Utils;
import com.gnod.geekr.weibo.api.RemindAPI.UNREAD_TYPE;
import com.gnod.geekr.widget.AvatarView;
import com.gnod.geekr.widget.ColorToast;
import com.gnod.geekr.widget.ColorToast.ToastColor;

public class CommentsActivity extends BaseActivity {

	private ListView mListView;
	private CommentFetcher mFetcher;
	private ArrayList<CommentModel> mList = new ArrayList<CommentModel>();

	private DrawableManager mDrawableMgr;
	private ColorToast mTopToastView;
	private ColorToast mBottomToastView;

	private int mSelectedType = 0;
	private MenuItem mRefresh;
	private boolean isAllLoaded;
	private SpinnerAdapter mSpinAdapter;

	private String[] mSpinArrays = { "所有评论", "发出评论", "收到评论", "@我评论" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comments);
		mDrawableMgr = AppConfig.getDrawableManager();
		initView();
		bindListener();
		bindView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AppConfig.sImageFetcher.setExitTasksEarly(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		AppConfig.sImageFetcher.setPauseWork(false);
		AppConfig.sImageFetcher.setExitTasksEarly(true);
		AppConfig.sImageFetcher.flushCache();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_refresh, menu);
		mRefresh = menu.findItem(R.id.menu_refresh);
		setRefreshing(true);
		fetchComments();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_refresh:
			setRefreshing(true);
			isAllLoaded = false;
			fetchComments();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * init 之前必须确保appConfig必须已经初始化.
	 */
	private void initView() {
		mListView = (ListView) findViewById(R.id.view_comment_list);
		mTopToastView = (ColorToast) findViewById(R.id.view_comments_toast_top);
		mBottomToastView = (ColorToast) findViewById(R.id.view_comments_toast_bottom);
		mListView.setAdapter(listAdapter);
	}

	private void bindListener() {
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			private boolean lastViewVisible = false;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
					if (!Utils.hasHoneycomb()) {
						AppConfig.sImageFetcher.setPauseWork(true);
					}
				} else {
					AppConfig.sImageFetcher.setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (mList.size() == 0)
					return;
				if (firstVisibleItem + visibleItemCount >= totalItemCount
						&& !lastViewVisible) {
					lastViewVisible = true;
					if (isAllLoaded == false
							&& mRefresh.getActionView() == null) {
						setRefreshing(true);
						long maxId = Long.parseLong(mList.get(mList.size() - 1).ID);
						fetchComments(0, maxId, 20);
					}
				} else if (firstVisibleItem + visibleItemCount < totalItemCount) {
					lastViewVisible = false;
				}
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ItemView itemView = (ItemView) view.getTag();
				if (itemView == null || position > mList.size())
					return;
				CommentModel item = mList.get(position);
				Intent intent = new Intent(view.getContext(),
						PostStatusActivity.class);
				intent.putExtra("Type", "replyComment");
				intent.putExtra("CommentID", item.ID);
				intent.putExtra("StatusID", item.statusID);
				startActivity(intent);
			}
		});

	}

	private void bindView() {
		mFetcher = new CommentFetcher();

		mSpinAdapter = new SpinnerAdapter(this, mSpinArrays);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(mSpinAdapter,
				new OnNavigationListener() {
					@Override
					public boolean onNavigationItemSelected(int itemPosition,
							long itemId) {
						if (itemPosition == mSelectedType)
							return true;
						mSelectedType = itemPosition;
						setRefreshing(true);
						fetchComments();
						return true;
					}
				});
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mSelectedType = extras.getInt("Type", 0);
			getSupportActionBar().setSelectedNavigationItem(mSelectedType);
			if (mSelectedType == 2)
				WeiboBaseTool.getInstance().resetUnRead(UNREAD_TYPE.CMT, null);
			else if (mSelectedType == 3)
				WeiboBaseTool.getInstance().resetUnRead(
						UNREAD_TYPE.MENTION_CMT, null);
		}
	}

	private void setRefreshing(boolean checked) {
		if (mRefresh != null) {
			if (checked)
				mRefresh.setActionView(R.layout.layout_loading);
			else
				mRefresh.setActionView(null);
		}
	}

	public void fetchComments() {
		fetchComments(0, 0, 10);
	}

	public void fetchComments(long sinceId, long maxId, int count) {
		mFetcher.fetchComments(sinceId, maxId, count, mSelectedType,
				new OnFetchListener(mSelectedType));
	}

	private class OnFetchListener implements FetchCompleteListener {
		private int type = 0;

		public OnFetchListener(int type) {
			this.type = type;
		}

		@Override
		public void fetchComplete(int state, int code, Object obj) {
			setRefreshing(false);
			if (type != mSelectedType)
				return;
			switch (state) {
			case NoticeFetcher.FETCH_NOT_NETWORK:
			case NoticeFetcher.FETCH_AUTH_FAILED:
				mTopToastView.show((String) obj, ToastColor.RED);
				break;
			case NoticeFetcher.FETCH_EMPTY:
				break;
			case NoticeFetcher.FETCH_FAILED:
				break;
			case NoticeFetcher.FETCH_SUCCEED_NEWS:
			case NoticeFetcher.FETCH_SUCCEED_MORE:
				ArrayList<CommentModel> resultList = (ArrayList<CommentModel>) obj;
				if (resultList.size() > 1) {
					if (state == NoticeFetcher.FETCH_SUCCEED_NEWS) {
						mTopToastView.show("更新" + resultList.size() + "条新消息",
								ToastColor.BLUE);
						mList.clear();
						mList.addAll(resultList);
					} else if (state == NoticeFetcher.FETCH_SUCCEED_MORE) {
						mBottomToastView.show(
								"更新" + resultList.size() + "条新消息",
								ToastColor.BLUE);
						// 由于more时，根据当前最后一条id进行fetch，结果中第一条与当前最后一条
						// 出现重复，需除去一个重复item
						int lastIndex = mList.size() - 1;
						mList.remove(lastIndex);
						mList.addAll(resultList);
					}
					listAdapter.notifyDataSetChanged();
					if (state == NoticeFetcher.FETCH_SUCCEED_NEWS)
						mListView.setSelection(0);
				}
				if (resultList.size() < 5) {
					isAllLoaded = true;
				}
				break;
			}
		}
	};

	private BaseAdapter listAdapter = new BaseAdapter() {
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			ItemView item;
			if (view == null) {
				LayoutInflater inflater = LayoutInflater
						.from(CommentsActivity.this);
				view = inflater.inflate(R.layout.listitem_comments, null);

				item = new ItemView();
				item.imageAvatar = (AvatarView) view
						.findViewById(R.id.layout_met_item_avatar);
				item.imageVerified = (ImageView) view
						.findViewById(R.id.layout_met_item_verified_image);
				item.textName = (TextView) view
						.findViewById(R.id.layout_met_item_name);
				item.textContent = (TextView) view
						.findViewById(R.id.layout_met_item_content);
				item.textSource = (TextView) view
						.findViewById(R.id.layout_met_item_from);
				item.layoutRetweet = view
						.findViewById(R.id.layout_met_item_retweet);
				item.textRetweetContent = (TextView) view
						.findViewById(R.id.layout_met_item_retweet_content);
				item.textTime = (TextView) view
						.findViewById(R.id.layout_met_item_time);

				view.setTag(item);

				item.layoutRetweet.setOnClickListener(retweetClickListener);
			} else {
				item = (ItemView) view.getTag();
			}

			CommentModel model = mList.get(position);
			UserInfoModel userInfo = model.userInfo;
			if (item == null && userInfo == null)
				return null;
			item.imageAvatar.setItem(userInfo);
			// item.imageAvatar.setImageResource(R.drawable.avatar_default);
			// mDrawableMgr.loadBitmap(userInfo.iconURL, item.imageAvatar,
			// true);
			AppConfig.sImageFetcher.loadImage(userInfo.iconURL,
					item.imageAvatar, R.drawable.avatar_default);
			item.textName.setText(userInfo.nickName);
			if (userInfo.verifiedType == 3 || userInfo.verifiedType == 2
					|| userInfo.verifiedType == 7) {
				item.imageVerified.setVisibility(View.VISIBLE);
				item.imageVerified
						.setImageResource(R.drawable.ic_verified_blue);
			} else if (userInfo.verifiedType == 220) {
				item.imageVerified.setVisibility(View.VISIBLE);
				item.imageVerified.setImageResource(R.drawable.ic_daren);
			} else if (userInfo.verifiedType == 0) {
				item.imageVerified.setVisibility(View.VISIBLE);
				item.imageVerified.setImageResource(R.drawable.ic_verified);
			} else {
				item.imageVerified.setVisibility(View.GONE);
			}
			item.textContent.setText(model.content);

			item.layoutRetweet.setTag(model);
			if (model.replyComment == null && model.status == null) {
				item.layoutRetweet.setVisibility(View.GONE);
			} else if (model.replyComment != null) {
				item.layoutRetweet.setVisibility(View.VISIBLE);
				String content = "";
				if (model.replyComment.userInfo != null)
					;
				content = "回复 @" + model.replyComment.userInfo.nickName
						+ " 的评论: ";
				content += model.replyComment.content;
				item.textRetweetContent.setText(content);
			} else {
				item.layoutRetweet.setVisibility(View.VISIBLE);
				String content = "";
				if (model.status.userInfo != null)
					;
				content = "评论 @" + model.status.userInfo.nickName + " 的微博:  ";
				content += model.status.content;
				item.textRetweetContent.setText(content);
			}
			item.textTime.setText(DateUtils.getMagicTime(model.time));
			if (!StringUtils.isNullOrEmpty(model.source))
				item.textSource.setText(Html.fromHtml(model.source).toString());
			return view;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		private View.OnClickListener retweetClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CommentModel model = (CommentModel) v.getTag();
				if (model.status != null) {
					LaunchHelper.startDetailActivity(v.getContext(),
							model.status);
				}
			}
		};

	};

	class ItemView {
		public AvatarView imageAvatar;
		public ImageView imageVerified;
		public TextView textName;
		public TextView textContent;
		public TextView textSource;
		public View layoutRetweet;
		public TextView textRetweetContent;
		public TextView textTime;
	}

}
