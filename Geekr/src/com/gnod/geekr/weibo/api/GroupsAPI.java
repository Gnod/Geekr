package com.gnod.geekr.weibo.api;

import android.util.Log;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.RequestListener;
/**
 *  * 此类封装了好友分组的接口，详情见<a href=http://open.weibo.com/wiki/API%E6%96%87%E6%A1%A3_V2#.E5.85.B3.E7.B3.BB">关系接口</a>
 * @author Gnod
 *
 */
public class GroupsAPI extends WeiboAPI {
	public GroupsAPI(Oauth2AccessToken accessToken) {
        super(accessToken);
    }

    private static final String SERVER_URL_PRIX = API_SERVER + "/friendships";

	/**
	 * 获取当前登陆用户好友分组列表
	 * 
	 * @param listener
	 */
	public void groupsList(RequestListener listener) {
		WeiboParameters params = new WeiboParameters();
		request( SERVER_URL_PRIX + "/groups.json", params, HTTPMETHOD_GET, listener);
	}

	/**
	 * 获取当前登录用户某一好友分组的微博列表
	 * 
	 * @param list_id 需要查询的好友分组ID，建议使用返回值里的idstr，当查询的为私有分组时，则当前登录用户必须为其所有者。
	 * @param since_id 若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0
	 * @param max_id 若指定此参数，则返回ID小于或等于max_id的微博，默认为0。
	 * @param count 单页返回的记录条数，默认为50。
	 * @param page 返回结果的页码，默认为1。
	 * @param base_app 是否只获取当前应用的数据。false为否（所有数据），true为是（仅当前应用），默认为false。
	 * @param feature 过滤类型ID，0：全部、1：原创、2：图片、3：视频、4：音乐，默认为0。
	 * @param listener
	 */
	public void timeline(long list_id, long since_id, long max_id, int count, int page,
			boolean base_app,  FEATURE feature,  RequestListener listener) {		
		WeiboParameters params = new WeiboParameters();
		params.add("list_id", list_id);
		params.add("since_id", since_id);
		params.add("max_id", max_id);
		params.add("count", count);
		params.add("page", page);
		if (base_app) {
			params.add("base_app", 1);
		} else {
			params.add("base_app", 0);
		}
		params.add("feature", feature.ordinal());
		request( SERVER_URL_PRIX + "/groups/timeline.json", params, HTTPMETHOD_GET, listener);
	}
}
