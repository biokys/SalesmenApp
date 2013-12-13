package eu.janmuller.application.salesmenapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.google.inject.Inject;
import eu.janmuller.application.salesmenapp.activity.InquiryListActivity;
import eu.janmuller.application.salesmenapp.server.DownloadService;
import roboguice.service.RoboIntentService;
import roboguice.util.Ln;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 11.12.13
 * Time: 21:42
 */
public class NewInquiriesService extends RoboIntentService {

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
}
