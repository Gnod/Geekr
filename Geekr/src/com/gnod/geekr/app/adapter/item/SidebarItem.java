package com.gnod.geekr.app.adapter.item;


public class SidebarItem {
	public int type;
	public String name;
	public int iconId;
	
	public SidebarItem(int type, String name, int iconId) {
		this.type = type;
		this.name = name;
		this.iconId = iconId;
	}
	
	public SidebarItem(int type, String name) {
		this(type, name, -1);
	}
}
