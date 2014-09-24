package eu.janmuller.application.salesmenapp.activity;

import android.view.ViewGroup;
import android.webkit.WebView;

import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.component.viewpager.MyViewPager;
import eu.janmuller.application.salesmenapp.model.db.DocumentPage;

public class PageContainer {

    final private ViewGroup viewGroup;
    final private DocumentPage documentPage;

    public PageContainer(ViewGroup viewGroup, DocumentPage documentPage) {

        this.viewGroup = viewGroup;
        this.documentPage = documentPage;
    }

    public boolean isViewPager() {
        return documentPage.hasChildren();
    }

    public ViewGroup getView() {
        return viewGroup;
    }

    public void modifyWebView(boolean editMode) {
        ViewActivityHelper.modifyHtml(editMode, getWebView(), documentPage);
    }

    public MyViewPager getViewPager() {

        if (documentPage.hasChildren()) {
            return (MyViewPager) viewGroup.findViewById(R.id.horizontal_view_pager);
        }
        return null;
    }

    public WebView getWebView() {

        if (documentPage.hasChildren()) {
            MyViewPager myViewPager = (MyViewPager) viewGroup.findViewById(R.id.horizontal_view_pager);
            return myViewPager.getCurrentWebView();
        } else {
            return (WebView) viewGroup;
        }
    }

    public DocumentPage getDocumentPage() {
        return documentPage;
    }
}