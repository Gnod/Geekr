package com.gnod.geekr.ui.activity;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import com.actionbarsherlock.internal.widget.IcsAdapterView;
import com.actionbarsherlock.internal.widget.IcsAdapterView.OnItemSelectedListener;
import com.actionbarsherlock.internal.widget.IcsSpinner;
import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.app.AppManager;
import com.gnod.geekr.app.adapter.SpinnerAdapter;
import com.gnod.geekr.app.adapter.TimelineAdapter;
import com.gnod.geekr.holder.StatusDataHolder;
import com.gnod.geekr.model.AccountModel;
import com.gnod.geekr.model.GroupsModel;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.tool.GeekrTool;
import com.gnod.geekr.tool.LaunchHelper;
import com.gnod.geekr.tool.WeiboBaseTool;
import com.gnod.geekr.tool.ToastHelper;
import com.gnod.geekr.tool.fetcher.BaseFetcher;
import com.gnod.geekr.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.gnod.geekr.tool.fetcher.GroupsFetcher;
import com.gnod.geekr.tool.fetcher.StatusFetcher;
import com.gnod.geekr.tool.manager.AccountManager;
import com.gnod.geekr.tool.manager.GroupsManager;
import com.gnod.geekr.tool.manager.SettingManager;
import com.gnod.geekr.tool.manager.StatusManager;
import com.gnod.geekr.ui.fragment.SideBarMenuFragment;
import com.gnod.geekr.weibo.api.RemindAPI.UNREAD_TYPE;
import com.gnod.geekr.widget.ColorToast;
import com.gnod.geekr.widget.ColorToast.ToastColor;
import com.gnod.geekr.widget.GeekrPanel;
import com.gnod.geekr.widget.PullToRefreshListView;
import com.gnod.geekr.widget.PullToRefreshListView.OnHeadVisualChangeListener;
import com.gnod.geekr.widget.PullToRefreshListView.OnLastItemVisibleListener;
import com.gnod.geekr.widget.PullToRefreshListView.OnRefreshListener;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import com.umeng.update.UmengDownloadListener;
import com.umeng.update.UmengUpdateAgent;

