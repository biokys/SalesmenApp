package eu.janmuller.application.salesmenapp.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.Id;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.IHideAble;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.db.*;

import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 02.11.13
 * Time: 11:12
 */
class ViewActivityHelper {

    static String getBaseUrl(Template template) {

        return Helper.getTemplateFolderAsFile(template).getPath();
    }

    static void showHtml(WebView webView, Template template, DocumentPage page) {

        webView.loadUrl("file://" + getBaseUrl(template) + File.separator + page.file);
    }

    static void createDocuments(List<Document> documents, Id inquiryId) {

        List<Template> templates = Template.getAllObjects(Template.class);
        for (Template template : templates) {

            Document document = new Document(template, inquiryId);
            document.save();

            List<TemplatePage> pageList = TemplatePage.getByQuery(TemplatePage.class, "templateId=" + template.id.getId());
            for (TemplatePage templatePage : pageList) {


                DocumentPage documentPage = new DocumentPage(templatePage, document);
                documentPage.save();

                List<TemplateTag> tagList = TemplateTag.getByQuery(TemplateTag.class, "pageId=" + templatePage.id.getId());
                for (TemplateTag tag : tagList) {

                    DocumentTag documentTag = new DocumentTag(tag, documentPage);
                    documentTag.save();
                }
            }
            documents.add(document);
        }
    }

    static void configureWebView(WebView webView) {

        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }

    static void manageVisibility(boolean editMode, View view, ImageView imageView,
                                         final IHideAble document, final IVisibilityChangeCallback callback) {

        ImageView deleteView = (ImageView) view.findViewById(R.id.delete);
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideDocument(document, callback);
            }
        });

        ImageView showView = (ImageView) view.findViewById(R.id.show);
        showView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDocument(document, callback);
            }
        });

        if (editMode) {

            deleteView.setVisibility(document.isVisible() ? View.VISIBLE : View.GONE);
            showView.setVisibility(!document.isVisible() ? View.VISIBLE : View.GONE);
            imageView.setColorFilter(document.isVisible() ? Color.TRANSPARENT : Color.parseColor("#66000000"));
        } else {

            deleteView.setVisibility(View.GONE);
            showView.setVisibility(View.GONE);
        }
    }

    private static void hideDocument(IHideAble document, IVisibilityChangeCallback callback) {

        changeDocumentVisibility(document, false, callback);
    }

    private static void showDocument(IHideAble document, IVisibilityChangeCallback callback) {

        changeDocumentVisibility(document, true, callback);
    }

    private static void changeDocumentVisibility(IHideAble document, boolean show, IVisibilityChangeCallback callback) {

        document.setVisibility(show);
        if (document instanceof BaseDateModel) {

            ((BaseDateModel) document).save();
        }
        callback.onVisibilityChanged();

    }

    static ImageView getThumbnailImage(View view, Document document, String filename) {

        Bitmap bitmap = BitmapFactory.decodeFile(ViewActivityHelper.getBaseUrl(document) + File.separator + filename);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);
        return imageView;
    }

    static boolean excludeHidden(IHideAble hideAble, boolean editMode) {

        return !hideAble.isVisible() && !editMode;
    }

    interface IVisibilityChangeCallback {

        public void onVisibilityChanged();
    }
}
