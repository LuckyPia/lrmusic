package com.example.me.materialtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;



public class MainActivity extends BaseActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener {

    private DrawerLayout mDrawerLayout;

    private static String TAG = "MainActivity";//当前Activity标记
    MyApp app = null;//加载MyApp
    private MyAdapter adapter;
    Intent MediaServiceIntent;//服务实例
    private static  MediaService.MyBinder mMyBinder=null;//绑定实例
    long startTime = 0;

    RecyclerView rv = null;//
    private FloatingActionButton fab;//收藏按钮
    private ProgressBar musicProcessBar;//底部进度条
    private CircleProgressBar circleProgressBar;//圆形进度条
    private TextView musicNameTextView;//音乐播放音乐名称
    private TextView musicArtistTextView;//音乐播放歌手名称
    private ImageView musicPlayListBtn;//音乐播放播放列表按钮，已移除
    private ImageView musicPlayBtn;//音乐播放播放或者暂停按钮
    private ImageView musicNextBtn;//音乐播放下一曲按钮


    /*
    *底部控制栏相关
     */
    public View mFloatView;//底部控制栏布局
    public FrameLayout mContentContainer;
    private SwipeRefreshLayout swipeRefresh;//下拉刷新
    FrameLayout.LayoutParams layoutParams;
    Boolean musicControlisOnCreate=false;
    protected float mFirstY;//触摸下去的位置
    protected float mCurrentY;//滑动时Y的位置
    protected int direction;//判断是否上滑或者下滑的标志
    private int mTouchShop;//最小滑动距离
    private boolean isPost=false;

    @SuppressLint("HandlerLeak")
    public  Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    if(!isPost){
                        mHandler.post(mRunnable);
                        isPost=true;
                    }
                    newUI();
                    adapter.setCurrentItem(mMyBinder.getNowPlayPosition());
                    adapter.notifyDataSetChanged();
                    break;
                case 2:

