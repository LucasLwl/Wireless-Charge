package com.phone.konka.wirelesscharge.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phone.konka.wirelesscharge.R;
import com.phone.konka.wirelesscharge.Service.MyAccessibilityService;
import com.phone.konka.wirelesscharge.Service.TimeService;

public class MainActivity extends Activity {

    private static final int ELECTRICITY_BG_HEIGHT = 978;

    private ElectricityReceiver mElectricityReceiver;

    private TextView mTvElectricity;

    private View mViewStatusBar;

    private View mViewElectricity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initState();

        initView();

        mElectricityReceiver = new ElectricityReceiver();
        registerReceiver(mElectricityReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

//        PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    }


    @Override
    protected void onStop() {
        super.onStop();
        startService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mElectricityReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            finish();
        }
        return super.onTouchEvent(event);
    }

    /**
     * 初始化状态
     */
    private void initState() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            mViewStatusBar = findViewById(R.id.view_statusBar);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mViewStatusBar.getLayoutParams();
            lp.height = getStatusBarHeight();
            mViewStatusBar.setLayoutParams(lp);
        }
    }

    private void initView() {

        mTvElectricity = (TextView) findViewById(R.id.tv_electricity);
        mViewElectricity = findViewById(R.id.view_electricity);

        Typeface arialBoldMT = Typeface.createFromAsset(getAssets(), "font/arial_boldmt.ttf");
        Typeface arialNarrow = Typeface.createFromAsset(getAssets(), "font/arial_narrow.ttf");
        Typeface msyi = Typeface.createFromAsset(getAssets(), "font/msyi.ttf");

        ((TextView) findViewById(R.id.tv_title)).setTypeface(arialBoldMT);
        ((TextView) findViewById(R.id.tv_percent)).setTypeface(arialNarrow);
        ((TextView) findViewById(R.id.tv_status)).setTypeface(msyi);
        mTvElectricity.setTypeface(arialBoldMT);
    }


    private int getStatusBarHeight() {

        int result = 0;
        //获取状态栏高度的资源id
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void startService() {

        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);

//        Intent intent = new Intent(this, MyAccessibilityService.class);
////        startService(intent);
//        bindService(intent, new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//
//            }
//        }, BIND_AUTO_CREATE);
    }


    /**
     * 电量广播接收者
     */
    private class ElectricityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int current = intent.getExtras().getInt("level");
            int total = intent.getExtras().getInt("scale");
            int percent = current * 100 / total;

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mViewElectricity.getLayoutParams();
            lp.height = percent * ELECTRICITY_BG_HEIGHT / 100;
            mViewElectricity.setLayoutParams(lp);
            mTvElectricity.setText(String.valueOf(percent));
        }
    }
}
