package com.gnod.geekr.service;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.model.AccountModel;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.model.UnReadModel;
import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.fetcher.BaseFetcher;
import com.gnod.geekr.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.gnod.geekr.tool.fetcher.NoticeFetcher;
import com.gnod.geekr.tool.fetcher.ProfileFetcher;
import com.gnod.geekr.tool.manager.AccountManager;
import com.gnod.geekr.ui.activity.CommentsActivity;
import com.gnod.geekr.ui.activity.FriendListActivity;
import com.gnod.geekr.ui.activity.ProfileActivity;
import com.gnod.geekr.ui.activity.TimeLineActivity;
import com.gnod.geekr.weibo.api.RemindAPI.UNREAD_TYPE;

public class PollingService extends Service {

	private static int prevNewFansCount;
	private static int prevCmtCount;
	private static int prevAtMeCount;
	private static int prevMetAtMeCount;
	private static long prevSpecialStatusId = 0;
	
	private NotificationManager mNotificationMgr;
	private NoticeFetcher fetcher;
	private AppConfig config;

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		config = (AppConfig) getApplication();
		fetcher = new NoticeFetcher();
		
		fetch();
		stopSelf();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	public void fetch() {
		if(!PollingManager.isPolling(config)){
			stopSelf();
			return;
		}
		if(PollingManager.isPollingAvoidNightDistrubed(config)){
			Calendar date = Calendar.getInstance();
			date.setTimeInMillis(System.currentTimeMillis());
			int hour = date.get(Calendar.HOUR_OF_DAY);
			if(hour >= 0 && hour < 8) {
				stopSelf();
				return;
			}
		}
		final AccountModel account = AccountManager.getActivityAccount();
		if(account == null) {
			stopSelf();
			return;
		}
		fetcher.fetchUnReadCount(account, 
				new PollingFetchListener(config, account));
		
		pollingSpecial();
		stopSelf();
	}
	
	private void pollingSpecial() {
		final String userName = PollingManager.getPollingSpecialPersonName(config);
		if(StringUtils.isNullOrEmpty(userName)) {
			return;
		}
		ProfileFetcher profileFetcher = new ProfileFetcher();
		profileFetcher.fetchUserStatus("", userName, prevSpecialStatusId, 0, 1, 1, 
			new FetchCompleteListener() {
				@Override
				public void fetchComplete(int state, int errorCode, Object obj) {
					if(state == BaseFetcher.FETCH_SUCCEED_NEWS){
						ArrayList<StatusModel> list = (ArrayList<StatusModel>) obj;
						if(prevSpecialStatusId != 0){
							String hintText = new StringBuilder().append(userName).
							append("有新消息更新").toString();
							Intent intent = new Intent(config, 
									ProfileActivity.class);
							UserInfoModel user = new UserInfoModel();
							user.nickName = userName;
							intent.putExtra("UserInfoModel", user);
							notifyUser(R.string.special_person, 
									R.drawable.icon_notification, 
									"特别关注", hintText, intent);
						}
						prevSpecialStatusId = Long.parseLong(list.get(0).ID);
					}
				}
			});
	}

	private void notifyUser(int typeId, int iconId, String title, String text, Intent intent) {
		NotificationBuilder builder = new NotificationBuilder(
				PollingService.this,
				iconId, 
				text, 
				System.currentTimeMillis(), 
				Notification.FLAG_AUTO_CANCEL, 
				intent, 
				title, 
				text);
		Notification notification = builder.getNotification();
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		mNotificationMgr.notify(typeId, notification);
	}
	
	private class PollingFetchListener implements FetchCompleteListener {
		private AccountModel account;
		private AppConfig config;
		public PollingFetchListener(AppConfig config, AccountModel account) {
			this.account = account;
			this.config = config;
		}
		
		@Override
		public void fetchComplete(int state, int errorCode, Object obj) {
			if(state == BaseFetcher.FETCH_SUCCEED_NEWS){
				UnReadModel model = (UnReadModel) obj;
				checkNewFans(model);
				checkNewCmt(model);
				checkNewStatusAtMe(model);
				checkNewCmtAtMe(model);
			}
		}
		
		private void checkNewFans(UnReadModel model) {
			if(model.followerCount > 0 && PollingManager.isPollingNewFans(config)){
				//Avoid Noising Notice
				String type = UNREAD_TYPE.FOLLOWER.toString();
				if(model.followerCount == prevNewFansCount){
					return;
				}
				prevNewFansCount = model.followerCount;
				String hintText = new StringBuilder().append("增加")
						.append(model.followerCount).append("位新粉丝")
						.toString();
				Intent intent = new Intent(config, FriendListActivity.class);
				UserInfoModel user = new UserInfoModel();
				user.userID = account.uID;
				user.nickName = account.name;
				intent.putExtra("UserInfoModel", user);
				intent.putExtra("Type", 0);
				intent.putExtra("IsFromNotice", true);
				notifyUser(R.string.new_fans, R.drawable.icon_notification, 
						account.name, hintText, intent);
			} else {
				prevNewFansCount = 0;
				mNotificationMgr.cancel(R.string.new_fans);
			}
		}
		
		private void checkNewCmt(UnReadModel model) {
			if(model.cmtCount > 0 && PollingManager.isPollingNewComment(config)) {
				String type = UNREAD_TYPE.CMT.toString();
				if(model.cmtCount == prevCmtCount){
					return;
				} 
				prevCmtCount = model.cmtCount;
				String hintText = new StringBuilder().append("收到")
				.append(model.cmtCount).append("条新评论")
				.toString();
				Intent intent = new Intent(config, CommentsActivity.class);
				intent.putExtra("Type", 2);
				notifyUser(R.string.new_comment, R.drawable.icon_notification, 
						account.name, hintText, intent);
			} else {
				prevCmtCount = 0;
				mNotificationMgr.cancel(R.string.new_comment);
			}
		}
		
		private void checkNewStatusAtMe(UnReadModel model) {
			if(model.atMeCount >0 && PollingManager.isPollingAtMe(config)) {
				String type = UNREAD_TYPE.MENTION_STATUS.toString();
				if(model.atMeCount == prevAtMeCount){
					return;
				}
				prevMetAtMeCount = model.atMeCount;
				String hintText = new StringBuilder().append("收到")
				.append(model.atMeCount).append("条@我的消息")
				.toString();
				Intent intent = new Intent(config, TimeLineActivity.class);
				intent.putExtra("Type", 1);
				intent.putExtra("IsFromNotice", true);
				notifyUser(R.string.at_me, R.drawable.icon_notification, 
						account.name, hintText, intent);
			} else {
				prevAtMeCount = 0;
				mNotificationMgr.cancel(R.string.at_me);
			}
		}
		
		private void checkNewCmtAtMe(UnReadModel model) {
			if(model.metAtMeCount > 0 && PollingManager.isPollingCommentAtMe(config)) {
				String type = UNREAD_TYPE.MENTION_CMT.toString();
				if(model.metAtMeCount == prevMetAtMeCount){
					return;
				} 
				prevMetAtMeCount = model.metAtMeCount;
				String hintText = new StringBuilder().append("收到")
				.append(model.metAtMeCount).append("条@我的评论")
				.toString();
				Intent intent = new Intent(config, CommentsActivity.class);
				intent.putExtra("Type", 3);
				notifyUser(R.string.comment_atme, R.drawable.icon_notification, 
						account.name, hintText, intent);
			}else {
				prevMetAtMeCount = 0;
				mNotificationMgr.cancel(R.string.comment_atme);
			}
		}
	};
}
