package eu.janmuller.application.salesmenapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.*;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.db.Document;
import eu.janmuller.application.salesmenapp.model.db.DocumentPage;
import eu.janmuller.application.salesmenapp.model.db.Inquiry;
import eu.janmuller.application.salesmenapp.model.db.Template;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

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

    @InjectView(R.id.list)
    private LinearLayout mSideBarView;

    @InjectView(R.id.webview)
    private WebView mWebView;

    @InjectView(R.id.scrollview)
    private ScrollView mScrollView;

    private List<Template> mTemplates;
    private Inquiry        mInquiry;
    private List<Document> mDocuments;
    private Document       mDocument;
    private boolean        mEditMode;
    private boolean        mPageViewMode;
    private int            mActionBarDisplayOptions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // ziskam Inquiry objekt z EXTRAS
        Intent intent = getIntent();
        mInquiry = (Inquiry) intent.getSerializableExtra(INQUIRY);
        if (mInquiry != null) {

            Toast.makeText(this, "Otevrena poptavka " + mInquiry.contact, Toast.LENGTH_SHORT).show();
        }

        // configure actionbar
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setTitle(mInquiry.title);

        setUp();
    }

    private void setUp() {

        ViewActivityHelper.configureWebView(mWebView);

        mDocuments = Document.getByQuery(Document.class, "inquiryId=" + mInquiry.id.getId());

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

                    Helper.showHtml(mWebView, document, page);
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

            Helper.showHtml(mWebView, document, firstShowablePage);
        }
    }

    /**
     * Zobrazi v postranim panelu nahledy jednotlivych dokumentu
     *
     * @param documents
     */
    private void fillSideBar(final List<Document> documents) {

        mSideBarView.removeAllViews();
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
        }


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                finish();
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
        }
        return super.onOptionsItemSelected(item);
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
        refreshSideBar();
        mActionBar.setDisplayOptions(mActionBarDisplayOptions);
        invalidateOptionsMenu();
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

            fillSideBar(mDocuments);
        } else {

            super.onBackPressed();
        }
    }

}
