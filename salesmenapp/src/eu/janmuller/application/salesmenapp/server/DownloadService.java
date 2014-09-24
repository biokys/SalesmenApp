package eu.janmuller.application.salesmenapp.server;

import android.content.Context;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.InquiriesEnvelope;
import eu.janmuller.application.salesmenapp.model.TemplatesEnvelope;
import eu.janmuller.application.salesmenapp.model.db.Inquiry;
import eu.janmuller.application.salesmenapp.model.db.Page;
import eu.janmuller.application.salesmenapp.model.db.Template;
import eu.janmuller.application.salesmenapp.model.db.TemplatePage;
import eu.janmuller.application.salesmenapp.model.db.TemplateTag;
import roboguice.util.Ln;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Handles templates and inquires downloads.
 *
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
     * Iterate over all the files in all templates and checking it against file storage, if the file exists.
     *
     * @return true, if everything is ok, otherwise false.
     */
    public boolean testDataConsistency() throws IOException {

        for (Template template : Template.getAllObjects(Template.class)) {
            File parentFolder = Helper.getTemplateFolderAsFile(template);
            for (String filename : readFromByteArray(template.fileNamesAsByteArray)) {
                File file = new File(parentFolder, filename);
                if (!file.exists()) {
                    return false;
                }
            }
        }
        return true;
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
     * Vraci jen ty, ktere jeste nejsou ulozeny v DB a zaroven maze vsechny z DB, ktere jsou jiz neaktualni
     *
     * @param templates
     * @return
     */
    private Template[] getOnlyNewTemplates(Template[] templates) {

        List<Template> templatesInDb = Template.getAllObjects(Template.class);
        List<Template> templatesToDelete = new ArrayList<Template>(templatesInDb);
        List<Template> list = new ArrayList<Template>();
        boolean skip;
        for (Template template : templates) {
            // find and remove template from list used for deleting unused templates
            templatesToDelete.remove(template);
            skip = false;
            for (Template templateInDb : templatesInDb) {
                if (templateInDb.ident.equals(template.ident)) {

                    // versions are same, so skip saving
                    if (template.version == templateInDb.version) {
                        skip = true;
                    }
                }
            }
            if (skip) {
                continue;
            }
            Ln.i("Adding new template %s [version %s]", template.ident, template.version);
            list.add(template);
        }
        Template.removeTemplates(templatesToDelete);
        return list.toArray(new Template[list.size()]);
    }

    /**
     * Called when only minor change occurred. Download documents and update current on file system.
     */
    private void moveToNextMinorVersion() {
        /*Ln.i("Deleting old template %s [version %f]", templateInDb.ident, templateInDb.version);
        templateInDb.deleteCompleteTemplate();
        list.add(template);*/
    }

    /**
     * Downloads bright new documents and store them to file system next to older version.
     */
    private void moveToNextMajorVersion() {

    }

    /**
     * Saves all the templates with respect to tree structure (parent/child). Tree structure is generated by
     * parentId property.
     *
     * @param template The template.
     */
    private void saveTemplateMetadata2Db(Template template) {

        GenericModel.beginTx();
        try {
            convertFileNamesToByteArray(template);
            template.save();
            for (TemplatePage page : template.pages) {

                page.templateId = template.id;
                page.parentId = -1;
                page.save();
                saveVersionsAsPage(page);
                saveTags(page);
            }
            GenericModel.setTxSuccesfull();
        } catch (IOException e) {
            Ln.e(e);
        } finally {
            GenericModel.endTx();
        }
    }

    /**
     * Save all the versions of one page like a normal page with parentId property set.
     * @param parentPage The parent {@link eu.janmuller.application.salesmenapp.model.db.TemplatePage}
     */
    private void saveVersionsAsPage(TemplatePage parentPage) {

        if (parentPage.versions == null) {
            return;
        }
        // save pages on 2nd level to db
        for (TemplatePage version : parentPage.versions) {
            version.parentId = (Long)parentPage.id.getId();
            version.templateId = parentPage.templateId;
            version.save();
            saveTags(version);
        }
    }

    /**
     * Save all the tags from {@link eu.janmuller.application.salesmenapp.model.db.TemplatePage}
     * @param page The page.
     */
    private void saveTags(TemplatePage page) {

        for (TemplateTag tag : page.tags) {
            tag.pageId = page.id;
            tag.save();
        }
    }

    /**
     * Converts filename array to byte array.
     *
     * @param template The template.
     */
    private void convertFileNamesToByteArray(Template template) throws IOException {

        List<String> list = Arrays.asList(template.files);
        template.fileNamesAsByteArray = writeToByteArray(list);
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

    /**
     * Write string list to byte array.
     *
     * @param list The list.
     */
    private byte[] writeToByteArray(List<String> list) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        for (String element : list) {
            out.writeUTF(element);
        }
        return baos.toByteArray();
    }

    /**
     * Read from byte array.
     *
     * @param bytes The data bytes.
     * @return List of strings.
     * @throws IOException
     */
    private List<String> readFromByteArray(byte[] bytes) throws IOException {

        List<String> list = new ArrayList<String>();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(bais);
        while (in.available() > 0) {
            String element = in.readUTF();
            list.add(element);
        }
        return list;
    }
}
