package com.gnod.geekr.ui.activity;

import java.util.ArrayList;
import java.util.Random;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.app.adapter.ProfileTimelineAdapter;
import com.gnod.geekr.model.AccountModel;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.tool.GeekrTool;
import com.gnod.geekr.tool.LaunchHelper;
import com.gnod.geekr.tool.WeiboBaseTool;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.ToastHelper;
import com.gnod.geekr.tool.fetcher.BaseFetcher;
import com.gnod.geekr.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.gnod.geekr.tool.fetcher.NoticeFetcher;
import com.gnod.geekr.tool.fetcher.ProfileFetcher;
import com.gnod.geekr.tool.manager.AccountManager;
import com.gnod.geekr.tool.manager.DrawableManager;
import com.gnod.geekr.widget.ColorToast;
import com.gnod.geekr.widget.ColorToast.ToastColor;
import com.gnod.geekr.widget.GeekrPanel;
import com.gnod.geekr.widget.ListViewFooter;
import com.gnod.geekr.widget.ParallaxScollListView;

public class ProfileActivity extends BaseActivity {

	private ProfileTimelineAdapter userStatusAdapter;
	private ParallaxScollListView statusListView;
	private ProfileFetcher mProfileFetcher;
	private DrawableManager drawManager;
	private ArrayList<StatusModel> mList = new ArrayList<StatusModel>();
	private View layoutUserInfo;
	private UserHeadView headView;
	private boolean isSelfInfo = false;
	
	private boolean allLoaded = false;
	private UserInfoModel user;
	
	private int[] backgrondIds = {
			R.drawable.header_photo_001,
			R.drawable.header_photo_002,
			R.drawable.header_photo_003,
			R.drawable.header_photo_004,
			R.drawable.header_photo_005,
			R.drawable.header_photo_006,
			R.drawable.header_photo_007,
			R.drawable.header_photo_008,
			R.drawable.header_photo_009
	};
	private MenuItem refresh;
	private ListViewFooter footer;
	private ColorToast toastTop;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		mProfileFetcher = new ProfileFetcher();
		drawManager = AppConfig.getDrawableManager();
		
		Intent intent = this.getIntent();
		user = (UserInfoModel)intent.getSerializableExtra("UserInfoModel");
		if(user == null)
			finish();
		if(!StringUtils.isNullOrEmpty(intent.getStringExtra("IsSelfInfo"))){
			isSelfInfo = true;
		}
		
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
		refresh = menu.findItem(R.id.menu_refresh);
		setRefreshing(true);
		footer.startLoading();
		fetchView();
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
			footer.startLoading();
			allLoaded = false;
			fetchView();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setRefreshing(boolean checked) {
		if(refresh != null){
			if(checked)
				refresh.setActionView(R.layout.layout_loading);
			else 
				refresh.setActionView(null);
		}
	}
	
