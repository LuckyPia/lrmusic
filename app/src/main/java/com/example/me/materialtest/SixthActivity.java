package com.example.me.materialtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.me.materialtest.MediaService;

import java.text.SimpleDateFormat;

import static android.widget.Toast.LENGTH_SHORT;

public class SixthActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SixthActivity";
    private static  MediaService.MyBinder mMyBinder;
    //private MediaService mMediaService;

    private ImageButton  nextButton;
    private ImageButton preciousButton;
    private  ImageButton musicPlay;
    private ImageButton imageButton;
    private Animation animation;
    private ImageView imageView;
    private Mp3Info song;
    private boolean checked;
    private TextView now;
    private  SeekBar mSeekBar;
    private  TextView mTextView;
    private  TextView mTextView2;
    private  TextView mSingerName;
    private  TextView mTextView3;
    @SuppressLint("StaticFieldLeak")
    private static WaveformView mVisualizerView;
    private MyApp app = null;
    private CheckBox cbx ;
    private static android.support.v7.app.ActionBar actionBar;
    //进度条下面的当前进度文字，将毫秒化为m:ss格式
    @SuppressLint("SimpleDateFormat")
    private static  SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    private static Visualizer mVisualizer;
    private Intent intent;
    private int newi;
    private Flag newf;
    private int mTouchShop=300;//最小滑动距离


    private Visualizer visualizer;
    /* “绑定”服务的intent */
    Intent MediaServiceIntent;

    //public static MyHandler mHandler = new MyHandler();
    @SuppressLint("HandlerLeak")
    public Handler mHandler=new Handler(){
        //public Bundle b=new Bundle();
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case  1:
                    mSeekBar.setMax(mMyBinder.getProgress());
                    mTextView3.setText(mMyBinder.getSongName());
                    mSingerName.setText(mMyBinder.getSingerName());
                    mTextView2.setText(TimerFormatter.formatterTime(mMyBinder.getProgress()));
                    if (mMyBinder.isPlaying()) {
                        imageButton.setImageResource(R.drawable.ic_stop);
                    } else {
                        imageButton.setImageResource(R.drawable.ic_play);
                    }
                    break;
                case 2:
                    mSeekBar.setProgress(mMyBinder.getPlayPosition());
                    mTextView.setText(time.format(mMyBinder.getPlayPosition()));
                    break;
            }
            super.handleMessage(msg);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sixth);
        app = (MyApp)getApplication();

        //View初始化
        initView();
        intent=getIntent();
        newi=intent.getIntExtra("old_activity",-1);
        newf = new Flag();

        //绑定服务
        MediaServiceIntent = new Intent(this, MediaService.class);
        bindService(MediaServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    private static void setupVisualizerFxAndUi() {
        Log.v("ID","AudioSessionId"+mMyBinder.getAudioSessionId());
        //实例化Visualizer，参数SessionId可以通过MediaPlayer的对象获得
        mVisualizer = new Visualizer(mMyBinder.getAudioSessionId());
        //采样 - 参数内必须是2的位数 - 如64,128,256,512,1024
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                // 用waveform波形数据更新mVisualizerView组件
                mVisualizerView.updateVisualizer(fft);
            }

            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
            }
        }, Visualizer.getMaxCaptureRate() / 2, false, true);
        mVisualizer.setEnabled(true);
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

    //获取到权限回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[]permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bindService(MediaServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                } else {
                    show_Toast("权限不够获取不到音乐，程序将退出");
                    finish();
                }
                break;
            default:
                break;
        }
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMyBinder = (MediaService.MyBinder) service;
            //mMediaService = ((MediaService.MyBinder) service).getInstance();
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
            setupVisualizerFxAndUi();
            newUI();
            Log.d(TAG, "Service与SixthActivity已连接");

            intent=getIntent();
            if(newi!=-1){
                newf.setFlag(newi);
                mMyBinder.newMusic();
                newUI();
            }else{
            }
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //这里很重要，如果不判断是否来自用户操作进度条，会不断执行下面语句块里面的逻辑，然后就会卡顿
                    if(fromUser){
                        now.setText(TimerFormatter.formatterTime(progress));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    now.setText("");
                    mMyBinder.seekToPositon(seekBar.getProgress());
                }
            });
            mHandler.post(mRunnable);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    //初始化
    private void initView() {
        nextButton = (ImageButton) findViewById(R.id.next);
        preciousButton = (ImageButton) findViewById(R.id.precious);
        musicPlay=(ImageButton) findViewById(R.id.music_play);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mTextView = (TextView) findViewById(R.id.text1);
        mTextView2=(TextView) findViewById(R.id.text2);
        mTextView3=(TextView) findViewById(R.id.text3);
        mSingerName=(TextView) findViewById(R.id.singerName);
        imageButton=(ImageButton) findViewById(R.id.playImage);
        imageButton.setImageResource(R.drawable.ic_stop);
        mVisualizerView=(WaveformView) findViewById(R.id.visualizerview);

        now=(TextView) findViewById(R.id.now);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        cbx = (CheckBox) findViewById(R.id.song_collect);
        imageButton.setOnClickListener(new PalyListener());
        cbx.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        musicPlay.setOnClickListener(this);
        preciousButton.setOnClickListener(this);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("当前播放");
        }
        Preference p = Preference.getInstance(SixthActivity.this);
        String type = p.get();
        if(type.equals("0")){
            musicPlay.setImageResource(R.drawable.music_playmode_listloop);//顺序播放
        }else if(type.equals("1")){
            musicPlay.setImageResource(R.drawable.music_playmode_singloop);//单曲循环
        }else if(type.equals("2")){
            musicPlay.setImageResource(R.drawable.music_playmode_random);//随机播放
        }
    }

    /*播放或暂停事件处理*/
    private class PalyListener implements View.OnClickListener {
        public void onClick(View v) {
            if(!mMyBinder.isPlaying())
            {
                imageButton.setImageResource(R.drawable.ic_stop);
                mMyBinder.playMusic();
                newUI();

            } else{
                imageButton.setImageResource(R.drawable.ic_play);
                mMyBinder.pauseMusic();
                newUI();
            }
        }
    }

    @Override
    public void onClick(View v) {
        Preference p = Preference.getInstance(SixthActivity.this);
        String type = p.get();

        switch (v.getId()) {
            case R.id.next:
                mMyBinder.nextMusic();
                newUI();
                imageButton.setImageResource(R.drawable.ic_stop);
                break;
            case R.id.precious:
                mMyBinder.preciousMusic();
                newUI();
                imageButton.setImageResource(R.drawable.ic_stop);
                break;
            case R.id.song_collect:
                checked = app.favoriteList.contains(song);
                if(checked){
                    app.favoriteList.remove(song);
                    song.setFavorite("NO");
                    show_Toast("已取消收藏");

                }else{
                    app.favoriteList.add(song);
                    song.setFavorite("YES");
                    show_Toast("已添加收藏");
                }
                app.updateSong(song);
                break;
            case R.id.music_play:
                if(type.equals("0")){
                    musicPlay.setImageResource(R.drawable.music_playmode_singloop);
                    p.save("1");
                    show_Toast("当前为单曲循环");
                }else if(type.equals("1")){
                    musicPlay.setImageResource(R.drawable.music_playmode_random);
                    p.save("2");
                    show_Toast("当前为随机播放");
            }else if(type.equals("2")){
                    musicPlay.setImageResource(R.drawable.music_playmode_listloop);
                    p.save("0");
                    show_Toast("当前为顺序播放");
            }
                break;
        }
    }

    public void newUI(){
        mSeekBar.setMax(mMyBinder.getProgress());
        mTextView2.setText(TimerFormatter.formatterTime(mMyBinder.getProgress()));
        mTextView3.setText(mMyBinder.getSongName());
        mSingerName.setText(mMyBinder.getSingerName());
        song = app.mp3List.get(mMyBinder.getPlayNum());
        checked = app.favoriteList.contains(song);
        cbx.setChecked(checked);
        if (mMyBinder.isPlaying()) {
            imageButton.setImageResource(R.drawable.ic_stop);
        } else {
            imageButton.setImageResource(R.drawable.ic_play);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //handler发送是定时1000s发送，如果不关闭，MediaPlayer release掉了还在获取getCurrentPosition就会爆IllegalStateException错误
        //mMyBinder.closeMedia();
        mHandler.removeCallbacks(mRunnable);
        mVisualizer.release();
        unbindService(mServiceConnection);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
        mVisualizer.release();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mHandler.post(mRunnable);
        setupVisualizerFxAndUi();
    }

    /**
     * 更新ui的runnable
     */
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(2);
            mHandler.postDelayed(mRunnable, 1000);
        }
    };


}
