package com.phone.konka.wirelesscharge.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.phone.konka.wirelesscharge.Activity.MainActivity;

/**
 * Created by 廖伟龙 on 2018-1-23.
 */

public class WirelessChargeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent receiver = context.registerReceiver(null, filter);

        int status = receiver.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
        boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
        if (isCharging) {
            int chargePlug = receiver.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean isWirelessCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS;
            if (isWirelessCharge) {
                Intent activity = new Intent(context, MainActivity.class);
                activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(activity);
            }
        }
    }
}
