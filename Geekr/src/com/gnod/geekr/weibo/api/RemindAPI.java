package com.gnod.geekr.weibo.api;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.RequestListener;

/**
 * 该类封装了接口，详情请参考<a href="http://open.weibo.com/wiki/API%E6%96%87%E6%A1%A3_V2#.E7.94.A8.E6.88.B7">提醒</a>
 * @author Gnod
 * 
 */
public class RemindAPI extends WeiboAPI{

	/**
	 * 设置未读数计数的消息项，follower：新粉丝数、cmt：新评论数、dm：新私信数、
	 * mention_status：新提及我的微博数、 mention_cmt：新提及我的评论数、
	 * group：微群消息数、notice：新通知数、invite：新邀请数、badge：新勋章数、
	 * photo：相册消息数、close_friends_feeds：密友feeds未读数、
	 * close_friends_mention_status：密友提及我的微博未读数 、
	 * close_friends_mention_cmt：密友提及我的评论未读数、
	 * close_friends_cmt：密友评论未读数、close_friends_attitude
	 * ：密友表态未读数、close_friends_common_cmt
	 * ：密友共同评论未读数、close_friends_invite：密友邀请未读数，一次只能操作一项。
	 */
	public enum UNREAD_TYPE {
		FOLLOWER("follower"), CMT("cmt"), DM("dm"), 
		MENTION_STATUS("mention_status"), MENTION_CMT("mention_cmt");
		
		private String str;
		
		UNREAD_TYPE(String str){
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}
	private static final String SERVER_URL_PRIX = "https://rm.api.weibo.com/2/remind";
	
	public RemindAPI(Oauth2AccessToken oauth2AccessToken) {
		super(oauth2AccessToken);
	}
	
	public void unreadCount( long uid, RequestListener listener) {
		WeiboParameters params = new WeiboParameters();
		params.add("uid", uid);
		request( SERVER_URL_PRIX + "/unread_count.json", params, HTTPMETHOD_GET, listener);
	}
	
	public void resetCount(UNREAD_TYPE type, RequestListener listener) {
		WeiboParameters params = new WeiboParameters();
		params.add("type", type.toString());
		request( SERVER_URL_PRIX + "/set_count.json", params, HTTPMETHOD_POST, listener);
	}
}
