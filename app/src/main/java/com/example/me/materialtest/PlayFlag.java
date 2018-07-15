package com.example.me.materialtest;
//用于判断当前播放哪个列表
public class PlayFlag {
    static  int playflag=0;
    public static int getPlayflag(){
        return playflag;
    }
    public  void setPlayflag( int playflag){
        this.playflag=playflag;
    }
}
