package com.gnod.geekr.tool.fetcher;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Message;

import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.model.CommentModel;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.tool.converter.WeiboConverter;
import com.gnod.geekr.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.gnod.geekr.tool.fetcher.BaseFetcher.FetchHandler;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.WeiboAPI.AUTHOR_FILTER;
import com.weibo.sdk.android.api.WeiboAPI.SRC_FILTER;
import com.weibo.sdk.android.net.RequestListener;

public class CommentFetcher extends BaseFetcher {

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
	 * 根据微博ID返回某条微博的评论列表
	 * @param weiboId 需要查询的微博ID。
	 * @param sinceId 若指定此参数，则返回ID比since_id大的评论（即比since_id时间晚的评论），默认为0。
	 * @param maxId 若指定此参数，则返回ID小于或等于max_id的评论，默认为0。
	 * @param count 单页返回的记录条数，默认为50
	 * @param listener
	 */
	public void fetchComment(final String weiboId, final long sinceId, 
			final long maxId, final int count, final int page, 
			final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<CommentModel> handler = new FetchHandler<CommentModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				CommentsAPI commentsAPI = new CommentsAPI(oa);
				commentsAPI.show(Long.valueOf(weiboId), 
						sinceId, maxId, count, page, AUTHOR_FILTER.ALL , listener);
			}

			@Override
			public void convertData(String arg0, ArrayList<CommentModel> list, Message msg) 
				throws JSONException{
				JSONObject root = new JSONObject(arg0);
				JSONArray commentArray = root.optJSONArray("comments");
				if(commentArray == null || commentArray.length() == 0) {
					msg.arg1 = FETCH_EMPTY;
					msg.obj = list;
					return;
				}
				for(int i = 0; i < commentArray.length(); i++ ) {
					JSONObject comment = commentArray.getJSONObject(i);
					CommentModel commentModel = new CommentModel();
					
					if(WeiboConverter.convertCommentToModel(comment, commentModel, false)) {
						list.add(commentModel);
					}
				}
				if(sinceId == 0 && maxId == 0) {
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
	 * 根据type获取当前用户的评论信息
	 * @param type 0:所有评论， 1:我发出的评论, 2: 我收到的评论, 3 @ 我的评论
	 */
	public void fetchComments(final long sinceId, 
			final long maxId, 
			final int count, 
			final int type,
			final FetchCompleteListener listener) {
		if(listener == null )
			throw new NullPointerException("Fetch Listener can not be null");

		FetchHandler<CommentModel> handler = new FetchHandler<CommentModel>(listener) {
			@Override
			public void callAPI(Oauth2AccessToken oa, RequestListener listener) {
				CommentsAPI commentsAPI = new CommentsAPI(oa);
				switch (type) {
				case 0:
					commentsAPI.timeline(sinceId, maxId, count, 1, false, listener);
					break;
				case 1:
					commentsAPI.byME(sinceId, maxId, count, 1, SRC_FILTER.ALL, listener);
					break;
				case 2:
					commentsAPI.toME(sinceId, maxId, count, 1, AUTHOR_FILTER.ALL, SRC_FILTER.ALL, listener);
					break;
				case 3:
					commentsAPI.mentions(sinceId, maxId, count, 
							1, AUTHOR_FILTER.ALL, SRC_FILTER.ALL, listener);
				default:
					break;
				}
			}

			@Override
			public void convertData(String arg0, ArrayList<CommentModel> list, Message msg) 
				throws JSONException{
				JSONObject root = new JSONObject(arg0);
				JSONArray commentArray = root.optJSONArray("comments");
				if(commentArray == null || commentArray.length() == 0) {
					msg.arg1 = FETCH_EMPTY;
					msg.obj = list;
					return;
				}
				for(int i = 0; i < commentArray.length(); i++ ) {
					JSONObject comment = commentArray.getJSONObject(i);
					CommentModel commentModel = new CommentModel();
					WeiboConverter.convertCommentToModel(comment, commentModel, true);

					if(commentModel != null ) {
						list.add(commentModel);
					}
				}
				if(maxId == 0) {
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
