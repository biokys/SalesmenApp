package eu.janmuller.application.salesmenapp.server;

import org.apache.http.NameValuePair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 02.12.13
 * Time: 15:00
 */
public class ConnectionHelper {

    public static void doPost(HttpURLConnection urlConnection, List<NameValuePair> params) throws IOException {

        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");


        OutputStream os = urlConnection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(getQuery(params));
        writer.flush();
        writer.close();
        os.close();

        urlConnection.connect();
    }

    public static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params) {

            if (first) {

                first = false;
            } else {

                result.append("&");
            }

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
