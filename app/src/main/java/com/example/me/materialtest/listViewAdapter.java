package com.example.me.materialtest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

/**
 * Created by ChuPeng on 2017/3/25.
 */

public class listViewAdapter extends BaseAdapter
{
    private Context context;
    private List<iconInformation> list;
    private LayoutInflater layoutInflater;
    private boolean isClick = false;
    private int mCurrentItem = 0;

    public listViewAdapter(Context context, List<iconInformation> list)
    {
        this.context = context;
        this.list = list;
        layoutInflater = LayoutInflater.from(context);
    }

    public int getCount()
    {
        return list.size();
    }
    public Object getItem(int position)
    {
        return list.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public int getI(int position){return list.get(position).getI();}

    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        View view;
        ViewHolder viewHolder;
        iconInformation iconInformation = list.get(position);
        if(convertView == null)
        {
            view = layoutInflater.inflate(R.layout.listview_item, null);
            viewHolder = new ViewHolder();
            viewHolder.iconName = (TextView) view.findViewById(R.id.iconName);
            viewHolder.iconSinger = (TextView) view.findViewById(R.id.iconSinger);
            view.setTag(viewHolder);
        }
        else
        {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.iconName.setText(iconInformation.getName());
        viewHolder.iconSinger.setText(iconInformation.getSinger());
        return view;
    }
    private class ViewHolder
    {
        TextView iconName,iconSinger;
    }
    public void setCurrentItem(int currentItem) {
        this.mCurrentItem = currentItem;
    }

    public void setClick(boolean click) {
        this.isClick = click;
    }
}
