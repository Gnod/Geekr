package com.gnod.geekr.app.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gnod.geekr.R;
import com.gnod.geekr.app.AppConfig;
import com.gnod.geekr.model.CommentModel;
import com.gnod.geekr.model.StatusModel;
import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.tool.DateUtils;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.manager.DrawableManager;
import com.gnod.geekr.widget.AvatarView;

public class StatusDetailAdapter extends BaseAdapter {

	private Context mContext;
	private StatusModel mItemModel;
	private ArrayList<CommentModel> mList;
	private DrawableManager mDrawableMgr;
	
	public StatusDetailAdapter(Context context, StatusModel itemModel, ArrayList<CommentModel> commentList) {
		this.mContext = context;
		this.mItemModel = itemModel;
		this.mList = commentList;
		mDrawableMgr = AppConfig.getDrawableManager();
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
			StatusDetailCommentView commentView = null;
			if(convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_detail_comment, null);
				commentView = new StatusDetailCommentView();
				
				commentView.imageAvatar = (AvatarView)convertView.findViewById(R.id.itemimage_comment_avatar);
				commentView.textName = (TextView) convertView.findViewById(R.id.itemtext_comment_name);
				commentView.textContent = (TextView)convertView.findViewById(R.id.itemtext_comment_content);
				commentView.textTime = (TextView) convertView.findViewById(R.id.itemtext_comment_time);
				commentView.textSource = (TextView)convertView.findViewById(R.id.itemtext_comment_from);
				convertView.setTag(commentView);
			} else {
				commentView = (StatusDetailCommentView)convertView.getTag();
			}
			CommentModel comment = mList.get(position);
			commentView.imageAvatar.setImageResource(R.drawable.avatar_default);
			UserInfoModel userInfo = comment.userInfo;
			commentView.imageAvatar.setItem(userInfo);
			
			mDrawableMgr.loadBitmap(userInfo.iconURL, commentView.imageAvatar, true);
			commentView.textName.setText(userInfo.nickName);
			
			commentView.textContent.setText(comment.content);
			if (StringUtils.isNullOrEmpty(comment.content)) {
				commentView.textContent.setVisibility(View.GONE);
			} else {
				commentView.textContent.setVisibility(View.VISIBLE);
			}
			commentView.textTime.setText(DateUtils.getMagicTime(comment.time));
			if(!StringUtils.isNullOrEmpty(comment.source))
				commentView.textSource.setText(Html.fromHtml(comment.source).toString());
		return convertView;
	}

	
	
	public class StatusDetailCommentView {
		public AvatarView imageAvatar;
		public TextView textName;
		public TextView textContent;
		public TextView textTime;
		public TextView textSource;
	}
}
