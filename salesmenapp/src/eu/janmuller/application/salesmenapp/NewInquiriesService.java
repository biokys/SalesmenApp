package eu.janmuller.application.salesmenapp;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.google.inject.Inject;
import eu.janmuller.application.salesmenapp.activity.InquiryListActivity;
import eu.janmuller.application.salesmenapp.server.DownloadService;
import roboguice.service.RoboIntentService;
import roboguice.util.Ln;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 11.12.13
 * Time: 21:42
 */
public class NewInquiriesService extends RoboIntentService {

    // minuta v ms
    public static final int ONE_MINUTE = 60 * 1000;

    @Inject
    private DownloadService mDownloadService;

    public NewInquiriesService() {

        super("NewInquiriesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            int newInquiriesCount = mDownloadService.downloadAndSaveInquiries(null);
            showNotification(newInquiriesCount);
        } catch (IOException e) {

            Ln.e(e);
        }
    }

    private void showNotification(int count) {

        if (count == 0) {

            return;
        }
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.stat_notify_sdcard_prepare)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getResources().getQuantityString(R.plurals.new_inquiries_count, count, count));
        mBuilder.setContentIntent(getPendingIntent());

        int mNotificationId = 001;
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

    private PendingIntent getPendingIntent() {

        Intent resultIntent = new Intent(this, InquiryListActivity.class);
        return PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    public static void scheduleInquiryDownloadService(Context context) {

        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(context, NewInquiriesService.class);
        PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int minutes = context.getResources().getInteger(R.integer.new_inquiries_update_period_in_minutes) * ONE_MINUTE;
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + minutes, minutes, pintent);
    }
}
