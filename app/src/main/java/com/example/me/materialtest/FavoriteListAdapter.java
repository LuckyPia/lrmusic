package com.example.me.materialtest;

import android.widget.BaseAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class FavoriteListAdapter extends BaseAdapter{
    MyApp app;

    public FavoriteListAdapter(MyApp app) {
        this.app = app;
    }

    @Override
    public int getCount() {
        return app.favoriteList.size();
    }

    @Override
    public Object getItem(int position) {
        return app.favoriteList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(app);
        convertView = inflater.inflate(R.layout.favorite_list_item, null);
        TextView songName = (TextView)convertView.findViewById(R.id.fav_item_song_name);
        songName.setText(app.favoriteList.get(position).getName());
        TextView songSinger = (TextView)convertView.findViewById(R.id.fav_item_song_singer);
        songSinger.setText(String.valueOf(app.favoriteList.get(position).getSinger()));
        CheckBox cbx = (CheckBox)convertView.findViewById(R.id.favoriteItem_layout_checkbox);

        //点击从最喜欢列表中删除
        cbx.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicDao dao = MusicDao.getInstance(app);
                dao.init();
                Mp3Info song = app.favoriteList.get(position);
                app.favoriteList.remove(song);
                FavoriteListAdapter.this.notifyDataSetChanged();
                song.setFavorite("NO");
                dao.update(song);
                dao.close();
            }
        });
        return convertView;
    }

}
