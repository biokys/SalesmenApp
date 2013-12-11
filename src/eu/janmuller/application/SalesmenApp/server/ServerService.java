package eu.janmuller.application.salesmenapp.server;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.db.Document;
import eu.janmuller.application.salesmenapp.model.db.DocumentPage;
import eu.janmuller.application.salesmenapp.model.db.DocumentTag;
import eu.janmuller.application.salesmenapp.model.db.Inquiry;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import roboguice.util.Ln;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.11.13
 * Time: 17:00
 */
@Singleton
public class ServerService {

    public static final String GENERAL_ERROR = "Obecná chyba";
    private Context mContext;

    private String mBaseUrl;

    @Inject
    public ServerService(Context context) {

        mContext = context;
        mBaseUrl = context.getResources().getString(R.string.base_url);
    }

    public void closeInquiry(Inquiry inquiry) throws ConnectionException {

        HttpURLConnection urlConnection = null;
        try {

            String urlParams = String.format("?auth=%s", Helper.getUniqueId(mContext));
            URL url = new URL(mBaseUrl + "/inquiry-close" + urlParams);
            urlConnection = (HttpURLConnection) url.openConnection();

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", inquiry.serverId));

            ConnectionHelper.doPost(urlConnection, params);
            urlConnection.connect();
            if (urlConnection.getResponseCode() != 200) {

                throw new ConnectionException(urlConnection.getResponseMessage());
            }
        } catch (IOException e) {

            Ln.e(e);
            throw new ConnectionException(GENERAL_ERROR);
        } finally {

            if (urlConnection != null) {

                urlConnection.disconnect();
            }
        }
    }

    /**
     * Zjistuje, zda je zarizeni jiz registrovano
     *
     * @return vraci true, pokud byl dotaz uspesny a zarizeni je jiz registrovano
     */
    public boolean isDeviceRegistered() throws ConnectionException {

        HttpURLConnection urlConnection = null;
        try {
            String params = String.format("?auth=%s", Helper.getUniqueId(mContext));
            URL url = new URL(mBaseUrl + "/check-registration" + params);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();

            if (responseCode == 200) {

                InputStream inputStream = urlConnection.getInputStream();
                final Gson gson = new Gson();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                ResultObject resultObject = gson.fromJson(reader, ResultObject.class);
                return resultObject.status;
            } else {

                throw new ConnectionException(urlConnection.getResponseMessage());
            }
        } catch (IOException e) {

            Ln.e(e);
            throw new ConnectionException(GENERAL_ERROR);
        } finally {

            if (urlConnection != null) {

                urlConnection.disconnect();
            }
        }
    }

    /**
     * Znovu-osloveni
     *
     * @param inquiry poptavka ke ktere se vztahuje znovuosloveni
     * @param date    Datum znovuosloveni
     * @param message Popis znovuosloveni
     */
    public void followUp(Inquiry inquiry, String date, String message) throws ConnectionException {

        HttpURLConnection urlConnection = null;
        try {
            String inquiryId = inquiry.serverId;
            String urlParams = String.format("?auth=%s",
                    Helper.getUniqueId(mContext));

            URL url = new URL(mBaseUrl + "/followup" + urlParams);
            urlConnection = (HttpURLConnection) url.openConnection();

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", inquiryId));
            params.add(new BasicNameValuePair("date", date));
            params.add(new BasicNameValuePair("description", message));
            ConnectionHelper.doPost(urlConnection, params);
            urlConnection.connect();
            if (urlConnection.getResponseCode() != 200) {

                throw new ConnectionException(urlConnection.getResponseMessage());
            }
        } catch (IOException e) {

            Ln.e(e);
            throw new ConnectionException(GENERAL_ERROR);
        } finally {

            if (urlConnection != null) {

                urlConnection.disconnect();
            }
        }
    }


    public void send(Inquiry inquiry, String mail, String title, String text, List<Document> documents) throws ConnectionException {

        SendDataObject sendDataObject = getSendDataObject(documents);
        String json = new Gson().toJson(sendDataObject);

        HttpURLConnection urlConnection = null;
        try {
            String urlParams = String.format("?auth=%s",
                    Helper.getUniqueId(mContext));

            URL url = new URL(mBaseUrl + "/send" + urlParams);
            urlConnection = (HttpURLConnection) url.openConnection();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("mail", mail));
            params.add(new BasicNameValuePair("title", title));
            params.add(new BasicNameValuePair("text", text));
            params.add(new BasicNameValuePair("data", StringEscapeUtils.escapeJava(json)));
            params.add(new BasicNameValuePair("id", inquiry.serverId));
            ConnectionHelper.doPost(urlConnection, params);
            urlConnection.connect();
            if (urlConnection.getResponseCode() != 200) {

                throw new ConnectionException(urlConnection.getResponseMessage());
            }
        } catch (IOException e) {

            Ln.e(e);
            throw new ConnectionException(GENERAL_ERROR);
        } finally {

            if (urlConnection != null) {

                urlConnection.disconnect();
            }
        }
    }

    private SendDataObject getSendDataObject(List<Document> documents) {

        SendDataObject sendDataObject = new SendDataObject();
        SendDataObject.Document[] sendDocuments = new SendDataObject.Document[documents.size()];
        int mainLoop = 0;
        for (Document document : documents) {

            if (!document.show) {

                continue;
            }
            SendDataObject.Document sendDocument = new SendDataObject.Document();
            sendDocument.ident = document.ident;
            sendDocument.version = document.version;
            List<DocumentPage> documentPages = document.getDocumentPagesByDocument();
            SendDataObject.Document.Page[] pages = new SendDataObject.Document.Page[documentPages.size()];
            int loop = 0;
            for (DocumentPage documentPage : documentPages) {

                SendDataObject.Document.Page page = new SendDataObject.Document.Page();
                List<DocumentTag> documentTags = documentPage.getDocumentTagsByPage();
                SendDataObject.Document.Page.Tag[] tags = new SendDataObject.Document.Page.Tag[documentTags.size()];
                int tagLoop = 0;
                for (DocumentTag documentTag : documentTags) {

                    SendDataObject.Document.Page.Tag tag = new SendDataObject.Document.Page.Tag();
                    tag.ident = documentTag.tagIdent;
                    tag.value = documentTag.value;
                    tags[tagLoop++] = tag;
                }
                page.id = documentPage.file;
                page.tags = tags;
                pages[loop++] = page;
            }
            sendDocument.pages = pages;
            sendDocuments[mainLoop++] = sendDocument;
        }
        sendDataObject.documents = sendDocuments;
        return sendDataObject;
    }

    private class ResultObject {

        public boolean status;
    }

    public static class SendDataObject {

        public static class Document {

            public static class Page {

                public static class Tag {

                    @SerializedName("ident")
                    public String ident;

                    @SerializedName("value")
                    public String value;
                }

                @SerializedName("tags")
                public Tag[] tags;

                @SerializedName("id")
                public String id;
            }

            @SerializedName("ident")
            public String ident;

            @SerializedName("version")
            public double version;

            @SerializedName("pages")
            public Page[] pages;
        }

        @SerializedName("documents")
        public Document[] documents;
    }
}
