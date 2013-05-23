package com.gnod.geekr.ui.activity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gnod.geekr.R;
import com.gnod.geekr.model.AccountModel;
import com.gnod.geekr.tool.WeiboBaseTool;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.ToastHelper;
import com.gnod.geekr.tool.URLTool;
import com.gnod.geekr.tool.manager.AccountManager;
import com.gnod.geekr.weibo.api.OAuthAPI;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.AsyncWeiboRunner;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.util.Utility;

public class OAuthActivity extends BaseActivity {

	private WebView webView;
    private MenuItem refreshItem;
	private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        setTitle("登录");
        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WeiboWebViewClient());


        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSaveFormData(false);
        settings.setSavePassword(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        
        progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.clearCache(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getSupportMenuInflater().inflate(R.menu.menu_oauth, menu);
        refreshItem = menu.findItem(R.id.menu_oauth_refresh);
        onRefresh();
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	finish();
                return true;
            case R.id.menu_oauth_refresh:
                onRefresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void onRefresh() {
        webView.clearView();
        webView.loadUrl("about:blank");
        refreshItem.setActionView(R.layout.layout_loading);
        webView.loadUrl(getWeiboOAuthURL());
    }

    
    private void completeRefresh() {
        if (refreshItem.getActionView() != null) {
            refreshItem.setActionView(null);
        }
    }

    private String getWeiboOAuthURL() {

        Map<String, String> params = new HashMap<String, String>();
        params.put("client_id", WeiboBaseTool.APP_KEY);
        params.put("response_type", "code");
        params.put("redirect_uri", WeiboBaseTool.REDIRECT_URL);
        params.put("display", "mobile");
        return WeiboBaseTool.OAUTH2_ACCESS_AUTHORIZE_URL + "?" 
        				+ URLTool.encodeURL(params)
                + "&scope=friendships_groups_read";
    }
    
    private class WeiboWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            if (url.startsWith(WeiboBaseTool.REDIRECT_URL)) {
                handleRedirectUrl(view, url);
                view.stopLoading();
                return;
            }
            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!url.equals("about:blank"))
                completeRefresh();
        }
    }

    private void handleRedirectUrl(WebView view, String url) {
        final Bundle values = Utility.parseUrl(url);
        String error = values.getString("error");
        String error_code = values.getString("error_code");

        Intent intent = new Intent();
        intent.putExtras(values);
        
        final Message msg = new Message();
        
        if (error == null && error_code == null) {
        	progressDialog.setMessage("初始化用户信息中...");
    		progressDialog.show();
    		
            setResult(RESULT_OK, intent);
    		
    		new Thread(new Runnable() {
				@Override
				public void run() {
					getAccessToken(msg, values.getString("code"));
				}
			}).start();
        } else {
        	ToastHelper.show("授权失败");
            finish();
        }

    }
    
    private void getAccessToken(final Message msg, String code) {
    	OAuthAPI oauthAPI = new OAuthAPI();
        oauthAPI.getToken(code, new RequestListener() {
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
    				if(StringUtils.isNullOrEmpty(arg0)) {
    					msg.arg1 = -1;
    	    			handler.sendMessage(msg);
    	    			return;
    				}
    				
					JSONObject values = new JSONObject(arg0);
					String id = values.optString("uid");
					String token = values.optString("access_token");
					String expires_in = values.optString("expires_in");
					
					long exp = System.currentTimeMillis() + Long.parseLong(expires_in)
							* 1000;

					AccountModel account = new AccountModel();
					account.uID = id;
					account.token = token;
					account.expTime = exp;
					account.type = AccountManager.TYPE_SINA_WEIBO;
					fetchAccountInfo(account, msg);
					
    			}catch (Exception e) {
    				msg.arg1 = -1;
    				handler.sendMessage(msg);
    			}
    		}
    	});
    }
    
    private void fetchAccountInfo(final AccountModel account, final Message msg) {
    	Oauth2AccessToken oauth2AccessToken = new Oauth2AccessToken();
    	oauth2AccessToken.setToken(account.token);
    	oauth2AccessToken.setExpiresTime(account.expTime);
    	
    	UsersAPI usersAPI=new UsersAPI(oauth2AccessToken);
		long lID = Long.parseLong(account.uID);
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
    
    private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			progressDialog.dismiss();
			if(msg.arg1 > 0) {
				Intent intent = new Intent(OAuthActivity.this, TimeLineActivity.class);
				startActivity(intent);
			} else {
				ToastHelper.show("获取信息过程发生错误");
			}
		}
	};
	
	
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
        	ToastHelper.show("您取消了授权");
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            webView.stopLoading();
    }
}
