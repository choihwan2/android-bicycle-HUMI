package com.tempoup.sdj.humi;

public class RankListViewItem {
    private int icon;
    private String name;
    public int getIcon(){return icon;}
    public String getName(){return name;}
    public RankListViewItem(int icon,String name){
        this.icon=icon;
        this.name=name;
    }
}