                    musicProcessBar.setProgress(mMyBinder.getPlayPosition());
                    circleProgressBar.setProgress(mMyBinder.getPlayPosition());
                    if (mMyBinder.isPlaying()) {
                        musicPlayBtn.setImageResource(R.drawable.playbar_btn_pause);
                    } else {
                        musicPlayBtn.setImageResource(R.drawable.playbar_btn_play);
                    }
                    break;
            }
            super.handleMessage(msg);

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main);
        requestAllPower();
        MyAppWidgetProvider wp=new MyAppWidgetProvider();
        app = (MyApp)getApplication();
        //View初始化
        initView();

        if(!app.tableIsEmpty()){
            bindService(MediaServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            if(!musicControlisOnCreate){
                musicControl();
            }
            layoutParams.bottomMargin = -300;
        }else{
            show_Toast("当前列表没有歌曲，请点击右上角下载按钮刷新");
        }

        showList();

    }


    public void requestAllPower() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 2);
        }

    }

    //获取到权限回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[]permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    show_Toast("权限不够无法播放音乐，程序将退出");
                    finish();
                }
                break;
            default:
                break;
        }
    }


    public void initView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_navigation_drawer);
        }

        navView.setCheckedItem(R.id.nav_setting);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            public boolean onNavigationItemSelected(MenuItem item) {
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        swipeRefresh=(SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent,R.color.bg_black);
        swipeRefresh.setProgressViewEndTarget(false,200);
        swipeRefresh.setOnRefreshListener(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent favorite_layout = new Intent(MainActivity.this,FavoriteListActivity.class);
                startActivity(favorite_layout, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
            }
        });
        //绑定服务初始化
        MediaServiceIntent = new Intent(this, MediaService.class);
    }

    @Override
    public void onRefresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    app.update();
                    if(mMyBinder!=null){
                        mMyBinder.newList();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                        if(app.mp3List.isEmpty()){
                            show_Toast("手机里暂时没有音乐，快去下载吧！");
                        }else {
                            show_Toast("刷新成功,共有"+app.mp3List.size()+"首音乐");
                        }
                    }
                });
            }
        }).start();
    }



    /*private int getFileSize(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        int size = fis.available();
        fis.close();
        return size;
    }*/

    public void musicControl(){
        mFloatView = LayoutInflater.from(getBaseContext()).inflate(R.layout.float_music_control_layout,null);
        ViewGroup mDecorView =  (ViewGroup) getWindow().getDecorView();
        mContentContainer = (FrameLayout)((ViewGroup)mDecorView.getChildAt(0)).getChildAt(1);
        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        
        musicProcessBar = (ProgressBar)mFloatView.findViewById(R.id.float_Music_ProgressBar);
        musicNameTextView = (TextView)mFloatView.findViewById(R.id.float_Music_Name);
        musicArtistTextView = (TextView)mFloatView.findViewById(R.id.float_Music_Artist);
        circleProgressBar = (CircleProgressBar) mFloatView.findViewById(R.id.float_ProgressBar);
        musicPlayBtn = (ImageView)mFloatView.findViewById(R.id.float_Play_Btn);
        musicNextBtn = (ImageView)mFloatView.findViewById(R.id.float_Next_Music);
        mFloatView.findViewById(R.id.float_Music_Container).setOnClickListener(this);
        musicPlayBtn.setOnClickListener(this);
        musicNextBtn.setOnClickListener(this);
        layoutParams.gravity = Gravity.BOTTOM;//设置对齐位置
        mContentContainer.addView(mFloatView,layoutParams);
        musicControlisOnCreate=true;
    }
    /**
     * 点击事件
     * @param v
     */
    @Override
    public void onClick(View v){
        switch(v.getId())
        {
            case R.id.float_Play_Btn://播放或者暂停按钮
                ImageView imageview = (ImageView)v;
                if(!mMyBinder.isPlaying())
                {
                    mMyBinder.playMusic();
                    newUI();
                } else{
                    mMyBinder.pauseMusic();
                    newUI();
                }
                break;

            case R.id.float_Next_Music://下一曲按钮
                mMyBinder.nextMusic();
                newUI();
                break;
            case R.id.float_Music_Container://播放控制容器
                Intent second_layout=new Intent(MainActivity.this,SixthActivity.class);
                startActivity(second_layout);
                break;
        }
    }

    private void showList() {
        //构造数据源
        //List<HashMap<String, Object>> songList = new ArrayList<HashMap<String, Object>>();
        /*songList = new ArrayList<Mp3Info>();
        for (int i = 0; i < mp3List.size(); i++) {

            //HashMap<String, Object> song= new HashMap<String, Object>();
            Mp3Info song = new Mp3Info();
            Mp3Info info = mp3List.get(i);
            song.setName(info.getName().substring(info.getName().lastIndexOf("-") + 1, info.getName().lastIndexOf(".")));
            song.setSinger(info.getName().substring(0, info.getName().lastIndexOf("-")));
            song.setPath(info.getPath());
            songList.add(song);
        }*/
        //创建适配器，并且将数据源绑定到适配器自己
        /*SimpleAdapter adapter = new SimpleAdapter(
                this,
                songList,
                R.layout.chat_item,
                new String[]{"歌曲名称", "歌曲歌手", "歌曲路径"},
                new int[]{R.id.item_song_name, R.id.item_song_size, R.id.item_song_path});*/

        //获取RecyclerView 控件对象
        rv = (RecyclerView) findViewById(R.id.song_list);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        /*  NestedScrolling机制能够让父View和子View在滚动式进行配合
        *   在这套机制中子View是发起者，父view是接受回调并做出响应的。
        *   方法只有在Android5.0以上才有效果，需要判断android版本是否在5.0(棒棒糖)以上
        *   运用RecyclerView就不会出现此问题
        */
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rv.setNestedScrollingEnabled(true);
        }*/
        //绑定适配器和listView
        //lv.setAdapter(adapter);
        adapter = new MyAdapter(app);
        //setListViewHeightBasedOnChildren(lv);

        //为ListView设置点击监听
        adapter.setOnItemListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int  position) {
                adapter.setCurrentItem(position);
                adapter.setClick(true);
                Flag f=new Flag();
                f.setFlag(position);

                PlayFlag pf=new PlayFlag();
                pf.setPlayflag(0);
                if(!musicControlisOnCreate){
                    musicControl();
                }
                layoutParams.bottomMargin = 0;

                mMyBinder.newMusic();

            }

            @Override
            public void onItemLongClick(View view) {
                int position = rv.getChildAdapterPosition(view);
                show_Toast("长按"+position);
            }

        });
        rv.setAdapter(adapter);
        /**
         * 上滑下滑功能，用于布局隐藏
         */
        rv.setOnTouchListener(new View.OnTouchListener() {//listview的触摸事件
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mFirstY = event.getY();//按下时获取位置
                        break;

                    case MotionEvent.ACTION_MOVE:
                        mCurrentY = event.getY();//得到滑动的位置
                        if(mCurrentY - mFirstY > mTouchShop){//滑动的位置减去按下的位置大于最小滑动距离  则表示向下滑动
                            fab.show();
                            if(musicControlisOnCreate){
                                layoutParams.bottomMargin = 0;
                            }
                        }else if(mFirstY - mCurrentY > mTouchShop){//反之向上滑动
                            fab.hide();
                            if(musicControlisOnCreate){
                                layoutParams.bottomMargin = -300;
                            }
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        break;

                }
                return false;
            }
        });

    }

    /**
     * 服务连接绑定
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMyBinder = (MediaService.MyBinder) service;
            //mMediaService = ((MediaService.MyBinder) service).getInstance();
            receiveMessage();
            Log.d(TAG, "Service与MainActivity已连接");
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * ListView适配器,已改用RecyclerView
     */
    /*public class MyAdapter extends BaseAdapter {
        private int mCurrentItem = 0;
        private boolean isClick = false;
        MyApp app = null;

        MyAdapter(MyApp app){
            this.app = app;
        }

        @Override
        public int getCount() {
            return app.mp3List.size();

        }

        @Override
        public Object getItem(int position) {
            return app.mp3List.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            ItemViewHolder holder;
            TextView songname = null, songsinger = null,musicduration;
            CheckBox cbx = null;
            final Mp3Info song = app.mp3List.get(position);
            System.out.print("名称:"+app.mp3List.get(position).getName());
            System.out.print("歌手:"+app.mp3List.get(position).getSinger());
            final boolean checked = app.favoriteList.contains(song);

            if(convertView == null){
                //通过布局文件，生成视图
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.chat_item, null);
                songname = (TextView) convertView.findViewById(R.id.item_song_name);
                songsinger = (TextView) convertView.findViewById(R.id.item_song_size);
                musicduration=(TextView) convertView.findViewById(R.id.music_duration);
                cbx = (CheckBox) convertView.findViewById(R.id.songItem_layout_checkbox);
                holder = new ItemViewHolder();
                holder.name = songname;
                holder.singer = songsinger;
                holder.musicduration=musicduration;
                holder.cbx = cbx;
                convertView.setTag(holder);
            }else{
                //如果该item视图对象之前已经创建好了
                holder = (ItemViewHolder) convertView.getTag();
                songname = holder.name;
                songsinger = holder.singer;
                musicduration=holder.musicduration;
                cbx = holder.cbx;
            }

            songname.setText(song.getName());
            songsinger.setText(song.getSinger());
            musicduration.setText(TimerFormatter.formatterTime(song.getDuration()));

            if (mCurrentItem == position && isClick) {
                songname.setTextColor(getResources().getColor(R.color.colorPrimary));
                songsinger.setTextColor(getResources().getColor(R.color.colorPrimary));;
            } else {
                songname.setTextColor(getResources().getColor(R.color.bg_black));
                songsinger.setTextColor(getResources().getColor(R.color.text_hui));
            }

            cbx.setChecked(checked);
            //对checkbox进行点击事件注册
            cbx.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    MusicDao dao = MusicDao.getInstance(app);
                    dao.init();
                    if(app.favoriteList.contains(song)){
                            ((MyApp) app).favoriteList.remove(song);
                            song.setFavorite("NO");
                            Toast.makeText(MainActivity.this,"已取消收藏",LENGTH_SHORT).show();
                            System.out.print("已取消收藏");

                    }else{
                            ((MyApp) app).favoriteList.add(song);
                            song.setFavorite("YES");
                            Toast.makeText(MainActivity.this,"已添加收藏",LENGTH_SHORT).show();
                            System.out.print("已添加收藏");

                    }
                    dao.update(song);
                    dao.close();
                }
            });

            return convertView;
        }



        public void setCurrentItem(int currentItem) {
            this.mCurrentItem = currentItem;
        }

        public void setClick(boolean click) {
            this.isClick = click;
        }
    }*/

    /**
     * 右拉抽屉菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    /**
     * 重启该activity时
     */
    @Override
    protected void onResume() {
        super.onResume();
        //adapter.notifyDataSetChanged();
    }

    /**
     * 标题栏按钮
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.backup:
                Intent seven_layout=new Intent(MainActivity.this,SevenActivity.class);
                startActivity(seven_layout);
                break;
            case R.id.delete:
                swipeRefresh.setRefreshing(true);
                onRefresh();

                if(app.mp3List.isEmpty()){
                }else {
                    //绑定服务
                    bindService(MediaServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                    if (!musicControlisOnCreate) {
                        musicControl();
                    }
                    layoutParams.bottomMargin = -300;
                    mHandler.post(mRunnable);
                }
                break;
            case R.id.setting:
                Intent search_activity=new Intent(MainActivity.this,SearchActivity.class);
                startActivity(search_activity);
                break;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }

    /**
     * 更新ui的runnable
     */
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mHandler.sendEmptyMessage(2);
            }catch (Exception e){
                e.printStackTrace();
            }
            mHandler.postDelayed(mRunnable, 1000);
        }
    };

    /**
     * 更新ui
     */
    public  void newUI(){
        musicProcessBar.setMax(mMyBinder.getProgress());
        circleProgressBar.setMax(mMyBinder.getProgress());
        musicProcessBar.setProgress(mMyBinder.getPlayPosition());
        circleProgressBar.setProgress(mMyBinder.getPlayPosition());
        musicNameTextView.setText(mMyBinder.getSongName());
        musicArtistTextView.setText(mMyBinder.getSingerName());
        if (mMyBinder.isPlaying()) {
            musicPlayBtn.setImageResource(R.drawable.playbar_btn_pause);
        } else {
            musicPlayBtn.setImageResource(R.drawable.playbar_btn_play);
        }
    }

    private void receiveMessage(){
        mMyBinder.getMyService().setCallback(new MediaService.Callback(){
            @Override
            public void onDataChange(String data) {
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("data",data);
                msg.setData(b);
                msg.what=1;
                mHandler.sendMessage(msg);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        receiveMessage();
        mHandler.post(mRunnable);
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - startTime) >= 2000) {
            show_Toast("再按一次退出");
            startTime = currentTime;
        } else {
            removeALLActivity();
        }
    }

    /**
     * 被销毁时
     */
   @Override
    protected void onDestroy() {
        super.onDestroy();
        //handler发送是定时1000s发送，如果不关闭，MediaPlayer release掉了还在获取getCurrentPosition就会爆IllegalStateException错误
        mHandler.removeCallbacks(mRunnable);
        mMyBinder.closeMedia();
        unbindService(mServiceConnection);
    }

}
