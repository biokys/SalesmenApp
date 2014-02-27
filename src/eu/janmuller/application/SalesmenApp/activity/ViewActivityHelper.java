package eu.janmuller.application.salesmenapp.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import com.google.gson.annotations.SerializedName;
import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.adapter.ISidebarShowable;
import eu.janmuller.application.salesmenapp.model.db.*;
import org.apache.commons.lang3.StringEscapeUtils;
import roboguice.util.Ln;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 02.11.13
 * Time: 11:12
 */
public class ViewActivityHelper {

    public static final Pattern TAG_REPLACE_REGEX = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    /**
     * Vytvori dokumenty k poptavce ze vsech dostupnych sablon
     * Zmeni stav poptavky na Otevrenou
     * Vse probiha v transakci
     * @param documents
     * @param inquiry
     */
    static void createDocuments(List<Document> documents, Inquiry inquiry) {

        float maxVersion = -1;
        for (Template t : Template.getByQuery(Template.class, "type='Doc'")) {
            maxVersion = Math.max(maxVersion, t.version);
        }

        List<Template> templates = Template.getByQuery(Template.class, "version=" + maxVersion + " and type='Doc'");
        try {

            GenericModel.beginTx();
            for (Template template : templates) {

                Document document = new Document(template, inquiry.id);
                document.save();

                List<TemplatePage> pageList = template.getTemplatePagesByTemplate();
                for (TemplatePage templatePage : pageList) {

                    DocumentPage documentPage = new DocumentPage(templatePage, document);
                    documentPage.save();

                    List<TemplateTag> tagList = templatePage.getTemplateTagsByPage();
                    for (TemplateTag tag : tagList) {

                        DocumentTag documentTag = new DocumentTag(tag, documentPage);
                        replaceTagByInquiryData(inquiry, documentTag);
                        documentTag.save();
                    }
                }
                documents.add(document);
            }
            inquiry.state = Inquiry.State.OPEN;
            inquiry.save();
            GenericModel.setTxSuccesfull();
        } catch (Exception e) {

            Ln.e(e);
        } finally {

            GenericModel.endTx();
        }
    }

    public static void replaceTagByInquiryData(Inquiry inquiry, Tag tag) {

        if (inquiry.temporary) {

            tag.value = "";
            return;
        }
        String value = tag.value;

        Matcher matcher = TAG_REPLACE_REGEX.matcher(value);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {

            // dostaneme text ve tvaru {{..}}
            String textToReplace = matcher.group();

            // odfiltrujeme {{ a }}
            textToReplace = textToReplace.substring(2);
            textToReplace = textToReplace.substring(0, textToReplace.length() - 2);

            try {

                // vymenime tag za skutecnou hodnotu z instance objektu Inquiry
                textToReplace = getValueByAttributeName(inquiry, textToReplace);
            } catch (Exception e) {

                Ln.e(e, "text: " + textToReplace);
            }
            if (textToReplace != null) {

                matcher.appendReplacement(stringBuffer, textToReplace);
            }
        }
        matcher.appendTail(stringBuffer);
        tag.value = stringBuffer.toString();

    }

    /**
     * Na zaklade jmena promenne (takova, ktera chodi v JSONu), ktere odpovida anotaci SerializedName
     * najdeme hodnotu a vratime
     * @throws IllegalAccessException
     */
    private static String getValueByAttributeName(Inquiry inquiry, String name) throws Exception {

        Field[] fields = inquiry.getClass().getFields();
        for (Field field : fields) {

            if (field.isAnnotationPresent(SerializedName.class)) {

                SerializedName annotation = field.getAnnotation(SerializedName.class);
                if (name.equals(annotation.value())) {

                    if (field.getType() == String.class) {

                        return (String)field.get(inquiry);
                    }
                }
            }
        }

        return "";
    }

    public static void configureWebView(WebView webView) {

        webView.setWebViewClient(new WebViewClient());
        webView.requestFocus(View.FOCUS_DOWN);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLightTouchEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webView.addJavascriptInterface(new WebAppInterface(), "Android");
    }

    static void setEditHtmlCellsVisibility(WebView webView, boolean visible) {

        webView.loadUrl("javascript:switchMode(" + (visible ? "'edit'" : "") + ")");
    }

    static void setCustomText(WebView webView, Tag documentTag) {

        String text = StringEscapeUtils.escapeJava(documentTag.value);
        webView.loadUrl("javascript:setCustomText('" + documentTag.tagIdent + "','" + text + "')");
    }

    static void getAndSaveTags(WebView webView, DocumentPage documentPage) {

        webView.loadUrl("javascript:getAllEditableElements('" + documentPage.id.getId().toString() + "')");
    }

    public static void manageVisibility(boolean editMode, View view, ImageView imageView,
                                 final ISidebarShowable document, final IVisibilityChangeCallback callback) {

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

            imageView.setColorFilter(Color.TRANSPARENT);
            deleteView.setVisibility(View.GONE);
            showView.setVisibility(View.GONE);
        }
    }

    private static void hideDocument(ISidebarShowable document, IVisibilityChangeCallback callback) {

        changeDocumentVisibility(document, false, callback);
    }

    private static void showDocument(ISidebarShowable document, IVisibilityChangeCallback callback) {

        changeDocumentVisibility(document, true, callback);
    }

    private static void changeDocumentVisibility(ISidebarShowable document, boolean show, IVisibilityChangeCallback callback) {

        document.setVisibility(show);
        if (document instanceof BaseDateModel) {

            ((BaseDateModel) document).save();
        }
        callback.onVisibilityChanged();

    }

    public static ImageView getThumbnailImage(View view, Document document, String filename) {

        Bitmap bitmap = BitmapFactory.decodeFile(Helper.getBaseUrl(document) + File.separator + filename);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);
        return imageView;
    }

    /**
     * Vrati prvni zobrazitelnou stranku
     *
     * @param documentPages
     * @return null, jestlize jsou vsechny sranky skryte, jinak vrati prvni zobrazitelnout
     */
    static DocumentPage getFirstShowablePage(List<DocumentPage> documentPages) {

        for (DocumentPage documentPage : documentPages) {

            if (documentPage.show) {

                return documentPage;
            }
        }

        return null;
    }

    /**
     * Pomocna metoda, ktera odfiltruje z listu polozky podle logiky dane metodou excludeHidden
     * @param items vstupni list polozek
     * @return pouze polozky ktere se maji zobrazit
     */
    static List<ISidebarShowable> filterHiddenItems(List items, boolean editMode) {

        List<ISidebarShowable> visibleItems = new ArrayList<ISidebarShowable>();
        for (ISidebarShowable item : (List<ISidebarShowable>) items) {

            if (!excludeHidden(item, editMode)) {

                visibleItems.add(item);
            }
        }

        return visibleItems;
    }

    /**
     * Rozhoduje o viditelnosti polozky - polozka se nezobrazi pokud se jedna o normalni mod a zaroven je polozka
     * skryta. Naopak zobrazi se, pokud je polozka oznacena jako sktryta ale jsme v editacnim rezimu
     * @param hideAble polozka implentujici ISidebarShowable interface
     * @param editMode rezim zobrazeni (normal/edit)
     * @return true pokud se polozka nema zobrazit
     */
    private static boolean excludeHidden(ISidebarShowable hideAble, boolean editMode) {

        return !hideAble.isVisible() && !editMode;
    }

    public interface IVisibilityChangeCallback {

        public void onVisibilityChanged();
    }
}
