package com.phone.konka.wirelesscharge.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.phone.konka.wirelesscharge.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

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
     * 动画状态
     */
    private boolean mIsDrawing;


    /**
     * 每帧时间
     */
    private int mFrameSpaceTime = 33;


    /**
     * 动画drawable的资源id数组
     */
    private TypedArray mTypeArr;


    /**
     * 当前帧
     */
    private AtomicInteger mCurrentIndex = new AtomicInteger(0);


    /**
     * 图片区域
     */
    private Rect mRect;


    /**
     * 程线程池
     */
    private ExecutorService mThreadPool;


    /**
     * 信号量 用于控制加载图片线程与显示图片线程
     */
    private Semaphore mSemaphore;


    /**
     * 用于加载图片
     */
    private BitmapFactory.Options mOptions;


    /**
     * 当前缓存的index
     */
    private int mCacheIndex = 0;


    /**
     * 图片缓存数量
     */
    private int mCacheCount = 2;


    /**
     * 图片缓存
     */
    private SparseArray<Bitmap> mBitmapCache = new SparseArray<>();


    /**
     * 是否再充电
     */
    private boolean isCharging = false;


    /**
     * 是否第一次绘制
     * <p>
     * 用于无充电时显示第一帧
     */
    private boolean isFirstDraw = true;


    public FrameSurfaceView(Context context) {
        this(context, null);
    }

    public FrameSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FrameSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mHolder = getHolder();
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        mHolder.addCallback(this);

        mTypeArr = getResources().obtainTypedArray(R.array.frame_anim);

        mOptions = new BitmapFactory.Options();
        mOptions.inMutable = true;
        mOptions.inSampleSize = 1;

        mThreadPool = Executors.newFixedThreadPool(mCacheCount);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mRect = new Rect(0, 0, getWidth(), getHeight());
    }


    /**
     * 初始化缓存，index
     * 开始动画线程
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        isFirstDraw = true;
        mCurrentIndex.set(0);
        mCacheIndex = 0;

//        初始缓存
        for (int i = 0; i < mCacheCount; i++)
            mBitmapCache.put(i, loadPicture(mBitmapCache.get(i)));
        mSemaphore = new Semaphore(mCacheCount);
        new Thread(this).start();
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
        mBitmapCache.clear();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTypeArr.recycle();
    }


    /**
     * 加载图片
     *
     * @param bitmap 复用Bitmap
     * @return 加载的Bitmap
     */
    private Bitmap loadPicture(Bitmap bitmap) {
        mOptions.inBitmap = bitmap;
        int index = mCurrentIndex.getAndIncrement();
        if (index == mTypeArr.length() - 1)
            mCurrentIndex.set(0);
//        bitmap = BitmapFactory.decodeResource(getResources(), mTypeArr.getResourceId(mCurrentIndex.get(), R.drawable.anim00), mOptions);
        bitmap = BitmapFactory.decodeResource(getResources(), mTypeArr.getResourceId(index, R.drawable.anim00), mOptions);
//        if (mCurrentIndex.incrementAndGet() == mTypeArr.length())
//            mCurrentIndex.set(0);
        return bitmap;
    }


    /**
     * 设置是否充电状态
     *
     * @param isCharging 是否充电
     */
    public void setCharge(boolean isCharging) {
        this.isCharging = isCharging;
    }


    /**
     * 充电状态则正常显示动画
     * 无充电时先显示第一帧，接着睡眠
     */
    @Override
    public void run() {
        long start;
        sleep(mFrameSpaceTime);
        while (mIsDrawing) {
            if (isCharging) {
                start = System.currentTimeMillis();
                drawView();
                sleep(Math.max(0, mFrameSpaceTime - (System.currentTimeMillis() - start)));
            } else if (isFirstDraw) {
                isFirstDraw = false;
                drawFirstFrame();
            } else {
                sleep(mFrameSpaceTime);
            }
        }
    }


    /**
     * 进行睡眠
     *
     * @param time 休眠时长  ms
     */
    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 绘制第一帧
     */
    private void drawFirstFrame() {
        mCanvas = mHolder.lockCanvas();
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mCanvas.drawBitmap(mBitmapCache.get(0), null, mRect, null);
        if (mCanvas != null)
            mHolder.unlockCanvasAndPost(mCanvas);
    }


    /**
     * 绘制正常动画
     */
    private void drawView() {
        Bitmap bitmap = mBitmapCache.get(mCacheIndex);
        try {
            mSemaphore.acquire();
            mCanvas = mHolder.lockCanvas();
            if (mCanvas == null)
                return;
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mCanvas.drawBitmap(bitmap, null, mRect, null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null)
                mHolder.unlockCanvasAndPost(mCanvas);

//            子线程加载图片
//            if (mCacheIndex == 0)
//                mThreadPool.execute(new LoadPicRunnable(mCacheCount - 1));
//            else
//                mThreadPool.execute(new LoadPicRunnable(mCacheIndex - 1));

            mThreadPool.execute(new LoadPicRunnable(mCacheIndex));

            if (++mCacheIndex >= mCacheCount)
                mCacheIndex = 0;
        }
    }


    /**
     * 加载动画图片
     */
    private class LoadPicRunnable implements Runnable {

        private int index;

        public LoadPicRunnable(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            mBitmapCache.put(index, loadPicture(mBitmapCache.get(index)));
            mSemaphore.release();
        }
    }
}

