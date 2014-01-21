package eu.janmuller.application.salesmenapp.server;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.janmuller.application.salesmenapp.model.db.*;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 15.11.13
 * Time: 13:47
 */
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

    public void run(IDownloadDataCallback downloadDataCallback, boolean downloadInquiries) {

        mDownloadDataCallback = downloadDataCallback;
        mDownloadInquiries = downloadInquiries;
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

                int size = getDataSize(mTemplates);
                if (size > 0) {

                    showDownloadTemplateDataDialog(size);
                } else {

                    mDownloadDataCallback.onTemplatesDownloaded();
                }
            }
        }).execute();
    }

    private void showDownloadTemplateDataDialog(int dataSize) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setNegativeButton("Později", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mDownloadDataCallback != null) {

                    mDownloadDataCallback.onTemplatesDownloadPostponed();
                }
            }
        });
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
        builder.setTitle("Aktualizace šablon");

        int dataSizeInKb = dataSize / 1024;
        String formattedSize = dataSizeInKb + "kB";
        builder.setMessage("Jsou k dispozici nové verze dokumentů. Celková velikost souborů je " + formattedSize + ".");
        builder.create().show();
    }

    public class DownloadTask extends RoboAsyncTask<Object> {

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

        IProgressCallback mProgressCallback = new IProgressCallback() {
            @Override
            public void onProgressUpdate(final String text, final int actual, final int total) {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        mDownloadDataCallback.onProgressUpdate(total, actual, text);
                    }
                });
            }
        };

        @Override
        public Object call() throws Exception {

            Object result = null;
            switch (mType) {

                case INQUIRIES_JSON:

                    setTitle("Stahuji poptávky: ");
                    mDownloadService.downloadAndSaveInquiries(mProgressCallback);
                    break;
                case TEMPLATES_JSON:


                    setTitle("Stahuji definici šablon: ");
                    mTemplates = mDownloadService.downloadTemplatesJson(mProgressCallback);
                    break;
                case TEMPLATES_DATA:

                    setTitle("Stahuji šablony: ");
                    mDownloadService.downloadTemplatesData(mTemplates, mProgressCallback);
                    break;
            }

            return result;
        }


        private void setTitle(final String title) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    mDownloadDataCallback.onDownloadTypeChanged(title);
                }
            });
        }

        @Override
        protected void onException(Exception e) throws RuntimeException {

            //Toast.makeText(mContext, "Během stahování došlo k chybě", Toast.LENGTH_SHORT).show();
            Ln.e(e);

            if (mTaskCompleteCallback != null) {

                mTaskCompleteCallback.onTaskComplete(null);
            }
        }

        @Override
        protected void onSuccess(Object o) throws Exception {

            if (mTaskCompleteCallback != null) {

                mTaskCompleteCallback.onTaskComplete(o);
            }
        }
    }

    public int getDataSize(Template[] templates) {

        int size = 0;
        if (templates == null) {

            return size;
        }
        for (Template template : templates) {

            if (template.type.equals(Template.Type.DOCUMENT.label)) {

                size += template.dataSize;
            }
        }

        return size;
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

        public void onTemplatesDownloadPostponed();

        public void onProgressUpdate(int total, int progress, String message);

        public void onDownloadTypeChanged(String action);
    }
}
