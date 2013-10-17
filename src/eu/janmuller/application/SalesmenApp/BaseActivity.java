package eu.janmuller.application.SalesmenApp;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import roboguice.activity.RoboActivity;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 17.10.13
 * Time: 17:07
 */
public class BaseActivity extends RoboActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
}
