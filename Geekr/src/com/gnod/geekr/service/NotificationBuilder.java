package com.gnod.geekr.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificationBuilder {
	private Context context;
	private int iconId;
	private CharSequence tickerText;
	private long when;
	private int flags;
	private Intent intent;
	private CharSequence contentTitle;
	private CharSequence contentText;
	
	public NotificationBuilder(Context context, int iconId,
			CharSequence tickerText, long when, int flags, Intent intent,
			CharSequence contentTitle, CharSequence contentText) {
		this.context = context;
		this.iconId = iconId;
		this.tickerText = tickerText;
		this.when = when;
		this.flags = flags;
		this.intent = intent;
		this.contentTitle = contentTitle;
		this.contentText = contentText;
	}


	public Notification getNotification() {
		Notification notification = new Notification();
		
		notification.icon = iconId;
		notification.tickerText = tickerText;
		notification.when = when;

		notification.flags |= flags;
		
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent operation = PendingIntent.getActivity(context, 0, intent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, contentTitle, contentText, operation);
		return notification;
	}
}
