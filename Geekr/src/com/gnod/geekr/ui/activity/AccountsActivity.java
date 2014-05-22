package com.gnod.geekr.ui.activity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.model.AccountModel;
import com.gnod.geekr.tool.manager.AccountManager;
import com.gnod.geekr.tool.manager.DrawableManager;
import com.gnod.geekr.tool.manager.Utils;
import com.gnod.geekr.widget.AvatarView;

public class AccountsActivity extends BaseActivity {

	private AppConfig appConfig;
	
	private ListView statusListView;
	private DrawableManager drawableManager;
	private ArrayList<AccountModel> accounts;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accounts);
		appConfig = (AppConfig) getApplication();
		drawableManager = AppConfig.getDrawableManager();
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
		getSupportMenuInflater().inflate(R.menu.menu_account, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_accounts_add:
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initView() {
		statusListView = (ListView)findViewById(R.id.view_accounts_list);
	}
	
	private void bindListener() {
		statusListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position >= accounts.size())
					return;
				AccountManager.setActivityIndex(position);
				Intent intent = new Intent(AccountsActivity.this, 
						TimeLineActivity.class);
				startActivity(intent);
			}
		});
		statusListView.setOnItemLongClickListener(longClickListener);
		statusListView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
	}
	
	private void bindView() {
		setTitle("帐号管理"  );
		
		accounts = AccountManager.getAccounts();
		statusListView.setAdapter(accountsAdapter);
	}
	
	private OnItemLongClickListener longClickListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			int itemType = ExpandableListView.getPackedPositionType(id);
			if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
				final int groupPos = ExpandableListView.getPackedPositionGroup(id);

				AlertDialog.Builder builder = new AlertDialog.Builder(AccountsActivity.this);
				builder.setMessage("删除用户");
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AccountManager.removeAccount(groupPos);
						accounts = AccountManager.getAccounts();
						accountsAdapter.notifyDataSetChanged();
						dialog.dismiss();
					}
				});
				builder.create().show();
			}
			
			return false;
		}
	};


	private BaseAdapter accountsAdapter = new BaseAdapter() {
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public Object getItem(int position) {
			return accounts.get(position);
		}
		
		@Override
		public int getCount() {
			return accounts.size();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ItemView itemView;
			if(convertView == null) {
				convertView = LayoutInflater.from(appConfig).inflate(R.layout.listitem_accounts, null);
				itemView = new ItemView();
				itemView.avatar = (AvatarView) convertView.findViewById(R.id.image_account_avatar);
				itemView.verifiedImage = (ImageView) convertView.findViewById(R.id.image_account_verified);
				itemView.textName = (TextView) convertView.findViewById(R.id.text_account_name);
				itemView.textType = (TextView) convertView.findViewById(R.id.text_account_type);
				itemView.imageSelected = (ImageView) convertView.findViewById(R.id.image_accout_selected);
				convertView.setTag(itemView);
			} else {
				itemView = (ItemView) convertView.getTag();
			}
			bindItemView(itemView, position);
			return convertView;
		}

		private void bindItemView(ItemView view, int position) {
			AccountModel account = accounts.get(position);
			view.textName.setText(account.name);
			view.textType.setText(account.getTypeName());
			view.imageSelected.setVisibility(View.GONE);
			view.avatar.setImageResource(R.drawable.avatar_default);
//			drawableManager.loadAvatar(account.iconURL, view.avatar, true);
			AppConfig.sImageFetcher.loadImage(
					account.iconURL, view.avatar,
					R.drawable.avatar_default);
		}
	};

	public class ItemView {
		public AvatarView avatar;
		public ImageView verifiedImage;
		public TextView textName;
		public TextView textType;
		
		public ImageView imageSelected;
	}
}
