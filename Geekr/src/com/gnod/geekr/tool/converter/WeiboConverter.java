package com.gnod.geekr.tool.converter;

import org.json.JSONObject;

import android.util.Log;

import com.gnod.geekr.model.CommentModel;
import com.gnod.geekr.model.GroupsModel;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.model.UnReadModel;
import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.tool.DateUtils;

public class WeiboConverter {

	public static boolean convertStatusToModel(JSONObject status, StatusModel itemModel) {
		if(status == null || itemModel == null)
			return false;
		
		try {
			itemModel.ID = status.optString("id");
			itemModel.content = status.optString("text");
			itemModel.imageURL = status.optString("thumbnail_pic");
			itemModel.midImageURL = status.optString("bmiddle_pic");
			itemModel.fullImageURL = status.optString("original_pic");
			
			itemModel.retweetCount = status.optString("reposts_count");
			itemModel.commentCount = status.optString("comments_count");
			itemModel.time = DateUtils.convertSinaWeiboDateStringToDate(status.optString("created_at"));
			itemModel.source = status.optString("source");
			JSONObject user = status.optJSONObject("user");
			if(user == null) 
				return false;
			
			itemModel.userInfo = new UserInfoModel();
			//对每条状态加载信息,虽说可以提高查看用户性息时体验,但更多的时候
		    //会增加timeline消息的刷新时间
//			convertUserInfoToModel(user, itemModel.userInfo);
			itemModel.userInfo.userID = user.optString("id");
			itemModel.userInfo.nickName = user.optString("name");
			itemModel.userInfo.iconURL = user.optString("profile_image_url");
			itemModel.userInfo.verifiedType = user.optInt("verified_type");
			
			
			
			JSONObject retweetedStatus = status.optJSONObject("retweeted_status");
			if(retweetedStatus != null) {
				itemModel.retweetItem = new StatusModel();
				convertStatusToModel(retweetedStatus, itemModel.retweetItem);
			}
		} catch (Exception e) {
			Log.e("Converter", e.getMessage());
			return false;
		}
		return true;
	}
	
	public static boolean convertUserInfoToModel(JSONObject user, UserInfoModel infoModel) {
		if(user == null)
			return false;
		
		try {
			infoModel.userID = user.optString("id");
			infoModel.nickName = user.optString("name");
			infoModel.iconURL = user.optString("profile_image_url");
			infoModel.largeIconURL = user.optString("avatar_large");
			infoModel.province = user.optInt("province");
			infoModel.city = user.optInt("city");
			infoModel.location = user.optString("location");
			infoModel.verifiedReason = user.optString("verified_reason");
			infoModel.verifiedType = user.optInt("verified_type");
			
			infoModel.description = user.optString("description");
			infoModel.gender = user.optString("gender");
			infoModel.followed = user.optBoolean("following");
			
			infoModel.followersCount = user.optString("followers_count");
			infoModel.friendsCount = user.optString("friends_count");
			infoModel.statusCount = user.optString("statuses_count");
			
		} catch (Exception e) {
			Log.e("Converter", e.getMessage());
			return false;
		}
		return true;
	}
	
	public static boolean convertUnReadToModel(JSONObject root, UnReadModel unReadModel) {
		if(unReadModel == null)
			return false;
		try {
			unReadModel.statusCount = root.optInt("status");
			unReadModel.followerCount = root.optInt("follower");
			unReadModel.cmtCount = root.optInt("cmt");
			unReadModel.atMeCount = root.optInt("mention_status");
			unReadModel.metAtMeCount = root.optInt("mention_cmt");
		} catch (Exception e) {
			Log.e("Converter", e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public static boolean convertGroupsModel(JSONObject root, GroupsModel model) {
		if(model == null)
			return false;
		try {
			model.id = root.optString("idstr");
			model.name = root.optString("name");
			model.mode = root.optString("mode");
			model.memberCount = root.optInt("member_count");
		} catch (Exception e) {
			Log.e("Converter", e.getMessage());
			return false;
		}
		
		return true;
	}
	

	/**
	 * @param trimReply  值为true时， 如果当前评论是对某一评论的评论，则转换所评论的评论，
	 * 					否则不进行转换操作 
	 */
	public static boolean convertCommentToModel(JSONObject comment, CommentModel commentModel, boolean trimReply)
	{
		if(comment == null || commentModel == null)
			return false;
		
		try {
			commentModel.ID = comment.optString("id");
			commentModel.content = comment.optString("text");
			commentModel.source = comment.optString("source");
			String rawTime = comment.optString("created_at");
			commentModel.time = DateUtils.convertSinaWeiboDateStringToDate(rawTime);

			JSONObject status = comment.optJSONObject("status");
			commentModel.statusID = status.optString("id");
			
			JSONObject user = comment.optJSONObject("user");
			if(user == null)
				return false;
			commentModel.userInfo = new UserInfoModel();
			commentModel.userInfo.userID = user.optString("id");
			commentModel.userInfo.nickName = user.optString("name");
			commentModel.userInfo.iconURL = user.optString("profile_image_url");
			commentModel.userInfo.largeIconURL = user.optString("avatar_large");
			
			if(trimReply) {
				JSONObject reply = comment.optJSONObject("reply_comment");
				if(reply != null) {
					commentModel.replyComment = new CommentModel();
					commentModel.replyComment.ID = reply.optString("id");
					commentModel.replyComment.content = reply.optString("text");
					JSONObject replyUser = reply.optJSONObject("user");
					commentModel.replyComment.userInfo = new UserInfoModel();
					commentModel.replyComment.userInfo.userID = replyUser.optString("id");
					commentModel.replyComment.userInfo.nickName = replyUser.optString("name");
				}

				commentModel.status = new StatusModel();
				convertStatusToModel(status, commentModel.status);
			}
			
		} catch (Exception e) {
			Log.e("Converter", e.getMessage());
			return false;
		}
		return true;
	}
}
