package com.phone.konka.wirelesscharge.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phone.konka.wirelesscharge.R;

public class MainActivity extends Activity {


    /**
     * 电量背景最大高度
     */
    private static final int ELECTRICITY_BG_HEIGHT = 978;


    /**
     * 电量广播接收者
     */
    private ElectricityReceiver mElectricityReceiver;


    /**
     * 显示电量TextView
     */
    private TextView mTvElectricity;


    /**
     * 状态栏占位View
     */
    private View mViewStatusBar;


    /**
     * 电量背景View
     */
    private View mViewElectricity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initState();

        initView();

        mElectricityReceiver = new ElectricityReceiver();
        registerReceiver(mElectricityReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        openAccessibility();

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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            finish();
        }
        return super.dispatchTouchEvent(ev);
    }


    /**
     * 初始化状态
     */
    private void initState() {
//        设置不熄屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        设置底部导航栏透明
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

//        设置沉浸式状态栏
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

//        设置字体
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


    /**
     * 跳转到系统辅助功能设置
     */
    private void openAccessibility() {
        if (!isAccessibilitySettingOn(this, getPackageName() + ".Service.TimerAccessibilityService")) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }


    /**
     * 检测是否已打开Accessibility服务
     *
     * @param context
     * @param accessName
     * @return
     */
    private boolean isAccessibilitySettingOn(Context context, String accessName) {

        int accessibilityEnable = 0;
        String serviceName = getPackageName() + "/" + accessName;
        try {
            accessibilityEnable = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (accessibilityEnable == 1) {
            TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
            String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(serviceName)) {
                        return true;
                    }
                }
            }
        }
        return false;
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
