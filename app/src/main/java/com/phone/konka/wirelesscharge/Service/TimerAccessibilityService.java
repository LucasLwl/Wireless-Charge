package com.phone.konka.wirelesscharge.Service;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import com.phone.konka.wirelesscharge.Activity.MainActivity;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by 廖伟龙 on 2018-1-23.
 */

public class TimerAccessibilityService extends AccessibilityService {


    /**
     * Message 启动Activity
     */
    private static final int MSG_START_SCREEN_SAVER = 0x0001;


    /**
     * 后台无操作启动Activity时间
     */
    private static final int STANDBY_TIME_MILLIS = 1000 * 50;


    /**
     * 屏幕是否亮屏
     */
    private boolean mScreenIsOn = true;


    /**
     * 上次用户操作时间
     */
    private long mLastTime;


    /**
     * 是否通话中
     */
    private boolean phoneRunning = false;


    private Handler mHandler;


    /**
     * 后台计时线程
     */
    private TimerThread mThread;


    /**
     * 亮屏、暗屏接收者
     */
    private BroadcastReceiver mReceiver;


    @Override
    protected void onServiceConnected() {
        mLastTime = System.currentTimeMillis();
        registerBroadcastReceiver();
        judgeStartActivity();
        listenPhone();
        super.onServiceConnected();
    }


    /**
     * 有用户操作，重新计时
     *
     * @param event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int type = event.getEventType();
        if (type != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            mThread.unPark();
            mLastTime = System.currentTimeMillis();
        }
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            return super.onKeyEvent(event);
        }

        mThread.unPark();
        mLastTime = System.currentTimeMillis();
        return super.onKeyEvent(event);
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public boolean onUnbind(Intent intent) {
        mThread.stopTread();
        unregisterReceiver(mReceiver);
        return super.onUnbind(intent);
    }


    /**
     * 注册亮暗屏广播接收者
     */
    private void registerBroadcastReceiver() {
        mReceiver = new ScreenInfoBroadcastReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mReceiver, filter);
    }


    /**
     * 开启后台计时启动Activity
     */
    private void judgeStartActivity() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_START_SCREEN_SAVER) {
                    startScreenSaverOrNot();
                }
            }
        };
        mThread = new TimerThread();
        mThread.start();
    }


    /**
     * 判断是否通话中
     */
    private void listenPhone() {
        TelephonyManager mTelephonyManager = (TelephonyManager) this
                .getSystemService(this.TELEPHONY_SERVICE);
        mTelephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        phoneRunning = true;
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        phoneRunning = false;
                        break;
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);
    }


    /**
     * 启动Activity
     */
    private void startScreenSaverOrNot() {
        if (!isForeground(this, "com.phone.konka.wirelesscharge.Activity.MainActivity") && !isPlay(this) && !phoneRunning && mScreenIsOn) {
            Intent start = new Intent(this, MainActivity.class);
            start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(start);
        }
    }


    /**
     * 判断是否有视频播放
     *
     * @param context
     * @return
     */
    private boolean isPlay(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        return audioManager.isMusicActive();
    }


    /**
     * 判断需要启动的Activity是否已处于栈顶
     *
     * @param context
     * @param className
     * @return
     */
    private boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * 后台计时线程
     */
    private class TimerThread extends Thread {
        private boolean stop = false;
        private boolean flags = false;

        @Override
        public void run() {
            try {
                while (!stop) {
                    long nowTime = System.currentTimeMillis();

                    if (nowTime > (mLastTime + STANDBY_TIME_MILLIS)) {
                        Message msg = new Message();
                        msg.what = MSG_START_SCREEN_SAVER;
                        mHandler.sendMessage(msg);
                        park();
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void park() {

            if (!flags) {
                flags = true;
                LockSupport.park();
            }
        }

        private void unPark() {

            if (flags) {

                flags = false;
                LockSupport.unpark(this);
            }
        }

        private void stopTread() {
            stop = true;
        }
    }


    /**
     * 亮暗屏广播接收者
     */
    class ScreenInfoBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
                mScreenIsOn = false;
            else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
                mScreenIsOn = true;
        }
    }
}
