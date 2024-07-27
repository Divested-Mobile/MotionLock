/*
Copyright (c) 2017 Divested Computing Group

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package us.spotco.motionlock;

import android.app.KeyguardManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.github.nisrulz.sensey.FlipDetector;
import com.github.nisrulz.sensey.MovementDetector;
import com.github.nisrulz.sensey.Sensey;

public class WatchdogService extends Service {

    private static final String logTag = "MotionLock";

    private static long lastLockTime = SystemClock.elapsedRealtime();

    private static final int lockThreshholdFaceDown = 1;
    private static int lockCounterFaceDown = 0;
    private static final int lockThreshholdNoMovement = 3;
    private static int lockCounterNoMovement = 0;

    private static BroadcastReceiver mScreenStateReceiver;
    private static ComponentName mLockReceiver;
    private static DevicePolicyManager mDPM;
    private static KeyguardManager mKM;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "MotionLock: Service Started", Toast.LENGTH_SHORT).show();

        mScreenStateReceiver = new ScreenStateReceiver();
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStateReceiver, screenStateFilter);

        mLockReceiver = new ComponentName(this, LockReceiver.class);
        mDPM = (DevicePolicyManager)getSystemService(DEVICE_POLICY_SERVICE);
        mKM = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);

        if(AdminHelper.getAdminHelper(this).isAdmin()) {
            Sensey.getInstance().init(this, Sensey.SAMPLING_PERIOD_NORMAL);

            setupLockGestures();
            setWatching(true, "First start");
        } else {
            return START_NOT_STICKY;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "MotionLock: Service Stopped", Toast.LENGTH_SHORT).show();
    }

    protected static void logAction(String action) {
        Log.d(logTag, action);
    }

    private static void resetLockCouinters() {
        lockCounterNoMovement = lockCounterFaceDown = 0;
    }

    private static void lock(String reason, int lockCounter, int lockThreshhold) {
        if(!mKM.inKeyguardRestrictedInputMode()) {
            logAction("Considering on locking! Counter: " + lockCounter + ", Threshold: " + lockThreshhold + ", Reason: " + reason);

            if ((SystemClock.elapsedRealtime() - lastLockTime) >= (45 * 1000)) {
                resetLockCouinters();
                logAction("Timed out lock counters!");
            }

            if (lockCounter >= lockThreshhold) {
                resetLockCouinters();
                mDPM.lockNow();
                logAction("Locking!");
            }
            lastLockTime = SystemClock.elapsedRealtime();
        } else {
            setWatching(false, "Screen is locked!");
        }
    }

    private static FlipDetector.FlipListener flipListener;
    private static MovementDetector.MovementListener movementListener;

    private static void setupLockGestures() {
        flipListener = new FlipDetector.FlipListener() {
            @Override public void onFaceUp() {
                lockCounterFaceDown = 0;
            }

            @Override public void onFaceDown() {
                lockCounterFaceDown++;
                lock("Facing down on table!", lockCounterFaceDown, lockThreshholdFaceDown);
            }
        };

        movementListener = new MovementDetector.MovementListener() {
            @Override public void onMovement() {
            }

            @Override public void onStationary() {
                lockCounterNoMovement++;
                lock("No movement detected!", lockCounterNoMovement, lockThreshholdNoMovement);
            }

        };
    }

    protected static void setWatching(boolean watching, String reason) {
        if(watching) {
            Sensey.getInstance().startFlipDetection(flipListener);
            Sensey.getInstance().startMovementDetection(movementListener);
        } else {
            Sensey.getInstance().stopFlipDetection(flipListener);
            Sensey.getInstance().stopMovementDetection(movementListener);
        }
        logAction((watching ? "Started" : "Stopped") + " watching sensors! Reason: " + reason);
    }
}
