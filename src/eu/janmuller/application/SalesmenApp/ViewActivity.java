package eu.janmuller.application.SalesmenApp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.*;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import eu.janmuller.application.SalesmenApp.model.Inquiry;
import eu.janmuller.application.SalesmenApp.model.Page;
import eu.janmuller.application.SalesmenApp.model.Template;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

import java.io.*;
import java.util.List;

/**
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

    private ScaleGestureDetector mScaleDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Inquiry inquiry = (Inquiry) intent.getSerializableExtra(INQUIRY);
        if (inquiry != null) {

            Toast.makeText(this, "Otevrena poptavka " + inquiry.contact, Toast.LENGTH_SHORT).show();
        }

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());

        mWebView.setWebViewClient(new WebViewClient());

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        List<Template> templates = Template.getAllObjects(Template.class);
        Template template = templates.get(0);

        fillSideBar(template);
    }

    private String getBaseUrl(Template template) {

        return Helper.getParentFolderAsFile(template.baseUrl).getPath();
    }

    private void showHtml(Template template, Page page) {

        mWebView.loadUrl("file://" + getBaseUrl(template) + File.separator + page.file);
    }

    private void fillSideBar(final Template template) {

        List<Page> pages = Page.getByQuery(Page.class, "templateId=" + template.id.getId());
        if (pages.size() == 0) {

            return;
        }

        for (final Page page : pages) {

            View view = getLayoutInflater().inflate(R.layout.documentlistview, null);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    showHtml(template, page);
                }
            });

            Bitmap bitmap = BitmapFactory.decodeFile(getBaseUrl(template) + "/" + page.thumbnail);
            ImageView imageView = (ImageView) view.findViewById(R.id.image);
            imageView.setImageBitmap(bitmap);
            mSideBarView.addView(view);
        }

        showHtml(template, pages.get(0));
        mSideBarView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                mScaleDetector.onTouchEvent(motionEvent);
                return true;
            }
        });
    }

    private void groupDocuments(int viewIndex) {


        for (int i = 0; i < mSideBarView.getChildCount(); i++) {

            final View view = mSideBarView.getChildAt(i);
            if (i < 3) {

                view.animate().rotationBy((i - 1) * 10).y(0).setDuration(500).withLayer();
            } else {

                view.animate().alpha(0).setDuration(200).withStartAction(new Runnable() {
                    @Override
                    public void run() {

                        mSideBarView.removeView(view);
                    }
                }).withLayer();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.view_menu, menu);
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

                groupDocuments(1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSendActivity() {

        Intent intent = new Intent(this, SendActivity.class);
        startActivity(intent);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float mScaleFactor = 1.f;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            Ln.i("scale: " + mScaleFactor);
            groupDocuments(0);
            //invalidate();
            return true;
        }
    }
}
