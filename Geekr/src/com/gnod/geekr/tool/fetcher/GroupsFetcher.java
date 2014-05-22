package com.gnod.geekr.tool.fetcher;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Message;

import com.gnod.geekr.model.AccountModel;
import com.gnod.geekr.model.GroupsModel;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.tool.converter.WeiboConverter;
import com.gnod.geekr.weibo.api.GroupsAPI;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.api.WeiboAPI.FEATURE;
import com.weibo.sdk.android.net.RequestListener;

public class GroupsFetcher extends BaseFetcher {
	/**
	 * 获取account指定用户的好友分组列表
	 * 
	 * @param  account 为null时默认获取当前activity用户的列表
	 * 
	 */
	public void fetchList(AccountModel account, final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<GroupsModel> handler = new FetchHandler<GroupsModel>(account, listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				GroupsAPI groupsAPI = new GroupsAPI(oa);
				groupsAPI.groupsList(listener);
			}

			@Override
			public void convertData(String arg0, ArrayList<GroupsModel> list, Message msg) 
				throws JSONException{
				JSONObject root = new JSONObject(arg0);
				JSONArray groupsArray = root.optJSONArray("lists");
				if(groupsArray == null || groupsArray.length() == 0) {
					msg.arg1 = FETCH_EMPTY;
					msg.obj = list;
					return;
				}
				for(int i = 0; i < groupsArray.length(); i++ ) {
					JSONObject groups = groupsArray.getJSONObject(i);
					GroupsModel model = new GroupsModel();
					if(WeiboConverter.convertGroupsModel(groups, model)) {
						list.add(model);
					}
				}
				msg.arg1 = FETCH_SUCCEED_NEWS;
				msg.obj = list;
			}
		};
		handler.fetch();
	}
	
	public void fetchStatus(final long list_id, final long since_id, 
			final long max_id, final int count, 
			final int page, FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<StatusModel> handler = new FetchHandler<StatusModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				GroupsAPI groupsAPI = new GroupsAPI(oa);
				groupsAPI.timeline(list_id, since_id, max_id, count, page, false, FEATURE.ALL, listener);
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
				if(max_id == 0) {
					msg.arg1 = FETCH_SUCCEED_NEWS;
				} else {
					msg.arg1 = FETCH_SUCCEED_MORE;
				}
				msg.obj = list;
			}
		};
		handler.fetch();
	}
}
