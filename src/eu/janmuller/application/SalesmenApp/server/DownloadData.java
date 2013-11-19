package eu.janmuller.application.salesmenapp.server;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.widget.Toast;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.janmuller.application.salesmenapp.model.db.*;
import roboguice.inject.ContextSingleton;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 15.11.13
 * Time: 13:47
 */
@ContextSingleton
public class DownloadData {

    public enum Type {

        TEMPLATES_JSON,
        TEMPLATES_DATA,
        INQUIRIES_JSON
    }

    private boolean mDownloadInquiries = true;

    @Inject
    private Context mContext;

    @Inject
    private DownloadService mDownloadService;

    private Template[]            mTemplates;
    private IDownloadDataCallback mDownloadDataCallback;

    public DownloadData setDownloadInquiries(boolean downloadInquiries) {

        mDownloadInquiries = downloadInquiries;
        return this;
    }

    public void run(IDownloadDataCallback downloadDataCallback) {

        mDownloadDataCallback = downloadDataCallback;
        download();
    }

    private void download() {

        if (mDownloadInquiries) {

            new DownloadTask(mContext, Type.INQUIRIES_JSON, new ITaskCompleteCallback() {
                @Override
                public void onTaskComplete(Object result) {

                    mDownloadDataCallback.onInquiriesDownloaded();
                }
            }).execute();
        }

        new DownloadTask(mContext, Type.TEMPLATES_JSON, new ITaskCompleteCallback() {
            @Override
            public void onTaskComplete(Object result) {

                if (result instanceof Integer) {

                    int size = (Integer) result;
                    if (size > 0) {

                        showDownloadTemplateDataDialog(size);
                    }
                }
            }
        }).execute();
    }

    private void showDownloadTemplateDataDialog(int dataSize) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setNegativeButton("Později", null);
        builder.setPositiveButton("Aktualizovat teď", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                new DownloadTask(mContext, Type.TEMPLATES_DATA, new ITaskCompleteCallback() {
                    @Override
                    public void onTaskComplete(Object result) {

                        mDownloadDataCallback.onTemplatesDownloaded();
                    }
                }).execute();
            }
        });
        builder.setTitle("Aktualizovat šablony");

        double dataSizeInKb = dataSize / 1000d;
        String formattedSize = (int) dataSizeInKb + " kB";
        String formattedDuration = "";
        double linkSpeed = 0;//getLinkSpeedInKb();
        if (linkSpeed > 0) {

            double downloadDurationInSec = dataSizeInKb / linkSpeed;
            formattedDuration = "Bude to trvat zhruba " + downloadDurationInSec + " sekund.";
        }
        builder.setMessage("Jsou k dispozici nové verze dokumentů. Celková velikost souborů je " + formattedSize + ". " + formattedDuration);

        builder.create().show();
    }

    public class DownloadTask extends RoboAsyncTask<Object> {

        private ProgressDialog mProgressDialog;
        private Type           mType;
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
        public Object call() throws Exception {

            Object result = null;
            switch (mType) {

                case INQUIRIES_JSON:

                    setTitle("Stahuji poptávky");
                    mDownloadService.downloadAndSaveInquiries(mProgressCallback);
                    break;
                case TEMPLATES_JSON:


                    setTitle("Stahuji definici šablon");
                    mTemplates = mDownloadService.downloadTemplatesJson(mProgressCallback);
                    result = getDataSize(mTemplates);
                    break;
                case TEMPLATES_DATA:

                    setTitle("Stahuji šablony");
                    mDownloadService.downloadTemplatesData(mTemplates, mProgressCallback);
                    break;
            }

            return result;
        }

        public int getDataSize(Template[] templates) {

            int size = 0;
            for (Template template : templates) {

                if (template.type.equals(Template.Type.DOCUMENT.label)) {

                    size += template.dataSize;
                }
            }

            return size;
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
        protected void onException(Exception e) throws RuntimeException {

            if (mProgressDialog != null && mProgressDialog.isShowing()) {

                mProgressDialog.dismiss();
            }

            Toast.makeText(mContext, "Během stahování došlo k chybě", Toast.LENGTH_SHORT).show();
            Ln.e(e);
        }

        @Override
        protected void onSuccess(Object o) throws Exception {

            if (mProgressDialog != null && mProgressDialog.isShowing()) {

                mProgressDialog.dismiss();
            }

            if (mTaskCompleteCallback != null) {

                mTaskCompleteCallback.onTaskComplete(o);
            }
        }
    }

    private void deleteAll() {

        TemplateTag.deleteAll(TemplateTag.class);
        DocumentTag.deleteAll(DocumentTag.class);
        TemplatePage.deleteAll(TemplatePage.class);
        DocumentPage.deleteAll(DocumentPage.class);
        Template.deleteAll(Template.class);
        Document.deleteAll(Document.class);
        Inquiry.deleteAll(Inquiry.class);
    }

    interface IProgressCallback {

        public void onProgressUpdate(String text, int actual, int total);
    }

    interface ITaskCompleteCallback {

        public void onTaskComplete(Object object);
    }

    public interface IDownloadDataCallback {

        public void onInquiriesDownloaded();

        public void onTemplatesDownloaded();
    }
}
