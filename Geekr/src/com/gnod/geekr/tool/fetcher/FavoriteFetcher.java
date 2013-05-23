package com.gnod.geekr.tool.fetcher;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Message;

import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.tool.converter.WeiboConverter;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.api.FavoritesAPI;
import com.weibo.sdk.android.net.RequestListener;

public class FavoriteFetcher extends BaseFetcher{

	/**
	 * 获取收藏的微博
	 */
	public void fetchFavorities(final int count, 
			final int page, final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<StatusModel> handler = new FetchHandler<StatusModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				FavoritesAPI favoritesAPI = new FavoritesAPI(oa);
				favoritesAPI.favorites(count, page, listener);
			}

			@Override
			public void convertData(String arg0, ArrayList<StatusModel> list, Message msg) 
				throws JSONException{
				JSONObject root = new JSONObject(arg0);
				JSONArray statusArray = root.optJSONArray("favorites");
				if(statusArray == null || statusArray.length() == 0){
					msg.arg1 = FETCH_EMPTY;
					msg.obj = list;
					return;
				}
				for(int i = 0; i < statusArray.length(); i++ ) {
					JSONObject fav = statusArray.getJSONObject(i);
					JSONObject status = fav.getJSONObject("status");
					StatusModel itemModel = new StatusModel();
					WeiboConverter.convertStatusToModel(status, itemModel);
					
					if(itemModel != null ) {
						list.add(itemModel);
					}
				}
				if( page == 1) {
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
	 * 取消收藏一条微博
	 * */
	public void destroy(final String id, FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<StatusModel> handler = new FetchHandler<StatusModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				FavoritesAPI favoritesAPI = new FavoritesAPI(oa);
				favoritesAPI.destroy(Long.parseLong(id), listener);
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
