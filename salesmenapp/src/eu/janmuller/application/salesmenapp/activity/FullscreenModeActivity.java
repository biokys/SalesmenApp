package eu.janmuller.application.salesmenapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;

import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.component.viewpager.VerticalDocumentPager;
import eu.janmuller.application.salesmenapp.model.db.Document;
import eu.janmuller.application.salesmenapp.model.db.DocumentPage;
import eu.janmuller.application.salesmenapp.model.db.DocumentTag;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 02.11.13
 * Time: 14:41
 */
@ContentView(R.layout.fullscreen_activity)
public class FullscreenModeActivity extends BaseActivity {

    public static final String DOCUMENT = "document";
    public static final String CURRENT_PAGE_CODE = "current_page";

    @InjectView(R.id.vertical_viewpager)
    private VerticalDocumentPager mVerticalDocumentPager;

    private Document mDocument;
    private int mCurrentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // nastavime fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mDocument = (Document) intent.getSerializableExtra(DOCUMENT);
        if (mDocument == null) {
            finish();
            return;
        }

        List<DocumentPage> pages = mDocument.getDocumentPagesByDocument();
        mVerticalDocumentPager.setData(mDocument, ViewActivityHelper.filterHiddenItems(pages, false));
        mVerticalDocumentPager.setVerticalDocumentPagerCallback(new VerticalDocumentPager.VerticalDocumentPagerCallback() {
            @Override
            public void onPageChanged(int index, DocumentPage documentPage) {
                mCurrentPage = index;
            }
        });

        if (intent.hasExtra(CURRENT_PAGE_CODE)) {
            int position2Show = intent.getIntExtra(CURRENT_PAGE_CODE, 0);
            mVerticalDocumentPager.setCurrentPage(position2Show, false);
        }
    }

    @Override
    public void onBackPressed() {

        PageContainer currentPageContainer = mVerticalDocumentPager.getCurrentPageContainer();
        if (currentPageContainer != null && currentPageContainer.getWebView().canGoBack()) {
            currentPageContainer.getWebView().goBack();
        } else {
            Intent intent = new Intent();
            intent.putExtra(CURRENT_PAGE_CODE, mCurrentPage);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
