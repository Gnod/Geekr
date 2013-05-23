package com.gnod.geekr.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gnod.geekr.R;
import com.gnod.geekr.holder.StatusViewHolder;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.tool.converter.GeekrViewConverter;
import com.gnod.geekr.tool.manager.SettingManager;

public class StatusItemLayout extends RelativeLayout {

	private StatusViewHolder mViewHolder;

	public StatusItemLayout(Context context) {
		this(context, null);
	}

	public StatusItemLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutParams(new AbsListView.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		setBackgroundResource(R.drawable.listview_item_bg);
		View view = LayoutInflater.from(context).inflate(R.layout.listitem_timeline, null);
		addView(view);
		
		mViewHolder = new StatusViewHolder();
		mViewHolder.togglePanel = (GeekrPanel)view.findViewById(R.id.view_timeline_panel);
		mViewHolder.layoutAvatar = (RelativeLayout)view.findViewById(R.id.layout_timeline_avatar);
		mViewHolder.imageAvatar = (AvatarView) view.findViewById(R.id.image_avatar_small);
		mViewHolder.textName = (TextView) view.findViewById(R.id.status_list_item_name);
		mViewHolder.verifiedImage = (ImageView)view.findViewById(R.id.image_avatar_verified);
		mViewHolder.textContent = (TextView) view.findViewById(R.id.text_timeline_status);
		mViewHolder.imageThumb = (URLImageView) view.findViewById(R.id.status_list_item_thumb);

		mViewHolder.layoutRetweet = (LinearLayout) view.findViewById(R.id.status_list_item_retweet);
		mViewHolder.textRetweetContent = (TextView) view.findViewById(R.id.text_timeline_retweet_status);
		mViewHolder.imageRetweetThumb = (URLImageView) view.findViewById(R.id.status_list_item_retweet_thumb);
		mViewHolder.textTime = (TextView) view.findViewById(R.id.status_list_item_time);
		mViewHolder.textSource = (TextView) view.findViewById(R.id.text_timeline_status_from);
		mViewHolder.textCommentCount = (TextView) view.findViewById(R.id.status_list_item_comment_count);
		mViewHolder.textRetweetCount = (TextView)view.findViewById(R.id.status_list_item_retweet_count);
		setTag(mViewHolder);
	}
	
	public void attachViewData(StatusModel item, int position) {
		GeekrViewConverter.attachViewDatas(mViewHolder, item, position, SettingManager.getPicModel());
	}
	
}
