package com.example.me.materialtest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MusicDataBaseHelper extends SQLiteOpenHelper {
    public MusicDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //数据库文件被创建的时候调用
    @Override
    public void onCreate(SQLiteDatabase db) {
        //arg0.create();
        db.execSQL("create table songs(_id int,song_name varchar[40],song_singer varchar[40], song_duration int, song_path varchar[100], song_favorite varchar[3])");
//        arg0.execSQL("CREATE TABLE " + "students" + " (" + "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                      "stu_id" + " INTEGER," + "stu_name" + " TEXT NOT NULL);");
    }

    //数据库升级的时候被调用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
         * 数据库升级的代码：
         * 比如：备份老数据库的信息，新建新数据库，把老数据导入新数据库
         */
    }
}
