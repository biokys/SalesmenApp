package eu.janmuller.application.salesmenapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import roboguice.activity.RoboSplashActivity;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.10.13
 * Time: 12:59
 */
public class SplashActivity extends RoboSplashActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        minDisplayMs = 1000;
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.splash_activity);
    }

    @Override
    protected void startNextActivity() {

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {

        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
