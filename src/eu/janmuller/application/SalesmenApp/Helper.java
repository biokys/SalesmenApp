package eu.janmuller.application.salesmenapp;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.webkit.WebView;
import eu.janmuller.application.salesmenapp.model.db.DocumentPage;
import eu.janmuller.application.salesmenapp.model.db.Template;
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

    /**
     * @return md5ku podle zadaneho stringu
     */
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

    /**
     * @return Hlavni folder, kde jsou vsechny data aplikace
     */
    public static File getRootFolderAsFile() {

        return getTemplateFolderAsFile(null);
    }

    /**
     *
     * @param template Sablona
     * @return folder, kde se nachazi vsechna data sablony
     */
    public static File getTemplateFolderAsFile(Template template) {

        File parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (template == null) {

            return new File(parent, SALESMANAPP_FOLDER);
        }

        String dir = Helper.md5(template.baseUrl);
        String completePath = SALESMANAPP_FOLDER + File.separator + dir;
        return new File(parent, completePath);
    }

    /**
     * @return unikatni cislo zarizeni
     */
    public static String getUniqueId(Context context) {

        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    /**
     * Formatuje datum
     * @param date datum
     * @return Dnes, Vcera, jinak dd.MM.yyyy
     */
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