public class TimeLineActivity extends SlidingFragmentActivity implements
		OnRefreshListener, OnLastItemVisibleListener{

	public static final int TYPE_SINA_WEIBO_STATUS = 0;
	public static final int TYPE_SINA_WEIBO_AT = 1;
	public static final int TYPE_SINA_WEIBO_COMMENT = 2;

	public static boolean isShowMenuAnimation = false;
	
	private PullToRefreshListView mListView;
	private TimelineAdapter mListAdapter;
	private StatusFetcher mStatusFetcher;
	private AppConfig mConfig;
	private StatusDataHolder mStatusHolder = new StatusDataHolder();

	private View mDrafboxView;
	private View mProgress;
	private ColorToast mToastTop;
	private ColorToast mToastBottom;
	private IcsSpinner mSpinner;
	private TimeLineSpinAdapter mSpinnerAdapter;
	private GroupsFetcher mGroupsFetcher;
	private SpinnerManager spinnerMgr;
	private View mActionBarBg;
	

	// 由于StatusLineActivity独立继承于SlidingFragmentActivity， 而非继承自BaseActivity
	// 需注意在onCreate与onDestroy方法中调用AppManager相应Activity管理方法
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		// add activity to atctivity's stack
		AppManager.getInstance().addActivity(this);
		SettingManager.registNetworkStateReceiver(this);
		mConfig = (AppConfig) getApplication();
		mStatusFetcher = new StatusFetcher();
		mGroupsFetcher = new GroupsFetcher();
		isShowMenuAnimation = mConfig.isShowMenuAnim();

		checkUpdate();
		initSlidingMenu();
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
	protected void onStop() {
		super.onStop();
		StatusManager.setCacheStatus(mStatusHolder.list);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//unregister network state broadcast.
		SettingManager.unRegistNetworkStateReceiver(this);
		// remove activity from activity's stack
		AppManager.getInstance().finishActivity(this);
	}
	
	/**
	 * 使用umeng实现更新检测
	 */
	private void checkUpdate() {
		UmengUpdateAgent.update(this);
		UmengUpdateAgent.setOnDownloadListener(new UmengDownloadListener(){
		    @Override
		    public void OnDownloadEnd(int result) {
		        ToastHelper.show("download result: " + result);
		    }           
		});
	}
	
	private void initSlidingMenu() {
		setBehindContentView(R.layout.sidebar_frame);
		// customize the SlidingMenu
		FragmentTransaction t = this.getSupportFragmentManager()
				.beginTransaction();
		SideBarMenuFragment menuFragment = new SideBarMenuFragment();
		menuFragment.setOnItemClickListener(mOnMenuItemClicked);
		t.replace(R.id.layout_sidebar_menu, menuFragment);
		t.commit();

		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setFadeDegree(0.35f);
		setSlidingActionBarEnabled(true);
		sm.setBehindScrollScale(0.0f);
		sm.setBehindCanvasTransformer(new CanvasTransformer() {
			@Override
			public void transformCanvas(Canvas canvas, float percentOpen) {
				if (isShowMenuAnimation) {
					float scale = (float) (percentOpen * 0.25 + 0.75);
					canvas.scale(scale, scale, canvas.getWidth() / 2,
							canvas.getHeight() / 2);
					// canvas.scale(percentOpen, 1, 0, 0);
				}
			}
		});
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// 可以说这个操作基本上是为了在初次登录时退出后不至于从这个页面返回时回退到Login界面,
		// 除此之外目前暂时没有过多其余的作用
		if(getSlidingMenu().isMenuShowing() == false) {
			if(StatusManager.mStatusType != 0) {
				StatusManager.mStatusType = 0;
				mSpinnerAdapter.notifyDataSetChanged();
				onPullRefresh();
			} else if(keyCode == KeyEvent.KEYCODE_BACK) {
				AppManager.getInstance().appExit(this);
			} 
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * init 之前必须确保appConfig必须已经初始化.
	 */
	private void initView() {
		mActionBarBg = findViewById(R.id.image_timleline_actionbar);
		mDrafboxView = findViewById(R.id.btn_timeline_draftbox);
		mProgress = findViewById(R.id.view_timeline_progressbar);
		mToastTop = (ColorToast)findViewById(R.id.view_timeline_toast_top);
		mToastBottom = (ColorToast)findViewById(R.id.view_timeline_toast_bottom);
		mListView = (PullToRefreshListView) findViewById(R.id.view_timeline_list);
		mSpinner = (IcsSpinner) findViewById(R.id.view_timeline_spinner);
		
		mSpinnerAdapter = new TimeLineSpinAdapter(this);
		spinnerMgr = new SpinnerManager(mSpinner, mSpinnerAdapter);
		
	}

	private void bindListener() {
		mListView.setOnRefreshListener(this);
		mListView.setOnLastItemVisibleListener(this);
		mListView.setOnHeadVisualChangeListener(onFirstItemVisualChanged);
		
		mDrafboxView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LaunchHelper.startPostActivity(TimeLineActivity.this);
			}
		});
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(IcsAdapterView<?> parent, View view,
					int position, long id) {
				//Activity 启动时会触发一次ItemSelected
				if(StatusManager.mGroupType == position) {
					return;
				}
				StatusManager.mGroupType = position;
				onPullRefresh();
			}
			@Override
			public void onNothingSelected(IcsAdapterView<?> parent) {
			}
		});
		mSpinner.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				spinnerMgr.buildSpinList();
			}
			
		});
	}
	
	private void bindView() {
		boolean isFromNotice = false;
		Bundle extras = getIntent().getExtras();
		StatusManager.mStatusType = 0;
		if(extras != null) {
			StatusManager.mStatusType = extras.getInt("Type", 0);
			isFromNotice = extras.getBoolean("IsFromNotice", false);
			WeiboBaseTool.getInstance().resetUnRead(UNREAD_TYPE.MENTION_STATUS, null);
		}
		
		mStatusHolder.list = StatusManager.getCacheStatus();
		mListAdapter = new TimelineAdapter(this, mStatusHolder, onPanelItemClicked);
		mListView.setAdapter(mListAdapter);
		
		if (mStatusHolder.list.size() == 0 || mConfig.isAutoFetch() || isFromNotice) {
			onPullRefresh();
		}
	}
	
	@Override
	public void onRefresh() {
		if(!isLoading()) {
			long sinceId;
			mStatusHolder.list = StatusManager.getCacheStatus();
			mListAdapter.notifyDataSetChanged();
			if (mStatusHolder.list.size() == 0) {
				sinceId = 0;
			} else {
				sinceId = Long.parseLong(mStatusHolder.list.get(0).ID);
			}
			fetchStatus(StatusManager.mStatusType, sinceId, 0, 20, 1);
		} else {
			mListView.onRefreshComplete();
		}
	}

	@Override
	public void onLastItemVisible(int lastIndex) {
		if (lastIndex <= 0 || mListView.isRefreshing())
			return;
		mProgress.setVisibility(View.VISIBLE);
		long maxId = Long.parseLong(mStatusHolder.list.get(lastIndex).ID);
		fetchStatus(StatusManager.mStatusType, 0, maxId, 20, 1);
	}
	
	public void onPullRefresh() {
		mListView.clickRefresh();
	}
	
	public void fetchStatus(int type, long sinceId, long maxId, int count, int page) {
		// 当删除所有账号时，提示登录
		if (AccountManager.getAccounts().size() == 0) {
			mToastTop.show("请先登录一个帐号", ToastColor.GREY);
			mListView.onRefreshComplete();
			return;
		}
		int position = StatusManager.mGroupType;
		switch (type) {
		case 0:
			if (position < 2)
				mStatusFetcher.fetchStatus(sinceId, maxId, count, page,
						position, new FetchListener(type));
			else {
				mGroupsFetcher.fetchStatus(spinnerMgr.getId(position),
						sinceId, maxId, count, page, new FetchListener(type));
			}
			break;
		case 1:
			mStatusFetcher.fetchAtMe(sinceId, maxId, count, page, position,
					new FetchListener(StatusManager.mStatusType));
			break;
		default:
			break;
		}
	}
	
	public boolean isLoading() {
		return mProgress.getVisibility() == View.VISIBLE;
	}
	
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
	
	private SideBarMenuFragment.OnItemClickListener mOnMenuItemClicked = 
		new SideBarMenuFragment.OnItemClickListener() {
			@Override
			public void onClick(View v, int position) {
				toggle();
				if(StatusManager.mStatusType == position) {
					onPullRefresh();
					return;
				}
				StatusManager.mStatusType = position;
				spinnerMgr.switchListByType(position);
				mSpinnerAdapter.notifyDataSetChanged();
				mSpinner.setSelection(0);
				onPullRefresh();
			}
	};
	
	private class TimeLineSpinAdapter extends SpinnerAdapter {
		private SpinnerManager manager;

		public TimeLineSpinAdapter(Context context) {
			super(context);
		}
		
		public void setManager(SpinnerManager mgr) {
			this.manager = mgr;
		}
		@Override
		public void setItemText(TextView view, int position) {
			if(position != 0) {
				view.setText(((GroupsModel)getList().get(position)).name);
			} else {
				view.setText(StatusManager.mStatusType == 0? "Geekr" : "@"); //Set the title
			}
		}

		@Override
		public void setDropDownItemText(TextView view, int position) {
			view.setText(((GroupsModel)getList().get(position)).name);
		}

		@Override
		public ArrayList getList() {
			return manager.getList();
		}
	}
	
	private class SpinnerManager {
		private GroupsManager groupsMgr;
		private TimeLineSpinAdapter mAdapter;
		private ArrayList<GroupsModel> curSpinListDatas;
		private ArrayList<GroupsModel> mSpinListStatus = new ArrayList<GroupsModel>();
		private ArrayList<GroupsModel> mSpinListAtMe = new ArrayList<GroupsModel>();
		
		private int mType = 0;
		
		public SpinnerManager(IcsSpinner spinner, TimeLineSpinAdapter adapter) {
			mAdapter = adapter;
			initSpinList();
			adapter.setManager(this);
			spinner.setAdapter(adapter);
		}
		
		public void initSpinList(){
			Resources res = getResources();
			String[] spinAtMe = res.getStringArray(R.array.arrays_status_met);
			for(int i = 0; i < spinAtMe.length; i ++) {
				mSpinListAtMe.add(GroupsModel.getModel(i, spinAtMe[i]));
			}
			buildGroupList();
			curSpinListDatas = mSpinListStatus;
		}
		
		/**
		 * Build Spinner List Datas
		 */
		public void buildSpinList() {
			if(mType == 0) {
				buildGroupList();
			}
		}
		
		public void switchListByType(int type) {
			mType = type;
			StatusManager.mGroupType = 0;
			if(type == 0){
				curSpinListDatas = mSpinListStatus;
			} else {
				curSpinListDatas = mSpinListAtMe;
			}
			mAdapter.notifyDataSetChanged();
		}
		
		public ArrayList<GroupsModel> getList() {
			return curSpinListDatas;
		}
		
		public long getId(int pos) {
			return Long.parseLong(mSpinListStatus.get(pos).id);
		}

		private void buildGroupList() {
			if(groupsMgr == null)
				groupsMgr = new GroupsManager();
			final AccountModel account = AccountManager.getActivityAccount();
			ArrayList<GroupsModel> list = groupsMgr.getGroupsList(account);
			
			if(list != null) {
				refreshGroupsListAdapter(list);
			} else {
				refreshGroupsListAdapter(getDefaultGroupsList());
				
				mGroupsFetcher.fetchList(account, new FetchCompleteListener() {
					@Override
					public void fetchComplete(int state, int errorCode, Object obj) {
						if(state == BaseFetcher.FETCH_SUCCEED_NEWS) {
							ArrayList<GroupsModel> newList = getDefaultGroupsList();
							newList.addAll((ArrayList<GroupsModel>)obj);
							groupsMgr.setGroupsList(account, newList);
							if(account == AccountManager.getActivityAccount()){
								refreshGroupsListAdapter(newList);
							}
						}
					}
				});
			}
		}
		
		private ArrayList<GroupsModel> getDefaultGroupsList() {
			Resources res = getResources();
			String[] spinStatus = res.getStringArray(R.array.arrays_status);
			ArrayList<GroupsModel> list = new ArrayList<GroupsModel>();
			for(int i = 0; i < spinStatus.length; i ++) {
				list.add(GroupsModel.getModel(i, spinStatus[i]));
			}
			return list;
		}
		
		private void refreshGroupsListAdapter(ArrayList<GroupsModel> list) {
			mSpinListStatus.clear();
			mSpinListStatus.addAll(list);
			mAdapter.notifyDataSetChanged();
		}
		
	}

	private class FetchListener implements FetchCompleteListener {
		private int fetchType;
		public FetchListener(int fetchType) {
			this.fetchType = fetchType;
		}
		@Override
		public void fetchComplete(int state, int code, Object obj) {
			mProgress.setVisibility(View.GONE);
			mListView.onRefreshComplete();
			
			if(this.fetchType != StatusManager.mStatusType){
				return;
			}
		
			switch(state) {
			case BaseFetcher.FETCH_NOT_NETWORK:
			case BaseFetcher.FETCH_AUTH_FAILED:
				mToastTop.show((String) obj, ToastColor.RED);
				break;
			case BaseFetcher.FETCH_EMPTY:
				mToastTop.show("无消息更新", ToastColor.GREY);
				break;
			case BaseFetcher.FETCH_SUCCEED_NEWS:
				ArrayList<StatusModel> resultList = (ArrayList<StatusModel>) obj;
				mToastTop.show("更新" + resultList.size() + "条新消息", 
						ToastColor.BLUE);

				resultList.addAll(mStatusHolder.list);
				mStatusHolder.list.clear();
				if (resultList.size() > 20) {
					mStatusHolder.list.addAll(resultList.subList(0, 20));
				} else {
					mStatusHolder.list.addAll(resultList);
				}
				StatusManager.setCacheStatus(mStatusHolder.list);
				mListAdapter.notifyDataSetChanged();
				mListView.setSelection(0);
				break;
			case BaseFetcher.FETCH_SUCCEED_MORE:
				ArrayList<StatusModel> lists = (ArrayList<StatusModel>) obj;
				mToastBottom.show("更新" + lists.size() + "条新消息", ToastColor.BLUE);
				// 由于more时，根据当前最后一条id进行fetch，结果中第一条与当前最后一条
				// 出现重复，需除去一个重复item
				int lastIndex = mStatusHolder.list.size() - 1;
				mStatusHolder.list.remove(lastIndex);
				mStatusHolder.list.addAll(lists);
				mListAdapter.notifyDataSetChanged();
				break;
			}
		}
	}
	
	private OnHeadVisualChangeListener onFirstItemVisualChanged = new OnHeadVisualChangeListener() {
		@Override
		public void onVisualChange(boolean visible) {
			mActionBarBg.setBackgroundResource(visible?
					R.drawable.actionbar_bg_black : 
						R.drawable.actionbar_bg_black_transparent);
		}
	};
}
