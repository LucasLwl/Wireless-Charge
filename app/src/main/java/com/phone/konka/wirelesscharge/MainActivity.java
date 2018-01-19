package com.phone.konka.wirelesscharge;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {

    private ElectricityReceiver mElectricityReceiver;

    private TextView mTvElectricity;

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

    private void initView() {
        mTvElectricity = (TextView) findViewById(R.id.tv_electricity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mElectricityReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startService();
        finish();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startService();
            finish();
        }
        return super.onTouchEvent(event);
    }

    private void initState() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    private void startService() {
        Intent intent = new Intent(this, TimeService.class);
        startService(intent);
    }


    private class ElectricityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int current = intent.getExtras().getInt("level");
            int total = intent.getExtras().getInt("scale");
            int percent = current * 100 / total;
            mTvElectricity.setText(String.valueOf(percent));
        }
    }
}
