package com.gnod.geekr.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.ToastHelper;
import com.gnod.geekr.tool.fetcher.BaseFetcher;
import com.gnod.geekr.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.gnod.geekr.tool.fetcher.NoticeFetcher;
import com.gnod.geekr.tool.fetcher.SearchFetcher;
import com.gnod.geekr.tool.manager.DrawableManager;
import com.gnod.geekr.widget.ListViewFooter;

public class AtUserActivity extends BaseActivity {

	private static final int FETCH_COUNT = 30;
	public static final int TYPE_FOLLOWERS = 0;
	public static final int TYPE_FOLLOWING = 1;
	public static final int TYPE_AT_LIST = 2;
	
	private ListView mListView;
	private ArrayList<UserInfoModel> mList = new ArrayList<UserInfoModel>();
	private SearchFetcher mFetcher;
	private ListViewFooter mFooter;
	private EditText mEditor;
	private MenuItem mRefreshMenu;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_atsearch);
		mFetcher = new SearchFetcher();
		
		initView();
		bindListener();
		bindView();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_search, menu);
		mRefreshMenu = menu.findItem(R.id.menu_searchitem);
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

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initView() {
		mListView = (ListView)findViewById(R.id.view_atsearch_list);
		mFooter = new ListViewFooter(this);
		mListView.addFooterView(mFooter);
		mFooter.stopLoading("");
		mListView.setAdapter(userAdapter);
		
		mEditor = (EditText)findViewById(R.id.view_atsearch_edittext);
	}
	
	private void bindListener() {
		mEditor.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				if(s.length() == 0){
					mList.clear();
					mFooter.stopLoading("");
					userAdapter.notifyDataSetChanged();
					return;
				}
				mFetcher.fetchAtUsers(s.toString(), 20, onFetchListener);
				mFooter.startLoading();
				setRefreshing(true);
			}
		});
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(mList.size() == 0|| position >= mList.size())
					return;
				Intent intent = new Intent();
				UserInfoModel user = mList.get(position);
				intent.putExtra("Uid", user.userID);
				intent.putExtra("Name", user.nickName);
				AtUserActivity.this.setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
	
	private void bindView() {
		Intent intent = getIntent();
		String type = intent.getStringExtra("Type");
		if(type.equalsIgnoreCase("AtUser")) {
			setTitle("提到");
		} else if(type.equalsIgnoreCase("Special")){
			setTitle("选择");
		}
	}
	
	private void setRefreshing(boolean checked) {
		if(mRefreshMenu != null){
			if(checked)
				mRefreshMenu.setActionView(R.layout.layout_loading);
			else 
				mRefreshMenu.setActionView(null);
		}
	}

	private FetchCompleteListener onFetchListener = new FetchCompleteListener() {
		@Override
		public void fetchComplete(int state, int code, Object obj) {
				setRefreshing(false);
				if(mEditor.length() == 0){
					mFooter.stopLoading("");
					return;
				};
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
					mList.addAll( (ArrayList<UserInfoModel>)obj);
					userAdapter.notifyDataSetChanged();
					mFooter.stopLoading("-END-");
					break;
				default:
					mFooter.stopLoading("");
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
				convertView = LayoutInflater.from(AtUserActivity.this).inflate(R.layout.listitem_search, null);
				itemView.textName = (TextView) convertView.findViewById(R.id.text_search_item_username);
				convertView.setTag(itemView);
			} else {
				itemView = (ItemView) convertView.getTag();
			}
			UserInfoModel user = mList.get(position);
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
			public TextView textName;
		}
	};
}