	private void initView() {
		statusListView = (ParallaxScollListView)findViewById(R.id.view_profile_list);
		toastTop = (ColorToast)findViewById(R.id.view_profile_top);
		
		headView = new UserHeadView();
		layoutUserInfo = LayoutInflater.from(this).inflate(R.layout.listitem_profile_header, null);

		headView.layoutContent = layoutUserInfo.findViewById(R.id.layout_profile_frame);
		headView.imageBackground = (ImageView)layoutUserInfo.findViewById(R.id.image_profile_bg);
		headView.imageAvatar = (ImageView)layoutUserInfo.findViewById(R.id.image_profile_avatar);
		headView.imageVerified = (ImageView)layoutUserInfo.findViewById(R.id.image_profile_verified);
		headView.imageGender = (ImageView)layoutUserInfo.findViewById(R.id.image_profile_gender);
		headView.textName = (TextView) layoutUserInfo.findViewById(R.id.text_profile_name);
		headView.textLocation = (TextView)layoutUserInfo.findViewById(R.id.text_profile_location);
		headView.layoutDescription = layoutUserInfo.findViewById(R.id.layout_profile_description);
		headView.textDescription = (TextView)layoutUserInfo.findViewById(R.id.text_profile_description);
		headView.textverifiedReason = (TextView)layoutUserInfo.findViewById(R.id.text_profile_verified_reason);
		headView.textFollowersCount = (TextView)layoutUserInfo.findViewById(R.id.text_profile_followers_count);
		headView.textFriendsCount = (TextView)layoutUserInfo.findViewById(R.id.text_profile_friends_count);
		headView.textStatusesCount = (TextView)layoutUserInfo.findViewById(R.id.text_profile_statuses_count);
		headView.layoutBtnView = layoutUserInfo.findViewById(R.id.layout_profile_btn);
		headView.btnFollowState = layoutUserInfo.findViewById(R.id.btn_profile_follow);
		headView.progressIndicate = (ProgressBar)layoutUserInfo.findViewById(R.id.view_profile_follow_progress);
		headView.textFollowState = (TextView)layoutUserInfo.findViewById(R.id.text_profile_followstate);
		
		headView.layoutFansCounts = layoutUserInfo.findViewById(R.id.layout_fans_counts);
		headView.layoutFollowsCounts = layoutUserInfo.findViewById(R.id.layout_follows_counts);
		headView.layoutStatusCounts = layoutUserInfo.findViewById(R.id.layout_status_counts);
		
		footer = new ListViewFooter(this);
		
		statusListView.addHeaderView(layoutUserInfo);
		statusListView.addFooterView(footer);
		userStatusAdapter = new ProfileTimelineAdapter(this, mList);
		userStatusAdapter.setSelfFlag(isSelfInfo);
		userStatusAdapter.setOnPanelItemClickListener(onPanelItemClicked);
		statusListView.setAdapter(userStatusAdapter);
	}
	
	private void bindListener() {
		statusListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			private boolean lastViewVisible = false;
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(mList.size() == 0)
					return;
				if(firstVisibleItem + visibleItemCount >= totalItemCount && 
						!lastViewVisible) {
					lastViewVisible = true;
					if(allLoaded == false && !footer.isLoading()) {
						setRefreshing(true);
						footer.startLoading();
						long maxId = Long.parseLong(mList.get(mList.size() - 1).ID);

						mProfileFetcher.fetchUserStatus(user.userID, 
								user.nickName, 
								0, maxId, 20, 1, onStatusListener);
					}
				} else if(firstVisibleItem + visibleItemCount < totalItemCount) {
					lastViewVisible = false;
				}
			}
		});
	}
	
	
	private GeekrPanel.OnItemClcikListener onPanelItemClicked = new GeekrPanel.OnItemClcikListener() {
		@Override
		public void onClick(final int position, View v) {
			switch (v.getId()) {
			case R.id.sd_btn_fav:
				WeiboBaseTool.getInstance().favoriteStatus(
						mList.get(position).ID);
				break;
			case R.id.sd_btn_copy:
				GeekrTool.copyTextToClipboard(
						mList.get(position).content);
				break;
			case R.id.sd_btn_ret:
				LaunchHelper.startRetweetActivity(v.getContext(), 
						mList.get(position));
				break;
			case R.id.sd_btn_cmt:
				LaunchHelper.startCommentActivity(v.getContext(), 
						mList.get(position));
				break;
			case R.id.sd_btn_del:
				ProfileFetcher mProfileFetcher = new ProfileFetcher();
				StatusModel item = mList.get(position);
				mProfileFetcher.deleteStatus(item.ID, new FetchCompleteListener() {
					@Override
					public void fetchComplete(int state, int errorCode, Object obj) {
						if(state == BaseFetcher.FETCH_SUCCEED_NEWS){
							mList.remove(position);
							userStatusAdapter.notifyDataSetChanged();
							toastTop.show("删除成功", ToastColor.BLUE);
						} else {
							toastTop.show("删除失败", ToastColor.RED);
						}
					}
				});
			default:
				break;
			}
		}
	};
	
	private void bindView() {
		setTitle("个人主页");
		setMagicImage();
//		drawManager.loadAvatar(user.largeIconURL, headView.imageAvatar, false);
		AppConfig.sImageFetcher.loadImage(
				user.largeIconURL, headView.imageAvatar, 
				R.drawable.avatar_default);
	}

	private void setMagicImage() {
		int backIdx = 0;
		if (!StringUtils.isNullOrEmpty(user.userID)) {
			backIdx = (int) Math.abs((Long.parseLong(user.userID))
					% backgrondIds.length);
		} else if (!StringUtils.isNullOrEmpty(user.nickName)) {
			backIdx = Math.abs(user.nickName.hashCode()) % backgrondIds.length;
		} else {
			backIdx = Math.abs(new Random().nextInt()) % backgrondIds.length;
		}
		headView.imageBackground.setImageResource(backgrondIds[backIdx]);
		statusListView.setParallaxImageView(headView.imageBackground);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		statusListView.setViewsBounds(ParallaxScollListView.ZOOM_X2);
	}

	private void fetchView() {
		mProfileFetcher.fetchUserInfo(user.userID, user.nickName,
				onUserInfoListener);
		mProfileFetcher.fetchUserStatus(user.userID, user.nickName, 
				0, 0, 10, 1, onStatusListener);
	}

	private void updateUserView(final UserInfoModel userInfo) {
		if(isSelfInfo) {
			AccountModel account = AccountManager.getActivityAccount();
			account.name = userInfo.nickName;
			account.iconURL = userInfo.iconURL;
			AccountManager.updateActivityAccount(account);
			
			headView.layoutBtnView.setVisibility(View.GONE);
		} else {
			headView.layoutBtnView.setVisibility(View.VISIBLE);
			setFollowState(userInfo.followed);
			
			headView.btnFollowState.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					headView.progressIndicate.setVisibility(View.VISIBLE);
					mProfileFetcher.setFriendships(!userInfo.followed, userInfo.userID, userInfo.nickName, new FetchCompleteListener() {
						@Override
						public void fetchComplete(int state, int code, Object obj) {
							headView.progressIndicate.setVisibility(View.GONE);
							if(state != NoticeFetcher.FETCH_SUCCEED_NEWS) {
								return;
							}
							userInfo.followed = !userInfo.followed;
							setFollowState(userInfo.followed);
						}
					});
				}
			});
			
		}
