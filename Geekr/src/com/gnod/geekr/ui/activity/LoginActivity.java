package com.gnod.geekr.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gnod.geekr.R;

public class LoginActivity extends BaseActivity {

	private int choice = 0;
	private Button btnChoice;
	private Button btnLogin;
	private View btnLoding;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final View view = View.inflate(this, R.layout.activity_login, null);
		setContentView(view);
		btnLoding = findViewById(R.id.layout_login_loading);
		btnLogin = (Button)findViewById(R.id.btn_login_black);
		btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(choice == 0){
					Intent intent = new Intent(LoginActivity.this, OAuthActivity.class);
					startActivity(intent);
				} else if(choice == 1){
//					AppConfig config = (AppConfig) getApplication();
//					if(!config.isNetworkConnected()){
//						ToastHelper.show("网络连接错误");
//						return;
//					}
//					btnLogin.setVisibility(View.GONE);
//					btnLoding.setVisibility(View.VISIBLE);
//					
//					SinaWeiboTool.getInstance().authorize(LoginActivity.this, 
//							new SinaWeiboTool.SinaWeiboAuthorizedListener() {
//						@Override
//						public void onSuccessed() {
//							Intent intent = new Intent(LoginActivity.this, TimeLineActivity.class);
//							startActivity(intent);
//						}
//						@Override
//						public void onFailed() {
//							ToastHelper.show("授权过程发生错误，请确保网络畅通");
//							btnLoding.setVisibility(View.GONE);
//							btnLogin.setVisibility(View.VISIBLE);
//						}
//						@Override
//						public void onCancel() {
//							btnLoding.setVisibility(View.GONE);
//							btnLogin.setVisibility(View.VISIBLE);
//						}
//					});
				}
			}
		});
		
//		btnChoice = (Button)findViewById(R.id.layout_start_choice);
//		btnChoice.setOnClickListener(new View.OnClickListener() {
//			String[] items = {
//				"OAuth验证", "SSO验证"	
//			};
//			@Override
//			public void onClick(View v) {
//				AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
//				builder.setItems(items, new OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						choice = which;
//						dialog.dismiss();
//					}
//				});
//				builder.setCancelable(true);
//				builder.show();
//			}
//		});
	}
	
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		SinaWeiboTool.getInstance().authorizeCallBack(requestCode, resultCode, data);
//	}
}
