package eu.janmuller.application.salesmenapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.db.Document;
import eu.janmuller.application.salesmenapp.model.db.DocumentPage;
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

    @InjectView(R.id.pager)
    private ViewPager mViewPager;

    private List<DocumentPage> mDocumentPages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // nastavime fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        final Document document = (Document) intent.getSerializableExtra(DOCUMENT);

        if (document == null) {

            finish();
            return;
        }

        getPages(document);

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

                WebView webView = new WebView(FullscreenModeActivity.this);
                Helper.showHtml(webView, document, mDocumentPages.get(position));
                container.addView(webView);
                return webView;
            }
        });

    }

    private void getPages(Document document) {

        mDocumentPages = DocumentPage.getByQuery(
                DocumentPage.class, "show=1 and documentId=" + document.id.getId());
    }
}
