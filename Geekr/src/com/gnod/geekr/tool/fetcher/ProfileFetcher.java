package com.gnod.geekr.tool.fetcher;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Message;

import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.converter.WeiboConverter;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.api.FriendshipsAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.api.WeiboAPI.FEATURE;
import com.weibo.sdk.android.net.RequestListener;

public class ProfileFetcher extends BaseFetcher{

	/**
	 * 通过用户ID或用户名获得指定用户的个人信息，参数userId，与name只要求至少一个不为空
	 * 即可，但优选使用userId进行用户信息获取
	 * 
	 * @param userId 用户ID
	 * @param name 	用户名
	 */
	public void fetchUserInfo(final String userId, final String name, final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<UserInfoModel> handler = new FetchHandler<UserInfoModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				UsersAPI userAPI = new UsersAPI(oa);
				if(!StringUtils.isNullOrEmpty(userId))
					userAPI.show(Long.parseLong(userId), listener);
				else 
					userAPI.show(name, listener);
			}

			@Override
			public void convertData(String arg0, ArrayList<UserInfoModel> list, Message msg) 
				throws JSONException{
				JSONObject info = new JSONObject(arg0);
				if(info == null){
					msg.arg1 = FETCH_EMPTY;
					return;
				}
				UserInfoModel userInfo = new UserInfoModel();
				WeiboConverter.convertUserInfoToModel(info, userInfo);
				msg.arg1 = FETCH_SUCCEED_NEWS;
				msg.obj = userInfo;
			}
		};
		handler.fetch();
	}
	
	/**
	 * 获取userId或name指定的用户的微博
	 * 
	 */
	public void fetchUserStatus(final String userId, final String name, final long since_id, final long max_id, final int count, final int page, final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<StatusModel> handler = new FetchHandler<StatusModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				StatusesAPI statusesAPI = new StatusesAPI(oa);
				
				if(!StringUtils.isNullOrEmpty(userId))
					statusesAPI.userTimeline(Long.parseLong(userId),since_id, 
							max_id, count, page, false, FEATURE.ALL, false, listener);
				else 
					statusesAPI.userTimeline(name, since_id, max_id, count, page, 
							false, FEATURE.ALL, false, listener);
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
				for(int i = 0; i < statusArray.length(); i++ ) {
					JSONObject status = statusArray.getJSONObject(i);
					StatusModel itemModel = new StatusModel();
					if(WeiboConverter.convertStatusToModel(status, itemModel)) {
						list.add(itemModel);
					}
				}
				if( max_id == 0) {
					msg.arg1 = FETCH_SUCCEED_NEWS;
				} else {
					msg.arg1 = FETCH_SUCCEED_MORE;
				}
				msg.obj = list;
			}
		};
		handler.fetch();
	}
	
	/**
	 * @param type 0:粉丝, 1:关注者
	 * 
	 */
	public void fetchFollows(final String userId, final String name, final int type,
			final int count, final int cursor, final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");
		FetchHandler<UserInfoModel> handler = new FetchHandler<UserInfoModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				FriendshipsAPI friendshipAPI = new FriendshipsAPI(oa);
				switch (type) {
				case 0:
					if(!StringUtils.isNullOrEmpty(userId)){
						friendshipAPI.followers(Long.parseLong(userId), count, cursor, false, listener);
					} else {
						friendshipAPI.followers(name, count, cursor, false, listener);
					}
					break;
				case 1:
					if(!StringUtils.isNullOrEmpty(userId)){
						friendshipAPI.friends(Long.parseLong(userId), count, cursor, false, listener);
					} else {
						friendshipAPI.friends(name, count, cursor, false, listener);
					}
					break;
				default:
					break;
				}
			}

			@Override
			public void convertData(String arg0, ArrayList<UserInfoModel> list, Message msg) 
				throws JSONException{
				JSONObject root = new JSONObject(arg0);
				JSONArray statusArray = root.optJSONArray("users");
				if(statusArray == null || statusArray.length() == 0){
					msg.arg1 = FETCH_EMPTY;
					msg.obj = list;
					return;
				}
				for(int i = 0; i < statusArray.length(); i++ ) {
					JSONObject status = statusArray.getJSONObject(i);
					UserInfoModel infoModel = new UserInfoModel();
					
					if(WeiboConverter.convertUserInfoToModel(status, infoModel)) {
						list.add(infoModel);
					}
				}
				if(cursor == 0) {
					msg.arg1 = FETCH_SUCCEED_NEWS;
				} else {
					msg.arg1 = FETCH_SUCCEED_MORE;
				}
				msg.obj = list;
			}
		};
		handler.fetch();
	}
	
	/**
	 * @param follow  true:关注指定用户， false：取消关注指定用户
	 */
	public void setFriendships(final boolean follow, final String userId, final String name, final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<UserInfoModel> handler = new FetchHandler<UserInfoModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				FriendshipsAPI friendshipAPI = new FriendshipsAPI(oa);
				if(follow) {
					friendshipAPI.create(Long.parseLong(userId), name, listener);
				} else {
					friendshipAPI.destroy(Long.parseLong(userId), name, listener);
				}
				
			}

			@Override
			public void convertData(String arg0, ArrayList<UserInfoModel> list, Message msg) 
				throws JSONException{
				
				msg.arg1 = FETCH_SUCCEED_NEWS;
			}
		};
		handler.fetch();
	}
	
	public void deleteStatus(final String id, final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<StatusModel> handler = new FetchHandler<StatusModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				StatusesAPI statusesAPI = new StatusesAPI(oa);
				statusesAPI.destroy(Long.parseLong(id), listener);
			}

			@Override
			public void convertData(String arg0, ArrayList<StatusModel> list, Message msg) 
				throws JSONException{
					msg.arg1 = FETCH_SUCCEED_NEWS;
			}
		};
		handler.fetch();
	}
}
