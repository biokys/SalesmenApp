package eu.janmuller.application.salesmenapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.webkit.WebView;
import eu.janmuller.application.salesmenapp.model.db.DocumentPage;
import eu.janmuller.application.salesmenapp.model.db.DocumentTag;
import eu.janmuller.application.salesmenapp.model.db.Page;
import eu.janmuller.application.salesmenapp.model.db.Template;
import org.joda.time.DateTime;
import roboguice.util.Ln;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 31.10.13
 * Time: 14:23
 */
public class Helper {

    public static final SimpleDateFormat sSdf = new SimpleDateFormat("dd.MM.yyyy");
    public static final String IS_PAIRED = "isPaired";

    /**
     * @param template Sablona pro kterou se vytvori slozka na filesystemu
     * @return folder, kde se nachazi vsechna data sablony
     */
    public static File getTemplateFolderAsFile(Template template) {

        String dir = template.ident + "_" + template.version;
        String completePath = Config.sActualVendor + File.separator + dir;
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), completePath);
    }

    /**
     * @return unikatni cislo zarizeni
     */
    public static String getUniqueId(Context context) {

        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static void setPaired(Activity activity) {

        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(IS_PAIRED, true);
        editor.commit();
    }

    public static boolean isPaired(Activity activity) {

        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getBoolean(IS_PAIRED, false);
    }



    public static String loadJsHtml(Context context) {

        String html = "";
        try {

            BufferedReader r = new BufferedReader(new InputStreamReader(context.getAssets().open("contenteditable.html")));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {

                total.append(line);
            }

            html = total.toString();
        } catch (IOException e) {

            Ln.e(e);
        }

        return html;
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

    public static String getBaseUrl(Template template) {

        return Helper.getTemplateFolderAsFile(template).getPath();
    }

    public static void showHtml(WebView webView, Template template, Page page) {

        webView.loadUrl("file://" + getBaseUrl(template) + File.separator + page.file);
    }
}
