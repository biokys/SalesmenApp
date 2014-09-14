package eu.janmuller.application.salesmenapp.component.viewpager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.List;

import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.activity.ViewActivityHelper;
import eu.janmuller.application.salesmenapp.model.db.Document;
import eu.janmuller.application.salesmenapp.model.db.DocumentPage;

public class MyViewPager extends ViewPager {

    private MyPagerAdapter mPagerAdapter;
    private boolean mEditMode;

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(CirclePageIndicator pageIndicator, Document document, DocumentPage parentPage,
                        boolean editMode) {
        setOffscreenPageLimit(parentPage.versions.size());
        mEditMode = editMode;
        mPagerAdapter = new MyPagerAdapter(document, parentPage);
        setAdapter(mPagerAdapter);
        pageIndicator.setViewPager(this);
    }

    public WebView getCurrentWebView() {
        return mPagerAdapter.getWebView(getCurrentItem());
    }

    private class MyPagerAdapter extends PagerAdapter {

        List<DocumentPage> mVersions;
        SparseArray<WebView> mWebViewSparseArray;
        Document mDocument;

        private MyPagerAdapter(Document document, DocumentPage parentPage) {
            mWebViewSparseArray = new SparseArray<WebView>();
            mVersions = parentPage.versions;
            mDocument = document;
        }

        private WebView getWebView(int position) {
            return mWebViewSparseArray.get(position);
        }

        @Override
        public int getCount() {
            return mVersions.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            final DocumentPage documentPage = mVersions.get(position);
            WebView webView = new MyWebView(getContext());
            ViewActivityHelper.configureWebView(webView);
            Helper.showHtml(webView, mDocument, documentPage, new WebViewClient() {

                @Override
                public void onPageFinished(WebView view, String url) {

                    ViewActivityHelper.modifyHtml(mEditMode, view, documentPage);
                }
            });
            mWebViewSparseArray.put(position, webView);
            container.addView(webView);
            return webView;
        }
    }
}