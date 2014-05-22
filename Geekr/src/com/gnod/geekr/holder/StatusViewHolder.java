package com.gnod.geekr.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gnod.geekr.widget.AvatarView;
import com.gnod.geekr.widget.GeekrPanel;
import com.gnod.geekr.widget.URLImageView;

public class StatusViewHolder {
	public int tag;
	public RelativeLayout layoutAvatar;
	public AvatarView imageAvatar;
	public TextView textName;
	public ImageView verifiedImage;
	public TextView textContent;
	public URLImageView imageThumb;
	public LinearLayout layoutRetweet;
	public TextView textRetweetContent;
	public URLImageView imageRetweetThumb;
	public TextView textTime;
	public TextView textSource;
	public TextView textRetweetCount;
	public TextView textCommentCount;
	
	public GeekrPanel togglePanel;
	public View btnGeekrPanel;
}
