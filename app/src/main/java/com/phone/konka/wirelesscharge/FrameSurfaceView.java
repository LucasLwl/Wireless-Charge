package com.phone.konka.wirelesscharge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by 廖伟龙 on 2018-1-19.
 */

public class FrameSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mHolder;

    private Canvas mCanvas;

    private Bitmap mBitmap;

    private boolean mIsDrawing;

    private long mFrameSpaceTime = 100;

    private BitmapFactory.Options mOptions;

    private TypedArray mTypeArr;

    private int mCurrentIndex = 0;

    private Rect mRect;

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

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
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
    public void run() {
        while (mIsDrawing) {
            drawView();
            try {
                Thread.sleep(mFrameSpaceTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());

    }

    private void drawView() {
        try {
            mCanvas = mHolder.lockCanvas();
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            if (mBitmap == null) {
                mBitmap = BitmapFactory.decodeResource(getResources(), mTypeArr.getResourceId(mCurrentIndex, R.drawable.anim00));
            } else {
                mOptions.inBitmap = mBitmap;
                mBitmap = BitmapFactory.decodeResource(getResources(), mTypeArr.getResourceId(mCurrentIndex, R.drawable.anim00), mOptions);
            }


            mCanvas.drawBitmap(mBitmap, null, mRect, null);

            if (mCurrentIndex == mTypeArr.length())
                mCurrentIndex = 0;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mCurrentIndex++;
            if (mCanvas != null)
                mHolder.unlockCanvasAndPost(mCanvas);
        }
    }
}
