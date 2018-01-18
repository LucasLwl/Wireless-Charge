package com.phone.konka.wirelesscharge;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 后台计时器
 * <p>
 * Created by 廖伟龙 on 2018-1-18.
 */

public class TimeService extends Service {

    private static final int TIME = 1000 * 5;

    Timer mTimer = new Timer();

    @Override
    public void onCreate() {
        super.onCreate();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Intent activity = new Intent(TimeService.this, MainActivity.class);
                activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(activity);
                stopSelf();
            }
        };
        mTimer.schedule(task, TIME);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
