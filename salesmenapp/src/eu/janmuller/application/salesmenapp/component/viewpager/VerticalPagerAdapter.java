package eu.janmuller.application.salesmenapp.component.viewpager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.activity.PageContainer;
import eu.janmuller.application.salesmenapp.activity.ViewActivityHelper;
import eu.janmuller.application.salesmenapp.model.db.Document;
import eu.janmuller.application.salesmenapp.model.db.DocumentPage;

/**
 * Created by muller on 14/09/14.
 */
class VerticalPagerAdapter extends PagerAdapter {

    private Context mContext;
    private List<DocumentPage> mDocumentPages;
    private Document mDocument;
    private Map<DocumentPage, PageContainer> mPageContainerMap;
    private boolean mEditMode;

    /**
     * First item is page number, the second one is version page number.
     */
    private int[] mVersionPageIndex2show;

    public VerticalPagerAdapter(Context context) {
        mContext = context;
        mPageContainerMap = new HashMap<DocumentPage, PageContainer>();
    }

    void setData(Document document, List<DocumentPage> documentPages, int[] versionPageIndex2show) {
        mDocumentPages = documentPages;
        mDocument = document;
        mVersionPageIndex2show = versionPageIndex2show;
        notifyDataSetChanged();
    }

    void setEditMode(boolean editMode) {
        mEditMode = editMode;
        notifyDataSetChanged();
    }

    public PageContainer getPageContainerByIndex(int index) {
        if (mDocumentPages.size() > index) {
            return mPageContainerMap.get(mDocumentPages.get(index));
        }
        return null;
    }

    @Override
    public int getCount() {
        return mDocumentPages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        final DocumentPage page = mDocumentPages.get(position);
        List<DocumentPage> versions = page.versions;
        PageContainer pageContainer;
        ViewGroup view;
        if (versions != null) {
            int versionNumber = -1;
            if (mVersionPageIndex2show != null && mVersionPageIndex2show[0] == position) {
                versionNumber = mVersionPageIndex2show[1];
            }
            view = createViewPager(mDocument, page, versionNumber);
            pageContainer = new PageContainer(view, page);
        } else {
            WebView webView = new MyWebView(mContext);
            ViewActivityHelper.configureWebView(webView);
            Helper.showHtml(webView, mDocument, page, new WebViewClient() {

                @Override
                public void onPageFinished(WebView view, String url) {

                    ViewActivityHelper.modifyHtml(mEditMode, view, page);
                }
            });

            pageContainer = new PageContainer(webView, page);
            view = webView;
        }
        mPageContainerMap.put(page, pageContainer);
        container.addView(view);
        return view;
    }

    private ViewGroup createViewPager(final Document document, final DocumentPage parentPage, int version2show) {

        final List<DocumentPage> versions = parentPage.versions;
        if (versions == null) {
            return null;
        }

        List<DocumentPage> filteredPages = new ArrayList<DocumentPage>();
        for (DocumentPage documentPage : parentPage.versions) {
            if (documentPage.isVisible()) {
                filteredPages.add(documentPage);
            }
        }

        ViewGroup layout = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.view_page_layout, null);
        MyViewPager viewPager = (MyViewPager) layout.findViewById(R.id.horizontal_view_pager);
        CirclePageIndicator pageIndicator = (CirclePageIndicator) layout.findViewById(R.id.page_indicator);
        viewPager.setData(pageIndicator, document, filteredPages, mEditMode);
        if (version2show != -1) {
            viewPager.setCurrentItem(version2show);
        }
        return layout;
    }
}
