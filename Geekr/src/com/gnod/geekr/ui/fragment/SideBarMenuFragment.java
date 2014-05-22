package com.gnod.geekr.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.app.adapter.SidebarMenuAdapter;
import com.gnod.geekr.app.adapter.item.SidebarItem;
import com.gnod.geekr.model.AccountModel;
import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.tool.manager.AccountManager;
import com.gnod.geekr.tool.manager.DrawableManager;
import com.gnod.geekr.ui.activity.AccountsActivity;
import com.gnod.geekr.ui.activity.CommentsActivity;
import com.gnod.geekr.ui.activity.FavoritesActivity;
import com.gnod.geekr.ui.activity.ProfileActivity;
import com.gnod.geekr.ui.activity.SearchActivity;
import com.gnod.geekr.ui.activity.SettingActivity;
import com.gnod.geekr.widget.AvatarView;

public class SideBarMenuFragment extends Fragment {

	public static final int TYPE_WEIBO_STATUS = 0;
	public static final int TYPE_WEIBO_AT = 1;
	public static final int TYPE_WEIBO_COMMENT = 2;
	public static final int TYPE_WEIBO_PROFILE = 3;
	public static final int TYPE_WEIBO_FAVORITE = 4;
	public static final int TYPE_WEIBO_SEARCH = 5;
	
	private SidebarMenuAdapter mAdapter;
	private View mBtnSetting;
	private OnItemClickListener mOnItemClickListener;
	private AccountView mAccountView;
	private DrawableManager mDrawableMgr;
	private ListView mListView;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.sidebar_list, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mDrawableMgr = AppConfig.getDrawableManager();
		
		initView();
		bindView();
		bindListener();
		
		mBtnSetting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), SettingActivity.class);
				startActivity(intent);
			}
		});
	}
	
	private void bindListener() {
		mAccountView.layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), AccountsActivity.class);
				startActivity(intent);
			}
		});
		mListView.setOnItemClickListener(itemClicked);
		
	}

	private int[] itemIds = new int[]{
			TYPE_WEIBO_STATUS, TYPE_WEIBO_AT, 
			TYPE_WEIBO_COMMENT, TYPE_WEIBO_FAVORITE,
			TYPE_WEIBO_SEARCH, TYPE_WEIBO_PROFILE
	};
	
	private String[] itemNames = new String[] {
			"首        页", "@        我", 
			"评        论", "收        藏",
			"搜        索", "个人主页"
	};
	
	private int[] itemIcons = new int[] {
			R.drawable.radio_timeline, R.drawable.radio_mention,
			R.drawable.radio_comments, R.drawable.radio_favorite,
			R.drawable.radio_search, R.drawable.radio_profile
	};
	
	private void initView() {
		mAccountView = new AccountView();
		mAccountView.layout = findViewById(R.id.fragment_account);
		mAccountView.avatar = (AvatarView) findViewById(R.id.fragment_account_avatar);
		mAccountView.verifiedImage = (ImageView) findViewById(R.id.fragment_account_verified_image);
		mAccountView.textName = (TextView) findViewById(R.id.fragment_account_name);
		mAccountView.textType = (TextView)findViewById(R.id.fragement_account_type);
		
		mListView = (ListView)findViewById(R.id.account_list);
		mBtnSetting = findViewById(R.id.layout_menu_setting);
	}
	
	private void bindView() {
		AccountModel account = AccountManager.getActivityAccount();
		setAccount(account);
		
		mAdapter = new SidebarMenuAdapter(getActivity());
		for(int i = 0; i < itemIds.length; i ++) {
			mAdapter.addGroup(new SidebarItem(itemIds[i], itemNames[i], itemIcons[i]));
		}
		mListView.setAdapter(mAdapter);
	}
	
	
	private void setAccount(AccountModel account) {
		mAccountView.textName.setText(account.name);
		mAccountView.textType.setText(account.getTypeName());
//		mDrawableMgr.loadAvatar(account.iconURL, mAccountView.avatar, true);
		AppConfig.sImageFetcher.loadImage(
				account.iconURL, mAccountView.avatar, 
				R.drawable.avatar_default);
	}


	private View findViewById(int id){
		return getView().findViewById(id);
	}
	
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mOnItemClickListener = listener;
	}
	
	private AdapterView.OnItemClickListener itemClicked = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View v,
				int position, long id) {
			switch (itemIds[position]) {
			case TYPE_WEIBO_STATUS:
			case TYPE_WEIBO_AT:
				if(mOnItemClickListener != null)
					mOnItemClickListener.onClick(v, position);
				break;
			case TYPE_WEIBO_COMMENT:
				Intent metIntent = new Intent(getActivity(), CommentsActivity.class);
				getActivity().startActivity(metIntent);
				break;
			case TYPE_WEIBO_PROFILE:
				Intent intent = new Intent(getActivity(), ProfileActivity.class);
				AccountModel account = AccountManager.getActivityAccount();
				UserInfoModel user = new UserInfoModel();
				user.userID = account.uID;
				user.nickName = account.name;
				user.iconURL = account.iconURL;
				
				intent.putExtra("UserInfoModel", user);
				intent.putExtra("IsSelfInfo", "Self");
				getActivity().startActivity(intent);
				break;
			case TYPE_WEIBO_FAVORITE:
				Intent favIntent = new Intent(getActivity(), FavoritesActivity.class);
				getActivity().startActivity(favIntent);
				break;
			case TYPE_WEIBO_SEARCH:
				Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
				getActivity().startActivity(searchIntent);
				break;
			}
		}
	};
	
	public interface OnItemClickListener {
		public void onClick(View v, int position);
	}
	
	public class AccountView {
		public View layout;
		public AvatarView avatar;
		public ImageView verifiedImage;
		public TextView textName;
		public TextView textType;
	}
}
