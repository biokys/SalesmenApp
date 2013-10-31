package eu.janmuller.application.salesmenapp;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.janmuller.application.salesmenapp.model.Page;
import eu.janmuller.application.salesmenapp.model.Tag;
import eu.janmuller.application.salesmenapp.model.Template;
import eu.janmuller.application.salesmenapp.model.TemplatesEnvelope;
import roboguice.util.Ln;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.10.13
 * Time: 16:51
 */
@Singleton
public class DownloadService {

    @Inject
    Context mContext;

    /**
     * 1. Stahne JSON data sablon ze serveru
     * 2. Metadata ulozi do DB
     * 3. Stahne data pro zobrazeni HTML (html, css, obrazky)
     * 4. Ulozi je na kartu
     */
    public void downloadTemplates() {

        try {

            TemplatesEnvelope root = downloadTemplatesJson();
            Template[] templates = root.templates;
            saveTemplateMetadata2Db(templates);
            downloadAndSaveTemplateFiles(templates);
        } catch (Exception e) {

            Ln.e(e);
        }
    }

    /**
     * Stahne ze serveru JSON obsahujici data sablon a deserializuje na objektovou strukturu
     *
     * @return pole sablon
     */
    private TemplatesEnvelope downloadTemplatesJson() throws IOException {

        //TEMP

        InputStream inputStream = mContext.getResources().getAssets().open("templates.json");
        final Gson gson = new Gson();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return gson.fromJson(reader, TemplatesEnvelope.class);
    }

    private void saveTemplateMetadata2Db(Template[] templates) {

        for (Template template : templates) {

            template.save();
            for (Page page : template.pages) {

                page.templateId = template.id;
                page.save();
                for (Tag tag : page.tags) {

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
    private void downloadAndSaveTemplateFiles(Template[] templates) throws Exception {

        for (Template template : templates) {

            String baseUrl = template.baseUrl;
            String[] files = template.files;

            // ziskam folder, do ktereho budu stahovat vsechny fily
            File baseFolder = Helper.getParentFolderAsFile(baseUrl);
            // smazu vcetne podadresaru
            deleteRecursive(baseFolder);

            for (String fileName : files) {

                downloadFile(baseUrl, fileName);
            }
        }
    }

    private void downloadFile(String baseUrl, String fileName) throws Exception {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(baseUrl + fileName));

        File parentFolder = Helper.getParentFolderAsFile(baseUrl);
        File completePath = new File(parentFolder, fileName);
        Files.createParentDirs(completePath);
        Uri uri = Uri.fromFile(completePath);
        request.setDestinationUri(uri);
        DownloadManager manager = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }



    void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())

            for (File child : fileOrDirectory.listFiles()) {

                deleteRecursive(child);
            }

        fileOrDirectory.delete();
    }
}
