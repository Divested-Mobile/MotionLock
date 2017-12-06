package us.spotco.motionlock;

import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