//		drawManager.loadAvatar(userInfo.largeIconURL, 
//				headView.imageAvatar, false);
		AppConfig.sImageFetcher.loadImage(
				userInfo.largeIconURL, 
				headView.imageAvatar, 
				R.drawable.avatar_default);
		headView.textName.setText(userInfo.nickName);
		
		if(!StringUtils.isNullOrEmpty(userInfo.description)) {
			headView.layoutDescription.setVisibility(View.VISIBLE);
			headView.textDescription.setText(userInfo.description);
		} else {
			headView.layoutDescription.setVisibility(View.GONE);
		}
		
		headView.textLocation.setText(userInfo.location);
		headView.textFollowersCount.setText(userInfo.followersCount);
		headView.layoutFansCounts.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), FriendListActivity.class);
				intent.putExtra("UserInfoModel", userInfo);
				intent.putExtra("Type", FriendListActivity.TYPE_FOLLOWERS);
				v.getContext().startActivity(intent);
			}
		});
		
		headView.textFriendsCount.setText(userInfo.friendsCount);
		headView.layoutFollowsCounts.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), FriendListActivity.class);
				intent.putExtra("UserInfoModel", userInfo);
				intent.putExtra("Type", FriendListActivity.TYPE_FOLLOWING);
				v.getContext().startActivity(intent);
			}
		});
		headView.textStatusesCount.setText(userInfo.statusCount);
		headView.layoutStatusCounts.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mList.size() > 0)
					statusListView.setSelection(1);
			}
		});
		
		headView.textverifiedReason.setText(userInfo.verifiedReason);
		if(!StringUtils.isNullOrEmpty(userInfo.verifiedReason)){
			switch (userInfo.verifiedType) {
			case 0:
				headView.imageVerified.setVisibility(View.VISIBLE);
				headView.imageVerified.setImageResource(R.drawable.userinfo_ic_verified);
				break;
			case 2:
			case 3:
			case 7:
				headView.imageVerified.setVisibility(View.VISIBLE);
				headView.imageVerified.setImageResource(R.drawable.userinfo_ic_verified_blue);
				break;
			case 220:
				headView.imageVerified.setVisibility(View.VISIBLE);
				headView.imageVerified.setImageResource(R.drawable.ic_daren);
				break;
			default:
				headView.imageVerified.setVisibility(View.GONE);
				break;
			}
		}
		
		switch (userInfo.getGender()) {
		case 0:
			headView.imageGender.setImageResource(R.drawable.ic_boy);
			break;
		case 1:
			headView.imageGender.setImageResource(R.drawable.ic_girl);
			break;
		default:
			break;
		}
	}

	private void setFollowState(boolean followed) {
		int bgId;
		String text = "";
		if(followed == true) {
			bgId = R.drawable.bg_grey;
			text = "取消关注";
		} else {
			bgId = R.drawable.bg_blue;
			text = "关注";
		}
		headView.btnFollowState.setBackgroundResource(bgId);
		headView.textFollowState.setText(text);
	}

	private FetchCompleteListener onUserInfoListener = new FetchCompleteListener() {
		@Override
		public void fetchComplete(int state, int code, Object info) {
			switch (state) {
			case NoticeFetcher.FETCH_NOT_NETWORK:
			case NoticeFetcher.FETCH_AUTH_FAILED:
				toastTop.show((String) info, ToastColor.RED);
				break;
			case NoticeFetcher.FETCH_EMPTY:
				break;
			case NoticeFetcher.FETCH_FAILED:
				if(code == 20003) {
					ToastHelper.show("该用户不存在", 2);
					finish();
				}
				break;
			case NoticeFetcher.FETCH_SUCCEED_NEWS:
				if(info != null){
					UserInfoModel userInfo = (UserInfoModel)info;
					if(userInfo.userID.equals(AccountManager.getActivityAccount().uID)){
						isSelfInfo = true;
					}
					updateUserView(userInfo);
				}
				break;
			default:
				break;
			}
		}
	};
	
	private FetchCompleteListener onStatusListener = new FetchCompleteListener() {
		@SuppressWarnings("unchecked")
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
				footer.stopLoading("-NO STATUS-");
				break;
			case NoticeFetcher.FETCH_FAILED:
				footer.stopLoading("");
				break;
			case NoticeFetcher.FETCH_SUCCEED_NEWS:
				ArrayList<StatusModel> lists = (ArrayList<StatusModel>)obj;
				mList.clear();
				mList.addAll(lists);
				userStatusAdapter.notifyDataSetChanged();
				statusListView.setSelection(0);
				if(lists.size() < 5) {
					allLoaded = true;
					footer.stopLoading("-END-");
				} else {
					footer.stopLoading("-MORE-");
				}
				break;
			case NoticeFetcher.FETCH_SUCCEED_MORE:
				ArrayList<StatusModel> resultList = (ArrayList<StatusModel>)obj;
				if(resultList.size() > 1){
					int lastIndex = mList.size() - 1;
					mList.remove(lastIndex);
					mList.addAll(resultList);
					userStatusAdapter.notifyDataSetChanged();
				}
				if(resultList.size() < 5) {
					allLoaded = true;
					footer.stopLoading("-END-");
				} else {
					footer.stopLoading("-MORE-");
				}
				break;
			default:
				footer.stopLoading("");
			}
		}
	};
	public class UserHeadView {
		public View layoutContent;
		public ImageView imageBackground;
		public ImageView imageAvatar;
		public ImageView imageVerified;
		public ImageView imageGender;
		public TextView textName;
		public TextView textLocation;
		public View layoutDescription;
		public TextView textDescription;
		public View layoutBtnView;
		public View btnFollowState;
		public ProgressBar progressIndicate;
		public TextView textFollowState;
		public TextView textverifiedReason;
		public TextView textFollowersCount;
		public TextView textFriendsCount;
		public TextView textStatusesCount;
		
		public View layoutFansCounts;
		public View layoutFollowsCounts;
		public View layoutStatusCounts;
	}
	
}
