package eu.janmuller.application.salesmenapp;

import android.content.Context;
import android.content.Intent;
import roboguice.receiver.RoboBroadcastReceiver;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 16.12.13
 * Time: 12:50
 */
public class StartupReceiver extends RoboBroadcastReceiver {

    @Override
    protected void handleReceive(Context context, Intent intent) {

        NewInquiriesService.scheduleInquiryDownloadService(context);
    }
}
