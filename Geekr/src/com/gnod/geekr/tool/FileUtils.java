package com.gnod.geekr.tool;

import java.io.File;
import java.text.DecimalFormat;

public class FileUtils {

	/**
	 * 统计目录大小
	 * @param 
	 * @return Byte 
	 */
	public static long getDirSize(File dir) {
		if(dir == null) {
			return 0;
		}
		if(!dir.isDirectory()) {
			return 0;
		}
		long totalSize = 0;
		File[] files = dir.listFiles();
		for(File file: files){
			if(file.isFile()) {
				totalSize += file.length();
			} else if(file.isDirectory()) {
//				totalSize += file.length();
				totalSize +=getDirSize(file); //递归统计子目录大小
			}
		}
		return totalSize;
	}

	/**
	 *  删除指定目录
	 *  @param  curTime 系统当前时间
	 *  @return 删除文件数目(包括目录) 
	 */
	public static int deleteDir(File dir, long curTime) {
		int count = 0;
		if(dir == null) return 0;
		if(!dir.isDirectory()) return 0;
		
		File[] files = dir.listFiles();
		for(File file: files) {
			if(file.isDirectory()) {
				count += deleteDir(file, curTime);
			}
			if(file.lastModified() < curTime) {
				if(file.delete()) {
					++ count;
				}
			}
		}
		return count;
	}
	
	/**
	 * 文件大小转换函数
	 * @param size 
	 * @return  B/KB/MB/GB
	 */
	public static String formatSize(long size) {
		DecimalFormat df = new DecimalFormat("#.00");
		
		String fileSizeStr = "";
		if(size < 1024) { 
			fileSizeStr = df.format((double)size) + "B";
		} else if(size < 1048576) {
			fileSizeStr = df.format((double)size/1024) + "KB";
		} else if(size < 1073741824) {
			fileSizeStr = df.format((double)size/1048576) + "MB";
		} else {
			fileSizeStr = df.format((double)size /1073741824) + "GB";
		}
		return fileSizeStr;
	}
	
	public static String convertUrl(String url){
		return url.replaceAll("[/.:]", "");
	}
}
