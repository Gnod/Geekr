package com.gnod.geekr.tool.fetcher;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Message;
import android.util.Log;

import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.converter.WeiboConverter;
import com.gnod.geekr.weibo.api.TopicsAPI;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.SearchAPI;
import com.weibo.sdk.android.api.WeiboAPI.FRIEND_TYPE;
import com.weibo.sdk.android.api.WeiboAPI.RANGE;
import com.weibo.sdk.android.net.RequestListener;

public class SearchFetcher extends BaseFetcher {
	
	/**
	 * 搜索用户时的联想搜索建议
	 * 
	 * @param  account 为null时默认获取当前activity用户的列表
	 * 
	 */
	public void fetchUsers(final String query, final int count, 
			final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<UserInfoModel> handler = new FetchHandler<UserInfoModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				SearchAPI searchAPI = new SearchAPI(oa);
				searchAPI.users(query, count, listener);
			}

			@Override
			public void convertData(String arg0, ArrayList<UserInfoModel> list, Message msg) 
				throws JSONException{
				JSONArray array = new JSONArray(arg0);
				if(array == null || array.length() == 0) {
					msg.arg1 = FETCH_EMPTY;
					return;
				}
				for(int i = 0; i < array.length(); i ++) {
					JSONObject status = array.getJSONObject(i);
					UserInfoModel infoModel = new UserInfoModel();
					infoModel.userID = status.optString("uid");
					infoModel.nickName = status.optString("screen_name");
					list.add(infoModel);
				}
				msg.arg1 = FETCH_SUCCEED_NEWS;
				msg.obj = list;
			}
		};
		handler.fetch();
	}
	
	/**
	 * 获取 @ 用户时的联想建议
	 */
	public void fetchAtUsers(final String query, final int count, final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<UserInfoModel> handler = new FetchHandler<UserInfoModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				SearchAPI searchAPI = new SearchAPI(oa);
				if(!StringUtils.isNullOrEmpty(query))
					searchAPI.atUsers(query,
							count, FRIEND_TYPE.ATTENTIONS, RANGE.ALL, listener);
			}

			@Override
			public void convertData(String arg0, ArrayList<UserInfoModel> list, Message msg) 
				throws JSONException{
				
				JSONArray array = new JSONArray(arg0);
				if(array == null || array.length() == 0) {
					msg.arg1 = FETCH_EMPTY;
					return;
				}
				for(int i = 0; i < array.length(); i ++) {
					JSONObject status = array.getJSONObject(i);
					UserInfoModel infoModel = new UserInfoModel();
					infoModel.userID = status.optString("uid");
					infoModel.nickName = status.optString("nickname");
					list.add(infoModel);
				}
				msg.arg1 = FETCH_SUCCEED_NEWS;
				msg.obj = list;
			}
		};
		handler.fetch();
	}
	
	/**
	 * 搜索微博时的联想搜索建议
	 */
	public void fetchStatuses(final String query, final int count, 
			final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<StatusModel> handler = new FetchHandler<StatusModel>(listener) {
			@Override
			public void callAPI(final Oauth2AccessToken oa, final RequestListener listener) {
				SearchAPI searchAPI = new SearchAPI(oa);
				searchAPI.statuses(query, 1, new RequestListener() {
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
						JSONArray array;
						try {
							array = new JSONArray(arg0);
							String suggest = array.getJSONObject(0).optString("suggestion");
							TopicsAPI topicAPI = new TopicsAPI(oa);
							topicAPI.topics(suggest, count, 1, listener);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
			}

			@Override
			public void convertData(String arg0, ArrayList<StatusModel> list, Message msg) 
				throws JSONException{
				
				JSONObject root = new JSONObject(arg0);
				JSONArray statusArray = root.optJSONArray("statuses");
				if(statusArray == null || statusArray.length() == 0){
					msg.arg1 = FETCH_EMPTY;
					msg.obj = list;
					return;
				}
				list.clear();
				for(int i = 0; i < statusArray.length(); i++ ) {
					JSONObject status = statusArray.getJSONObject(i);
					StatusModel itemModel = new StatusModel();
					
					WeiboConverter.convertStatusToModel(status, itemModel);
					
					
					if(itemModel != null ) {
						list.add(itemModel);
					}
				}
				msg.arg1 = FETCH_SUCCEED_NEWS;
				msg.obj = list;
			}
		};
		handler.fetch();
	}
}
