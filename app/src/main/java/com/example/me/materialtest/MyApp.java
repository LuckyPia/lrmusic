package com.example.me.materialtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class MyApp extends Application{
    private static String TAG = "MyApp";
    String data = null;
    int CAPACITY = 10;
    int index = 0;
    Activity[] activities = new Activity[CAPACITY];
    //所有歌曲列表
    List<Mp3Info> mp3List = new ArrayList<Mp3Info>();
    //最喜爱歌曲列表
    List<Mp3Info> favoriteList = new ArrayList<Mp3Info>();
    String musicDataBase = null;
    static String songListFile = null;
    private MusicDao dao = null;
    File musicFolder;
    protected List<Activity> activityList;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");
        activityList = new ArrayList<Activity>();

        dao = MusicDao.getInstance(this);
        dao.init();
        if(!dao.dbIsEmpty()){
            System.out.println("已经存在自定义数据库文件");
            dao.query(mp3List, favoriteList);
        }else{
            System.out.println("还不存在自定义数据库文件");

            musicFolder = new File("/mnt/sdcard/music");
            Log.v("", musicFolder.toString());
            try {
                //getMp3FromSDcard(musicFolder);
                getMp3();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //把歌曲信息存入自定义的数据库文件
            for(Mp3Info info : mp3List){
                dao.insert(info);
            }
        }
        dao.close();
    }

    public void update(){
        dao = MusicDao.getInstance(this);
        mp3List.clear();
        dao.delete();
        dao.init();
        try {
            musicFolder = new File("/mnt/sdcard/music");
            //getMp3FromSDcard(musicFolder);
            getMp3();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //把歌曲信息存入自定义的数据库文件
        for(Mp3Info info : mp3List){
            dao.insert(info);
        }
        dao.close();

    }
    public boolean tableIsEmpty(){
        dao = MusicDao.getInstance(this);
        dao.init();
        if(!dao.dbIsEmpty()){
            dao.close();
            return false;
        }else{
            dao.close();
            return true;
        }
    }
    public void updateSong(Mp3Info song){
        MusicDao dao = MusicDao.getInstance(this);
        dao.init();
        dao.update(song);
        dao.close();
    }

    private void getMp3(){
        Uri uri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;//external_content_uri
        ContentResolver cr = getContentResolver();
        Cursor cursor=cr.query(uri, null, null, null, null);
        assert cursor != null;//尚不明确
        while(cursor.moveToNext())
        {
            if(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))/60000>=1&&
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))/60000<=8){
                String name;
                String singer;
                String songpath;
                int songduration=0;
                String songname=null;
                try {
                    songname = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    name = songname.substring(songname.lastIndexOf("-") + 1, songname.lastIndexOf("."));
                    singer=songname.substring(0, songname.lastIndexOf("-"));
                    //songpath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));//MediaStore.MediaColumns.DATA
                    songpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    songduration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
                if(songname.endsWith(".aac")){
                    continue;
                }
            Mp3Info song = new Mp3Info();
            song.setName(name);
            song.setSinger(singer);
            song.setPath(songpath);
            song.setDuration(songduration);
            song.setFavorite("NO");
            mp3List.add(song);
            }else{
                Log.d("MyApp","时长不够无法加入音乐列表！");
            }
        }
    }


    private void getMp3FromSDcard(File groupPath) throws IOException{
        System.out.println(groupPath.toString());
        File[] files = groupPath.listFiles();
        if (files == null) {
            System.out.println("files == null");
            return;
        } else {
            for(int i=0; i < files.length; i++){
                File childFile = files[i];
                System.out.println("childFile:" + childFile);
                if(childFile.isDirectory()) {
                    getMp3FromSDcard(childFile);
                } else {
                    if(childFile.toString().endsWith(".mp3")) {
                        Mp3Info info = new Mp3Info();
                        info.setName(childFile.getName().substring(childFile.getName().lastIndexOf("-") + 1, childFile.getName().lastIndexOf(".")));
                        info.setSinger(childFile.getName().substring(0, childFile.getName().lastIndexOf("-")));
                        info.setPath(childFile.getAbsolutePath());
                        info.setDuration(getFileSize(childFile));
                        info.setFavorite("NO");
                        mp3List.add(info);
                    }
                }
            }
        }
    }

    @Override
    public void onTerminate() {
        long id = Thread.currentThread().getId();
        Log.v(TAG, "onTerminate"+id);
        MusicDao.saveToDisk_serialize(this);
        super.onTerminate();
    }

    private int getFileSize(File f) throws IOException{
        FileInputStream fis = new FileInputStream(f);
        int size = fis.available();
        fis.close();
        return size;
    }
    /**
     * 添加Activity
     */
    public void addActivity_(Activity activity) {
// 判断当前集合中不存在该Activity
        if (!activityList.contains(activity)) {
            activityList.add(activity);//把当前Activity添加到集合中
        }
    }

    /**
     * 销毁单个Activity
     */
    public void removeActivity_(Activity activity) {
//判断当前集合中存在该Activity
        if (activityList.contains(activity)) {
            activityList.remove(activity);//从集合中移除
            activity.finish();//销毁当前Activity
        }
    }

    /**
     * 销毁所有的Activity
     */
    public void removeALLActivity_() {
        //通过循环，把集合中的所有Activity销毁
        for (Activity activity : activityList) {
            activity.finish();
        }
        activityList.clear();
    }
}
