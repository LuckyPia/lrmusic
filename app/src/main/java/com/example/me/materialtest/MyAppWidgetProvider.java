package com.example.me.materialtest;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class MyAppWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        //views.setTextViewText(R.id.song_name, widgetText);
        //views.setTextViewText(R.id.song_singer, "歌手");
        Intent intentPlay = new Intent("play");//新建意图，并设置action标记为"play"，用于接收广播时过滤意图信息
        PendingIntent pIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, 0);
        views.setOnClickPendingIntent(R.id.n_play, pIntentPlay);//为play控件注册事件

        Intent intentNext = new Intent("next");
        PendingIntent pIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, 0);
        views.setOnClickPendingIntent(R.id.n_next, pIntentNext);

        Intent intentPrecious = new Intent("precious");
        PendingIntent pIntentPrecious = PendingIntent.getBroadcast(context, 0, intentPrecious, 0);
        views.setOnClickPendingIntent(R.id.n_precious, pIntentPrecious);

        Intent intentOpen = new Intent("open");
        PendingIntent pIntentOpen = PendingIntent.getBroadcast(context, 0, intentOpen, 0);
        views.setOnClickPendingIntent(R.id.notificationbar,pIntentOpen);

        appWidgetManager.updateAppWidget(appWidgetIds, views);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("jieshou", intent.getAction());
            if ("update_now".equals(intent.getAction())) {
                //接收数据
                Intent _intent = intent;
                Bundle b = _intent.getExtras();
                String sn = b.getString("song_name");
                String ss = b.getString("song_singer");
                Log.v("haode", ss);
                boolean ip = b.getBoolean("isplay");

                //更新 appwidget
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                remoteViews.setTextViewText(R.id.song_name, sn);
                remoteViews.setTextViewText(R.id.song_singer, ss);
                if (ip) {
                    remoteViews.setImageViewResource(R.id.n_play, R.drawable.ic_stop);
                } else {
                    remoteViews.setImageViewResource(R.id.n_play, R.drawable.ic_play);
                }
                appWidgetManager.updateAppWidget(new ComponentName(context, MyAppWidgetProvider.class), remoteViews);
            }
            super.onReceive(context,intent);
        }


}
