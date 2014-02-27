package eu.janmuller.application.salesmenapp.server;

import android.content.Context;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.InquiriesEnvelope;
import eu.janmuller.application.salesmenapp.model.TemplatesEnvelope;
import eu.janmuller.application.salesmenapp.model.db.Inquiry;
import eu.janmuller.application.salesmenapp.model.db.Template;
import eu.janmuller.application.salesmenapp.model.db.TemplatePage;
import eu.janmuller.application.salesmenapp.model.db.TemplateTag;
import roboguice.util.Ln;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Trida slouzici pro stahovani POPTAVEK a SABLON
 * <p/>
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.10.13
 * Time: 16:51
 */
@Singleton
public class DownloadService {

    public static String TEMPLATES_JSON_URL = "/templates?auth=%s";
    public static String INQUIRIES_JSON_URL = "/inquiries?auth=%s";

    public static final String TEMPLATES_JSON = "templates.json";
    public static final String INQUIRIES_JSON = "inquiries.json";

    private Context mContext;
    private String  mHtmlWithJs;
    private String  mTemplateJsonUrl;
    private String  mInquiriesJsonUrl;

    @Inject
    public DownloadService(Context context) {

        mContext = context;
        mTemplateJsonUrl = context.getResources().getString(R.string.base_url) + TEMPLATES_JSON_URL;
        mInquiriesJsonUrl = context.getResources().getString(R.string.base_url) + INQUIRIES_JSON_URL;
    }

    /**
     * 1. Stahne JSON data sablon ze serveru
     * 2. Metadata ulozi do DB
     *
     * @return velikost vsech sablon
     */
    public Template[] downloadTemplatesJson(DownloadData.IProgressCallback callback) throws IOException {

        // nacteme z resourcu cast html s JS pro zmenu classy EDIT
        mHtmlWithJs = Helper.loadJsHtml(mContext);

        callback.onProgressUpdate(TEMPLATES_JSON, 0, 100);
        TemplatesEnvelope root = downloadTemplatesJson();
        callback.onProgressUpdate(TEMPLATES_JSON, 100, 100);
        Template[] templates = root.templates;
        return getOnlyNewTemplates(templates);
    }

    /**
     * 1. Stahne data pro zobrazeni HTML (html, css, obrazky)
     * 2. Ulozi je na kartu
     *
     * @param templates
     * @param callback
     */
    public void downloadTemplatesData(Template[] templates, DownloadData.IProgressCallback callback) throws IOException {

        downloadAndSaveTemplateFiles(templates, callback);
    }

    /**
     * Stahne a ulozi data poptavek do databaze
     *
     * @param callback
     * @return pocet nove prijatych poptavek
     */
    public int downloadAndSaveInquiries(DownloadData.IProgressCallback callback) throws IOException {

        if (callback != null) {

            callback.onProgressUpdate(INQUIRIES_JSON, 0, 100);
        }

        URL url = new URL(String.format(mInquiriesJsonUrl, Helper.getUniqueId(mContext)));
        HttpURLConnection urlConnection = getConnectionFromUrl(url);
        InputStream inputStream = urlConnection.getInputStream();
        final Gson gson = new Gson();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        InquiriesEnvelope inquiriesEnvelope = gson.fromJson(reader, InquiriesEnvelope.class);
        int newInquiryCounter = 0;
        for (Inquiry inquiry : inquiriesEnvelope.inquiries) {

            // pokud inquiry jiz v DB je, pak ho zmergujeme
            // data ze serveru maji prioritu pri mergovani, jedine co se zachovava je STATE
            // pokud ze serveru prijde NEW, pak nechame NEW, pokud neco jineho, pak nastavime OPEN, tzn. ze pokud je
            // na tabletu stav COMPLETE, vrati se do stavu OPEN
            List<Inquiry> list = Inquiry.getByQuery(Inquiry.class, "serverId=" + inquiry.serverId);
            if (list.size() > 0) {

                Inquiry existingInquiry = list.get(0);
                existingInquiry.mergeWith(inquiry);
                continue;
            }
            newInquiryCounter++;
            inquiry.attachments = "";
            inquiry.state = Inquiry.State.NEW;
            inquiry.save();
        }
        if (callback != null) {

            callback.onProgressUpdate(INQUIRIES_JSON, 100, 100);
        }
        return newInquiryCounter;
    }

