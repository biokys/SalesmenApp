package eu.janmuller.application.salesmenapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.adapter.DocumentAdapter;
import eu.janmuller.application.salesmenapp.adapter.ISidebarShowable;
import eu.janmuller.application.salesmenapp.model.db.*;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

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

    public static final int FULLSCREEN_REQUEST_CODE = 1999;

    @InjectView(R.id.webview_container)
    private LinearLayout mWebViewContainer;

    @InjectView(R.id.listview)
    private ListView mListView;

    private Handler mHandler = new Handler();

    private Inquiry                    mInquiry;
    private List<Document>             mDocuments;
    private Document                   mDocument;
    private boolean                    mEditMode;
    private boolean                    mPageViewMode;
    private int                        mActionBarDisplayOptions;
    private Map<DocumentPage, WebView> mWebViewMap;
    private PageContainer              mActualPage;
    private boolean                    mTempInquiry;
    private WebView                    mInfoWebView;
    private DocumentAdapter            mDocumentAdapter;
    private ProgressDialog             mProgressDialog;

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

    /**
     * Metoda vytvori list adapter, nacte z db documenty, ktere patri poptavce
     */
    private void setUp() {

        mWebViewMap = new HashMap<DocumentPage, WebView>();
        mDocumentAdapter = new DocumentAdapter(this);
        mListView.setAdapter(mDocumentAdapter);
        handleListViewItemClicks();
        mProgressDialog = ProgressDialog.show(this, null, "Pracuji...");

        new RoboAsyncTask<Void>(this) {

            @Override
            public Void call() throws Exception {

                mDocuments = mInquiry.getDocumentsByInquiry();

                // pokud otevirame poptavku poprve, pak k ni priradime vsechny sablony
                if (mDocuments.size() == 0) {

                    ViewActivityHelper.createDocuments(mDocuments, mInquiry);
                }
                return null;
            }

            @Override
            protected void onSuccess(Void aVoid) throws Exception {

                // zobrazim dokumenty jako stocky v postranim menu
                fillSideBar(mDocuments);
            }
        }.execute();
    }

    /**
     * Handluje click na polozku v listview
     */
    private void handleListViewItemClicks() {

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                setPage(i);
            }
        });
    }

    /**
     * Nastavi stranku podle dane pozice, nebo dokument v zavislosti na mPageViewMode
     * Pokud se jedna o editaci, pak ulozime aktualni stranku pred tim, nez prejdeme na dalsi
     * Zaroven take aktualni stranku odzoomujem na defaultni zoom level
     *
     * @param position pozice v listview (indexace od 0)
     */
    private void setPage(int position) {

        ISidebarShowable item = mDocumentAdapter.getItem(position);
        if (mPageViewMode) {

            final WebView lastWebView = mActualPage.webview;

            if (mEditMode) {

                saveActualPage(mActualPage);
            }
            // zobrazime novou stranku
            showHtml(item.getDocument(), (DocumentPage) item);

            // zoom-outujem aktualni webview na defaultni zoom
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    while (lastWebView.zoomOut()) {
                    }
                }
            });

        } else {

            fillSideBar(item.getDocument());
        }
    }

    /**
     * Zobrazi v postranim panelu nahledy jednotlivych dokumentu
     *
     * @param documents
     */
    private void fillSideBar(final List<Document> documents) {

        mActualPage = null;
        mPageViewMode = false;

        mDocumentAdapter.clear();
        mDocumentAdapter.setEditMode(mEditMode);
        mWebViewContainer.removeAllViews();

        invalidateOptionsMenu();

        mDocumentAdapter.addAll(ViewActivityHelper.filterHiddenItems(documents, mEditMode));
        mListView.setSelection(0);
        showInlineInquiryInfo();
    }

    /**
     * Zobrazi stranky konkretniho dokumentu + zobrazi pohled na 1. stranku dokumentu
     *
     * @param document
     */
    private void fillSideBar(final Document document) {

        boolean setPositionToStart = !mPageViewMode;
        mPageViewMode = true;

        mDocumentAdapter.clear();
        mDocumentAdapter.setEditMode(mEditMode);
        mDocument = document;
        invalidateOptionsMenu();

        List<DocumentPage> pages = document.getDocumentPagesByDocument();
        if (pages.size() == 0) {

            return;
        }

        for (DocumentPage page : pages) {

            page.parentDocument = document;
        }
        mDocumentAdapter.addAll(ViewActivityHelper.filterHiddenItems(pages, mEditMode));
        if (setPositionToStart) {

            mListView.setSelection(0);
        }

        // zobrazim prvni zobrazitelnou stranku
        DocumentPage firstShowablePage = ViewActivityHelper.getFirstShowablePage(pages);
        if (firstShowablePage != null) {

            if (mActualPage != null) {

                DocumentPage documentPage = mActualPage.documentPage;
                if (documentPage != null && documentPage.show) {

                    firstShowablePage = documentPage;
                }
            }
            showHtml(document, firstShowablePage);
        }
    }


    /**
     * Metoda slouzi k zobrazeni stranky dokumentu do webview
     * Webview se cachuji a tak se inicializuji pouze pri prvnim prohlizeni.
     * Do sablony ve webview se pomoci JS nastavuji informace z poptavky a z ulozenych dat
     * Zaroven se prepina mezi view/edit modem
     */
    private void showHtml(Document document, final DocumentPage page) {

        WebView webView = mWebViewMap.get(page);

        if (webView == null) {

            webView = new WebView(ViewActivity.this);
            ViewActivityHelper.configureWebView(webView);
            Helper.showHtml(webView, document, page);
            mWebViewMap.put(page, webView);
        }

        mActualPage = new PageContainer(webView, page);
        final WebView _webview = webView;
        new Handler().post(new Runnable() {
            @Override
            public void run() {

                List<DocumentTag> list = page.getDocumentTagsByPage();
                for (DocumentTag documentTag : list) {

                    if (documentTag.tagIdent != null && documentTag.tagIdent.length() > 0) {
                        ViewActivityHelper.setCustomText(_webview, documentTag);
                    }
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
     * Metoda, ktera zobrazuje uvodni obrazovku pri zobrazeni dokumentuuu v poptavce
     * Pokud se jedna o regulerni poptavku, pak se zde zobrazi informace o poptavce
     * Vezme se sablona a do ni se vsadi informace s poptavky
     * <p/>
     * Pokud se jedna o docasnou poptavku (coz je poptavka pouze pro zobrazeni sablon), pak se zde zobrazi
     * logo vendora
     */
    private void showInlineInquiryInfo() {

        if (!mTempInquiry) {

            if (mInfoWebView == null) {

                mInfoWebView = new WebView(ViewActivity.this);
                ViewActivityHelper.configureWebView(mInfoWebView);
                List<Template> templates = Template.getByQuery(Template.class, "type='Info'");
                if (templates.size() > 0) {

                    final Template template = templates.get(0);
                    List<TemplatePage> pages = template.getTemplatePagesByTemplate();
                    if (pages.size() > 0) {

                        final Page page = pages.get(0);
                        Helper.showHtml(mInfoWebView, template, page, new WebViewClient() {

                            @Override
                            public void onPageFinished(WebView view, String url) {

                                List<TemplateTag> list = ((TemplatePage) page).getTemplateTagsByPage();
                                for (TemplateTag templateTag : list) {

                                    ViewActivityHelper.replaceTagByInquiryData(mInquiry, templateTag);
                                    ViewActivityHelper.setCustomText(mInfoWebView, templateTag);
                                }
                                dismissStartupDialog();
                            }
                        });
                    }
                }
            }
            mWebViewContainer.addView(mInfoWebView);
        } else {

            if (mWebViewContainer.getChildCount() == 0) {

                getLayoutInflater().inflate(R.layout.view_splash, mWebViewContainer);
            }
            dismissStartupDialog();
        }
    }

    /**
     * Metoda zodpovedna za vytvareni menu v actionbaru
     */
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
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Metoda zodpovedna za obsluhovani stisku tlacitek menu v actionbaru
     */
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

                openFullscreenModeActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Prepnuti do editacniho modu
     */
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

    /**
     * Prepnuti do prohlizeciho modu
     */
    private void switch2ViewMode() {

        mEditMode = false;

        if (mPageViewMode) {

            // pri prechodu z editace ulozime zmeny v aktualnim webview
            saveActualPage(mActualPage);
            // disablujem vsechny editacni policka
            disableContentEditable();
            mActualPage = null;
        }
        mActionBar.setDisplayOptions(mActionBarDisplayOptions);
        refreshSideBar();
    }

    /**
     * Ulozi aktualne zobrazenou stranku - editacni pole
     *
     * @param pageContainer aktualne zobrazene webview vcetne DocumentPage
     */
    private void saveActualPage(PageContainer pageContainer) {

        if (pageContainer != null) {

            ViewActivityHelper.getAndSaveTags(pageContainer.webview, pageContainer.documentPage);
            ViewActivityHelper.setEditHtmlCellsVisibility(pageContainer.webview, false);
        }
    }

    /**
     * U vsech stranek dokumentu odebere classu pro editaci, tzn. ze disabluje
     * editaci techto dokumentu
     */
    private void disableContentEditable() {

        Iterator<DocumentPage> iterator = mWebViewMap.keySet().iterator();
        while (iterator.hasNext()) {

            DocumentPage page = iterator.next();
            WebView webView = mWebViewMap.get(page);
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

    private void dismissStartupDialog() {

        if (mProgressDialog.isShowing()) {

            mProgressDialog.dismiss();
        }
    }

    /**
     * Otevre aktivitu na posilani zprav a preda ji aktualni poptavku
     */
    private void openSendActivity() {

        Intent intent = new Intent(this, SendActivity.class);
        intent.putExtra(INQUIRY, mInquiry);
        startActivity(intent);
    }

    /**
     * Otevre aktivitu zobrazujici fullscreen prezentaci + ji preda aktualne zobrazeny dokument
     */
    private void openFullscreenModeActivity() {

        Intent intent = new Intent(this, FullscreenModeActivity.class);
        intent.putExtra(FullscreenModeActivity.DOCUMENT, mDocument);
        startActivityForResult(intent, FULLSCREEN_REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {

        if (mEditMode) {

            switch2ViewMode();
        } else if (mPageViewMode) {

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FULLSCREEN_REQUEST_CODE && resultCode == RESULT_OK) {

            int page2Show = data.getIntExtra(FullscreenModeActivity.CURRENT_PAGE_CODE, 0);
            setPage(page2Show);
        }
    }

    private class PageContainer {

        final public WebView      webview;
        final public DocumentPage documentPage;

        private PageContainer(WebView webview, DocumentPage documentPage) {

            this.webview = webview;
            this.documentPage = documentPage;
        }
    }
}
