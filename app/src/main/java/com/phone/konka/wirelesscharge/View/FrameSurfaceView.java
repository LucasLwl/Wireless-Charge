package com.phone.konka.wirelesscharge.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.phone.konka.wirelesscharge.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created by 廖伟龙 on 2018-1-19.
 */

public class FrameSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    /**
     * holder
     */
    private SurfaceHolder mHolder;


    /**
     * 画板
     */
    private Canvas mCanvas;


    /**
     * 动画图片
     */
    private Bitmap mBitmap;


    /**
     * 动画状态
     */
    private boolean mIsDrawing;


    /**
     * 每帧时间
     */
    private int mFrameSpaceTime = 120;


    /**
     * 用于设置inBitmap
     */
    private BitmapFactory.Options mOptions;


    /**
     * 动画drawable的资源id数组
     */
    private TypedArray mTypeArr;


    /**
     * 当前帧
     */
    private int mCurrentIndex = 0;


    /**
     * 图片区域
     */
    private Rect mRect;


    /**
     * 信号量 用于控制加载图片线程与显示图片线程
     */
    private Semaphore mSemaphore;


    /**
     * 加载动画画片
     */
    private Runnable mLoadPicRunnable;


    /**
     * 单线程线程池
     */
    private ExecutorService mSingleThreadPool;

    private Paint mPaint;


    public FrameSurfaceView(Context context) {
        this(context, null);
    }

    public FrameSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FrameSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mHolder = getHolder();
        mHolder.addCallback(this);


        mOptions = new BitmapFactory.Options();
        mOptions.inMutable = true;
        mOptions.inSampleSize = 1;

        mTypeArr = getResources().obtainTypedArray(R.array.frame_anim);

        mSemaphore = new Semaphore(1);

        mLoadPicRunnable = new Runnable() {
            @Override
            public void run() {
                loadPicture();
                mSemaphore.release();
            }
        };
        mSingleThreadPool = Executors.newSingleThreadExecutor();

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mRect = new Rect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        loadPicture();
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBitmap.recycle();
        mTypeArr.recycle();
    }

    @Override
    public void run() {
        while (mIsDrawing) {
            drawView();
            mSingleThreadPool.execute(mLoadPicRunnable);
            try {
                Thread.sleep(mFrameSpaceTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void drawView() {
        try {
            mCanvas = mHolder.lockCanvas();
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mSemaphore.acquire();
            mCanvas.drawBitmap(mBitmap, null, mRect, mPaint);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (++mCurrentIndex == mTypeArr.length())
                mCurrentIndex = 0;
            if (mCanvas != null)
                mHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    private void loadPicture() {
        mOptions.inBitmap = mBitmap;
        mBitmap = BitmapFactory.decodeResource(getResources(), mTypeArr.getResourceId(mCurrentIndex, R.drawable.anim00), mOptions);
    }
}

