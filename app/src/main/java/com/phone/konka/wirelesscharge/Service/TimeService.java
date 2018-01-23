package com.phone.konka.wirelesscharge.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.phone.konka.wirelesscharge.Activity.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 后台计时器
 * <p>
 * Created by 廖伟龙 on 2018-1-18.
 */

public class TimeService extends Service {

    private static final int TIME = 1000 * 500;

    private Timer mTimer;

    private TimerTask mScheduleTask;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (manager.isDeviceIdleMode())
                Log.i("ddd", "isDeviceIdleMode");
            if (manager.isInteractive())
                Log.i("ddd", "isInteractive");
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mTimer != null)
            mTimer.cancel();
        mTimer = new Timer();
        mScheduleTask = new TimerTask() {
            @Override
            public void run() {
                Intent activity = new Intent(TimeService.this, MainActivity.class);
                activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(activity);
                stopSelf();
            }
        };
        mTimer.schedule(mScheduleTask, TIME);


//        mHandler.sendEmptyMessage(0);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
