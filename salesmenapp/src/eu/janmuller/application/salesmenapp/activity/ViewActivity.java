package eu.janmuller.application.salesmenapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.adapter.DocumentAdapter;
import eu.janmuller.application.salesmenapp.adapter.ISidebarShowable;
import eu.janmuller.application.salesmenapp.component.viewpager.MyViewPager;
import eu.janmuller.application.salesmenapp.component.viewpager.VerticalDocumentPager;
import eu.janmuller.application.salesmenapp.model.db.*;
import it.sephiroth.android.library.widget.HListView;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

import java.util.List;

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
    public static final String TEMP = "temp";

    public static final int FULLSCREEN_REQUEST_CODE = 1999;
    public static final String TAG_CHILD_CONTAINER = "test";

    @InjectView(R.id.vertical_viewpager)
    private VerticalDocumentPager mVerticalDocumentPager;

    @InjectView(R.id.listview)
    private ListView mListView;

    @InjectView(R.id.button_hide)
    private Button mButtonHide;

    @InjectView(R.id.button_show)
    private Button mButtonShow;

    @InjectView(R.id.layout_visibility)
    private LinearLayout mLayoutButtonsVisibility;

    @InjectView(R.id.static_container)
    private LinearLayout mStaticContainer;

    private Handler mHandler = new Handler();

    private Inquiry mInquiry;
    private List<Document> mDocuments;
    private Document mDocument;
    private boolean mEditMode;
    private boolean mPageViewMode;
    private int mActionBarDisplayOptions;
    private int mCurrentNumber;
    private int mCurrentVisibleSubPagesIndex;
    private WebView mInfoWebView;
    private DocumentAdapter mDocumentAdapter;
    private ProgressDialog mProgressDialog;
    private List<DocumentPage> mPages;
    private DocumentAdapter mChildPagesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // ziskam Inquiry objekt z EXTRAS
        Intent intent = getIntent();
        mInquiry = (Inquiry) intent.getSerializableExtra(INQUIRY);
        mCurrentVisibleSubPagesIndex = -1;

        // configure actionbar
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setTitle(mInquiry.title);

        mButtonHide.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                manageVisibility(false);
            }
        });

        mButtonShow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                manageVisibility(true);
            }
        });

        mVerticalDocumentPager.setVerticalDocumentPagerCallback(new VerticalDocumentPager.VerticalDocumentPagerCallback() {
            @Override
            public void onPageChanged(int index, DocumentPage documentPage) {
                mListView.smoothScrollToPosition(index);
                mDocumentAdapter.setSelectedItemIndex(index);
            }
        });
        setUp();
    }

    private void manageVisibility(boolean show) {
        mButtonHide.setEnabled(false);
        mButtonShow.setEnabled(false);
        mDocumentAdapter.setAllObjectsVisibility(show);
        mButtonHide.setEnabled(true);
        mButtonShow.setEnabled(true);
    }

    /**
     * Metoda vytvori list adapter, nacte z db documenty, ktere patri poptavce
     */
    private void setUp() {

        mDocumentAdapter = new DocumentAdapter(this);
        mListView.setAdapter(mDocumentAdapter);
        handleListViewItemClicks();
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.please_wait));
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

                mDocumentAdapter.setSelectedItemIndex(i);
                ISidebarShowable item = mDocumentAdapter.getItem(i);
                ViewGroup group = ((ViewGroup) ViewActivity.this.findViewById(android.R.id.content));
                View v = group.findViewWithTag(TAG_CHILD_CONTAINER);
                if (v != null) {
                    group.removeView(v);
                }
                if (i != mCurrentVisibleSubPagesIndex) {
                    if (mEditMode && item.hasChildren()) {
                        float top = view.getY();
                        int height = view.getHeight();

                        HListView hListView = new HListView(ViewActivity.this);
                        mChildPagesAdapter = new DocumentAdapter(ViewActivity.this);
                        mChildPagesAdapter.setEditMode(mEditMode);
                        List<DocumentPage> pages = mPages.get(i).versions;
                        List filteredPages = ViewActivityHelper.filterHiddenItems(pages, mEditMode);
                        mChildPagesAdapter.addAll(filteredPages);
                        hListView.setAdapter(mChildPagesAdapter);
                        hListView.setLayoutParams(new LinearLayout.LayoutParams(1000, height));
                        hListView.setBackgroundResource(R.color.app_background);
                        hListView.setY(top);
                        hListView.setX(getResources().getDimensionPixelSize(R.dimen.sidebar_width));
                        hListView.setTag(TAG_CHILD_CONTAINER);
                        group.addView(hListView);
                        mCurrentVisibleSubPagesIndex = i;
                    }
                } else {
                    mCurrentVisibleSubPagesIndex = -1;
                }
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

        mCurrentNumber = position;
        ISidebarShowable item = mDocumentAdapter.getItem(position);
        if (mPageViewMode) {

            mVerticalDocumentPager.setCurrentPage(position);
            /*WebView lastWebView = mActualPage.getWebView();

            if (mEditMode) {

                saveActualPage(mActualPage);
            }
            // zobrazime novou stranku
            //showHtml(item.getDocument(), (DocumentPage) item);

            // zoom-outujem aktualni webview na defaultni zoom
            final WebView _webview = lastWebView;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (_webview != null) {
                        while (_webview.zoomOut()) {
                        }
                    }
                }
            });*/

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

        mPageViewMode = false;
        mDocumentAdapter.clear();
        mVerticalDocumentPager.clear();
        mDocumentAdapter.setEditMode(mEditMode);
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

        mStaticContainer.setVisibility(View.GONE);
        boolean setPositionToStart = !mPageViewMode;
        mPageViewMode = true;

        mDocumentAdapter.clear();
        mDocumentAdapter.setEditMode(mEditMode);
        if (mChildPagesAdapter != null) {
            mChildPagesAdapter.setEditMode(mEditMode);
            mChildPagesAdapter.notifyDataSetChanged();
        }
        mDocument = document;
        invalidateOptionsMenu();

        mPages = document.getDocumentPagesByDocument();
        if (mPages.size() == 0) {
            return;
        }

        List filteredPages = ViewActivityHelper.filterHiddenItems(mPages, mEditMode);
        mDocumentAdapter.addAll(filteredPages);
        mVerticalDocumentPager.setData(document, (List<DocumentPage>) filteredPages);

        if (setPositionToStart) {
            mListView.setSelection(0);
        }

        DocumentPage firstShowablePage = null;
        PageContainer pageContainer = mVerticalDocumentPager.getCurrentPageContainer();
        if (pageContainer != null) {
            DocumentPage documentPage = pageContainer.getDocumentPage();
            if (documentPage != null && documentPage.show) {
                firstShowablePage = documentPage;
            }
        }
        if (firstShowablePage == null) {
            // zobrazim prvni zobrazitelnou stranku
            firstShowablePage = ViewActivityHelper.getFirstShowablePage(mPages);
        }
        //showHtml(document, firstShowablePage);
    }

    /**
     * Metoda, ktera zobrazuje uvodni obrazovku pri zobrazeni dokumentu v poptavce
     * Pokud se jedna o regulerni poptavku, pak se zde zobrazi informace o poptavce
     * Vezme se sablona a do ni se vsadi informace s poptavky
     * <p/>
     * Pokud se jedna o docasnou poptavku (coz je poptavka pouze pro zobrazeni sablon), pak se zde zobrazi
     * logo vendora
     */
    private void showInlineInquiryInfo() {

        mStaticContainer.setVisibility(View.VISIBLE);
        if (!mInquiry.temporary) {
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
                mStaticContainer.addView(mInfoWebView);
            }
        } else {
            if (mStaticContainer.getChildCount() == 0) {
                getLayoutInflater().inflate(R.layout.view_splash, mStaticContainer);
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
        mLayoutButtonsVisibility.setVisibility(View.VISIBLE);
        mVerticalDocumentPager.setEditMode(true);
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

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
        mEditMode = false;

        ViewGroup group = ((ViewGroup) ViewActivity.this.findViewById(android.R.id.content));
        View v = group.findViewWithTag(TAG_CHILD_CONTAINER);
        if (v != null) {
            group.removeView(v);
        }
        mLayoutButtonsVisibility.setVisibility(View.GONE);

        if (mPageViewMode) {

            // pri prechodu z editace ulozime zmeny v aktualnim webview
            PageContainer currentPageContainer = mVerticalDocumentPager.getCurrentPageContainer();
            if (currentPageContainer.getWebView() != null) {
                saveActualPage(currentPageContainer);
            }
            // disablujem vsechny editacni policka
            mVerticalDocumentPager.setEditMode(false);
        }
        mActionBar.setDisplayOptions(mActionBarDisplayOptions);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                progressDialog.dismiss();
                refreshSideBar();
            }
        }, 500);
    }

    /**
     * Ulozi aktualne zobrazenou stranku - editacni pole
     *
     * @param pageContainer aktualne zobrazene webview vcetne DocumentPage
     */
    private void saveActualPage(PageContainer pageContainer) {

        if (pageContainer != null) {

            ViewActivityHelper.getAndSaveTags(pageContainer.getWebView(), pageContainer.getDocumentPage());
            ViewActivityHelper.setEditHtmlCellsVisibility(pageContainer.getWebView(), false);
        }
    }

    private void refreshSideBar() {

        if (mPageViewMode) {
            final PageContainer pageContainer = mVerticalDocumentPager.getCurrentPageContainer();
            if (pageContainer != null && pageContainer.getDocumentPage() != null) {
                DocumentPage documentPage = DocumentPage.findObjectById(DocumentPage.class, pageContainer.getDocumentPage().id);
                if (documentPage != null) {
                    pageContainer.getDocumentPage().show = documentPage.show;
                }
                // zoom-outujem aktualni webview na defaultni zoom
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {

                        WebView webView = pageContainer.getWebView();
                        if (webView != null) {
                            while (webView.zoomOut()) {
                            }
                        }
                    }
                });
            }
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
        intent.putExtra(FullscreenModeActivity.CURRENT_PAGE_CODE, mCurrentNumber);
        startActivityForResult(intent, FULLSCREEN_REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {

        PageContainer pageContainer = mVerticalDocumentPager.getCurrentPageContainer();
        if (pageContainer != null
                && pageContainer.getView() != null) {
            WebView webView = pageContainer.getWebView();
            if (webView.canGoBack()) {
                // go back
                webView.goBack();
            } else {
                fillSideBar(mDocuments);
            }
        } else if (mEditMode) {
            switch2ViewMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {

        if (mInquiry.temporary) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.show();
            new Thread(new Runnable() {

                @Override
                public void run() {

                    mInquiry.delete();
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            ViewActivity.super.finish();
                        }
                    });
                }
            }).start();
        } else {

            super.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FULLSCREEN_REQUEST_CODE && resultCode == RESULT_OK) {

            int page2Show = data.getIntExtra(FullscreenModeActivity.CURRENT_PAGE_CODE, 0);
            setPage(page2Show);
        }
    }
}
