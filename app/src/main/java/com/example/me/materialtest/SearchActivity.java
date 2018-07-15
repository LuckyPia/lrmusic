package com.example.me.materialtest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class SearchActivity extends BaseActivity {
    private SearchView searchView;
    private ListView listView;
    private List<iconInformation> list;
    private List<iconInformation> findList;
    private listViewAdapter adapter;
    private List<iconInformation> nameList;
    //private MusicDao dao = null;
    MyApp app=null;
    List<Mp3Info> mp3List = new ArrayList<Mp3Info>();
    List<Mp3Info> favoriteList = new ArrayList<Mp3Info>();
    private listViewAdapter findAdapter;
    protected float mFirstX;//触摸下去的位置
    protected float mCurrentX;//滑动时Y的位置
    private int mTouchShop=300;//最小滑动距离


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_search);

        listView = (ListView) findViewById(R.id.listView);
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);


        findList = new ArrayList<iconInformation>();
        nameList = new ArrayList<iconInformation>();
        //searchView.setSubmitButtonEnabled(true);

        app = (MyApp)getApplication();
        this.mp3List=app.mp3List;

        myAdapter();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //输入完成后，提交时触发的方法，一般情况是点击输入法中的搜索按钮才会触发，表示现在正式提交了
            public boolean onQueryTextSubmit(String query) {

                if (TextUtils.isEmpty(query)) {
                    show_Toast("请输入查找内容！");
                    listView.setAdapter(adapter);
                } else {
                    findList.clear();
                    for (int i = 0; i < list.size(); i++) {
                        iconInformation information = list.get(i);
                        if (information.getName().contains(query)||information.getSinger().contains(query)) {
                            findList.add(information);
                            continue;
                        }
                    }

                    if (findList.size() == 0) {
                        show_Toast("查找的音乐不在列表中");
                    } else {
                        show_Toast("查找成功,共有"+findList.size()+"项符合！");
                        findAdapter = new listViewAdapter(SearchActivity.this, findList);
                        findAdapter.notifyDataSetChanged();
                        listView.setAdapter(findAdapter);
                    }
                }
                return true;
            }

            //在输入时触发的方法，当字符真正显示到searchView中才触发，像是拼音，在输入法组词的时候不会触发
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    listView.setAdapter(adapter);
                } else {
                    findList.clear();
                    for (int i = 0; i < list.size(); i++) {
                        iconInformation information = list.get(i);
                        if (information.getName().contains(newText)||information.getSinger().contains(newText)) {
                            findList.add(information);
                            continue;
                        }
                    }
                    findAdapter = new listViewAdapter(SearchActivity.this, findList);
                    listView.setAdapter(findAdapter);
                    findAdapter.notifyDataSetChanged();

                }
                return true;
            }
        });

        listView.setOnTouchListener(new View.OnTouchListener() {//listview的触摸事件
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mFirstX = event.getX();//按下时获取位置
                        break;

                    case MotionEvent.ACTION_MOVE:
                        mCurrentX = event.getX();//得到滑动的位置
                        if(mCurrentX - mFirstX > mTouchShop){//滑动的位置减去按下的位置大于最小滑动距离  则表示向下滑动
                            show_Toast("退出");
                            SearchActivity.this.finish();
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        break;

                }
                return false;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int  position, long l) {
                int i=findAdapter.getI(position);
                PlayFlag pf=new PlayFlag();
                pf.setPlayflag(0);
                Intent sixth_activity= new Intent(SearchActivity.this, SixthActivity.class);
                sixth_activity.putExtra("old_activity",i);
                startActivity(sixth_activity);
            }
        });

    }

    private void myAdapter(){
        list = new ArrayList<iconInformation>();
        for (int i = 0; i < mp3List.size(); i++) {
            iconInformation iconInfo = new iconInformation();
            iconInfo.setName(mp3List.get(i).getName());
            iconInfo.setSinger(mp3List.get(i).getSinger());
            iconInfo.setI(i);
            list.add(iconInfo);
        }
        adapter = new listViewAdapter(SearchActivity.this, list);
        listView.setAdapter(adapter);
        for(int i = 0; i < list.size(); i++)
        {
            iconInformation information = list.get(i);
            nameList.add(information);
        }
        findAdapter = new listViewAdapter(SearchActivity.this, nameList);
        listView.setAdapter(findAdapter);
    }


}
