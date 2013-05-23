package com.gnod.geekr.ui.activity;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gnod.geekr.R;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.model.UserInfoModel;

public class AboutActivity extends BaseActivity {

	private TextView textVersion;
	private View btnAuthor;
	private TextView textCopyRight;
	private View btnWebsite;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		setTitle("关于");

		textVersion = (TextView)findViewById(R.id.about_version);
		btnWebsite = findViewById(R.id.about_website);
		btnAuthor = findViewById(R.id.about_author);
		PackageInfo info = null;
		try {
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e("about", e.getMessage());
		}
		if(info != null) 
			textVersion.setText("版本号 ( " + info.versionName + " )");
	
		btnWebsite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				openWeb(v.getContext(), "http://gnod.github.io/Geekr/");
			}
		});
		
		btnAuthor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent(v.getContext(), UserInfoActivity.class);
//				UserInfoModel userInfo = new UserInfoModel();
//				userInfo.userID = "1777044635";
//				intent.putExtra("UserInfoModel", userInfo);
//				v.getContext().startActivity(intent);
				openWeb(v.getContext(), "http://about.me/gnod/");
			}
		});
		
		textCopyRight = (TextView)findViewById(R.id.about_copyright);
		Date date = new Date();
		textCopyRight.setText("Gnod Studio All Rights Reserved ⓒ2012-" + (1900 + date.getYear()));
	}
	
	public void openWeb(Context context, String str) {
		Uri uri = Uri.parse(str);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        context.startActivity(intent);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_default, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
