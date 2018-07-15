package com.example.me.materialtest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

public class FavoriteListActivity extends BaseActivity{
    private static android.support.v7.app.ActionBar actionBar;
    private FavoriteListAdapter adapter;
    protected float mFirstX;//触摸下去的位置
    protected float mCurrentX;//滑动时Y的位置
    private int mTouchShop=300;//最小滑动距离

    private ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        Transition slide = TransitionInflater.from(this).inflateTransition(R.transition.slide);
        //退出时使用
        getWindow().setExitTransition(slide);
        //第一次进入时使用
        getWindow().setEnterTransition(slide);
        //再次进入时使用
        getWindow().setReenterTransition(slide);

        setContentView(R.layout.favorite);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle("收藏夹");
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        showList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter = new FavoriteListAdapter((MyApp)getApplication());
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showList(){
        lv = (ListView)findViewById(R.id.favorite_list);
        adapter = new FavoriteListAdapter((MyApp)getApplication());
        lv.setAdapter(adapter);
        //为ListView设置点击监听
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int  position, long l) {
                int i=position;
                PlayFlag pf=new PlayFlag();
                pf.setPlayflag(1);
                Intent sixth_activity= new Intent(FavoriteListActivity.this, SixthActivity.class);
                sixth_activity.putExtra("old_activity",i);
                startActivity(sixth_activity, ActivityOptions.makeSceneTransitionAnimation(FavoriteListActivity.this).toBundle());
            }
        });
        lv.setOnTouchListener(new View.OnTouchListener() {//listview的触摸事件
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
                            FavoriteListActivity.this.finish();
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        break;

                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
