package eu.janmuller.application.salesmenapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.db.*;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Aktivita ma na starosti zobrazovani seznamu dokumentu + zobrazovani konkretnich stranek jednotlivych dokumentu
 * <p/>
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 18.10.13
 * Time: 13:42
 */
@ContentView(R.layout.view_activity)
public class ViewActivity extends BaseActivity {

    public static final String INQUIRY = "inquiry";
    public static final String TEMP    = "temp";

    @InjectView(R.id.list)
    private LinearLayout mSideBarView;

    @InjectView(R.id.webview_container)
    private LinearLayout mWebViewContainer;

    @InjectView(R.id.scrollview)
    private ScrollView mScrollView;

    private Inquiry                    mInquiry;
    private List<Document>             mDocuments;
    private Document                   mDocument;
    private boolean                    mEditMode;
    private boolean                    mPageViewMode;
    private int                        mActionBarDisplayOptions;
    private Map<DocumentPage, WebView> mWebViewMap;
    private DocumentPage               mActualPage;
    private boolean                    mTempInquiry;
    private WebView                    mInfoWebView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // ziskam Inquiry objekt z EXTRAS
        Intent intent = getIntent();
        mInquiry = (Inquiry) intent.getSerializableExtra(INQUIRY);
        mTempInquiry = intent.getBooleanExtra(TEMP, false);

        // configure actionbar
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setTitle(mInquiry.title);

