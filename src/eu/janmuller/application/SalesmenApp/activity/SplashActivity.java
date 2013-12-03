package eu.janmuller.application.salesmenapp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.inject.Inject;
import eu.janmuller.application.salesmenapp.Config;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.db.Inquiry;
import eu.janmuller.application.salesmenapp.model.db.Template;
import eu.janmuller.application.salesmenapp.server.DownloadData;
import eu.janmuller.application.salesmenapp.server.ServerService;
import roboguice.RoboGuice;
import roboguice.activity.RoboSplashActivity;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.10.13
 * Time: 12:59
 */
public class SplashActivity extends RoboSplashActivity {

    public static final int STARTUP_DURATION_IN_MS = 1000;
    public static final int STARTUP_PROGRESS_STEP  = 50;

    @Inject
    private DownloadData mDownloadData;

    @Inject
    private ServerService mServerService;

    private ProgressBar mProgressBar;
    private TextView    mTextProgress;
    private TextView    mTextAction;

    private int mLoop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        minDisplayMs = STARTUP_DURATION_IN_MS;
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.splash_activity);

        mTextProgress = (TextView) findViewById(R.id.text_progress);
        mTextAction = (TextView) findViewById(R.id.text_action);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mProgressBar.setMax(STARTUP_DURATION_IN_MS);
        Config.sActualVendor = getString(R.string.vendor);

        final Handler handler = new Handler();
        new Thread() {
            @Override
            public void run() {

                while (mLoop < STARTUP_DURATION_IN_MS) {

                    mLoop += STARTUP_PROGRESS_STEP;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            mProgressBar.setProgress(mLoop);
                        }
                    });
                    try {

                        sleep(STARTUP_PROGRESS_STEP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();
    }

    private void showDeviceIdDialog() {

        final String deviceId = Helper.getUniqueId(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                finish();
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                checkIfPaired();
            }
        });
        builder.setTitle("ID zařízení");
        builder.setMessage(deviceId);
        builder.setCancelable(false);

        builder.create().show();
    }

    @Override
    protected void startNextActivity() {

        RoboGuice.getInjector(this).injectMembers(this);
        checkIfPaired();
    }

    @Override
    protected void andFinishThisOne() {

        // must be empty - dont call super method
    }

    @Override
    protected void onPause() {

        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void checkIfPaired() {

        if (Helper.isPaired(this)) {

            afterRoboGuiceInit();
            return;
        }
        final Handler handler = new Handler();
        new Thread() {
            @Override
            public void run() {

                if (mServerService.isDeviceRegistered()) {

                    Helper.setPaired(SplashActivity.this);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            afterRoboGuiceInit();
                        }
                    });
                } else {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            showDeviceIdDialog();
                        }
                    });
                }
            }
        }.start();
    }

    private void afterRoboGuiceInit() {

        if (Config.sActualVendor.equals(Config.VENDOR_MAFRA)) {

            if (Template.getCountByQuery(Template.class, "1=1") == 0) {

                loadData();
            } else {

                List<Inquiry> list = Inquiry.getAllObjects(Inquiry.class);
                if (list.size() > 0) {

                    InquiryActivityHelper.openViewActivity(this, list.get(0));
                } else {

                    InquiryActivityHelper.deleteAll();
                    loadData();
                }
            }
        } else {

            loadData();
        }
    }


    private void loadData() {

        mDownloadData
                .setDownloadInquiries(Config.sActualVendor.equals(Config.VENDOR_DALLMAYR))
                .run(new DownloadData.IDownloadDataCallback() {
                    @Override
                    public void onInquiriesDownloaded() {
/*
                        if (Config.sActualVendor.equals(Config.VENDOR_DALLMAYR)) {

                            fillInquiriesTable();
                        }*/
                    }

                    @Override
                    public void onDownloadTypeChanged(String action) {

                        mTextAction.setText(action);
                    }

                    @Override
                    public void onProgressUpdate(int total, int progress, String message) {

                        mProgressBar.setProgress(progress);
                        mProgressBar.setMax(total);
                        mTextProgress.setText(message);
                    }

                    @Override
                    public void onNoNewTemplatesFound() {

                        startInquiryActivity();
                    }

                    @Override
                    public void onTemplatesDownloaded() {

                        startInquiryActivity();
                        /*if (sActualVendor.equals(VENDOR_MAFRA)) {

                            List<Inquiry> list = Inquiry.getAllObjects(Inquiry.class);
                            Inquiry inquiry;

                            inquiry = list.size() > 0 ? list.get(0) : new Inquiry();
                            inquiry.title = "Mafra";
                            inquiry.created = InquiriesAdapter.mSdf.format(new Date());
                            inquiry.state = Inquiry.State.NEW;
                            inquiry.save();
                            InquiryActivityHelper.openViewActivity(InquiryListActivity.this, inquiry);
                        }*/
                    }
                });
    }

    private void startInquiryActivity() {

        Intent intent = new Intent(this, InquiryListActivity.class);
        startActivity(intent);
        finish();
    }

}
