package com.gnod.geekr.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.tool.WeiboBaseTool;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.ToastHelper;
import com.gnod.geekr.tool.fetcher.BaseFetcher;
import com.gnod.geekr.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.gnod.geekr.tool.fetcher.ProfileFetcher;
import com.gnod.geekr.tool.manager.DrawableManager;
import com.gnod.geekr.tool.manager.Utils;
import com.gnod.geekr.weibo.api.RemindAPI.UNREAD_TYPE;
import com.gnod.geekr.widget.AvatarView;
import com.gnod.geekr.widget.ListViewFooter;

public class FriendListActivity extends BaseActivity {

	private static final int FETCH_COUNT = 30;
	public static final int TYPE_FOLLOWERS = 0;
	public static final int TYPE_FOLLOWING = 1;

	private ListView mListView;
	private ArrayList<UserInfoModel> mList = new ArrayList<UserInfoModel>();
	private ProfileFetcher mFetcher;
	private DrawableManager mDrawableMgr;

	private UserInfoModel mUserInfoModel;
	private boolean isAllLoaded;
	private int type;
	private ListViewFooter mFooter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_follow);

		mFetcher = new ProfileFetcher();
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
		getSupportMenuInflater().inflate(R.menu.menu_follow, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initView() {

		mListView = (ListView) findViewById(R.id.view_follow_list);

		mFooter = new ListViewFooter(this);

		mListView.addFooterView(mFooter);
		mListView.setAdapter(userAdapter);
		mFooter.startLoading();
	}

	private void bindListener() {
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				if (position < mList.size()) {
					Intent intent = new Intent(v.getContext(),
							ProfileActivity.class);
					StatusModel item = new StatusModel();
					UserInfoModel user = mList.get(position);
					intent.putExtra("UserInfoModel", user);
					v.getContext().startActivity(intent);
				}
			}
		});
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
					if (isAllLoaded == false && !mFooter.isLoading()) {
						mFooter.startLoading();
						mFetcher.fetchFollows(mUserInfoModel.userID,
								mUserInfoModel.nickName,
								type == TYPE_FOLLOWERS ? 0 : 1, 30, mList
										.size() + 1, onFetchListener);
					}
				} else if (firstVisibleItem + visibleItemCount < totalItemCount) {
					lastViewVisible = false;
				}
			}
		});
	}

	private void bindView() {
		Bundle extras = getIntent().getExtras();
		if (extras == null)
			finish();
		mUserInfoModel = (UserInfoModel) extras
				.getSerializable("UserInfoModel");
		type = extras.getInt("Type", 0);
		boolean isFromNotice = extras.getBoolean("IsFromNotice", false);
		if (isFromNotice) {
			WeiboBaseTool.getInstance().resetUnRead(UNREAD_TYPE.FOLLOWER, null);
		}
		String userName = mUserInfoModel.nickName;
		if (StringUtils.isNullOrEmpty(userName)) {
			throw new NullPointerException("User Name Can Not Be NULL!");
		}

		if (type == TYPE_FOLLOWERS) {
			setTitle(userName + "·粉丝");
		} else if (type == TYPE_FOLLOWING) {
			setTitle(userName + "·关注");
		}
		mFetcher.fetchFollows(mUserInfoModel.userID, mUserInfoModel.nickName,
				type == TYPE_FOLLOWERS ? 0 : 1, 30, 0, onFetchListener);
	}

	private FetchCompleteListener onFetchListener = new FetchCompleteListener() {
		@Override
		public void fetchComplete(int state, int code, Object obj) {
			switch (state) {
			case BaseFetcher.FETCH_NOT_NETWORK:
			case BaseFetcher.FETCH_AUTH_FAILED:
				ToastHelper.show((String) obj, 2);
				mFooter.stopLoading("-FAILED-");
				break;
			case BaseFetcher.FETCH_EMPTY:
				mFooter.stopLoading("-ZERO-");
				break;
			case BaseFetcher.FETCH_SUCCEED_NEWS:
				mList.clear();
				mList.addAll((ArrayList<UserInfoModel>) obj);
				userAdapter.notifyDataSetChanged();
				break;
			case BaseFetcher.FETCH_SUCCEED_MORE:
				mList.addAll((ArrayList<UserInfoModel>) obj);
				userAdapter.notifyDataSetChanged();
			default:
				break;
			}
			if (state == BaseFetcher.FETCH_SUCCEED_NEWS
					|| state == BaseFetcher.FETCH_SUCCEED_MORE) {
				if (((ArrayList<UserInfoModel>) obj).size() < 10) {
					mFooter.stopLoading("-END-");
					isAllLoaded = true;
				} else {
					mFooter.stopLoading("-MORE-");
				}
			}
		}
	};

	private BaseAdapter userAdapter = new BaseAdapter() {
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ItemView itemView;
			if (convertView == null) {
				itemView = new ItemView();
				convertView = LayoutInflater.from(FriendListActivity.this)
						.inflate(R.layout.listitem_users, null);
				itemView.imageAvatar = (AvatarView) convertView
						.findViewById(R.id.image_users_item_avatar);
				itemView.textName = (TextView) convertView
						.findViewById(R.id.text_users_item_name);
				convertView.setTag(itemView);
			} else {
				itemView = (ItemView) convertView.getTag();
			}
			UserInfoModel user = mList.get(position);
			itemView.imageAvatar.setItem(user);
			// itemView.imageAvatar.setImageResource(R.drawable.avatar_default);
			// mDrawableMgr.loadBitmap(user.iconURL, itemView.imageAvatar,
			// true);
			AppConfig.sImageFetcher.loadImage(user.iconURL,
					itemView.imageAvatar, R.drawable.avatar_default);
			itemView.textName.setText(user.nickName);
			return convertView;
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

		class ItemView {
			public AvatarView imageAvatar;
			public TextView textName;
		}
	};
}
