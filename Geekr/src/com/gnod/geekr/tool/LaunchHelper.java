package com.gnod.geekr.tool;

import android.content.Context;
import android.content.Intent;

import com.gnod.geekr.model.CommentModel;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.ui.activity.PostStatusActivity;
import com.gnod.geekr.ui.activity.ProfileActivity;
import com.gnod.geekr.ui.activity.StatusDetailActivity;

public class LaunchHelper {

	public static void startCommentActivity(Context context, StatusModel item) {
		Intent intent = new Intent(context, PostStatusActivity.class);
		intent.putExtra("Type", "comment");
		intent.putExtra("StatusID", item.ID);
		context.startActivity(intent);
	}
	
	public static void startRetweetActivity(Context context, StatusModel item) {
		Intent intent = new Intent(context, PostStatusActivity.class);
		intent.putExtra("Type", "retweet");
		intent.putExtra("StatusID", item.ID);
		if(item.retweetItem != null ){
			intent.putExtra("Content", 
				new StringBuilder().append("//@").append(item.userInfo.nickName)
					.append(":").append(item.content).toString());
		}
		context.startActivity(intent);
	}
	
	public static void startReplyActivity(Context context, CommentModel model) {
		Intent intent = new Intent(context, PostStatusActivity.class);
		intent.putExtra("Type", "replyComment");
		intent.putExtra("CommentID", model.ID);
		intent.putExtra("StatusID", model.statusID);
		context.startActivity(intent);
	}
	
	public static void startPostActivity(Context context) {
		Intent intent = new Intent(context, PostStatusActivity.class);
		intent.putExtra("Type", "PostStatus");
		context.startActivity(intent);
	}
	
	public static void startProfileActivity(Context context, UserInfoModel model) {
		Intent intent = new Intent(context, ProfileActivity.class);
		
		intent.putExtra("UserInfoModel", model);
		context.startActivity(intent);
	}
	
	public static void startDetailActivity(Context context, StatusModel itemModel, 
			int position, String typeTag) {
		Intent intent = new Intent(context, 
				StatusDetailActivity.class);
		intent.putExtra("itemModel", itemModel);
		if(position != -1) {
			intent.putExtra("Position", position);
		}
		if(!StringUtils.isNullOrEmpty(typeTag)){
			intent.putExtra("StatusTag", typeTag);
		}
		context.startActivity(intent);
	}
	
	public static void startDetailActivity(Context context, StatusModel itemModel) {
		startDetailActivity(context, itemModel, -1, null);
	}
}
