package eu.janmuller.application.salesmenapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import com.google.inject.Inject;
import eu.janmuller.application.salesmenapp.model.Inquiry;
import eu.janmuller.application.salesmenapp.model.Page;
import eu.janmuller.application.salesmenapp.model.Tag;
import eu.janmuller.application.salesmenapp.model.Template;
import roboguice.util.RoboAsyncTask;

public class DownloadTask extends RoboAsyncTask<Void> {

    public enum Type {

        TEMPLATES,
        INQUIRIES
    }

    @Inject
    private DownloadService mDownloadService;

    private ProgressDialog  mProgressDialog;
    private Type mType;
    private final Handler mHandler = new Handler();
    private ITaskCompleteCallback mTaskCompleteCallback;

    public DownloadTask(Context context, Type type) {

        super(context);
        this.mType = type;
    }
    public DownloadTask(Context context, Type type, ITaskCompleteCallback taskCompleteCallback) {

        this(context, type);
        this.mTaskCompleteCallback = taskCompleteCallback;
    }

    @Override
    protected void onPreExecute() throws Exception {

        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage("");
        mProgressDialog.setTitle("Stahuji data");
        mProgressDialog.show();
    }

    IProgressCallback mProgressCallback = new IProgressCallback() {
        @Override
        public void onProgressUpdate(final String text, final int actual, final int total) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    mProgressDialog.setProgress(actual);
                    mProgressDialog.setMax(total);
                    mProgressDialog.setMessage(text);
                }
            });
        }
    };

    @Override
    public Void call() throws Exception {

        switch (mType) {

            case INQUIRIES:

                Inquiry.deleteAll(Inquiry.class);
                setTitle("Stahuji poptávky");
                mDownloadService.downloadAndSaveInquiries(mProgressCallback);
                break;
            case TEMPLATES:

                Tag.deleteAll(Tag.class);
                Page.deleteAll(Page.class);
                Template.deleteAll(Template.class);
                setTitle("Stahuji šablony");
                mDownloadService.downloadTemplates(mProgressCallback);
                break;
        }

        return null;
    }

    private void setTitle(final String title) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                mProgressDialog.setTitle(title);
            }
        });
    }

    @Override
    protected void onFinally() throws RuntimeException {

        if (mProgressDialog != null && mProgressDialog.isShowing()) {

            mProgressDialog.dismiss();
        }

        if (mTaskCompleteCallback != null) {

            mTaskCompleteCallback.onTaskComplete();
        }
    }

    public interface IProgressCallback {

        public void onProgressUpdate(String text, int actual, int total);
    }

    public interface ITaskCompleteCallback {

        public void onTaskComplete();
    }
}