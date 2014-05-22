package com.gnod.geekr.tool;

import java.io.IOException;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.model.AccountModel;
import com.gnod.geekr.tool.manager.AccountManager;
import com.gnod.geekr.weibo.api.RemindAPI;
import com.gnod.geekr.weibo.api.RemindAPI.UNREAD_TYPE;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.FavoritesAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.api.WeiboAPI.COMMENTS_TYPE;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;

public class WeiboBaseTool {

	public static final String APP_KEY = "1969002064";
	public static final String APP_SECRET = "d67cd9c2384e4abfde9a2ae72205d4bc";
	public static final String REDIRECT_URL = "http://api.weibo.com/oauth2/default.html";
	public static final String OAUTH2_ACCESS_AUTHORIZE_URL = "https://api.weibo.com/oauth2/authorize";
	
	public static final String ID = "SinaWeiboID";
	public static final String TOKEN = "SinaWeiboToken";
	public static final String EXPIRES_TIME = "SinaWeiboExpiresTime";
	
	private AppConfig config;
	private SsoHandler ssoHandler;
	private static WeiboBaseTool instance;
	private Weibo weibo;
	
	private WeiboBaseTool(AppConfig config) {
		this.config = config;
	}
	
	public static void init(AppConfig config) {
		if(instance == null) {
			instance = new WeiboBaseTool(config);
			instance.getWeibo();
		}
	}
	
	public static WeiboBaseTool getInstance() {
		if(instance == null) 
			throw new NullPointerException("Instance didn't initialized.");
		return instance;
	}
	
	public Weibo getWeibo() {
		if(weibo == null)
			weibo = Weibo.getInstance(APP_KEY, REDIRECT_URL);
		return weibo;
	}
	
	public boolean isAuthValid(AccountModel account) {
		if(account == null) {
			return false;
		}
		
		String token = account.token;
		if(StringUtils.isNullOrEmpty(token)) {
			return false;
		}
		
		Long expiresTime = account.expTime;
		if(expiresTime < 0 || expiresTime < System.currentTimeMillis()) {
			return false;
		}
		return true;
	}
	
	public Oauth2AccessToken getOauth2AccessToken(AccountModel account) {
		if(isAuthValid(account) == false){
			return null;
		}

		String token = account.token;
		Long expiresTime = account.expTime;
		Oauth2AccessToken oauthToken = new Oauth2AccessToken();
		oauthToken.setExpiresTime(expiresTime);
		oauthToken.setToken(token);
		return oauthToken;
	}
	
	public void authorize(Activity activity, final SinaWeiboAuthorizedListener listener){
		try {
			Class.forName("com.weibo.sdk.android.sso.SsoHandler");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		ssoHandler = new SsoHandler(activity, getWeibo());
		ssoHandler.authorize(new WeiboAuthListener() {
			@Override
			public void onWeiboException(WeiboException e) {
				listener.onFailed();
			}
			
			@Override
			public void onError(WeiboDialogError e) {
				listener.onFailed();				
			}
			
			@Override
			public void onComplete(Bundle values) {
				try {
	    			final String id = values.getString("uid");
	    			String token = values.getString("access_token");
	    			String expires_in = values.getString("expires_in");
	    			long exp = System.currentTimeMillis() + Long.parseLong(expires_in)
	    					* 1000;

	    			final AccountModel account = new AccountModel();
	    			account.uID = id;
	    			account.token = token;
	    			account.expTime = exp;
	    			account.type = AccountManager.TYPE_SINA_WEIBO;
	    			
	    			final Oauth2AccessToken oauth2AccessToken = new Oauth2AccessToken();
	        		oauth2AccessToken.setToken(token);
	        		oauth2AccessToken.setExpiresTime(exp);	
	        		
	        		new Thread(new Runnable() {
						@Override
						public void run() {
							final Message msg = new Message();
							UsersAPI usersAPI=new UsersAPI(oauth2AccessToken);
			        		long lID = Long.parseLong(id);
			        		usersAPI.show(lID, new RequestListener() {
								@Override
								public void onIOException(IOException arg0) {
									msg.arg1 = -1;
									handler.sendMessage(msg);
								}
								@Override
								public void onError(WeiboException arg0) {
									msg.arg1 = -1;
									handler.sendMessage(msg);
								}
								@Override
								public void onComplete(String arg0) {
									try{
										JSONObject user = new JSONObject(arg0);
										account.name = user.optString("name");
										account.iconURL = user.optString("profile_image_url");
										AccountManager.addAccount(account);
										msg.arg1 = 1;
										handler.sendMessage(msg);
									}catch (Exception e) {
										msg.arg1 = -1;
										handler.sendMessage(msg);
									}
								}
							});
						}
					}).start();
	        		
				} catch (Exception e) {
					listener.onFailed();
				}
			}
			
			@Override
			public void onCancel() {
				listener.onCancel();				
			}
			
			private Handler handler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					if(msg.arg1 > 0) {
						listener.onSuccessed();
					} else {
						listener.onFailed();
					}
				}
			};
		});
	}
	
	/**
     * 发起认证的Activity必须重写onActivityResult， 这个方法必须在 onActivityResult 方法内调用
     */
	public void authorizeCallBack(int requestCode, int resultCode, Intent data){
		 if (ssoHandler != null) {
	            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
	     }
	}
	
	/**
	 * 收藏一条微博
	 * */
	public void favoriteStatus(String id) {
		Oauth2AccessToken oa = getOauth2AccessToken(AccountManager.getActivityAccount());
		if(oa == null) {
			ToastHelper.show("验证失败或已过期", 2);
			return;
		}
		FavoritesAPI favoritesAPI = new FavoritesAPI(oa);
		if(!StringUtils.isNullOrEmpty(id)){
			favoritesAPI.create(Long.parseLong(id), new RequestListener() {
				@Override
				public void onIOException(IOException arg0) {
					ToastHelper.show("收藏失败", 2);
				}
				@Override
				public void onError(WeiboException arg0) {
					if(arg0.getMessage().contains("20704")){
						ToastHelper.show("该微博已收藏过", 2);
					}
				}
				@Override
				public void onComplete(String arg0) {
					ToastHelper.show("收藏成功", 0);					
				}
			});
		} 
	}
	
	
	/**
	 * 清零指定类型的未读消息数目
	 */
	public void resetUnRead(UNREAD_TYPE type, RequestListener listener) {
		Oauth2AccessToken oa = getOauth2AccessToken(AccountManager.getActivityAccount());
		RemindAPI remindAPI = new RemindAPI(oa);
		if(listener == null)
			listener = emptyListener;
		remindAPI.resetCount(type, listener);
	}
	
	public interface SinaWeiboAuthorizedListener {
		public void onSuccessed();
        public void onFailed();
        public void onCancel();
	}

	private RequestListener emptyListener = new RequestListener() {
		@Override
		public void onIOException(IOException arg0) {
		}
		
		@Override
		public void onError(WeiboException arg0) {
		}
		
		@Override
		public void onComplete(String arg0) {
		}
	};
}
