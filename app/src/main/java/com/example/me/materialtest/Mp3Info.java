package com.example.me.materialtest;

import java.io.Serializable;

public class Mp3Info implements Serializable{
    //	private static final long serialVersionUID = 44L;
    private String mName;
    private int mDuration;
    private String mPath;
    private String mSinger;
    private  String mFavorite;

    public Mp3Info(){

    }

    public Mp3Info(String name,String singer, int duration, String path, String favorite){
        mName = name;
        mSinger=singer;
        mDuration = duration;
        mPath = path;
        mFavorite = favorite;
    }

    public String getName() {
        return mName;
    }
    public void setName(String name) {
        mName = name;
    }

    public int getDuration() {
        return mDuration;
    }
    public void setDuration(int duration) {
        mDuration = duration;
    }

    public String getSinger() {
        return mSinger;
    }
    public void setSinger(String singer) {
        mSinger = singer;
    }

    public String getPath() {
        return mPath;
    }
    public void setPath(String path) {
        mPath = path;
    }

    public String getFavorite() {
        return mFavorite;
    }
    public void setFavorite(String favorite) {
        mFavorite = favorite;
    }
    public String toString(){
        return mName +" " + mDuration + " " + mPath;
    }
}
