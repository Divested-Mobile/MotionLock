package us.spotco.motionlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            WatchdogService.setWatching(false, "Screen is off");
        } else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            WatchdogService.setWatching(true, "Screen is on");
        }
    }

}
