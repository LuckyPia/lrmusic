package com.example.me.materialtest;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.Toast;

public class MusicDao {
	private static MusicDao instance;
	MusicDataBaseHelper dbHelper;
	SQLiteDatabase db;
	Context ctx;

    //获取上下文
	private MusicDao(Context ctx){
		this.ctx = ctx;
	}

	static MusicDao getInstance(Context ctx){
		if(instance == null){
			instance = new MusicDao(ctx);
		}
		return instance;
	}

	void init(){
		dbHelper = new MusicDataBaseHelper(ctx, "musicdatabase", null, 1);
		db = dbHelper.getWritableDatabase();
	}

	public void delete() {
		System.out.println("delete table.....");
		//db.delete("songs",null,null);
		ctx.deleteDatabase("musicdatabase");
	}

	public boolean dbIsEmpty(){
		Cursor cursor=db.rawQuery("select * from songs",null);
		if(cursor.getCount()==0){
			return true;
		}else{
			return false;
		}
	}

	public void insert(Mp3Info song) {
		ContentValues cv = new ContentValues();
		cv.put("song_name", song.getName());
		cv.put("song_singer", song.getSinger());
		cv.put("song_duration", song.getDuration());
		cv.put("song_path", song.getPath());
		cv.put("song_favorite", song.getFavorite());
		db.insert("songs", null, cv);
		//db.execSQL("insert songs values(song.getName(),song.getSize(),song.getPath(),song.getFavorite()) ");
	}


	public void update(Mp3Info song) {
		ContentValues cv = new ContentValues();
		cv.put("song_favorite", song.getFavorite());
		String fav = song.getFavorite();
		String path = song.getPath();
		db.update("songs", cv, "song_path=?", new String[]{song.getPath()});
		//db.execSQL("update songs set song_favorite="+fav+" where song_path="+path);
	}

	public void query(List<Mp3Info> list, List<Mp3Info> favList) {
		System.out.println("query()!!!!!!!!!");
		Cursor c = db.query("songs", new String[] {"song_name", "song_singer","song_path", "song_duration", "song_favorite"}, null, null, null, null, null);
		while (c.moveToNext()) {
			try {
				String name = c.getString(c.getColumnIndex("song_name"));
				String singer= c.getString(c.getColumnIndex("song_singer"));
				int duration = c.getInt(c.getColumnIndex("song_duration"));
				String path = c.getString(c.getColumnIndex("song_path"));
				String favorite = c.getString(c.getColumnIndex("song_favorite"));
				Mp3Info info = new Mp3Info(name,singer, duration, path, favorite);
				System.out.println(info);
				list.add(info);
				if (favorite.equals("YES")) {
					favList.add(info);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		c.close();
	}

	void close(){
		db.close();
	}

	//自定义序列化协议，把mp3List的信息序列化到磁盘文件
	/*static void saveToDisk(MyApp app){
		FileWriter fw = null;
		try{
			fw = new FileWriter(MyApp.songListFile);
			int num = app.mp3List.size();
			Mp3Info song = null;

			for(int i = 0; i < num; i++){
				StringBuffer sb = new StringBuffer();
				song = app.mp3List.get(i);
				sb.append(song.getName() + "@");
				sb.append(song.getSize() + "@");
				sb.append(song.getPath() + "@");
				sb.append(song.getFavorite() + "");
				System.out.println(sb.toString());
				fw.write(sb.toString());
				fw.write("\n");
			}
		}catch(IOException e){
			e.printStackTrace();
		} finally {
			try {
				if(fw != null)
					fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}*/

	//把磁盘文件的信息反序列化到mp3List
	/*static void readListFromDisk(MyApp app) {
		BufferedReader br = null;
		String line = null;
		String[] songInfo = null;
		try{
			br = new BufferedReader(new FileReader(MyApp.songListFile));
			while(true){
				line = br.readLine();
				if(line == null){
					break;
				}
				songInfo = line.split("@");
				Mp3Info song = new Mp3Info(songInfo[0], songInfo[1],songInfo[2],songInfo[3]);
				app.mp3List.add(song);
				if(songInfo[3].equals("YES")){
					app.favoriteList.add(song);
				}
				System.out.println(song);
			}
		} catch(IOException e){
			e.printStackTrace();
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}*/

	//借用JDK默认的序列化协议，把mp3List的信息序列化到磁盘文件
	static void saveToDisk_serialize(MyApp app){
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(
					new FileOutputStream(MyApp.songListFile));
			oos.writeObject(app.mp3List);
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(oos != null){
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static void readListFromDisk_serialize(MyApp app) {
		ObjectInputStream oos = null;
		try {
			oos = new ObjectInputStream(
					new FileInputStream(MyApp.songListFile));
			app.mp3List = (List<Mp3Info>) oos.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(oos != null){
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}



}