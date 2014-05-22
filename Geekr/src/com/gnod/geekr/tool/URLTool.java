package com.gnod.geekr.tool;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import android.os.Bundle;

public class URLTool {

	public static String encodeURL(Map<String, String> param) {
		if(param == null)
			return "";
		
		StringBuilder builder = new StringBuilder();
		Set<String> keys = param.keySet();
		boolean isFirstParam = true; 
		String value;
		for(String key: keys) {
			value = param.get(key);
			if(!StringUtils.isNullOrEmpty(value) || key.equals("description")
					|| key.equals("url")){
				if(isFirstParam) {
					isFirstParam = false;
				} else {
					builder.append("&");
				}
				
				try {
					builder.append(URLEncoder.encode(key, "UTF-8"))
					.append("=")
					.append(URLEncoder.encode(param.get(key), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
				}
			}
		}
		return builder.toString();
	}
	
	public static Bundle decodeURL(String arg) {
		Bundle params = new Bundle();
		if(!StringUtils.isNullOrEmpty(arg)){
			String[] array = arg.split("&");
			for(String str: array){
				String[] keyValue = str.split("=");
				try {
					params.putString(URLDecoder.decode(keyValue[0], "UTF-8"), 
							URLDecoder.decode(keyValue[1], "UTF-8"));
				} catch (UnsupportedEncodingException e) {
				}
			}
		}
		
		return params;
	}
}
