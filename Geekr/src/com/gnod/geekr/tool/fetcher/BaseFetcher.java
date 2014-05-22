package com.gnod.geekr.tool.fetcher;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.model.AccountModel;
import com.gnod.geekr.tool.WeiboBaseTool;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.manager.AccountManager;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.net.RequestListener;


public abstract class BaseFetcher {

	public static final int FETCH_FAILED = 0;
	public static final int FETCH_SUCCEED_NEWS = 1;
	public static final int FETCH_SUCCEED_MORE = 2;
	public static final int FETCH_EMPTY = 3;
	public static final int FETCH_NOT_NETWORK = 4;
	public static final int FETCH_AUTH_FAILED = 5;

	public interface FetchCompleteListener
	{
		public void fetchComplete(int state, int errorCode, Object obj);
	}
	
	public AppConfig getConfig() {
		return AppConfig.getInstance();
	}
	
	protected abstract class FetchHandler<T> implements Runnable{
		private FetchCompleteListener mCompleteListener;
		private ArrayList<T> resultList;
		private AccountModel account;
		
		public FetchHandler(FetchCompleteListener listener) {
			this.mCompleteListener = listener;
		}
		
		public FetchHandler(AccountModel account, FetchCompleteListener listener){
			this(listener);
			this.account = account;
		}
		
		public void fetch() {
			Thread thread = new Thread(this);
			thread.start();
		}
		
		@Override
		public void run() {
			if(!getConfig().isNetworkConnected()) {
				Message msg = new Message();
				msg.arg1 = FETCH_NOT_NETWORK;
				msg.obj = "网络连接错误";
				mFriendsTimeLineHandler.sendMessage(msg);
				return;
			}

			WeiboBaseTool tool = WeiboBaseTool.getInstance();
			Oauth2AccessToken oa;
			if(account == null){
				account = AccountManager.getActivityAccount();
			}
			oa = tool.getOauth2AccessToken(account);
			if (oa == null) {
				Message msg = new Message();
				msg.arg1 = FETCH_AUTH_FAILED;
				msg.obj = "验证失败或已过期";
				mFriendsTimeLineHandler.sendMessage(msg);
				return;
			}
			
			resultList = new ArrayList<T>();
			resultList.clear();
			callAPI(oa, mFetchListener);
		}

		public abstract void callAPI(Oauth2AccessToken oa, RequestListener listener);
		public abstract void convertData(String arg0, ArrayList<T> list, Message msg) throws JSONException;
		
		protected void fetchComplete(String arg0) {
			Message msg = new Message();
			msg.arg1 = FETCH_FAILED;
			if(StringUtils.isNullOrEmpty(arg0)) {
				msg.obj = "获取数据失败";
			} else {
				JSONObject object;
				try {
					object = new JSONObject(arg0);
					msg.arg2 = object.optInt("error_code");
					msg.obj = object.optString("error");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			mFriendsTimeLineHandler.sendMessage(msg);
		}
		private RequestListener mFetchListener = new RequestListener() {
			@Override
			public void onIOException(IOException arg0) {
				Log.e("fetch", arg0.getMessage());
				fetchComplete("");
			}
			@Override
			public void onError(WeiboException arg0) {
				Log.e("fetch", arg0.getMessage() + " statusCode:" + arg0.getStatusCode());
				fetchComplete(arg0.getMessage());			
			}
			
			@Override
			public void onComplete(String arg0) {
				Message msg = new Message();
				msg.arg2 = -1;
				try {
					convertData(arg0, resultList, msg);
				} catch (Exception e) {
					Log.e("error", e.getMessage());
					msg.arg1 = FETCH_FAILED;
					msg.obj = "获取数据失败";
				}finally{
					mFriendsTimeLineHandler.sendMessage(msg);
				}
			}
		};
		
		private Handler mFriendsTimeLineHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(mCompleteListener != null) {
					if(msg.arg1 != BaseFetcher.FETCH_NOT_NETWORK &&
							account != AccountManager.getActivityAccount()){
						msg.arg1 = FETCH_FAILED;
					}
					mCompleteListener.fetchComplete(msg.arg1, msg.arg2, msg.obj);
				}
			}
			
		};
	}
}
