package com.gnod.geekr.tool.fetcher;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Message;

import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.model.AccountModel;
import com.gnod.geekr.model.CommentModel;
import com.gnod.geekr.model.UnReadModel;
import com.gnod.geekr.tool.converter.WeiboConverter;
import com.gnod.geekr.weibo.api.RemindAPI;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.WeiboAPI.AUTHOR_FILTER;
import com.weibo.sdk.android.api.WeiboAPI.SRC_FILTER;
import com.weibo.sdk.android.net.RequestListener;

public class NoticeFetcher extends BaseFetcher {
	
	/**
	 * 获得指定登录用户的各种未读消息数目
	 * 
	 * @param account  指定用户对应Model
	 * 
	 */
	public void fetchUnReadCount(final AccountModel account, final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");
		FetchHandler<UnReadModel> handler = new FetchHandler<UnReadModel>(account, listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				RemindAPI remindAPI = new RemindAPI(oa);
				
				if(account != null)
					remindAPI.unreadCount(Long.parseLong(account.uID), listener);
			}

			@Override
			public void convertData(String arg0, ArrayList<UnReadModel> list, Message msg) 
				throws JSONException{
				JSONObject root = new JSONObject(arg0);
				UnReadModel model = new UnReadModel();
				WeiboConverter.convertUnReadToModel(root, model);
				msg.obj = model;
				msg.arg1 = FETCH_SUCCEED_NEWS;
			}
		};
		handler.fetch();
	}
	
}
