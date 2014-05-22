package com.gnod.geekr.ui.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.app.adapter.SpinnerAdapter;
import com.gnod.geekr.app.adapter.TimelineAdapter;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.tool.LaunchHelper;
import com.gnod.geekr.tool.ToastHelper;
import com.gnod.geekr.tool.fetcher.BaseFetcher;
import com.gnod.geekr.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.gnod.geekr.tool.fetcher.SearchFetcher;
import com.gnod.geekr.tool.manager.Utils;

public class SearchActivity extends BaseActivity {

	private static final int FETCH_COUNT = 30;
	public static final int TYPE_AT_LIST = 2;
	
	private ListView listView;
	private ArrayList<UserInfoModel> userList = new ArrayList<UserInfoModel>();
	private ArrayList<StatusModel> statusList = new ArrayList<StatusModel>();
	private SearchFetcher sinaWeiboFetcher;
	private EditText editor;
	private MenuItem refreshMenu;
	protected int mSearchType;
	private View mSearchBtn;
	private TimelineAdapter statusAdapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		sinaWeiboFetcher = new SearchFetcher();
		
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
		getSupportMenuInflater().inflate(R.menu.menu_search, menu);
		refreshMenu = menu.findItem(R.id.menu_searchitem);
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
		listView = (ListView)findViewById(R.id.view_search_list);
		mSearchBtn = findViewById(R.id.btn_search);
		listView.setAdapter(userAdapter);
		statusAdapter = new TimelineAdapter(this, statusList);
		
		editor = (EditText)findViewById(R.id.view_search_edittext);
	}
	
	private void bindListener() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(userList.size() == 0|| position >= userList.size())
					return;
				if(mSearchType == 0) {
					UserInfoModel user = userList.get(position);
					LaunchHelper.startProfileActivity(view.getContext(), user);
				} else {
					StatusModel item = statusList.get(position);
					LaunchHelper.startDetailActivity(view.getContext(), item);
				}
			}
		});
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_FLING) {
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
				// TODO Auto-generated method stub
				
			}
		});
		mSearchBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = editor.getText().toString();
				if(text.trim().length() == 0) {
					ToastHelper.show("搜索内容为空", 2);
					return;
				}
				if(mSearchType == 0) {
					sinaWeiboFetcher.fetchUsers(text, 20, onFetchUsers);
					setRefreshing(true);
				} else {
					sinaWeiboFetcher.fetchStatuses(text, 20, onFetchStatuses);
					setRefreshing(true);
				}
			}
		});
	}
	
	private void bindView() {
		final String[] searchType = new String[] {
				"搜索用户·Beta", "搜索微博·Beta"};
		SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this, searchType);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				if(mSearchType == itemPosition)
					return false;
				mSearchType = itemPosition;
				editor.setHint(searchType[itemPosition]);
				if(itemPosition == 0) {
					listView.setAdapter(userAdapter);
				} else {
					listView.setAdapter(statusAdapter);
				}
				return true;
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

	private FetchCompleteListener onFetchUsers = new FetchCompleteListener() {
		@Override
		public void fetchComplete(int state, int code, Object obj) {
				setRefreshing(false);
				if(editor.length() == 0){
					return;
				};
				switch (state) {
				case BaseFetcher.FETCH_NOT_NETWORK:
				case BaseFetcher.FETCH_AUTH_FAILED:
					ToastHelper.show((String) obj, 2);
					break;
				case BaseFetcher.FETCH_EMPTY:
					break;
				case BaseFetcher.FETCH_SUCCEED_NEWS:
					userList.clear();
					userList.addAll( (ArrayList<UserInfoModel>)obj);
					userAdapter.notifyDataSetChanged();
					break;
				default:
					break;
				}
		}
	};
	
	private FetchCompleteListener onFetchStatuses = new FetchCompleteListener() {
		@Override
		public void fetchComplete(int state, int code, Object obj) {
				setRefreshing(false);
				if(editor.length() == 0){
					return;
				};
				switch (state) {
				case BaseFetcher.FETCH_NOT_NETWORK:
				case BaseFetcher.FETCH_AUTH_FAILED:
					ToastHelper.show((String) obj, 2);
					break;
				case BaseFetcher.FETCH_EMPTY:
					break;
				case BaseFetcher.FETCH_SUCCEED_NEWS:
					statusList.clear();
					statusList.addAll( (ArrayList<StatusModel>)obj);
					statusAdapter.notifyDataSetChanged();
					break;
				default:
					break;
				}
		}
	};
	
	private BaseAdapter userAdapter = new BaseAdapter() {
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ItemView itemView;
			if(convertView == null) {
				itemView = new ItemView();
				convertView = LayoutInflater.from(SearchActivity.this).inflate(R.layout.listitem_search, null);
				itemView.textName = (TextView) convertView.findViewById(R.id.text_search_item_username);
				convertView.setTag(itemView);
			} else {
				itemView = (ItemView) convertView.getTag();
			}
			UserInfoModel user = userList.get(position);
			itemView.textName.setText(user.nickName);
			return convertView;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public Object getItem(int position) {
			return userList.get(position);
		}
		
		@Override
		public int getCount() {
			return userList.size();
		}
		
		class ItemView {
			public TextView textName;
		}
	};
	
	
}
