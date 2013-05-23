package com.gnod.geekr.widget;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Browser;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.gnod.geekr.model.UserInfoModel;
import com.gnod.geekr.tool.ImageHelper;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.ui.activity.ProfileActivity;
import com.gnod.geekr.ui.activity.TopicsActivity;

public class ImageTextView extends TextView {

	public ImageTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ImageTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImageTextView(Context context) {
		super(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		Spanned span = convertAtTag(text);
		span = convertEmotion(span);
		span = convertTopicTag(span);
		span = convertURLTag(span);
		super.setText(span, type);
	}
	
	private Spanned convertAtTag(CharSequence text) {
		SpannableString span = new SpannableString(text);
		Pattern p = Pattern.compile("@([\\w-]+)");
		Matcher m = p.matcher(text);
		while(m.find()){
			HightLightSpan urlSpan = new HightLightSpan(m.group(1), 
					HightLightSpan.TAG_AT);
			span.setSpan(urlSpan, m.start(), m.end(), 
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return span;
	}
	
	private Spanned convertTopicTag(CharSequence text) {
		SpannableString span = new SpannableString(text);
		Pattern p = Pattern.compile("#([\\w-\\. \\*]*)#");
		Matcher m = p.matcher(text);
		while(m.find()){
			HightLightSpan urlSpan = new HightLightSpan(m.group(1), 
					HightLightSpan.TAG_TOPIC);
			span.setSpan(urlSpan, m.start(), m.end(), 
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return span;
	}
	private Spanned convertURLTag(CharSequence text) {
		SpannableString span = new SpannableString(text);
		Pattern p = Pattern.compile("(http|https|ftp)://[\\w\\.-/]*");
		Matcher m = p.matcher(text);
		while(m.find()){
			HightLightSpan urlSpan = new HightLightSpan(m.group(), 
					HightLightSpan.TAG_URL);
			span.setSpan(urlSpan, m.start(), m.end(), 
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return span;
	}
	
	private Spanned convertEmotion(CharSequence text) {
		SpannableString span = new SpannableString(text);
		Set<String> keys = ImageHelper.emotionsMap.keySet();
		Pattern p = Pattern.compile("\\[[^\\[\\]]+\\]");
		Matcher m = p.matcher(text);
		while(m.find()){
			try {
				String fileName = ImageHelper.emotionsMap.get(m.group());
				if(!StringUtils.isNullOrEmpty(fileName)){
					InputStream input = getContext().getAssets().open("smileys" + 
							File.separator + fileName);
					if(input != null) {
						Bitmap bitmap = BitmapFactory.decodeStream(input);
						bitmap = Bitmap.createScaledBitmap(bitmap, 32, 32, true);
						
						ImageSpan imageSpan = new ImageSpan(getContext(), bitmap);
						span.setSpan(imageSpan, m.start(), m.end(), 
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}
			} catch (IOException e) {
				Log.e("sms", "Failed to loaded content " + m.group(), e);
			}
		}
		return span;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//检测Clickable tag
		SpannableString span = new SpannableString(getText());
		if (detectClickableArea(this, span, event))
			return true;
		return super.onTouchEvent(event);
	}
	
    public boolean detectClickableArea(TextView widget, Spannable buffer,
                                MotionEvent event) {
        int action = event.getAction();

        try { //in some extermely case widget.getExternalPaddingTop() will throws NullPointerException
        	if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
				int x = (int) event.getX();
				int y = (int) event.getY();

				x -= widget.getTotalPaddingLeft();
				y -= widget.getTotalPaddingTop();

				x += widget.getScrollX();
				y += widget.getScrollY();

				Layout layout = widget.getLayout();
				int line = layout.getLineForVertical(y);
				int off = layout.getOffsetForHorizontal(line, x);

				ClickableSpan[] link = buffer.getSpans(off, off,
						ClickableSpan.class);

				if (link.length != 0) {
					if (action == MotionEvent.ACTION_UP) {
						link[0].onClick(widget);
					}
					return true;
				}
                }
		} catch (Exception e) {
			return false;
		}
        return false;
    }

	private class HightLightSpan extends ClickableSpan {
		public static final int TAG_AT = 0;
		public static final int TAG_URL = 1;
		public static final int TAG_TOPIC = 2;
		private String tag;
		private int type;
		
		public HightLightSpan(String tag, int type) {
			this.tag = tag;
			this.type = type;
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setColor(ds.linkColor);
			ds.setUnderlineText(false);
		}

		@Override
		public void onClick(View widget) {
			Intent intent;
			switch (type) {
			case TAG_AT:
				intent = new Intent(widget.getContext(), ProfileActivity.class);
				UserInfoModel userInfo = new UserInfoModel();
				userInfo.nickName = tag;
				intent.putExtra("UserInfoModel", userInfo);
				widget.getContext().startActivity(intent);
				break;
			case TAG_URL:
				Uri uri = Uri.parse(tag);
		        Context context = widget.getContext();
		        intent = new Intent(Intent.ACTION_VIEW, uri);
		        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
		        context.startActivity(intent);
				break;
			case TAG_TOPIC:
				intent = new Intent(widget.getContext(), TopicsActivity.class);
				intent.putExtra("Topics", tag);
				widget.getContext().startActivity(intent);				
				break;
			default:
				break;
			}
		}
	}
}
