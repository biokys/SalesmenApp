package eu.janmuller.application.salesmenapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import eu.janmuller.application.salesmenapp.activity.InquiryActivityHelper;
import eu.janmuller.application.salesmenapp.server.ServerService;
import roboguice.receiver.RoboBroadcastReceiver;

public class NetworkChangeReceiver extends RoboBroadcastReceiver {

    private ServerService mServerService;

    @Override
    protected void handleReceive(Context context, Intent intent) {

        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isAvailable() || mobile.isAvailable()) {
            // Do something

            InquiryActivityHelper.resendMessages(mServerService, new InquiryActivityHelper.IResendMessageCallback() {

                @Override
                public void onMesagesSent(int count) {

                }
            });
        }
    }
}
