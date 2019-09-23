/*
Copyright (c) 2017 Divested Computing Group

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package us.spotco.motionlock;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        setContentView(R.layout.activity_main);

        if(!AdminHelper.getAdminHelper(this).isAdmin()) {
            AdminHelper.getAdminHelper(this).requestAdmin(this);
        } else {
            Intent watchdog = new Intent(this, WatchdogService.class);
            startService(watchdog);
            finish();
        }
    }

}
