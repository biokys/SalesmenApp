package eu.janmuller.application.salesmenapp;

import android.content.Context;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.janmuller.application.salesmenapp.model.*;
import eu.janmuller.application.salesmenapp.model.db.*;
import roboguice.util.Ln;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.10.13
 * Time: 16:51
 */
@Singleton
public class DownloadService {

    public static final String TEMPLATES_JSON_URL = "http://api-dallmayr.sb2000.cz/service.ashx/templates?auth=%s";
    public static final String INQUIRIES_JSON_URL = "http://api-dallmayr.sb2000.cz/service.ashx/inquiries?auth=%s";
    public static final String TEMPLATES_JSON     = "templates.json";
    public static final String INQUIRIES_JSON     = "inquiries.json";

    @Inject
    Context mContext;

    /**
     * 1. Stahne JSON data sablon ze serveru
     * 2. Metadata ulozi do DB
     * 3. Stahne data pro zobrazeni HTML (html, css, obrazky)
     * 4. Ulozi je na kartu
     */
    public void downloadTemplates(DownloadTask.IProgressCallback callback) {

        try {

            callback.onProgressUpdate(TEMPLATES_JSON, 0, 100);
            TemplatesEnvelope root = downloadTemplatesJson();
            callback.onProgressUpdate(TEMPLATES_JSON, 100, 100);
            Template[] templates = root.templates;
            saveTemplateMetadata2Db(templates);
            downloadAndSaveTemplateFiles(templates, callback);
        } catch (Exception e) {

            Ln.e(e);
        }
    }

    /**
     * Stahne a ulozi data poptavek do databaze
     *
     * @param callback
     */
    public void downloadAndSaveInquiries(DownloadTask.IProgressCallback callback) {

        try {

            callback.onProgressUpdate(INQUIRIES_JSON, 0, 100);
            URL url = new URL(String.format(INQUIRIES_JSON_URL, Helper.getUniqueId(mContext)));
            HttpURLConnection urlConnection = getConnectionFromUrl(url);
            InputStream inputStream = urlConnection.getInputStream();
            final Gson gson = new Gson();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            InquiriesEnvelope inquiriesEnvelope = gson.fromJson(reader, InquiriesEnvelope.class);
            for (Inquiry inquiry : inquiriesEnvelope.inquiries) {

                inquiry.attachments = "1a, 2b, 3c";
                inquiry.state = Inquiry.State.NEW;
                inquiry.save();
            }
            callback.onProgressUpdate(INQUIRIES_JSON, 100, 100);
        } catch (Exception e) {

            Ln.e(e);
        }
    }

    /**
     * Stahne ze serveru JSON obsahujici data sablon a deserializuje na objektovou strukturu
     *
     * @return pole sablon
     */
    private TemplatesEnvelope downloadTemplatesJson() throws Exception {

        URL url = new URL(String.format(TEMPLATES_JSON_URL, Helper.getUniqueId(mContext)));
        HttpURLConnection urlConnection = getConnectionFromUrl(url);
        InputStream inputStream = urlConnection.getInputStream();
        final Gson gson = new Gson();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return gson.fromJson(reader, TemplatesEnvelope.class);
    }

    private void saveTemplateMetadata2Db(Template[] templates) {

        for (Template template : templates) {

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
    }

    /**
     * Stahne vsechny soubory ke konkretni sablone a ulozi je do folderu v telefonu
     * Folder se jmenuje stejne jako baseUrl atribut v sablone
     *
     * @param templates
     * @throws Exception
     */
    private void downloadAndSaveTemplateFiles(Template[] templates, DownloadTask.IProgressCallback callback) throws Exception {

        deleteRecursive(Helper.getRootFolderAsFile());

        for (Template template : templates) {

            String[] files = template.files;

            for (String fileName : files) {

                downloadFile(template, fileName, callback);
            }
        }
    }

    private void downloadFile(Template template, String fileName, DownloadTask.IProgressCallback progressCallback) {

        try {

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
            fileOutput.close();

        } catch (Exception e) {

            Ln.e(e);
        }
    }

    private HttpURLConnection getConnectionFromUrl(URL url) throws Exception {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoOutput(true);
        urlConnection.connect();
        return urlConnection;
    }

    void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())

            for (File child : fileOrDirectory.listFiles()) {

                deleteRecursive(child);
            }

        fileOrDirectory.delete();
    }
}
