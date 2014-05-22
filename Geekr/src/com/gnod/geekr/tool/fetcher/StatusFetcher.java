package com.gnod.geekr.tool.fetcher;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Message;

import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.tool.ImageHelper;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.converter.WeiboConverter;
import com.gnod.geekr.weibo.api.TopicsAPI;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.WeiboAPI.AUTHOR_FILTER;
import com.weibo.sdk.android.api.WeiboAPI.COMMENTS_TYPE;
import com.weibo.sdk.android.api.WeiboAPI.FEATURE;
import com.weibo.sdk.android.api.WeiboAPI.SRC_FILTER;
import com.weibo.sdk.android.api.WeiboAPI.TYPE_FILTER;
import com.weibo.sdk.android.net.RequestListener;

public class StatusFetcher extends BaseFetcher {

	/**
	 * 
	 * @param type 0: 所有用户，1：相互关注用户
	 */
	public void fetchStatus(final long since_id, final long max_id, final int count, final int page, final int type, FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<StatusModel> handler = new FetchHandler<StatusModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				StatusesAPI statusesAPI = new StatusesAPI(oa);
				if(type == 0) {
					statusesAPI.friendsTimeline(since_id, max_id, count, page, false, FEATURE.ALL, false, listener);
				} else if(type == 1) {
					statusesAPI.bilateralTimeline(since_id, max_id, count, page, false, FEATURE.ALL, false, listener);
				}
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
					
					if(WeiboConverter.convertStatusToModel(status, itemModel)) {
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
	
	/**
	 * 根据微博ID获取单条微博内容
	 * 
	 * @param id 需要获取的微博ID。
	 * @param listener
	 */
	public void fetchStatus(final String id, FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<StatusModel> handler = new FetchHandler<StatusModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				StatusesAPI statusesAPI = new StatusesAPI(oa);
				statusesAPI.show(Long.parseLong(id), listener);
			}

			@Override
			public void convertData(String arg0, ArrayList<StatusModel> list, Message msg) 
				throws JSONException{
				JSONObject status = new JSONObject(arg0);
				StatusModel itemModel = new StatusModel();
				
				if(WeiboConverter.convertStatusToModel(status, itemModel)){
					msg.arg1 = FETCH_SUCCEED_NEWS;
					msg.obj = itemModel;
				} else {
					msg.arg1 = FETCH_FAILED;
					msg.obj = list;
				}
			}
		};
		handler.fetch();
	}
	
	/**
	 * 获取@当前用户的最新微博
	 */
	public void fetchAtMe(final long since_id, final long max_id, final int count, 
			final int page,  final int type, final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<StatusModel> handler = new FetchHandler<StatusModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				StatusesAPI statusesAPI = new StatusesAPI(oa);
				statusesAPI.mentions(since_id, 
						max_id, count, page, 
						type == 0? AUTHOR_FILTER.ALL : AUTHOR_FILTER.ATTENTIONS, 
						SRC_FILTER.ALL, 
						TYPE_FILTER.ALL, 
						false, 
						listener);
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
	 * 获取某一话题下的微博
	 */
	public void fetchTopics(final String topic, final int count, 
			final int page, final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<StatusModel> handler = new FetchHandler<StatusModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				TopicsAPI topicAPI = new TopicsAPI(oa);
				topicAPI.topics(topic, count, page, listener);
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
	 * 发布一条新微博(连续两次发布的微博不可以重复)
	 * 
	 * @param content 要发布的微博文本内容，内容不超过140个汉字。
	 * @param file   上传图片path
	 * @param lat 纬度，有效范围：-90.0到+90.0，+表示北纬，默认为0.0。
	 * @param lon 经度，有效范围：-180.0到+180.0，+表示东经，默认为0.0。
	 * @param listener
	 */
	public void sendStatus(final String content, final String file, 
			final String lat, final String lon,
			FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<StatusModel> handler = new FetchHandler<StatusModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				StatusesAPI statusAPI = new StatusesAPI(oa);
				if(StringUtils.isNullOrEmpty(file)){
					statusAPI.update(content, lat, lon, listener);
				} else {
					String path = ImageHelper.compressImage(getConfig(), file);
					statusAPI.upload(content, path, lat, lon, listener);
				}
			}

			@Override
			public void convertData(String arg0, ArrayList<StatusModel> list, Message msg) 
				throws JSONException{
				msg.arg1 = FETCH_SUCCEED_NEWS;
			}
		};
		handler.fetch();
	}
	
	/**
	 * 转发微博
	 * @param isComment true 同时评论当前Weibo， false 仅转发
	 */
	public void retweetStatus(final String id, final String status, 
			final boolean isComment,
			final FetchCompleteListener listener){
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<StatusModel> handler = new FetchHandler<StatusModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				StatusesAPI statusesAPI = new StatusesAPI(oa);
				COMMENTS_TYPE type;
				if(isComment)
					type = COMMENTS_TYPE.CUR_STATUSES;
				else 
					type = COMMENTS_TYPE.NONE;
				statusesAPI.repost(Long.parseLong(id), status, type, listener);
			}

			@Override
			public void convertData(String arg0, ArrayList<StatusModel> list, Message msg) 
				throws JSONException{
				msg.arg1 = FETCH_SUCCEED_NEWS;
			}
		};
		handler.fetch();
	}
	
	/**
	 * 评论一条状态
	 */
	public void commentStatus(final String id, final String comment, 
			final boolean commentOri, final FetchCompleteListener listener){
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<StatusModel> handler = new FetchHandler<StatusModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				CommentsAPI commentsAPI  = new CommentsAPI(oa);
				commentsAPI.create(comment, Long.valueOf(id), !commentOri, listener);
			}

			@Override
			public void convertData(String arg0, ArrayList<StatusModel> list, Message msg) 
				throws JSONException{
				msg.arg1 = FETCH_SUCCEED_NEWS;
			}
		};
		handler.fetch();
	}
	
	/**
	 * 仅回复指定评论
	 * 
	 */
	public void replyComment(final String cid, final String id, 
			final String comment, final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<StatusModel> handler = new FetchHandler<StatusModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				CommentsAPI commentsAPI = new CommentsAPI(oa);
				commentsAPI.reply(Long.parseLong(cid),
						Long.parseLong(id), comment, false, 
						false, listener);
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
