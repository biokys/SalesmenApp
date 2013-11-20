package eu.janmuller.application.salesmenapp.server;

import android.content.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.model.db.Inquiry;
import roboguice.util.Ln;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.11.13
 * Time: 17:00
 */
@Singleton
public class ServerService {

    @Inject
    private Context mContext;

    public static final String URL = "http://api-dallmayr.sb2000.cz/service.ashx/inquiry-close";

    public boolean closeInquiry(Inquiry inquiry) {

        try {

            String params = String.format("?auth=%s&id=%s", Helper.getUniqueId(mContext), inquiry.serverId);
            URL url = new URL(URL + params);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            return responseCode == 200;

        } catch (Exception e) {

            Ln.e(e);
        }

        return false;
    }
}
