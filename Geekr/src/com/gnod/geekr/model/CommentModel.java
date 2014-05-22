package com.gnod.geekr.model;

import java.io.Serializable;
import java.util.Date;

public class CommentModel implements Serializable{

	public int type;

	public String ID;
	public String statusID;
	public String content;
	public String source;
	public Date time;

	public UserInfoModel userInfo;
	public StatusModel status;
	public CommentModel replyComment;
}
