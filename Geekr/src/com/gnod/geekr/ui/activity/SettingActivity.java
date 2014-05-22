package com.gnod.geekr.ui.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.app.AppManager;
import com.gnod.geekr.tool.ImageHelper;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.ToastHelper;
import com.gnod.geekr.tool.manager.SettingManager;
import com.umeng.fb.UMFeedbackService;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

public class SettingActivity extends SherlockPreferenceActivity {

	private CheckBoxPreference autoFetch;
	private AppConfig config;
	private Preference imgPath;
	private CheckBoxPreference imgFetch;
	private Preference notifiedCenter;
	private Preference cleanImgCache;
	private Preference cleanOtherCache;
	private Preference feedback;
	private Preference about;
	private CheckBoxPreference showSplash;
	private CheckBoxPreference menuAnim;
	private Preference checkUPdate;
	private Preference imgUploadQuality;
	private Preference imgShow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.xml.preferences);
		
		AppManager.getInstance().addActivity(this);
		setTitle("设置");
		
		config = (AppConfig) getApplication();
		
		//基本设置
		//首次进入自动加载
		autoFetch = (CheckBoxPreference)findPreference("autofetch");
		autoFetch.setChecked(config.isAutoFetch());
		autoFetch.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				config.setAutoFetch(autoFetch.isChecked());
				return true;
			}
		});
		
		showSplash = (CheckBoxPreference)findPreference("showsplash");
		showSplash.setChecked(config.isShowSplash());
		showSplash.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				config.setShowSplash(showSplash.isChecked());
				return true;
			}
		});
		
		//通知中心
		notifiedCenter = (Preference)findPreference("notification");
		notifiedCenter.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Intent intent = new Intent(pref.getContext(), NotificationActivity.class);
				pref.getContext().startActivity(intent);
				return true;
			}
		});
		
		//图片保存路径
		imgPath = (Preference)findPreference("imgsavepath");
		imgPath.setSummary(config.getImgPath());
		imgPath.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Context context = preference.getContext();
				final EditText editor = new EditText(context);
				editor.setText(config.getImgFolder());
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("图片保存路径");
				builder.setView(editor);
				builder.setPositiveButton("确定", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String content = editor.getText().toString().trim();
						if(!StringUtils.isNullOrEmpty(content)){
							config.setImgFolder(content);
							imgPath.setSummary(config.getImgPath());
						}
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("取消", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.show();
				return true;
			}
		});
		
		//图片设置
		//加载图片
		imgFetch = (CheckBoxPreference)findPreference("imgfetch");
		imgFetch.setChecked(config.isImgFetch());
		imgFetch.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				config.setImgFetch(imgFetch.isChecked());
				return true;
			}
		});
		imgUploadQuality = (Preference)findPreference("uploadquality");
		imgUploadQuality.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
				builder.setSingleChoiceItems(R.array.upload_pic_quality, 
						SettingManager.getUploadQuality(), 
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								SettingManager.setUploadQuality(which);
								dialog.dismiss();
							}
						});
				builder.setCancelable(true);
				builder.show();
				return true;
			}
		});
		
		//图片显示模式
		imgShow = (Preference)findPreference("imgshow");
		imgShow.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
				builder.setSingleChoiceItems(R.array.pic_show_model, 
						SettingManager.getPicShowModel(), 
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								SettingManager.setPicShowModel(which);
								dialog.dismiss();
							}
						});
				builder.setCancelable(true);
				builder.show();
				return true;
			}
		});
		
		
		//缓存设置
		//清除图片缓存
		cleanImgCache = (Preference)findPreference("cleanimgcache");
		cleanImgCache.setSummary(ImageHelper.getCacheSize(config));
		cleanImgCache.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				AlertDialog.Builder builder = new AlertDialog.Builder(pref.getContext());
				builder.setMessage("确认清除缓存?");
				builder.setPositiveButton("确认", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final Handler handler = new Handler() {
							@Override
							public void handleMessage(Message msg) {
								if(msg.arg1 == 1){
									ToastHelper.show("清除缓存成功");
								} else {
									ToastHelper.show("清除缓存失败");
								}
							}
						};
						new Thread() {
							@Override
							public void run() {
								Message msg = new Message();
								try {
									ImageHelper.clearCache(AppConfig.getAppContext());
									msg.arg1 = 1;
								} catch (Exception e) {
									msg.arg1 = -1;
								} finally {
									handler.sendMessage(msg);
								}
							}
						}.start();
						cleanImgCache.setSummary("0KB");
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("取消", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.show();
				return true;
			}
		});
		
		//清除其它缓存
		cleanOtherCache = (Preference)findPreference("cleanothercache");
		cleanOtherCache.setSummary(config.getObjectCacheSize());
		cleanOtherCache.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				AlertDialog.Builder builder = new AlertDialog.Builder(pref.getContext());
				builder.setMessage("确认清除缓存?");
				builder.setPositiveButton("确认", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						final Handler handler = new Handler() {
							@Override
							public void handleMessage(Message msg) {
								if(msg.arg1 == 1){
									ToastHelper.show("清除缓存成功");
								} else {
									ToastHelper.show("清除缓存失败");
								}
							}
							
						};
						new Thread() {
							@Override
							public void run() {
								Message msg = new Message();
								try {
									config.clearObjectCache();
									msg.arg1 = 1;
								} catch (Exception e) {
									msg.arg1 = -1;
								} finally {
									handler.sendMessage(msg);
								}
							}
						}.start();
						cleanOtherCache.setSummary("0KB");
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("取消", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.show();
				return true;
			}
		});
		
		//动画效果
		//菜单动画效果
		menuAnim = (CheckBoxPreference)findPreference("menuanimation");
		menuAnim.setChecked(config.isShowMenuAnim());
		menuAnim.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				config.setMenuAnim(menuAnim.isChecked());
				return true;
			}
		});
		
		//其它
		checkUPdate = (Preference)findPreference("checkupdate");
		checkUPdate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				final Context context = pref.getContext();
				UmengUpdateAgent.update(context);
				UmengUpdateAgent.setUpdateAutoPopup(false);
				UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
				        @Override
				        public void onUpdateReturned(int updateStatus,UpdateResponse updateInfo) {
				            switch (updateStatus) {
				            case 0: // has update
				                UmengUpdateAgent.showUpdateDialog(context, updateInfo);
				                break;
				            case 1: // has no update
				                Toast.makeText(context, "已经是最新版本", Toast.LENGTH_SHORT)
				                        .show();
				                break;
				            case 2: // none wifi
				                Toast.makeText(context, "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT)
				                        .show();
				                break;
				            case 3: // time out
				                Toast.makeText(context, "超时", Toast.LENGTH_SHORT)
				                        .show();
				                break;
				            }
				        }
				});
				return true;
			}
		});
		//意见反馈
		feedback = (Preference)findPreference("feedback");
		feedback.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				UMFeedbackService.setGoBackButtonVisible();
				UMFeedbackService.openUmengFeedbackSDK(pref.getContext());
//				Intent intent = new Intent(pref.getContext(), PostStatusActivity.class);
//				intent.putExtra("Type", "PostStatus");
//				intent.putExtra("Content", "#Geekr Feeback#");
//				startActivity(intent);
				return true;
			}
		});
		
		about = (Preference)findPreference("about");
		about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Intent intent = new Intent(pref.getContext(), AboutActivity.class);
				startActivity(intent);
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

	
}
