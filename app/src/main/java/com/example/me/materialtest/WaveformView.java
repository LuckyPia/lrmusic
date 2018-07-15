package com.example.me.materialtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

class WaveformView extends View {
    private Paint mPaint;
    private float mStrokeWidth =4;
    private float mRadius = 36;
    private int mWidth;
    private int mHeight;
    float r0=350;
    float r=2;

    Context ctx;

    protected final static int MAX_LEVEL = 30;
    protected Visualizer mVisualizer = null;
    private int levelStep = 0;
    protected final static int CYLINDER_NUM = 128;

    boolean mDataEn = true;

    private byte[] mBytes;


    public WaveformView(Context context) {
        super(context);
        ctx=context;
        init();
    }
    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx=context;
        init();
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ctx=context;
        init();
    }

    // 完成相关参数初始化
    private void init() {
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.text_white));
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);

        mPaint.setStrokeJoin(Paint.Join.ROUND); //频块圆角
        mPaint.setStrokeCap(Paint.Cap.ROUND); //频块圆角

    }

    // 重写测量大小的onMeasure方法和绘制View的核心方法onDraw()
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getRealSize(widthMeasureSpec);
        mHeight = getRealSize(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int ox=mWidth / 2,oy=mHeight/2;
        //Log.v("wave","数量"+CYLINDER_NUM);
        if(mBytes==null){
        }else{
            for(int i = 0; i < CYLINDER_NUM; i++){
                //R * cos (PI/180*一次旋转的角度数) ,-R * sin (PI/180*一次旋转的角度数)
                double angle1=Math.cos((i * 2.81)*Math.PI/180);
                double angle2=-Math.sin((i * 2.81)*Math.PI/180);
                float x1=(float)(r0*angle1)+ox;
                float y1=(float)(r0*angle2)+oy;
                float x2=(float)((r0+1+mBytes[i])*angle1)+ox;
                float y2=(float)((r0+1+mBytes[i])*angle2)+oy;
                float x3=(float)((r0-mBytes[i])*angle1)+ox;
                float y3=(float)((r0-mBytes[i])*angle2)+oy;

                canvas.drawLine(x1,y1,x2,y2,mPaint);
                canvas.drawPoint(x3,y3,mPaint);
            }
        }


    }

    public int getRealSize(int measureSpec) {
        int result = 1;
        int mode = View.MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.UNSPECIFIED) {
            //自己计算
            result = (int) (mRadius * 2 + mStrokeWidth);
        } else {
            result = size;
        }
        return result;
    }


    //这个回调应该采集的是快速傅里叶变换有关的数据

    public void updateVisualizer(byte[] fft)
    {
        byte[] model = new byte[fft.length / 2 + 1];

        model[0] = (byte) Math.abs(fft[0]);
        if(model[0]<0){
            model[0]=0;
        }else if(model[0]>60){
            model[0]=60;
        }

        for (int i = 1, j = 1; j < CYLINDER_NUM;)
        {
            model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
            if(model[j]<0){
                model[j]=0;
            }else if(model[j]>60){
                model[j]=60;
            }
            i += 1;
            j++;
        }
        mBytes = model;
        invalidate();
    }


}
