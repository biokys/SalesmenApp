package eu.janmuller.application.salesmenapp.server;

import android.content.Context;
import android.util.Base64;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.db.*;
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
     * @return vraci ResultObject
     */
    public ResultObject isDeviceRegistered() throws ConnectionException {

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
                return resultObject;
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
            String urlParams = String.format("?auth=%s",
                    Helper.getUniqueId(mContext));

            URL url = new URL(mBaseUrl + "/followup" + urlParams);
            urlConnection = (HttpURLConnection) url.openConnection();

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", inquiry.temporary ? "-1" : inquiry.serverId));
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

    /**
     * Metoda natahne frontu neodeslanych zprav z DB, vsechny postupne odesle. Pri kazdem odeslani zpravy se smaze
     * zprava z fronty
     * @throws ConnectionException
     */
    public int sendFromSendQueue() throws ConnectionException {

        int count = 0;
        for (SendQueue sendQueue : SendQueue.getAllObjects(SendQueue.class)) {
            Inquiry inquiry = Inquiry.getByServerId(sendQueue.inquiryServerId);
            if (inquiry != null) {

                send(inquiry, sendQueue.mail, sendQueue.title, sendQueue.text, sendQueue.json);
                inquiry.delete();
                count++;
            }
        }

        return count;
    }

    public void send(Inquiry inquiry, String mail, String title, String text, List<Document> documents) throws ConnectionException {

        SendDataObject sendDataObject = getSendDataObject(documents);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String json = gson.toJson(sendDataObject);

        try {

            send(inquiry, mail, title, text, json);
        } catch (ConnectionException e) {

            // pokud dojde k chybe pri odesilani, pak zpravu ulozime do fronty
            SendQueue.push(inquiry, mail, title, text, json);
            throw e;
        }
    }

    public void send(Inquiry inquiry, String mail, String title, String text, String json) throws ConnectionException {

        HttpURLConnection urlConnection = null;
        try {
            String urlParams = String.format("?auth=%s",
                    Helper.getUniqueId(mContext));

            URL url = new URL(mBaseUrl + "/send" + urlParams);
            //URL url = new URL("http://192.168.1.12:4444/service.ashx/send" + urlParams);
            urlConnection = (HttpURLConnection) url.openConnection();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("mail", mail));
            params.add(new BasicNameValuePair("title", title));
            params.add(new BasicNameValuePair("text", text));
            params.add(new BasicNameValuePair("data", json/*StringEscapeUtils.escapeJava(json)*/));
            params.add(new BasicNameValuePair("id", inquiry.temporary ? "-1" : inquiry.serverId));
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

        // list obsahuujici jen viditelne dokumenty
        List<Document> filteredList = new ArrayList<Document>();
        for (Document document : documents) {

            if (document.show) {

                filteredList.add(document);
            }
        }
        SendDataObject sendDataObject = new SendDataObject();
        SendDataObject.Document[] sendDocuments = new SendDataObject.Document[filteredList.size()];
        int mainLoop = 0;
        for (Document document : filteredList) {

            SendDataObject.Document sendDocument = new SendDataObject.Document();
            sendDocument.ident = document.ident;
            sendDocument.version = document.version;
            List<DocumentPage> documentPages = document.getDocumentPagesByDocument(true);
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
                    //tag.value = Base64.encode(documentTag.value.getBytes(), Base64.DEFAULT);
                    //tag.value = // base64 StringEscapeUtils.escapeJava(documentTag.value);
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

    public class ResultObject {

        public boolean status;
        public String url;
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
