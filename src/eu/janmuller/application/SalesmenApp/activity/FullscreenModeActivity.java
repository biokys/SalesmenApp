package eu.janmuller.application.salesmenapp.activity;

import android.content.Intent;
import android.os.Bundle;
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

    @InjectView(R.id.previous)
    private View previousSlide;

    @InjectView(R.id.next)
    private View nextSlide;

    @InjectView(R.id.icon_next)
    private ImageView mImageViewNext;

    @InjectView(R.id.icon_previous)
    private ImageView mImageViewPrev;

    @InjectView(R.id.pager)
    private ViewPager mViewPager;

    private Document             mDocument;
    private List<DocumentPage>   mDocumentPages;
    private SparseArray<WebView> mWebViewSparseArray;

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

        getPages(mDocument);

        previousSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mViewPager.getCurrentItem() > 0) {

                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
                }
            }
        });

        nextSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mViewPager.getCurrentItem() < mDocumentPages.size() - 1) {

                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                }
            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                setArrowsVisibility(position);
            }
        });

        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {

                return mDocumentPages.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object o) {

                return view == o;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {

            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {

                return showPage(container, position);
            }
        });

        setArrowsVisibility(0);

    }

    private WebView showPage(ViewGroup container, final int position) {

        WebView webView = mWebViewSparseArray.get(position);

        if (webView == null) {

            webView = new WebView(FullscreenModeActivity.this);
            ViewActivityHelper.configureWebView(webView);
            Helper.showHtml(webView, mDocument, mDocumentPages.get(position));
            container.addView(webView);

            final WebView _webview = webView;
            webView.post(new Runnable() {
                @Override
                public void run() {

                    DocumentPage documentPage = mDocumentPages.get(position);
                    List<DocumentTag> list = DocumentTag.getByQuery(DocumentTag.class, "documentPageId=" + documentPage.id.getId());
                    for (DocumentTag documentTag : list) {

                        ViewActivityHelper.setCustomText(_webview, documentTag);
                    }
                }
            });
            mWebViewSparseArray.put(position, webView);
        }
        return webView;
    }

    private void setArrowsVisibility(int position) {

        mImageViewNext.setVisibility(position < mDocumentPages.size() - 1 ? View.VISIBLE : View.INVISIBLE);
        mImageViewPrev.setVisibility(position > 0 ? View.VISIBLE : View.INVISIBLE);
    }

    private void getPages(Document document) {

        mDocumentPages = DocumentPage.getByQuery(
                DocumentPage.class, "show=1 and documentId=" + document.id.getId());
        mWebViewSparseArray = new SparseArray<WebView>(mDocumentPages.size());
    }
}
