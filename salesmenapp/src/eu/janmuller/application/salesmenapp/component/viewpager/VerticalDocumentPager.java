package eu.janmuller.application.salesmenapp.component.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.janmuller.application.salesmenapp.activity.PageContainer;
import eu.janmuller.application.salesmenapp.model.db.Document;
import eu.janmuller.application.salesmenapp.model.db.DocumentPage;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;

/**
 * Created by muller on 14/09/14.
 */
public class VerticalDocumentPager extends VerticalViewPager {

    private Document mDocument;
    private VerticalPagerAdapter mVerticalPagerAdapter;
    private VerticalDocumentPagerCallback mVerticalDocumentPagerCallback;
    private int mCurrentPosition;

    public VerticalDocumentPager(Context context) {
        super(context);
        init();
    }

    public VerticalDocumentPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mVerticalPagerAdapter = new VerticalPagerAdapter(getContext());
    }

    public void setData(Document document, List<DocumentPage> documentPages) {
        setData(document, documentPages, null);
    }

    /**
     * Set the data for view pager.
     *
     * @param document The document.
     * @param documentPages The documentPages.
     * @param versionPageIndex2show The two item array, containing document page as first item,
     *                              where should be applied page version set. The second item contains index of
     *                              version page.
     */
    public void setData(Document document, List<DocumentPage> documentPages, int[] versionPageIndex2show) {
        mDocument = document;
        mVerticalPagerAdapter.setData(document, documentPages, versionPageIndex2show);
        mCurrentPosition = 0;
        setAdapter(mVerticalPagerAdapter);
    }

    public void setCurrentPage(int index) {
        setCurrentPage(index, true);
    }

    public void setEditMode(boolean editMode) {
        mVerticalPagerAdapter.setEditMode(editMode);
    }

    public void setCurrentPage(int index, boolean smooth) {
        mCurrentPosition = index;
        setCurrentItem(index, smooth);
    }

    public void clear() {
        setData(mDocument, new ArrayList<DocumentPage>());
        mCurrentPosition = -1;
    }

    public PageContainer getCurrentPageContainer() {
        if (mCurrentPosition == -1) {
            return null;
        }
        return mVerticalPagerAdapter.getPageContainerByIndex(mCurrentPosition);
    }

    public void setVerticalDocumentPagerCallback(final VerticalDocumentPagerCallback verticalDocumentPagerCallback) {
        mVerticalDocumentPagerCallback = verticalDocumentPagerCallback;
        setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mCurrentPosition = position;
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == SCROLL_STATE_IDLE) {
                    if (verticalDocumentPagerCallback != null) {
                        verticalDocumentPagerCallback.onPageChanged(mCurrentPosition,
                                mVerticalPagerAdapter.getPageContainerByIndex(mCurrentPosition).getDocumentPage());
                    }
                }
            }
        });
    }

    public interface VerticalDocumentPagerCallback {
        public void onPageChanged(int index, DocumentPage documentPage);
    }
}
