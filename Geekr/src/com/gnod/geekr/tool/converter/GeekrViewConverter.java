package com.gnod.geekr.tool.converter;

import android.text.Html;
import android.view.View;

import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.holder.StatusViewHolder;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.tool.DateUtils;
import com.gnod.geekr.tool.ImageHelper;
import com.gnod.geekr.tool.StringUtils;

public class GeekrViewConverter {

	public enum IMAGE_MODEL{
		SMALL,BIG,NONE,AUTO;
	}
	
	public static void attachViewDatas(StatusViewHolder viewHolder,
			StatusModel item, int position, IMAGE_MODEL type) {
		viewHolder.tag = position;
		viewHolder.imageAvatar.setImageResource(R.drawable.avatar_default);

		UserInfoModel userInfo = item.userInfo;
		if (userInfo != null) {
			if(viewHolder.layoutAvatar.getVisibility() == View.VISIBLE) {
				viewHolder.imageAvatar.setItem(userInfo);
//				AppConfig.getDrawableManager().loadBitmap(userInfo.iconURL,
//						viewHolder.imageAvatar, true);
				AppConfig.sImageFetcher.loadImage(userInfo.iconURL, viewHolder.imageAvatar, R.drawable.avatar_default);
				viewHolder.textName.setText(userInfo.nickName);
	
				ImageHelper.setVerifiedImage(viewHolder.verifiedImage,
						userInfo.verifiedType);
			}
		} else {
			viewHolder.textName.setText("");
		}

		viewHolder.textContent.setText(item.content);

		if (StringUtils.isNullOrEmpty(item.content)) {
			viewHolder.textContent.setVisibility(View.GONE);
		} else {
			viewHolder.textContent.setVisibility(View.VISIBLE);
		}
		if (!AppConfig.mFetchImage || StringUtils.isNullOrEmpty(item.imageURL)) {
			viewHolder.imageThumb.setVisibility(View.GONE);
		} else {
			viewHolder.imageThumb.setVisibility(View.VISIBLE);
			viewHolder.imageThumb.setURL(item.midImageURL);
			if(type == IMAGE_MODEL.SMALL) {
//				viewHolder.imageThumb.setImageResource(R.drawable.wb_pic_loading);
//				AppConfig.getDrawableManager().loadBitmap(item.imageURL,
//						viewHolder.imageThumb, true);
				AppConfig.sImageFetcher.loadImage(
						item.imageURL,
						viewHolder.imageThumb, 
						R.drawable.wb_pic_loading);
				
			} else if(type == IMAGE_MODEL.BIG) {
//				viewHolder.imageThumb.setImageResource(
//						R.drawable.wb_pic_loading_large);
//				AppConfig.getDrawableManager().loadBitmap(item.midImageURL,
//						viewHolder.imageThumb, true);
				AppConfig.sImageFetcher.loadImage(
						item.midImageURL,
						viewHolder.imageThumb, 
						R.drawable.wb_pic_loading_large);
			}
		}

		if (item.retweetItem == null) {
			viewHolder.layoutRetweet.setVisibility(View.GONE);
		} else {
			viewHolder.layoutRetweet.setVisibility(View.VISIBLE);
			String retweetContent;
			if (item.retweetItem.userInfo != null) {
				StringBuilder retweetSbr = new StringBuilder();
				retweetSbr.append("@")
						.append(item.retweetItem.userInfo.nickName)
						.append(": ").append(item.retweetItem.content);
				retweetContent = retweetSbr.toString();
			} else {
				retweetContent = item.retweetItem.content;
			}
			viewHolder.textRetweetContent.setText(retweetContent);

			if (StringUtils.isNullOrEmpty(retweetContent)) {
				viewHolder.textRetweetContent.setVisibility(View.GONE);
			} else {
				viewHolder.textRetweetContent.setVisibility(View.VISIBLE);
			}

			if (!AppConfig.mFetchImage || 
					StringUtils.isNullOrEmpty(item.retweetItem.imageURL)) {
				viewHolder.imageRetweetThumb.setVisibility(View.GONE);
			} else {
				viewHolder.imageRetweetThumb.setVisibility(View.VISIBLE);
				viewHolder.imageRetweetThumb.setURL(
						item.retweetItem.midImageURL);
				
				if(type == IMAGE_MODEL.SMALL) {
//					viewHolder.imageRetweetThumb.setImageResource(
//							R.drawable.wb_pic_loading);
//					AppConfig.getDrawableManager().loadBitmap(
//							item.retweetItem.imageURL,
//							viewHolder.imageRetweetThumb, true);
					AppConfig.sImageFetcher.loadImage(
							item.retweetItem.imageURL,
							viewHolder.imageRetweetThumb, 
							R.drawable.wb_pic_loading_large);
				} else if(type == IMAGE_MODEL.BIG) {
//					viewHolder.imageRetweetThumb.setImageResource(
//							R.drawable.wb_pic_loading_large);
//					AppConfig.getDrawableManager().loadBitmap(
//							item.retweetItem.midImageURL,
//							viewHolder.imageRetweetThumb, true);
					AppConfig.sImageFetcher.loadImage(
							item.retweetItem.midImageURL,
							viewHolder.imageRetweetThumb, 
							R.drawable.wb_pic_loading_large);
				}
			}
		}
		viewHolder.textTime.setText(DateUtils.getMagicTime(item.time));
		if (!StringUtils.isNullOrEmpty(item.source))
			viewHolder.textSource
					.setText(Html.fromHtml(item.source).toString());
		viewHolder.textCommentCount.setText(item.getCommentCount());
		viewHolder.textRetweetCount.setText(item.getRetweetCount());
	}
}
