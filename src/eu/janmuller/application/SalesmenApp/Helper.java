package eu.janmuller.application.salesmenapp;

import android.os.Environment;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 31.10.13
 * Time: 14:23
 */
public class Helper {

    public static final String SALESMANAPP_FOLDER = "salesmanapp";

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

    public static File getParentFolderAsFile(String baseUrl) {

        String dir = Helper.md5(baseUrl);
        String completePath = SALESMANAPP_FOLDER + File.separator + dir;
        File parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(parent, completePath);
    }
}
