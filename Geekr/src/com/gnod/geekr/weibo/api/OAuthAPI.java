package com.gnod.geekr.weibo.api;

import java.io.IOException;

import android.util.Log;

import com.gnod.geekr.tool.WeiboBaseTool;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.AsyncWeiboRunner;
import com.weibo.sdk.android.net.RequestListener;

public class OAuthAPI {

	private static final String SERVER_URL = "https://api.weibo.com/oauth2/access_token";
	
	public void getToken(String code, RequestListener listener) {
		WeiboParameters params = new WeiboParameters();
        params.add("client_id", WeiboBaseTool.APP_KEY);
        params.add("client_secret", WeiboBaseTool.APP_SECRET);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", WeiboBaseTool.REDIRECT_URL);
        params.add("code", code);
        AsyncWeiboRunner.request(SERVER_URL, params, 
        		WeiboAPI.HTTPMETHOD_POST, listener);
	}
}
