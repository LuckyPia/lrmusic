package com.example.me.materialtest;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.app.Notification.VISIBILITY_PUBLIC;

/**
 * Created by NIWA on 2017/3/17.
 */

public class MediaService extends Service {

    private static final String TAG = "MediaService";
    private MyBinder mBinder = new MyBinder();
    //标记当前歌曲的序号
    private static int i = 0;
    private static  String path;
    private Preference p;
    private String type;
    List<Mp3Info> mp3List = new ArrayList<Mp3Info>();
    List<Mp3Info> favoriteList = new ArrayList<Mp3Info>();
    List<Mp3Info> playList=new ArrayList<Mp3Info>();//音乐准备播放列表

    public NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;
    private RemoteViews contentView;
    //初始化MediaPlayer
    private static MediaPlayer mediaPlayer = null;
    private Callback callback;
    private String data = "服务正在运行";


    public MediaPlayer mMediaPlayer=getMedia();
    private MusicDao dao = null;

    private Notification notification;
    private RemoteViews remoteViews;
    private ButtonBroadcastReceiver playerReceiver;

    private NotificationManager manager;
    MyApp app=null;

    static MediaPlayer getMedia(){
        //单例模式
        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
        }
        return mediaPlayer;
    }
    private void getMusic(){
        dao = MusicDao.getInstance(this);
        dao.init();
        dao.query(mp3List,favoriteList);
        dao.close();
        /*app = (MyApp)getApplication();
        this.mp3List=app.mp3List;
        this.favoriteList=app.favoriteList;*/
    }

    @Override
    public void onCreate() {
        super.onCreate();
        playerReceiver = new ButtonBroadcastReceiver();
        IntentFilter mFilter=new IntentFilter();
        mFilter.addAction("play");
        mFilter.addAction("precious");
        mFilter.addAction("next");
        mFilter.addAction("open");
        registerReceiver(playerReceiver,mFilter);

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String channelId ="chat";
            String channelName ="聊天消息";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

            channelId ="subscribe";
            channelName ="订阅消息";
            importance = NotificationManager.IMPORTANCE_DEFAULT;

            createNotificationChannel(channelId, channelName, importance);
            channelId ="321";
            channelName ="通知栏播放控制";
            importance = NotificationManager.IMPORTANCE_LOW;
            createNotificationChannel(channelId, channelName, importance);
        }
        initNotificationBar();
    }

    public MediaService() {
        //File sdcardFile = new File("/mnt/sdcard/music");

        //File sdcardFile = Environment.getExternalStorageDirectory();
        try{
            //getMp3FromSDcard(sdcardFile);
            getMusic();
            //默认播放列表为mp3List
            if(PlayFlag.getPlayflag()==0){
                playList=mp3List;
            }else{
                playList=favoriteList;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        i = Flag.Getflag();
        iniMediaPlayerFile(i);


        //播放完成监听
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp ) {
                //int playflag=PlayFlag.getPlayflag();
                p = Preference.getInstance(MediaService.this);
                type = p.get();

                switch (type){
                    case "0":
                        if(i==playList.size()-1){
                            i=0;
                            iniMediaPlayerFile(i);
                            mMediaPlayer.start();

                        }else {
                            i++;
                            iniMediaPlayerFile(i);
                            mMediaPlayer.start();
                        }
                        break;
                    case "1":
                        iniMediaPlayerFile(i);
                        mMediaPlayer.start();
                        break;
                    case "2":
                        Random rand = new Random();
                        i = rand.nextInt(playList.size());
                        iniMediaPlayerFile(i);
                        mMediaPlayer.start();
                        break;
                }
                initNotificationBar();
                sendMessage_1();

            }
        });

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {

        /*
         *  获取MediaService.this（方便在ServiceConnection中）
        *
         * *//*
        public MediaService getInstance() {
            return MediaService.this;
        }*/

        public void setData(String data){
            MediaService.this.data = data;
        }
        public MediaService getMyService(){
            return MediaService.this;
        }

        /**
         * 播放音乐
         */
        public void playMusic() {
            if (!mMediaPlayer.isPlaying()) {
                //如果还没开始播放，就开始
                mMediaPlayer.start();
            }
            initNotificationBar();
        }

        /**
         * 暂停播放
         */
        public void pauseMusic() {
            if (mMediaPlayer.isPlaying()) {
                //暂停
                mMediaPlayer.pause();
            }
            initNotificationBar();
        }

        public void ppMusic(){
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
            }else{
                mMediaPlayer.start();
            }
            initNotificationBar();
        }
        /**
         * reset
         */
        public void resetMusic() {
            if (mMediaPlayer.isPlaying()) {
                //重置
                mMediaPlayer.reset();
            }
        }

        /**
         * 关闭播放器
         */
        public void closeMedia() {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                //mMediaPlayer.reset();
                //mMediaPlayer.release();
            }
        }

        /**
         * 下一首
         */
        public void nextMusic() {
            if (mMediaPlayer != null) {
                //切换歌曲reset()很重要很重要很重要，没有会报IllegalStateException
                mMediaPlayer.reset();
                p = Preference.getInstance(MediaService.this);
                type = p.get();
                if (type.equals("2")) {
                    Random rand = new Random();
                    i = rand.nextInt(playList.size());
                    iniMediaPlayerFile(i);
                    playMusic();
                } else {
                    //不让歌曲的序号越界
                    if (i == playList.size() - 1) {
                        i = 0;
                        iniMediaPlayerFile(i);
                        playMusic();
                    } else {
                        i = i + 1;
                        iniMediaPlayerFile(i);
                        playMusic();
                    }
                }
            }
            initNotificationBar();
            sendMessage_1();
        }

        /**
         * 上一首
         */
        public void preciousMusic() {
            if (mMediaPlayer != null) {
                //mMediaPlayer.reset();
                if (i == 0) {
                    i=playList.size()-1;
                    iniMediaPlayerFile(i);
                    mMediaPlayer.start();
                } else {
                    i = i - 1;
                    iniMediaPlayerFile(i);
                    mMediaPlayer.start();
                }
            }
            initNotificationBar();
            sendMessage_1();
        }

        /*
         *切换音乐时
        */
        public void newMusic(){
            if(PlayFlag.getPlayflag()==1){
                getMusic();
                playList=favoriteList;
            }else{
                playList=mp3List;
            }

            if(i != Flag.Getflag()){
                i = Flag.Getflag();
                iniMediaPlayerFile(i);
                mMediaPlayer.start();
             }else {
                if (isPlaying()) {
                    mMediaPlayer.pause();
                } else {
                    mMediaPlayer.start();
                }
            }
            initNotificationBar();
            sendMessage_1();

        }
        /**
         * 获取歌曲长度
         **/
        public int getProgress() { return mMediaPlayer.getDuration(); }

        /**
         * 获取播放位置
         */
        public int getPlayPosition() {
            return mMediaPlayer.getCurrentPosition();
        }

        public int getPlayNum() {
            return i;
        }
        /**
         * 获取当前播放歌曲序号
         */
        public int getNowPlayPosition() {
            return i;
        }
        /**
         * 播放指定位置
         */
        public void seekToPositon(int msec) {
            mMediaPlayer.seekTo(msec);
        }
        public String getSongName(){
            return playList.get(i).getName();
        }
        public String getSingerName(){
            return  playList.get(i).getSinger();
        }
        public Boolean isPlaying(){
            return  mMediaPlayer.isPlaying();
        }
        public  int getAudioSessionId(){
            return mMediaPlayer.getAudioSessionId();
        }
        public void newList(){
            playList.clear();
            try{
                getMusic();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }


    /**
     * 添加file文件到MediaPlayer对象并且准备播放音频
     */
    private void iniMediaPlayerFile(int dex) {
        //获取文件路径
        try {
            //此处的两个方法需要捕获IO异常
            mMediaPlayer.reset();
            //设置音频文件到MediaPlayer对象中
            mMediaPlayer.setDataSource(playList.get(i).getPath());
            //播放历史列表，待完善
            //playList.add(i);
            //让MediaPlayer对象准备
            mMediaPlayer.prepare();
        } catch (IOException e) {
            Log.d(TAG, "设置资源出错");
            e.printStackTrace();
        }
    }



    public class ButtonBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();//获取action标记，用户区分点击事件
            if ("play".equals(action)) {
                mBinder.ppMusic();
                sendMessage_1();
                System.out.println("play");
            }
            else if ("next".equals(action)) {
                mBinder.nextMusic();
                sendMessage_1();
                System.out.println("next");
            } else if ("precious".equals(action)) {
                mBinder.preciousMusic();
                sendMessage_1();
                System.out.println("precious");
            }else if ("open".equals(action)) {
                Intent open=new Intent(MediaService.this,SixthActivity.class);
                startActivity(open);
                try {
                    @SuppressLint("WrongConstant") Object statusBarManager = context.getSystemService("statusbar");
                    Method collapse;

                    if (Build.VERSION.SDK_INT <= 16) {
                        assert statusBarManager != null;
                        collapse = statusBarManager.getClass().getMethod("collapse");
                    } else {
                        assert statusBarManager != null;
                        collapse = statusBarManager.getClass().getMethod("collapsePanels");
                    }
                    collapse.invoke(statusBarManager);
                } catch (Exception localException) {
                    localException.printStackTrace();
                }
                System.out.println("open");
            }
        }
    }

    public void newNotificationBar(){
        contentView.setTextViewText(R.id.song_name, mBinder.getSongName());
        contentView.setTextViewText(R.id.song_singer, mBinder.getSingerName());
        if(mMediaPlayer.isPlaying()){
            contentView.setImageViewResource(R.id.n_play,R.drawable.ic_stop);
        }else{
            contentView.setImageViewResource(R.id.n_play,R.drawable.ic_play);
        }
    }


    public void initNotificationBar() {
        //初始化通知
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //如果是8以上的系统。需要传一个channelId.
            builder = new NotificationCompat.Builder(this, "321");
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        updateAppWidget();

        contentView = new RemoteViews(getPackageName(), R.layout.customnotice);
        //更新UI
        newNotificationBar();

        Intent intentPlay = new Intent("play");//新建意图，并设置action标记为"play"，用于接收广播时过滤意图信息
        PendingIntent pIntentPlay = PendingIntent.getBroadcast(this, 0, intentPlay, 0);
        contentView.setOnClickPendingIntent(R.id.n_play, pIntentPlay);//为play控件注册事件

        Intent intentNext = new Intent("next");
        PendingIntent pIntentNext = PendingIntent.getBroadcast(this, 0, intentNext, 0);
        contentView.setOnClickPendingIntent(R.id.n_next, pIntentNext);

        Intent intentPrecious = new Intent("precious");
        PendingIntent pIntentPrecious = PendingIntent.getBroadcast(this, 0, intentPrecious, 0);
        contentView.setOnClickPendingIntent(R.id.n_precious, pIntentPrecious);

        Intent intentOpen = new Intent("open");
        PendingIntent pIntentOpen = PendingIntent.getBroadcast(this, 0, intentOpen, 0);
        contentView.setOnClickPendingIntent(R.id.notificationbar,pIntentOpen);

        /*Intent intentCancel = new Intent("cancel");
        PendingIntent pIntentCancel = PendingIntent.getBroadcast(this, 0,
                intentCancel, 0);
        contentView.setOnClickPendingIntent(R.id.bt_notic_cancel, pIntentCancel);*/

        //builder.setPriority(Notification.PRIORITY_DEFAULT);
        //builder.setWhen(System.currentTimeMillis());
        //builder.setTicker("正在播放");

        /* VISIBILITY_PRIVATE : 显示基本信息，如通知的图标，但隐藏通知的全部内容
        *  VISIBILITY_PUBLIC : 显示通知的全部内容
        *  VISIBILITY_SECRET : 不显示任何内容，包括图标
        */
        builder.setVisibility(VISIBILITY_PUBLIC);
        builder.setSound(null);
        builder.setSmallIcon(R.drawable.n_music);

        builder.setOngoing(true);//true：必须手动清除代码
        //builder.setAutoCancel(true);
        builder.setContent(contentView);
        builder.setSound(null);
        //builder.setDefaults(Notification.DEFAULT_VIBRATE);
        //builder.setContentTitle("12321312");
        //builder.setContentText("21312312231");

        notification = builder.build();

        //notification.flags = notification.FLAG_NO_CLEAR;//设置通知点击或滑动时不被清除
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        //manager.notify(200, notification);//开启通知
        startForeground(1,notification);

    }
    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName,int importance){

        NotificationChannel channel =new NotificationChannel(channelId, channelName, importance);
        channel.setSound(null,null);

        NotificationManager notificationManager = (NotificationManager) getSystemService(

                NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);

    }

    public void updateAppWidget(){
        Log.v("fasong","dao");
        Bundle bundle = new Bundle();
        bundle.putBoolean("isplay",mBinder.isPlaying());
        bundle.putString("song_name",mBinder.getSongName());
        bundle.putString("song_singer",mBinder.getSingerName());
        Intent intent = new Intent("update_now");
        Log.v("fasong",intent.getAction());
        intent.putExtras(bundle);
        //android8.0必须设置，否则发不出去action！！！！！
        intent.setComponent(new ComponentName(this,MyAppWidgetProvider.class));
        sendBroadcast(intent);
    }

    //与activity通信
    private void sendMessage_1(){
        callback.onDataChange(data);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public static interface Callback{
        void onDataChange(String data);
    }


    @Override
    public void onDestroy() {
        stopForeground(true);
        if (playerReceiver != null) {
            unregisterReceiver(playerReceiver);
        }
        super.onDestroy();
    }
}
