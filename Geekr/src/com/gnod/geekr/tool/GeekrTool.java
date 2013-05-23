package com.gnod.geekr.tool;

import android.content.ClipData;
import android.content.Context;
import android.text.ClipboardManager;

import com.gnod.geekr.app.AppConfig;

public class GeekrTool {

	public static void copyTextToClipboard(String text) {
		Context c = AppConfig.getInstance();
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB){
			android.text.ClipboardManager clipboard = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
		} else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData data = ClipData.newPlainText("Copied Text", text);
			clipboard.setPrimaryClip(data);
		}
		ToastHelper.show("已复制");
	}
}
