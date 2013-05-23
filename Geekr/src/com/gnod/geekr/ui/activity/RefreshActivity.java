package com.gnod.geekr.ui.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gnod.geekr.R;
import com.gnod.geekr.tool.fetcher.BaseFetcher;
import com.gnod.geekr.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.gnod.geekr.tool.fetcher.NoticeFetcher;
import com.gnod.geekr.widget.ColorToast;
import com.gnod.geekr.widget.ColorToast.ToastColor;
import com.gnod.geekr.widget.ListViewFooter;

public abstract class RefreshActivity<T> extends BaseActivity {

	public static final int DEFAULT_COUNT = 20;
	
	private boolean isAllLoaded = false;
	
	private MenuItem mRefresh;
	private ListViewFooter mFooter;
	private ColorToast mToastTop;
	private ListView mListView;
	private int mPage;

	private View mHeadView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_refreshlist);
		initFetcher();
		initView();
		bindView();
		bindListener();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_refresh, menu);
		mRefresh = menu.findItem(R.id.menu_refresh);
		setRefreshing(true);
		mPage = 1;
		//初始化加载10条，提高加载速度
		fetchDatas(10, mPage);
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
			mFooter.startLoading();
			isAllLoaded = false;
			mPage = 1;
			fetchDatas(DEFAULT_COUNT, mPage);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setRefreshing(boolean checked) {
		if(mRefresh != null){
			if(checked)
				mRefresh.setActionView(R.layout.layout_loading);
			else 
				mRefresh.setActionView(null);
		}
	}

	private void initView() {
		mListView = (ListView)findViewById(R.id.view_refresh_list);
		mToastTop = (ColorToast)findViewById(R.id.layout_listview_top);

		mHeadView = getHeadView();
		if(mHeadView != null) 
			mListView.addHeaderView(mHeadView);
		
		mFooter = getFootView();
		if(mFooter != null)
			mListView.addFooterView(mFooter);
		initAdapter();
		mListView.setAdapter(getAdapter());
	}
	

	private View getHeadView() {
		return null;
	}

	protected ListViewFooter getFootView() {
		return new ListViewFooter(this);
	}

	public abstract void initFetcher();
	public abstract void initAdapter();
	public abstract ListAdapter getAdapter();
	public abstract int getListSize();
	public abstract void bindView();
	
	public abstract void fetchDatas(int count, int page);
	public abstract void onFetchSucceed(int state, ArrayList<T> resultList);
	
	public void onLastItemVisible() {
		if(isAllLoaded == false && !mFooter.isLoading()) {
			setRefreshing(true);
			mFooter.startLoading();

			fetchDatas(DEFAULT_COUNT, ++ mPage);
		}
	}
	
	public FetchCompleteListener getFetchListener() {
		return onFetchListener;
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
				if(getListSize() == 0)
					return;
				if(firstVisibleItem + visibleItemCount >= totalItemCount && 
						!lastViewVisible) {
					lastViewVisible = true;
					onLastItemVisible();
				} else if(firstVisibleItem + visibleItemCount < totalItemCount) {
					lastViewVisible = false;
				}
			}
		});
		
		setItemClckListener();
		setItemLongClickListener();
		
	}
	
	protected void setItemClckListener() {
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if((getHeadView() != null && position == 0) ||
						position >= getListSize()) {
					return;
				}
				if(getHeadView() != null)
					-- position;
				onItemClicked(view, position, id);
			}
		});
	}
	
	protected void setItemLongClickListener() {
		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if((getHeadView() != null && position == 0) ||
						position >= getListSize()) {
					return false;
				}
				if(getHeadView() != null)
					-- position;
				onItemLongClicked(view, position, id);
				return false;
			}
		});
	}
	

	protected void onItemClicked(View view, int position, long id){
		
	}
	protected void onItemLongClicked(View view, int position, long id) {
	}

	private FetchCompleteListener onFetchListener = new FetchCompleteListener() {
		@SuppressWarnings("unchecked")
		@Override
		public void fetchComplete(int state, int code, Object obj) {
			setRefreshing(false);
			switch (state) {
			case BaseFetcher.FETCH_NOT_NETWORK:
			case BaseFetcher.FETCH_AUTH_FAILED:
				mToastTop.show((String) obj, ToastColor.RED);
				mFooter.stopLoading("-FAILED-");
				break;
			case BaseFetcher.FETCH_EMPTY:
				mFooter.stopLoading("-NO STATUS-");
				break;
			case BaseFetcher.FETCH_FAILED:
				mFooter.stopLoading("");
				break;
			case BaseFetcher.FETCH_SUCCEED_NEWS:
			case BaseFetcher.FETCH_SUCCEED_MORE:
				ArrayList<T> resultList = (ArrayList<T>)obj;
				onFetchSucceed(state, resultList);
				if (state == NoticeFetcher.FETCH_SUCCEED_NEWS)
					mListView.setSelection(0);
				if(resultList.size() < 5) {
					isAllLoaded = true;
					mFooter.stopLoading("-END-");
				} else {
					mFooter.stopLoading("-MORE-");
				}
				break;
			default:
				mFooter.stopLoading("");
			}
		}
	};
}
