package com.gnod.geekr.weibo.api;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.api.WeiboAPI.FRIEND_TYPE;
import com.weibo.sdk.android.api.WeiboAPI.RANGE;
import com.weibo.sdk.android.net.RequestListener;

/**
 * 该类封装了接口，详情请参考<a href="http://open.weibo.com/wiki/API%E6%96%87%E6%A1%A3_V2#.E7.94.A8.E6.88.B7">提醒</a>
 * @author Gnod
 * 
 */
public class TopicsAPI extends WeiboAPI{

	private static final String SERVER_URL_PRIX = API_SERVER + "/search";
	
	public TopicsAPI(Oauth2AccessToken oauth2AccessToken) {
		super(oauth2AccessToken);
	}
	
	/**
	 *  获取某一话题下的微博
	 * 
	 * @param q 搜索的关键字
	 * @param count 单页返回的记录条数，默认为10，最大为50。
	 * @param 返回结果的页码，默认为1。
	 * @param listener
	 */
	public void topics(String q, int count, int page, RequestListener listener) {
		WeiboParameters params = new WeiboParameters();
		params.add("q", q);
		params.add("count", count);
		params.add("page", page);
		request(SERVER_URL_PRIX + "/topics.json", params, HTTPMETHOD_GET,
				listener);
	} 
}
