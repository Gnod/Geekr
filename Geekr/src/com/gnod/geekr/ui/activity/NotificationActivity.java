package com.gnod.geekr.ui.activity;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.app.AppManager;
import com.gnod.geekr.service.PollingManager;
import com.gnod.geekr.tool.StringUtils;

public class NotificationActivity extends SherlockPreferenceActivity {

	private AppConfig config;
	private CheckBoxPreference newFans;
	private CheckBoxPreference atMeComment;
	private CheckBoxPreference atMeWeibo;
	private CheckBoxPreference newComment;
	private Preference notificationInterval;
	private CheckBoxPreference nightDisturbed;
	private CheckBoxPreference notifiedEnable;
	private CheckBoxPreference specialPerson;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.xml.notification_center);
		
		AppManager.getInstance().addActivity(this);
		setTitle("通知中心");
		
		config = (AppConfig) getApplication();
		
		notifiedEnable = (CheckBoxPreference)findPreference("notification_switch");
		notifiedEnable.setChecked(PollingManager.isPolling(config));
		notifiedEnable.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				PollingManager.setPolling(config, notifiedEnable.isChecked());
				return true;
			}
		});
		
		newFans = (CheckBoxPreference)findPreference("newfans");
		newFans.setChecked(PollingManager.isPollingNewFans(config));
		newFans.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				PollingManager.setPollingNewFans(config, newFans.isChecked());
				return true;
			}
		});
		
		newComment = (CheckBoxPreference)findPreference("newcomment");
		newComment.setChecked(PollingManager.isPollingNewComment(config));
		newComment.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				PollingManager.setPollingNewComment(config, newComment.isChecked());
				return true;
			}
		});
		
		atMeComment = (CheckBoxPreference)findPreference("commentatme");
		atMeComment.setChecked(PollingManager.isPollingCommentAtMe(config));
		atMeComment.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				PollingManager.setPollingCommentAtMe(config, atMeComment.isChecked());
				return true;
			}
		});
		
		atMeWeibo = (CheckBoxPreference)findPreference("atmeweibo");
		atMeWeibo.setChecked(PollingManager.isPollingAtMe(config));
		atMeWeibo.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				PollingManager.setPollingAtMe(config, atMeWeibo.isChecked());
				return true;
			}
		});
		
		specialPerson = (CheckBoxPreference)findPreference("specialperson");
		String specialName = PollingManager.getPollingSpecialPersonName(config);
		if(StringUtils.isNullOrEmpty(specialName)) {
			specialPerson.setChecked(false);
			specialPerson.setSummary("当指定用户有消息更新时收到提醒");
		} else {
			specialPerson.setChecked(true);
			specialPerson.setSummary(specialName + "(当该用户有消息更新时收到提醒)");
		}
		specialPerson.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if(specialPerson.isChecked()) {
					Intent intent = new Intent(NotificationActivity.this, 
							AtUserActivity.class);
					intent.putExtra("Type", "Special");
					startActivityForResult(intent, 1);
				} else {
					PollingManager.setPollingSpecialPerson(config, "");
					specialPerson.setSummary("当指定用户有消息更新时收到提醒");
				}
				return true;
			}
		});
		
		
		notificationInterval = (Preference)findPreference("notificationinterval");
		notificationInterval.setSummary(PollingManager.formateInterval(config));
		notificationInterval.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			String[] timeArray = {
				"30秒", "1分钟", "3分钟", "5分钟", "10分钟"	
			};
			@Override
			public boolean onPreferenceClick(Preference pref) {
				AlertDialog.Builder builder = new AlertDialog.Builder(pref.getContext());
				builder.setTitle("轮询时间间隔");
				builder.setItems(timeArray, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						long interval = 3 * 60 * 1000;
						switch (which) {
						case 0:
							interval = 30 * 1000;
							break;
						case 1:
							interval = 60 * 1000;
							break;
						case 2:
							interval = 3 * 60 * 1000;
							break;
						case 3:
							interval = 5 * 60 * 1000;
							break;
						case 4:
							interval = 10 * 60 * 1000;
							break;
						default:
							break;
						}
						PollingManager.setPollingInterval(config, interval);
						PollingManager.setPolling(config, PollingManager.isPolling(config));
						notificationInterval.setSummary(timeArray[which]);
						dialog.dismiss();
					}
				});
				builder.show();
				return true;
			}
		});
		
		nightDisturbed = (CheckBoxPreference)findPreference("nodistrubed");
		nightDisturbed.setChecked(PollingManager.isPollingAvoidNightDistrubed(config));
		nightDisturbed.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				PollingManager.setPollingAvoidNightDistrubed(
						config, nightDisturbed.isChecked());
				return true;
			}
		});
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		AppManager.getInstance().finishActivity(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case 1:
			if(resultCode == RESULT_OK) {
				String userName = data.getStringExtra("Name");
				PollingManager.setPollingSpecialPerson(config, userName);
				specialPerson.setSummary(userName + "(当该用户有消息更新时收到提醒)");
			}
			break;
		default:
			break;
		}
	}
	
}
