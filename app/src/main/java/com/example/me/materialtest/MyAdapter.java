package com.example.me.materialtest;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_SHORT;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private int mCurrentItem = 0;
    private boolean isClick = false;
    private OnItemClickListener mOnItemClickListener;
    private Context ctx=null;
    MyApp app = null;

    public MyAdapter(MyApp app) {
        this.app = app;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ctx=parent.getContext();
        View view = LayoutInflater.from(ctx).inflate(R.layout.chat_item, parent, false);
        MyAdapter.ViewHolder viewHolder = new MyAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MyAdapter.ViewHolder holder, final int position) {
        final Mp3Info song = app.mp3List.get(position);
        System.out.print("名称:"+app.mp3List.get(position).getName());
        System.out.print("歌手:"+app.mp3List.get(position).getSinger());
        final boolean checked = app.favoriteList.contains(song);

        holder.songname.setText(song.getName());
        holder.songsinger.setText(song.getSinger());
        holder.musicduration.setText(TimerFormatter.formatterTime(song.getDuration()));

        if (mCurrentItem == position && isClick) {
            holder.songname.setTextColor(ctx.getResources().getColor(R.color.colorPrimary));
            holder.songsinger.setTextColor(ctx.getResources().getColor(R.color.colorPrimary));
        } else {
            holder.songname.setTextColor(ctx.getResources().getColor(R.color.bg_black));
            holder.songsinger.setTextColor(ctx.getResources().getColor(R.color.text_hui));
        }
        holder.cbx.setChecked(checked);
        //对checkbox进行点击事件注册
        holder.cbx.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(app.favoriteList.contains(song)){
                    song.setFavorite("NO");
                    ((MyApp) app).favoriteList.remove(song);
                    Toast.makeText(ctx,"已取消收藏",LENGTH_SHORT).show();
                    System.out.print("已取消收藏");

                }else{
                    song.setFavorite("YES");
                    ((MyApp) app).favoriteList.add(song);
                    Toast.makeText(ctx,"已添加收藏",LENGTH_SHORT).show();
                    System.out.print("已添加收藏");

                }
                app.updateSong(song);
            }
        });

        if(mOnItemClickListener!=null){
            holder.cardview_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(position);

                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(v);
                    return true;
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return app.mp3List.size();
    }
    public void setCurrentItem(int currentItem) {
        this.mCurrentItem = currentItem;
    }

    public void setClick(boolean click) {
        this.isClick = click;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView songname = null, songsinger = null,musicduration=null;
        CheckBox cbx = null;
        private CardView cardview_layout;

        ViewHolder(View itemView) {
            super(itemView);
            cardview_layout=(CardView) itemView.findViewById(R.id.item_song);
            songname = (TextView) itemView.findViewById(R.id.item_song_name);
            songsinger = (TextView) itemView.findViewById(R.id.item_song_size);
            musicduration=(TextView) itemView.findViewById(R.id.music_duration);
            cbx = (CheckBox) itemView.findViewById(R.id.songItem_layout_checkbox);

        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onItemLongClick(View view);
    }

    public void setOnItemListener(OnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener=mOnItemClickListener;
    }


}
