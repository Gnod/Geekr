package com.gnod.geekr.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.gnod.geekr.R;
import com.gnod.geekr.model.AccountModel;
import com.gnod.geekr.service.PollingManager;
import com.gnod.geekr.tool.manager.AccountManager;
import com.gnod.geekr.ui.activity.LoginActivity;
import com.gnod.geekr.ui.activity.TimeLineActivity;

public class AppStart extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final View view = View.inflate(this, R.layout.splash, null);
		setContentView(view);
		AppConfig appConfig = (AppConfig) getApplication();
		
		PollingManager.checkPolling(appConfig);
		if(appConfig.isShowSplash()) {
			AlphaAnimation anim = new AlphaAnimation(0.3f,1.0f);
			anim.setDuration(1000);
			anim.setAnimationListener(new AnimationListener(){
				@Override
				public void onAnimationEnd(Animation arg0) {
					redirect();
				}
				@Override public void onAnimationRepeat(Animation animation) {}
				@Override public void onAnimationStart(Animation animation) {}
				
			});
			view.startAnimation(anim);
		} else {
			redirect();
		}
	}

	private void redirect() {
		AccountModel currentAccount = AccountManager.getActivityAccount();
		Intent intent;
		if(currentAccount == null){
			intent = new Intent(this, LoginActivity.class);
		} else {
			intent = new Intent(this, TimeLineActivity.class);
		}
		startActivity(intent);
	}

}
