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

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class AdminHelper {

    private static AdminHelper adminHelper;

    private static ComponentName mLockReceiver;
    private static DevicePolicyManager mDPM;

    public AdminHelper(Context ctx) {
        mLockReceiver = new ComponentName(ctx, LockReceiver.class);
        mDPM = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    protected static AdminHelper getAdminHelper(Context ctx) {
        if(adminHelper == null) {
            adminHelper = new AdminHelper(ctx);
        }
        return adminHelper;
    }

    protected static boolean isAdmin() {
        return mDPM.isAdminActive(mLockReceiver);
    }

    protected void requestAdmin(Activity activity) {
        Intent requestAdmin = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        requestAdmin.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mLockReceiver);
        requestAdmin.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required to lock the device");
        activity.startActivityForResult(requestAdmin, 1);
    }
}
