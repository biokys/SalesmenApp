package eu.janmuller.application.salesmenapp;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import org.joda.time.DateTime;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 31.10.13
 * Time: 14:23
 */
public class Helper {

    public static final String SALESMANAPP_FOLDER = "salesmanapp";

    private static final SimpleDateFormat sSdf = new SimpleDateFormat("dd.MM.yyyy");

    public static String md5(String s) {

        MessageDigest digest;
        try {

            digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes(), 0, s.length());
            return new BigInteger(1, digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        }
        return "";
    }

    public static File getRootFolderAsFile() {

        return getParentFolderAsFile(null);
    }

    public static File getParentFolderAsFile(String baseUrl) {

        File parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (baseUrl == null) {

            return new File(parent, SALESMANAPP_FOLDER);
        }

        String dir = Helper.md5(baseUrl);
        String completePath = SALESMANAPP_FOLDER + File.separator + dir;
        return new File(parent, completePath);
    }

    public static String getUniqueId(Context context) {

        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static String formatDate(Date date) {

        DateTime dateTime = new DateTime();
        Date todayBegin = dateTime.withTimeAtStartOfDay().toDate();
        Date yesterdayBegin = dateTime.minusDays(1).withTimeAtStartOfDay().toDate();
        if (date.after(todayBegin)) {

            return "Dnes";
        } else if (date.after(yesterdayBegin) && date.before(todayBegin)) {

            return "Včera";
        } else {

            return sSdf.format(date);
        }
    }
}
