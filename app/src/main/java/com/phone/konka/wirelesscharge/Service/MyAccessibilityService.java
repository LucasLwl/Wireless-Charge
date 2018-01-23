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
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.phone.konka.wirelesscharge.Activity.MainActivity;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by 廖伟龙 on 2018-1-23.
 */

public class MyAccessibilityService extends AccessibilityService {

    private static final int MSG_START_SCREEN_SAVER = 0x0001;

    private static final int STANDBY_TIME_MILLIS = 1000 * 5;

    private boolean mScreenIsOn = true;

    private long mLastTime;

    private boolean phoneRunning = false;

    private Handler mHandler;

    private MyThread mThread;

    private TelephonyManager mTelephonyManager;

    private BroadcastReceiver mReceiver;


//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.i("lwl", "onCreate");
//        Toast.makeText(this,"onCreate",Toast.LENGTH_SHORT).show();
//        mLastTime = System.currentTimeMillis();
//        registerBroadcastReceiver();
//        judgeStartActivity();
//        listenPhone();
//    }

    @Override
    protected void onServiceConnected() {

        Log.i("lwl", "onServiceConnected");
        Toast.makeText(this, "onServiceConnected", Toast.LENGTH_SHORT).show();
        mLastTime = System.currentTimeMillis();
        registerBroadcastReceiver();
        judgeStartActivity();
        listenPhone();
        super.onServiceConnected();
    }

    private void judgeStartActivity() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_START_SCREEN_SAVER) {
                    startScreenSaverOrNot();
                }
            }
        };
        mThread = new MyThread();
        mThread.start();
    }

    private void startScreenSaverOrNot() {
        if (!isForeground(this, "com.phone.konka.wirelesscharge.Activity.MainActivity") && !isPlay(this) && !phoneRunning && mScreenIsOn) {
            Intent start = new Intent(this, MainActivity.class);
            start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(start);
        }
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != 2048) {
            mThread.unpark();
            /** 获取用户事件的时间，更新计时起点*/
            mLastTime = System.currentTimeMillis();
        }
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

    private void listenPhone() {
        mTelephonyManager = (TelephonyManager) this
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

    private boolean isPlay(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        return audioManager.isMusicActive();
    }

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

    private void registerBroadcastReceiver() {
        mReceiver = new ScreenInfoBroadcastReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mReceiver, filter);
    }

    class MyThread extends Thread {
        private boolean stop = false;
        private boolean flags = false;

        @Override
        public void run() {
            try {
                while (true && !stop) {
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

        public void park() {

            if (!flags) {

                flags = true;
                LockSupport.park();
            }
        }

        public void unpark() {

            if (flags) {

                flags = false;
                LockSupport.unpark(this);
            }

        }

        private void stopTread() {
            stop = true;
        }


    }

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
