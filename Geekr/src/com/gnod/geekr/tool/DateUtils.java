package com.gnod.geekr.tool;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
	
	private static final int MIN_MILL = 60 * 1000;
	private static final int HOUR_MILL = 60 * MIN_MILL;
	private static final int DAY_MILL = 24 * HOUR_MILL;
	
	private static final String JUST_NOW = "刚刚";
	private static final String SEC_AGO = "秒前";
	private static final String MIN_AGO = "分钟前";
	private static final String TODAY = "今天";
	private static final String YESTERDAY = "昨天";
	
	private static final String DAY_FORMAT = "HH:mm";
	private static final String DATE_FORMAT = "MM/dd HH:mm";
	private static final String YEAR_FORMAT = "yyyy/MM/dd HH:mm";
	
	
	private static SimpleDateFormat dayFormat;
	private static SimpleDateFormat dateFormat;
	private static SimpleDateFormat yearFormat;
	private static Calendar calCalender;
	
	/**
	 * Sina Weibo 日期格式为 EEE MMM d HH:mm:ss Z yyyy
	 */
	static SimpleDateFormat weiboSdf = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.ENGLISH);
	private static DateFormat datTagFormat;
	
	
	/**
	 * 根据传入时间自动返回适合显示的对应时间字符串 <br>
	 * <60s  		刚刚<br>
	 * <1h			分钟显示<br>
	 * <1d 			HH:mm显示<br>
	 * <2d 			昨天<br>
	 * 当前同一年      	MM/dd HH:mm<br>
	 * 其它			yyyy/MM/dd HH:mm <br>
	 * 
	 */
	public static String getMagicTime(Date date) {
		long mills = date.getTime();
		long nowMills = System.currentTimeMillis();
		
		Calendar nowCalendar = Calendar.getInstance();
		
		if(calCalender == null) 
			calCalender = Calendar.getInstance();
		
		calCalender.setTimeInMillis(mills);
		
		long secElapse = (nowMills - mills) / 1000;
		
		if(secElapse < 60)
			return JUST_NOW;
		
		long minElapse = secElapse / 60;
		if(minElapse < 60)
			return new StringBuilder().append(minElapse).append(MIN_AGO).toString();
		
		long hourElapse = minElapse / 60;
		if(hourElapse < 24 && isSameDay(calCalender, nowCalendar)) {
			if(dayFormat == null) 
				dayFormat = new SimpleDateFormat(DAY_FORMAT);
			
			String result = dayFormat.format(date);
			return new StringBuilder().append(TODAY).append(" ").append(result).toString();
		}
		
		long dayElapse = hourElapse / 24;
		if(dayElapse == 1) {
			if(dayFormat == null) 
				dayFormat = new SimpleDateFormat(DAY_FORMAT);
			
			String result = dayFormat.format(date);
			return new StringBuilder().append(YESTERDAY).append(" ").append(result).toString();
		}
		
		if(isSameYear(calCalender, nowCalendar)){
			if(dateFormat == null)
				dateFormat = new SimpleDateFormat(DATE_FORMAT);
			
			String result = dateFormat.format(date);
			return new StringBuilder().append(result).toString();
		} 
		
		if(yearFormat == null) 
			yearFormat = new SimpleDateFormat(YEAR_FORMAT);
		
		String result = yearFormat.format(date);
		return new StringBuilder().append(result).toString();
	}
	
	private static boolean isSameDay(Calendar calDay, Calendar now) {
		return calDay.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);
	}
	
	private static boolean isSameYear(Calendar calDay, Calendar now) {
		return calDay.get(Calendar.YEAR) == now.get(Calendar.YEAR);
	}
	
	public static String getFullTime(Date date) {
		if(yearFormat == null) 
			yearFormat = new SimpleDateFormat(YEAR_FORMAT);
		
		String result = yearFormat.format(date);
		return new StringBuilder().append(result).toString();
	}
	
	public static Date convertSinaWeiboDateStringToDate(String rawDate){
		if(StringUtils.isNullOrEmpty(rawDate))
			return new Date();
		
		if(weiboSdf == null)
			weiboSdf = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.ENGLISH);

	    Date resultDate = null;
		try {
			resultDate = weiboSdf.parse(rawDate);			
		} catch (ParseException e) {
			resultDate = new Date();
			return resultDate;
		}
		return resultDate;
	}
	
	public static String getDateTag(Date date)
	{
		if(date == null)
			return "";
		if(datTagFormat == null)
			datTagFormat = new SimpleDateFormat("yyyy_MMdd_HHmmss", Locale.ENGLISH);
		return datTagFormat.format(date);	
	}
	
	
}