    /**
     * Stahne ze serveru JSON obsahujici data sablon a deserializuje na objektovou strukturu
     *
     * @return pole sablon
     */
    private TemplatesEnvelope downloadTemplatesJson() throws IOException {

        URL url = new URL(String.format(mTemplateJsonUrl, Helper.getUniqueId(mContext)));
        HttpURLConnection urlConnection = getConnectionFromUrl(url);
        InputStream inputStream = urlConnection.getInputStream();
        final Gson gson = new Gson();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return gson.fromJson(reader, TemplatesEnvelope.class);
    }

    /**
     * Vraci jen ty, ktere jeste nejsou ulozeny v DB
     *
     * @param templates
     * @return
     */
    private Template[] getOnlyNewTemplates(Template[] templates) {

        List<Template> templatesInDb = Template.getAllObjects(Template.class);
        List<Template> list = new ArrayList<Template>();
        for (Template template : templates) {

            // pokud dana sablona uz v db je, pak ji neukladame
            if (templatesInDb.contains(template)) {
                templatesInDb.remove(template);
                continue;
            }
            list.add(template);
        }
        for (Template template : templatesInDb) {
            Ln.d("Deleting template %s [version %f], which is not in received json", template.ident, template.version);
            //template.delete();
        }
        return list.toArray(new Template[list.size()]);
    }

    private void deleteTemplatesDeletedOnServer(Template[] templates) {

        List<Template> templateList = Template.getAllObjects(Template.class);
        for (Template template : templates) {
            if (templateList.contains(template)) {
                templateList.remove(template);
            }
        }
        for (Template template : templateList) {
            Ln.d("Deleting template %s [version %f], which is not in received json", template.ident, template.version);
            //template.delete();
        }
    }

    /**
     * Ulozi nove sablony
     */
    private void saveTemplateMetadata2Db(Template template) {

        template.save();
        for (TemplatePage page : template.pages) {

            page.templateId = template.id;
            page.save();
            for (TemplateTag tag : page.tags) {

                tag.pageId = page.id;
                tag.save();
            }
        }
    }

    /**
     * Stahne vsechny soubory ke konkretni sablone a ulozi je do folderu v telefonu
     * Folder se jmenuje stejne jako baseUrl atribut v sablone
     *
     * @param templates
     * @throws Exception
     */
    private void downloadAndSaveTemplateFiles(Template[] templates, DownloadData.IProgressCallback callback) {

        for (Template template : templates) {
            try {
                String[] files = template.files;
                for (String fileName : files) {
                    downloadFile(template, fileName, callback);
                }
                saveTemplateMetadata2Db(template);
            } catch (IOException e) {
                Ln.w(e, "Error while downloading template");
            }
        }
    }

    /**
     * Stahuje konkretni soubor
     *
     * @param template         sablona, ktera soubor obsahuje
     * @param fileName         jmeno souboru
     * @param progressCallback callback informujici o progressu
     * @throws IOException
     */
    private void downloadFile(Template template, String fileName, DownloadData.IProgressCallback progressCallback) throws IOException {

        URL url = new URL(template.baseUrl + fileName);
        File parentFolder = Helper.getTemplateFolderAsFile(template);
        File completePath = new File(parentFolder, fileName);
        Files.createParentDirs(completePath);

        FileOutputStream fileOutput = new FileOutputStream(completePath);
        HttpURLConnection urlConnection = getConnectionFromUrl(url);
        InputStream inputStream = urlConnection.getInputStream();
        int totalSize = urlConnection.getContentLength();
        int downloadedSize = 0;

        byte[] buffer = new byte[1024];
        int bufferLength;

        while ((bufferLength = inputStream.read(buffer)) > 0) {

            fileOutput.write(buffer, 0, bufferLength);
            downloadedSize += bufferLength;
            progressCallback.onProgressUpdate(fileName, downloadedSize, totalSize);
        }

        // pokud se jedna o HTML soubor, pak na jeho konec pridame fragment JS kodu pro zmenu
        // contenteditable vlastnosti
        if (fileName.endsWith(".html")) {

            fileOutput.write(mHtmlWithJs.getBytes());
        }
        fileOutput.close();

    }

    private HttpURLConnection getConnectionFromUrl(URL url) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoOutput(true);
        urlConnection.connect();
        return urlConnection;
    }
}