        setUp();
    }

    private void setUp() {

        mDocuments = Document.getByQuery(Document.class, "inquiryId=" + mInquiry.id.getId());
        mWebViewMap = new HashMap<DocumentPage, WebView>();

        // pokud otevirame poptavku poprve, pak k ni priradime vsechny sablony
        if (mDocuments.size() == 0) {

            ViewActivityHelper.createDocuments(mDocuments, mInquiry);
        }

        // zobrazim dokumenty jako stocky v postranim menu
        fillSideBar(mDocuments);
    }

    /**
     * Zobrazi stranky konkretniho dokumentu + zobrazi pohled na 1. stranku dokumentu
     *
     * @param document
     */
    private void fillSideBar(final Document document) {

        mSideBarView.removeAllViews();
        mPageViewMode = true;
        mDocument = document;
        invalidateOptionsMenu();

        List<DocumentPage> pages = DocumentPage.getByQuery(DocumentPage.class, "documentId=" + document.id.getId());
        if (pages.size() == 0) {

            return;
        }

        for (final DocumentPage page : pages) {

            // vynechame ty, ktere jsou schovane
            if (ViewActivityHelper.excludeHidden(page, mEditMode)) {

                continue;
            }

            View view = getLayoutInflater().inflate(R.layout.documentlistview, null);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    showHtml(document, page);
                }
            });

            ImageView imageView = ViewActivityHelper.getThumbnailImage(view, document, page.thumbnail);
            TextView textView = (TextView) view.findViewById(R.id.text);
            textView.setText(String.valueOf(page.position));

            ViewActivityHelper.manageVisibility(mEditMode, view, imageView, page, new ViewActivityHelper.IVisibilityChangeCallback() {
                @Override
                public void onVisibilityChanged() {

                    fillSideBar(document);
                }
            });
            mSideBarView.addView(view);
        }

        // zobrazim prvni zobrazitelnou stranku
        DocumentPage firstShowablePage = ViewActivityHelper.getFirstShowablePage(pages);
        if (firstShowablePage != null) {

            if (mActualPage != null && mActualPage.show) {

                firstShowablePage = mActualPage;
            }
            showHtml(document, firstShowablePage);
        }
    }

    private void showHtml(Document document, final DocumentPage page) {

        mActualPage = page;

        WebView webView = mWebViewMap.get(page);

        if (webView == null) {

            webView = new WebView(ViewActivity.this);
            ViewActivityHelper.configureWebView(webView);
            Helper.showHtml(webView, document, page);
            mWebViewMap.put(page, webView);
        }

        final WebView _webview = webView;
        new Handler().post(new Runnable() {
            @Override
            public void run() {

                List<DocumentTag> list = DocumentTag.getByQuery(DocumentTag.class, "documentPageId=" + page.id.getId());
                for (DocumentTag documentTag : list) {

                    ViewActivityHelper.setCustomText(_webview, documentTag);
                }

                if (mEditMode) {

                    ViewActivityHelper.setEditHtmlCellsVisibility(_webview, true);
                }


            }
        });

        mWebViewContainer.removeAllViews();
        mWebViewContainer.addView(webView);
    }

    /**
     * Zobrazi v postranim panelu nahledy jednotlivych dokumentu
     *
     * @param documents
     */
    private void fillSideBar(final List<Document> documents) {

        mSideBarView.removeAllViews();
        mWebViewContainer.removeAllViews();
        mPageViewMode = false;
        invalidateOptionsMenu();

        for (final Document document : documents) {

            // vynechame ty, ktere jsou schovane pokud nejsme v editmodu
            if (ViewActivityHelper.excludeHidden(document, mEditMode)) {

                continue;
            }

            View view = getLayoutInflater().inflate(R.layout.documentlistview, null);

            // po kliku na stocek zobrazim v postranim panelu jednotlive stranky
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    fillSideBar(document);
                }
            });

            TextView textView = (TextView) view.findViewById(R.id.text);
            textView.setText(document.name);

            // nastavim nahled dokumentu
            ImageView imageView = ViewActivityHelper.getThumbnailImage(view, document, document.thumbnail);

            // nastavim stav nahledu (shovany, zobrazeny)
            ViewActivityHelper.manageVisibility(mEditMode, view, imageView, document, new ViewActivityHelper.IVisibilityChangeCallback() {
                @Override
                public void onVisibilityChanged() {

                    fillSideBar(mDocuments);
                }
            });

            // pridam do containeru
            mSideBarView.addView(view);

        }
        showInlineInquiryInfo();
    }

    private void showInlineInquiryInfo() {

        if (mInfoWebView == null) {

            final WebView webView = new WebView(ViewActivity.this);
            ViewActivityHelper.configureWebView(webView);
            mInfoWebView = webView;
            List<Template> templates = Template.getByQuery(Template.class, "type='Info'");
            if (templates.size() > 0) {

                Template template = templates.get(0);
                List<TemplatePage> pages = TemplatePage.getByQuery(TemplatePage.class, "templateId=" + template.id.getId());
                if (pages.size() > 0) {

                    final Page page = pages.get(0);

                    Helper.showHtml(webView, template, page);

                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {

                            List<TemplateTag> list = TemplateTag.getByQuery(TemplateTag.class, "pageId=" + page.id.getId());
                            for (TemplateTag templateTag : list) {

                                ViewActivityHelper.replaceTagByInquiryData(mInquiry, templateTag);
                                ViewActivityHelper.setCustomText(webView, templateTag);
                            }
                        }
                    });

                }
            }
        }

        mWebViewContainer.removeAllViews();
        mWebViewContainer.addView(mInfoWebView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.view_menu, menu);

        for (int i = 0; i < menu.size(); i++) {

            MenuItem item = menu.getItem(i);

            if (item.getItemId() == R.id.menu_fullscreen) {

                item.setVisible(!mEditMode && mPageViewMode);
            } else {

                item.setVisible(!mEditMode);
            }

            if (item.getItemId() == R.id.menu_info) {

                item.setVisible(!mPageViewMode);
            }
        }


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                onBackPressed();
                break;
            case R.id.menu_send:

                openSendActivity();
                break;
            case R.id.menu_edit:

                switch2EditMode();
                break;
            case R.id.menu_fullscreen:

                runFullscreenMode();
                break;
            case R.id.menu_info:

                showInquiryInfo();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showInquiryInfo() {

        Intent intent = new Intent(this, InquiryInfoActivity.class);
        intent.putExtra(INQUIRY, mInquiry);
        startActivity(intent);
    }

    private void runFullscreenMode() {

        Intent intent = new Intent(this, FullscreenModeActivity.class);
        intent.putExtra(FullscreenModeActivity.DOCUMENT, mDocument);
        startActivity(intent);
    }

    private void switch2EditMode() {

        mEditMode = true;
        refreshSideBar();


        View view = getLayoutInflater().inflate(R.layout.action_bar_done, null);
        view.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch2ViewMode();
            }
        });
        mActionBarDisplayOptions = mActionBar.getDisplayOptions();
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setCustomView(view);
        invalidateOptionsMenu();
    }

    private void switch2ViewMode() {

        mEditMode = false;

        if (mPageViewMode) {

            mActualPage = null;
            saveEditChanges();
        }
        refreshSideBar();
        mActionBar.setDisplayOptions(mActionBarDisplayOptions);
        invalidateOptionsMenu();
    }

    private void saveEditChanges() {

        Iterator<DocumentPage> iterator = mWebViewMap.keySet().iterator();
        while (iterator.hasNext()) {

            DocumentPage page = iterator.next();
            WebView webView = mWebViewMap.get(page);
            ViewActivityHelper.getAndSaveTags(webView, page);
            ViewActivityHelper.setEditHtmlCellsVisibility(webView, false);
        }
    }

    private void refreshSideBar() {

        if (mPageViewMode) {

            fillSideBar(mDocument);
        } else {

            fillSideBar(mDocuments);
        }
    }

    private void openSendActivity() {

        Intent intent = new Intent(this, SendActivity.class);
        intent.putExtra(INQUIRY, mInquiry);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

        if (mPageViewMode) {

            mActualPage = null;
            fillSideBar(mDocuments);
        } else {

            super.onBackPressed();
        }
    }

    @Override
    public void finish() {

        if (mTempInquiry) {

            mInquiry.delete();
        }
        super.finish();
    }
}
